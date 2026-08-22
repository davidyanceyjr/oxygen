package com.oxygen.weather.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.GeocodingLocationCandidate
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.provider.GeocodingError
import com.oxygen.weather.core.provider.GeocodingRepository
import com.oxygen.weather.core.provider.GeocodingRepositoryResult
import java.time.ZoneId
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class FirstRunLocationStateHolderTest {
    @Test
    fun `default app state starts at first-run manual location entry`() {
        val stateHolder = OxygenAppStateHolder()

        val state = stateHolder.presentationState

        assertTrue(state.screen is OxygenAppScreen.FirstRunLocationEntry)
        assertNull(state.selectedLocation)
        assertFalse(state.isShowingHome)
        assertFalse(state.usesScaffoldWeather)
    }

    @Test
    fun `manual search submission uses repository results without requesting permission or routing home`() {
        val repository = RecordingGeocodingRepository(
            listOf(
                GeocodingRepositoryResult.Loading,
                GeocodingRepositoryResult.Success(
                    listOf(
                        candidate(
                            id = "oxygen-location-1",
                            name = "Madison",
                            admin = listOf("Wisconsin"),
                            country = "United States",
                            latitude = 43.0731,
                            longitude = -89.4012,
                            zoneId = "America/Chicago",
                        ),
                    ),
                ),
            ),
        )
        val stateHolder = OxygenAppStateHolder(
            geocodingRepository = repository,
            searchExecutor = DirectExecutor,
        )

        stateHolder.onManualLocationQueryChanged("Madison")
        stateHolder.onManualLocationSearchSubmitted()

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals("Madison", firstRunState.query)
        assertEquals("Madison", firstRunState.submittedQuery)
        assertTrue(firstRunState.searchState is ManualLocationSearchState.Results)
        val results = firstRunState.searchState as ManualLocationSearchState.Results
        assertEquals("Madison", repository.queries.single())
        assertEquals("Madison", results.query)
        assertEquals("Madison", results.candidates.single().title)
        assertEquals("Wisconsin, United States", results.candidates.single().subtitle)
        assertEquals("43.0731, -89.4012", results.candidates.single().coordinateText)
        assertEquals("America/Chicago", results.candidates.single().timezoneText)
        assertNull(stateHolder.consumeNextCommand())
        assertNull(stateHolder.presentationState.selectedLocation)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }

    @Test
    fun `manual search trims submitted query but retains typed query`() {
        val repository = RecordingGeocodingRepository(listOf(GeocodingRepositoryResult.Empty))
        val stateHolder = OxygenAppStateHolder(
            geocodingRepository = repository,
            searchExecutor = DirectExecutor,
        )

        stateHolder.onManualLocationQueryChanged("  Madison, WI  ")
        stateHolder.onManualLocationSearchSubmitted()

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals("  Madison, WI  ", firstRunState.query)
        assertEquals("Madison, WI", firstRunState.submittedQuery)
        assertEquals("Madison, WI", repository.queries.single())
        assertTrue(firstRunState.searchState is ManualLocationSearchState.Empty)
    }

    @Test
    fun `empty manual search asks for a place without requesting permission or routing home`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onManualLocationQueryChanged(" ")
        stateHolder.onManualLocationSearchSubmitted()

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals(FirstRunLocationMessage.EnterPlaceName, firstRunState.message)
        assertNull(firstRunState.submittedQuery)
        assertTrue(firstRunState.searchState is ManualLocationSearchState.Idle)
        assertNull(stateHolder.consumeNextCommand())
        assertFalse(stateHolder.presentationState.isShowingHome)
    }

    @Test
    fun `manual search shows retryable failure and retry reuses retained query`() {
        val repository = RecordingGeocodingRepository(
            listOf(GeocodingRepositoryResult.Failure(GeocodingError.NetworkUnavailable)),
            listOf(
                GeocodingRepositoryResult.Success(
                    listOf(candidate(id = "oxygen-location-2", name = "Madison", country = "United States")),
                ),
            ),
        )
        val stateHolder = OxygenAppStateHolder(
            geocodingRepository = repository,
            searchExecutor = DirectExecutor,
        )

        stateHolder.onManualLocationQueryChanged("Madison")
        stateHolder.onManualLocationSearchSubmitted()

        val failed = (stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry)
            .searchState as ManualLocationSearchState.Failure
        assertEquals("Madison", failed.query)
        assertTrue(failed.canRetry)
        assertEquals(FirstRunLocationMessage.SearchNetworkUnavailable, failed.message)

        stateHolder.onManualLocationSearchRetry()

        val results = (stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry)
            .searchState as ManualLocationSearchState.Results
        assertEquals(listOf("Madison", "Madison"), repository.queries)
        assertEquals("Madison", results.query)
    }

    @Test
    fun `all repository failure classes map to visible provider-neutral messages`() {
        val errors = listOf(
            GeocodingError.InvalidQuery to FirstRunLocationMessage.EnterPlaceName,
            GeocodingError.NetworkUnavailable to FirstRunLocationMessage.SearchNetworkUnavailable,
            GeocodingError.RateLimited("open-meteo") to FirstRunLocationMessage.SearchRateLimited,
            GeocodingError.ProviderUnavailable("open-meteo") to FirstRunLocationMessage.SearchProviderUnavailable,
            GeocodingError.InvalidResponse("open-meteo") to FirstRunLocationMessage.SearchInvalidResponse,
            GeocodingError.ProviderRejectedRequest("open-meteo") to FirstRunLocationMessage.SearchRejected,
            GeocodingError.UnexpectedProviderFailure("open-meteo") to FirstRunLocationMessage.SearchUnexpectedFailure,
        )

        errors.forEach { (error, expectedMessage) ->
            val stateHolder = OxygenAppStateHolder(
                geocodingRepository = RecordingGeocodingRepository(
                    listOf(GeocodingRepositoryResult.Failure(error)),
                ),
                searchExecutor = DirectExecutor,
            )

            stateHolder.onManualLocationQueryChanged("Madison")
            stateHolder.onManualLocationSearchSubmitted()

            val failure = (stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry)
                .searchState as ManualLocationSearchState.Failure
            assertEquals(expectedMessage, failure.message)
            assertTrue(failure.message.text.isNotBlank())
        }
    }

    @Test
    fun `selecting manual search result routes home loading for exact weather location`() {
        val candidate = candidate(
            id = "oxygen-location-stable",
            name = "Springfield",
            admin = listOf("Illinois", "Sangamon County"),
            country = "United States",
        )
        val stateHolder = OxygenAppStateHolder(
            geocodingRepository = RecordingGeocodingRepository(
                listOf(GeocodingRepositoryResult.Success(listOf(candidate))),
            ),
            searchExecutor = DirectExecutor,
        )

        stateHolder.onManualLocationQueryChanged("Springfield")
        stateHolder.onManualLocationSearchSubmitted()
        val result = ((stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry)
            .searchState as ManualLocationSearchState.Results).candidates.single()
        stateHolder.onManualLocationCandidateSelected(result.id)

        val home = stateHolder.presentationState.screen as OxygenAppScreen.Home
        assertSame(result.location, stateHolder.presentationState.selectedLocation)
        assertSame(result.location, home.loading.location)
        assertEquals(candidate.location, home.loading.location)
        assertEquals("Springfield", home.loading.title)
        assertTrue(home.loading.subtitle.contains("39.7817, -89.6501"))
        assertTrue(home.loading.subtitle.contains("America/Chicago"))
        assertTrue(home.loading.statusText.contains("Springfield"))
        assertTrue(stateHolder.presentationState.isShowingHome)
        assertNull(stateHolder.consumeNextCommand())
    }

    @Test
    fun `late manual search emissions after selection cannot undo home handoff`() {
        val selectedCandidate = candidate(
            id = "oxygen-location-selected",
            name = "Madison",
            country = "United States",
        )
        val lateCandidate = candidate(
            id = "oxygen-location-late",
            name = "Late Result",
            country = "United States",
        )
        val repository = ControlledGeocodingRepository()
        val executor = Executors.newSingleThreadExecutor()
        val stateHolder = OxygenAppStateHolder(
            geocodingRepository = repository,
            searchExecutor = executor,
        )

        try {
            stateHolder.onManualLocationQueryChanged("Madison")
            stateHolder.onManualLocationSearchSubmitted()
            repository.emit(GeocodingRepositoryResult.Success(listOf(selectedCandidate)))
            val result = awaitResults(stateHolder).candidates.single()
            stateHolder.onManualLocationCandidateSelected(result.id)

            repository.emit(GeocodingRepositoryResult.Success(listOf(lateCandidate)))
            repository.finish()
            executor.shutdown()
            assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS))

            val home = stateHolder.presentationState.screen as OxygenAppScreen.Home
            assertSame(result.location, stateHolder.presentationState.selectedLocation)
            assertSame(result.location, home.loading.location)
            assertEquals("Madison", home.loading.title)
            assertTrue(stateHolder.presentationState.isShowingHome)
        } finally {
            repository.finish()
            executor.shutdownNow()
        }
    }

    @Test
    fun `older submitted query results cannot replace newer first-run search state`() {
        val repository = RecordingGeocodingRepository(
            listOf(
                GeocodingRepositoryResult.Success(
                    listOf(candidate(id = "oxygen-location-old", name = "Madison", country = "United States")),
                ),
            ),
            listOf(
                GeocodingRepositoryResult.Success(
                    listOf(candidate(id = "oxygen-location-new", name = "Madrid", country = "Spain")),
                ),
            ),
        )
        val executor = QueuedExecutor()
        val stateHolder = OxygenAppStateHolder(
            geocodingRepository = repository,
            searchExecutor = executor,
        )

        stateHolder.onManualLocationQueryChanged("Madison")
        stateHolder.onManualLocationSearchSubmitted()
        stateHolder.onManualLocationQueryChanged("Madrid")
        stateHolder.onManualLocationSearchSubmitted()
        executor.runNext()
        executor.runNext()

        val results = (stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry)
            .searchState as ManualLocationSearchState.Results
        assertEquals("Madrid", results.query)
        assertEquals("Madrid", results.candidates.single().title)
        assertNull(stateHolder.presentationState.selectedLocation)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }

    @Test
    fun `long selected place name remains in home loading presentation state`() {
        val longName = "A Very Long Municipality Name With Several Districts And Administrative Qualifiers"
        val candidate = candidate(
            id = "oxygen-location-long",
            name = longName,
            admin = listOf("A Long Region Name", "A Longer County Name"),
            country = "United States",
        )
        val stateHolder = OxygenAppStateHolder(
            geocodingRepository = RecordingGeocodingRepository(
                listOf(GeocodingRepositoryResult.Success(listOf(candidate))),
            ),
            searchExecutor = DirectExecutor,
        )

        stateHolder.onManualLocationQueryChanged(longName)
        stateHolder.onManualLocationSearchSubmitted()
        val result = ((stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry)
            .searchState as ManualLocationSearchState.Results).candidates.single()
        stateHolder.onManualLocationCandidateSelected(result.id)

        val loading = (stateHolder.presentationState.screen as OxygenAppScreen.Home).loading
        assertSame(result.location, loading.location)
        assertEquals(longName, loading.title)
        assertTrue(loading.statusText.contains(longName))
        assertTrue(loading.subtitle.contains("America/Chicago"))
    }

    @Test
    fun `use my location emits one provider-neutral permission request command`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onUseMyLocation()

        assertEquals(OxygenAppCommand.RequestLocationPermission, stateHolder.consumeNextCommand())
        assertNull(stateHolder.consumeNextCommand())
        assertTrue(stateHolder.presentationState.screen is OxygenAppScreen.FirstRunLocationEntry)
        assertNull(stateHolder.presentationState.selectedLocation)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }

    @Test
    fun `denied location permission remains on manual entry with optional-location message`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onUseMyLocation()
        stateHolder.consumeNextCommand()
        stateHolder.onLocationPermissionResult(LocationPermissionResult.Denied)

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals(FirstRunLocationMessage.LocationPermissionOptional, firstRunState.message)
        assertNull(stateHolder.presentationState.selectedLocation)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }

    @Test
    fun `unavailable location permission remains on manual entry with optional-location message`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onLocationPermissionResult(LocationPermissionResult.Unavailable)

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals(FirstRunLocationMessage.LocationPermissionOptional, firstRunState.message)
        assertNull(stateHolder.presentationState.selectedLocation)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }
}

private object DirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class QueuedExecutor : Executor {
    private val commands = ArrayDeque<Runnable>()

    override fun execute(command: Runnable) {
        commands += command
    }

    fun runNext() {
        commands.removeFirst().run()
    }
}

private class RecordingGeocodingRepository(
    private vararg val responses: List<GeocodingRepositoryResult>,
) : GeocodingRepository {
    val queries = mutableListOf<String>()
    private var callIndex = 0

    override fun search(
        query: String,
        count: Int,
        language: String?,
        countryCode: String?,
    ): Sequence<GeocodingRepositoryResult> {
        queries += query
        val response = responses.getOrElse(callIndex) { responses.last() }
        callIndex += 1
        return response.asSequence()
    }
}

private class ControlledGeocodingRepository : GeocodingRepository {
    private val results = LinkedBlockingQueue<ControlledResult>()

    override fun search(
        query: String,
        count: Int,
        language: String?,
        countryCode: String?,
    ): Sequence<GeocodingRepositoryResult> = sequence {
        while (true) {
            when (val result = results.take()) {
                ControlledResult.Finished -> return@sequence
                is ControlledResult.Next -> yield(result.value)
            }
        }
    }

    fun emit(result: GeocodingRepositoryResult) {
        results += ControlledResult.Next(result)
    }

    fun finish() {
        results += ControlledResult.Finished
    }
}

private sealed interface ControlledResult {
    data class Next(val value: GeocodingRepositoryResult) : ControlledResult
    data object Finished : ControlledResult
}

private fun awaitResults(stateHolder: OxygenAppStateHolder): ManualLocationSearchState.Results {
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
    while (System.nanoTime() < deadline) {
        val results = (stateHolder.presentationState.screen as? OxygenAppScreen.FirstRunLocationEntry)
            ?.searchState as? ManualLocationSearchState.Results
        if (results != null) return results
        Thread.sleep(10)
    }
    error("Timed out waiting for manual location search results")
}

private fun candidate(
    id: String,
    name: String,
    admin: List<String> = emptyList(),
    country: String = "United States",
    latitude: Double = 39.7817,
    longitude: Double = -89.6501,
    zoneId: String = "America/Chicago",
): GeocodingLocationCandidate =
    GeocodingLocationCandidate(
        locationId = LocationId(id),
        displayName = name,
        point = GeoPoint(latitude, longitude),
        zoneId = ZoneId.of(zoneId),
        country = country,
        countryCode = "US",
        administrativeAreas = admin,
    )

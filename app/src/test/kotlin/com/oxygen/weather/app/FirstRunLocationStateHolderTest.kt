package com.oxygen.weather.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

class FirstRunLocationStateHolderTest {
    @Test
    fun `default app state starts at first-run manual location entry`() {
        val stateHolder = OxygenAppStateHolder()

        val state = stateHolder.presentationState

        assertTrue(state.screen is OxygenAppScreen.FirstRunLocationEntry)
        assertNull(state.selectedLocation)
        assertFalse(state.isShowingHome)
        assertFalse(state.usesSampleWeather)
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
    fun `selecting manual search result records exact weather location without routing home`() {
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

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals(candidate.location, stateHolder.presentationState.selectedLocation)
        assertEquals(FirstRunLocationMessage.LocationSelectedForNextSlice, firstRunState.message)
        assertFalse(stateHolder.presentationState.isShowingHome)
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

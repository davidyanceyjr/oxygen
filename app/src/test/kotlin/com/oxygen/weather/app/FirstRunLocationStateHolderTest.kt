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
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.location.SavedLocationStorage
import com.oxygen.weather.core.provider.CoordinateTimeZoneResolver
import com.oxygen.weather.core.provider.CoordinateTimeZoneResult
import com.oxygen.weather.core.provider.CoordinateTimeZoneError
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
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
            weatherRepository = NeverCompletingWeatherRepository,
            forecastExecutor = DirectExecutor,
        )

        stateHolder.onManualLocationQueryChanged("Springfield")
        stateHolder.onManualLocationSearchSubmitted()
        val result = ((stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry)
            .searchState as ManualLocationSearchState.Results).candidates.single()
        stateHolder.onManualLocationCandidateSelected(result.id)

        val home = stateHolder.presentationState.screen as OxygenAppScreen.Home
        val loading = home.forecast as HomeForecastPresentationState.Loading
        assertSame(result.location, stateHolder.presentationState.selectedLocation)
        assertSame(result.location, loading.location)
        assertEquals(candidate.location, loading.location)
        assertEquals("Springfield", loading.title)
        assertTrue(loading.subtitle.contains("39.7817, -89.6501"))
        assertTrue(loading.subtitle.contains("America/Chicago"))
        assertTrue(loading.statusText.contains("Springfield"))
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
            weatherRepository = NeverCompletingWeatherRepository,
            forecastExecutor = DirectExecutor,
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
            val loading = home.forecast as HomeForecastPresentationState.Loading
            assertSame(result.location, stateHolder.presentationState.selectedLocation)
            assertSame(result.location, loading.location)
            assertEquals("Madison", loading.title)
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
            weatherRepository = NeverCompletingWeatherRepository,
            forecastExecutor = DirectExecutor,
        )

        stateHolder.onManualLocationQueryChanged(longName)
        stateHolder.onManualLocationSearchSubmitted()
        val result = ((stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry)
            .searchState as ManualLocationSearchState.Results).candidates.single()
        stateHolder.onManualLocationCandidateSelected(result.id)

        val loading = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.Loading
        assertSame(result.location, loading.location)
        assertEquals(longName, loading.title)
        assertTrue(loading.statusText.contains(longName))
        assertTrue(loading.subtitle.contains("America/Chicago"))
    }

    @Test
    fun `use my location emits one provider-neutral permission request command`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onUseMyLocation()

        assertEquals(OxygenAppCommand.RequestLocationPermission(1), stateHolder.consumeNextCommand())
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
        stateHolder.onLocationPermissionResult(1, LocationPermissionResult.Denied)

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals(FirstRunLocationMessage.LocationPermissionOptional, firstRunState.message)
        assertNull(stateHolder.presentationState.selectedLocation)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }

    @Test
    fun `unavailable location permission remains on manual entry with optional-location message`() {
        val stateHolder = OxygenAppStateHolder()

        stateHolder.onUseMyLocation()
        stateHolder.onLocationPermissionResult(1, LocationPermissionResult.Unavailable)

        val firstRunState = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals(FirstRunLocationMessage.LocationPermissionOptional, firstRunState.message)
        assertNull(stateHolder.presentationState.selectedLocation)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }
    @Test fun `launch manual search refresh and unsolicited permission do not acquire device point`() {
        val h = DeviceHarness()
        h.holder.onLocationPermissionResult(1, LocationPermissionResult.Granted)
        h.holder.onManualLocationQueryChanged("Madison")
        h.holder.onManualLocationSearchSubmitted()
        h.holder.onManualLocationCandidateSelected(h.manual.locationId)
        h.holder.onHomeForecastRefresh()
        assertEquals(0, h.sourceCalls)
        assertNull(h.holder.consumeNextCommand())
        assertEquals(h.manual.location, h.storage.selected)
    }

    @Test fun `device success commits original point before Home and does not save a place`() {
        val h = DeviceHarness()
        h.grant()
        assertEquals(DeviceLocationProgress.Locating, h.entry().deviceProgress)
        h.pointCallback(DeviceLocationResult.Success(h.point))
        assertEquals(h.point, h.storage.selected?.point)
        assertEquals("America/Chicago", h.storage.selected?.zoneId?.id)
        assertEquals("Approximate device location", h.storage.selected?.displayName)
        assertNull(h.storage.selected?.elevationMeters)
        assertTrue(h.storage.selected!!.id.value.startsWith("device-"))
        assertEquals(listOf("write", "forecast"), h.events)
        assertEquals(h.storage.selected, h.holder.presentationState.selectedLocation)
        assertEquals(emptyList<WeatherLocation>(), h.saved.listLocations())
        assertTrue(h.holder.presentationState.isShowingHome)
    }

    @Test fun `duplicate taps permission and fixes coalesce into one attempt`() {
        val h = DeviceHarness()
        h.holder.onUseMyLocation()
        h.holder.onUseMyLocation()
        val command = h.holder.consumeNextCommand() as OxygenAppCommand.RequestLocationPermission
        assertNull(h.holder.consumeNextCommand())
        h.holder.onLocationPermissionResult(command.attempt, LocationPermissionResult.Granted)
        h.holder.onLocationPermissionResult(command.attempt, LocationPermissionResult.Granted)
        h.holder.onUseMyLocation()
        assertNull(h.holder.consumeNextCommand())
        h.pointCallback(DeviceLocationResult.Success(h.point))
        h.pointCallback(DeviceLocationResult.Success(h.point))
        assertEquals(1, h.sourceCalls)
        assertEquals(listOf("write", "forecast"), h.events)
    }

    @Test fun `denial timeout unavailable resolver and storage failure preserve manual results and old selection`() {
        listOf("denied", "timeout", "unavailable", "resolver", "storage").forEach { outcome ->
            val h = DeviceHarness()
            h.holder.onManualLocationQueryChanged("Madison")
            h.holder.onManualLocationSearchSubmitted()
            h.holder.onManualLocationCandidateSelected(h.manual.locationId)
            h.holder.onChangeLocation()
            h.holder.onManualLocationQueryChanged("Madison")
            h.holder.onManualLocationSearchSubmitted()
            val before = h.entry()
            h.events.clear()
            h.holder.onUseMyLocation()
            val command = h.holder.consumeNextCommand() as OxygenAppCommand.RequestLocationPermission
            if (outcome == "denied") {
                h.holder.onLocationPermissionResult(command.attempt, LocationPermissionResult.Denied)
            } else {
                h.holder.onLocationPermissionResult(command.attempt, LocationPermissionResult.Granted)
                if (outcome == "resolver") h.resolved = CoordinateTimeZoneResult.Failure(CoordinateTimeZoneError.InvalidResponse)
                if (outcome == "storage") h.storage.fail = true
                h.pointCallback(when (outcome) {
                    "timeout" -> DeviceLocationResult.TimedOut
                    "unavailable" -> DeviceLocationResult.Unavailable
                    else -> DeviceLocationResult.Success(h.point)
                })
            }
            assertEquals(outcome, before.query, h.entry().query)
            assertEquals(before.searchState, h.entry().searchState)
            assertEquals(h.manual.location, h.holder.presentationState.selectedLocation)
            assertEquals(h.manual.location, h.storage.selected)
            assertTrue(h.entry().message != null)
            assertNull(h.entry().deviceProgress)
            assertTrue(h.events.isEmpty())
        }
    }

    @Test fun `late permission from cancelled attempt cannot start newer attempt`() {
        val h = DeviceHarness()
        h.holder.onUseMyLocation()
        val old = h.holder.consumeNextCommand() as OxygenAppCommand.RequestLocationPermission
        h.holder.cancelDeviceLocation()
        h.holder.onUseMyLocation()
        val current = h.holder.consumeNextCommand() as OxygenAppCommand.RequestLocationPermission
        h.holder.onLocationPermissionResult(old.attempt, LocationPermissionResult.Granted)
        assertEquals(0, h.sourceCalls)
        h.holder.onLocationPermissionResult(current.attempt, LocationPermissionResult.Granted)
        assertEquals(1, h.sourceCalls)
    }

    @Test fun `manual search saved selection Back About cancel and activity stop invalidate late fix`() {
        listOf("query", "search", "manual", "saved", "back", "about", "cancel", "stop").forEach { action ->
            val h = DeviceHarness()
            h.holder.onManualLocationQueryChanged("Madison")
            h.holder.onManualLocationSearchSubmitted()
            h.saved.saveLocation(h.manual.location)
            h.grant()
            h.exit(action)
            val before = h.holder.presentationState
            val stored = h.storage.selected
            h.pointCallback(DeviceLocationResult.Success(h.point))
            h.pointCallback(DeviceLocationResult.TimedOut)
            assertEquals(action, before, h.holder.presentationState)
            assertEquals(stored, h.storage.selected)
            assertEquals(1, h.cancellations)
        }
    }

    @Test fun `manual saved and exit while timezone resolving win before any device write`() {
        listOf("manual", "saved", "back", "about", "cancel", "stop", "search").forEach { action ->
            val h = DeviceHarness()
            h.holder.onManualLocationQueryChanged("Madison")
            h.holder.onManualLocationSearchSubmitted()
            h.saved.saveLocation(h.manual.location)
            h.duringResolve = {
                assertEquals(DeviceLocationProgress.Resolving, h.entry().deviceProgress)
                h.exit(action)
            }
            h.grant()
            h.pointCallback(DeviceLocationResult.Success(h.point))
            assertFalse(action, h.storage.selected?.id?.value?.startsWith("device-") == true)
            if (action in listOf("manual", "saved")) assertEquals(h.manual.location, h.storage.selected)
        }
    }

    @Test fun `resolver cannot substitute grid point or invalid fixed offset zone`() {
        val h = DeviceHarness()
        h.resolved = CoordinateTimeZoneResult.Success(GeoPoint(0.0, 0.0), ZoneId.of("America/Chicago"))
        h.grant()
        h.pointCallback(DeviceLocationResult.Success(h.point))
        assertNull(h.storage.selected)
        assertEquals(FirstRunLocationMessage.DeviceTimezoneUnavailable, h.entry().message)
        h.resolved = CoordinateTimeZoneResult.Success(h.point, ZoneId.of("+02:00"))
        h.grant()
        h.pointCallback(DeviceLocationResult.Success(h.point))
        assertNull(h.storage.selected)
    }

    @Test fun `concurrent resolver return after selection B cannot persist or publish device A`() {
        listOf("manual", "saved").forEach { selection ->
            val executor = Executors.newSingleThreadExecutor()
            val entered = java.util.concurrent.CountDownLatch(1)
            val release = java.util.concurrent.CountDownLatch(1)
            try {
                val h = DeviceHarness(executor)
                h.holder.onManualLocationQueryChanged("Madison")
                h.holder.onManualLocationSearchSubmitted()
                h.saved.saveLocation(h.manual.location)
                h.duringResolve = {
                    entered.countDown()
                    check(release.await(5, TimeUnit.SECONDS))
                }
                h.grant()
                h.pointCallback(DeviceLocationResult.Success(h.point))
                assertTrue(entered.await(5, TimeUnit.SECONDS))
                h.exit(selection)
                val before = h.holder.presentationState
                release.countDown()
                executor.submit { }.get(5, TimeUnit.SECONDS)
                assertEquals(h.manual.location, h.storage.selected)
                assertEquals(before, h.holder.presentationState)
                assertEquals(listOf("write", "forecast"), h.events)
            } finally {
                release.countDown()
                executor.shutdownNow()
            }
        }
    }
}

/** All device points in this harness are controlled input. */
private class DeviceHarness(deviceExecutor: Executor = DirectExecutor) {
    val point = GeoPoint(43.0731, -89.4012)
    val manual = candidate(id = "manual-madison", name = "Madison", country = "United States")
    val events = mutableListOf<String>()
    var sourceCalls = 0
    var cancellations = 0
    lateinit var pointCallback: (DeviceLocationResult) -> Unit
    var duringResolve: () -> Unit = {}
    var resolved: CoordinateTimeZoneResult = CoordinateTimeZoneResult.Success(point, ZoneId.of("America/Chicago"))
    val storage = DeviceTestStorage(events)
    val saved = object : SavedLocationStorage {
        val rows = mutableListOf<WeatherLocation>()
        override fun listLocations() = rows.toList()
        override fun saveLocation(location: WeatherLocation) { rows += location }
        override fun removeLocation(locationId: LocationId) { rows.removeAll { it.id == locationId } }
    }
    val holder = OxygenAppStateHolder(
        selectedLocationStorage = storage,
        savedLocationStorage = saved,
        geocodingRepository = RecordingGeocodingRepository(listOf(GeocodingRepositoryResult.Success(listOf(manual)))),
        weatherRepository = object : WeatherRepository {
            override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
                assertEquals(location, storage.selected)
                events += "forecast"
                return sequenceOf(WeatherRepositoryResult.Loading)
            }
        },
        deviceLocationSource = DeviceLocationSource { callback ->
            sourceCalls++
            pointCallback = callback
            LocationCancellation { cancellations++ }
        },
        timeZoneResolver = CoordinateTimeZoneResolver { duringResolve(); resolved },
        searchExecutor = DirectExecutor,
        forecastExecutor = DirectExecutor,
        deviceExecutor = deviceExecutor,
    )
    fun entry() = holder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
    fun grant() {
        holder.onUseMyLocation()
        val command = holder.consumeNextCommand() as OxygenAppCommand.RequestLocationPermission
        holder.onLocationPermissionResult(command.attempt, LocationPermissionResult.Granted)
    }
    fun exit(action: String) {
        when (action) {
            "query" -> holder.onManualLocationQueryChanged("Chicago")
            "search" -> holder.onManualLocationSearchSubmitted()
            "manual" -> holder.onManualLocationCandidateSelected(manual.locationId)
            "saved" -> holder.onSavedLocationSelected(manual.locationId)
            "back" -> holder.onLocationEntryBack()
            "about" -> holder.onOpenSettings()
            "cancel", "stop" -> holder.cancelDeviceLocation() // same boundary MainActivity.onStop invokes
        }
    }
}

private object DirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class DeviceTestStorage(private val events: MutableList<String>) : SelectedLocationStorage {
    var selected: WeatherLocation? = null
    var fail = false
    override fun readSelectedLocation() = selected
    override fun writeSelectedLocation(location: WeatherLocation) {
        if (fail) throw IllegalStateException("Controlled storage failure")
        events += "write"
        selected = location
    }
}

private object NeverCompletingWeatherRepository : WeatherRepository {
    override fun refresh(location: com.oxygen.weather.core.model.WeatherLocation): Sequence<WeatherRepositoryResult> =
        sequenceOf(WeatherRepositoryResult.Loading)
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

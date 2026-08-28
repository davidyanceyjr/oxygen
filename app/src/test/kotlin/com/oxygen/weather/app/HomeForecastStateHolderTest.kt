package com.oxygen.weather.app

import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.AlertSeverity
import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherAlert
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.model.Wind
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.ForecastFreshness
import com.oxygen.weather.core.provider.GeocodingRepository
import com.oxygen.weather.core.provider.GeocodingRepositoryResult
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeForecastStateHolderTest {
    @Test
    fun `no selected location stays first-run and does not request weather`() {
        val weatherRepository = RecordingWeatherRepository(listOf(WeatherRepositoryResult.Loading))

        val stateHolder = OxygenAppStateHolder(
            weatherRepository = weatherRepository,
            forecastExecutor = DirectForecastExecutor,
        )

        assertTrue(stateHolder.presentationState.screen is OxygenAppScreen.FirstRunLocationEntry)
        assertEquals(emptyList<WeatherLocation>(), weatherRepository.locations)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }

    @Test
    fun `selected manual candidate starts one home forecast load for exact location`() {
        val location = weatherLocation("manual-madison", "Madison")
        val weatherRepository = RecordingWeatherRepository(listOf(WeatherRepositoryResult.Loading))
        val stateHolder = OxygenAppStateHolder(
            geocodingRepository = StaticGeocodingRepository(location),
            weatherRepository = weatherRepository,
            searchExecutor = DirectForecastExecutor,
            forecastExecutor = DirectForecastExecutor,
        )

        stateHolder.onManualLocationQueryChanged("Madison")
        stateHolder.onManualLocationSearchSubmitted()
        val result = ((stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry)
            .searchState as ManualLocationSearchState.Results).candidates.single()
        stateHolder.onManualLocationCandidateSelected(result.id)

        val home = stateHolder.presentationState.screen as OxygenAppScreen.Home
        val loading = home.forecast as HomeForecastPresentationState.Loading
        assertEquals(listOf(result.location), weatherRepository.locations)
        assertSame(result.location, stateHolder.presentationState.selectedLocation)
        assertSame(result.location, loading.location)
        assertTrue(loading.statusText.contains("Madison"))
        assertTrue(loading.forecastDisclosure.contains("Open-Meteo"))
        assertTrue(loading.forecastPrivacyNote.contains("coordinates"))
        assertTrue(loading.forecastPrivacyNote.contains("timezone"))
    }

    @Test
    fun `repository success becomes visible non-loading terminal home state`() {
        val location = weatherLocation("manual-chicago", "Chicago")
        val bundle = fullWeatherBundle(location)
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = RecordingWeatherRepository(
                listOf(
                    WeatherRepositoryResult.Loading,
                    WeatherRepositoryResult.Success(bundle),
                ),
            ),
            forecastExecutor = DirectForecastExecutor,
        )

        val home = stateHolder.presentationState.screen as OxygenAppScreen.Home
        val ready = home.forecast as HomeForecastPresentationState.ForecastReady
        assertSame(location, stateHolder.presentationState.selectedLocation)
        assertSame(location, ready.location)
        assertTrue(ready.forecastPrivacyNote.contains("Open-Meteo"))
        assertEquals("Chicago", ready.dashboard.locationName)
        assertEquals("65 deg F", ready.dashboard.current?.temperature)
        assertEquals("Rain showers", ready.dashboard.current?.condition)
        assertEquals("Feels like 63 deg F", ready.dashboard.current?.apparentTemperature)
        assertEquals("H 73 deg F", ready.dashboard.current?.highTemperature)
        assertEquals("L 54 deg F", ready.dashboard.current?.lowTemperature)
        assertEquals("Updated 5:30 AM", ready.dashboard.current?.updatedTime)
        assertEquals("Model estimate", ready.dashboard.current?.dataTypeLabel)
        assertEquals("Up to 60% precipitation chance in the next 6 hours; 1.2 mm possible in the next 6 hours", ready.dashboard.precipitationSummary)
        assertEquals("6 AM", ready.dashboard.hourly[0].time)
        assertEquals("64 deg F", ready.dashboard.hourly[0].temperature)
        assertEquals("60%", ready.dashboard.hourly[0].precipitationProbability)
        assertEquals("Sat, Aug 22", ready.dashboard.daily[0].date)
        assertEquals("Low 54 deg F", ready.dashboard.daily[0].low)
        assertEquals("High 73 deg F", ready.dashboard.daily[0].high)
        assertEquals("5:15 AM", ready.dashboard.daily[0].sunrise)
        assertEquals("8:01 PM", ready.dashboard.daily[0].sunset)
        assertEquals(HomeMetricPresentation("Wind", "14 km/h, gust 25 km/h, 225 deg"), ready.dashboard.metrics.single { it.label == "Wind" })
        assertEquals(HomeMetricPresentation("Visibility", "9.5 km"), ready.dashboard.metrics.single { it.label == "Visibility" })
        assertEquals(HomeMetricPresentation("Feels like", "63 deg F"), ready.dashboard.metrics.single { it.label == "Feels like" })
        assertEquals(HomeMetricPresentation("Dew point", "53 deg F"), ready.dashboard.metrics.single { it.label == "Dew point" })
        assertEquals("Open-Meteo", ready.dashboard.source.sourceName)
        assertEquals("Model estimate", ready.dashboard.source.dataType)
        assertEquals("Fetched Aug 22, 7:00 AM CDT", ready.dashboard.source.fetchedAt)
        assertEquals("Issued Aug 22, 6:45 AM CDT", ready.dashboard.source.issuedAt)
        assertEquals("CC BY 4.0", ready.dashboard.source.license)
        assertEquals("Flood Watch", ready.dashboard.alerts.single().event)
        assertFalse(ready.dashboard.visibleText().contains("internal-open-meteo-id"))
        assertEquals("Refresh", ready.refreshLabel)
        assertTrue(ready.canRefresh)
        assertFalse(ready.canRetry)
        assertEquals(
            listOf(
                HomeSuccessSection.LocationHeader,
                HomeSuccessSection.Alerts,
                HomeSuccessSection.Current,
                HomeSuccessSection.NearTermPrecipitation,
                HomeSuccessSection.Hourly,
                HomeSuccessSection.Daily,
                HomeSuccessSection.Metrics,
                HomeSuccessSection.Sun,
                HomeSuccessSection.Source,
                HomeSuccessSection.ProvenanceFooter,
            ),
            ready.dashboard.sectionOrder,
        )
    }

    @Test
    fun `current hero omits high and low when daily range values are unavailable`() {
        val location = weatherLocation("manual-no-range", "No Range City")
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = RecordingWeatherRepository(
                listOf(
                    WeatherRepositoryResult.Success(
                        fullWeatherBundle(location).copy(
                            daily = listOf(
                                DailyForecast(
                                    dateEpochDay = java.time.LocalDate.parse("2026-08-22").toEpochDay(),
                                    highC = null,
                                    lowC = null,
                                    precipitationProbabilityPercent = 40,
                                    condition = WeatherCondition.RAIN_SHOWERS,
                                    sunrise = Instant.parse("2026-08-22T10:15:00Z"),
                                    sunset = Instant.parse("2026-08-23T01:01:00Z"),
                                    provenance = forecastProvenance(type = DataType.FORECAST),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            forecastExecutor = DirectForecastExecutor,
        )

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady

        assertEquals(null, ready.dashboard.current?.highTemperature)
        assertEquals(null, ready.dashboard.current?.lowTemperature)
        assertFalse(ready.dashboard.visibleText().contains("H unavailable"))
        assertFalse(ready.dashboard.visibleText().contains("L unavailable"))
    }

    @Test
    fun `Open-Meteo success renders Open-Meteo forecast footer disclosure`() {
        val location = weatherLocation("manual-open-meteo-footer", "Open-Meteo Footer City")
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = RecordingWeatherRepository(
                listOf(
                    WeatherRepositoryResult.Success(
                        fullWeatherBundle(
                            location = location,
                            provenance = forecastProvenance(
                                providerId = "internal-open-meteo-id",
                                sourceName = "Open-Meteo",
                                licenseId = "CC BY 4.0",
                            ),
                        ),
                    ),
                ),
            ),
            forecastExecutor = DirectForecastExecutor,
        )

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady

        assertEquals("Weather data by Open-Meteo.", ready.forecastDisclosure)
        assertTrue(ready.forecastPrivacyNote.contains("Open-Meteo"))
    }

    @Test
    fun `MET Norway success renders MET Norway forecast footer disclosure`() {
        val location = weatherLocation("manual-metno-footer", "MET Norway Footer City")
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = RecordingWeatherRepository(
                listOf(
                    WeatherRepositoryResult.Success(
                        fullWeatherBundle(
                            location = location,
                            provenance = forecastProvenance(
                                providerId = "metno-provider-id",
                                sourceName = "MET Norway",
                                licenseId = "NLOD 2.0",
                            ),
                        ),
                    ),
                ),
            ),
            forecastExecutor = DirectForecastExecutor,
        )

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady

        assertEquals("Weather data by MET Norway.", ready.forecastDisclosure)
        assertTrue(ready.forecastPrivacyNote.contains("MET Norway"))
        assertFalse(ready.forecastDisclosure.contains("Open-Meteo"))
    }

    @Test
    fun `unavailable forecast provenance does not guess Open-Meteo footer disclosure`() {
        val location = weatherLocation("manual-unavailable-footer", "Unavailable Footer City")
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = RecordingWeatherRepository(
                listOf(WeatherRepositoryResult.Success(weatherBundle(location))),
            ),
            forecastExecutor = DirectForecastExecutor,
        )

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady

        assertEquals("Weather data source unavailable.", ready.forecastDisclosure)
        assertFalse(ready.forecastPrivacyNote.contains("Open-Meteo"))
    }

    @Test
    fun `success with no returned weather data shows unavailable state without fabricated dashboard values`() {
        val location = weatherLocation("manual-empty", "Empty City")
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = RecordingWeatherRepository(
                listOf(
                    WeatherRepositoryResult.Loading,
                    WeatherRepositoryResult.Success(weatherBundle(location)),
                ),
            ),
            forecastExecutor = DirectForecastExecutor,
        )

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady
        assertEquals("Provider returned no current, hourly, or daily weather data for this location.", ready.dashboard.returnedDataUnavailableText)
        assertEquals(null, ready.dashboard.current)
        assertEquals(emptyList<HomeHourlyPresentation>(), ready.dashboard.hourly)
        assertEquals(emptyList<HomeDailyPresentation>(), ready.dashboard.daily)
        assertEquals(emptyList<HomeMetricPresentation>(), ready.dashboard.metrics)
        assertEquals("Source unavailable", ready.dashboard.source.sourceName)
        assertEquals("Fetched Aug 22, 7:00 AM CDT", ready.dashboard.source.fetchedAt)
        assertFalse(ready.dashboard.visibleText().contains("0 deg F"))
        assertFalse(ready.dashboard.visibleText().contains("0%"))
    }

    @Test
    fun `partial success omits unavailable optional sections and preserves selected timezone`() {
        val location = weatherLocation(
            id = "manual-tokyo",
            name = "Tokyo",
            zoneId = "Asia/Tokyo",
        )
        val bundle = WeatherBundle(
            location = location,
            current = null,
            hourly = listOf(
                HourlyForecast(
                    time = Instant.parse("2026-08-22T15:00:00Z"),
                    temperatureC = null,
                    precipitationProbabilityPercent = null,
                    precipitationMm = null,
                    condition = WeatherCondition.UNKNOWN,
                    provenance = forecastProvenance(),
                ),
            ),
            daily = emptyList(),
            fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = RecordingWeatherRepository(
                listOf(WeatherRepositoryResult.Success(bundle)),
            ),
            forecastExecutor = DirectForecastExecutor,
        )

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady
        assertEquals("Current conditions unavailable", ready.dashboard.currentUnavailableText)
        assertEquals(null, ready.dashboard.precipitationSummary)
        assertEquals("12 AM", ready.dashboard.hourly.single().time)
        assertEquals("Unavailable", ready.dashboard.hourly.single().temperature)
        assertEquals(null, ready.dashboard.hourly.single().precipitationProbability)
        assertEquals(emptyList<HomeMetricPresentation>(), ready.dashboard.metrics)
        assertEquals(null, ready.dashboard.sun)
    }

    @Test
    fun `repository failures map to retryable provider-neutral no-cache home errors`() {
        val location = weatherLocation("manual-error", "Error City")
        val cases = listOf(
            ForecastError.NetworkUnavailable to HomeForecastMessage.NetworkUnavailable,
            ForecastError.RateLimited("open-meteo") to HomeForecastMessage.RateLimited,
            ForecastError.ProviderUnavailable("open-meteo") to HomeForecastMessage.ProviderUnavailable,
            ForecastError.InvalidResponse("open-meteo") to HomeForecastMessage.InvalidResponse,
            ForecastError.ProviderRejectedRequest("open-meteo") to HomeForecastMessage.RejectedRequest,
            ForecastError.LocalCacheFailure to HomeForecastMessage.LocalCacheFailure,
            ForecastError.UnexpectedProviderFailure("open-meteo") to HomeForecastMessage.UnexpectedFailure,
        )

        cases.forEach { (error, expectedMessage) ->
            val stateHolder = OxygenAppStateHolder(
                selectedLocation = location,
                weatherRepository = RecordingWeatherRepository(
                    listOf(
                        WeatherRepositoryResult.Loading,
                        WeatherRepositoryResult.Failure(error),
                    ),
                ),
                forecastExecutor = DirectForecastExecutor,
            )

            val home = stateHolder.presentationState.screen as OxygenAppScreen.Home
            val forecastError = home.forecast as HomeForecastPresentationState.NoCacheError
            assertSame(location, forecastError.location)
            assertEquals(expectedMessage, forecastError.message)
            assertTrue(forecastError.canRetry)
            assertFalse(forecastError.message.text.contains("open-meteo"))
            assertFalse(forecastError.message.text.contains("providerId"))
            assertTrue(forecastError.forecastDisclosure.contains("Open-Meteo"))
        }
    }

    @Test
    fun `stale success after failed refresh keeps dashboard visible with refresh metadata`() {
        val location = weatherLocation("manual-stale-cache", "Stale Cache City")
        val bundle = fullWeatherBundle(location)
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = RecordingWeatherRepository(
                listOf(
                    WeatherRepositoryResult.Loading,
                    WeatherRepositoryResult.Success(
                        weather = bundle,
                        freshness = ForecastFreshness.StaleAfterFailedRefresh(
                            staleAge = Duration.ofMinutes(45),
                            refreshFailure = ForecastError.NetworkUnavailable,
                        ),
                    ),
                ),
            ),
            forecastExecutor = DirectForecastExecutor,
        )

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady
        val stale = ready.freshness as HomeForecastFreshness.StaleAfterFailedRefresh

        assertSame(location, ready.location)
        assertEquals("65 deg F", ready.dashboard.current?.temperature)
        assertEquals("Open-Meteo", ready.dashboard.source.sourceName)
        assertEquals("Fetched Aug 22, 7:00 AM CDT", ready.dashboard.source.fetchedAt)
        assertEquals("45 minutes", stale.staleAgeText)
        assertEquals(HomeRefreshFailureMessage.NetworkUnavailable, stale.refreshFailureMessage)
        assertEquals("Refresh could not reach the weather service or network.", stale.refreshFailureMessage.text)
        assertFalse(stale.refreshFailureMessage.text.contains("No cached forecast is available yet"))
        assertTrue(stale.statusText.contains("cached forecast"))
        assertTrue(stale.statusText.contains("45 minutes"))
        assertTrue(ready.canRefresh)
        assertEquals("Refresh", ready.refreshLabel)
        assertFalse(ready.canRetry)
        assertFalse(ready.isRefreshInProgress)
    }

    @Test
    fun `explicit home refresh while ready keeps previous dashboard visible`() {
        val location = weatherLocation("manual-refresh-ready", "Refresh Ready City")
        val weatherRepository = ControlledWeatherRepository()
        val executor = Executors.newFixedThreadPool(2)
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = weatherRepository,
            forecastExecutor = executor,
        )

        try {
            val first = weatherRepository.awaitCall(0)
            first.emit(WeatherRepositoryResult.Success(fullWeatherBundle(location)))
            val initialReady = awaitHomeState<HomeForecastPresentationState.ForecastReady>(stateHolder)
            assertEquals("65 deg F", initialReady.dashboard.current?.temperature)

            stateHolder.onHomeForecastRefresh()
            val second = weatherRepository.awaitCall(1)
            second.emit(WeatherRepositoryResult.Loading)
            val refreshing = awaitHomeState<HomeForecastPresentationState.ForecastReady>(stateHolder)

            assertSame(location, refreshing.location)
            assertEquals("65 deg F", refreshing.dashboard.current?.temperature)
            assertTrue(refreshing.isRefreshInProgress)
            assertEquals("Refreshing weather for Refresh Ready City", refreshing.refreshInProgressText)
            assertEquals(listOf(location, location), weatherRepository.locations)
        } finally {
            weatherRepository.finishAll()
            executor.shutdownNow()
        }
    }

    @Test
    fun `explicit home refresh success replaces dashboard and clears stale metadata`() {
        val location = weatherLocation("manual-refresh-success", "Refresh Success City")
        val firstBundle = fullWeatherBundle(location)
        val refreshedBundle = fullWeatherBundle(location).copy(
            current = fullWeatherBundle(location).current?.copy(temperatureC = 24.0),
            fetchedAt = Instant.parse("2026-08-22T13:00:00Z"),
        )
        val weatherRepository = ControlledWeatherRepository()
        val executor = Executors.newFixedThreadPool(2)
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = weatherRepository,
            forecastExecutor = executor,
        )

        try {
            val first = weatherRepository.awaitCall(0)
            first.emit(
                WeatherRepositoryResult.Success(
                    weather = firstBundle,
                    freshness = ForecastFreshness.StaleAfterFailedRefresh(
                        staleAge = Duration.ofMinutes(30),
                        refreshFailure = ForecastError.NetworkUnavailable,
                    ),
                ),
            )
            val stale = awaitHomeState<HomeForecastPresentationState.ForecastReady>(stateHolder)
            assertTrue(stale.freshness is HomeForecastFreshness.StaleAfterFailedRefresh)

            stateHolder.onHomeForecastRefresh()
            val second = weatherRepository.awaitCall(1)
            second.emit(WeatherRepositoryResult.Loading)
            assertTrue(awaitHomeState<HomeForecastPresentationState.ForecastReady>(stateHolder).isRefreshInProgress)
            second.emit(WeatherRepositoryResult.Success(refreshedBundle))

            val refreshed = awaitReadyState(stateHolder) { it.dashboard.current?.temperature == "75 deg F" }
            assertSame(location, refreshed.location)
            assertEquals("75 deg F", refreshed.dashboard.current?.temperature)
            assertEquals(HomeForecastFreshness.Fresh, refreshed.freshness)
            assertFalse(refreshed.isRefreshInProgress)
            assertEquals(null, refreshed.refreshInProgressText)
            assertEquals(listOf(location, location), weatherRepository.locations)
        } finally {
            weatherRepository.finishAll()
            executor.shutdownNow()
        }
    }

    @Test
    fun `explicit home refresh failure without cache becomes no-cache error`() {
        val location = weatherLocation("manual-refresh-no-cache", "Refresh No Cache City")
        val weatherRepository = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(fullWeatherBundle(location))),
            listOf(
                WeatherRepositoryResult.Loading,
                WeatherRepositoryResult.Failure(ForecastError.NetworkUnavailable),
            ),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = weatherRepository,
            forecastExecutor = DirectForecastExecutor,
        )

        stateHolder.onHomeForecastRefresh()

        val error = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.NoCacheError
        assertSame(location, error.location)
        assertEquals(HomeForecastMessage.NetworkUnavailable, error.message)
        assertEquals(listOf(location, location), weatherRepository.locations)
    }

    @Test
    fun `home retry requests weather again for same selected location`() {
        val location = weatherLocation("manual-retry", "Retry City")
        val weatherRepository = RecordingWeatherRepository(
            listOf(
                WeatherRepositoryResult.Loading,
                WeatherRepositoryResult.Failure(ForecastError.NetworkUnavailable),
            ),
            listOf(
                WeatherRepositoryResult.Loading,
                WeatherRepositoryResult.Failure(ForecastError.ProviderUnavailable("open-meteo")),
            ),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = weatherRepository,
            forecastExecutor = DirectForecastExecutor,
        )

        stateHolder.onHomeForecastRetry()

        val home = stateHolder.presentationState.screen as OxygenAppScreen.Home
        val error = home.forecast as HomeForecastPresentationState.NoCacheError
        assertEquals(listOf(location, location), weatherRepository.locations)
        assertSame(location, stateHolder.presentationState.selectedLocation)
        assertSame(location, error.location)
        assertEquals(HomeForecastMessage.ProviderUnavailable, error.message)
    }

    @Test
    fun `obsolete retry emissions cannot replace active home forecast state`() {
        val location = weatherLocation("manual-stale", "Stale City")
        val weatherRepository = ControlledWeatherRepository()
        val executor = Executors.newFixedThreadPool(2)
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = weatherRepository,
            forecastExecutor = executor,
        )

        try {
            val first = weatherRepository.awaitCall(0)
            first.emit(WeatherRepositoryResult.Loading)
            awaitHomeState<HomeForecastPresentationState.Loading>(stateHolder)

            stateHolder.onHomeForecastRetry()
            val second = weatherRepository.awaitCall(1)
            second.emit(WeatherRepositoryResult.Loading)
            second.emit(WeatherRepositoryResult.Success(weatherBundle(location)))
            val ready = awaitHomeState<HomeForecastPresentationState.ForecastReady>(stateHolder)

            first.emit(WeatherRepositoryResult.Failure(ForecastError.NetworkUnavailable))
            Thread.sleep(100)

            assertSame(location, ready.location)
            assertTrue(stateHolder.presentationState.screen is OxygenAppScreen.Home)
            assertTrue((stateHolder.presentationState.screen as OxygenAppScreen.Home).forecast is HomeForecastPresentationState.ForecastReady)
            assertEquals(listOf(location, location), weatherRepository.locations)
        } finally {
            weatherRepository.finishAll()
            executor.shutdownNow()
        }
    }
}

private object DirectForecastExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class StaticGeocodingRepository(
    private val location: WeatherLocation,
) : GeocodingRepository {
    override fun search(
        query: String,
        count: Int,
        language: String?,
        countryCode: String?,
    ): Sequence<GeocodingRepositoryResult> =
        sequenceOf(
            GeocodingRepositoryResult.Success(
                listOf(
                    com.oxygen.weather.core.model.GeocodingLocationCandidate(
                        locationId = location.id,
                        displayName = location.displayName,
                        point = location.point,
                        zoneId = location.zoneId,
                        country = "United States",
                        countryCode = "US",
                    ),
                ),
            ),
        )
}

private class RecordingWeatherRepository(
    private vararg val responses: List<WeatherRepositoryResult>,
) : WeatherRepository {
    val locations = mutableListOf<WeatherLocation>()
    private var callIndex = 0

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        locations += location
        val response = responses.getOrElse(callIndex) { responses.last() }
        callIndex += 1
        return response.asSequence()
    }
}

private class ControlledWeatherRepository : WeatherRepository {
    val locations = mutableListOf<WeatherLocation>()
    private val calls = mutableListOf<ControlledWeatherCall>()
    private val lock = ReentrantLock()
    private val callAdded = lock.newCondition()

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        locations += location
        val call = ControlledWeatherCall()
        lock.withLock {
            calls += call
            callAdded.signalAll()
        }
        return call.results()
    }

    fun awaitCall(index: Int): ControlledWeatherCall {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
        lock.withLock {
            while (calls.size <= index && System.nanoTime() < deadline) {
                callAdded.await(10, TimeUnit.MILLISECONDS)
            }
            if (calls.size <= index) error("Timed out waiting for weather repository call $index")
            return calls[index]
        }
    }

    fun finishAll() {
        lock.withLock {
            calls.forEach { it.finish() }
        }
    }
}

private class ControlledWeatherCall {
    private val results = LinkedBlockingQueue<ControlledWeatherResult>()

    fun results(): Sequence<WeatherRepositoryResult> = sequence {
        while (true) {
            when (val result = results.take()) {
                ControlledWeatherResult.Finished -> return@sequence
                is ControlledWeatherResult.Next -> yield(result.value)
            }
        }
    }

    fun emit(result: WeatherRepositoryResult) {
        results += ControlledWeatherResult.Next(result)
    }

    fun finish() {
        results += ControlledWeatherResult.Finished
    }
}

private sealed interface ControlledWeatherResult {
    data class Next(val value: WeatherRepositoryResult) : ControlledWeatherResult
    data object Finished : ControlledWeatherResult
}

private inline fun <reified T : HomeForecastPresentationState> awaitHomeState(
    stateHolder: OxygenAppStateHolder,
): T {
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
    while (System.nanoTime() < deadline) {
        val forecast = (stateHolder.presentationState.screen as? OxygenAppScreen.Home)?.forecast
        if (forecast is T) return forecast
        Thread.sleep(10)
    }
    error("Timed out waiting for Home forecast state ${T::class.java.simpleName}")
}

private fun awaitReadyState(
    stateHolder: OxygenAppStateHolder,
    predicate: (HomeForecastPresentationState.ForecastReady) -> Boolean,
): HomeForecastPresentationState.ForecastReady {
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
    while (System.nanoTime() < deadline) {
        val forecast = (stateHolder.presentationState.screen as? OxygenAppScreen.Home)?.forecast
        if (forecast is HomeForecastPresentationState.ForecastReady && predicate(forecast)) return forecast
        Thread.sleep(10)
    }
    error("Timed out waiting for matching Home ready state")
}

private fun weatherLocation(
    id: String,
    name: String,
    zoneId: String = "America/Chicago",
): WeatherLocation =
    WeatherLocation(
        id = LocationId(id),
        displayName = name,
        point = GeoPoint(43.0731, -89.4012),
        zoneId = ZoneId.of(zoneId),
    )

private fun weatherBundle(location: WeatherLocation): WeatherBundle =
    WeatherBundle(
        location = location,
        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
    )

private fun fullWeatherBundle(
    location: WeatherLocation,
    provenance: DataProvenance = forecastProvenance(),
): WeatherBundle =
    WeatherBundle(
        location = location,
        current = CurrentConditions(
            time = Instant.parse("2026-08-22T10:30:00Z"),
            temperatureC = 18.4,
            apparentTemperatureC = 17.2,
            dewPointC = 11.6,
            humidityPercent = 72,
            pressureHpa = 1012.4,
            visibilityMeters = 9500.0,
            cloudCoverPercent = 88,
            wind = Wind(
                speedMetersPerSecond = 4.0,
                gustMetersPerSecond = 7.0,
                directionDegrees = 225.0,
            ),
            precipitationMm = 0.4,
            condition = WeatherCondition.RAIN_SHOWERS,
            provenance = provenance,
        ),
        hourly = listOf(
            HourlyForecast(
                time = Instant.parse("2026-08-22T11:00:00Z"),
                temperatureC = 18.0,
                precipitationProbabilityPercent = 60,
                precipitationMm = 1.2,
                condition = WeatherCondition.RAIN,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            HourlyForecast(
                time = Instant.parse("2026-08-22T12:00:00Z"),
                temperatureC = 19.2,
                precipitationProbabilityPercent = null,
                precipitationMm = null,
                condition = WeatherCondition.CLOUDY,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
        ),
        daily = listOf(
            DailyForecast(
                dateEpochDay = java.time.LocalDate.parse("2026-08-22").toEpochDay(),
                highC = 22.7,
                lowC = 12.3,
                precipitationProbabilityPercent = 40,
                condition = WeatherCondition.RAIN_SHOWERS,
                sunrise = Instant.parse("2026-08-22T10:15:00Z"),
                sunset = Instant.parse("2026-08-23T01:01:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
        ),
        alerts = listOf(
            WeatherAlert(
                id = "alert-1",
                event = "Flood Watch",
                headline = "Flooding possible near rivers",
                severity = AlertSeverity.MODERATE,
                effective = Instant.parse("2026-08-22T12:00:00Z"),
                expires = Instant.parse("2026-08-22T18:00:00Z"),
                issuer = "Test Weather Office",
                provenance = provenance.copy(type = DataType.OFFICIAL_ALERT),
            ),
        ),
        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
    )

private fun forecastProvenance(
    type: DataType = DataType.MODEL_ESTIMATE,
    providerId: String = "internal-open-meteo-id",
    sourceName: String = "Open-Meteo",
    licenseId: String = "CC BY 4.0",
): DataProvenance =
    DataProvenance(
        providerId = providerId,
        sourceName = sourceName,
        issuedAt = Instant.parse("2026-08-22T11:45:00Z"),
        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
        type = type,
        licenseId = licenseId,
    )

private fun HomeSuccessPresentation.visibleText(): String =
    buildString {
        append(locationName).append('\n')
        append(locationSubtitle).append('\n')
        alerts.forEach { append(listOfNotNull(it.event, it.headline, it.severity, it.issuer, it.effective, it.expires).joinToString(" ")).append('\n') }
        current?.let {
            append(
                listOfNotNull(
                    it.temperature,
                    it.condition,
                    it.apparentTemperature,
                    it.highTemperature,
                    it.lowTemperature,
                    it.updatedTime,
                    it.dataTypeLabel,
                ).joinToString(" "),
            ).append('\n')
        }
        append(currentUnavailableText.orEmpty()).append('\n')
        append(precipitationSummary.orEmpty()).append('\n')
        hourly.forEach { append(listOfNotNull(it.time, it.condition, it.temperature, it.precipitationProbability).joinToString(" ")).append('\n') }
        daily.forEach { append(listOfNotNull(it.date, it.condition, it.precipitationProbability, it.high, it.low, it.sunrise, it.sunset).joinToString(" ")).append('\n') }
        metrics.forEach { append("${it.label} ${it.value}\n") }
        sun?.let { append("${it.sunrise} ${it.sunset}\n") }
        append(listOfNotNull(source.sourceName, source.dataType, source.fetchedAt, source.issuedAt, source.license).joinToString(" "))
        append(returnedDataUnavailableText.orEmpty())
    }

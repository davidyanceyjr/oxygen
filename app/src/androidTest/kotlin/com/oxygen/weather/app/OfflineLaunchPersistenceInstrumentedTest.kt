package com.oxygen.weather.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.model.Wind
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import com.oxygen.weather.core.provider.cache.CachedWeatherRepository
import com.oxygen.weather.core.provider.cache.room.RoomForecastCacheStorageFactory
import com.oxygen.weather.core.provider.cache.room.RoomSavedLocationStorageFactory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineLaunchPersistenceInstrumentedTest {
    @Test
    fun seedDeterministicInstalledHomeScreenshotState() {
        val context = targetContext()
        val selectedLocationStorage = DataStoreSelectedLocationStorage(context)
        val forecastCacheStorage = RoomForecastCacheStorageFactory.create(context)
        val savedLocationStorage = RoomSavedLocationStorageFactory.create(context)
        val location = weatherLocation(
            id = "android-installed-screenshot",
            name = "Madison, Wisconsin, United States",
        )
        val savedComparison = weatherLocation(
            id = "android-installed-screenshot-comparison",
            name = "Madison, Alabama, United States",
            latitude = 34.6993,
            longitude = -86.7483,
        )
        val bundle = fullWeatherBundle(location)

        selectedLocationStorage.writeSelectedLocation(location)
        savedLocationStorage.saveLocation(location)
        savedLocationStorage.saveLocation(savedComparison)
        forecastCacheStorage.replaceBundle(bundle)

        assertEquals(location, selectedLocationStorage.readSelectedLocation())
        assertEquals(listOf(location, savedComparison), savedLocationStorage.listLocations())
        assertEquals(bundle.location, forecastCacheStorage.readBundle(location.id)?.location)
    }

    @Test
    fun dataStoreSelectedLocationSnapshotWritesAndReadsProviderNeutralFields() {
        val storage = DataStoreSelectedLocationStorage(targetContext())
        val location = weatherLocation(
            id = "android-selected-readback-${System.nanoTime()}",
            name = "Android Readback City",
            latitude = 41.8781,
            longitude = -87.6298,
            elevationMeters = 181.0,
        )

        storage.writeSelectedLocation(location)

        assertEquals(location, storage.readSelectedLocation())
    }

    @Test
    fun removingSavedLocationWithSelectedIdLeavesDataStoreSelectedLocationUnchanged() {
        val context = targetContext()
        context.deleteDatabase("oxygen_forecast_cache.db")
        val selectedLocationStorage = DataStoreSelectedLocationStorage(context)
        val savedLocationStorage = RoomSavedLocationStorageFactory.create(context)
        val location = weatherLocation(
            id = "android-selected-saved-independent-${System.nanoTime()}",
            name = "Android Selected Saved Independent City",
        )

        selectedLocationStorage.writeSelectedLocation(location)
        savedLocationStorage.saveLocation(location)
        savedLocationStorage.removeLocation(location.id)

        assertEquals(location, selectedLocationStorage.readSelectedLocation())
        assertEquals(emptyList<WeatherLocation>(), savedLocationStorage.listLocations())
        context.deleteDatabase("oxygen_forecast_cache.db")
    }

    @Test
    fun startupWithSelectedLocationAndRoomCacheRendersCachedHomeAfterNetworkFailure() {
        val context = targetContext()
        val selectedLocationStorage = DataStoreSelectedLocationStorage(context)
        val forecastCacheStorage = RoomForecastCacheStorageFactory.create(context)
        val location = weatherLocation(
            id = "android-cached-launch-${System.nanoTime()}",
            name = "Android Cached Launch City",
        )
        selectedLocationStorage.writeSelectedLocation(location)
        forecastCacheStorage.replaceBundle(fullWeatherBundle(location))

        val stateHolder = OxygenAppStateHolder(
            selectedLocationStorage = selectedLocationStorage,
            forecastCacheStorage = forecastCacheStorage,
            weatherRepository = CachedWeatherRepository(
                upstream = FailingWeatherRepository,
                storage = forecastCacheStorage,
                clock = java.time.Clock.fixed(Instant.parse("2026-08-22T12:45:00Z"), ZoneId.of("UTC")),
            ),
            forecastExecutor = DirectExecutor,
            clock = java.time.Clock.fixed(Instant.parse("2026-08-22T12:45:00Z"), ZoneId.of("UTC")),
        )

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady
        val stale = ready.freshness as HomeForecastFreshness.StaleAfterFailedRefresh

        assertEquals(location, stateHolder.presentationState.selectedLocation)
        assertEquals(location, ready.location)
        assertEquals("65 deg F", ready.dashboard.current?.temperature)
        assertEquals("Open-Meteo", ready.dashboard.source.sourceName)
        assertEquals("45 minutes", stale.staleAgeText)
        assertEquals(HomeRefreshFailureMessage.NetworkUnavailable, stale.refreshFailureMessage)
        assertFalse(ready.dashboard.visibleText().contains("SampleWeather"))
    }

    @Test
    fun savedRoomLocationSelectionPersistsSelectedLocationAndRestoresMatchingRoomCache() {
        val context = targetContext()
        context.deleteDatabase("oxygen_forecast_cache.db")
        val selectedLocationStorage = DataStoreSelectedLocationStorage(context)
        val savedLocationStorage = RoomSavedLocationStorageFactory.create(context)
        val forecastCacheStorage = RoomForecastCacheStorageFactory.create(context)
        val location = weatherLocation(
            id = "android-saved-selection-${System.nanoTime()}",
            name = "Android Saved Selection City",
        )
        savedLocationStorage.saveLocation(location)
        forecastCacheStorage.replaceBundle(fullWeatherBundle(location))

        val stateHolder = OxygenAppStateHolder(
            selectedLocationStorage = selectedLocationStorage,
            savedLocationStorage = savedLocationStorage,
            forecastCacheStorage = forecastCacheStorage,
            weatherRepository = FailingWeatherRepository,
            forecastExecutor = DirectExecutor,
            clock = java.time.Clock.fixed(Instant.parse("2026-08-22T12:45:00Z"), ZoneId.of("UTC")),
        )

        stateHolder.onSavedLocationSelected(location.id)

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady
        val stale = ready.freshness as HomeForecastFreshness.StaleAfterFailedRefresh
        assertEquals(location, selectedLocationStorage.readSelectedLocation())
        assertEquals(location, stateHolder.presentationState.selectedLocation)
        assertEquals(location, ready.location)
        assertEquals("45 minutes", stale.staleAgeText)
        assertEquals(HomeRefreshFailureMessage.NetworkUnavailable, stale.refreshFailureMessage)
        context.deleteDatabase("oxygen_forecast_cache.db")
    }

    @Test
    fun locationEntryLoadsProductionRoomSavedRowsAndSelectionDrivesHomeByLocalLocationId() {
        val context = targetContext()
        context.deleteDatabase("oxygen_forecast_cache.db")
        val selectedLocationStorage = DataStoreSelectedLocationStorage(context)
        val savedLocationStorage = RoomSavedLocationStorageFactory.create(context)
        val forecastCacheStorage = RoomForecastCacheStorageFactory.create(context)
        val oldLocation = weatherLocation(
            id = "android-location-entry-old-${System.nanoTime()}",
            name = "Android Location Entry Old City",
        )
        val savedMadison = weatherLocation(
            id = "android-location-entry-madison-${System.nanoTime()}",
            name = "Madison, Wisconsin, United States",
        )
        val savedChicago = weatherLocation(
            id = "android-location-entry-chicago-${System.nanoTime()}",
            name = "Chicago, Illinois, United States",
            latitude = 41.8781,
            longitude = -87.6298,
        )
        savedLocationStorage.saveLocation(savedMadison)
        savedLocationStorage.saveLocation(savedChicago)
        forecastCacheStorage.replaceBundle(fullWeatherBundle(savedChicago))
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = oldLocation,
            selectedLocationStorage = selectedLocationStorage,
            savedLocationStorage = savedLocationStorage,
            forecastCacheStorage = forecastCacheStorage,
            weatherRepository = FailingWeatherRepository,
            forecastExecutor = DirectExecutor,
            clock = java.time.Clock.fixed(Instant.parse("2026-08-22T12:45:00Z"), ZoneId.of("UTC")),
        )

        stateHolder.onChangeLocation()
        val savedRows = stateHolder.presentationState.savedLocations as SavedLocationsPresentationState.Loaded
        stateHolder.onSavedLocationSelected(savedChicago.id)

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady
        assertEquals(listOf(savedMadison, savedChicago), savedRows.locations)
        assertEquals(savedChicago, selectedLocationStorage.readSelectedLocation())
        assertEquals(savedChicago.id, stateHolder.presentationState.selectedLocation?.id)
        assertEquals(savedChicago.id, ready.location.id)
        assertTrue(ready.dashboard.visibleText().contains("Open-Meteo"))
        context.deleteDatabase("oxygen_forecast_cache.db")
    }

    @Test
    fun startupWithSelectedLocationAndNoRoomCacheRendersRetryableNoCacheError() {
        val context = targetContext()
        val selectedLocationStorage = DataStoreSelectedLocationStorage(context)
        val location = weatherLocation(
            id = "android-no-cache-launch-${System.nanoTime()}",
            name = "Android No Cache Launch City",
        )
        selectedLocationStorage.writeSelectedLocation(location)

        val stateHolder = OxygenAppStateHolder(
            selectedLocationStorage = selectedLocationStorage,
            forecastCacheStorage = RoomForecastCacheStorageFactory.create(context),
            weatherRepository = FailingWeatherRepository,
            forecastExecutor = DirectExecutor,
        )

        val error = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.NoCacheError
        assertEquals(location, stateHolder.presentationState.selectedLocation)
        assertEquals(HomeForecastMessage.NetworkUnavailable, error.message)
        assertTrue(error.canRetry)
    }
}

private fun targetContext() = InstrumentationRegistry.getInstrumentation().targetContext

private object DirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private object FailingWeatherRepository : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> =
        sequenceOf(
            WeatherRepositoryResult.Loading,
            WeatherRepositoryResult.Failure(ForecastError.NetworkUnavailable),
        )
}

private fun weatherLocation(
    id: String,
    name: String,
    latitude: Double = 43.0731,
    longitude: Double = -89.4012,
    elevationMeters: Double? = null,
): WeatherLocation =
    WeatherLocation(
        id = LocationId(id),
        displayName = name,
        point = GeoPoint(latitude, longitude),
        elevationMeters = elevationMeters,
        zoneId = ZoneId.of("America/Chicago"),
    )

private fun fullWeatherBundle(location: WeatherLocation): WeatherBundle {
    val provenance = DataProvenance(
        providerId = "internal-provider-id",
        sourceName = "Open-Meteo",
        issuedAt = Instant.parse("2026-08-22T11:45:00Z"),
        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
        type = DataType.MODEL_ESTIMATE,
        licenseId = "CC BY 4.0",
    )
    return WeatherBundle(
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
            HourlyForecast(
                time = Instant.parse("2026-08-22T13:00:00Z"),
                temperatureC = 20.0,
                precipitationProbabilityPercent = 20,
                precipitationMm = 0.2,
                condition = WeatherCondition.PARTLY_CLOUDY,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            HourlyForecast(
                time = Instant.parse("2026-08-22T14:00:00Z"),
                temperatureC = 21.1,
                precipitationProbabilityPercent = 10,
                precipitationMm = 0.0,
                condition = WeatherCondition.MOSTLY_CLEAR,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            HourlyForecast(
                time = Instant.parse("2026-08-22T15:00:00Z"),
                temperatureC = 22.0,
                precipitationProbabilityPercent = null,
                precipitationMm = null,
                condition = WeatherCondition.THUNDERSTORM,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            HourlyForecast(
                time = Instant.parse("2026-08-22T16:00:00Z"),
                temperatureC = 21.5,
                precipitationProbabilityPercent = 40,
                precipitationMm = 0.8,
                condition = WeatherCondition.RAIN_SHOWERS,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
        ),
        daily = listOf(
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-22").toEpochDay(),
                highC = 22.7,
                lowC = 12.3,
                precipitationProbabilityPercent = 40,
                condition = WeatherCondition.RAIN_SHOWERS,
                sunrise = Instant.parse("2026-08-22T10:15:00Z"),
                sunset = Instant.parse("2026-08-23T01:01:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-23").toEpochDay(),
                highC = 21.1,
                lowC = 11.2,
                precipitationProbabilityPercent = null,
                condition = WeatherCondition.CLOUDY,
                sunrise = Instant.parse("2026-08-23T10:16:00Z"),
                sunset = Instant.parse("2026-08-24T00:59:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-24").toEpochDay(),
                highC = 24.8,
                lowC = 14.1,
                precipitationProbabilityPercent = 20,
                condition = WeatherCondition.PARTLY_CLOUDY,
                sunrise = Instant.parse("2026-08-24T10:17:00Z"),
                sunset = Instant.parse("2026-08-25T00:57:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-25").toEpochDay(),
                highC = 27.6,
                lowC = 16.1,
                precipitationProbabilityPercent = 10,
                condition = WeatherCondition.MOSTLY_CLEAR,
                sunrise = Instant.parse("2026-08-25T10:18:00Z"),
                sunset = Instant.parse("2026-08-26T00:55:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-26").toEpochDay(),
                highC = null,
                lowC = 15.0,
                precipitationProbabilityPercent = 50,
                condition = WeatherCondition.THUNDERSTORM,
                sunrise = Instant.parse("2026-08-26T10:19:00Z"),
                sunset = Instant.parse("2026-08-27T00:53:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-27").toEpochDay(),
                highC = 19.5,
                lowC = null,
                precipitationProbabilityPercent = null,
                condition = WeatherCondition.RAIN,
                sunrise = Instant.parse("2026-08-27T10:20:00Z"),
                sunset = Instant.parse("2026-08-28T00:51:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
        ),
        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
    )
}

private fun HomeSuccessPresentation.visibleText(): String =
    buildString {
        append(locationName).append('\n')
        current?.let { append("${it.temperature} ${it.condition}\n") }
        hourly.forEach { append("${it.time} ${it.temperature}\n") }
        daily.forEach { append("${it.date} ${it.high} ${it.low}\n") }
        append(source.sourceName).append('\n')
    }

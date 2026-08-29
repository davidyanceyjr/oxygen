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
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import com.oxygen.weather.core.provider.cache.CachedWeatherRepository
import com.oxygen.weather.core.provider.cache.room.RoomForecastCacheStorageFactory
import java.time.Instant
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
            condition = WeatherCondition.RAIN_SHOWERS,
            provenance = provenance,
        ),
        hourly = listOf(
            HourlyForecast(
                time = Instant.parse("2026-08-22T11:00:00Z"),
                temperatureC = 18.0,
                precipitationProbabilityPercent = 60,
                condition = WeatherCondition.RAIN,
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

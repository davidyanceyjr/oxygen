package com.oxygen.weather.core.provider.cache.room

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.oxygen.weather.core.model.AirQuality
import com.oxygen.weather.core.model.AlertSeverity
import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherAlert
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.model.Wind
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import com.oxygen.weather.core.provider.cache.CachedWeatherRepository
import com.oxygen.weather.core.provider.cache.ForecastCacheStorage
import java.time.Instant
import java.time.ZoneId
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomForecastCacheStorageInstrumentedTest {
    private lateinit var database: OxygenForecastCacheDatabase
    private lateinit var storage: RoomForecastCacheStorage

    @Before
    fun setUp() {
        database = inMemoryDatabase()
        storage = RoomForecastCacheStorage(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun productionFactoryCreatesUsableForecastCacheStorageFromContext() {
        val context = targetContext()
        context.deleteDatabase("oxygen_forecast_cache.db")
        val factoryStorage = RoomForecastCacheStorageFactory.create(context)
        val bundle = fullBundle(chicago, providerId = "open-meteo")

        factoryStorage.replaceBundle(bundle)

        assertEquals(bundle, factoryStorage.readBundle(chicago.id))
        context.deleteDatabase("oxygen_forecast_cache.db")
    }

    @Test
    fun roomStorageRoundTripsProviderNeutralForecastBundle() {
        val bundle = fullBundle(chicago, providerId = "open-meteo")

        storage.replaceBundle(bundle)

        assertEquals(bundle, storage.readBundle(chicago.id))
    }

    @Test
    fun cachedRepositoryEmitsSuccessFromRoomReadbackAfterProviderSuccess() {
        val providerBundle = fullBundle(chicago, providerId = "open-meteo")

        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(providerBundle)),
            storage = storage,
        ).refresh(chicago).terminalSuccess()

        assertEquals(providerBundle, success.weather)
        assertEquals(providerBundle, storage.readBundle(chicago.id))
    }

    @Test
    fun replacementTransactionLeavesPreviousBundleVisibleWhenCommitFails() {
        val oldBundle = fullBundle(chicago, providerId = "open-meteo", temperatureC = 21.0)
        val newBundle = fullBundle(chicago, providerId = "open-meteo", temperatureC = 31.0)
        storage.replaceBundle(oldBundle)
        val failingStorage = RoomForecastCacheStorage(database) {
            error("injected failure before transaction commit")
        }

        assertThrowsIllegalState { failingStorage.replaceBundle(newBundle) }

        assertEquals(oldBundle, storage.readBundle(chicago.id))
    }

    @Test
    fun failedReplacementDoesNotExposePartialBundleForEmptyLocation() {
        val failingStorage = RoomForecastCacheStorage(database) {
            error("injected failure before transaction commit")
        }

        assertThrowsIllegalState { failingStorage.replaceBundle(fullBundle(chicago, "open-meteo")) }

        assertNull(storage.readBundle(chicago.id))
    }

    @Test
    fun locationIdScopesRoomReadback() {
        val chicagoBundle = fullBundle(chicago, providerId = "open-meteo", temperatureC = 19.0)
        val madisonBundle = fullBundle(madison, providerId = "met-norway", temperatureC = 14.0)

        storage.replaceBundle(chicagoBundle)
        storage.replaceBundle(madisonBundle)

        assertEquals(chicagoBundle, storage.readBundle(chicago.id))
        assertEquals(madisonBundle, storage.readBundle(madison.id))
        assertNull(storage.readBundle(LocationId("manual-detroit")))
    }

    @Test
    fun preservesNullsAndRowOrderingWithoutFabrication() {
        val bundle = sparseBundle(chicago)

        storage.replaceBundle(bundle)

        val readback = requireNotNull(storage.readBundle(chicago.id))
        val current = requireNotNull(readback.current)
        assertNull(current.temperatureC)
        assertNull(current.apparentTemperatureC)
        assertNull(current.humidityPercent)
        assertNull(current.wind)
        assertNull(readback.hourly.first().temperatureC)
        assertNull(readback.hourly.first().precipitationProbabilityPercent)
        assertEquals(
            listOf(
                Instant.parse("2026-08-26T12:00:00Z"),
                Instant.parse("2026-08-26T11:00:00Z"),
            ),
            readback.hourly.map { it.time },
        )
        assertNull(readback.daily.first().lowC)
        assertNull(readback.daily.first().sunrise)
        assertEquals(listOf(20692L, 20691L), readback.daily.map { it.dateEpochDay })
    }

    @Test
    fun preservesProvenanceLocationAndForecastTimestamps() {
        val bundle = fullBundle(chicago, providerId = "met-norway")

        storage.replaceBundle(bundle)

        val readback = requireNotNull(storage.readBundle(chicago.id))
        assertEquals(chicago.id, readback.location.id)
        assertEquals(chicago.displayName, readback.location.displayName)
        assertEquals(chicago.point, readback.location.point)
        assertEquals(chicago.elevationMeters, readback.location.elevationMeters)
        assertEquals(chicago.zoneId, readback.location.zoneId)
        assertEquals(Instant.parse("2026-08-26T10:20:00Z"), readback.fetchedAt)
        assertEquals(provenance("met-norway"), requireNotNull(readback.current).provenance)
        assertEquals(provenance("met-norway"), readback.hourly.first().provenance)
        assertEquals(provenance("met-norway"), readback.daily.first().provenance)
    }

    @Test
    fun rejectsAlertAndAirQualityPayloadsAtForecastOnlyBoundary() {
        assertThrowsIllegalArgument {
            storage.replaceBundle(fullBundle(chicago, "open-meteo").copy(alerts = listOf(alert())))
        }

        assertThrowsIllegalArgument {
            storage.replaceBundle(fullBundle(chicago, "open-meteo").copy(airQuality = airQuality()))
        }

        assertNull(storage.readBundle(chicago.id))
    }

    @Test
    fun roomFailureAfterProviderSuccessKeepsProviderWeatherVisible() {
        val failingStorage = RoomForecastCacheStorage(database) {
            error("injected local Room transaction failure")
        }
        val providerBundle = fullBundle(chicago, "open-meteo")

        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(providerBundle)),
            storage = failingStorage,
        ).refresh(chicago).terminalSuccess()

        assertEquals(providerBundle, success.weather)
    }

    private fun inMemoryDatabase(): OxygenForecastCacheDatabase =
        Room.inMemoryDatabaseBuilder(
            targetContext(),
            OxygenForecastCacheDatabase::class.java,
        ).build()

    private fun targetContext(): Context =
        InstrumentationRegistry.getInstrumentation().targetContext

    private val chicago = WeatherLocation(
        id = LocationId("manual-chicago"),
        displayName = "Chicago, Illinois",
        point = GeoPoint(41.875, -87.625),
        elevationMeters = 181.0,
        zoneId = ZoneId.of("America/Chicago"),
    )

    private val madison = WeatherLocation(
        id = LocationId("manual-madison"),
        displayName = "Madison, Wisconsin",
        point = GeoPoint(43.0747, -89.3844),
        zoneId = ZoneId.of("America/Chicago"),
    )

    private fun fullBundle(
        location: WeatherLocation,
        providerId: String,
        temperatureC: Double? = 21.5,
    ): WeatherBundle {
        val provenance = provenance(providerId)
        return WeatherBundle(
            location = location,
            current = CurrentConditions(
                time = Instant.parse("2026-08-26T10:00:00Z"),
                temperatureC = temperatureC,
                apparentTemperatureC = 22.0,
                dewPointC = 12.5,
                humidityPercent = 61,
                pressureHpa = 1013.2,
                visibilityMeters = 14000.0,
                cloudCoverPercent = 40,
                wind = Wind(
                    speedMetersPerSecond = 4.1,
                    gustMetersPerSecond = 8.2,
                    directionDegrees = 220.0,
                ),
                precipitationMm = 0.1,
                condition = WeatherCondition.PARTLY_CLOUDY,
                provenance = provenance,
            ),
            hourly = listOf(
                HourlyForecast(
                    time = Instant.parse("2026-08-26T11:00:00Z"),
                    temperatureC = 23.0,
                    precipitationProbabilityPercent = 30,
                    precipitationMm = 0.2,
                    condition = WeatherCondition.RAIN,
                    provenance = provenance,
                ),
                HourlyForecast(
                    time = Instant.parse("2026-08-26T12:00:00Z"),
                    temperatureC = 24.0,
                    precipitationProbabilityPercent = 35,
                    precipitationMm = 0.0,
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    provenance = provenance,
                ),
            ),
            daily = listOf(
                DailyForecast(
                    dateEpochDay = 20691,
                    highC = 25.0,
                    lowC = 17.0,
                    precipitationProbabilityPercent = 55,
                    condition = WeatherCondition.RAIN_SHOWERS,
                    sunrise = Instant.parse("2026-08-26T11:10:00Z"),
                    sunset = Instant.parse("2026-08-27T00:31:00Z"),
                    provenance = provenance,
                ),
            ),
            fetchedAt = Instant.parse("2026-08-26T10:20:00Z"),
        )
    }

    private fun sparseBundle(location: WeatherLocation): WeatherBundle {
        val provenance = provenance("open-meteo")
        return WeatherBundle(
            location = location,
            current = CurrentConditions(
                time = Instant.parse("2026-08-26T10:00:00Z"),
                temperatureC = null,
                apparentTemperatureC = null,
                humidityPercent = null,
                wind = null,
                condition = WeatherCondition.UNKNOWN,
                provenance = provenance,
            ),
            hourly = listOf(
                HourlyForecast(
                    time = Instant.parse("2026-08-26T12:00:00Z"),
                    temperatureC = null,
                    precipitationProbabilityPercent = null,
                    condition = WeatherCondition.UNKNOWN,
                    provenance = provenance,
                ),
                HourlyForecast(
                    time = Instant.parse("2026-08-26T11:00:00Z"),
                    temperatureC = 18.0,
                    precipitationProbabilityPercent = 10,
                    condition = WeatherCondition.CLOUDY,
                    provenance = provenance,
                ),
            ),
            daily = listOf(
                DailyForecast(
                    dateEpochDay = 20692,
                    highC = null,
                    lowC = null,
                    condition = WeatherCondition.UNKNOWN,
                    provenance = provenance,
                ),
                DailyForecast(
                    dateEpochDay = 20691,
                    highC = 24.0,
                    lowC = 16.0,
                    condition = WeatherCondition.CLEAR,
                    sunrise = Instant.parse("2026-08-26T11:10:00Z"),
                    sunset = Instant.parse("2026-08-27T00:31:00Z"),
                    provenance = provenance,
                ),
            ),
            fetchedAt = Instant.parse("2026-08-26T10:20:00Z"),
        )
    }

    private fun provenance(providerId: String): DataProvenance =
        DataProvenance(
            providerId = providerId,
            sourceName = "Forecast source",
            issuedAt = Instant.parse("2026-08-26T10:15:00Z"),
            fetchedAt = Instant.parse("2026-08-26T10:20:00Z"),
            type = DataType.FORECAST,
            licenseId = "CC-BY-4.0",
        )

    private fun alert(): WeatherAlert =
        WeatherAlert(
            id = "alert-1",
            event = "Test Alert",
            severity = AlertSeverity.MINOR,
            issuer = "Test issuer",
            provenance = provenance("nws"),
        )

    private fun airQuality(): AirQuality =
        AirQuality(
            timestamp = Instant.parse("2026-08-26T10:00:00Z"),
            aqi = 42,
            standardName = "Test AQI",
            provenance = provenance("open-meteo-air-quality"),
        )
}

private class FixedWeatherRepository(
    private vararg val results: WeatherRepositoryResult,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = results.asSequence()
}

private fun Sequence<WeatherRepositoryResult>.terminalSuccess(): WeatherRepositoryResult.Success =
    first { it !is WeatherRepositoryResult.Loading } as WeatherRepositoryResult.Success

private fun Sequence<WeatherRepositoryResult>.terminalFailure(): WeatherRepositoryResult.Failure =
    first { it !is WeatherRepositoryResult.Loading } as WeatherRepositoryResult.Failure

private fun assertThrowsIllegalState(block: () -> Unit) {
    try {
        block()
    } catch (_: IllegalStateException) {
        return
    }
    throw AssertionError("Expected IllegalStateException")
}

private fun assertThrowsIllegalArgument(block: () -> Unit) {
    try {
        block()
    } catch (_: IllegalArgumentException) {
        return
    }
    throw AssertionError("Expected IllegalArgumentException")
}

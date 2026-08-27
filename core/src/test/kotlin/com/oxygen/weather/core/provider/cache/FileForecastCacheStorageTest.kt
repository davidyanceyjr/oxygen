package com.oxygen.weather.core.provider.cache

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
import java.io.File
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.Rule

class FileForecastCacheStorageTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun durableFileStorageRoundTripsOneForecastBundleAcrossInstances() {
        val directory = temporaryFolder.newFolder("forecast-cache")
        val bundle = fullBundle(location("manual-chicago"), "open-meteo")

        FileForecastCacheStorage(directory).replaceBundle(bundle)
        val readback = FileForecastCacheStorage(directory).readBundle(bundle.location.id)

        assertEquals(bundle, readback)
    }

    @Test
    fun preservesNullForecastFieldsWithoutDefaultFabrication() {
        val directory = temporaryFolder.newFolder("forecast-cache")
        val bundle = sparseBundle(location("manual-chicago"), "met-norway")

        FileForecastCacheStorage(directory).replaceBundle(bundle)
        val readback = requireNotNull(FileForecastCacheStorage(directory).readBundle(bundle.location.id))

        val current = requireNotNull(readback.current)
        assertNull(current.temperatureC)
        assertNull(current.apparentTemperatureC)
        assertNull(current.humidityPercent)
        assertNull(current.wind)
        assertNull(readback.hourly.single().temperatureC)
        assertNull(readback.hourly.single().precipitationProbabilityPercent)
        assertNull(readback.daily.single().lowC)
        assertNull(readback.daily.single().sunrise)
    }

    @Test
    fun locationIdScopesStoredBundles() {
        val directory = temporaryFolder.newFolder("forecast-cache")
        val chicago = fullBundle(location("manual-chicago"), "open-meteo")
        val madison = fullBundle(location("manual-madison"), "met-norway")

        val storage = FileForecastCacheStorage(directory)
        storage.replaceBundle(chicago)
        storage.replaceBundle(madison)

        assertEquals(chicago, storage.readBundle(chicago.location.id))
        assertEquals(madison, storage.readBundle(madison.location.id))
    }

    @Test
    fun failedReplacementDoesNotExposePartialBundleForEmptyLocation() {
        val directory = temporaryFolder.newFolder("forecast-cache")
        val storage = FileForecastCacheStorage(directory) { error("injected failure before commit") }
        val bundle = fullBundle(location("manual-chicago"), "open-meteo")

        assertThrowsIllegalState { storage.replaceBundle(bundle) }

        assertNull(FileForecastCacheStorage(directory).readBundle(bundle.location.id))
        assertTrue(directory.listFiles().orEmpty().none { it.extension == "forecast" })
    }

    @Test
    fun failedReplacementLeavesPreviousBundleVisible() {
        val directory = temporaryFolder.newFolder("forecast-cache")
        val oldBundle = fullBundle(location("manual-chicago"), "open-meteo", temperatureC = 20.0)
        val newBundle = fullBundle(location("manual-chicago"), "open-meteo", temperatureC = 30.0)

        FileForecastCacheStorage(directory).replaceBundle(oldBundle)
        val failingStorage = FileForecastCacheStorage(directory) { error("injected failure before commit") }

        assertThrowsIllegalState { failingStorage.replaceBundle(newBundle) }

        assertEquals(oldBundle, FileForecastCacheStorage(directory).readBundle(oldBundle.location.id))
    }

    private fun location(id: String): WeatherLocation =
        WeatherLocation(
            id = LocationId(id),
            displayName = id,
            point = GeoPoint(41.875, -87.625),
            elevationMeters = 181.0,
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

    private fun sparseBundle(location: WeatherLocation, providerId: String): WeatherBundle {
        val provenance = provenance(providerId)
        return WeatherBundle(
            location = location,
            current = CurrentConditions(
                time = Instant.parse("2026-08-26T10:00:00Z"),
                temperatureC = null,
                condition = WeatherCondition.UNKNOWN,
                provenance = provenance,
            ),
            hourly = listOf(
                HourlyForecast(
                    time = Instant.parse("2026-08-26T11:00:00Z"),
                    temperatureC = null,
                    condition = WeatherCondition.UNKNOWN,
                    provenance = provenance,
                ),
            ),
            daily = listOf(
                DailyForecast(
                    dateEpochDay = 20691,
                    highC = null,
                    lowC = null,
                    condition = WeatherCondition.UNKNOWN,
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
}

private fun assertThrowsIllegalState(block: () -> Unit) {
    try {
        block()
    } catch (_: IllegalStateException) {
        return
    }
    throw AssertionError("Expected IllegalStateException")
}

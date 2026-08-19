package com.oxygen.weather.core.provider.openmeteo

import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpenMeteoForecastMapperTest {
    private val fetchedAt = Instant.parse("2026-08-19T13:20:00Z")
    private val chicago = WeatherLocation(
        id = LocationId("manual-chicago"),
        displayName = "Chicago, Illinois",
        point = GeoPoint(41.875, -87.625),
        elevationMeters = 182.0,
        zoneId = ZoneId.of("America/Chicago"),
    )

    @Test
    fun mapsHomeForecastToProviderNeutralDomain() {
        val bundle = OpenMeteoForecastMapper.map(
            location = chicago,
            response = parsedFixture("home_forecast_normal.json"),
            fetchedAt = fetchedAt,
        )

        assertEquals(chicago, bundle.location)
        assertEquals(fetchedAt, bundle.fetchedAt)

        val current = requireNotNull(bundle.current)
        assertEquals(Instant.parse("2026-08-19T13:15:00Z"), current.time)
        assertEquals(22.4, requireNotNull(current.temperatureC), 0.0)
        assertEquals(23.1, requireNotNull(current.apparentTemperatureC), 0.0)
        assertEquals(61, current.humidityPercent)
        assertEquals(1015.4, requireNotNull(current.pressureHpa), 0.0)
        assertEquals(38, current.cloudCoverPercent)
        assertEquals(0.0, requireNotNull(current.precipitationMm), 0.0)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, current.condition)
        assertEquals(14.2 / 3.6, requireNotNull(requireNotNull(current.wind).speedMetersPerSecond), 0.000001)
        assertEquals(24.5 / 3.6, requireNotNull(current.wind.gustMetersPerSecond), 0.000001)
        assertEquals(230.0, requireNotNull(current.wind.directionDegrees), 0.0)
        assertOpenMeteoProvenance(DataType.MODEL_ESTIMATE, current.provenance)

        assertEquals(3, bundle.hourly.size)
        assertEquals(Instant.parse("2026-08-19T15:00:00Z"), bundle.hourly[2].time)
        assertEquals(24.3, requireNotNull(bundle.hourly[2].temperatureC), 0.0)
        assertEquals(15, bundle.hourly[2].precipitationProbabilityPercent)
        assertEquals(0.1, requireNotNull(bundle.hourly[2].precipitationMm), 0.0)
        assertEquals(WeatherCondition.RAIN, bundle.hourly[2].condition)
        assertOpenMeteoProvenance(DataType.FORECAST, bundle.hourly[2].provenance)

        assertEquals(2, bundle.daily.size)
        assertEquals(LocalDate.of(2026, 8, 20).toEpochDay(), bundle.daily[1].dateEpochDay)
        assertEquals(26.1, requireNotNull(bundle.daily[1].highC), 0.0)
        assertEquals(17.6, requireNotNull(bundle.daily[1].lowC), 0.0)
        assertEquals(72, bundle.daily[1].precipitationProbabilityPercent)
        assertEquals(WeatherCondition.RAIN, bundle.daily[1].condition)
        assertEquals(Instant.parse("2026-08-20T11:05:00Z"), bundle.daily[1].sunrise)
        assertEquals(Instant.parse("2026-08-21T00:43:00Z"), bundle.daily[1].sunset)
        assertOpenMeteoProvenance(DataType.FORECAST, bundle.daily[1].provenance)
    }

    @Test
    fun preservesMissingValuesAsNullAndUnknownCondition() {
        val bundle = OpenMeteoForecastMapper.map(
            location = chicago,
            response = parsedFixture("home_forecast_missing_optional.json"),
            fetchedAt = fetchedAt,
        )

        val current = requireNotNull(bundle.current)
        assertNull(current.temperatureC)
        assertNull(current.apparentTemperatureC)
        assertNull(current.humidityPercent)
        assertNull(current.precipitationMm)
        assertNull(current.wind)
        assertEquals(WeatherCondition.UNKNOWN, current.condition)

        assertNull(bundle.hourly[0].temperatureC)
        assertNull(bundle.hourly[0].precipitationProbabilityPercent)
        assertEquals(WeatherCondition.UNKNOWN, bundle.hourly[0].condition)

        assertNull(bundle.daily[0].highC)
        assertNull(bundle.daily[0].lowC)
        assertNull(bundle.daily[0].precipitationProbabilityPercent)
        assertEquals(WeatherCondition.UNKNOWN, bundle.daily[0].condition)
        assertNull(bundle.daily[0].sunrise)
        assertNull(bundle.daily[0].sunset)
    }

    @Test
    fun mapsTimestampsUsingProviderTimezoneNotPhoneTimezone() {
        val tokyo = chicago.copy(
            id = LocationId("manual-tokyo"),
            displayName = "Tokyo, Japan",
            point = GeoPoint(35.6895, 139.6917),
            zoneId = ZoneId.of("Asia/Tokyo"),
        )

        val bundle = OpenMeteoForecastMapper.map(
            location = tokyo,
            response = parsedFixture("home_forecast_timezone_sensitive.json"),
            fetchedAt = fetchedAt,
        )

        assertEquals(Instant.parse("2026-12-31T14:30:00Z"), requireNotNull(bundle.current).time)
        assertEquals(Instant.parse("2026-12-31T15:00:00Z"), bundle.hourly[1].time)
        assertEquals(LocalDate.of(2027, 1, 1).toEpochDay(), bundle.daily[1].dateEpochDay)
        assertEquals(Instant.parse("2026-12-31T21:50:00Z"), bundle.daily[1].sunrise)
        assertEquals(Instant.parse("2027-01-01T07:39:00Z"), bundle.daily[1].sunset)
    }

    @Test
    fun mapsContractedWeatherCodesAndUnknowns() {
        val expected = mapOf(
            null to WeatherCondition.UNKNOWN,
            0 to WeatherCondition.CLEAR,
            1 to WeatherCondition.MOSTLY_CLEAR,
            2 to WeatherCondition.PARTLY_CLOUDY,
            3 to WeatherCondition.CLOUDY,
            45 to WeatherCondition.FOG,
            48 to WeatherCondition.FOG,
            51 to WeatherCondition.DRIZZLE,
            53 to WeatherCondition.DRIZZLE,
            55 to WeatherCondition.DRIZZLE,
            56 to WeatherCondition.FREEZING_DRIZZLE,
            57 to WeatherCondition.FREEZING_DRIZZLE,
            61 to WeatherCondition.RAIN,
            63 to WeatherCondition.RAIN,
            65 to WeatherCondition.RAIN,
            66 to WeatherCondition.FREEZING_RAIN,
            67 to WeatherCondition.FREEZING_RAIN,
            71 to WeatherCondition.SNOW,
            73 to WeatherCondition.SNOW,
            75 to WeatherCondition.SNOW,
            77 to WeatherCondition.SNOW,
            80 to WeatherCondition.RAIN_SHOWERS,
            81 to WeatherCondition.RAIN_SHOWERS,
            82 to WeatherCondition.RAIN_SHOWERS,
            85 to WeatherCondition.SNOW_SHOWERS,
            86 to WeatherCondition.SNOW_SHOWERS,
            95 to WeatherCondition.THUNDERSTORM,
            96 to WeatherCondition.THUNDERSTORM_HAIL,
            99 to WeatherCondition.THUNDERSTORM_HAIL,
            -1 to WeatherCondition.UNKNOWN,
            999 to WeatherCondition.UNKNOWN,
        )

        expected.forEach { (code, condition) ->
            assertEquals("code $code", condition, OpenMeteoForecastMapper.mapWeatherCode(code))
        }
    }

    private fun assertOpenMeteoProvenance(type: DataType, provenance: com.oxygen.weather.core.model.DataProvenance) {
        assertEquals("open-meteo", provenance.providerId)
        assertEquals("Open-Meteo", provenance.sourceName)
        assertEquals("CC-BY-4.0", provenance.licenseId)
        assertEquals(fetchedAt, provenance.fetchedAt)
        assertEquals(type, provenance.type)
        assertNull(provenance.issuedAt)
    }

    private fun parsedFixture(name: String): OpenMeteoForecastResponse =
        OpenMeteoForecastParser.parseForecast(fixture(name))

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/openmeteo/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}

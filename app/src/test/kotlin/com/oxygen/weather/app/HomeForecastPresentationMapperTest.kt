package com.oxygen.weather.app

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
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeForecastPresentationMapperTest {
    @Test
    fun `mapper keeps canonical values separate from formatted Home text`() {
        val presentation = fullWeatherBundle().toHomeSuccessPresentation(testLocation)

        val current = requireNotNull(presentation.current)
        assertEquals("65 deg F", current.temperature)
        assertEquals(18.4, current.temperatureC)
        assertEquals("Feels like 63 deg F", current.apparentTemperature)
        assertEquals(17.2, current.apparentTemperatureC)
        assertEquals("H 73 deg F", current.highTemperature)
        assertEquals(22.7, current.highTemperatureC)
        assertEquals("L 54 deg F", current.lowTemperature)
        assertEquals(12.3, current.lowTemperatureC)
        assertEquals(WeatherCondition.RAIN_SHOWERS, current.conditionIdentity)

        assertEquals("64 deg F", presentation.hourly.single().temperature)
        assertEquals(18.0, presentation.hourly.single().temperatureC)
        assertEquals("60%", presentation.hourly.single().precipitationProbability)
        assertEquals(60, presentation.hourly.single().precipitationProbabilityPercent)
        assertEquals(WeatherCondition.RAIN, presentation.hourly.single().conditionIdentity)

        assertEquals("High 73 deg F", presentation.daily.single().high)
        assertEquals(22.7, presentation.daily.single().highC)
        assertEquals("40%", presentation.daily.single().precipitationProbability)
        assertEquals(40, presentation.daily.single().precipitationProbabilityPercent)
        assertEquals(WeatherCondition.RAIN_SHOWERS, presentation.daily.single().conditionIdentity)

        assertEquals(
            HomeMetricNumericValues.Wind(
                speedMetersPerSecond = 4.0,
                gustMetersPerSecond = 7.0,
                directionDegrees = 225.0,
            ),
            presentation.metrics.single { it.identity == HomeMetricIdentity.Wind }.numericValues,
        )
        assertEquals(
            HomeMetricNumericValues.DistanceMeters(9500.0),
            presentation.metrics.single { it.identity == HomeMetricIdentity.Visibility }.numericValues,
        )
        assertEquals(
            HomeMetricNumericValues.PressureHpa(1012.4),
            presentation.metrics.single { it.identity == HomeMetricIdentity.Pressure }.numericValues,
        )
    }

    @Test
    fun `mapper retains missing semantic values as null instead of parsing unavailable text`() {
        val weather = fullWeatherBundle().copy(
            current = fullWeatherBundle().current?.copy(
                temperatureC = null,
                apparentTemperatureC = null,
                dewPointC = null,
                humidityPercent = null,
                pressureHpa = null,
                visibilityMeters = null,
                cloudCoverPercent = null,
                wind = null,
                precipitationMm = null,
            ),
            hourly = listOf(fullWeatherBundle().hourly.single().copy(
                temperatureC = null,
                precipitationProbabilityPercent = null,
            )),
            daily = listOf(fullWeatherBundle().daily.single().copy(
                highC = null,
                lowC = null,
                precipitationProbabilityPercent = null,
            )),
        )

        val presentation = weather.toHomeSuccessPresentation(testLocation)
        val current = requireNotNull(presentation.current)
        assertEquals("Unavailable", current.temperature)
        assertNull(current.temperatureC)
        assertEquals("Feels like unavailable", current.apparentTemperature)
        assertNull(current.apparentTemperatureC)
        assertNull(current.highTemperatureC)
        assertNull(current.lowTemperatureC)
        assertEquals(WeatherCondition.RAIN_SHOWERS, current.conditionIdentity)

        assertEquals("Unavailable", presentation.hourly.single().temperature)
        assertNull(presentation.hourly.single().temperatureC)
        assertNull(presentation.hourly.single().precipitationProbability)
        assertNull(presentation.hourly.single().precipitationProbabilityPercent)
        assertEquals(WeatherCondition.RAIN, presentation.hourly.single().conditionIdentity)

        assertEquals("High unavailable", presentation.daily.single().high)
        assertNull(presentation.daily.single().highC)
        assertNull(presentation.daily.single().lowC)
        assertNull(presentation.daily.single().precipitationProbabilityPercent)
        assertEquals(WeatherCondition.RAIN_SHOWERS, presentation.daily.single().conditionIdentity)

        assertEquals(
            HomeMetricNumericValues.TemperatureC(null),
            presentation.metrics.single { it.identity == HomeMetricIdentity.ApparentTemperature }.numericValues,
        )
    }

    private fun fullWeatherBundle(): WeatherBundle =
        WeatherBundle(
            location = testLocation,
            current = CurrentConditions(
                time = Instant.parse("2026-08-22T10:30:00Z"),
                temperatureC = 18.4,
                apparentTemperatureC = 17.2,
                dewPointC = 11.6,
                humidityPercent = 72,
                pressureHpa = 1012.4,
                visibilityMeters = 9500.0,
                cloudCoverPercent = 88,
                wind = Wind(4.0, 7.0, 225.0),
                precipitationMm = 0.4,
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
                    provenance = provenance.copy(type = DataType.FORECAST),
                ),
            ),
            fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
        )

    private companion object {
        val testLocation = WeatherLocation(
            id = LocationId("mapper-test"),
            displayName = "Mapper City",
            point = GeoPoint(43.0731, -89.4012),
            zoneId = ZoneId.of("America/Chicago"),
        )
        val provenance = DataProvenance(
            providerId = "mapper-provider",
            sourceName = "Mapper Provider",
            fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
            type = DataType.MODEL_ESTIMATE,
        )
    }
}

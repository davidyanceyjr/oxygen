package com.oxygen.weather.core.provider.openmeteo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class OpenMeteoForecastParserTest {
    @Test
    fun parsesHomeForecastFixture() {
        val forecast = OpenMeteoForecastParser.parseForecast(fixture("home_forecast_normal.json"))

        assertEquals(41.875, forecast.latitude, 0.0)
        assertEquals(-87.625, forecast.longitude, 0.0)
        assertEquals("America/Chicago", forecast.timezone)
        assertEquals(-18000, forecast.utcOffsetSeconds)
        assertEquals("2026-08-19T08:15", forecast.current.time)
        assertEquals(22.4, requireNotNull(forecast.current.temperature2m), 0.0)
        assertEquals(2, forecast.current.weatherCode)
        assertEquals(3, forecast.hourly.time.size)
        assertEquals(61, forecast.hourly.weatherCode[2])
        assertEquals(2, forecast.daily.time.size)
        assertEquals("2026-08-20T19:43", forecast.daily.sunset[1])
    }

    @Test
    fun preservesMissingWeatherValuesAsNull() {
        val forecast = OpenMeteoForecastParser.parseForecast(fixture("home_forecast_missing_optional.json"))

        assertNull(forecast.current.temperature2m)
        assertNull(forecast.current.relativeHumidity2m)
        assertNull(forecast.current.weatherCode)
        assertNull(forecast.hourly.temperature2m[0])
        assertEquals(23.0, requireNotNull(forecast.hourly.temperature2m[1]), 0.0)
        assertNull(forecast.hourly.precipitationProbability[0])
        assertNull(forecast.daily.temperature2mMax[0])
        assertNull(forecast.daily.weatherCode[0])
    }

    @Test
    fun failsDeterministicallyForMissingRequiredEnvelope() {
        val error = assertThrows(OpenMeteoParseException.MissingField::class.java) {
            OpenMeteoForecastParser.parseForecast(fixture("home_forecast_malformed_envelope.json"))
        }

        assertEquals("daily", error.fieldPath)
    }

    @Test
    fun reportsProviderErrorBody() {
        val error = assertThrows(OpenMeteoParseException.ProviderError::class.java) {
            OpenMeteoForecastParser.parseForecast(fixture("error_response.json"))
        }

        assertEquals("Parameter 'latitude' and 'longitude' must have the same number of elements", error.reason)
    }

    @Test
    fun preservesInvalidWeatherCodeForLaterMapping() {
        val forecast = OpenMeteoForecastParser.parseForecast(fixture("home_forecast_invalid_weather_code.json"))

        assertEquals(999, forecast.current.weatherCode)
        assertEquals(999, forecast.hourly.weatherCode.single())
        assertEquals(999, forecast.daily.weatherCode.single())
    }

    @Test
    fun parsesTimezoneMetadataWithoutPhoneTimezone() {
        val forecast = OpenMeteoForecastParser.parseForecast(fixture("home_forecast_timezone_sensitive.json"))

        assertEquals("Asia/Tokyo", forecast.timezone)
        assertEquals("GMT+9", forecast.timezoneAbbreviation)
        assertEquals(32400, forecast.utcOffsetSeconds)
        assertEquals("2026-12-31T23:30", forecast.current.time)
        assertEquals("2027-01-01T00:00", forecast.hourly.time[1])
    }

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/openmeteo/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}

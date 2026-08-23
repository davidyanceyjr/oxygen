package com.oxygen.weather.core.provider.metno

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class MetNoForecastParserTest {
    @Test
    fun parsesHomeForecastCompactFixture() {
        val forecast = MetNoForecastParser.parseForecast(fixture("home_forecast_normal.json"))

        assertEquals("Feature", forecast.type)
        assertEquals(-87.625, forecast.geometry.longitude, 0.0)
        assertEquals(41.875, forecast.geometry.latitude, 0.0)
        assertEquals(181.0, requireNotNull(forecast.geometry.altitudeMeters), 0.0)
        assertEquals("2026-08-23T10:15:00Z", forecast.meta.updatedAt)
        assertEquals("celsius", forecast.meta.units["air_temperature"])
        assertEquals("m/s", forecast.meta.units["wind_speed"])
        assertEquals(2, forecast.timeseries.size)

        val first = forecast.timeseries.first()
        assertEquals("2026-08-23T11:00:00Z", first.time)
        assertEquals(22.4, requireNotNull(first.instant.details.airTemperature), 0.0)
        assertEquals(1012.3, requireNotNull(first.instant.details.airPressureAtSeaLevel), 0.0)
        assertEquals(37.5, requireNotNull(first.instant.details.cloudAreaFraction), 0.0)
        assertEquals(16.1, requireNotNull(first.instant.details.dewPointTemperature), 0.0)
        assertEquals(68.0, requireNotNull(first.instant.details.relativeHumidity), 0.0)
        assertEquals(220.0, requireNotNull(first.instant.details.windFromDirection), 0.0)
        assertEquals(4.1, requireNotNull(first.instant.details.windSpeed), 0.0)
        val next1Hours = requireNotNull(first.next1Hours)
        val next6Hours = requireNotNull(first.next6Hours)
        val next12Hours = requireNotNull(first.next12Hours)
        val next1Details = requireNotNull(next1Hours.details)
        val next6Details = requireNotNull(next6Hours.details)
        assertEquals("partlycloudy_day", next1Hours.summary?.symbolCode)
        assertEquals(0.2, requireNotNull(next1Details.precipitationAmount), 0.0)
        assertEquals(35.0, requireNotNull(next1Details.probabilityOfPrecipitation), 0.0)
        assertEquals("rain", next6Hours.summary?.symbolCode)
        assertEquals(19.1, requireNotNull(next6Details.airTemperatureMin), 0.0)
        assertEquals("cloudy", next12Hours.summary?.symbolCode)
    }

    @Test
    fun preservesMissingWeatherValuesAsNull() {
        val forecast = MetNoForecastParser.parseForecast(fixture("home_forecast_missing_optional.json"))

        assertNull(forecast.geometry.altitudeMeters)
        assertNull(forecast.timeseries[0].instant.details.airTemperature)
        assertNull(forecast.timeseries[0].instant.details.relativeHumidity)
        assertNull(forecast.timeseries[0].instant.details.windSpeed)
        assertNull(forecast.timeseries[0].next1Hours?.summary?.symbolCode)
        assertNull(forecast.timeseries[0].next1Hours?.details?.precipitationAmount)
        assertNull(forecast.timeseries[1].next1Hours)
        assertEquals(23.0, requireNotNull(forecast.timeseries[1].instant.details.airTemperature), 0.0)
    }

    @Test
    fun failsDeterministicallyForMissingRequiredEnvelope() {
        val error = assertThrows(MetNoParseException.MissingField::class.java) {
            MetNoForecastParser.parseForecast(fixture("home_forecast_malformed_envelope.json"))
        }

        assertEquals("timeseries", error.fieldPath)
    }

    @Test
    fun preservesUnexpectedUnitMetadataForLaterValidation() {
        val forecast = MetNoForecastParser.parseForecast(fixture("home_forecast_unexpected_units.json"))

        assertEquals("kelvin", forecast.meta.units["air_temperature"])
        assertEquals("knots", forecast.meta.units["wind_speed"])
        assertEquals("inches", forecast.meta.units["precipitation_amount"])
        assertEquals(295.55, requireNotNull(forecast.timeseries.single().instant.details.airTemperature), 0.0)
    }

    @Test
    fun preservesUnknownSymbolCodeForLaterMapping() {
        val forecast = MetNoForecastParser.parseForecast(fixture("home_forecast_unknown_symbol.json"))

        assertEquals("volcanic_ash_day", forecast.timeseries.single().next1Hours?.summary?.symbolCode)
    }

    @Test
    fun preservesDocumentedTypoSymbolCodeForLaterMapping() {
        val forecast = MetNoForecastParser.parseForecast(fixture("home_forecast_documented_typo_symbol.json"))

        assertEquals("lightssnowshowersandthunder_day", forecast.timeseries.single().next1Hours?.summary?.symbolCode)
    }

    @Test
    fun parsesUtcTimeMetadataWithoutPhoneTimezone() {
        val forecast = MetNoForecastParser.parseForecast(fixture("home_forecast_timezone_sensitive_utc.json"))

        assertEquals("2026-12-31T14:30:00Z", forecast.meta.updatedAt)
        assertEquals("2026-12-31T14:00:00Z", forecast.timeseries[0].time)
        assertEquals("2026-12-31T15:00:00Z", forecast.timeseries[1].time)
        assertEquals("fair_night", forecast.timeseries[0].next1Hours?.summary?.symbolCode)
    }

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/metno/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}

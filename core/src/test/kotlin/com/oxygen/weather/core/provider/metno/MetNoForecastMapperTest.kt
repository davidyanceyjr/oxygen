package com.oxygen.weather.core.provider.metno

import com.oxygen.weather.core.model.DataProvenance
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
import org.junit.Assert.assertThrows
import org.junit.Test

class MetNoForecastMapperTest {
    private val fetchedAt = Instant.parse("2026-08-23T13:20:00Z")
    private val chicago = WeatherLocation(
        id = LocationId("manual-chicago"),
        displayName = "Chicago, Illinois",
        point = GeoPoint(41.875, -87.625),
        elevationMeters = 182.0,
        zoneId = ZoneId.of("America/Chicago"),
    )

    @Test
    fun mapsHomeForecastToProviderNeutralDomain() {
        val bundle = MetNoForecastMapper.map(
            location = chicago,
            response = parsedFixture("home_forecast_normal.json"),
            fetchedAt = fetchedAt,
        )

        assertEquals(chicago, bundle.location)
        assertEquals(fetchedAt, bundle.fetchedAt)

        val current = requireNotNull(bundle.current)
        assertEquals(Instant.parse("2026-08-23T11:00:00Z"), current.time)
        assertEquals(22.4, requireNotNull(current.temperatureC), 0.0)
        assertNull(current.apparentTemperatureC)
        assertEquals(16.1, requireNotNull(current.dewPointC), 0.0)
        assertEquals(68, current.humidityPercent)
        assertEquals(1012.3, requireNotNull(current.pressureHpa), 0.0)
        assertNull(current.visibilityMeters)
        assertEquals(38, current.cloudCoverPercent)
        assertEquals(0.2, requireNotNull(current.precipitationMm), 0.0)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, current.condition)
        assertEquals(4.1, requireNotNull(requireNotNull(current.wind).speedMetersPerSecond), 0.0)
        assertEquals(8.7, requireNotNull(current.wind.gustMetersPerSecond), 0.0)
        assertEquals(220.0, requireNotNull(current.wind.directionDegrees), 0.0)
        assertMetNoProvenance(DataType.MODEL_ESTIMATE, current.provenance)

        assertEquals(2, bundle.hourly.size)
        assertEquals(Instant.parse("2026-08-23T12:00:00Z"), bundle.hourly[1].time)
        assertEquals(23.0, requireNotNull(bundle.hourly[1].temperatureC), 0.0)
        assertEquals(48, bundle.hourly[1].precipitationProbabilityPercent)
        assertEquals(0.5, requireNotNull(bundle.hourly[1].precipitationMm), 0.0)
        assertEquals(WeatherCondition.RAIN_SHOWERS, bundle.hourly[1].condition)
        assertMetNoProvenance(DataType.FORECAST, bundle.hourly[1].provenance)

        assertEquals(1, bundle.daily.size)
        val daily = bundle.daily.single()
        assertEquals(LocalDate.of(2026, 8, 23).toEpochDay(), daily.dateEpochDay)
        assertEquals(24.9, requireNotNull(daily.highC), 0.0)
        assertEquals(18.7, requireNotNull(daily.lowC), 0.0)
        assertEquals(78, daily.precipitationProbabilityPercent)
        assertEquals(WeatherCondition.CLOUDY, daily.condition)
        assertNull(daily.sunrise)
        assertNull(daily.sunset)
        assertMetNoProvenance(DataType.FORECAST, daily.provenance)
    }

    @Test
    fun preservesMissingValuesAsNullAndUnknownCondition() {
        val bundle = MetNoForecastMapper.map(
            location = chicago,
            response = parsedFixture("home_forecast_missing_optional.json"),
            fetchedAt = fetchedAt,
        )

        val current = requireNotNull(bundle.current)
        assertNull(current.temperatureC)
        assertNull(current.apparentTemperatureC)
        assertNull(current.dewPointC)
        assertNull(current.humidityPercent)
        assertNull(current.pressureHpa)
        assertNull(current.cloudCoverPercent)
        assertNull(current.precipitationMm)
        assertNull(current.wind)
        assertEquals(WeatherCondition.UNKNOWN, current.condition)

        assertNull(bundle.hourly[0].temperatureC)
        assertNull(bundle.hourly[0].precipitationProbabilityPercent)
        assertNull(bundle.hourly[0].precipitationMm)
        assertEquals(WeatherCondition.UNKNOWN, bundle.hourly[0].condition)

        assertEquals(23.0, requireNotNull(bundle.daily.single().highC), 0.0)
        assertNull(bundle.daily.single().precipitationProbabilityPercent)
        assertEquals(WeatherCondition.UNKNOWN, bundle.daily.single().condition)
    }

    @Test
    fun mapsUtcTimestampsAndGroupsDailyRowsBySelectedLocationTimezone() {
        val tokyo = WeatherLocation(
            id = LocationId("manual-tokyo"),
            displayName = "Tokyo, Japan",
            point = GeoPoint(35.6895, 139.6917),
            elevationMeters = 40.0,
            zoneId = ZoneId.of("Asia/Tokyo"),
        )

        val bundle = MetNoForecastMapper.map(
            location = tokyo,
            response = parsedFixture("home_forecast_timezone_sensitive_utc.json"),
            fetchedAt = fetchedAt,
        )

        assertEquals(Instant.parse("2026-12-31T14:00:00Z"), requireNotNull(bundle.current).time)
        assertEquals(Instant.parse("2026-12-31T15:00:00Z"), bundle.hourly[1].time)
        assertEquals(2, bundle.daily.size)
        assertEquals(LocalDate.of(2026, 12, 31).toEpochDay(), bundle.daily[0].dateEpochDay)
        assertEquals(5.2, requireNotNull(bundle.daily[0].highC), 0.0)
        assertEquals(LocalDate.of(2027, 1, 1).toEpochDay(), bundle.daily[1].dateEpochDay)
        assertEquals(4.9, requireNotNull(bundle.daily[1].lowC), 0.0)
    }

    @Test
    fun usesDailyConditionPeriodPriorityBeforeTimestepOrder() {
        val forecast = parsedFixture("home_forecast_normal.json")
        val response = forecast.copy(
            timeseries = listOf(
                forecast.timeseries[0].copy(
                    next6Hours = null,
                    next12Hours = null,
                ),
                forecast.timeseries[1].copy(
                    next12Hours = MetNoPeriodForecast(
                        summary = MetNoPeriodSummary(symbolCode = "fair_day"),
                        details = null,
                    ),
                ),
            ),
        )

        val bundle = MetNoForecastMapper.map(
            location = chicago,
            response = response,
            fetchedAt = fetchedAt,
        )

        assertEquals(WeatherCondition.MOSTLY_CLEAR, bundle.daily.single().condition)
    }

    @Test
    fun validatesMappedUnitsBeforeMapping() {
        val error = assertThrows(MetNoMapperException.UnexpectedUnit::class.java) {
            MetNoForecastMapper.map(
                location = chicago,
                response = parsedFixture("home_forecast_unexpected_units.json"),
                fetchedAt = fetchedAt,
            )
        }

        assertEquals("air_temperature", error.field)
        assertEquals("celsius", error.expectedUnit)
        assertEquals("kelvin", error.actualUnit)
    }

    @Test
    fun failsDeterministicallyForInvalidTimestampsAndNoUsableTimesteps() {
        val forecast = parsedFixture("home_forecast_normal.json")

        assertThrows(MetNoMapperException.InvalidTimestamp::class.java) {
            MetNoForecastMapper.map(
                location = chicago,
                response = forecast.copy(meta = forecast.meta.copy(updatedAt = "2026-08-23 10:15")),
                fetchedAt = fetchedAt,
            )
        }

        assertThrows(MetNoMapperException.InvalidTimestamp::class.java) {
            MetNoForecastMapper.map(
                location = chicago,
                response = forecast.copy(timeseries = listOf(forecast.timeseries.first().copy(time = "2026-08-23 11:00"))),
                fetchedAt = fetchedAt,
            )
        }

        assertThrows(MetNoMapperException.NoUsableTimeSteps::class.java) {
            MetNoForecastMapper.map(
                location = chicago,
                response = forecast.copy(timeseries = emptyList()),
                fetchedAt = fetchedAt,
            )
        }
    }

    @Test
    fun mapsOfficialWeathericonSymbolStemsAndUnknowns() {
        val expected = officialWeatherIconStems().associateWith { stem ->
            when {
                stem == "clearsky_day" || stem == "clearsky_night" || stem == "clearsky_polartwilight" -> WeatherCondition.CLEAR
                stem == "fair_day" || stem == "fair_night" || stem == "fair_polartwilight" -> WeatherCondition.MOSTLY_CLEAR
                stem == "partlycloudy_day" || stem == "partlycloudy_night" || stem == "partlycloudy_polartwilight" -> WeatherCondition.PARTLY_CLOUDY
                stem == "cloudy" -> WeatherCondition.CLOUDY
                stem == "fog" -> WeatherCondition.FOG
                "andthunder" in stem -> WeatherCondition.THUNDERSTORM
                stem.startsWith("lightrain") -> WeatherCondition.DRIZZLE
                stem.startsWith("rainshowers") || stem.startsWith("heavyrainshowers") -> WeatherCondition.RAIN_SHOWERS
                stem.startsWith("rain") || stem.startsWith("heavyrain") -> WeatherCondition.RAIN
                stem.startsWith("sleet") || stem.startsWith("lightsleet") || stem.startsWith("heavysleet") -> WeatherCondition.SLEET
                stem.startsWith("snowshowers") || stem.startsWith("lightsnowshowers") || stem.startsWith("heavysnowshowers") -> WeatherCondition.SNOW_SHOWERS
                stem.startsWith("snow") || stem.startsWith("lightsnow") || stem.startsWith("heavysnow") -> WeatherCondition.SNOW
                else -> WeatherCondition.UNKNOWN
            }
        }

        assertEquals(83, expected.size)
        expected.forEach { (symbolCode, condition) ->
            assertEquals(symbolCode, condition, MetNoForecastMapper.mapSymbolCode(symbolCode))
        }

        assertEquals(WeatherCondition.UNKNOWN, MetNoForecastMapper.mapSymbolCode(null))
        assertEquals(WeatherCondition.UNKNOWN, MetNoForecastMapper.mapSymbolCode(""))
        assertEquals(WeatherCondition.UNKNOWN, MetNoForecastMapper.mapSymbolCode(" "))
        assertEquals(WeatherCondition.UNKNOWN, MetNoForecastMapper.mapSymbolCode("clearsky_day "))
        assertEquals(WeatherCondition.UNKNOWN, MetNoForecastMapper.mapSymbolCode("clearsky_evening"))
        assertEquals(WeatherCondition.UNKNOWN, MetNoForecastMapper.mapSymbolCode("volcanic_ash_day"))
    }

    private fun assertMetNoProvenance(type: DataType, provenance: DataProvenance) {
        assertEquals("met-norway", provenance.providerId)
        assertEquals("MET Norway", provenance.sourceName)
        assertEquals("NLOD-2.0 OR CC-BY-4.0", provenance.licenseId)
        assertEquals(Instant.parse("2026-08-23T10:15:00Z"), provenance.issuedAt)
        assertEquals(fetchedAt, provenance.fetchedAt)
        assertEquals(type, provenance.type)
    }

    private fun parsedFixture(name: String): MetNoForecastResponse =
        MetNoForecastParser.parseForecast(fixture(name))

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/metno/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }

    private fun officialWeatherIconStems(): List<String> {
        val resource = requireNotNull(
            javaClass.classLoader?.getResource("providers/metno/weathericons_weather_stems_2026-08-23.txt"),
        ) {
            "Missing pinned weathericons stem resource"
        }
        return resource.readText().lineSequence().filter { it.isNotBlank() }.toList()
    }
}

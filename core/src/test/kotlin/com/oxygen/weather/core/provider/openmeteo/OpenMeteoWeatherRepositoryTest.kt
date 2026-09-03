package com.oxygen.weather.core.provider.openmeteo

import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.io.IOException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoWeatherRepositoryTest {
    private val fetchedAt = Instant.parse("2026-08-19T13:20:00Z")
    private val chicago = WeatherLocation(
        id = LocationId("manual-chicago"),
        displayName = "Chicago, Illinois",
        point = GeoPoint(41.875, -87.625),
        elevationMeters = 182.0,
        zoneId = ZoneId.of("America/Chicago"),
    )

    @Test
    fun emitsLoadingBeforeSuccess() {
        val repository = repository(
            transport = RepositoryStaticTransport(OpenMeteoHttpResponse(200, fixture("home_forecast_normal.json"))),
        )

        val results = repository.refresh(chicago).toList()

        assertEquals(2, results.size)
        assertSame(WeatherRepositoryResult.Loading, results[0])
        assertTrue(results[1] is WeatherRepositoryResult.Success)
    }

    @Test
    fun usesExplicitLocationCoordinatesAndTimezone() {
        val transport = RepositoryRecordingTransport(OpenMeteoHttpResponse(200, fixture("home_forecast_normal.json")))
        val repository = repository(transport = transport)

        repository.refresh(chicago).toList()

        val query = transport.requestedUrl.queryParameters()
        assertEquals("41.875", query["latitude"])
        assertEquals("-87.625", query["longitude"])
        assertEquals("America/Chicago", query["timezone"])
    }

    @Test
    fun mapsFixtureBackedSuccessToProviderNeutralWeatherBundle() {
        val repository = repository(
            transport = RepositoryStaticTransport(OpenMeteoHttpResponse(200, fixture("home_forecast_normal.json"))),
        )

        val success = repository.refresh(chicago).terminalSuccess()
        val bundle = success.weather

        assertEquals(chicago, bundle.location)
        assertEquals(fetchedAt, bundle.fetchedAt)
        assertEquals("2026-08-19T13:15:00Z", requireNotNull(bundle.current).time.toString())
        assertEquals(DataType.MODEL_ESTIMATE, bundle.current.provenance.type)
        assertEquals("open-meteo", bundle.current.provenance.providerId)
        assertEquals("Open-Meteo", bundle.current.provenance.sourceName)
        assertEquals("CC-BY-4.0", bundle.current.provenance.licenseId)
        assertEquals(fetchedAt, bundle.current.provenance.fetchedAt)
        assertEquals(DataType.FORECAST, bundle.hourly.first().provenance.type)
        assertEquals(DataType.FORECAST, bundle.daily.first().provenance.type)
    }

    @Test
    fun mapsRepresentativeMadisonLiveResponseShapeToProviderNeutralWeatherBundle() {
        val madison = WeatherLocation(
            id = LocationId("manual-madison"),
            displayName = "Madison, Wisconsin, United States",
            point = GeoPoint(43.0731, -89.4012),
            elevationMeters = 267.0,
            zoneId = ZoneId.of("America/Chicago"),
        )
        val repository = repository(
            transport = RepositoryStaticTransport(OpenMeteoHttpResponse(200, representativeMadisonForecastBody)),
        )

        val bundle = repository.refresh(madison).terminalSuccess().weather

        assertEquals(madison, bundle.location)
        assertEquals("2026-09-03T01:45:00Z", requireNotNull(bundle.current).time.toString())
        assertEquals(25.3, requireNotNull(bundle.current.temperatureC), 0.0)
        assertEquals(DataType.MODEL_ESTIMATE, bundle.current.provenance.type)
        assertEquals("open-meteo", bundle.current.provenance.providerId)
        assertEquals("Open-Meteo", bundle.current.provenance.sourceName)
        assertEquals("CC-BY-4.0", bundle.current.provenance.licenseId)
        assertEquals(fetchedAt, bundle.current.provenance.fetchedAt)
        assertEquals(2, bundle.hourly.size)
        assertEquals(DataType.FORECAST, bundle.hourly.first().provenance.type)
        assertEquals(2, bundle.daily.size)
        assertEquals(DataType.FORECAST, bundle.daily.first().provenance.type)
    }

    @Test
    fun translatesOpenMeteoClientErrorsToProviderNeutralForecastErrors() {
        val cases = listOf(
            RepositoryStaticTransport(OpenMeteoHttpResponse(429, """{"reason":"Too many requests"}""")) to
                ForecastError.RateLimited("open-meteo"),
            RepositoryStaticTransport(OpenMeteoHttpResponse(503, """{"reason":"maintenance"}""")) to
                ForecastError.ProviderUnavailable("open-meteo"),
            RepositoryStaticTransport(OpenMeteoHttpResponse(200, """{"latitude":41.875}""")) to
                ForecastError.InvalidResponse("open-meteo"),
            RepositoryStaticTransport(OpenMeteoHttpResponse(400, fixture("error_response.json"))) to
                ForecastError.ProviderRejectedRequest("open-meteo"),
            RepositoryStaticTransport(OpenMeteoHttpResponse(418, """{"reason":"teapot"}""")) to
                ForecastError.UnexpectedProviderFailure("open-meteo"),
            RepositoryThrowingTransport(IOException("offline")) to
                ForecastError.NetworkUnavailable,
        )

        cases.forEach { (transport, expected) ->
            val failure = repository(transport = transport).refresh(chicago).terminalFailure()

            assertEquals(expected, failure.error)
        }
    }

    @Test
    fun repositoryBoundaryDoesNotExposeProviderSpecificTypes() {
        val repository = repository(
            transport = RepositoryStaticTransport(OpenMeteoHttpResponse(200, fixture("home_forecast_normal.json"))),
        )

        val results = repository.refresh(chicago).toList()

        assertFalse(results.any { it::class.qualifiedName.orEmpty().contains(".openmeteo.") })
        assertTrue(results.first() is WeatherRepositoryResult.Loading)
        assertTrue(results.last() is WeatherRepositoryResult.Success)
    }

    private fun repository(transport: OpenMeteoHttpTransport): OpenMeteoWeatherRepository =
        OpenMeteoWeatherRepository(
            client = OpenMeteoForecastClient(
                baseUrl = "https://weather.test/v1/forecast",
                transport = transport,
            ),
            clock = { fetchedAt },
        )

    private fun Sequence<WeatherRepositoryResult>.terminalSuccess(): WeatherRepositoryResult.Success =
        toList().last() as WeatherRepositoryResult.Success

    private fun Sequence<WeatherRepositoryResult>.terminalFailure(): WeatherRepositoryResult.Failure =
        toList().last() as WeatherRepositoryResult.Failure

    private fun URL.queryParameters(): Map<String, String> =
        query.split("&")
            .filter { it.isNotBlank() }
            .associate { pair ->
                val parts = pair.split("=", limit = 2)
                decode(parts[0]) to decode(parts.getOrElse(1) { "" })
            }

    private fun decode(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8.name())

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/openmeteo/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}

private class RepositoryRecordingTransport(
    private val response: OpenMeteoHttpResponse,
) : OpenMeteoHttpTransport {
    lateinit var requestedUrl: URL

    override fun get(url: URL): OpenMeteoHttpResponse {
        requestedUrl = url
        return response
    }
}

private class RepositoryStaticTransport(
    private val response: OpenMeteoHttpResponse,
) : OpenMeteoHttpTransport {
    override fun get(url: URL): OpenMeteoHttpResponse = response
}

private class RepositoryThrowingTransport(
    private val error: IOException,
) : OpenMeteoHttpTransport {
    override fun get(url: URL): OpenMeteoHttpResponse {
        throw error
    }
}

private val representativeMadisonForecastBody = """
    {
      "latitude": 43.060394,
      "longitude": -89.39947,
      "generationtime_ms": 1.6463994979858398,
      "utc_offset_seconds": -18000,
      "timezone": "America/Chicago",
      "timezone_abbreviation": "GMT-5",
      "elevation": 272.0,
      "current_units": {
        "time": "iso8601",
        "interval": "seconds",
        "temperature_2m": "°C",
        "relative_humidity_2m": "%",
        "apparent_temperature": "°C",
        "is_day": "",
        "precipitation": "mm",
        "rain": "mm",
        "showers": "mm",
        "snowfall": "cm",
        "weather_code": "wmo code",
        "cloud_cover": "%",
        "pressure_msl": "hPa",
        "surface_pressure": "hPa",
        "wind_speed_10m": "km/h",
        "wind_direction_10m": "°",
        "wind_gusts_10m": "km/h"
      },
      "current": {
        "time": "2026-09-02T20:45",
        "interval": 900,
        "temperature_2m": 25.3,
        "relative_humidity_2m": 83,
        "apparent_temperature": 29.4,
        "is_day": 0,
        "precipitation": 0.00,
        "rain": 0.00,
        "showers": 0.00,
        "snowfall": 0.00,
        "weather_code": 0,
        "cloud_cover": 0,
        "pressure_msl": 1010.9,
        "surface_pressure": 980.0,
        "wind_speed_10m": 6.5,
        "wind_direction_10m": 214,
        "wind_gusts_10m": 6.5
      },
      "hourly_units": {
        "time": "iso8601",
        "temperature_2m": "°C",
        "relative_humidity_2m": "%",
        "dew_point_2m": "°C",
        "apparent_temperature": "°C",
        "precipitation_probability": "%",
        "precipitation": "mm",
        "rain": "mm",
        "showers": "mm",
        "snowfall": "cm",
        "weather_code": "wmo code",
        "pressure_msl": "hPa",
        "surface_pressure": "hPa",
        "cloud_cover": "%",
        "visibility": "m",
        "uv_index": "",
        "is_day": "",
        "wind_speed_10m": "km/h",
        "wind_direction_10m": "°",
        "wind_gusts_10m": "km/h"
      },
      "hourly": {
        "time": ["2026-09-02T20:00", "2026-09-02T21:00"],
        "temperature_2m": [26.2, 25.0],
        "relative_humidity_2m": [79, 83],
        "dew_point_2m": [22.3, 21.8],
        "apparent_temperature": [29.8, 28.7],
        "precipitation_probability": [5, 3],
        "precipitation": [0.00, 0.00],
        "rain": [0.00, 0.00],
        "showers": [0.00, 0.00],
        "snowfall": [0.00, 0.00],
        "weather_code": [0, 0],
        "pressure_msl": [1010.8, 1011.1],
        "surface_pressure": [979.9, 980.1],
        "cloud_cover": [0, 0],
        "visibility": [24140.0, 24140.0],
        "uv_index": [0.0, 0.0],
        "is_day": [0, 0],
        "wind_speed_10m": [6.2, 5.8],
        "wind_direction_10m": [200, 209],
        "wind_gusts_10m": [6.2, 5.8]
      },
      "daily_units": {
        "time": "iso8601",
        "weather_code": "wmo code",
        "temperature_2m_max": "°C",
        "temperature_2m_min": "°C",
        "apparent_temperature_max": "°C",
        "apparent_temperature_min": "°C",
        "uv_index_max": "",
        "sunrise": "iso8601",
        "sunset": "iso8601",
        "daylight_duration": "s",
        "rain_sum": "mm",
        "showers_sum": "mm",
        "snowfall_sum": "cm",
        "precipitation_sum": "mm",
        "precipitation_hours": "h",
        "precipitation_probability_max": "%",
        "wind_speed_10m_max": "km/h",
        "wind_gusts_10m_max": "km/h",
        "wind_direction_10m_dominant": "°"
      },
      "daily": {
        "time": ["2026-09-02", "2026-09-03"],
        "weather_code": [0, 61],
        "temperature_2m_max": [30.5, 23.4],
        "temperature_2m_min": [21.0, 16.3],
        "apparent_temperature_max": [32.1, 22.0],
        "apparent_temperature_min": [21.8, 15.4],
        "uv_index_max": [5.45, 4.60],
        "sunrise": ["2026-09-02T06:23", "2026-09-03T06:24"],
        "sunset": ["2026-09-02T19:31", "2026-09-03T19:29"],
        "daylight_duration": [47280.0, 47100.0],
        "rain_sum": [0.00, 2.20],
        "showers_sum": [0.00, 0.00],
        "snowfall_sum": [0.00, 0.00],
        "precipitation_sum": [0.00, 2.20],
        "precipitation_hours": [0.0, 2.0],
        "precipitation_probability_max": [10, 72],
        "wind_speed_10m_max": [12.0, 18.2],
        "wind_gusts_10m_max": [20.9, 31.7],
        "wind_direction_10m_dominant": [194, 177]
      }
    }
""".trimIndent()

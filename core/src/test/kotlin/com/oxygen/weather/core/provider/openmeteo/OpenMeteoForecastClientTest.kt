package com.oxygen.weather.core.provider.openmeteo

import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoForecastClientTest {
    private val request = OpenMeteoForecastRequest(
        latitude = 41.875,
        longitude = -87.625,
        timezone = "America/Chicago",
    )

    @Test
    fun buildsContractedHomeForecastQueryFromConfigurableBaseUrl() {
        val transport = RecordingTransport(OpenMeteoHttpResponse(200, fixture("home_forecast_normal.json")))
        val client = OpenMeteoForecastClient(
            baseUrl = "https://weather.test/v1/forecast",
            transport = transport,
        )

        val result = client.fetchForecast(request)

        assertTrue(result is OpenMeteoForecastClientResult.Success)
        assertEquals("https", transport.requestedUrl.protocol)
        assertEquals("weather.test", transport.requestedUrl.host)
        assertEquals("/v1/forecast", transport.requestedUrl.path)

        val query = transport.requestedUrl.queryParameters()
        assertEquals("41.875", query["latitude"])
        assertEquals("-87.625", query["longitude"])
        assertEquals("America/Chicago", query["timezone"])
        assertEquals("iso8601", query["timeformat"])
        assertEquals("celsius", query["temperature_unit"])
        assertEquals("kmh", query["wind_speed_unit"])
        assertEquals("mm", query["precipitation_unit"])
        assertEquals("10", query["forecast_days"])
        assertEquals("48", query["forecast_hours"])
        assertEquals(OpenMeteoForecastRequest.currentFields.joinToString(","), query["current"])
        assertEquals(OpenMeteoForecastRequest.hourlyFields.joinToString(","), query["hourly"])
        assertEquals(OpenMeteoForecastRequest.dailyFields.joinToString(","), query["daily"])
    }

    @Test
    fun parsesSuccessfulResponseThroughProductionParser() {
        val client = OpenMeteoForecastClient(
            baseUrl = "https://weather.test/v1/forecast",
            transport = StaticTransport(OpenMeteoHttpResponse(200, fixture("home_forecast_normal.json"))),
        )

        val result = client.fetchForecast(request)

        val response = (result as OpenMeteoForecastClientResult.Success).response
        assertEquals(41.875, response.latitude, 0.0)
        assertEquals("America/Chicago", response.timezone)
        assertEquals("2026-08-19T08:15", response.current.time)
    }

    @Test
    fun classifiesIoFailureAsNetworkUnavailable() {
        val client = OpenMeteoForecastClient(
            baseUrl = "https://weather.test/v1/forecast",
            transport = ThrowingTransport(IOException("offline")),
        )

        val result = client.fetchForecast(request)

        assertEquals(OpenMeteoForecastClientError.NetworkUnavailable, result.error())
    }

    @Test
    fun classifiesRateLimitStatus() {
        val client = OpenMeteoForecastClient(
            baseUrl = "https://weather.test/v1/forecast",
            transport = StaticTransport(OpenMeteoHttpResponse(429, """{"reason":"Too many requests"}""")),
        )

        val result = client.fetchForecast(request)

        assertEquals(OpenMeteoForecastClientError.RateLimited, result.error())
    }

    @Test
    fun classifiesServerStatusAsProviderUnavailable() {
        val client = OpenMeteoForecastClient(
            baseUrl = "https://weather.test/v1/forecast",
            transport = StaticTransport(OpenMeteoHttpResponse(503, """{"reason":"maintenance"}""")),
        )

        val result = client.fetchForecast(request)

        assertEquals(OpenMeteoForecastClientError.ProviderUnavailable(503), result.error())
    }

    @Test
    fun classifiesProviderErrorBodyWithoutLeakingParseException() {
        val client = OpenMeteoForecastClient(
            baseUrl = "https://weather.test/v1/forecast",
            transport = StaticTransport(OpenMeteoHttpResponse(400, fixture("error_response.json"))),
        )

        val result = client.fetchForecast(request)

        assertEquals(
            OpenMeteoForecastClientError.ProviderRejectedRequest(
                statusCode = 400,
                reason = "Parameter 'latitude' and 'longitude' must have the same number of elements",
            ),
            result.error(),
        )
    }

    @Test
    fun classifiesMalformedSuccessBodyAsInvalidResponse() {
        val client = OpenMeteoForecastClient(
            baseUrl = "https://weather.test/v1/forecast",
            transport = StaticTransport(OpenMeteoHttpResponse(200, """{"latitude":41.875}""")),
        )

        val result = client.fetchForecast(request)

        assertEquals(OpenMeteoForecastClientError.InvalidResponse, result.error())
    }

    @Test
    fun classifiesUnexpectedHttpStatus() {
        val client = OpenMeteoForecastClient(
            baseUrl = "https://weather.test/v1/forecast",
            transport = StaticTransport(OpenMeteoHttpResponse(418, """{"reason":"teapot"}""")),
        )

        val result = client.fetchForecast(request)

        assertEquals(OpenMeteoForecastClientError.UnexpectedHttpFailure(418), result.error())
    }

    private fun OpenMeteoForecastClientResult.error(): OpenMeteoForecastClientError =
        (this as OpenMeteoForecastClientResult.Failure).error

    private fun java.net.URL.queryParameters(): Map<String, String> =
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

private class RecordingTransport(
    private val response: OpenMeteoHttpResponse,
) : OpenMeteoHttpTransport {
    lateinit var requestedUrl: java.net.URL

    override fun get(url: java.net.URL): OpenMeteoHttpResponse {
        requestedUrl = url
        return response
    }
}

private class StaticTransport(
    private val response: OpenMeteoHttpResponse,
) : OpenMeteoHttpTransport {
    override fun get(url: java.net.URL): OpenMeteoHttpResponse = response
}

private class ThrowingTransport(
    private val error: IOException,
) : OpenMeteoHttpTransport {
    override fun get(url: java.net.URL): OpenMeteoHttpResponse {
        throw error
    }
}

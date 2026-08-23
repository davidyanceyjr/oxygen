package com.oxygen.weather.core.provider.metno

import java.io.IOException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MetNoForecastClientTest {
    @Test
    fun buildsCompactRequestFromDefaultBaseUrlWithRequiredHeaders() {
        val transport = RecordingMetNoTransport(MetNoHttpResponse(200, body = fixture("home_forecast_normal.json")))
        val client = MetNoForecastClient(transport = transport)

        val result = client.fetchForecast(MetNoForecastRequest(latitude = 41.875, longitude = -87.625))

        assertTrue(result is MetNoForecastClientResult.Success)
        assertEquals("https", transport.request.url.protocol)
        assertEquals("api.met.no", transport.request.url.host)
        assertEquals("/weatherapi/locationforecast/2.0/compact", transport.request.url.path)
        assertEquals(
            mapOf("lat" to "41.875", "lon" to "-87.625"),
            transport.request.url.queryParameters(),
        )
        assertEquals(MetNoForecastClient.DEFAULT_USER_AGENT, transport.request.headers["User-Agent"])
        assertEquals("application/json", transport.request.headers["Accept"])
        assertFalse(transport.request.headers.containsKey("If-Modified-Since"))
    }

    @Test
    fun rejectsConfiguredBaseUrlWithQueryOrFragmentBeforeTransport() {
        val queryTransport = CountingMetNoTransport()
        val fragmentTransport = CountingMetNoTransport()

        val queryResult = MetNoForecastClient(
            baseUrl = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=41",
            transport = queryTransport,
        ).fetchForecast(MetNoForecastRequest(latitude = 41.0, longitude = -87.0))
        val fragmentResult = MetNoForecastClient(
            baseUrl = "https://api.met.no/weatherapi/locationforecast/2.0/compact#forecast",
            transport = fragmentTransport,
        ).fetchForecast(MetNoForecastRequest(latitude = 41.0, longitude = -87.0))

        assertEquals(MetNoForecastClientError.InvalidRequest, queryResult.error())
        assertEquals(MetNoForecastClientError.InvalidRequest, fragmentResult.error())
        assertEquals(0, queryTransport.calls)
        assertEquals(0, fragmentTransport.calls)
    }

    @Test
    fun formatsCoordinatesDeterministicallyWithNoExtraCompactParameters() {
        val cases = listOf(
            MetNoForecastRequest(latitude = 41.123456, longitude = -87.00004) to mapOf("lat" to "41.1235", "lon" to "-87"),
            MetNoForecastRequest(latitude = 90.0, longitude = -180.0) to mapOf("lat" to "90", "lon" to "-180"),
            MetNoForecastRequest(latitude = -90.0, longitude = 180.0) to mapOf("lat" to "-90", "lon" to "180"),
        )

        cases.forEach { (request, expectedQuery) ->
            val transport = RecordingMetNoTransport(MetNoHttpResponse(304))
            val client = MetNoForecastClient(baseUrl = "https://weather.test/compact", transport = transport)

            client.fetchForecast(request)

            assertEquals(expectedQuery, transport.request.url.queryParameters())
            assertFalse(transport.request.url.queryParameters().containsKey("current"))
            assertFalse(transport.request.url.queryParameters().containsKey("hourly"))
            assertFalse(transport.request.url.queryParameters().containsKey("daily"))
            assertFalse(transport.request.url.queryParameters().containsKey("fields"))
            assertFalse(transport.request.url.queryParameters().containsKey("format"))
        }
    }

    @Test
    fun rejectsInvalidCoordinatesBeforeTransport() {
        val invalidRequests = listOf(
            MetNoForecastRequest(latitude = Double.NaN, longitude = -87.0),
            MetNoForecastRequest(latitude = Double.POSITIVE_INFINITY, longitude = -87.0),
            MetNoForecastRequest(latitude = 90.0001, longitude = -87.0),
            MetNoForecastRequest(latitude = 41.0, longitude = Double.NEGATIVE_INFINITY),
            MetNoForecastRequest(latitude = 41.0, longitude = -180.0001),
        )

        invalidRequests.forEach { request ->
            val transport = CountingMetNoTransport()
            val result = MetNoForecastClient(transport = transport).fetchForecast(request)

            assertEquals(MetNoForecastClientError.InvalidRequest, result.error())
            assertEquals(0, transport.calls)
        }
    }

    @Test
    fun sendsOptionalWholeMeterAltitudeAndConditionalRevalidationHeader() {
        val transport = RecordingMetNoTransport(MetNoHttpResponse(304, headers = mapOf("Expires" to "Sun, 23 Aug 2026 15:00:00 GMT")))
        val client = MetNoForecastClient(baseUrl = "https://weather.test/compact", transport = transport)

        val result = client.fetchForecast(
            MetNoForecastRequest(
                latitude = 41.875,
                longitude = -87.625,
                altitudeMeters = 181.6,
                cachedLastModified = "Sun, 23 Aug 2026 14:00:00 GMT",
            ),
        )

        assertEquals("182", transport.request.url.queryParameters()["altitude"])
        assertEquals("Sun, 23 Aug 2026 14:00:00 GMT", transport.request.headers["If-Modified-Since"])
        assertEquals(
            MetNoForecastCacheHeaders(expires = "Sun, 23 Aug 2026 15:00:00 GMT"),
            (result as MetNoForecastClientResult.NotModified).cacheHeaders,
        )
    }

    @Test
    fun rejectsInvalidUserAgentBeforeTransport() {
        val invalidAgents = listOf(
            "",
            "   ",
            "Java/26",
            "Dalvik/2.1.0",
            "Android",
            "okhttp/4.12.0",
            "Mozilla/5.0",
            "Chrome/120",
            "Safari/605.1.15",
            "curl/8.0.0",
            "OxygenWeather/0.1.0",
            "SomeApp/1.0 https://example.test",
        )

        invalidAgents.forEach { userAgent ->
            val transport = CountingMetNoTransport()
            val result = MetNoForecastClient(
                userAgent = userAgent,
                transport = transport,
            ).fetchForecast(MetNoForecastRequest(latitude = 41.0, longitude = -87.0))

            assertEquals(MetNoForecastClientError.IllegalIdentification(null, null), result.error())
            assertEquals(0, transport.calls)
        }
    }

    @Test
    fun parsesSuccessfulResponseAndCapturesCacheHeadersCaseInsensitively() {
        val client = MetNoForecastClient(
            baseUrl = "https://weather.test/compact",
            transport = StaticMetNoTransport(
                MetNoHttpResponse(
                    statusCode = 200,
                    headers = mapOf(
                        "expires" to "Sun, 23 Aug 2026 15:00:00 GMT",
                        "LAST-MODIFIED" to "Sun, 23 Aug 2026 14:00:00 GMT",
                        "eTag" to """"abc123"""",
                    ),
                    body = fixture("home_forecast_normal.json"),
                ),
            ),
        )

        val result = client.fetchForecast(MetNoForecastRequest(latitude = 41.875, longitude = -87.625))

        val success = result as MetNoForecastClientResult.Success
        assertEquals(41.875, success.response.geometry.latitude, 0.0)
        assertEquals("2026-08-23T10:15:00Z", success.response.meta.updatedAt)
        assertEquals(
            MetNoForecastCacheHeaders(
                expires = "Sun, 23 Aug 2026 15:00:00 GMT",
                lastModified = "Sun, 23 Aug 2026 14:00:00 GMT",
                etag = """"abc123"""",
            ),
            success.cacheHeaders,
        )
    }

    @Test
    fun classifiesIoFailureAsNetworkUnavailable() {
        val client = MetNoForecastClient(
            baseUrl = "https://weather.test/compact",
            transport = ThrowingMetNoTransport(IOException("offline")),
        )

        val result = client.fetchForecast(MetNoForecastRequest(latitude = 41.875, longitude = -87.625))

        assertEquals(MetNoForecastClientError.NetworkUnavailable, result.error())
    }

    @Test
    fun classifiesStatusAndKnownProviderErrorClasses() {
        val cases = listOf(
            MetNoHttpResponse(429) to MetNoForecastClientError.RateLimited(429, null),
            MetNoHttpResponse(400, headers = mapOf("X-ErrorClass" to "Ratelimitation")) to
                MetNoForecastClientError.RateLimited(400, "Ratelimitation"),
            MetNoHttpResponse(503) to MetNoForecastClientError.ProviderUnavailable(503, null),
            MetNoHttpResponse(400, headers = mapOf("X-ErrorClass" to "BackendError")) to
                MetNoForecastClientError.ProviderUnavailable(400, "BackendError"),
            MetNoHttpResponse(403) to MetNoForecastClientError.IllegalIdentification(403, null),
            MetNoHttpResponse(400, headers = mapOf("x-errorclass" to "IllegalUserAgent")) to
                MetNoForecastClientError.IllegalIdentification(400, "IllegalUserAgent"),
            MetNoHttpResponse(400, headers = mapOf("X-ErrorClass" to "ParameterError")) to
                MetNoForecastClientError.InvalidResponse(400, "ParameterError"),
            MetNoHttpResponse(404, headers = mapOf("X-ErrorClass" to "OutsideArea")) to
                MetNoForecastClientError.UnsupportedForecastData(404, "OutsideArea"),
            MetNoHttpResponse(418, headers = mapOf("X-ErrorClass" to "MysteryClass")) to
                MetNoForecastClientError.UnexpectedHttpFailure(418, "MysteryClass"),
        )

        cases.forEach { (response, expectedError) ->
            val client = MetNoForecastClient(
                baseUrl = "https://weather.test/compact",
                transport = StaticMetNoTransport(response),
            )

            val result = client.fetchForecast(MetNoForecastRequest(latitude = 41.875, longitude = -87.625))

            assertEquals(expectedError, result.error())
        }
    }

    @Test
    fun classifiesMalformedSuccessBodyAsInvalidResponse() {
        val client = MetNoForecastClient(
            baseUrl = "https://weather.test/compact",
            transport = StaticMetNoTransport(MetNoHttpResponse(200, body = """{"type":"Feature"}""")),
        )

        val result = client.fetchForecast(MetNoForecastRequest(latitude = 41.875, longitude = -87.625))

        assertEquals(MetNoForecastClientError.InvalidResponse(200, null), result.error())
    }

    private fun MetNoForecastClientResult.error(): MetNoForecastClientError =
        (this as MetNoForecastClientResult.Failure).error

    private fun URL.queryParameters(): Map<String, String> =
        requireNotNull(query).split("&")
            .filter { it.isNotBlank() }
            .associate { pair ->
                val parts = pair.split("=", limit = 2)
                decode(parts[0]) to decode(parts.getOrElse(1) { "" })
            }

    private fun decode(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8.name())

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/metno/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}

private class RecordingMetNoTransport(
    private val response: MetNoHttpResponse,
) : MetNoHttpTransport {
    lateinit var request: MetNoHttpRequest

    override fun get(request: MetNoHttpRequest): MetNoHttpResponse {
        this.request = request
        return response
    }
}

private class StaticMetNoTransport(
    private val response: MetNoHttpResponse,
) : MetNoHttpTransport {
    override fun get(request: MetNoHttpRequest): MetNoHttpResponse = response
}

private class ThrowingMetNoTransport(
    private val error: IOException,
) : MetNoHttpTransport {
    override fun get(request: MetNoHttpRequest): MetNoHttpResponse {
        throw error
    }
}

private class CountingMetNoTransport : MetNoHttpTransport {
    var calls = 0

    override fun get(request: MetNoHttpRequest): MetNoHttpResponse {
        calls += 1
        return MetNoHttpResponse(304)
    }
}

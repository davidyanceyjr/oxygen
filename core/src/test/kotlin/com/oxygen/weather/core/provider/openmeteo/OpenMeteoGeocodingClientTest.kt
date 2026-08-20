package com.oxygen.weather.core.provider.openmeteo

import java.io.IOException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoGeocodingClientTest {
    @Test
    fun buildsContractedSearchQueryFromConfigurableBaseUrl() {
        val transport = GeocodingRecordingTransport(
            OpenMeteoHttpResponse(200, fixture("search_normal.json")),
        )
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = transport,
        )

        val result = client.search(
            OpenMeteoGeocodingRequest(
                query = "  São Paulo  ",
                count = 25,
                language = "pt",
                countryCode = "br",
            ),
        )

        assertTrue(result is OpenMeteoGeocodingClientResult.Success)
        assertEquals("https", transport.requestedUrl.protocol)
        assertEquals("geo.test", transport.requestedUrl.host)
        assertEquals("/v1/search", transport.requestedUrl.path)

        val query = transport.requestedUrl.queryParameters()
        assertEquals("São Paulo", query["name"])
        assertEquals("20", query["count"])
        assertEquals("json", query["format"])
        assertEquals("pt", query["language"])
        assertEquals("BR", query["countryCode"])
    }

    @Test
    fun appliesDefaultCountAndOmitsBlankOptionalFilters() {
        val transport = GeocodingRecordingTransport(
            OpenMeteoHttpResponse(200, fixture("search_empty.json")),
        )
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = transport,
        )

        val result = client.search(
            OpenMeteoGeocodingRequest(
                query = "Chicago",
                language = " ",
                countryCode = null,
            ),
        )

        assertTrue(result is OpenMeteoGeocodingClientResult.Success)
        val query = transport.requestedUrl.queryParameters()
        assertEquals("10", query["count"])
        assertFalse(query.containsKey("language"))
        assertFalse(query.containsKey("countryCode"))
    }

    @Test
    fun clampsLowCountToOne() {
        val transport = GeocodingRecordingTransport(
            OpenMeteoHttpResponse(200, fixture("search_empty.json")),
        )
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = transport,
        )

        client.search(OpenMeteoGeocodingRequest(query = "Chicago", count = -4))

        assertEquals("1", transport.requestedUrl.queryParameters()["count"])
    }

    @Test
    fun rejectsBlankQueryWithoutCallingTransport() {
        val transport = GeocodingFailingTransport
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = transport,
        )

        val result = client.search(OpenMeteoGeocodingRequest(query = "   "))

        assertEquals(OpenMeteoGeocodingClientError.InvalidRequest, result.error())
    }

    @Test
    fun rejectsInvalidCountryCodeWithoutCallingTransport() {
        val transport = GeocodingFailingTransport
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = transport,
        )

        val result = client.search(
            OpenMeteoGeocodingRequest(
                query = "Chicago",
                countryCode = "USA",
            ),
        )

        assertEquals(OpenMeteoGeocodingClientError.InvalidRequest, result.error())
    }

    @Test
    fun parsesSuccessfulResponseThroughProductionParser() {
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = GeocodingStaticTransport(OpenMeteoHttpResponse(200, fixture("search_normal.json"))),
        )

        val result = client.search(OpenMeteoGeocodingRequest(query = "Chicago"))

        val response = (result as OpenMeteoGeocodingClientResult.Success).response
        assertEquals("Chicago", response.results.single().name)
        assertEquals("America/Chicago", response.results.single().timezone)
    }

    @Test
    fun parsesEmptyResultsAsSuccessfulResponse() {
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = GeocodingStaticTransport(OpenMeteoHttpResponse(200, fixture("search_empty.json"))),
        )

        val result = client.search(OpenMeteoGeocodingRequest(query = "No Such Place"))

        val response = (result as OpenMeteoGeocodingClientResult.Success).response
        assertEquals(emptyList<OpenMeteoGeocodingResult>(), response.results)
    }

    @Test
    fun classifiesIoFailureAsNetworkUnavailable() {
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = GeocodingThrowingTransport(IOException("offline")),
        )

        val result = client.search(OpenMeteoGeocodingRequest(query = "Chicago"))

        assertEquals(OpenMeteoGeocodingClientError.NetworkUnavailable, result.error())
    }

    @Test
    fun classifiesRateLimitStatus() {
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = GeocodingStaticTransport(OpenMeteoHttpResponse(429, """{"reason":"Too many requests"}""")),
        )

        val result = client.search(OpenMeteoGeocodingRequest(query = "Chicago"))

        assertEquals(OpenMeteoGeocodingClientError.RateLimited, result.error())
    }

    @Test
    fun classifiesServerStatusAsProviderUnavailable() {
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = GeocodingStaticTransport(OpenMeteoHttpResponse(503, """{"reason":"maintenance"}""")),
        )

        val result = client.search(OpenMeteoGeocodingRequest(query = "Chicago"))

        assertEquals(OpenMeteoGeocodingClientError.ProviderUnavailable(503), result.error())
    }

    @Test
    fun classifiesProviderErrorBodyWithoutLeakingParseException() {
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = GeocodingStaticTransport(OpenMeteoHttpResponse(400, fixture("search_provider_error.json"))),
        )

        val result = client.search(OpenMeteoGeocodingRequest(query = "Chicago"))

        assertEquals(
            OpenMeteoGeocodingClientError.ProviderRejectedRequest(
                statusCode = 400,
                reason = "Parameter 'name' is invalid",
            ),
            result.error(),
        )
    }

    @Test
    fun classifiesMalformedSuccessBodyAsInvalidResponse() {
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = GeocodingStaticTransport(OpenMeteoHttpResponse(200, """{"results":{}}""")),
        )

        val result = client.search(OpenMeteoGeocodingRequest(query = "Chicago"))

        assertEquals(OpenMeteoGeocodingClientError.InvalidResponse, result.error())
    }

    @Test
    fun classifiesUnexpectedHttpStatus() {
        val client = OpenMeteoGeocodingClient(
            baseUrl = "https://geo.test/v1/search",
            transport = GeocodingStaticTransport(OpenMeteoHttpResponse(418, """{"reason":"teapot"}""")),
        )

        val result = client.search(OpenMeteoGeocodingRequest(query = "Chicago"))

        assertEquals(OpenMeteoGeocodingClientError.UnexpectedHttpFailure(418), result.error())
    }

    private fun OpenMeteoGeocodingClientResult.error(): OpenMeteoGeocodingClientError =
        (this as OpenMeteoGeocodingClientResult.Failure).error

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
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/openmeteo/geocoding/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}

private class GeocodingRecordingTransport(
    private val response: OpenMeteoHttpResponse,
) : OpenMeteoHttpTransport {
    lateinit var requestedUrl: URL

    override fun get(url: URL): OpenMeteoHttpResponse {
        requestedUrl = url
        return response
    }
}

private class GeocodingStaticTransport(
    private val response: OpenMeteoHttpResponse,
) : OpenMeteoHttpTransport {
    override fun get(url: URL): OpenMeteoHttpResponse = response
}

private class GeocodingThrowingTransport(
    private val error: IOException,
) : OpenMeteoHttpTransport {
    override fun get(url: URL): OpenMeteoHttpResponse {
        throw error
    }
}

private object GeocodingFailingTransport : OpenMeteoHttpTransport {
    override fun get(url: URL): OpenMeteoHttpResponse {
        error("Transport should not be called")
    }
}

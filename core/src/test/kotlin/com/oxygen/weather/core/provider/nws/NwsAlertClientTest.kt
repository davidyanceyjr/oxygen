package com.oxygen.weather.core.provider.nws

import com.oxygen.weather.core.model.GeoPoint
import java.io.IOException
import java.net.URI
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NwsAlertClientTest {
    private val validPoint = GeoPoint(41.875, -87.625)

    @Test
    fun buildsContractedRequestFromConfigurableBaseUriAndHeaders() {
        val transport = RecordingTransport(
            NwsAlertHttpResponse(
                statusCode = 200,
                body = fixture("alerts_active_none.json"),
                contentType = "application/geo+json; charset=utf-8",
            ),
        )
        val client = NwsAlertClient(
            configuration = NwsAlertConfiguration(
                baseUri = URI("https://alerts.test/alerts/active"),
                userAgent = "TestAgent/1.0",
                accept = "application/geo+json",
            ),
            transport = transport,
        )

        val result = client.fetch(validPoint)

        assertTrue(result is NwsAlertClientResult.Success)
        assertEquals("https", transport.requestedUrl.protocol)
        assertEquals("alerts.test", transport.requestedUrl.host)
        assertEquals("/alerts/", transport.requestedUrl.path)
        assertEquals("point=41.875,-87.625", transport.requestedUrl.query)
        assertEquals("TestAgent/1.0", transport.requestedHeaders["User-Agent"])
        assertEquals("application/geo+json", transport.requestedHeaders["Accept"])
        assertEquals(1, transport.calls)
    }

    @Test
    fun formatsQueryCoordinatesIndependentlyOfDefaultLocale() {
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.GERMANY)
        try {
            val transport = RecordingTransport(
                NwsAlertHttpResponse(
                    statusCode = 200,
                    body = fixture("alerts_active_none.json"),
                    contentType = "application/geo+json",
                ),
            )
            val client = NwsAlertClient(
                configuration = NwsAlertConfiguration(baseUri = URI("https://alerts.test/alerts/active")),
                transport = transport,
            )

            val result = client.fetch(validPoint)

            assertTrue(result is NwsAlertClientResult.Success)
            assertEquals("point=41.875,-87.625", transport.requestedUrl.query)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun rejectsInvalidPointWithoutCallingTransport() {
        val transport = RecordingTransport(
            NwsAlertHttpResponse(
                statusCode = 200,
                body = fixture("alerts_active_none.json"),
                contentType = "application/geo+json",
            ),
        )
        val client = NwsAlertClient(transport = transport)

        listOf(
            GeoPoint(Double.NaN, 0.0),
            GeoPoint(0.0, Double.POSITIVE_INFINITY),
            GeoPoint(91.0, 0.0),
            GeoPoint(0.0, -181.0),
        ).forEach { point ->
            val result = client.fetch(point)

            assertEquals(NwsAlertClientError.InvalidPoint, result.error())
        }

        assertEquals(0, transport.calls)
    }

    @Test
    fun rejectsInvalidBaseUriConfigurationWithoutCallingTransport() {
        val invalidBaseUris = listOf(
            URI("http://alerts.test/alerts/active"),
            URI("https://alerts.test/alerts/active?foo=bar"),
            URI("https://alerts.test/alerts/active#fragment"),
            URI("/alerts/active"),
            URI("https:///alerts/active"),
        )

        invalidBaseUris.forEach { baseUri ->
            val transport = RecordingTransport(
                NwsAlertHttpResponse(
                    statusCode = 200,
                    body = fixture("alerts_active_none.json"),
                    contentType = "application/geo+json",
                ),
            )
            val client = NwsAlertClient(
                configuration = NwsAlertConfiguration(baseUri = baseUri),
                transport = transport,
            )

            val result = client.fetch(validPoint)

            assertEquals(NwsAlertClientError.InvalidRequest, result.error())
            assertEquals(0, transport.calls)
        }
    }

    @Test
    fun parsesEmptyAndNonEmptySuccessCollectionsThroughProductionParser() {
        val emptyClient = NwsAlertClient(
            transport = StaticTransport(
                NwsAlertHttpResponse(
                    statusCode = 200,
                    body = fixture("alerts_active_none.json"),
                    contentType = "application/geo+json",
                ),
            ),
        )
        val emptyResult = emptyClient.fetch(validPoint)
        val emptyCollection = (emptyResult as NwsAlertClientResult.Success).collection
        assertEquals("FeatureCollection", emptyCollection.type)
        assertTrue(emptyCollection.features.isEmpty())

        val oneClient = NwsAlertClient(
            transport = StaticTransport(
                NwsAlertHttpResponse(
                    statusCode = 200,
                    body = fixture("alerts_active_one.json"),
                    contentType = "application/geo+json; charset=utf-8",
                ),
            ),
        )
        val oneResult = oneClient.fetch(validPoint)
        val oneCollection = (oneResult as NwsAlertClientResult.Success).collection
        assertEquals(1, oneCollection.features.size)
        assertEquals("Flash Flood Warning", oneCollection.features.single().properties.event)
    }

    @Test
    fun classifiesUnsupportedRegionProblemBody() {
        val client = NwsAlertClient(
            transport = StaticTransport(
                NwsAlertHttpResponse(
                    statusCode = 400,
                    body = fixture("alerts_problem_unsupported_region.json"),
                    contentType = "APPLICATION/PROBLEM+JSON; charset=utf-8",
                ),
            ),
        )

        assertEquals(NwsAlertClientError.UnsupportedRegion, client.fetch(validPoint).error())
    }

    @Test
    fun classifiesOrdinaryBadRequestAsInvalidRequest() {
        val client = NwsAlertClient(
            transport = StaticTransport(
                NwsAlertHttpResponse(
                    statusCode = 400,
                    body = fixture("alerts_problem_ordinary.json"),
                    contentType = "application/problem+json",
                ),
            ),
        )

        assertEquals(NwsAlertClientError.InvalidRequest, client.fetch(validPoint).error())
    }

    @Test
    fun classifiesMalformedDeclaredProblemEnvelopeAsInvalidResponse() {
        val client = NwsAlertClient(
            transport = StaticTransport(
                NwsAlertHttpResponse(
                    statusCode = 400,
                    body = fixture("alerts_problem_malformed.json"),
                    contentType = "application/problem+json; charset=utf-8",
                ),
            ),
        )

        assertEquals(NwsAlertClientError.InvalidResponse, client.fetch(validPoint).error())
    }

    @Test
    fun classifiesIdentityRejectionAsSuch() {
        val client = NwsAlertClient(
            transport = StaticTransport(
                NwsAlertHttpResponse(
                    statusCode = 403,
                    body = fixture("alerts_problem_ordinary.json"),
                    contentType = "application/problem+json",
                ),
            ),
        )

        assertEquals(NwsAlertClientError.IdentificationRejected, client.fetch(validPoint).error())
    }

    @Test
    fun classifiesTimeoutOrOfflineAsNetwork() {
        val client = NwsAlertClient(
            transport = ThrowingTransport(IOException("offline")),
        )

        assertEquals(NwsAlertClientError.Network, client.fetch(validPoint).error())
    }

    @Test
    fun classifiesRetryAfterOnRateLimit() {
        val client = NwsAlertClient(
            transport = StaticTransport(
                NwsAlertHttpResponse(
                    statusCode = 429,
                    body = """{"title":"Too Many Requests"}""",
                    contentType = "application/problem+json",
                    headers = mapOf("rEtRy-AfTeR" to "120"),
                ),
            ),
        )

        assertEquals(NwsAlertClientError.RateLimited("120"), client.fetch(validPoint).error())
    }

    @Test
    fun classifiesProviderUnavailableStatuses() {
        val client = NwsAlertClient(
            transport = StaticTransport(
                NwsAlertHttpResponse(
                    statusCode = 503,
                    body = """{"title":"maintenance"}""",
                ),
            ),
        )

        assertEquals(NwsAlertClientError.ProviderUnavailable, client.fetch(validPoint).error())
    }

    @Test
    fun classifiesMalformedSuccessfulBodyAsInvalidResponse() {
        val client = NwsAlertClient(
            transport = StaticTransport(
                NwsAlertHttpResponse(
                    statusCode = 200,
                    body = fixture("alerts_active_malformed_envelope.json"),
                    contentType = "application/geo+json",
                ),
            ),
        )

        assertEquals(NwsAlertClientError.InvalidResponse, client.fetch(validPoint).error())
    }

    @Test
    fun classifiesUnexpectedProviderResponses() {
        val client = NwsAlertClient(
            transport = StaticTransport(
                NwsAlertHttpResponse(
                    statusCode = 418,
                    body = """{"title":"teapot"}""",
                ),
            ),
        )

        assertEquals(NwsAlertClientError.UnexpectedProvider, client.fetch(validPoint).error())
    }

    private fun NwsAlertClientResult.error(): NwsAlertClientError =
        (this as NwsAlertClientResult.Failure).error

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/nws/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}

private class RecordingTransport(
    private val response: NwsAlertHttpResponse,
) : NwsAlertHttpTransport {
    var calls: Int = 0
        private set
    lateinit var requestedUrl: java.net.URL
        private set
    lateinit var requestedHeaders: Map<String, String>
        private set

    override fun get(url: java.net.URL, headers: Map<String, String>): NwsAlertHttpResponse {
        calls += 1
        requestedUrl = url
        requestedHeaders = headers
        return response
    }
}

private class StaticTransport(
    private val response: NwsAlertHttpResponse,
) : NwsAlertHttpTransport {
    override fun get(url: java.net.URL, headers: Map<String, String>): NwsAlertHttpResponse = response
}

private class ThrowingTransport(
    private val error: IOException,
) : NwsAlertHttpTransport {
    override fun get(url: java.net.URL, headers: Map<String, String>): NwsAlertHttpResponse {
        throw error
    }
}

package com.oxygen.weather.core.provider.nws

import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.provider.AlertProviderError
import com.oxygen.weather.core.provider.AlertProviderResult
import com.oxygen.weather.core.provider.AlertSuccessMetadata
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NwsAlertProviderTest {
    private val point = GeoPoint(41.875, -87.625)
    private val fetchedAt = Instant.parse("2026-09-05T18:05:00Z")
    private val sentAt = Instant.parse("2026-09-05T18:00:00Z")
    private val clock = Clock.fixed(fetchedAt, ZoneOffset.UTC)

    @Test
    fun mapsSuccessfulCollectionAndMetadataToProviderNeutralResult() {
        val client = NwsAlertClient(
            configuration = NwsAlertConfiguration(
                baseUri = URI("https://alerts.test/alerts/active"),
                userAgent = "TestAgent/1.0",
                accept = "application/geo+json",
            ),
            transport = ProviderStaticTransport(
                NwsAlertHttpResponse(
                    statusCode = 200,
                    body = fixture("alerts_active_one.json"),
                    contentType = "application/geo+json; charset=utf-8",
                    headers = mapOf(
                        "Cache-Control" to "public, max-age=5",
                        "Expires" to "Wed, 05 Sep 2026 18:10:00 GMT",
                        "ETag" to "\"abc123\"",
                        "Last-Modified" to "Wed, 05 Sep 2026 18:00:00 GMT",
                    ),
                ),
            ),
        )
        val provider = NwsAlertProvider(client = client, clock = clock)

        val result = runSuspend { provider.getActiveAlerts(point) }

        val success = result as AlertProviderResult.Success
        assertEquals(point, success.metadata.requestPoint)
        assertEquals("nws", success.metadata.providerId)
        assertEquals(fetchedAt, success.metadata.fetchedAt)
        assertEquals("public, max-age=5", success.metadata.cacheControl)
        assertEquals("Wed, 05 Sep 2026 18:10:00 GMT", success.metadata.expires)
        assertEquals("\"abc123\"", success.metadata.etag)
        assertEquals("Wed, 05 Sep 2026 18:00:00 GMT", success.metadata.lastModified)
        assertEquals(1, success.alerts.size)

        val alert = success.alerts.single()
        assertEquals("urn:oid:2.49.0.1.840.0.full", alert.id)
        assertEquals("Flash Flood Warning", alert.event)
        assertEquals(fetchedAt, alert.provenance.fetchedAt)
        assertEquals(sentAt, alert.provenance.issuedAt)
        assertEquals("nws", alert.provenance.providerId)
        assertEquals("NOAA/National Weather Service", alert.provenance.sourceName)
        assertTrue(requireNotNull(alert.headline).contains("Madison County"))
    }

    @Test
    fun mapsMalformedSuccessfulBodyToInvalidResponse() {
        val provider = NwsAlertProvider(
            client = NwsAlertClient(
                transport = ProviderStaticTransport(
                    NwsAlertHttpResponse(
                        statusCode = 200,
                        body = fixture("alerts_active_invalid_timestamp.json"),
                        contentType = "application/geo+json",
                    ),
                ),
            ),
            clock = clock,
        )

        val result = runSuspend { provider.getActiveAlerts(point) }

        assertEquals(AlertProviderResult.Failure(AlertProviderError.InvalidResponse), result)
    }

    @Test
    fun preservesClientFailureClassificationsAtProviderBoundary() {
        val provider = NwsAlertProvider(
            client = NwsAlertClient(
                transport = ProviderStaticTransport(
                    NwsAlertHttpResponse(
                        statusCode = 429,
                        body = """{"title":"Too Many Requests"}""",
                        contentType = "application/problem+json",
                        headers = mapOf("Retry-After" to "120"),
                    ),
                ),
            ),
            clock = clock,
        )

        val result = runSuspend { provider.getActiveAlerts(point) }

        assertEquals(AlertProviderResult.Failure(AlertProviderError.RateLimited("120")), result)
    }

    @Test
    fun mapsUnsupportedRegionAtProviderBoundary() {
        val provider = NwsAlertProvider(
            client = NwsAlertClient(
                transport = ProviderStaticTransport(
                    NwsAlertHttpResponse(
                        statusCode = 400,
                        body = fixture("alerts_problem_unsupported_region.json"),
                        contentType = "application/problem+json",
                    ),
                ),
            ),
            clock = clock,
        )

        val result = runSuspend { provider.getActiveAlerts(point) }

        assertEquals(AlertProviderResult.Failure(AlertProviderError.UnsupportedRegion), result)
    }

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/nws/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}

private fun <T> runSuspend(block: suspend () -> T): T {
    var outcome: Result<T>? = null
    block.startCoroutine(object : kotlin.coroutines.Continuation<T> {
        override val context = kotlin.coroutines.EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            outcome = result
        }
    })
    return requireNotNull(outcome) { "Suspending block did not complete synchronously" }.getOrThrow()
}

private class ProviderStaticTransport(
    private val response: NwsAlertHttpResponse,
) : NwsAlertHttpTransport {
    override fun get(url: java.net.URL, headers: Map<String, String>): NwsAlertHttpResponse = response
}

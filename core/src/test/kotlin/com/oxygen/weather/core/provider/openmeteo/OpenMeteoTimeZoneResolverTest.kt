package com.oxygen.weather.core.provider.openmeteo

import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.provider.CoordinateTimeZoneError
import com.oxygen.weather.core.provider.CoordinateTimeZoneResult
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.time.ZoneId

class OpenMeteoTimeZoneResolverTest {
    private val point = GeoPoint(43.0731, -89.4012)

    @Test fun `metadata only request retains device point instead of provider grid`() {
        val resolver = OpenMeteoTimeZoneResolver("https://example.invalid/v1/forecast", transport = { url ->
            assertEquals("latitude=43.0731&longitude=-89.4012&timezone=auto", url.query)
            OpenMeteoHttpResponse(200, fixture("success"))
        })
        assertEquals(CoordinateTimeZoneResult.Success(point, ZoneId.of("America/Chicago")), resolver.resolve(point))
    }

    @Test fun `bad WGS84 input never calls transport`() {
        val resolver = OpenMeteoTimeZoneResolver(transport = { error("Transport must not run") })
        listOf(GeoPoint(Double.NaN, 0.0), GeoPoint(0.0, Double.POSITIVE_INFINITY),
            GeoPoint(90.01, 0.0), GeoPoint(-90.01, 0.0), GeoPoint(0.0, 180.01), GeoPoint(0.0, -180.01))
            .forEach { assertEquals(failure(CoordinateTimeZoneError.InvalidPoint), resolver.resolve(it)) }
    }

    @Test fun `invalid bodies cannot substitute timezone`() {
        val invalid = listOf(fixture("missing"), fixture("invalid"), fixture("malformed"),
            "[]", "null", "{\"timezone\":null}", "{\"timezone\":123}",
            "{\"timezone\":\"Unknown/Place\"}", "{\"timezone\":\"\"}")
        invalid.forEach { body ->
            assertEquals(body, failure(CoordinateTimeZoneError.InvalidResponse),
                OpenMeteoTimeZoneResolver(transport = { OpenMeteoHttpResponse(200, body) }).resolve(point))
        }
    }

    @Test fun `HTTP and provider error bodies map to neutral failure`() {
        listOf(200 to CoordinateTimeZoneError.RequestRejected, 400 to CoordinateTimeZoneError.RequestRejected,
            403 to CoordinateTimeZoneError.RequestRejected, 429 to CoordinateTimeZoneError.RateLimited,
            500 to CoordinateTimeZoneError.ProviderUnavailable, 503 to CoordinateTimeZoneError.ProviderUnavailable,
            302 to CoordinateTimeZoneError.ProviderUnavailable).forEach { (status, expected) ->
            assertEquals(failure(expected), OpenMeteoTimeZoneResolver(transport = {
                OpenMeteoHttpResponse(status, fixture("error"))
            }).resolve(point))
        }
    }

    @Test fun `offline and unexpected transport failures cannot resolve a location`() {
        assertEquals(failure(CoordinateTimeZoneError.NetworkUnavailable),
            OpenMeteoTimeZoneResolver(transport = { throw IOException("offline") }).resolve(point))
        assertEquals(failure(CoordinateTimeZoneError.ProviderUnavailable),
            OpenMeteoTimeZoneResolver(transport = { throw IllegalStateException() }).resolve(point))
    }

    @Test fun `valid IANA UTC zone and coordinate boundaries are accepted only when supplied`() {
        val boundary = GeoPoint(-90.0, 180.0)
        assertEquals(CoordinateTimeZoneResult.Success(boundary, ZoneId.of("Etc/UTC")),
            OpenMeteoTimeZoneResolver(transport = { OpenMeteoHttpResponse(200, "{\"timezone\":\"Etc/UTC\"}") })
                .resolve(boundary))
    }

    private fun failure(error: CoordinateTimeZoneError) = CoordinateTimeZoneResult.Failure(error)
    private fun fixture(name: String) = javaClass.getResource("/providers/openmeteo/timezone-$name.json")!!.readText()
}

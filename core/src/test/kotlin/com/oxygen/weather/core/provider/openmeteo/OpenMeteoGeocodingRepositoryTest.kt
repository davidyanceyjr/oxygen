package com.oxygen.weather.core.provider.openmeteo

import com.oxygen.weather.core.provider.GeocodingError
import com.oxygen.weather.core.provider.GeocodingRepositoryResult
import java.io.IOException
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoGeocodingRepositoryTest {
    @Test
    fun emitsLoadingBeforeSuccess() {
        val repository = repository(
            transport = RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(200, fixture("search_normal.json"))),
        )

        val results = repository.search("Chicago").toList()

        assertEquals(2, results.size)
        assertSame(GeocodingRepositoryResult.Loading, results[0])
        assertTrue(results[1] is GeocodingRepositoryResult.Success)
    }

    @Test
    fun passesSearchOptionsToClientBoundary() {
        val transport = RepositoryGeocodingRecordingTransport(
            OpenMeteoHttpResponse(200, fixture("search_empty.json")),
        )
        val repository = repository(transport = transport)

        repository.search(
            query = "Chicago",
            count = 8,
            language = "en",
            countryCode = "us",
        ).toList()

        val query = transport.requestedUrl.queryParameters()
        assertEquals("Chicago", query["name"])
        assertEquals("8", query["count"])
        assertEquals("en", query["language"])
        assertEquals("US", query["countryCode"])
    }

    @Test
    fun mapsFixtureBackedSuccessToProviderNeutralCandidates() {
        val repository = repository(
            transport = RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(200, fixture("search_normal.json"))),
        )

        val success = repository.search("Chicago").terminalSuccess()
        val candidate = success.candidates.single()

        assertEquals("Chicago, Illinois, United States", candidate.displayName)
        assertEquals(candidate.locationId, candidate.location.id)
        assertEquals(candidate.displayName, candidate.location.displayName)
        assertEquals(candidate.point, candidate.location.point)
        assertEquals(candidate.zoneId, candidate.location.zoneId)
    }

    @Test
    fun mapsEmptyProviderResultsToExplicitEmptyState() {
        val repository = repository(
            transport = RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(200, fixture("search_empty.json"))),
        )

        val results = repository.search("No Such Place").toList()

        assertSame(GeocodingRepositoryResult.Loading, results.first())
        assertSame(GeocodingRepositoryResult.Empty, results.last())
    }

    @Test
    fun translatesClientAndMappingErrorsToProviderNeutralErrors() {
        val cases = listOf(
            RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(429, """{"reason":"Too many requests"}""")) to
                GeocodingError.RateLimited("open-meteo"),
            RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(503, """{"reason":"maintenance"}""")) to
                GeocodingError.ProviderUnavailable("open-meteo"),
            RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(200, """{"results":{}}""")) to
                GeocodingError.InvalidResponse("open-meteo"),
            RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(200, fixture("search_invalid_coordinate.json"))) to
                GeocodingError.InvalidResponse("open-meteo"),
            RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(400, fixture("search_provider_error.json"))) to
                GeocodingError.ProviderRejectedRequest("open-meteo"),
            RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(418, """{"reason":"teapot"}""")) to
                GeocodingError.UnexpectedProviderFailure("open-meteo"),
            RepositoryGeocodingThrowingTransport(IOException("offline")) to
                GeocodingError.NetworkUnavailable,
        )

        cases.forEach { (transport, expected) ->
            val failure = repository(transport = transport).search("Chicago").terminalFailure()

            assertEquals(expected, failure.error)
        }
    }

    @Test
    fun translatesInvalidQueryToProviderNeutralFailure() {
        val repository = repository(transport = RepositoryGeocodingFailingTransport)

        val failure = repository.search("   ").terminalFailure()

        assertEquals(GeocodingError.InvalidQuery, failure.error)
    }

    @Test
    fun mapsProviderEmptyBodyWithoutResultsToExplicitEmptyState() {
        val repository = repository(
            transport = RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(200, fixture("search_malformed_envelope.json"))),
        )

        val results = repository.search("No Such Place").toList()

        assertSame(GeocodingRepositoryResult.Loading, results.first())
        assertSame(GeocodingRepositoryResult.Empty, results.last())
    }

    @Test
    fun preservesProviderResponseOrderAfterMapping() {
        val repository = repository(
            transport = RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(200, fixture("search_ambiguous.json"))),
        )

        val success = repository.search("New York").terminalSuccess()

        assertEquals(
            listOf(
                "New York, New York, United States",
                "New York, New Jersey, United States",
            ),
            success.candidates.map { it.displayName },
        )
    }

    @Test
    fun repositoryBoundaryDoesNotExposeProviderSpecificTypes() {
        val repository = repository(
            transport = RepositoryGeocodingStaticTransport(OpenMeteoHttpResponse(200, fixture("search_normal.json"))),
        )

        val results = repository.search("Chicago").toList()

        assertFalse(results.any { it::class.qualifiedName.orEmpty().contains(".openmeteo.") })
        assertTrue(results.first() is GeocodingRepositoryResult.Loading)
        assertTrue(results.last() is GeocodingRepositoryResult.Success)
    }

    private fun repository(transport: OpenMeteoHttpTransport): OpenMeteoGeocodingRepository =
        OpenMeteoGeocodingRepository(
            client = OpenMeteoGeocodingClient(
                baseUrl = "https://geo.test/v1/search",
                transport = transport,
            ),
        )

    private fun Sequence<GeocodingRepositoryResult>.terminalSuccess(): GeocodingRepositoryResult.Success =
        toList().last() as GeocodingRepositoryResult.Success

    private fun Sequence<GeocodingRepositoryResult>.terminalFailure(): GeocodingRepositoryResult.Failure =
        toList().last() as GeocodingRepositoryResult.Failure

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

private class RepositoryGeocodingRecordingTransport(
    private val response: OpenMeteoHttpResponse,
) : OpenMeteoHttpTransport {
    lateinit var requestedUrl: URL

    override fun get(url: URL): OpenMeteoHttpResponse {
        requestedUrl = url
        return response
    }
}

private class RepositoryGeocodingStaticTransport(
    private val response: OpenMeteoHttpResponse,
) : OpenMeteoHttpTransport {
    override fun get(url: URL): OpenMeteoHttpResponse = response
}

private class RepositoryGeocodingThrowingTransport(
    private val error: IOException,
) : OpenMeteoHttpTransport {
    override fun get(url: URL): OpenMeteoHttpResponse {
        throw error
    }
}

private object RepositoryGeocodingFailingTransport : OpenMeteoHttpTransport {
    override fun get(url: URL): OpenMeteoHttpResponse {
        error("Transport should not be called")
    }
}

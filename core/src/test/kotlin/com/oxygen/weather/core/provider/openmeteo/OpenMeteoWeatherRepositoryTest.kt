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

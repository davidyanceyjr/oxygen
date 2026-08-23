package com.oxygen.weather.core.provider.metno

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

class MetNoWeatherRepositoryTest {
    private val fetchedAt = Instant.parse("2026-08-23T13:20:00Z")
    private val chicago = WeatherLocation(
        id = LocationId("manual-chicago"),
        displayName = "Chicago, Illinois",
        point = GeoPoint(41.875, -87.625),
        elevationMeters = 181.6,
        zoneId = ZoneId.of("America/Chicago"),
    )

    @Test
    fun emitsLoadingBeforeSuccess() {
        val repository = repository(
            transport = RepositoryStaticMetNoTransport(MetNoHttpResponse(200, body = fixture("home_forecast_normal.json"))),
        )

        val results = repository.refresh(chicago).toList()

        assertEquals(2, results.size)
        assertSame(WeatherRepositoryResult.Loading, results[0])
        assertTrue(results[1] is WeatherRepositoryResult.Success)
    }

    @Test
    fun usesExplicitLocationCoordinatesAndOptionalElevationWithoutCacheRevalidation() {
        val transport = RepositoryRecordingMetNoTransport(
            MetNoHttpResponse(200, body = fixture("home_forecast_normal.json")),
        )
        val repository = repository(transport = transport)

        repository.refresh(chicago).toList()

        val query = transport.request.url.queryParameters()
        assertEquals("41.875", query["lat"])
        assertEquals("-87.625", query["lon"])
        assertEquals("182", query["altitude"])
        assertFalse(transport.request.headers.containsKey("If-Modified-Since"))
    }

    @Test
    fun omitsAltitudeWhenSelectedLocationHasNoElevation() {
        val locationWithoutElevation = chicago.copy(elevationMeters = null)
        val transport = RepositoryRecordingMetNoTransport(
            MetNoHttpResponse(200, body = fixture("home_forecast_normal.json")),
        )
        val repository = repository(transport = transport)

        repository.refresh(locationWithoutElevation).toList()

        assertFalse(transport.request.url.queryParameters().containsKey("altitude"))
    }

    @Test
    fun mapsFixtureBackedSuccessToProviderNeutralWeatherBundle() {
        val repository = repository(
            transport = RepositoryStaticMetNoTransport(MetNoHttpResponse(200, body = fixture("home_forecast_normal.json"))),
        )

        val success = repository.refresh(chicago).terminalSuccess()
        val bundle = success.weather

        assertEquals(chicago, bundle.location)
        assertEquals(fetchedAt, bundle.fetchedAt)
        assertEquals(Instant.parse("2026-08-23T11:00:00Z"), requireNotNull(bundle.current).time)
        assertEquals(DataType.MODEL_ESTIMATE, bundle.current.provenance.type)
        assertEquals("met-norway", bundle.current.provenance.providerId)
        assertEquals("MET Norway", bundle.current.provenance.sourceName)
        assertEquals("NLOD-2.0 OR CC-BY-4.0", bundle.current.provenance.licenseId)
        assertEquals(Instant.parse("2026-08-23T10:15:00Z"), bundle.current.provenance.issuedAt)
        assertEquals(fetchedAt, bundle.current.provenance.fetchedAt)
        assertEquals(DataType.FORECAST, bundle.hourly.first().provenance.type)
        assertEquals(DataType.FORECAST, bundle.daily.first().provenance.type)
    }

    @Test
    fun translatesMetNoClientErrorsToProviderNeutralForecastErrors() {
        val cases = listOf(
            RepositoryStaticMetNoTransport(MetNoHttpResponse(429)) to ForecastError.RateLimited("met-norway"),
            RepositoryStaticMetNoTransport(MetNoHttpResponse(503)) to ForecastError.ProviderUnavailable("met-norway"),
            RepositoryStaticMetNoTransport(MetNoHttpResponse(403)) to ForecastError.ProviderRejectedRequest("met-norway"),
            RepositoryStaticMetNoTransport(
                MetNoHttpResponse(400, headers = mapOf("X-ErrorClass" to "ParameterError")),
            ) to ForecastError.InvalidResponse("met-norway"),
            RepositoryStaticMetNoTransport(
                MetNoHttpResponse(404, headers = mapOf("X-ErrorClass" to "OutsideArea")),
            ) to ForecastError.ProviderRejectedRequest("met-norway"),
            RepositoryStaticMetNoTransport(MetNoHttpResponse(418)) to ForecastError.UnexpectedProviderFailure("met-norway"),
            RepositoryThrowingMetNoTransport(IOException("offline")) to ForecastError.NetworkUnavailable,
        )

        cases.forEach { (transport, expected) ->
            val failure = repository(transport = transport).refresh(chicago).terminalFailure()

            assertEquals(expected, failure.error)
        }
    }

    @Test
    fun translatesMapperFailuresToInvalidProviderResponse() {
        val cases = listOf(
            fixture("home_forecast_unexpected_units.json"),
            fixture("home_forecast_normal.json")
                .replace("2026-08-23T11:00:00Z", "2026-08-23 11:00"),
            emptyTimeseriesBody(),
        )

        cases.forEach { body ->
            val failure = repository(
                transport = RepositoryStaticMetNoTransport(MetNoHttpResponse(200, body = body)),
            ).refresh(chicago).terminalFailure()

            assertEquals(ForecastError.InvalidResponse("met-norway"), failure.error)
        }
    }

    @Test
    fun treatsUnexpectedNotModifiedWithoutCacheAsProviderNeutralFailure() {
        val failure = repository(
            transport = RepositoryStaticMetNoTransport(MetNoHttpResponse(304)),
        ).refresh(chicago).terminalFailure()

        assertEquals(ForecastError.InvalidResponse("met-norway"), failure.error)
    }

    @Test
    fun repositoryBoundaryDoesNotExposeProviderSpecificTypes() {
        val repository = repository(
            transport = RepositoryStaticMetNoTransport(MetNoHttpResponse(200, body = fixture("home_forecast_normal.json"))),
        )

        val results = repository.refresh(chicago).toList()

        assertFalse(results.any { it::class.qualifiedName.orEmpty().contains(".metno.") })
        assertTrue(results.first() is WeatherRepositoryResult.Loading)
        assertTrue(results.last() is WeatherRepositoryResult.Success)
    }

    private fun repository(transport: MetNoHttpTransport): MetNoWeatherRepository =
        MetNoWeatherRepository(
            client = MetNoForecastClient(
                baseUrl = "https://weather.test/compact",
                transport = transport,
            ),
            clock = { fetchedAt },
        )

    private fun Sequence<WeatherRepositoryResult>.terminalSuccess(): WeatherRepositoryResult.Success =
        toList().last() as WeatherRepositoryResult.Success

    private fun Sequence<WeatherRepositoryResult>.terminalFailure(): WeatherRepositoryResult.Failure =
        toList().last() as WeatherRepositoryResult.Failure

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

    private fun emptyTimeseriesBody(): String =
        """
        {
          "type": "Feature",
          "geometry": {
            "type": "Point",
            "coordinates": [-87.625, 41.875, 181.0]
          },
          "properties": {
            "meta": {
              "updated_at": "2026-08-23T10:15:00Z",
              "units": {}
            },
            "timeseries": []
          }
        }
        """.trimIndent()
}

private class RepositoryRecordingMetNoTransport(
    private val response: MetNoHttpResponse,
) : MetNoHttpTransport {
    lateinit var request: MetNoHttpRequest

    override fun get(request: MetNoHttpRequest): MetNoHttpResponse {
        this.request = request
        return response
    }
}

private class RepositoryStaticMetNoTransport(
    private val response: MetNoHttpResponse,
) : MetNoHttpTransport {
    override fun get(request: MetNoHttpRequest): MetNoHttpResponse = response
}

private class RepositoryThrowingMetNoTransport(
    private val error: IOException,
) : MetNoHttpTransport {
    override fun get(request: MetNoHttpRequest): MetNoHttpResponse {
        throw error
    }
}

package com.oxygen.weather.core.provider

import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackWeatherRepositoryTest {
    private val fetchedAt = Instant.parse("2026-08-23T13:20:00Z")
    private val chicago = WeatherLocation(
        id = LocationId("manual-chicago"),
        displayName = "Chicago, Illinois",
        point = GeoPoint(41.875, -87.625),
        elevationMeters = 181.6,
        zoneId = ZoneId.of("America/Chicago"),
    )
    private val madison = WeatherLocation(
        id = LocationId("manual-madison"),
        displayName = "Madison, Wisconsin",
        point = GeoPoint(43.0747, -89.3844),
        zoneId = ZoneId.of("America/Chicago"),
    )

    @Test
    fun emitsOneTopLevelLoadingAndSuppressesChildLoadingEmissions() {
        val repository = FallbackWeatherRepository(
            defaultRepository = RecordingWeatherRepository(
                listOf(WeatherRepositoryResult.Loading, WeatherRepositoryResult.Success(bundle("open-meteo", chicago))),
            ),
            fallbackRepository = RecordingWeatherRepository(
                listOf(WeatherRepositoryResult.Loading, WeatherRepositoryResult.Success(bundle("met-norway", chicago))),
            ),
        )

        val results = repository.refresh(chicago).toList()

        assertEquals(2, results.size)
        assertSame(WeatherRepositoryResult.Loading, results.first())
        assertTrue(results.last() is WeatherRepositoryResult.Success)
    }

    @Test
    fun returnsOpenMeteoSuccessWithoutCallingMetNorway() {
        val openMeteoBundle = bundle("open-meteo", chicago)
        val openMeteo = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Loading, WeatherRepositoryResult.Success(openMeteoBundle)),
        )
        val metNorway = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(bundle("met-norway", chicago))),
        )

        val success = FallbackWeatherRepository(openMeteo, metNorway).refresh(chicago).terminalSuccess()

        assertSame(openMeteoBundle, success.weather)
        assertEquals(listOf(chicago), openMeteo.locations)
        assertEquals(emptyList<WeatherLocation>(), metNorway.locations)
    }

    @Test
    fun eligibleOpenMeteoFailureAttemptsMetNorwayOnceWithExactLocationAndReturnsMetNorwayProvenance() {
        val metNorwayBundle = bundle("met-norway", chicago)
        val openMeteo = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Loading, WeatherRepositoryResult.Failure(ForecastError.RateLimited("open-meteo"))),
        )
        val metNorway = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Loading, WeatherRepositoryResult.Success(metNorwayBundle)),
        )

        val success = FallbackWeatherRepository(openMeteo, metNorway).refresh(chicago).terminalSuccess()

        assertSame(metNorwayBundle, success.weather)
        assertEquals("met-norway", requireNotNull(success.weather.current).provenance.providerId)
        assertEquals(listOf(chicago), openMeteo.locations)
        assertEquals(listOf(chicago), metNorway.locations)
    }

    @Test
    fun allEligibleOpenMeteoFailuresAttemptMetNorwayExactlyOnce() {
        val eligibleErrors = listOf(
            ForecastError.RateLimited("open-meteo"),
            ForecastError.ProviderUnavailable("open-meteo"),
            ForecastError.InvalidResponse("open-meteo"),
            ForecastError.UnexpectedProviderFailure("open-meteo"),
        )

        eligibleErrors.forEach { error ->
            val metNorway = RecordingWeatherRepository(
                listOf(WeatherRepositoryResult.Success(bundle("met-norway", chicago))),
            )

            FallbackWeatherRepository(
                defaultRepository = RecordingWeatherRepository(listOf(WeatherRepositoryResult.Failure(error))),
                fallbackRepository = metNorway,
            ).refresh(chicago).toList()

            assertEquals(listOf(chicago), metNorway.locations)
        }
    }

    @Test
    fun bothProviderFailureReturnsMetNorwayErrorAndOrderedDiagnostics() {
        val openMeteoError = ForecastError.ProviderUnavailable("open-meteo")
        val metNorwayError = ForecastError.InvalidResponse("met-norway")

        val failure = FallbackWeatherRepository(
            defaultRepository = RecordingWeatherRepository(listOf(WeatherRepositoryResult.Failure(openMeteoError))),
            fallbackRepository = RecordingWeatherRepository(listOf(WeatherRepositoryResult.Failure(metNorwayError))),
        ).refresh(chicago).terminalFailure()

        assertEquals(metNorwayError, failure.error)
        assertEquals(listOf(openMeteoError, metNorwayError), failure.diagnostics)
    }

    @Test
    fun nonEligibleOpenMeteoFailureReturnsImmediatelyWithoutMetNorwayAttempt() {
        val nonEligibleErrors = listOf(
            ForecastError.NetworkUnavailable,
            ForecastError.ProviderRejectedRequest("open-meteo"),
        )

        nonEligibleErrors.forEach { error ->
            val metNorway = RecordingWeatherRepository(
                listOf(WeatherRepositoryResult.Success(bundle("met-norway", chicago))),
            )

            val failure = FallbackWeatherRepository(
                defaultRepository = RecordingWeatherRepository(listOf(WeatherRepositoryResult.Failure(error))),
                fallbackRepository = metNorway,
            ).refresh(chicago).terminalFailure()

            assertEquals(error, failure.error)
            assertEquals(listOf(error), failure.diagnostics)
            assertEquals(emptyList<WeatherLocation>(), metNorway.locations)
        }
    }

    @Test
    fun repeatedRefreshCallsDoNotRetainRetryStateOrLoopAcrossLocations() {
        val openMeteoError = ForecastError.ProviderUnavailable("open-meteo")
        val metNorwayError = ForecastError.ProviderUnavailable("met-norway")
        val openMeteo = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Loading, WeatherRepositoryResult.Failure(openMeteoError)),
        )
        val metNorway = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Loading, WeatherRepositoryResult.Failure(metNorwayError)),
        )
        val repository = FallbackWeatherRepository(openMeteo, metNorway)

        repository.refresh(chicago).terminalFailure()
        repository.refresh(madison).terminalFailure()

        assertEquals(listOf(chicago, madison), openMeteo.locations)
        assertEquals(listOf(chicago, madison), metNorway.locations)
    }

    private fun Sequence<WeatherRepositoryResult>.terminalSuccess(): WeatherRepositoryResult.Success =
        toList().last() as WeatherRepositoryResult.Success

    private fun Sequence<WeatherRepositoryResult>.terminalFailure(): WeatherRepositoryResult.Failure =
        toList().last() as WeatherRepositoryResult.Failure

    private fun bundle(providerId: String, location: WeatherLocation): WeatherBundle =
        WeatherBundle(
            location = location,
            current = CurrentConditions(
                time = fetchedAt,
                temperatureC = 21.0,
                condition = WeatherCondition.PARTLY_CLOUDY,
                provenance = DataProvenance(
                    providerId = providerId,
                    sourceName = if (providerId == "met-norway") "MET Norway" else "Open-Meteo",
                    fetchedAt = fetchedAt,
                    type = DataType.MODEL_ESTIMATE,
                ),
            ),
            fetchedAt = fetchedAt,
        )
}

private class RecordingWeatherRepository(
    private val emissions: List<WeatherRepositoryResult>,
) : WeatherRepository {
    val locations = mutableListOf<WeatherLocation>()

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        locations += location
        return emissions.asSequence()
    }
}

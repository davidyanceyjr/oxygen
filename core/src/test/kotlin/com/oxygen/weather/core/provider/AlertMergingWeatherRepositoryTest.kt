package com.oxygen.weather.core.provider

import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherAlert
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.cache.CachedWeatherRepository
import com.oxygen.weather.core.provider.cache.ForecastCacheMetadata
import com.oxygen.weather.core.provider.cache.ForecastCacheStorage
import java.time.Duration
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertMergingWeatherRepositoryTest {
    private val location = WeatherLocation(
        id = LocationId("manual-madison"),
        displayName = "Madison, Wisconsin",
        point = GeoPoint(43.0747, -89.3844),
        zoneId = ZoneId.of("America/Chicago"),
    )
    private val forecastFetchedAt = Instant.parse("2026-09-06T14:00:00Z")

    @Test
    fun rateLimitGateReusesSuccessfulAlertsAndAllowsExactThirtySecondRetry() {
        val clock = MutableAlertClock(Instant.parse("2026-09-06T14:00:00Z"))
        val alert = alert("gated", "Gated alert")
        val provider = RecordingAlertProvider(
            AlertProviderResult.Success(listOf(alert), alertMetadata()),
        )
        val repository = AlertMergingWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(bundle())),
            alertProvider = provider,
            clock = clock,
        )

        val first = terminalSuccess(repository.refresh(location))
        val skipped = terminalSuccess(repository.refresh(location))
        clock.advanceSeconds(30)
        val eligible = terminalSuccess(repository.refresh(location))

        assertEquals(2, provider.locations.size)
        assertEquals(listOf(alert), first.weather.alerts)
        assertEquals(AlertLookupStatus.Available(alertMetadata()), skipped.alertStatus)
        assertEquals(listOf(alert), skipped.weather.alerts)
        assertEquals(AlertLookupStatus.Available(alertMetadata()), eligible.alertStatus)
    }

    @Test
    fun forwardsLoadingAndForecastFailureWithoutLookingUpAlerts() {
        val failure = WeatherRepositoryResult.Failure(ForecastError.ProviderUnavailable("open-meteo"))
        val provider = RecordingAlertProvider(AlertProviderResult.Success(emptyList(), alertMetadata()))
        val repository = AlertMergingWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Loading, failure),
            alertProvider = provider,
        )

        assertEquals(listOf(WeatherRepositoryResult.Loading, failure), repository.refresh(location).toList())
        assertEquals(emptyList<GeoPoint>(), provider.locations)
    }

    @Test
    fun successReplacesForecastAlertsCallsProviderOnceAndUsesLastDuplicateAtFirstPosition() {
        val oldAlert = alert("old", "Old forecast alert")
        val firstA = alert("a", "First A")
        val firstB = alert("b", "First B")
        val lastA = alert("a", "Last A")
        val onlyC = alert("c", "Only C")
        val lastB = alert("b", "Last B")
        val provider = RecordingAlertProvider(
            AlertProviderResult.Success(listOf(firstA, firstB, lastA, onlyC, lastB), alertMetadata()),
        )
        val forecast = bundle(alerts = listOf(oldAlert))

        val success = terminalSuccess(
            AlertMergingWeatherRepository(
                upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(forecast)),
                alertProvider = provider,
            ).refresh(location),
        )

        assertEquals(listOf(location.point), provider.locations)
        assertEquals(listOf(lastA, lastB, onlyC), success.weather.alerts)
        assertEquals(AlertLookupStatus.Available(alertMetadata()), success.alertStatus)
        assertEquals(forecast.copy(alerts = listOf(lastA, lastB, onlyC)), success.weather)
    }

    @Test
    fun emptySuccessAndUnsupportedRegionHaveDistinctStatuses() {
        val emptyProvider = RecordingAlertProvider(
            AlertProviderResult.Success(emptyList(), alertMetadata()),
        )
        val emptySuccess = terminalSuccess(
            AlertMergingWeatherRepository(
                FixedWeatherRepository(WeatherRepositoryResult.Success(bundle())),
                emptyProvider,
            ).refresh(location),
        )
        assertEquals(AlertLookupStatus.NoAlerts(alertMetadata()), emptySuccess.alertStatus)
        assertEquals(emptyList<WeatherAlert>(), emptySuccess.weather.alerts)

        val unsupportedProvider = RecordingAlertProvider(
            AlertProviderResult.Failure(AlertProviderError.UnsupportedRegion),
        )
        val unsupportedSuccess = terminalSuccess(
            AlertMergingWeatherRepository(
                FixedWeatherRepository(WeatherRepositoryResult.Success(bundle())),
                unsupportedProvider,
            ).refresh(location),
        )
        assertEquals(AlertLookupStatus.UnsupportedRegion, unsupportedSuccess.alertStatus)
        assertEquals(emptyList<WeatherAlert>(), unsupportedSuccess.weather.alerts)
    }

    @Test
    fun nonUnsupportedAlertFailuresRetainForecastSuccessAndExposeError() {
        val errors = listOf<AlertProviderError>(
            AlertProviderError.InvalidPoint,
            AlertProviderError.InvalidRequest,
            AlertProviderError.IdentificationRejected,
            AlertProviderError.Network,
            AlertProviderError.RateLimited("120"),
            AlertProviderError.ProviderUnavailable,
            AlertProviderError.InvalidResponse,
            AlertProviderError.UnexpectedProvider,
        )

        errors.forEach { error ->
            val forecast = bundle()
            val success = terminalSuccess(
                AlertMergingWeatherRepository(
                    FixedWeatherRepository(WeatherRepositoryResult.Success(forecast)),
                    RecordingAlertProvider(AlertProviderResult.Failure(error)),
                ).refresh(location),
            )

            assertEquals(AlertLookupStatus.Failed(error), success.alertStatus)
            assertEquals(forecast.copy(alerts = emptyList()), success.weather)
        }
    }

    @Test
    fun staleAndFallbackForecastSuccessesPreserveForecastMetadataAcrossAlertMerge() {
        val cacheMetadata = ForecastCacheMetadata(
            providerId = "met-norway",
            fetchedAt = forecastFetchedAt,
            expires = "Sun, 06 Sep 2026 15:00:00 GMT",
            etag = "\"forecast\"",
        )
        val stale = WeatherRepositoryResult.Success(
            weather = bundle(providerId = "met-norway"),
            freshness = ForecastFreshness.StaleAfterFailedRefresh(
                staleAge = Duration.ofMinutes(45),
                refreshFailure = ForecastError.NetworkUnavailable,
            ),
            cacheMetadata = cacheMetadata,
        )
        val alert = alert("active", "Active alert")

        listOf(stale, stale.copy(freshness = ForecastFreshness.Fresh)).forEach { upstreamSuccess ->
            val provider = RecordingAlertProvider(AlertProviderResult.Success(listOf(alert), alertMetadata()))
            val merged = terminalSuccess(
                AlertMergingWeatherRepository(
                    FixedWeatherRepository(upstreamSuccess),
                    provider,
                ).refresh(location),
            )

            assertEquals(listOf(location.point), provider.locations)
            assertEquals(upstreamSuccess.weather.copy(alerts = listOf(alert)), merged.weather)
            assertEquals(upstreamSuccess.freshness, merged.freshness)
            assertEquals(upstreamSuccess.cacheMetadata, merged.cacheMetadata)
            assertEquals(AlertLookupStatus.Available(alertMetadata()), merged.alertStatus)
            assertEquals(forecastFetchedAt, merged.weather.fetchedAt)
            assertEquals("met-norway", requireNotNull(merged.weather.current).provenance.providerId)
        }
    }

    @Test
    fun requiredCompositionKeepsMergedAlertsOutOfForecastCache() {
        val forecast = bundle()
        val storage = RecordingForecastCacheStorage(forecast)
        val alert = alert("nws-1", "Official alert")
        val merged = terminalSuccess(
            AlertMergingWeatherRepository(
                upstream = CachedWeatherRepository(
                    upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(forecast)),
                    storage = storage,
                ),
                alertProvider = RecordingAlertProvider(
                    AlertProviderResult.Success(listOf(alert), alertMetadata()),
                ),
            ).refresh(location),
        )

        assertEquals(listOf(forecast), storage.replacements)
        assertTrue(storage.replacements.all { it.alerts.isEmpty() })
        assertEquals(listOf(alert), merged.weather.alerts)
        assertEquals(AlertLookupStatus.Available(alertMetadata()), merged.alertStatus)
    }

    private fun alert(id: String, headline: String): WeatherAlert = WeatherAlert(
        id = id,
        event = "Test alert",
        headline = headline,
        severity = com.oxygen.weather.core.model.AlertSeverity.SEVERE,
        issuer = "Test issuer",
        provenance = DataProvenance(
            providerId = "nws",
            sourceName = "NOAA/National Weather Service",
            fetchedAt = forecastFetchedAt,
            type = DataType.OFFICIAL_ALERT,
        ),
    )

    private fun alertMetadata() = AlertSuccessMetadata(
        requestPoint = location.point,
        providerId = "nws",
        fetchedAt = Instant.parse("2026-09-06T14:05:00Z"),
    )

    private fun bundle(
        providerId: String = "open-meteo",
        alerts: List<WeatherAlert> = emptyList(),
    ) = WeatherBundle(
        location = location,
        current = CurrentConditions(
            time = forecastFetchedAt,
            temperatureC = 20.0,
            condition = WeatherCondition.CLEAR,
            provenance = DataProvenance(
                providerId = providerId,
                sourceName = if (providerId == "met-norway") "MET Norway" else "Open-Meteo",
                issuedAt = forecastFetchedAt.minusSeconds(300),
                fetchedAt = forecastFetchedAt,
                type = DataType.FORECAST,
            ),
        ),
        alerts = alerts,
        fetchedAt = forecastFetchedAt,
    )

    private fun terminalSuccess(results: Sequence<WeatherRepositoryResult>): WeatherRepositoryResult.Success =
        results.first { it !is WeatherRepositoryResult.Loading } as WeatherRepositoryResult.Success
}

private class MutableAlertClock(initial: Instant) : Clock() {
    private var current = initial

    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId): Clock = this

    override fun instant(): Instant = current

    fun advanceSeconds(seconds: Long) {
        current = current.plusSeconds(seconds)
    }
}

private class FixedWeatherRepository(
    private vararg val results: WeatherRepositoryResult,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = results.asSequence()
}

private class RecordingAlertProvider(
    private val result: AlertProviderResult,
) : AlertProvider {
    override val id: String = "recording"
    val locations = mutableListOf<GeoPoint>()

    override fun getActiveAlerts(location: GeoPoint): AlertProviderResult {
        locations += location
        return result
    }
}

private class RecordingForecastCacheStorage(
    private val storedBundle: WeatherBundle,
) : ForecastCacheStorage {
    val replacements = mutableListOf<WeatherBundle>()

    override fun replaceBundle(bundle: WeatherBundle) {
        replacements += bundle
    }

    override fun readBundle(locationId: LocationId): WeatherBundle = storedBundle
}

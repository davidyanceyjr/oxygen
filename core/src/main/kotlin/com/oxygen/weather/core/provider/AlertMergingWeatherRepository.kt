package com.oxygen.weather.core.provider

import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.WeatherAlert
import com.oxygen.weather.core.model.WeatherLocation
import java.time.Clock
import java.time.Duration
import java.time.Instant

/** Adds an independent official-alert lookup to terminal forecast successes. */
class AlertMergingWeatherRepository(
    private val upstream: WeatherRepository,
    private val alertProvider: AlertProvider,
    private val clock: Clock = Clock.systemUTC(),
    private val requestGate: AlertRequestGate = AlertRequestGate(clock),
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = sequence {
        upstream.refresh(location).forEach { result ->
            when (result) {
                WeatherRepositoryResult.Loading,
                is WeatherRepositoryResult.Failure,
                -> yield(result)

                is WeatherRepositoryResult.Success -> {
                    val lookup = requestGate.tryAcquire(alertProvider.id, location.point, clock.instant())
                    val merged = when (lookup) {
                        is AlertRequestGate.Decision.Skipped -> {
                            requestGate.retained(alertProvider.id, location.point)?.let { retained ->
                                retained
                            } ?: AlertRequestGate.RetainedResult(
                                alerts = emptyList(),
                                status = AlertLookupStatus.SkippedByRateLimit(
                                    providerId = alertProvider.id,
                                    requestPoint = location.point,
                                    nextEligibleAt = lookup.nextEligibleAt,
                                ),
                            )
                        }
                        is AlertRequestGate.Decision.Allowed -> {
                            val alertResult = try {
                                alertProvider.getActiveAlerts(location.point)
                            } catch (_: Exception) {
                                AlertProviderResult.Failure(AlertProviderError.UnexpectedProvider)
                            }
                            requestGate.recordResult(
                                providerId = alertProvider.id,
                                point = location.point,
                                startedAt = lookup.startedAt,
                                result = alertResult,
                            )
                            alertResult.toMergedAlerts()
                        }
                    }
                    yield(
                        result.copy(
                            weather = result.weather.copy(alerts = merged.alerts),
                            alertStatus = merged.status,
                        ),
                    )
                }
            }
        }
    }

    private fun AlertProviderResult.toMergedAlerts(): AlertRequestGate.RetainedResult = when (this) {
        is AlertProviderResult.Success -> {
            val alerts = alerts.deduplicateByIdKeepingLastValue()
            AlertRequestGate.RetainedResult(
                alerts = alerts,
                status = if (alerts.isEmpty()) {
                    AlertLookupStatus.NoAlerts(metadata)
                } else {
                    AlertLookupStatus.Available(metadata)
                },
            )
        }

        is AlertProviderResult.Failure -> {
            AlertRequestGate.RetainedResult(
                alerts = emptyList(),
                status = when (error) {
                    AlertProviderError.UnsupportedRegion -> AlertLookupStatus.UnsupportedRegion
                    else -> AlertLookupStatus.Failed(error)
                },
            )
        }
    }

    private fun List<WeatherAlert>.deduplicateByIdKeepingLastValue(): List<WeatherAlert> {
        val lastById = associateBy(WeatherAlert::id)
        val emittedIds = HashSet<String>(size)
        return mapNotNull { alert ->
            if (emittedIds.add(alert.id)) lastById[alert.id] else null
        }
    }
}

/** Process-local alert request throttling; it is neither persistence nor a cache. */
class AlertRequestGate(
    private val clock: Clock = Clock.systemUTC(),
    private val minimumInterval: Duration = Duration.ofSeconds(30),
) {
    private data class Key(val providerId: String, val point: GeoPoint)

    private data class Record(
        val nextEligibleAt: Instant,
        val retained: RetainedResult?,
    )

    sealed interface Decision {
        data class Allowed(val startedAt: Instant) : Decision
        data class Skipped(val nextEligibleAt: Instant) : Decision
    }

    data class RetainedResult(
        val alerts: List<WeatherAlert>,
        val status: AlertLookupStatus,
    )

    private val records = mutableMapOf<Key, Record>()

    @Synchronized
    fun tryAcquire(
        providerId: String,
        point: GeoPoint,
        now: Instant = clock.instant(),
    ): Decision {
        val key = Key(providerId, point)
        val record = records[key]
        return if (record != null && now.isBefore(record.nextEligibleAt)) {
            Decision.Skipped(record.nextEligibleAt)
        } else {
            records[key] = Record(
                nextEligibleAt = now.plus(minimumInterval),
                retained = record?.retained,
            )
            Decision.Allowed(now)
        }
    }

    @Synchronized
    fun recordResult(
        providerId: String,
        point: GeoPoint,
        startedAt: Instant,
        result: AlertProviderResult,
        completedAt: Instant = clock.instant(),
    ) {
        val retryAfter = (result as? AlertProviderResult.Failure)
            ?.error
            ?.let { it as? AlertProviderError.RateLimited }
            ?.retryAfter
            ?.toLongOrNull()
            ?.takeIf { it > 0 }
        val nextEligibleAt = maxOf(
            startedAt.plus(minimumInterval),
            retryAfter?.let { completedAt.plusSeconds(it) } ?: startedAt.plus(minimumInterval),
        )
        val retained = (result as? AlertProviderResult.Success)?.let { success ->
            val alerts = success.alerts.deduplicateByIdKeepingLastValue()
            RetainedResult(
                alerts = alerts,
                status = if (alerts.isEmpty()) {
                    AlertLookupStatus.NoAlerts(success.metadata)
                } else {
                    AlertLookupStatus.Available(success.metadata)
                },
            )
        }
        records[Key(providerId, point)] = Record(nextEligibleAt, retained)
    }

    @Synchronized
    fun retained(providerId: String, point: GeoPoint): RetainedResult? =
        records[Key(providerId, point)]?.retained

    private fun List<WeatherAlert>.deduplicateByIdKeepingLastValue(): List<WeatherAlert> {
        val lastById = associateBy(WeatherAlert::id)
        val emittedIds = HashSet<String>(size)
        return mapNotNull { alert ->
            if (emittedIds.add(alert.id)) lastById[alert.id] else null
        }
    }
}

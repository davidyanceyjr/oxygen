package com.oxygen.weather.core.provider

import com.oxygen.weather.core.model.WeatherAlert
import com.oxygen.weather.core.model.WeatherLocation

/** Adds an independent official-alert lookup to terminal forecast successes. */
class AlertMergingWeatherRepository(
    private val upstream: WeatherRepository,
    private val alertProvider: AlertProvider,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = sequence {
        upstream.refresh(location).forEach { result ->
            when (result) {
                WeatherRepositoryResult.Loading,
                is WeatherRepositoryResult.Failure,
                -> yield(result)

                is WeatherRepositoryResult.Success -> {
                    val alertResult = alertProvider.getActiveAlerts(location.point)
                    val (alerts, status) = alertResult.toMergedAlerts()
                    yield(
                        result.copy(
                            weather = result.weather.copy(alerts = alerts),
                            alertStatus = status,
                        ),
                    )
                }
            }
        }
    }

    private fun AlertProviderResult.toMergedAlerts(): Pair<List<WeatherAlert>, AlertLookupStatus> = when (this) {
        is AlertProviderResult.Success -> {
            val alerts = alerts.deduplicateByIdKeepingLastValue()
            alerts to if (alerts.isEmpty()) AlertLookupStatus.NoAlerts else AlertLookupStatus.Available
        }

        is AlertProviderResult.Failure -> {
            emptyList<WeatherAlert>() to when (error) {
                AlertProviderError.UnsupportedRegion -> AlertLookupStatus.UnsupportedRegion
                else -> AlertLookupStatus.Failed(error)
            }
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

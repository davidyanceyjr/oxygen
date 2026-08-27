package com.oxygen.weather.core.provider.cache

import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.ForecastFreshness
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.time.Clock
import java.time.Duration

interface ForecastCacheStorage {
    fun replaceBundle(bundle: WeatherBundle)
    fun readBundle(locationId: LocationId): WeatherBundle?
}

class CachedWeatherRepository(
    private val upstream: WeatherRepository,
    private val storage: ForecastCacheStorage,
    private val clock: Clock = Clock.systemUTC(),
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = sequence {
        upstream.refresh(location).forEach { result ->
            when (result) {
                WeatherRepositoryResult.Loading -> yield(result)
                is WeatherRepositoryResult.Failure -> yield(retainCacheAfterEligibleFailure(location, result))
                is WeatherRepositoryResult.Success -> {
                    val readback = try {
                        storage.replaceBundle(result.weather)
                        storage.readBundle(location.id)
                    } catch (_: Exception) {
                        null
                    }

                    if (readback == null) {
                        yield(WeatherRepositoryResult.Failure(ForecastError.LocalCacheFailure))
                    } else {
                        yield(WeatherRepositoryResult.Success(readback))
                    }
                }
            }
        }
    }

    private fun retainCacheAfterEligibleFailure(
        location: WeatherLocation,
        failure: WeatherRepositoryResult.Failure,
    ): WeatherRepositoryResult {
        if (!failure.error.isStaleCacheEligible()) return failure

        val cachedBundle = try {
            storage.readBundle(location.id)
        } catch (_: Exception) {
            return WeatherRepositoryResult.Failure(ForecastError.LocalCacheFailure)
        } ?: return failure

        return WeatherRepositoryResult.Success(
            weather = cachedBundle,
            freshness = ForecastFreshness.StaleAfterFailedRefresh(
                staleAge = Duration.between(cachedBundle.fetchedAt, clock.instant()).coerceAtLeast(Duration.ZERO),
                refreshFailure = failure.error,
            ),
        )
    }

    private fun ForecastError.isStaleCacheEligible(): Boolean =
        when (this) {
            ForecastError.NetworkUnavailable,
            is ForecastError.RateLimited,
            is ForecastError.ProviderUnavailable,
            is ForecastError.InvalidResponse,
            is ForecastError.UnexpectedProviderFailure -> true
            is ForecastError.ProviderRejectedRequest,
            ForecastError.LocalCacheFailure -> false
        }
}

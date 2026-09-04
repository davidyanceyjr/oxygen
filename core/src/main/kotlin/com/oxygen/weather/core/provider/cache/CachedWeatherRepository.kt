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
import java.time.Instant

interface ForecastCacheStorage {
    fun replaceBundle(bundle: WeatherBundle)
    fun readBundle(locationId: LocationId): WeatherBundle?
}

data class ForecastCacheMetadata(
    val providerId: String,
    val expires: String? = null,
    val lastModified: String? = null,
    val etag: String? = null,
    val fetchedAt: Instant,
    val responseLatitude: Double? = null,
    val responseLongitude: Double? = null,
    val responseElevationMeters: Double? = null,
    val providerUpdatedAt: Instant? = null,
)

interface ForecastCacheMetadataStorage : ForecastCacheStorage {
    fun replaceBundle(
        bundle: WeatherBundle,
        cacheMetadata: ForecastCacheMetadata,
    )

    fun readCacheMetadata(locationId: LocationId): ForecastCacheMetadata?
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
                        if (result.cacheMetadata != null && storage is ForecastCacheMetadataStorage) {
                            storage.replaceBundle(result.weather, result.cacheMetadata)
                        } else {
                            storage.replaceBundle(result.weather)
                        }
                        storage.readBundle(location.id)
                    } catch (_: Exception) {
                        null
                    }

                    yield(WeatherRepositoryResult.Success(readback ?: result.weather))
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

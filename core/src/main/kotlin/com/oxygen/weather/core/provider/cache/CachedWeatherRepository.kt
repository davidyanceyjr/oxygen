package com.oxygen.weather.core.provider.cache

import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult

interface ForecastCacheStorage {
    fun replaceBundle(bundle: WeatherBundle)
    fun readBundle(locationId: LocationId): WeatherBundle?
}

class CachedWeatherRepository(
    private val upstream: WeatherRepository,
    private val storage: ForecastCacheStorage,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = sequence {
        upstream.refresh(location).forEach { result ->
            when (result) {
                WeatherRepositoryResult.Loading -> yield(result)
                is WeatherRepositoryResult.Failure -> yield(result)
                is WeatherRepositoryResult.Success -> {
                    val readback = try {
                        storage.replaceBundle(result.weather)
                        storage.readBundle(location.id)
                    } catch (_: RuntimeException) {
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
}

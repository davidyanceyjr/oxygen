package com.oxygen.weather.app

import com.oxygen.weather.core.provider.FallbackWeatherRepository
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.cache.CachedWeatherRepository
import com.oxygen.weather.core.provider.cache.ForecastCacheStorage
import com.oxygen.weather.core.provider.metno.MetNoForecastClient
import com.oxygen.weather.core.provider.metno.MetNoWeatherRepository
import com.oxygen.weather.core.provider.openmeteo.OpenMeteoWeatherRepository
import java.time.Clock

object InstalledForecastRepositoryFactory {
    fun create(
        storage: ForecastCacheStorage,
        defaultRepository: WeatherRepository = OpenMeteoWeatherRepository(),
        fallbackRepository: WeatherRepository = MetNoWeatherRepository(),
        clock: Clock = Clock.systemUTC(),
    ): WeatherRepository =
        CachedWeatherRepository(
            upstream = FallbackWeatherRepository(
                defaultRepository = defaultRepository,
                fallbackRepository = fallbackRepository,
            ),
            storage = storage,
            clock = clock,
        )

    val metNorwayUserAgent: String
        get() = MetNoForecastClient.DEFAULT_USER_AGENT
}

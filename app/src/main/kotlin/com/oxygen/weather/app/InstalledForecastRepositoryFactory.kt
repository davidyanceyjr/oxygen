package com.oxygen.weather.app

import com.oxygen.weather.core.provider.FallbackWeatherRepository
import com.oxygen.weather.core.provider.AlertMergingWeatherRepository
import com.oxygen.weather.core.provider.AlertProvider
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.cache.CachedWeatherRepository
import com.oxygen.weather.core.provider.cache.ForecastCacheStorage
import com.oxygen.weather.core.provider.metno.MetNoForecastClient
import com.oxygen.weather.core.provider.metno.MetNoWeatherRepository
import com.oxygen.weather.core.provider.openmeteo.OpenMeteoWeatherRepository
import com.oxygen.weather.core.provider.nws.NwsAlertProvider
import java.time.Clock

object InstalledForecastRepositoryFactory {
    fun create(
        storage: ForecastCacheStorage,
        defaultRepository: WeatherRepository = OpenMeteoWeatherRepository(),
        fallbackRepository: WeatherRepository = MetNoWeatherRepository(),
        clock: Clock = Clock.systemUTC(),
        alertProvider: AlertProvider = NwsAlertProvider(clock = clock),
    ): WeatherRepository {
        val forecastRepository = CachedWeatherRepository(
            upstream = FallbackWeatherRepository(
                defaultRepository = defaultRepository,
                fallbackRepository = fallbackRepository,
            ),
            storage = storage,
            clock = clock,
        )
        return AlertMergingWeatherRepository(
            upstream = forecastRepository,
            alertProvider = alertProvider,
            clock = clock,
        )
    }

    val metNorwayUserAgent: String
        get() = MetNoForecastClient.DEFAULT_USER_AGENT
}

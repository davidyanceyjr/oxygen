package com.oxygen.weather.core.provider

import com.oxygen.weather.core.model.AirQuality
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.WeatherAlert
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherLocation

interface ForecastProvider {
    val id: String
    suspend fun getWeather(location: WeatherLocation): WeatherBundle
}

interface AlertProvider {
    val id: String
    suspend fun getActiveAlerts(location: GeoPoint): List<WeatherAlert>
}

interface AirQualityProvider {
    val id: String
    suspend fun getAirQuality(location: GeoPoint): AirQuality?
}

interface GeocodingProvider {
    val id: String
    suspend fun search(query: String): List<WeatherLocation>
}

interface RadarProvider {
    val id: String
    fun supports(location: GeoPoint): Boolean
}

sealed class ForecastError {
    data object NetworkUnavailable : ForecastError()

    data class RateLimited(
        val providerId: String,
    ) : ForecastError()

    data class ProviderUnavailable(
        val providerId: String,
    ) : ForecastError()

    data class InvalidResponse(
        val providerId: String,
    ) : ForecastError()

    data class ProviderRejectedRequest(
        val providerId: String,
    ) : ForecastError()

    data class UnexpectedProviderFailure(
        val providerId: String,
    ) : ForecastError()
}

sealed class WeatherRepositoryResult {
    data object Loading : WeatherRepositoryResult()

    data class Success(
        val weather: WeatherBundle,
    ) : WeatherRepositoryResult()

    data class Failure(
        val error: ForecastError,
    ) : WeatherRepositoryResult()
}

interface WeatherRepository {
    fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult>
}

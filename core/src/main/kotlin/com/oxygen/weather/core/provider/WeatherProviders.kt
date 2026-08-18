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

interface WeatherRepository {
    suspend fun refresh(location: WeatherLocation): WeatherBundle
}

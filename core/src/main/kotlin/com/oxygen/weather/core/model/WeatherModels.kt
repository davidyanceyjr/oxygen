package com.oxygen.weather.core.model

import java.time.Instant
import java.time.ZoneId

@JvmInline
value class LocationId(val value: String)

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class WeatherLocation(
    val id: LocationId,
    val displayName: String,
    val point: GeoPoint,
    val elevationMeters: Double? = null,
    val zoneId: ZoneId,
)

enum class DataType {
    OBSERVATION,
    MODEL_ESTIMATE,
    FORECAST,
    OFFICIAL_ALERT,
    DERIVED,
}

data class DataProvenance(
    val providerId: String,
    val sourceName: String,
    val issuedAt: Instant? = null,
    val fetchedAt: Instant,
    val type: DataType,
    val licenseId: String? = null,
)

enum class WeatherCondition {
    CLEAR,
    MOSTLY_CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    FREEZING_DRIZZLE,
    RAIN,
    FREEZING_RAIN,
    RAIN_SHOWERS,
    SNOW,
    SNOW_SHOWERS,
    SLEET,
    HAIL,
    THUNDERSTORM,
    THUNDERSTORM_HAIL,
    UNKNOWN,
}

data class Wind(
    val speedMetersPerSecond: Double,
    val gustMetersPerSecond: Double? = null,
    val directionDegrees: Double? = null,
)

data class CurrentConditions(
    val time: Instant,
    val temperatureC: Double,
    val apparentTemperatureC: Double? = null,
    val dewPointC: Double? = null,
    val humidityPercent: Int? = null,
    val pressureHpa: Double? = null,
    val visibilityMeters: Double? = null,
    val cloudCoverPercent: Int? = null,
    val wind: Wind? = null,
    val precipitationMm: Double? = null,
    val condition: WeatherCondition,
    val provenance: DataProvenance,
)

data class HourlyForecast(
    val time: Instant,
    val temperatureC: Double,
    val precipitationProbabilityPercent: Int? = null,
    val precipitationMm: Double? = null,
    val condition: WeatherCondition,
)

data class DailyForecast(
    val dateEpochDay: Long,
    val highC: Double,
    val lowC: Double,
    val precipitationProbabilityPercent: Int? = null,
    val condition: WeatherCondition,
    val sunrise: Instant? = null,
    val sunset: Instant? = null,
)

enum class AlertSeverity { EXTREME, SEVERE, MODERATE, MINOR, UNKNOWN }

data class WeatherAlert(
    val id: String,
    val event: String,
    val headline: String? = null,
    val severity: AlertSeverity,
    val effective: Instant? = null,
    val expires: Instant? = null,
    val description: String? = null,
    val instruction: String? = null,
    val issuer: String,
    val provenance: DataProvenance,
)

data class AirQuality(
    val timestamp: Instant,
    val aqi: Int? = null,
    val standardName: String? = null,
    val pm25: Double? = null,
    val pm10: Double? = null,
    val ozone: Double? = null,
    val nitrogenDioxide: Double? = null,
    val provenance: DataProvenance,
)

data class WeatherBundle(
    val location: WeatherLocation,
    val current: CurrentConditions? = null,
    val hourly: List<HourlyForecast> = emptyList(),
    val daily: List<DailyForecast> = emptyList(),
    val alerts: List<WeatherAlert> = emptyList(),
    val airQuality: AirQuality? = null,
    val fetchedAt: Instant,
)

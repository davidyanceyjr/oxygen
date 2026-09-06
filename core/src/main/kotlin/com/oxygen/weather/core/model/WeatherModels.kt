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

data class GeocodingLocationCandidate(
    val locationId: LocationId,
    val displayName: String,
    val point: GeoPoint,
    val zoneId: ZoneId,
    val country: String,
    val countryCode: String,
    val administrativeAreas: List<String> = emptyList(),
    val elevationMeters: Double? = null,
    val featureCode: String? = null,
    val population: Int? = null,
    val postcodes: List<String> = emptyList(),
) {
    val location: WeatherLocation = WeatherLocation(
        id = locationId,
        displayName = displayName,
        point = point,
        elevationMeters = elevationMeters,
        zoneId = zoneId,
    )
}

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
    val speedMetersPerSecond: Double?,
    val gustMetersPerSecond: Double? = null,
    val directionDegrees: Double? = null,
)

data class CurrentConditions(
    val time: Instant,
    val temperatureC: Double?,
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
    val temperatureC: Double?,
    val precipitationProbabilityPercent: Int? = null,
    val precipitationMm: Double? = null,
    val condition: WeatherCondition,
    val provenance: DataProvenance,
)

data class DailyForecast(
    val dateEpochDay: Long,
    val highC: Double?,
    val lowC: Double?,
    val precipitationProbabilityPercent: Int? = null,
    val condition: WeatherCondition,
    val sunrise: Instant? = null,
    val sunset: Instant? = null,
    val provenance: DataProvenance,
)

enum class AlertSeverity { EXTREME, SEVERE, MODERATE, MINOR, UNKNOWN }

enum class AlertUrgency { IMMEDIATE, EXPECTED, FUTURE, PAST, UNKNOWN }

enum class AlertCertainty { OBSERVED, LIKELY, POSSIBLE, UNLIKELY, UNKNOWN }

enum class AlertStatus { ACTUAL, EXERCISE, SYSTEM, TEST, DRAFT, UNKNOWN }

enum class AlertMessageType { ALERT, UPDATE, CANCEL, ACK, ERROR, UNKNOWN }

data class AlertReference(
    val id: String,
    val sender: String? = null,
    val sent: Instant? = null,
)

data class AlertAffectedArea(
    val areaDescription: String? = null,
    val ugcCodes: List<String> = emptyList(),
    val sameCodes: List<String> = emptyList(),
    val affectedZoneIds: List<String> = emptyList(),
)

sealed interface AlertGeometry {
    data class Point(val point: GeoPoint) : AlertGeometry
    data class MultiPoint(val points: List<GeoPoint>) : AlertGeometry
    data class LineString(val points: List<GeoPoint>) : AlertGeometry
    data class MultiLineString(val lines: List<List<GeoPoint>>) : AlertGeometry
    data class Polygon(val rings: List<List<GeoPoint>>) : AlertGeometry
    data class MultiPolygon(val polygons: List<List<List<GeoPoint>>>) : AlertGeometry
    data class GeometryCollection(val geometries: List<AlertGeometry>) : AlertGeometry
}

data class WeatherAlert(
    val id: String,
    val event: String,
    val headline: String? = null,
    val severity: AlertSeverity,
    val urgency: AlertUrgency = AlertUrgency.UNKNOWN,
    val certainty: AlertCertainty = AlertCertainty.UNKNOWN,
    val effective: Instant? = null,
    val expires: Instant? = null,
    val sent: Instant? = null,
    val onset: Instant? = null,
    val ends: Instant? = null,
    val status: AlertStatus = AlertStatus.UNKNOWN,
    val messageType: AlertMessageType = AlertMessageType.UNKNOWN,
    val references: List<AlertReference> = emptyList(),
    val affectedArea: AlertAffectedArea? = null,
    val geometry: AlertGeometry? = null,
    val category: String? = null,
    val response: String? = null,
    val scope: String? = null,
    val code: String? = null,
    val language: String? = null,
    val web: String? = null,
    val eventCodes: Map<String, List<String>>? = null,
    val parameters: Map<String, List<String>>? = null,
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

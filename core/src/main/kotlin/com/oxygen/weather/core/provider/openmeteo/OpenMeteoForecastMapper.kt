package com.oxygen.weather.core.provider.openmeteo

import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.model.Wind
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object OpenMeteoForecastMapper {
    private const val PROVIDER_ID = "open-meteo"
    private const val SOURCE_NAME = "Open-Meteo"
    private const val LICENSE_ID = "CC-BY-4.0"
    private const val KMH_TO_METERS_PER_SECOND = 1.0 / 3.6

    fun map(
        location: WeatherLocation,
        response: OpenMeteoForecastResponse,
        fetchedAt: Instant,
    ): WeatherBundle {
        val zoneId = ZoneId.of(response.timezone)
        val currentProvenance = provenance(fetchedAt, DataType.MODEL_ESTIMATE)
        val forecastProvenance = provenance(fetchedAt, DataType.FORECAST)

        return WeatherBundle(
            location = location,
            current = mapCurrent(response.current, zoneId, currentProvenance),
            hourly = mapHourly(response.hourly, zoneId, forecastProvenance),
            daily = mapDaily(response.daily, zoneId, forecastProvenance),
            fetchedAt = fetchedAt,
        )
    }

    fun mapWeatherCode(code: Int?): WeatherCondition = when (code) {
        0 -> WeatherCondition.CLEAR
        1 -> WeatherCondition.MOSTLY_CLEAR
        2 -> WeatherCondition.PARTLY_CLOUDY
        3 -> WeatherCondition.CLOUDY
        45, 48 -> WeatherCondition.FOG
        51, 53, 55 -> WeatherCondition.DRIZZLE
        56, 57 -> WeatherCondition.FREEZING_DRIZZLE
        61, 63, 65 -> WeatherCondition.RAIN
        66, 67 -> WeatherCondition.FREEZING_RAIN
        71, 73, 75, 77 -> WeatherCondition.SNOW
        80, 81, 82 -> WeatherCondition.RAIN_SHOWERS
        85, 86 -> WeatherCondition.SNOW_SHOWERS
        95 -> WeatherCondition.THUNDERSTORM
        96, 99 -> WeatherCondition.THUNDERSTORM_HAIL
        else -> WeatherCondition.UNKNOWN
    }

    private fun mapCurrent(
        current: OpenMeteoCurrentForecast,
        zoneId: ZoneId,
        provenance: DataProvenance,
    ): CurrentConditions =
        CurrentConditions(
            time = current.time.toInstant(zoneId),
            temperatureC = current.temperature2m,
            apparentTemperatureC = current.apparentTemperature,
            humidityPercent = current.relativeHumidity2m,
            pressureHpa = current.pressureMsl,
            cloudCoverPercent = current.cloudCover,
            wind = wind(
                speedKmh = current.windSpeed10m,
                gustKmh = current.windGusts10m,
                directionDegrees = current.windDirection10m,
            ),
            precipitationMm = current.precipitation,
            condition = mapWeatherCode(current.weatherCode),
            provenance = provenance,
        )

    private fun mapHourly(
        hourly: OpenMeteoHourlyForecast,
        zoneId: ZoneId,
        provenance: DataProvenance,
    ): List<HourlyForecast> =
        hourly.time.mapIndexed { index, time ->
            HourlyForecast(
                time = time.toInstant(zoneId),
                temperatureC = hourly.temperature2m.getOrNull(index),
                precipitationProbabilityPercent = hourly.precipitationProbability.getOrNull(index),
                precipitationMm = hourly.precipitation.getOrNull(index),
                condition = mapWeatherCode(hourly.weatherCode.getOrNull(index)),
                provenance = provenance,
            )
        }

    private fun mapDaily(
        daily: OpenMeteoDailyForecast,
        zoneId: ZoneId,
        provenance: DataProvenance,
    ): List<DailyForecast> =
        daily.time.mapIndexed { index, date ->
            DailyForecast(
                dateEpochDay = LocalDate.parse(date).toEpochDay(),
                highC = daily.temperature2mMax.getOrNull(index),
                lowC = daily.temperature2mMin.getOrNull(index),
                precipitationProbabilityPercent = daily.precipitationProbabilityMax.getOrNull(index),
                condition = mapWeatherCode(daily.weatherCode.getOrNull(index)),
                sunrise = daily.sunrise.getOrNull(index)?.toInstant(zoneId),
                sunset = daily.sunset.getOrNull(index)?.toInstant(zoneId),
                provenance = provenance,
            )
        }

    private fun wind(
        speedKmh: Double?,
        gustKmh: Double?,
        directionDegrees: Int?,
    ): Wind? {
        if (speedKmh == null && gustKmh == null && directionDegrees == null) return null

        return Wind(
            speedMetersPerSecond = speedKmh?.times(KMH_TO_METERS_PER_SECOND),
            gustMetersPerSecond = gustKmh?.times(KMH_TO_METERS_PER_SECOND),
            directionDegrees = directionDegrees?.toDouble(),
        )
    }

    private fun provenance(fetchedAt: Instant, type: DataType): DataProvenance =
        DataProvenance(
            providerId = PROVIDER_ID,
            sourceName = SOURCE_NAME,
            fetchedAt = fetchedAt,
            type = type,
            licenseId = LICENSE_ID,
        )

    private fun String.toInstant(zoneId: ZoneId): Instant =
        LocalDateTime.parse(this).atZone(zoneId).toInstant()
}

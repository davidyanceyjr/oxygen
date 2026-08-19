package com.oxygen.weather.app.sample

import com.oxygen.weather.core.model.AlertSeverity
import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherAlert
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.model.Wind
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object SampleWeather {
    private val now = Instant.parse("2026-08-17T16:00:00Z")
    private val source = DataProvenance(
        providerId = "sample",
        sourceName = "Oxygen sample data",
        fetchedAt = now,
        type = DataType.MODEL_ESTIMATE,
    )

    val bundle = WeatherBundle(
        location = WeatherLocation(
            id = LocationId("chicago"),
            displayName = "Chicago, Illinois",
            point = GeoPoint(41.8781, -87.6298),
            elevationMeters = 181.0,
            zoneId = ZoneId.of("America/Chicago"),
        ),
        current = CurrentConditions(
            time = now,
            temperatureC = 23.3,
            apparentTemperatureC = 24.4,
            dewPointC = 16.1,
            humidityPercent = 63,
            pressureHpa = 1014.0,
            visibilityMeters = 16093.0,
            cloudCoverPercent = 38,
            wind = Wind(5.4, 8.0, 225.0),
            condition = WeatherCondition.PARTLY_CLOUDY,
            provenance = source,
        ),
        hourly = List(8) { index ->
            HourlyForecast(
                time = now.plus(index.toLong(), ChronoUnit.HOURS),
                temperatureC = 23.3 + listOf(0.0, 0.7, 1.4, 1.8, 1.2, 0.3, -0.6, -1.2)[index],
                precipitationProbabilityPercent = listOf(5, 5, 10, 10, 20, 55, 70, 45)[index],
                condition = if (index >= 5) WeatherCondition.RAIN_SHOWERS else WeatherCondition.PARTLY_CLOUDY,
                provenance = source.copy(type = DataType.FORECAST),
            )
        },
        daily = List(7) { index ->
            DailyForecast(
                dateEpochDay = LocalDate.of(2026, 8, 17).plusDays(index.toLong()).toEpochDay(),
                highC = listOf(27.2, 25.0, 26.1, 28.3, 29.4, 27.8, 25.6)[index],
                lowC = listOf(18.9, 17.8, 18.3, 19.4, 20.0, 19.4, 17.2)[index],
                precipitationProbabilityPercent = listOf(70, 40, 15, 10, 20, 35, 20)[index],
                condition = listOf(
                    WeatherCondition.RAIN_SHOWERS,
                    WeatherCondition.PARTLY_CLOUDY,
                    WeatherCondition.MOSTLY_CLEAR,
                    WeatherCondition.CLEAR,
                    WeatherCondition.PARTLY_CLOUDY,
                    WeatherCondition.RAIN_SHOWERS,
                    WeatherCondition.PARTLY_CLOUDY,
                )[index],
                provenance = source.copy(type = DataType.FORECAST),
            )
        },
        alerts = listOf(
            WeatherAlert(
                id = "sample-watch",
                event = "Weather Watch",
                headline = "Sample alert card — live providers are not wired yet",
                severity = AlertSeverity.MODERATE,
                expires = now.plus(4, ChronoUnit.HOURS),
                issuer = "Oxygen scaffold",
                provenance = source.copy(type = DataType.OFFICIAL_ALERT),
            )
        ),
        fetchedAt = now,
    )
}

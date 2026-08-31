package com.oxygen.weather.app

import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.WeatherAlert
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.model.Wind
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private const val UNAVAILABLE = "Unavailable"
private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h a", Locale.US)
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)
private val FETCHED_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a z", Locale.US)

fun WeatherBundle.toHomeSuccessPresentation(selectedLocation: WeatherLocation): HomeSuccessPresentation {
    val zoneId = selectedLocation.zoneId
    val heroRange = daily.firstOrNull { it.highC != null || it.lowC != null }?.toHeroRangePresentation()
    val currentPresentation = current?.toCurrentPresentation(
        zoneId = zoneId,
        heroRange = heroRange,
    )
    val hourlyRows = hourly.take(12).map { it.toHourlyPresentation(zoneId) }
    val dailyRows = daily.take(10).map { it.toDailyPresentation(zoneId) }
    val metricRows = current?.toMetricRows().orEmpty()
    val sun = daily.firstOrNull { it.sunrise != null || it.sunset != null }?.toSunPresentation(zoneId)
    val provenance = mostRelevantProvenance()?.toSourcePresentation(zoneId) ?: bundleFallbackSource(zoneId)
    val alertsPresentation = alerts.map { it.toAlertPresentation(zoneId) }
    val precipitationSummary = hourly.nearTermPrecipitationSummary()
    val returnedDataUnavailable = current == null && hourly.isEmpty() && daily.isEmpty()

    return HomeSuccessPresentation(
        locationName = selectedLocation.displayName,
        locationSubtitle = selectedLocation.forecastSubtitle(),
        alerts = alertsPresentation,
        current = currentPresentation,
        currentUnavailableText = if (currentPresentation == null && !returnedDataUnavailable) {
            "Current conditions unavailable"
        } else {
            null
        },
        precipitationSummary = precipitationSummary,
        hourly = hourlyRows,
        daily = dailyRows,
        metrics = metricRows,
        sun = sun,
        source = provenance,
        returnedDataUnavailableText = if (returnedDataUnavailable) {
            "Provider returned no current, hourly, or daily weather data for this location."
        } else {
            null
        },
        sectionOrder = buildList {
            add(HomeSuccessSection.LocationHeader)
            if (alertsPresentation.isNotEmpty()) add(HomeSuccessSection.Alerts)
            add(HomeSuccessSection.Current)
            if (precipitationSummary != null) add(HomeSuccessSection.NearTermPrecipitation)
            if (hourlyRows.isNotEmpty()) add(HomeSuccessSection.Hourly)
            if (dailyRows.isNotEmpty()) add(HomeSuccessSection.Daily)
            if (metricRows.isNotEmpty()) add(HomeSuccessSection.Metrics)
            if (sun != null) add(HomeSuccessSection.Sun)
            add(HomeSuccessSection.Source)
            add(HomeSuccessSection.ProvenanceFooter)
        },
    )
}

data class HomeSuccessPresentation(
    val locationName: String,
    val locationSubtitle: String,
    val alerts: List<HomeAlertPresentation>,
    val current: HomeCurrentPresentation?,
    val currentUnavailableText: String?,
    val precipitationSummary: String?,
    val hourly: List<HomeHourlyPresentation>,
    val daily: List<HomeDailyPresentation>,
    val metrics: List<HomeMetricPresentation>,
    val sun: HomeSunPresentation?,
    val source: HomeSourcePresentation,
    val returnedDataUnavailableText: String?,
    val sectionOrder: List<HomeSuccessSection>,
)

enum class HomeSuccessSection {
    LocationHeader,
    Alerts,
    Current,
    NearTermPrecipitation,
    Hourly,
    Daily,
    Metrics,
    Sun,
    Source,
    ProvenanceFooter,
}

data class HomeAlertPresentation(
    val event: String,
    val headline: String,
    val severity: String,
    val issuer: String,
    val effective: String?,
    val expires: String?,
)

data class HomeCurrentPresentation(
    val temperature: String,
    val condition: String,
    val conditionIdentity: WeatherCondition,
    val apparentTemperature: String,
    val highTemperature: String?,
    val lowTemperature: String?,
    val updatedTime: String,
    val dataTypeLabel: String,
)

data class HomeHourlyPresentation(
    val time: String,
    val condition: String,
    val conditionIdentity: WeatherCondition,
    val temperature: String,
    val precipitationProbability: String?,
)

data class HomeDailyPresentation(
    val date: String,
    val condition: String,
    val conditionIdentity: WeatherCondition,
    val precipitationProbability: String?,
    val high: String,
    val low: String,
    val highC: Double?,
    val lowC: Double?,
    val sunrise: String?,
    val sunset: String?,
)

data class HomeMetricPresentation(
    val label: String,
    val value: String,
)

data class HomeSunPresentation(
    val sunrise: String,
    val sunset: String,
)

data class HomeSourcePresentation(
    val sourceName: String,
    val dataType: String,
    val fetchedAt: String,
    val issuedAt: String?,
    val license: String?,
)

private data class HomeHeroRangePresentation(
    val highTemperature: String?,
    val lowTemperature: String?,
)

private fun CurrentConditions.toCurrentPresentation(
    zoneId: ZoneId,
    heroRange: HomeHeroRangePresentation?,
): HomeCurrentPresentation =
    HomeCurrentPresentation(
        temperature = temperatureC.formatFahrenheit(),
        condition = condition.displayName(),
        conditionIdentity = condition,
        apparentTemperature = apparentTemperatureC?.let { "Feels like ${it.formatFahrenheit()}" } ?: "Feels like unavailable",
        highTemperature = heroRange?.highTemperature,
        lowTemperature = heroRange?.lowTemperature,
        updatedTime = "Updated ${time.formatLocalTime(zoneId)}",
        dataTypeLabel = provenance.type.displayLabel(),
    )

private fun DailyForecast.toHeroRangePresentation(): HomeHeroRangePresentation =
    HomeHeroRangePresentation(
        highTemperature = highC?.let { "H ${it.formatFahrenheit()}" },
        lowTemperature = lowC?.let { "L ${it.formatFahrenheit()}" },
    )

private fun HourlyForecast.toHourlyPresentation(zoneId: ZoneId): HomeHourlyPresentation =
    HomeHourlyPresentation(
        time = HOUR_FORMAT.format(time.atZone(zoneId)),
        condition = condition.displayName(),
        conditionIdentity = condition,
        temperature = temperatureC.formatFahrenheit(),
        precipitationProbability = precipitationProbabilityPercent?.let { "$it%" },
    )

private fun DailyForecast.toDailyPresentation(zoneId: ZoneId): HomeDailyPresentation =
    HomeDailyPresentation(
        date = DAY_FORMAT.format(LocalDate.ofEpochDay(dateEpochDay)),
        condition = condition.displayName(),
        conditionIdentity = condition,
        precipitationProbability = precipitationProbabilityPercent?.let { "$it%" },
        high = highC?.let { "High ${it.formatFahrenheit()}" } ?: "High unavailable",
        low = lowC?.let { "Low ${it.formatFahrenheit()}" } ?: "Low unavailable",
        highC = highC,
        lowC = lowC,
        sunrise = sunrise?.formatLocalTime(zoneId),
        sunset = sunset?.formatLocalTime(zoneId),
    )

private fun CurrentConditions.toMetricRows(): List<HomeMetricPresentation> = buildList {
    add(HomeMetricPresentation("Feels like", apparentTemperatureC.formatFahrenheit()))
    humidityPercent?.let { add(HomeMetricPresentation("Humidity", "$it%")) }
    wind?.toMetricText()?.let { add(HomeMetricPresentation("Wind", it)) }
    pressureHpa?.let { add(HomeMetricPresentation("Pressure", "${it.roundToInt()} hPa")) }
    visibilityMeters?.let { add(HomeMetricPresentation("Visibility", it.formatVisibility())) }
    dewPointC?.let { add(HomeMetricPresentation("Dew point", it.formatFahrenheit())) }
    cloudCoverPercent?.let { add(HomeMetricPresentation("Cloud cover", "$it%")) }
    precipitationMm?.let { add(HomeMetricPresentation("Precipitation", it.formatMillimeters())) }
}

private fun Wind.toMetricText(): String? {
    val speed = speedMetersPerSecond?.let { "${(it * 3.6).roundToInt()} km/h" }
    val gust = gustMetersPerSecond?.let { "gust ${(it * 3.6).roundToInt()} km/h" }
    val direction = directionDegrees?.let { "${it.roundToInt()} deg" }
    return listOfNotNull(speed, gust, direction).takeIf { it.isNotEmpty() }?.joinToString(", ")
}

private fun DailyForecast.toSunPresentation(zoneId: ZoneId): HomeSunPresentation =
    HomeSunPresentation(
        sunrise = sunrise?.formatLocalTime(zoneId) ?: UNAVAILABLE,
        sunset = sunset?.formatLocalTime(zoneId) ?: UNAVAILABLE,
    )

private fun WeatherAlert.toAlertPresentation(zoneId: ZoneId): HomeAlertPresentation =
    HomeAlertPresentation(
        event = event,
        headline = headline ?: event,
        severity = severity.name.lowercase().replaceFirstChar { it.uppercase() },
        issuer = issuer,
        effective = effective?.let { "Effective ${it.formatLocalTime(zoneId)}" },
        expires = expires?.let { "Expires ${it.formatLocalTime(zoneId)}" },
    )

private fun List<HourlyForecast>.nearTermPrecipitationSummary(): String? {
    val nearTerm = take(6)
    val probabilities = nearTerm.mapNotNull { it.precipitationProbabilityPercent }
    val amounts = nearTerm.mapNotNull { it.precipitationMm }
    if (probabilities.isEmpty() && amounts.isEmpty()) return null

    val parts = buildList {
        probabilities.maxOrNull()?.let { add("Up to $it% precipitation chance in the next 6 hours") }
        if (amounts.isNotEmpty()) add("${amounts.sum().formatMillimeters()} possible in the next 6 hours")
    }
    return parts.joinToString("; ")
}

private fun WeatherBundle.mostRelevantProvenance(): DataProvenance? =
    current?.provenance
        ?: hourly.firstOrNull()?.provenance
        ?: daily.firstOrNull()?.provenance
        ?: alerts.firstOrNull()?.provenance
        ?: airQuality?.provenance

private fun WeatherBundle.bundleFallbackSource(zoneId: ZoneId): HomeSourcePresentation =
    HomeSourcePresentation(
        sourceName = "Source unavailable",
        dataType = "Data type unavailable",
        fetchedAt = "Fetched ${fetchedAt.formatFetched(zoneId)}",
        issuedAt = null,
        license = null,
    )

private fun DataProvenance.toSourcePresentation(zoneId: ZoneId): HomeSourcePresentation =
    HomeSourcePresentation(
        sourceName = sourceName,
        dataType = type.displayLabel(),
        fetchedAt = "Fetched ${fetchedAt.formatFetched(zoneId)}",
        issuedAt = issuedAt?.let { "Issued ${it.formatFetched(zoneId)}" },
        license = licenseId,
    )

private fun Instant.formatLocalTime(zoneId: ZoneId): String = TIME_FORMAT.format(atZone(zoneId))

private fun Instant.formatFetched(zoneId: ZoneId): String = FETCHED_FORMAT.format(atZone(zoneId))

private fun Double?.formatFahrenheit(): String = this?.let { "${((it * 9.0 / 5.0) + 32.0).roundToInt()} deg F" } ?: UNAVAILABLE

private fun Double.formatMillimeters(): String = String.format(Locale.US, "%.1f mm", this)

private fun Double.formatVisibility(): String =
    if (this >= 1000.0) {
        String.format(Locale.US, "%.1f km", this / 1000.0)
    } else {
        "${roundToInt()} m"
    }

private fun DataType.displayLabel(): String = when (this) {
    DataType.OBSERVATION -> "Observation"
    DataType.MODEL_ESTIMATE -> "Model estimate"
    DataType.FORECAST -> "Forecast"
    DataType.OFFICIAL_ALERT -> "Official alert"
    DataType.DERIVED -> "Derived"
}

private fun WeatherCondition.displayName(): String = when (this) {
    WeatherCondition.CLEAR -> "Clear"
    WeatherCondition.MOSTLY_CLEAR -> "Mostly clear"
    WeatherCondition.PARTLY_CLOUDY -> "Partly cloudy"
    WeatherCondition.CLOUDY -> "Cloudy"
    WeatherCondition.FOG -> "Fog"
    WeatherCondition.DRIZZLE -> "Drizzle"
    WeatherCondition.FREEZING_DRIZZLE -> "Freezing drizzle"
    WeatherCondition.RAIN -> "Rain"
    WeatherCondition.FREEZING_RAIN -> "Freezing rain"
    WeatherCondition.RAIN_SHOWERS -> "Rain showers"
    WeatherCondition.SNOW -> "Snow"
    WeatherCondition.SNOW_SHOWERS -> "Snow showers"
    WeatherCondition.SLEET -> "Sleet"
    WeatherCondition.HAIL -> "Hail"
    WeatherCondition.THUNDERSTORM -> "Thunderstorm"
    WeatherCondition.THUNDERSTORM_HAIL -> "Thunderstorm with hail"
    WeatherCondition.UNKNOWN -> "Unknown"
}

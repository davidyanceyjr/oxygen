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
import com.oxygen.weather.core.model.PrecipitationUnit
import com.oxygen.weather.core.model.PressureUnit
import com.oxygen.weather.core.model.ResolvedUnitPreference
import com.oxygen.weather.core.model.TemperatureUnit
import com.oxygen.weather.core.model.UnitPreference
import com.oxygen.weather.core.model.VisibilityUnit
import com.oxygen.weather.core.model.WindSpeedUnit
import com.oxygen.weather.core.model.resolve
import com.oxygen.weather.core.provider.AlertLookupStatus
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val UNAVAILABLE = "Unavailable"
private val DEFAULT_HOME_UNIT_PREFERENCE = UnitPreference.Custom(
    temperature = TemperatureUnit.FAHRENHEIT,
    windSpeed = WindSpeedUnit.KILOMETERS_PER_HOUR,
    pressure = PressureUnit.HECTOPASCALS,
    precipitation = PrecipitationUnit.MILLIMETERS,
    visibility = VisibilityUnit.KILOMETERS,
)
private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h a", Locale.US)
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)
private val FETCHED_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a z", Locale.US)

fun WeatherBundle.toHomeSuccessPresentation(
    selectedLocation: WeatherLocation,
    unitPreference: UnitPreference? = null,
    alertStatus: AlertLookupStatus? = null,
): HomeSuccessPresentation {
    val resolvedPreference = unitPreference ?: DEFAULT_HOME_UNIT_PREFERENCE
    val units = resolvedPreference.resolve()
    val compatibilityDefault = unitPreference == null || resolvedPreference == DEFAULT_HOME_UNIT_PREFERENCE
    val zoneId = selectedLocation.zoneId
    val heroRange = daily.firstOrNull { it.highC != null || it.lowC != null }?.toHeroRangePresentation(units)
    val currentPresentation = current?.toCurrentPresentation(
        zoneId = zoneId,
        heroRange = heroRange,
        units = units,
    )
    val hourlyRows = hourly.take(12).map { it.toHourlyPresentation(zoneId, units) }
    val dailyRows = daily.take(10).map { it.toDailyPresentation(zoneId, units) }
    val metricRows = current?.toMetricRows(units, compatibilityDefault).orEmpty()
    val sun = daily.firstOrNull { it.sunrise != null || it.sunset != null }?.toSunPresentation(zoneId)
    val provenance = mostRelevantProvenance()?.toSourcePresentation(zoneId) ?: bundleFallbackSource(zoneId)
    val effectiveAlertStatus = alertStatus ?: alerts.legacyAlertStatus(selectedLocation)
    val alertSummary = effectiveAlertStatus?.toHomeAlertSummary(alerts, zoneId)
    val alertDetails = effectiveAlertStatus?.toHomeAlertDetails(alerts, zoneId).orEmpty()
    val precipitationSummary = hourly.nearTermPrecipitationSummary(units)
    val returnedDataUnavailable = current == null && hourly.isEmpty() && daily.isEmpty()

    return HomeSuccessPresentation(
        locationName = selectedLocation.displayName,
        locationSubtitle = selectedLocation.forecastSubtitle(),
        alerts = alerts,
        alertSummary = alertSummary,
        alertDetails = alertDetails,
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
            add(HomeSuccessSection.Current)
            if (alertSummary != null) add(HomeSuccessSection.Alerts)
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
    val alerts: List<WeatherAlert>,
    val alertSummary: HomeAlertSummaryPresentation?,
    val alertDetails: List<HomeAlertDetailPresentation>,
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

data class HomeAlertSummaryPresentation(
    val event: String,
    val severity: String,
    val issuer: String,
    val expires: String,
    val activeAlertCount: Int,
    val sourceCheckedAt: String,
    val attribution: String,
    val sourceLink: String,
    val sourceLinkLabel: String,
    val detailActionLabel: String,
    val detailActionContentDescription: String,
)

data class HomeAlertDetailPresentation(
    val id: String,
    val event: String,
    val headline: String?,
    val severity: String,
    val urgency: String,
    val certainty: String,
    val issuer: String,
    val effective: String,
    val expires: String,
    val sent: String?,
    val onset: String?,
    val ends: String?,
    val affectedArea: String,
    val description: String,
    val instruction: String,
    val sourceCheckedAt: String,
    val attribution: String,
    val sourceLink: String,
    val sourceLinkLabel: String,
)

data class HomeCurrentPresentation(
    val temperature: String,
    val temperatureC: Double?,
    val condition: String,
    val conditionIdentity: WeatherCondition,
    val apparentTemperature: String,
    val apparentTemperatureC: Double?,
    val highTemperature: String?,
    val highTemperatureC: Double?,
    val lowTemperature: String?,
    val lowTemperatureC: Double?,
    val updatedTime: String,
    val dataTypeLabel: String,
)

data class HomeHourlyPresentation(
    val time: String,
    val condition: String,
    val conditionIdentity: WeatherCondition,
    val temperature: String,
    val temperatureC: Double?,
    val precipitationProbability: String?,
    val precipitationProbabilityPercent: Int?,
)

data class HomeDailyPresentation(
    val date: String,
    val condition: String,
    val conditionIdentity: WeatherCondition,
    val precipitationProbability: String?,
    val precipitationProbabilityPercent: Int?,
    val high: String,
    val low: String,
    val highC: Double?,
    val lowC: Double?,
    val sunrise: String?,
    val sunset: String?,
)

data class HomeMetricPresentation(
    val identity: HomeMetricIdentity,
    val label: String,
    val value: String,
    val numericValues: HomeMetricNumericValues,
)

sealed interface HomeMetricNumericValues {
    data class TemperatureC(val temperatureC: Double?) : HomeMetricNumericValues

    data class Percent(val percent: Int?) : HomeMetricNumericValues

    data class Wind(
        val speedMetersPerSecond: Double?,
        val gustMetersPerSecond: Double?,
        val directionDegrees: Double?,
    ) : HomeMetricNumericValues

    data class PressureHpa(val pressureHpa: Double?) : HomeMetricNumericValues

    data class DistanceMeters(val distanceMeters: Double?) : HomeMetricNumericValues

    data class PrecipitationMm(val precipitationMm: Double?) : HomeMetricNumericValues
}

enum class HomeMetricIdentity {
    ApparentTemperature,
    Humidity,
    Wind,
    Pressure,
    Visibility,
    DewPoint,
    CloudCover,
    Precipitation,
}

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
    val highTemperatureC: Double?,
    val lowTemperature: String?,
    val lowTemperatureC: Double?,
)

private fun CurrentConditions.toCurrentPresentation(
    zoneId: ZoneId,
    heroRange: HomeHeroRangePresentation?,
    units: ResolvedUnitPreference,
): HomeCurrentPresentation =
    HomeCurrentPresentation(
        temperature = temperatureC.formatTemperature(units.temperature),
        temperatureC = temperatureC,
        condition = condition.displayName(),
        conditionIdentity = condition,
        apparentTemperature = apparentTemperatureC?.let { "Feels like ${it.formatTemperature(units.temperature)}" } ?: "Feels like unavailable",
        apparentTemperatureC = apparentTemperatureC,
        highTemperature = heroRange?.highTemperature,
        highTemperatureC = heroRange?.highTemperatureC,
        lowTemperature = heroRange?.lowTemperature,
        lowTemperatureC = heroRange?.lowTemperatureC,
        updatedTime = "Updated ${time.formatLocalTime(zoneId)}",
        dataTypeLabel = provenance.type.displayLabel(),
    )

private fun DailyForecast.toHeroRangePresentation(units: ResolvedUnitPreference): HomeHeroRangePresentation =
    HomeHeroRangePresentation(
        highTemperature = highC?.let { "H ${it.formatTemperature(units.temperature)}" },
        highTemperatureC = highC,
        lowTemperature = lowC?.let { "L ${it.formatTemperature(units.temperature)}" },
        lowTemperatureC = lowC,
    )

private fun HourlyForecast.toHourlyPresentation(
    zoneId: ZoneId,
    units: ResolvedUnitPreference,
): HomeHourlyPresentation =
    HomeHourlyPresentation(
        time = HOUR_FORMAT.format(time.atZone(zoneId)),
        condition = condition.displayName(),
        conditionIdentity = condition,
        temperature = temperatureC.formatTemperature(units.temperature),
        temperatureC = temperatureC,
        precipitationProbability = precipitationProbabilityPercent?.let { "$it%" },
        precipitationProbabilityPercent = precipitationProbabilityPercent,
    )

private fun DailyForecast.toDailyPresentation(
    zoneId: ZoneId,
    units: ResolvedUnitPreference,
): HomeDailyPresentation =
    HomeDailyPresentation(
        date = DAY_FORMAT.format(LocalDate.ofEpochDay(dateEpochDay)),
        condition = condition.displayName(),
        conditionIdentity = condition,
        precipitationProbability = precipitationProbabilityPercent?.let { "$it%" },
        precipitationProbabilityPercent = precipitationProbabilityPercent,
        high = highC?.let { "High ${it.formatTemperature(units.temperature)}" } ?: "High unavailable",
        low = lowC?.let { "Low ${it.formatTemperature(units.temperature)}" } ?: "Low unavailable",
        highC = highC,
        lowC = lowC,
        sunrise = sunrise?.formatLocalTime(zoneId),
        sunset = sunset?.formatLocalTime(zoneId),
    )

private fun CurrentConditions.toMetricRows(
    units: ResolvedUnitPreference,
    compatibilityDefault: Boolean,
): List<HomeMetricPresentation> = buildList {
    add(
        HomeMetricPresentation(
            identity = HomeMetricIdentity.ApparentTemperature,
            label = "Feels like",
            value = apparentTemperatureC.formatTemperature(units.temperature),
            numericValues = HomeMetricNumericValues.TemperatureC(apparentTemperatureC),
        ),
    )
    humidityPercent?.let {
        add(
            HomeMetricPresentation(
                identity = HomeMetricIdentity.Humidity,
                label = "Humidity",
                value = "$it%",
                numericValues = HomeMetricNumericValues.Percent(it),
            ),
        )
    }
    wind?.let { wind ->
        wind.toMetricText(units.windSpeed)?.let { value ->
            add(
                HomeMetricPresentation(
                    identity = HomeMetricIdentity.Wind,
                    label = "Wind",
                    value = value,
                    numericValues = HomeMetricNumericValues.Wind(
                        speedMetersPerSecond = wind.speedMetersPerSecond,
                        gustMetersPerSecond = wind.gustMetersPerSecond,
                        directionDegrees = wind.directionDegrees,
                    ),
                ),
            )
        }
    }
    pressureHpa?.let {
        add(
            HomeMetricPresentation(
                identity = HomeMetricIdentity.Pressure,
                label = "Pressure",
                value = it.formatPressure(units.pressure),
                numericValues = HomeMetricNumericValues.PressureHpa(it),
            ),
        )
    }
    visibilityMeters?.let {
        add(
            HomeMetricPresentation(
                identity = HomeMetricIdentity.Visibility,
                label = "Visibility",
                value = it.formatVisibility(units.visibility, compatibilityDefault),
                numericValues = HomeMetricNumericValues.DistanceMeters(it),
            ),
        )
    }
    dewPointC?.let {
        add(
            HomeMetricPresentation(
                identity = HomeMetricIdentity.DewPoint,
                label = "Dew point",
                value = it.formatTemperature(units.temperature),
                numericValues = HomeMetricNumericValues.TemperatureC(it),
            ),
        )
    }
    cloudCoverPercent?.let {
        add(
            HomeMetricPresentation(
                identity = HomeMetricIdentity.CloudCover,
                label = "Cloud cover",
                value = "$it%",
                numericValues = HomeMetricNumericValues.Percent(it),
            ),
        )
    }
    precipitationMm?.let {
        add(
            HomeMetricPresentation(
                identity = HomeMetricIdentity.Precipitation,
                label = "Precipitation",
                value = it.formatPrecipitation(units.precipitation),
                numericValues = HomeMetricNumericValues.PrecipitationMm(it),
            ),
        )
    }
}

private fun Wind.toMetricText(unit: WindSpeedUnit): String? {
    val speed = speedMetersPerSecond?.let { "${it.convertWindSpeed(unit).whole()} ${unit.symbol}" }
    val gust = gustMetersPerSecond?.let { "gust ${it.convertWindSpeed(unit).whole()} ${unit.symbol}" }
    val direction = directionDegrees?.let { "${it.whole()} deg" }
    return listOfNotNull(speed, gust, direction).takeIf { it.isNotEmpty() }?.joinToString(", ")
}

private fun DailyForecast.toSunPresentation(zoneId: ZoneId): HomeSunPresentation =
    HomeSunPresentation(
        sunrise = sunrise?.formatLocalTime(zoneId) ?: UNAVAILABLE,
        sunset = sunset?.formatLocalTime(zoneId) ?: UNAVAILABLE,
    )

private fun AlertLookupStatus.toHomeAlertSummary(
    alerts: List<WeatherAlert>,
    zoneId: ZoneId,
): HomeAlertSummaryPresentation? {
    if (this !is AlertLookupStatus.Available || alerts.isEmpty()) return null
    val first = alerts.first()
    return HomeAlertSummaryPresentation(
        event = first.event,
        severity = first.severity.name.lowercase().replaceFirstChar { it.uppercase() },
        issuer = first.issuer,
        expires = first.expires?.let { "Expires ${it.formatLocalTime(zoneId)}" } ?: "Expires unavailable",
        activeAlertCount = alerts.size,
        sourceCheckedAt = "Alert source checked ${metadata.fetchedAt.formatFetched(zoneId)}",
        attribution = "Official alerts from NOAA/National Weather Service",
        sourceLink = first.web.validAlertSourceUrl(),
        sourceLinkLabel = "Open official NOAA/National Weather Service alert source",
        detailActionLabel = "View alert details",
        detailActionContentDescription = "View official alert details",
    )
}

private fun AlertLookupStatus.toHomeAlertDetails(
    alerts: List<WeatherAlert>,
    zoneId: ZoneId,
): List<HomeAlertDetailPresentation>? {
    if (this !is AlertLookupStatus.Available || alerts.isEmpty()) return null
    require(alerts.map { it.id }.toSet().size == alerts.size) {
        "Available alerts must have unique IDs"
    }
    return alerts.map { alert ->
        HomeAlertDetailPresentation(
            id = alert.id,
            event = alert.event,
            headline = alert.headline,
            severity = alert.severity.readableAlertLabel(),
            urgency = alert.urgency.readableAlertLabel(),
            certainty = alert.certainty.readableAlertLabel(),
            issuer = alert.issuer,
            effective = alert.effective?.formatFetched(zoneId) ?: UNAVAILABLE,
            expires = alert.expires?.formatFetched(zoneId) ?: UNAVAILABLE,
            sent = alert.sent?.formatFetched(zoneId),
            onset = alert.onset?.formatFetched(zoneId),
            ends = alert.ends?.formatFetched(zoneId),
            affectedArea = alert.affectedArea?.areaDescription?.takeIf { it.isNotBlank() } ?: UNAVAILABLE,
            description = alert.description?.takeIf { it.isNotBlank() } ?: UNAVAILABLE,
            instruction = alert.instruction?.takeIf { it.isNotBlank() } ?: UNAVAILABLE,
            sourceCheckedAt = "Alert source checked ${metadata.fetchedAt.formatFetched(zoneId)}",
            attribution = "Official alerts from NOAA/National Weather Service",
            sourceLink = alert.web.validAlertSourceUrl(),
            sourceLinkLabel = "Open official NOAA/National Weather Service alert source for ${alert.event}",
        )
    }
}

private fun Enum<*>.readableAlertLabel(): String =
    name.lowercase(Locale.US).replace('_', ' ').replaceFirstChar { it.uppercase() }

private fun List<WeatherAlert>.legacyAlertStatus(selectedLocation: WeatherLocation): AlertLookupStatus? {
    val first = firstOrNull() ?: return null
    val provenance = first.provenance
    return AlertLookupStatus.Available(
        com.oxygen.weather.core.provider.AlertSuccessMetadata(
            requestPoint = selectedLocation.point,
            providerId = provenance.providerId,
            fetchedAt = provenance.fetchedAt,
        ),
    )
}

private fun String?.validAlertSourceUrl(): String {
    val candidate = this?.trim().orEmpty()
    return try {
        val uri = URI(candidate)
        if (uri.isAbsolute && uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()) {
            uri.toString()
        } else {
            OFFICIAL_ALERT_SOURCE_FALLBACK
        }
    } catch (_: Exception) {
        OFFICIAL_ALERT_SOURCE_FALLBACK
    }
}

private const val OFFICIAL_ALERT_SOURCE_FALLBACK = "https://www.weather.gov/"

private fun List<HourlyForecast>.nearTermPrecipitationSummary(units: ResolvedUnitPreference): String? {
    val nearTerm = take(6)
    val probabilities = nearTerm.mapNotNull { it.precipitationProbabilityPercent }
    val amounts = nearTerm.mapNotNull { it.precipitationMm }
    if (probabilities.isEmpty() && amounts.isEmpty()) return null

    val parts = buildList {
        probabilities.maxOrNull()?.let { add("Up to $it% precipitation chance in the next 6 hours") }
        if (amounts.isNotEmpty()) add("${amounts.sum().formatPrecipitation(units.precipitation)} possible in the next 6 hours")
    }
    return parts.joinToString("; ")
}

private fun WeatherBundle.mostRelevantProvenance(): DataProvenance? =
    current?.provenance
        ?: hourly.firstOrNull()?.provenance
        ?: daily.firstOrNull()?.provenance
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

private fun Double?.formatTemperature(unit: TemperatureUnit): String = this?.let {
    val value = when (unit) {
        TemperatureUnit.CELSIUS -> it
        TemperatureUnit.FAHRENHEIT -> it * 9.0 / 5.0 + 32.0
    }
    "${value.whole()} ${unit.symbol}"
} ?: UNAVAILABLE

private val TemperatureUnit.symbol: String
    get() = when (this) {
        TemperatureUnit.CELSIUS -> "deg C"
        TemperatureUnit.FAHRENHEIT -> "deg F"
    }

private fun Double.convertWindSpeed(unit: WindSpeedUnit): Double = when (unit) {
    WindSpeedUnit.KILOMETERS_PER_HOUR -> this * 3.6
    WindSpeedUnit.MILES_PER_HOUR -> this * 2.2369362920544
    WindSpeedUnit.METERS_PER_SECOND -> this
    WindSpeedUnit.KNOTS -> this * 1.9438444924406
}

private val WindSpeedUnit.symbol: String
    get() = when (this) {
        WindSpeedUnit.KILOMETERS_PER_HOUR -> "km/h"
        WindSpeedUnit.MILES_PER_HOUR -> "mph"
        WindSpeedUnit.METERS_PER_SECOND -> "m/s"
        WindSpeedUnit.KNOTS -> "kn"
    }

private fun Double.formatPressure(unit: PressureUnit): String = when (unit) {
    PressureUnit.HECTOPASCALS -> "${whole()} hPa"
    PressureUnit.INCHES_OF_MERCURY -> "${(this * 0.0295299830714).decimal(2)} inHg"
    PressureUnit.MILLIMETERS_OF_MERCURY -> "${(this * 0.750061683).whole()} mmHg"
}

private fun Double.formatPrecipitation(unit: PrecipitationUnit): String = when (unit) {
    PrecipitationUnit.MILLIMETERS -> "${decimal(1)} mm"
    PrecipitationUnit.INCHES -> "${(this / 25.4).decimal(2)} in"
}

private fun Double.formatVisibility(unit: VisibilityUnit, compatibilityDefault: Boolean): String = when {
    compatibilityDefault && unit == VisibilityUnit.KILOMETERS && this < 1000.0 -> "${whole()} m"
    unit == VisibilityUnit.KILOMETERS -> "${(this / 1000.0).decimal(1)} km"
    else -> "${(this / 1609.344).decimal(1)} mi"
}

private fun Double.whole(): String = decimal(0)

private fun Double.decimal(scale: Int): String = BigDecimal.valueOf(this)
    .setScale(scale, RoundingMode.HALF_UP)
    .toPlainString()

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

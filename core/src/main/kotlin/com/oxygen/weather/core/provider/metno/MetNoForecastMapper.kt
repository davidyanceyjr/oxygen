package com.oxygen.weather.core.provider.metno

import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.model.Wind
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import kotlin.math.roundToInt

object MetNoForecastMapper {
    private const val PROVIDER_ID = "met-norway"
    private const val SOURCE_NAME = "MET Norway"
    private const val LICENSE_ID = "NLOD-2.0 OR CC-BY-4.0"

    fun map(
        location: WeatherLocation,
        response: MetNoForecastResponse,
        fetchedAt: Instant,
    ): WeatherBundle {
        if (response.timeseries.isEmpty()) {
            throw MetNoMapperException.NoUsableTimeSteps
        }

        validateMappedUnits(response)

        val issuedAt = parseInstant("properties.meta.updated_at", response.meta.updatedAt)
        val currentProvenance = provenance(issuedAt, fetchedAt, DataType.MODEL_ESTIMATE)
        val forecastProvenance = provenance(issuedAt, fetchedAt, DataType.FORECAST)
        val timesteps = response.timeseries.map { timestep ->
            ParsedTimeStep(
                time = parseInstant("properties.timeseries.time", timestep.time),
                timestep = timestep,
            )
        }.sortedBy { it.time }

        return WeatherBundle(
            location = location,
            current = mapCurrent(timesteps.first(), currentProvenance),
            hourly = timesteps.map { mapHourly(it, forecastProvenance) },
            daily = mapDaily(timesteps, location, forecastProvenance),
            fetchedAt = fetchedAt,
        )
    }

    fun mapSymbolCode(symbolCode: String?): WeatherCondition {
        val family = symbolCode?.normalizedSymbolFamily() ?: return WeatherCondition.UNKNOWN
        if (family.isBlank()) return WeatherCondition.UNKNOWN

        return when {
            family == "clearsky" -> WeatherCondition.CLEAR
            family == "fair" -> WeatherCondition.MOSTLY_CLEAR
            family == "partlycloudy" -> WeatherCondition.PARTLY_CLOUDY
            family == "cloudy" -> WeatherCondition.CLOUDY
            family == "fog" -> WeatherCondition.FOG
            family.contains("andthunder") -> WeatherCondition.THUNDERSTORM
            family.startsWith("lightrain") -> WeatherCondition.DRIZZLE
            family.startsWith("rainshowers") || family.startsWith("heavyrainshowers") -> WeatherCondition.RAIN_SHOWERS
            family.startsWith("rain") || family.startsWith("heavyrain") -> WeatherCondition.RAIN
            family.startsWith("sleet") || family.startsWith("lightsleet") || family.startsWith("heavysleet") -> WeatherCondition.SLEET
            family.startsWith("snowshowers") || family.startsWith("lightsnowshowers") || family.startsWith("heavysnowshowers") -> WeatherCondition.SNOW_SHOWERS
            family.startsWith("snow") || family.startsWith("lightsnow") || family.startsWith("heavysnow") -> WeatherCondition.SNOW
            else -> WeatherCondition.UNKNOWN
        }
    }

    private fun mapCurrent(
        parsed: ParsedTimeStep,
        provenance: DataProvenance,
    ): CurrentConditions {
        val details = parsed.timestep.instant.details
        val next1Details = parsed.timestep.next1Hours?.details
        return CurrentConditions(
            time = parsed.time,
            temperatureC = details.airTemperature,
            apparentTemperatureC = null,
            dewPointC = details.dewPointTemperature,
            humidityPercent = details.relativeHumidity?.roundToInt(),
            pressureHpa = details.airPressureAtSeaLevel,
            visibilityMeters = null,
            cloudCoverPercent = details.cloudAreaFraction?.roundToInt(),
            wind = wind(details),
            precipitationMm = next1Details?.precipitationAmount,
            condition = mapSymbolCode(parsed.timestep.next1Hours?.summary?.symbolCode),
            provenance = provenance,
        )
    }

    private fun mapHourly(
        parsed: ParsedTimeStep,
        provenance: DataProvenance,
    ): HourlyForecast {
        val next1Details = parsed.timestep.next1Hours?.details
        return HourlyForecast(
            time = parsed.time,
            temperatureC = parsed.timestep.instant.details.airTemperature,
            precipitationProbabilityPercent = next1Details?.probabilityOfPrecipitation?.roundToInt(),
            precipitationMm = next1Details?.precipitationAmount,
            condition = mapSymbolCode(parsed.timestep.next1Hours?.summary?.symbolCode),
            provenance = provenance,
        )
    }

    private fun mapDaily(
        timesteps: List<ParsedTimeStep>,
        location: WeatherLocation,
        provenance: DataProvenance,
    ): List<DailyForecast> =
        timesteps
            .groupBy { LocalDate.ofInstant(it.time, location.zoneId) }
            .toSortedMap()
            .map { (date, daySteps) ->
                val highValues = daySteps.mapNotNull { it.periodDetails().mapNotNull(MetNoPeriodDetails::airTemperatureMax).maxOrNull() }
                val lowValues = daySteps.mapNotNull { it.periodDetails().mapNotNull(MetNoPeriodDetails::airTemperatureMin).minOrNull() }
                DailyForecast(
                    dateEpochDay = date.toEpochDay(),
                    highC = highValues.maxOrNull()
                        ?: daySteps.mapNotNull { it.timestep.instant.details.airTemperature }.maxOrNull(),
                    lowC = lowValues.minOrNull()
                        ?: daySteps.mapNotNull { it.timestep.instant.details.airTemperature }.minOrNull(),
                    precipitationProbabilityPercent = daySteps
                        .flatMap { it.periodDetails() }
                        .mapNotNull(MetNoPeriodDetails::probabilityOfPrecipitation)
                        .maxOrNull()
                        ?.roundToInt(),
                    condition = daySteps.firstMappedDailyCondition(),
                    sunrise = null,
                    sunset = null,
                    provenance = provenance,
                )
            }

    private fun List<ParsedTimeStep>.firstMappedDailyCondition(): WeatherCondition {
        val symbolsByPeriodPriority = listOf(
            mapNotNull { it.timestep.next12Hours?.summary?.symbolCode },
            mapNotNull { it.timestep.next6Hours?.summary?.symbolCode },
            mapNotNull { it.timestep.next1Hours?.summary?.symbolCode },
        )
        for (symbols in symbolsByPeriodPriority) {
            symbols.firstOrNull()?.let { return mapSymbolCode(it) }
        }
        return WeatherCondition.UNKNOWN
    }

    private fun ParsedTimeStep.periodDetails(): List<MetNoPeriodDetails> =
        listOfNotNull(
            timestep.next12Hours?.details,
            timestep.next6Hours?.details,
            timestep.next1Hours?.details,
        )

    private fun wind(details: MetNoInstantDetails): Wind? {
        if (
            details.windSpeed == null &&
            details.windSpeedOfGust == null &&
            details.windFromDirection == null
        ) {
            return null
        }

        return Wind(
            speedMetersPerSecond = details.windSpeed,
            gustMetersPerSecond = details.windSpeedOfGust,
            directionDegrees = details.windFromDirection,
        )
    }

    private fun validateMappedUnits(response: MetNoForecastResponse) {
        val checks = buildList {
            add(UnitCheck("air_temperature", "celsius") { instant.airTemperature })
            add(UnitCheck("air_pressure_at_sea_level", "hPa") { instant.airPressureAtSeaLevel })
            add(UnitCheck("cloud_area_fraction", "%") { instant.cloudAreaFraction })
            add(UnitCheck("dew_point_temperature", "celsius") { instant.dewPointTemperature })
            add(UnitCheck("relative_humidity", "%") { instant.relativeHumidity })
            add(UnitCheck("wind_from_direction", "degrees") { instant.windFromDirection })
            add(UnitCheck("wind_speed", "m/s") { instant.windSpeed })
            add(UnitCheck("wind_speed_of_gust", "m/s") { instant.windSpeedOfGust })
            add(UnitCheck("precipitation_amount", "mm") { periods.mapNotNull(MetNoPeriodDetails::precipitationAmount).firstOrNull() })
            add(UnitCheck("probability_of_precipitation", "%") { periods.mapNotNull(MetNoPeriodDetails::probabilityOfPrecipitation).firstOrNull() })
            add(UnitCheck("air_temperature_min", "celsius") { periods.mapNotNull(MetNoPeriodDetails::airTemperatureMin).firstOrNull() })
            add(UnitCheck("air_temperature_max", "celsius") { periods.mapNotNull(MetNoPeriodDetails::airTemperatureMax).firstOrNull() })
        }

        checks.forEach { check ->
            if (response.timeseries.any { timestep -> check.value(ValuesForUnitCheck(timestep)) != null }) {
                val actualUnit = response.meta.units[check.field]
                if (actualUnit != check.expectedUnit) {
                    throw MetNoMapperException.UnexpectedUnit(
                        field = check.field,
                        expectedUnit = check.expectedUnit,
                        actualUnit = actualUnit,
                    )
                }
            }
        }
    }

    private fun parseInstant(field: String, value: String): Instant =
        try {
            Instant.parse(value)
        } catch (error: DateTimeException) {
            throw MetNoMapperException.InvalidTimestamp(field, value)
        }

    private fun provenance(
        issuedAt: Instant,
        fetchedAt: Instant,
        type: DataType,
    ): DataProvenance =
        DataProvenance(
            providerId = PROVIDER_ID,
            sourceName = SOURCE_NAME,
            issuedAt = issuedAt,
            fetchedAt = fetchedAt,
            type = type,
            licenseId = LICENSE_ID,
        )

    private fun String.normalizedSymbolFamily(): String =
        when {
            endsWith("_polartwilight") -> removeSuffix("_polartwilight")
            endsWith("_night") -> removeSuffix("_night")
            endsWith("_day") -> removeSuffix("_day")
            else -> this
        }

    private data class ParsedTimeStep(
        val time: Instant,
        val timestep: MetNoTimeStep,
    )

    private data class UnitCheck(
        val field: String,
        val expectedUnit: String,
        val value: ValuesForUnitCheck.() -> Double?,
    )

    private data class ValuesForUnitCheck(
        val timestep: MetNoTimeStep,
    ) {
        val instant: MetNoInstantDetails = timestep.instant.details
        val periods: List<MetNoPeriodDetails> = listOfNotNull(
            timestep.next1Hours?.details,
            timestep.next6Hours?.details,
            timestep.next12Hours?.details,
        )
    }
}

sealed class MetNoMapperException(message: String) : IllegalArgumentException(message) {
    data class UnexpectedUnit(
        val field: String,
        val expectedUnit: String,
        val actualUnit: String?,
    ) : MetNoMapperException("Unexpected unit for $field: expected $expectedUnit, got ${actualUnit ?: "missing"}")

    data class InvalidTimestamp(
        val field: String,
        val value: String,
    ) : MetNoMapperException("Invalid timestamp for $field: $value")

    data object NoUsableTimeSteps : MetNoMapperException("No usable MET Norway forecast timesteps")
}

package com.oxygen.weather.core.provider.metno

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object MetNoForecastParser {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun parseForecast(body: String): MetNoForecastResponse {
        val root = try {
            json.parseToJsonElement(body).jsonObject
        } catch (error: IllegalArgumentException) {
            throw MetNoParseException.InvalidJson(error.message ?: "Invalid JSON")
        } catch (error: SerializationException) {
            throw MetNoParseException.InvalidJson(error.message ?: "Invalid JSON")
        }

        val properties = root.requiredObject("properties")
        val meta = properties.requiredObject("meta")

        return MetNoForecastResponse(
            type = root.requiredString("type"),
            geometry = parseGeometry(root.requiredObject("geometry")),
            meta = MetNoMeta(
                updatedAt = meta.requiredString("updated_at"),
                units = meta.requiredStringMap("units"),
            ),
            timeseries = properties.requiredArray("timeseries").mapIndexed { index, timeStep ->
                parseTimeStep(index, timeStep.jsonObjectOrInvalid("properties.timeseries[$index]"))
            },
        )
    }

    private fun parseGeometry(geometry: JsonObject): MetNoGeometry {
        val coordinates = geometry.requiredArray("coordinates").mapIndexed { index, value ->
            value.jsonPrimitiveOrNull()?.doubleOrNull
                ?: throw MetNoParseException.InvalidField("geometry.coordinates[$index]", "Expected number")
        }
        if (coordinates.size < 2) {
            throw MetNoParseException.InvalidField("geometry.coordinates", "Expected longitude and latitude")
        }
        return MetNoGeometry(
            type = geometry.optionalString("type"),
            coordinates = coordinates,
        )
    }

    private fun parseTimeStep(index: Int, timeStep: JsonObject): MetNoTimeStep {
        val field = "properties.timeseries[$index]"
        val data = timeStep.requiredObject("$field.data", "data")
        val instant = data.requiredObject("$field.data.instant", "instant")

        return MetNoTimeStep(
            time = timeStep.requiredString("$field.time", "time"),
            instant = MetNoInstant(
                details = parseInstantDetails("$field.data.instant.details", instant.requiredObject("details")),
            ),
            next1Hours = data.optionalObject("next_1_hours")?.let { parsePeriod("$field.data.next_1_hours", it) },
            next6Hours = data.optionalObject("next_6_hours")?.let { parsePeriod("$field.data.next_6_hours", it) },
            next12Hours = data.optionalObject("next_12_hours")?.let { parsePeriod("$field.data.next_12_hours", it) },
        )
    }

    private fun parseInstantDetails(field: String, details: JsonObject): MetNoInstantDetails =
        MetNoInstantDetails(
            airTemperature = details.optionalDouble("$field.air_temperature", "air_temperature"),
            airPressureAtSeaLevel = details.optionalDouble("$field.air_pressure_at_sea_level", "air_pressure_at_sea_level"),
            cloudAreaFraction = details.optionalDouble("$field.cloud_area_fraction", "cloud_area_fraction"),
            cloudAreaFractionHigh = details.optionalDouble("$field.cloud_area_fraction_high", "cloud_area_fraction_high"),
            cloudAreaFractionMedium = details.optionalDouble("$field.cloud_area_fraction_medium", "cloud_area_fraction_medium"),
            cloudAreaFractionLow = details.optionalDouble("$field.cloud_area_fraction_low", "cloud_area_fraction_low"),
            dewPointTemperature = details.optionalDouble("$field.dew_point_temperature", "dew_point_temperature"),
            fogAreaFraction = details.optionalDouble("$field.fog_area_fraction", "fog_area_fraction"),
            relativeHumidity = details.optionalDouble("$field.relative_humidity", "relative_humidity"),
            ultravioletIndexClearSky = details.optionalDouble("$field.ultraviolet_index_clear_sky", "ultraviolet_index_clear_sky"),
            windFromDirection = details.optionalDouble("$field.wind_from_direction", "wind_from_direction"),
            windSpeed = details.optionalDouble("$field.wind_speed", "wind_speed"),
            windSpeedOfGust = details.optionalDouble("$field.wind_speed_of_gust", "wind_speed_of_gust"),
        )

    private fun parsePeriod(field: String, period: JsonObject): MetNoPeriodForecast =
        MetNoPeriodForecast(
            summary = period.optionalObject("summary")?.let {
                MetNoPeriodSummary(symbolCode = it.optionalString("symbol_code"))
            },
            details = period.optionalObject("details")?.let { parsePeriodDetails("$field.details", it) },
        )

    private fun parsePeriodDetails(field: String, details: JsonObject): MetNoPeriodDetails =
        MetNoPeriodDetails(
            precipitationAmount = details.optionalDouble("$field.precipitation_amount", "precipitation_amount"),
            precipitationAmountMin = details.optionalDouble("$field.precipitation_amount_min", "precipitation_amount_min"),
            precipitationAmountMax = details.optionalDouble("$field.precipitation_amount_max", "precipitation_amount_max"),
            probabilityOfPrecipitation = details.optionalDouble("$field.probability_of_precipitation", "probability_of_precipitation"),
            probabilityOfThunder = details.optionalDouble("$field.probability_of_thunder", "probability_of_thunder"),
            airTemperatureMin = details.optionalDouble("$field.air_temperature_min", "air_temperature_min"),
            airTemperatureMax = details.optionalDouble("$field.air_temperature_max", "air_temperature_max"),
            ultravioletIndexClearSkyMax = details.optionalDouble("$field.ultraviolet_index_clear_sky_max", "ultraviolet_index_clear_sky_max"),
        )

    private fun JsonObject.requiredObject(field: String): JsonObject =
        required(field).jsonObjectOrInvalid(field)

    private fun JsonObject.requiredObject(fieldPath: String, field: String): JsonObject =
        required(fieldPath, field).jsonObjectOrInvalid(fieldPath)

    private fun JsonObject.optionalObject(field: String): JsonObject? {
        val element = this[field] ?: return null
        if (element is JsonNull) return null
        return element.jsonObjectOrInvalid(field)
    }

    private fun JsonObject.requiredStringMap(field: String): Map<String, String> =
        requiredObject(field).mapValues { (key, value) ->
            value.jsonPrimitiveOrNull()?.contentOrNull
                ?: throw MetNoParseException.InvalidField("$field.$key", "Expected string")
        }

    private fun JsonObject.requiredString(field: String): String =
        required(field).jsonPrimitiveOrNull()?.contentOrNull
            ?: throw MetNoParseException.InvalidField(field, "Expected string")

    private fun JsonObject.requiredString(fieldPath: String, field: String): String =
        required(fieldPath, field).jsonPrimitiveOrNull()?.contentOrNull
            ?: throw MetNoParseException.InvalidField(fieldPath, "Expected string")

    private fun JsonObject.optionalString(field: String): String? =
        optionalPrimitive(field)?.contentOrNull

    private fun JsonObject.optionalDouble(fieldPath: String, field: String): Double? =
        optionalPrimitive(field)?.doubleOrNull
            ?: if (this[field] == null || this[field] is JsonNull) {
                null
            } else {
                throw MetNoParseException.InvalidField(fieldPath, "Expected number or null")
            }

    private fun JsonObject.requiredArray(field: String): List<JsonElement> =
        required(field).jsonArrayOrInvalid(field)

    private fun JsonObject.required(field: String): JsonElement =
        this[field] ?: throw MetNoParseException.MissingField(field)

    private fun JsonObject.required(fieldPath: String, field: String): JsonElement =
        this[field] ?: throw MetNoParseException.MissingField(fieldPath)

    private fun JsonObject.optionalPrimitive(field: String): JsonPrimitive? {
        val element = this[field] ?: return null
        if (element is JsonNull) return null
        return element.jsonPrimitiveOrNull()
            ?: throw MetNoParseException.InvalidField(field, "Expected primitive or null")
    }

    private fun JsonElement.jsonArrayOrInvalid(field: String): List<JsonElement> =
        try {
            jsonArray
        } catch (error: IllegalArgumentException) {
            throw MetNoParseException.InvalidField(field, "Expected array")
        }

    private fun JsonElement.jsonObjectOrInvalid(field: String): JsonObject =
        try {
            jsonObject
        } catch (error: IllegalArgumentException) {
            throw MetNoParseException.InvalidField(field, "Expected object")
        }

    private fun JsonElement.jsonPrimitiveOrNull(): JsonPrimitive? =
        try {
            jsonPrimitive
        } catch (error: IllegalArgumentException) {
            null
        }
}

sealed class MetNoParseException(message: String) : IllegalArgumentException(message) {
    class InvalidJson(message: String) : MetNoParseException(message)

    class MissingField(
        val fieldPath: String,
    ) : MetNoParseException("Missing required field: $fieldPath")

    class InvalidField(
        val fieldPath: String,
        detail: String,
    ) : MetNoParseException("Invalid field $fieldPath: $detail")
}

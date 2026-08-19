package com.oxygen.weather.core.provider.openmeteo

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object OpenMeteoForecastParser {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun parseForecast(body: String): OpenMeteoForecastResponse {
        val root = try {
            json.parseToJsonElement(body).jsonObject
        } catch (error: IllegalArgumentException) {
            throw OpenMeteoParseException.InvalidJson(error.message ?: "Invalid JSON")
        } catch (error: SerializationException) {
            throw OpenMeteoParseException.InvalidJson(error.message ?: "Invalid JSON")
        }

        throwIfProviderError(root)

        val current = root.requiredObject("current")
        val hourly = root.requiredObject("hourly")
        val daily = root.requiredObject("daily")

        return OpenMeteoForecastResponse(
            latitude = root.requiredDouble("latitude"),
            longitude = root.requiredDouble("longitude"),
            generationTimeMs = root.requiredDouble("generationtime_ms"),
            utcOffsetSeconds = root.requiredInt("utc_offset_seconds"),
            timezone = root.requiredString("timezone"),
            timezoneAbbreviation = root.requiredString("timezone_abbreviation"),
            elevation = root.optionalDouble("elevation"),
            currentUnits = root.requiredStringMap("current_units"),
            current = parseCurrent(current),
            hourlyUnits = root.requiredStringMap("hourly_units"),
            hourly = parseHourly(hourly),
            dailyUnits = root.requiredStringMap("daily_units"),
            daily = parseDaily(daily),
        )
    }

    private fun throwIfProviderError(root: JsonObject) {
        val isError = root["error"]?.jsonPrimitiveOrNull()?.booleanOrNull == true
        if (isError) {
            throw OpenMeteoParseException.ProviderError(
                reason = root["reason"]?.jsonPrimitiveOrNull()?.contentOrNull ?: "Open-Meteo returned an error body",
            )
        }
    }

    private fun parseCurrent(current: JsonObject): OpenMeteoCurrentForecast =
        OpenMeteoCurrentForecast(
            time = current.requiredString("time"),
            interval = current.optionalInt("interval"),
            temperature2m = current.optionalDouble("temperature_2m"),
            relativeHumidity2m = current.optionalInt("relative_humidity_2m"),
            apparentTemperature = current.optionalDouble("apparent_temperature"),
            isDay = current.optionalInt("is_day"),
            precipitation = current.optionalDouble("precipitation"),
            rain = current.optionalDouble("rain"),
            showers = current.optionalDouble("showers"),
            snowfall = current.optionalDouble("snowfall"),
            weatherCode = current.optionalInt("weather_code"),
            cloudCover = current.optionalInt("cloud_cover"),
            pressureMsl = current.optionalDouble("pressure_msl"),
            surfacePressure = current.optionalDouble("surface_pressure"),
            windSpeed10m = current.optionalDouble("wind_speed_10m"),
            windDirection10m = current.optionalInt("wind_direction_10m"),
            windGusts10m = current.optionalDouble("wind_gusts_10m"),
        )

    private fun parseHourly(hourly: JsonObject): OpenMeteoHourlyForecast =
        OpenMeteoHourlyForecast(
            time = hourly.requiredStringList("time"),
            temperature2m = hourly.optionalDoubleList("temperature_2m"),
            relativeHumidity2m = hourly.optionalIntList("relative_humidity_2m"),
            dewPoint2m = hourly.optionalDoubleList("dew_point_2m"),
            apparentTemperature = hourly.optionalDoubleList("apparent_temperature"),
            precipitationProbability = hourly.optionalIntList("precipitation_probability"),
            precipitation = hourly.optionalDoubleList("precipitation"),
            rain = hourly.optionalDoubleList("rain"),
            showers = hourly.optionalDoubleList("showers"),
            snowfall = hourly.optionalDoubleList("snowfall"),
            weatherCode = hourly.optionalIntList("weather_code"),
            pressureMsl = hourly.optionalDoubleList("pressure_msl"),
            surfacePressure = hourly.optionalDoubleList("surface_pressure"),
            cloudCover = hourly.optionalIntList("cloud_cover"),
            visibility = hourly.optionalDoubleList("visibility"),
            uvIndex = hourly.optionalDoubleList("uv_index"),
            isDay = hourly.optionalIntList("is_day"),
            windSpeed10m = hourly.optionalDoubleList("wind_speed_10m"),
            windDirection10m = hourly.optionalIntList("wind_direction_10m"),
            windGusts10m = hourly.optionalDoubleList("wind_gusts_10m"),
        )

    private fun parseDaily(daily: JsonObject): OpenMeteoDailyForecast =
        OpenMeteoDailyForecast(
            time = daily.requiredStringList("time"),
            weatherCode = daily.optionalIntList("weather_code"),
            temperature2mMax = daily.optionalDoubleList("temperature_2m_max"),
            temperature2mMin = daily.optionalDoubleList("temperature_2m_min"),
            apparentTemperatureMax = daily.optionalDoubleList("apparent_temperature_max"),
            apparentTemperatureMin = daily.optionalDoubleList("apparent_temperature_min"),
            uvIndexMax = daily.optionalDoubleList("uv_index_max"),
            sunrise = daily.optionalStringList("sunrise"),
            sunset = daily.optionalStringList("sunset"),
            daylightDuration = daily.optionalDoubleList("daylight_duration"),
            rainSum = daily.optionalDoubleList("rain_sum"),
            showersSum = daily.optionalDoubleList("showers_sum"),
            snowfallSum = daily.optionalDoubleList("snowfall_sum"),
            precipitationSum = daily.optionalDoubleList("precipitation_sum"),
            precipitationHours = daily.optionalDoubleList("precipitation_hours"),
            precipitationProbabilityMax = daily.optionalIntList("precipitation_probability_max"),
            windSpeed10mMax = daily.optionalDoubleList("wind_speed_10m_max"),
            windGusts10mMax = daily.optionalDoubleList("wind_gusts_10m_max"),
            windDirection10mDominant = daily.optionalIntList("wind_direction_10m_dominant"),
        )

    private fun JsonObject.requiredObject(field: String): JsonObject =
        required(field).jsonObjectOrNull() ?: throw OpenMeteoParseException.InvalidField(field, "Expected object")

    private fun JsonObject.requiredStringMap(field: String): Map<String, String> =
        requiredObject(field).mapValues { (key, value) ->
            value.jsonPrimitiveOrNull()?.contentOrNull
                ?: throw OpenMeteoParseException.InvalidField("$field.$key", "Expected string")
        }

    private fun JsonObject.requiredString(field: String): String =
        required(field).jsonPrimitiveOrNull()?.contentOrNull
            ?: throw OpenMeteoParseException.InvalidField(field, "Expected string")

    private fun JsonObject.requiredDouble(field: String): Double =
        required(field).jsonPrimitiveOrNull()?.doubleOrNull
            ?: throw OpenMeteoParseException.InvalidField(field, "Expected number")

    private fun JsonObject.requiredInt(field: String): Int =
        required(field).jsonPrimitiveOrNull()?.intOrNull
            ?: throw OpenMeteoParseException.InvalidField(field, "Expected integer")

    private fun JsonObject.optionalString(field: String): String? =
        optionalPrimitive(field)?.contentOrNull

    private fun JsonObject.optionalDouble(field: String): Double? =
        optionalPrimitive(field)?.doubleOrNull

    private fun JsonObject.optionalInt(field: String): Int? =
        optionalPrimitive(field)?.intOrNull

    private fun JsonObject.requiredStringList(field: String): List<String> =
        required(field).jsonArrayOrNull(field).mapIndexed { index, value ->
            value.jsonPrimitiveOrNull()?.contentOrNull
                ?: throw OpenMeteoParseException.InvalidField("$field[$index]", "Expected string")
        }

    private fun JsonObject.optionalStringList(field: String): List<String?> =
        optionalArray(field).mapIndexed { index, value ->
            if (value is JsonNull) {
                null
            } else {
                value.jsonPrimitiveOrNull()?.contentOrNull
                    ?: throw OpenMeteoParseException.InvalidField("$field[$index]", "Expected string or null")
            }
        }

    private fun JsonObject.optionalDoubleList(field: String): List<Double?> =
        optionalArray(field).mapIndexed { index, value ->
            if (value is JsonNull) {
                null
            } else {
                value.jsonPrimitiveOrNull()?.doubleOrNull
                    ?: throw OpenMeteoParseException.InvalidField("$field[$index]", "Expected number or null")
            }
        }

    private fun JsonObject.optionalIntList(field: String): List<Int?> =
        optionalArray(field).mapIndexed { index, value ->
            if (value is JsonNull) {
                null
            } else {
                value.jsonPrimitiveOrNull()?.intOrNull
                    ?: throw OpenMeteoParseException.InvalidField("$field[$index]", "Expected integer or null")
            }
        }

    private fun JsonObject.required(field: String): JsonElement =
        this[field] ?: throw OpenMeteoParseException.MissingField(field)

    private fun JsonObject.optionalArray(field: String): List<JsonElement> =
        this[field]?.jsonArrayOrNull(field) ?: emptyList()

    private fun JsonObject.optionalPrimitive(field: String): JsonPrimitive? {
        val element = this[field] ?: return null
        if (element is JsonNull) return null
        return element.jsonPrimitiveOrNull()
            ?: throw OpenMeteoParseException.InvalidField(field, "Expected primitive or null")
    }

    private fun JsonElement.jsonArrayOrNull(field: String): List<JsonElement> =
        try {
            jsonArray
        } catch (error: IllegalArgumentException) {
            throw OpenMeteoParseException.InvalidField(field, "Expected array")
        }

    private fun JsonElement.jsonObjectOrNull(): JsonObject? =
        try {
            jsonObject
        } catch (error: IllegalArgumentException) {
            null
        }

    private fun JsonElement.jsonPrimitiveOrNull(): JsonPrimitive? =
        try {
            jsonPrimitive
        } catch (error: IllegalArgumentException) {
            null
        }
}

sealed class OpenMeteoParseException(message: String) : IllegalArgumentException(message) {
    class InvalidJson(message: String) : OpenMeteoParseException(message)

    class ProviderError(
        val reason: String,
    ) : OpenMeteoParseException(reason)

    class MissingField(
        val fieldPath: String,
    ) : OpenMeteoParseException("Missing required field: $fieldPath")

    class InvalidField(
        val fieldPath: String,
        detail: String,
    ) : OpenMeteoParseException("Invalid field $fieldPath: $detail")
}

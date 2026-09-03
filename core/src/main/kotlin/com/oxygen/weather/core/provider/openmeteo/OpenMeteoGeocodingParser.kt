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

object OpenMeteoGeocodingParser {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun parseSearch(body: String): OpenMeteoGeocodingResponse {
        val root = try {
            json.parseToJsonElement(body).jsonObject
        } catch (error: IllegalArgumentException) {
            throw OpenMeteoGeocodingException.InvalidJsonEnvelope("root", error.message ?: "Invalid JSON")
        } catch (error: SerializationException) {
            throw OpenMeteoGeocodingException.InvalidJsonEnvelope("root", error.message ?: "Invalid JSON")
        }

        throwIfProviderError(root)

        return OpenMeteoGeocodingResponse(
            results = root.optionalResultsArray("results").mapIndexed(::parseResult),
        )
    }

    private fun throwIfProviderError(root: JsonObject) {
        val isError = root["error"]?.jsonPrimitiveOrNull()?.booleanOrNull == true
        if (isError) {
            throw OpenMeteoGeocodingException.ProviderErrorBody(
                reason = root["reason"]?.jsonPrimitiveOrNull()?.contentOrNull
                    ?: "Open-Meteo returned an error body",
            )
        }
    }

    private fun parseResult(index: Int, element: JsonElement): OpenMeteoGeocodingResult {
        val fieldPrefix = "results[$index]"
        val result = element.jsonObjectOrNull()
            ?: throw OpenMeteoGeocodingException.InvalidJsonEnvelope(fieldPrefix, "Expected object")

        return OpenMeteoGeocodingResult(
            id = result.optionalInt("$fieldPrefix.id", "id"),
            name = result.requiredString("$fieldPrefix.name", "name"),
            latitude = result.requiredDouble("$fieldPrefix.latitude", "latitude"),
            longitude = result.requiredDouble("$fieldPrefix.longitude", "longitude"),
            timezone = result.requiredString("$fieldPrefix.timezone", "timezone"),
            country = result.requiredString("$fieldPrefix.country", "country"),
            countryCode = result.requiredString("$fieldPrefix.country_code", "country_code"),
            admin1 = result.optionalString("admin1"),
            admin2 = result.optionalString("admin2"),
            admin3 = result.optionalString("admin3"),
            admin4 = result.optionalString("admin4"),
            elevation = result.optionalDouble("$fieldPrefix.elevation", "elevation"),
            featureCode = result.optionalString("feature_code"),
            population = result.optionalInt("$fieldPrefix.population", "population"),
            postcodes = result.optionalStringList("$fieldPrefix.postcodes", "postcodes"),
        )
    }

    private fun JsonObject.optionalResultsArray(field: String): List<JsonElement> =
        this[field]?.jsonArrayOrNull(field) ?: emptyList()

    private fun JsonObject.requiredString(fieldPath: String, field: String): String {
        val element = this[field] ?: throw OpenMeteoGeocodingException.MissingRequiredField(fieldPath)
        if (element is JsonNull) throw OpenMeteoGeocodingException.MissingRequiredField(fieldPath)
        return element.jsonPrimitiveOrNull()?.contentOrNull
            ?: throw OpenMeteoGeocodingException.InvalidFieldValue(fieldPath, "Expected string")
    }

    private fun JsonObject.requiredDouble(fieldPath: String, field: String): Double {
        val element = this[field] ?: throw OpenMeteoGeocodingException.MissingRequiredField(fieldPath)
        if (element is JsonNull) throw OpenMeteoGeocodingException.MissingRequiredField(fieldPath)
        return element.jsonPrimitiveOrNull()?.doubleOrNull
            ?: throw OpenMeteoGeocodingException.InvalidFieldValue(fieldPath, "Expected number")
    }

    private fun JsonObject.optionalString(field: String): String? =
        optionalPrimitive(field)?.contentOrNull

    private fun JsonObject.optionalDouble(fieldPath: String, field: String): Double? {
        val element = this[field] ?: return null
        if (element is JsonNull) return null
        return element.jsonPrimitiveOrNull()?.doubleOrNull
            ?: throw OpenMeteoGeocodingException.InvalidFieldValue(fieldPath, "Expected number or null")
    }

    private fun JsonObject.optionalInt(fieldPath: String, field: String = fieldPath): Int? {
        val element = this[field] ?: return null
        if (element is JsonNull) return null
        return element.jsonPrimitiveOrNull()?.intOrNull
            ?: throw OpenMeteoGeocodingException.InvalidFieldValue(fieldPath, "Expected integer or null")
    }

    private fun JsonObject.optionalStringList(fieldPath: String, field: String): List<String> {
        val element = this[field] ?: return emptyList()
        if (element is JsonNull) return emptyList()
        return element.jsonArrayOrNull(fieldPath).mapIndexed { index, value ->
            if (value is JsonNull) {
                throw OpenMeteoGeocodingException.InvalidFieldValue("$fieldPath[$index]", "Expected string")
            }
            value.jsonPrimitiveOrNull()?.contentOrNull
                ?: throw OpenMeteoGeocodingException.InvalidFieldValue("$fieldPath[$index]", "Expected string")
        }
    }

    private fun JsonObject.optionalPrimitive(field: String): JsonPrimitive? {
        val element = this[field] ?: return null
        if (element is JsonNull) return null
        return element.jsonPrimitiveOrNull()
            ?: throw OpenMeteoGeocodingException.InvalidFieldValue(field, "Expected primitive or null")
    }

    private fun JsonElement.jsonArrayOrNull(fieldPath: String): List<JsonElement> =
        try {
            jsonArray
        } catch (error: IllegalArgumentException) {
            throw OpenMeteoGeocodingException.InvalidJsonEnvelope(fieldPath, "Expected array")
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

sealed class OpenMeteoGeocodingException(message: String) : IllegalArgumentException(message) {
    class InvalidJsonEnvelope(
        val fieldPath: String,
        detail: String,
    ) : OpenMeteoGeocodingException("Invalid geocoding JSON/envelope at $fieldPath: $detail")

    class ProviderErrorBody(
        val reason: String,
    ) : OpenMeteoGeocodingException(reason)

    class MissingRequiredField(
        val fieldPath: String,
    ) : OpenMeteoGeocodingException("Missing required geocoding field: $fieldPath")

    class InvalidFieldValue(
        val fieldPath: String,
        detail: String,
    ) : OpenMeteoGeocodingException("Invalid geocoding field $fieldPath: $detail")

    class InvalidCoordinate(
        val fieldPath: String,
        detail: String,
    ) : OpenMeteoGeocodingException("Invalid geocoding coordinate $fieldPath: $detail")

    class InvalidTimezone(
        val fieldPath: String,
        detail: String,
    ) : OpenMeteoGeocodingException("Invalid geocoding timezone $fieldPath: $detail")
}

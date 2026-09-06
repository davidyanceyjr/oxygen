package com.oxygen.weather.core.provider.nws

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object NwsAlertParser {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun parseActiveAlerts(body: String): NwsAlertCollection {
        val root = try {
            json.parseToJsonElement(body).jsonObject
        } catch (error: IllegalArgumentException) {
            throw NwsAlertParseException.InvalidJson(error.message ?: "Invalid JSON")
        } catch (error: SerializationException) {
            throw NwsAlertParseException.InvalidJson(error.message ?: "Invalid JSON")
        }

        val type = root.requiredString("type")
        if (type != "FeatureCollection") {
            throw NwsAlertParseException.InvalidField("type", "Expected FeatureCollection")
        }

        return NwsAlertCollection(
            type = type,
            features = root.requiredArray("features").mapIndexed { index, element ->
                parseFeature(index, element.jsonObjectOrInvalid("features[$index]"))
            },
        )
    }

    private fun parseFeature(index: Int, feature: JsonObject): NwsAlertFeature {
        val field = "features[$index]"
        val properties = feature.requiredObject("$field.properties", "properties")
        return NwsAlertFeature(
            id = feature.requiredString("$field.id", "id"),
            geometry = feature.optionalObject("geometry"),
            properties = parseProperties("$field.properties", properties),
        )
    }

    private fun parseProperties(field: String, properties: JsonObject): NwsAlertProperties =
        NwsAlertProperties(
            id = properties.requiredString("$field.id", "id"),
            areaDesc = properties.optionalString("areaDesc"),
            geocode = properties.optionalStringListMap("$field.geocode", "geocode"),
            affectedZones = properties.optionalStringList("$field.affectedZones", "affectedZones"),
            references = parseReferences("$field.references", properties.optionalArray("references")),
            sent = properties.optionalString("sent"),
            effective = properties.optionalString("effective"),
            onset = properties.optionalString("onset"),
            expires = properties.optionalString("expires"),
            ends = properties.optionalString("ends"),
            status = properties.optionalString("status"),
            messageType = properties.optionalString("messageType"),
            category = properties.optionalString("category"),
            severity = properties.optionalString("severity"),
            certainty = properties.optionalString("certainty"),
            urgency = properties.optionalString("urgency"),
            event = properties.requiredString("$field.event", "event"),
            sender = properties.optionalString("sender"),
            senderName = properties.requiredString("$field.senderName", "senderName"),
            headline = properties.optionalString("headline"),
            description = properties.optionalString("description"),
            instruction = properties.optionalString("instruction"),
            response = properties.optionalString("response"),
            parameters = properties.optionalStringListMap("$field.parameters", "parameters"),
            scope = properties.optionalString("scope"),
            code = properties.optionalString("code"),
            language = properties.optionalString("language"),
            web = properties.optionalString("web"),
            eventCode = properties.optionalStringListMap("$field.eventCode", "eventCode"),
        )

    private fun parseReferences(field: String, references: List<JsonElement>?): List<NwsAlertReferenceDto>? =
        references?.mapIndexed { index, element ->
            val reference = element.jsonObjectOrInvalid("$field[$index]")
            NwsAlertReferenceDto(
                id = reference.optionalString("@id") ?: reference.optionalString("id"),
                identifier = reference.optionalString("identifier"),
                sender = reference.optionalString("sender"),
                sent = reference.optionalString("sent"),
            )
        }

    private fun JsonObject.requiredObject(fieldPath: String, field: String): JsonObject =
        required(fieldPath, field).jsonObjectOrInvalid(fieldPath)

    private fun JsonObject.optionalObject(field: String): JsonObject? {
        val element = this[field] ?: return null
        if (element is JsonNull) return null
        return element.jsonObjectOrInvalid(field)
    }

    private fun JsonObject.requiredString(fieldPath: String, field: String = fieldPath): String =
        required(fieldPath, field).jsonPrimitiveOrNull()?.contentOrNull
            ?: throw NwsAlertParseException.InvalidField(fieldPath, "Expected string")

    private fun JsonObject.optionalString(field: String): String? =
        optionalPrimitive(field)?.contentOrNull

    private fun JsonObject.optionalStringList(fieldPath: String, field: String): List<String>? {
        val element = this[field] ?: return null
        if (element is JsonNull) return null
        return element.jsonArrayOrInvalid(fieldPath).mapIndexed { index, value ->
            value.jsonPrimitiveOrNull()?.contentOrNull
                ?: throw NwsAlertParseException.InvalidField("$fieldPath[$index]", "Expected string")
        }
    }

    private fun JsonObject.optionalStringListMap(fieldPath: String, field: String): Map<String, List<String>>? {
        val element = this[field] ?: return null
        if (element is JsonNull) return null
        return element.jsonObjectOrInvalid(fieldPath).mapValues { (key, value) ->
            value.jsonArrayOrInvalid("$fieldPath.$key").mapIndexed { index, entry ->
                entry.jsonPrimitiveOrNull()?.contentOrNull
                    ?: throw NwsAlertParseException.InvalidField("$fieldPath.$key[$index]", "Expected string")
            }
        }
    }

    private fun JsonObject.optionalArray(field: String): List<JsonElement>? {
        val element = this[field] ?: return null
        if (element is JsonNull) return null
        return element.jsonArrayOrInvalid(field)
    }

    private fun JsonObject.requiredArray(field: String): List<JsonElement> =
        required(field).jsonArrayOrInvalid(field)

    private fun JsonObject.required(field: String): JsonElement =
        this[field] ?: throw NwsAlertParseException.MissingField(field)

    private fun JsonObject.required(fieldPath: String, field: String): JsonElement =
        this[field] ?: throw NwsAlertParseException.MissingField(fieldPath)

    private fun JsonObject.optionalPrimitive(field: String): JsonPrimitive? {
        val element = this[field] ?: return null
        if (element is JsonNull) return null
        return element.jsonPrimitiveOrNull()
            ?: throw NwsAlertParseException.InvalidField(field, "Expected primitive or null")
    }

    private fun JsonElement.jsonArrayOrInvalid(field: String): List<JsonElement> =
        try {
            jsonArray
        } catch (error: IllegalArgumentException) {
            throw NwsAlertParseException.InvalidField(field, "Expected array")
        }

    private fun JsonElement.jsonObjectOrInvalid(field: String): JsonObject =
        try {
            jsonObject
        } catch (error: IllegalArgumentException) {
            throw NwsAlertParseException.InvalidField(field, "Expected object")
        }

    private fun JsonElement.jsonPrimitiveOrNull(): JsonPrimitive? =
        try {
            jsonPrimitive
        } catch (error: IllegalArgumentException) {
            null
        }
}

sealed class NwsAlertParseException(message: String) : IllegalArgumentException(message) {
    class InvalidJson(message: String) : NwsAlertParseException(message)

    class MissingField(
        val fieldPath: String,
    ) : NwsAlertParseException("Missing required field: $fieldPath")

    class InvalidField(
        val fieldPath: String,
        detail: String,
    ) : NwsAlertParseException("Invalid field $fieldPath: $detail")
}

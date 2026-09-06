package com.oxygen.weather.core.provider.nws

import com.oxygen.weather.core.model.AlertAffectedArea
import com.oxygen.weather.core.model.AlertCertainty
import com.oxygen.weather.core.model.AlertGeometry
import com.oxygen.weather.core.model.AlertMessageType
import com.oxygen.weather.core.model.AlertReference
import com.oxygen.weather.core.model.AlertSeverity
import com.oxygen.weather.core.model.AlertStatus
import com.oxygen.weather.core.model.AlertUrgency
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.WeatherAlert
import java.time.DateTimeException
import java.time.Instant
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object NwsAlertMapper {
    private const val PROVIDER_ID = "nws"
    private const val SOURCE_NAME = "NOAA/National Weather Service"

    fun map(collection: NwsAlertCollection, fetchedAt: Instant): List<WeatherAlert> =
        collection.features.mapIndexed { index, feature ->
            mapFeature(index, feature, fetchedAt)
        }

    private fun mapFeature(index: Int, feature: NwsAlertFeature, fetchedAt: Instant): WeatherAlert {
        val field = "features[$index]"
        val properties = feature.properties
        val sent = properties.sent?.parseInstant("$field.properties.sent")

        return WeatherAlert(
            id = properties.id,
            event = properties.event,
            headline = properties.headline,
            severity = mapSeverity(properties.severity),
            urgency = mapUrgency(properties.urgency),
            certainty = mapCertainty(properties.certainty),
            effective = properties.effective?.parseInstant("$field.properties.effective"),
            expires = properties.expires?.parseInstant("$field.properties.expires"),
            sent = sent,
            onset = properties.onset?.parseInstant("$field.properties.onset"),
            ends = properties.ends?.parseInstant("$field.properties.ends"),
            status = mapStatus(properties.status),
            messageType = mapMessageType(properties.messageType),
            references = mapReferences("$field.properties.references", properties.references),
            affectedArea = mapAffectedArea(properties),
            geometry = feature.geometry?.let { mapGeometry("$field.geometry", it) },
            category = properties.category,
            response = properties.response,
            scope = properties.scope,
            code = properties.code,
            language = properties.language,
            web = properties.web,
            eventCodes = properties.eventCode,
            parameters = properties.parameters,
            description = properties.description,
            instruction = properties.instruction,
            issuer = properties.senderName,
            provenance = DataProvenance(
                providerId = PROVIDER_ID,
                sourceName = SOURCE_NAME,
                issuedAt = sent,
                fetchedAt = fetchedAt,
                type = DataType.OFFICIAL_ALERT,
                licenseId = null,
            ),
        )
    }

    fun mapSeverity(value: String?): AlertSeverity = when (value) {
        "Extreme" -> AlertSeverity.EXTREME
        "Severe" -> AlertSeverity.SEVERE
        "Moderate" -> AlertSeverity.MODERATE
        "Minor" -> AlertSeverity.MINOR
        else -> AlertSeverity.UNKNOWN
    }

    private fun mapUrgency(value: String?): AlertUrgency = when (value) {
        "Immediate" -> AlertUrgency.IMMEDIATE
        "Expected" -> AlertUrgency.EXPECTED
        "Future" -> AlertUrgency.FUTURE
        "Past" -> AlertUrgency.PAST
        else -> AlertUrgency.UNKNOWN
    }

    private fun mapCertainty(value: String?): AlertCertainty = when (value) {
        "Observed" -> AlertCertainty.OBSERVED
        "Likely" -> AlertCertainty.LIKELY
        "Possible" -> AlertCertainty.POSSIBLE
        "Unlikely" -> AlertCertainty.UNLIKELY
        else -> AlertCertainty.UNKNOWN
    }

    private fun mapStatus(value: String?): AlertStatus = when (value) {
        "Actual" -> AlertStatus.ACTUAL
        "Exercise" -> AlertStatus.EXERCISE
        "System" -> AlertStatus.SYSTEM
        "Test" -> AlertStatus.TEST
        "Draft" -> AlertStatus.DRAFT
        else -> AlertStatus.UNKNOWN
    }

    private fun mapMessageType(value: String?): AlertMessageType = when (value) {
        "Alert" -> AlertMessageType.ALERT
        "Update" -> AlertMessageType.UPDATE
        "Cancel" -> AlertMessageType.CANCEL
        "Ack" -> AlertMessageType.ACK
        "Error" -> AlertMessageType.ERROR
        else -> AlertMessageType.UNKNOWN
    }

    private fun mapReferences(field: String, references: List<NwsAlertReferenceDto>?): List<AlertReference> =
        references.orEmpty().mapIndexed { index, reference ->
            val id = reference.identifier ?: reference.id
                ?: throw NwsAlertMapperException.InvalidField("$field[$index]", "Missing identifier")
            AlertReference(
                id = id,
                sender = reference.sender,
                sent = reference.sent?.parseInstant("$field[$index].sent"),
            )
        }

    private fun mapAffectedArea(properties: NwsAlertProperties): AlertAffectedArea? {
        val geocode = properties.geocode
        val ugcCodes = geocode?.get("UGC").orEmpty()
        val sameCodes = geocode?.get("SAME").orEmpty()
        val affectedZones = properties.affectedZones.orEmpty()
        val hasAnySource = properties.areaDesc != null || geocode != null || properties.affectedZones != null
        if (!hasAnySource) return null

        return AlertAffectedArea(
            areaDescription = properties.areaDesc,
            ugcCodes = ugcCodes,
            sameCodes = sameCodes,
            affectedZoneIds = affectedZones,
        )
    }

    private fun mapGeometry(field: String, geometry: JsonObject): AlertGeometry {
        val type = geometry.string("type", "$field.type")
        return when (type) {
            "Point" -> AlertGeometry.Point(point(geometry.required("coordinates", field), "$field.coordinates"))
            "MultiPoint" -> AlertGeometry.MultiPoint(pointList(geometry.required("coordinates", field), "$field.coordinates"))
            "LineString" -> lineString(geometry.required("coordinates", field), "$field.coordinates")
            "MultiLineString" -> AlertGeometry.MultiLineString(
                geometry.required("coordinates", field).array("$field.coordinates").mapIndexed { index, line ->
                    linePoints(line, "$field.coordinates[$index]")
                },
            )
            "Polygon" -> AlertGeometry.Polygon(polygonRings(geometry.required("coordinates", field), "$field.coordinates"))
            "MultiPolygon" -> AlertGeometry.MultiPolygon(
                geometry.required("coordinates", field).array("$field.coordinates").mapIndexed { index, polygon ->
                    polygonRings(polygon, "$field.coordinates[$index]")
                },
            )
            "GeometryCollection" -> AlertGeometry.GeometryCollection(
                geometry.required("geometries", field).array("$field.geometries").mapIndexed { index, element ->
                    mapGeometry("$field.geometries[$index]", element.obj("$field.geometries[$index]"))
                },
            )
            else -> throw NwsAlertMapperException.InvalidField("$field.type", "Unsupported geometry type: $type")
        }
    }

    private fun lineString(element: JsonElement, field: String): AlertGeometry.LineString =
        AlertGeometry.LineString(linePoints(element, field))

    private fun linePoints(element: JsonElement, field: String): List<GeoPoint> {
        val points = pointList(element, field)
        if (points.size < 2) {
            throw NwsAlertMapperException.InvalidField(field, "Expected at least two positions")
        }
        return points
    }

    private fun polygonRings(element: JsonElement, field: String): List<List<GeoPoint>> =
        element.array(field).mapIndexed { index, ring ->
            val points = pointList(ring, "$field[$index]")
            if (points.size < 4) {
                throw NwsAlertMapperException.InvalidField("$field[$index]", "Expected at least four positions")
            }
            if (points.first().longitude != points.last().longitude || points.first().latitude != points.last().latitude) {
                throw NwsAlertMapperException.InvalidField("$field[$index]", "Expected closed polygon ring")
            }
            points
        }

    private fun pointList(element: JsonElement, field: String): List<GeoPoint> =
        element.array(field).mapIndexed { index, item -> point(item, "$field[$index]") }

    private fun point(element: JsonElement, field: String): GeoPoint {
        val coordinates = element.array(field)
        if (coordinates.size < 2) {
            throw NwsAlertMapperException.InvalidField(field, "Expected longitude and latitude")
        }
        val longitude = coordinates[0].number("$field[0]")
        val latitude = coordinates[1].number("$field[1]")
        if (!longitude.isFinite() || longitude < -180.0 || longitude > 180.0) {
            throw NwsAlertMapperException.InvalidField("$field[0]", "Longitude outside WGS84 bounds")
        }
        if (!latitude.isFinite() || latitude < -90.0 || latitude > 90.0) {
            throw NwsAlertMapperException.InvalidField("$field[1]", "Latitude outside WGS84 bounds")
        }
        return GeoPoint(latitude = latitude, longitude = longitude)
    }

    private fun String.parseInstant(field: String): Instant =
        try {
            Instant.parse(this)
        } catch (error: DateTimeException) {
            throw NwsAlertMapperException.InvalidField(field, "Invalid timestamp: $this")
        }

    private fun JsonObject.required(name: String, parentField: String): JsonElement =
        this[name] ?: throw NwsAlertMapperException.InvalidField("$parentField.$name", "Missing field")

    private fun JsonObject.string(name: String, field: String): String =
        required(name, field.substringBeforeLast('.')).primitive(field).contentOrNull
            ?: throw NwsAlertMapperException.InvalidField(field, "Expected string")

    private fun JsonElement.array(field: String): JsonArray =
        try {
            jsonArray
        } catch (error: IllegalArgumentException) {
            throw NwsAlertMapperException.InvalidField(field, "Expected array")
        }

    private fun JsonElement.obj(field: String): JsonObject =
        try {
            jsonObject
        } catch (error: IllegalArgumentException) {
            throw NwsAlertMapperException.InvalidField(field, "Expected object")
        }

    private fun JsonElement.primitive(field: String): JsonPrimitive =
        try {
            jsonPrimitive
        } catch (error: IllegalArgumentException) {
            throw NwsAlertMapperException.InvalidField(field, "Expected primitive")
        }

    private fun JsonElement.number(field: String): Double {
        if (this is JsonNull) {
            throw NwsAlertMapperException.InvalidField(field, "Expected number")
        }
        return primitive(field).doubleOrNull
            ?: throw NwsAlertMapperException.InvalidField(field, "Expected number")
    }
}

sealed class NwsAlertMapperException(message: String) : IllegalArgumentException(message) {
    class InvalidField(
        val fieldPath: String,
        detail: String,
    ) : NwsAlertMapperException("Invalid field $fieldPath: $detail")
}

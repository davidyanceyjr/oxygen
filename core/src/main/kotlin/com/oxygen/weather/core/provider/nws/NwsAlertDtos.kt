package com.oxygen.weather.core.provider.nws

import kotlinx.serialization.json.JsonObject

data class NwsAlertCollection(
    val type: String,
    val features: List<NwsAlertFeature>,
)

data class NwsAlertFeature(
    val id: String,
    val geometry: JsonObject?,
    val properties: NwsAlertProperties,
)

data class NwsAlertProperties(
    val id: String,
    val areaDesc: String?,
    val geocode: Map<String, List<String>>?,
    val affectedZones: List<String>?,
    val references: List<NwsAlertReferenceDto>?,
    val sent: String?,
    val effective: String?,
    val onset: String?,
    val expires: String?,
    val ends: String?,
    val status: String?,
    val messageType: String?,
    val category: String?,
    val severity: String?,
    val certainty: String?,
    val urgency: String?,
    val event: String,
    val sender: String?,
    val senderName: String,
    val headline: String?,
    val description: String?,
    val instruction: String?,
    val response: String?,
    val parameters: Map<String, List<String>>?,
    val scope: String?,
    val code: String?,
    val language: String?,
    val web: String?,
    val eventCode: Map<String, List<String>>?,
)

data class NwsAlertReferenceDto(
    val id: String?,
    val identifier: String?,
    val sender: String?,
    val sent: String?,
)

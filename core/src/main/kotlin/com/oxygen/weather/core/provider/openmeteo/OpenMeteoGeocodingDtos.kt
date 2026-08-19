package com.oxygen.weather.core.provider.openmeteo

data class OpenMeteoGeocodingResponse(
    val results: List<OpenMeteoGeocodingResult>,
)

data class OpenMeteoGeocodingResult(
    val id: Int? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val country: String,
    val countryCode: String,
    val admin1: String? = null,
    val admin2: String? = null,
    val admin3: String? = null,
    val admin4: String? = null,
    val elevation: Double? = null,
    val featureCode: String? = null,
    val population: Int? = null,
    val postcodes: List<String> = emptyList(),
)

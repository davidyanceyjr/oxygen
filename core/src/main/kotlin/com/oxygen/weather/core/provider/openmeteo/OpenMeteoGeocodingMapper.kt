package com.oxygen.weather.core.provider.openmeteo

import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.GeocodingLocationCandidate
import com.oxygen.weather.core.model.LocationId
import java.math.RoundingMode
import java.security.MessageDigest
import java.text.Normalizer
import java.time.DateTimeException
import java.time.ZoneId
import java.util.Locale

object OpenMeteoGeocodingMapper {
    private const val LOCATION_ID_PREFIX = "loc"
    private const val COORDINATE_ID_DECIMAL_PLACES = 4

    fun map(response: OpenMeteoGeocodingResponse): List<GeocodingLocationCandidate> =
        response.results.mapIndexed { index, result ->
            val point = GeoPoint(
                latitude = result.latitude.validLatitude("results[$index].latitude"),
                longitude = result.longitude.validLongitude("results[$index].longitude"),
            )
            val zoneId = result.timezone.validZoneId("results[$index].timezone")
            val administrativeAreas = result.administrativeAreas()
            val displayName = displayName(result.name, administrativeAreas.firstOrNull(), result.country)
            val locationId = stableLocationId(
                displayName = displayName,
                countryCode = result.countryCode,
                administrativeAreas = administrativeAreas,
                point = point,
                timezone = zoneId.id,
            )

            GeocodingLocationCandidate(
                locationId = locationId,
                displayName = displayName,
                point = point,
                zoneId = zoneId,
                country = result.country,
                countryCode = result.countryCode,
                administrativeAreas = administrativeAreas,
                elevationMeters = result.elevation,
                featureCode = result.featureCode,
                population = result.population,
                postcodes = result.postcodes,
            )
        }

    private fun OpenMeteoGeocodingResult.administrativeAreas(): List<String> =
        listOfNotNull(admin1, admin2, admin3, admin4)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

    private fun displayName(
        name: String,
        primaryAdminArea: String?,
        country: String,
    ): String =
        listOfNotNull(name.trim(), primaryAdminArea?.trim(), country.trim())
            .filter { it.isNotEmpty() }
            .joinToString(", ")

    private fun Double.validLatitude(fieldPath: String): Double {
        if (this !in -90.0..90.0) {
            throw OpenMeteoGeocodingException.InvalidCoordinate(fieldPath, "Latitude must be between -90 and 90")
        }
        return this
    }

    private fun Double.validLongitude(fieldPath: String): Double {
        if (this !in -180.0..180.0) {
            throw OpenMeteoGeocodingException.InvalidCoordinate(fieldPath, "Longitude must be between -180 and 180")
        }
        return this
    }

    private fun String.validZoneId(fieldPath: String): ZoneId =
        try {
            ZoneId.of(this)
        } catch (error: DateTimeException) {
            throw OpenMeteoGeocodingException.InvalidTimezone(fieldPath, error.message ?: "Invalid IANA timezone")
        }

    private fun stableLocationId(
        displayName: String,
        countryCode: String,
        administrativeAreas: List<String>,
        point: GeoPoint,
        timezone: String,
    ): LocationId {
        val normalizedInputs = listOf(
            displayName.normalizedForId(),
            countryCode.trim().uppercase(Locale.ROOT),
            administrativeAreas.joinToString("|") { it.normalizedForId() },
            point.latitude.roundForId(),
            point.longitude.roundForId(),
            timezone.trim(),
        ).joinToString("\n")

        val digest = MessageDigest.getInstance("SHA-256").digest(normalizedInputs.toByteArray(Charsets.UTF_8))
        val shortHash = digest.take(12).joinToString("") { byte -> "%02x".format(byte) }
        return LocationId("$LOCATION_ID_PREFIX-$shortHash")
    }

    private fun String.normalizedForId(): String =
        Normalizer.normalize(trim(), Normalizer.Form.NFKC)
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")

    // Stable local IDs round coordinates to 4 decimal places, roughly 11 meters at the equator.
    private fun Double.roundForId(): String =
        toBigDecimal()
            .setScale(COORDINATE_ID_DECIMAL_PLACES, RoundingMode.HALF_UP)
            .toPlainString()
}

package com.oxygen.weather.core.provider.openmeteo

import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.provider.CoordinateTimeZoneError
import com.oxygen.weather.core.provider.CoordinateTimeZoneResolver
import com.oxygen.weather.core.provider.CoordinateTimeZoneResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import java.io.IOException
import java.net.URL
import java.time.ZoneId

class OpenMeteoTimeZoneResolver(
    private val baseUrl: String = "https://api.open-meteo.com/v1/forecast",
    private val transport: OpenMeteoHttpTransport = UrlConnectionOpenMeteoHttpTransport(),
) : CoordinateTimeZoneResolver {
    override fun resolve(point: GeoPoint): CoordinateTimeZoneResult {
        if (!point.latitude.isFinite() || point.latitude !in -90.0..90.0 ||
            !point.longitude.isFinite() || point.longitude !in -180.0..180.0
        ) return failure(CoordinateTimeZoneError.InvalidPoint)

        val response = try {
            val separator = if ('?' in baseUrl) '&' else '?'
            transport.get(URL("$baseUrl${separator}latitude=${point.latitude}&longitude=${point.longitude}&timezone=auto"))
        } catch (_: IOException) {
            return failure(CoordinateTimeZoneError.NetworkUnavailable)
        } catch (_: Exception) {
            return failure(CoordinateTimeZoneError.ProviderUnavailable)
        }
        when (response.statusCode) {
            200 -> Unit
            429 -> return failure(CoordinateTimeZoneError.RateLimited)
            in 400..499 -> return failure(CoordinateTimeZoneError.RequestRejected)
            else -> return failure(CoordinateTimeZoneError.ProviderUnavailable)
        }
        return try {
            val body = Json.parseToJsonElement(response.body) as? JsonObject
                ?: return failure(CoordinateTimeZoneError.InvalidResponse)
            if ((body["error"] as? JsonPrimitive)?.booleanOrNull == true) {
                return failure(CoordinateTimeZoneError.RequestRejected)
            }
            val zone = body["timezone"] as? JsonPrimitive
            if (zone == null || !zone.isString || zone.content !in ZoneId.getAvailableZoneIds()) {
                return failure(CoordinateTimeZoneError.InvalidResponse)
            }
            CoordinateTimeZoneResult.Success(point, ZoneId.of(zone.content))
        } catch (_: Exception) {
            failure(CoordinateTimeZoneError.InvalidResponse)
        }
    }

    private fun failure(error: CoordinateTimeZoneError) = CoordinateTimeZoneResult.Failure(error)
}

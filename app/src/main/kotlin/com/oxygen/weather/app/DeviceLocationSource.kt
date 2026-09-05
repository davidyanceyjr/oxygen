package com.oxygen.weather.app

import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherLocation
import java.math.RoundingMode
import java.security.MessageDigest
import java.time.ZoneId

fun interface LocationCancellation {
    fun cancel()
}

fun interface DeviceLocationSource {
    fun locate(onResult: (DeviceLocationResult) -> Unit): LocationCancellation
}

sealed interface DeviceLocationResult {
    data class Success(val point: GeoPoint) : DeviceLocationResult
    data object Unavailable : DeviceLocationResult
    data object TimedOut : DeviceLocationResult
    data object Cancelled : DeviceLocationResult
}

internal data class PlatformLocationFix(val point: GeoPoint, val elapsedRealtimeNanos: Long)

internal interface DeviceLocationPlatform {
    val apiLevel: Int
    fun enabledCoarseProvider(): String?
    fun elapsedRealtimeNanos(): Long
    fun scheduleTimeout(delayMillis: Long, action: () -> Unit): LocationCancellation
    fun currentLocation(provider: String, callback: (PlatformLocationFix?) -> Unit): LocationCancellation
    fun singleUpdate(provider: String, callback: (PlatformLocationFix?) -> Unit): LocationCancellation
}

/** One bounded foreground request; never reads last-known coordinates. */
internal class OneShotDeviceLocationSource(private val platform: DeviceLocationPlatform) : DeviceLocationSource {
    override fun locate(onResult: (DeviceLocationResult) -> Unit): LocationCancellation {
        val lock = Any()
        var completed = false
        var request: LocationCancellation? = null
        var timeout: LocationCancellation? = null
        fun finish(result: DeviceLocationResult) {
            val cleanup = synchronized(lock) {
                if (completed) return
                completed = true
                request to timeout
            }
            // Permission revocation can make platform cleanup throw; completion must still arrive.
            runCatching { cleanup.first?.cancel() }
            runCatching { cleanup.second?.cancel() }
            onResult(result)
        }
        val cancellation = LocationCancellation { finish(DeviceLocationResult.Cancelled) }
        try {
            val provider = platform.enabledCoarseProvider()
            if (provider == null) {
                finish(DeviceLocationResult.Unavailable)
                return cancellation
            }
            val timer = platform.scheduleTimeout(20_000) { finish(DeviceLocationResult.TimedOut) }
            synchronized(lock) {
                timeout = timer
                if (completed) timer.cancel()
            }
            val callback: (PlatformLocationFix?) -> Unit = { fix ->
                val age = fix?.let { platform.elapsedRealtimeNanos() - it.elapsedRealtimeNanos }
                val point = fix?.point
                val valid = point != null && point.latitude.isFinite() && point.longitude.isFinite() &&
                    point.latitude in -90.0..90.0 && point.longitude in -180.0..180.0 &&
                    fix.elapsedRealtimeNanos > 0 && age != null && age in 0..120_000_000_000L
                finish(if (valid) DeviceLocationResult.Success(point) else DeviceLocationResult.Unavailable)
            }
            synchronized(lock) {
                if (!completed) {
                    val handle = if (platform.apiLevel >= 30) {
                        platform.currentLocation(provider, callback)
                    } else {
                        platform.singleUpdate(provider, callback)
                    }
                    request = handle
                    if (completed) handle.cancel()
                }
            }
        } catch (_: Exception) {
            finish(DeviceLocationResult.Unavailable)
        }
        return cancellation
    }
}

internal fun approximateDeviceLocation(point: GeoPoint, zoneId: ZoneId): WeatherLocation {
    require(point.latitude.isFinite() && point.latitude in -90.0..90.0)
    require(point.longitude.isFinite() && point.longitude in -180.0..180.0)
    require(zoneId.id in ZoneId.getAvailableZoneIds())
    fun Double.normalized() = toBigDecimal().setScale(4, RoundingMode.HALF_UP).toPlainString()
    val identity = listOf("device", point.latitude.normalized(), point.longitude.normalized(), zoneId.id)
        .joinToString("\n")
    val hash = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray(Charsets.UTF_8))
        .take(12).joinToString("") { "%02x".format(it) }
    return WeatherLocation(
        id = LocationId("device-$hash"),
        displayName = "Approximate device location",
        point = point,
        elevationMeters = null,
        zoneId = zoneId,
    )
}

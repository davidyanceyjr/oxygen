package com.oxygen.weather.app

import com.oxygen.weather.core.model.GeoPoint
import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneId

class DeviceLocationSourceTest {
    @Test fun `both platform branches deliver once and remove request and deadline`() {
        listOf(26, 29, 30, 37).forEach { api ->
            val platform = ControlledPlatform(api)
            val results = mutableListOf<DeviceLocationResult>()
            val handle = OneShotDeviceLocationSource(platform).locate(results::add)
            assertEquals(if (api >= 30) "current" else "listener", platform.branch)
            platform.callback(platform.fix())
            platform.callback(platform.fix())
            platform.deadline()
            handle.cancel()
            assertEquals(listOf(DeviceLocationResult.Success(platform.point)), results)
            assertEquals(1, platform.requestCleanup)
            assertEquals(1, platform.timerCleanup)
        }
    }

    @Test fun `no enabled provider returns unavailable without registering request`() {
        val platform = ControlledPlatform(37).apply { provider = null }
        val results = mutableListOf<DeviceLocationResult>()
        OneShotDeviceLocationSource(platform).locate(results::add)
        assertEquals(listOf(DeviceLocationResult.Unavailable), results)
        assertNull(platform.branch)
    }

    @Test fun `null stale future and invalid fixes cannot be selected`() {
        val bad = listOf(null, PlatformLocationFix(GeoPoint(43.0, -89.0), 1),
            PlatformLocationFix(GeoPoint(43.0, -89.0), Long.MAX_VALUE),
            PlatformLocationFix(GeoPoint(Double.NaN, 0.0), NOW),
            PlatformLocationFix(GeoPoint(0.0, Double.POSITIVE_INFINITY), NOW),
            PlatformLocationFix(GeoPoint(91.0, 181.0), NOW))
        bad.forEach { fix ->
            val platform = ControlledPlatform(29)
            val results = mutableListOf<DeviceLocationResult>()
            OneShotDeviceLocationSource(platform).locate(results::add)
            platform.callback(fix)
            assertEquals(listOf(DeviceLocationResult.Unavailable), results)
            assertEquals(1, platform.requestCleanup)
            assertEquals(1, platform.timerCleanup)
        }
    }

    @Test fun `timeout and cancellation ignore late fixes and clean both branches`() {
        listOf(26, 37).forEach { api ->
            listOf(true, false).forEach { timeout ->
                val platform = ControlledPlatform(api)
                val results = mutableListOf<DeviceLocationResult>()
                val cancellation = OneShotDeviceLocationSource(platform).locate(results::add)
                if (timeout) platform.deadline() else cancellation.cancel()
                platform.callback(platform.fix())
                assertEquals(listOf(if (timeout) DeviceLocationResult.TimedOut else DeviceLocationResult.Cancelled), results)
                assertEquals(1, platform.requestCleanup)
                assertEquals(1, platform.timerCleanup)
            }
        }
    }

    @Test fun `synchronous callback and revoked grant still clean registration`() {
        val immediate = ControlledPlatform(37).apply { immediate = true }
        val results = mutableListOf<DeviceLocationResult>()
        OneShotDeviceLocationSource(immediate).locate(results::add)
        assertEquals(listOf(DeviceLocationResult.Success(immediate.point)), results)
        assertEquals(1, immediate.requestCleanup)
        val revoked = ControlledPlatform(26).apply { revoked = true }
        val failed = mutableListOf<DeviceLocationResult>()
        OneShotDeviceLocationSource(revoked).locate(failed::add)
        assertEquals(listOf(DeviceLocationResult.Unavailable), failed)
        assertEquals(1, revoked.timerCleanup)
    }

    @Test fun `device identity uses normalized coordinates and zone with honest label`() {
        val zone = ZoneId.of("America/Chicago")
        val a = approximateDeviceLocation(GeoPoint(43.073101, -89.401201), zone)
        val b = approximateDeviceLocation(GeoPoint(43.073102, -89.401202), zone)
        assertEquals(a.id, b.id)
        assertTrue(a.id.value.startsWith("device-"))
        assertNotEquals(a.id, approximateDeviceLocation(GeoPoint(44.0, -89.401201), zone).id)
        assertNotEquals(a.id, approximateDeviceLocation(a.point, ZoneId.of("America/New_York")).id)
        assertEquals("Approximate device location", a.displayName)
        assertNull(a.elevationMeters)
        assertEquals(GeoPoint(43.073101, -89.401201), a.point)
    }

    @Test fun `cleanup exception does not swallow timeout or allow late completion`() {
        val platform = ControlledPlatform(29).apply { cleanupThrows = true }
        val results = mutableListOf<DeviceLocationResult>()
        OneShotDeviceLocationSource(platform).locate(results::add)
        platform.deadline()
        platform.callback(platform.fix())
        assertEquals(listOf(DeviceLocationResult.TimedOut), results)
        assertEquals(1, platform.timerCleanup)
    }
}

private const val NOW = 500_000_000_000L

/** Controlled input, not a real platform-fix verification. */
private class ControlledPlatform(override val apiLevel: Int) : DeviceLocationPlatform {
    val point = GeoPoint(43.0731, -89.4012)
    var provider: String? = "network"
    var branch: String? = null
    var requestCleanup = 0
    var timerCleanup = 0
    var immediate = false
    var revoked = false
    var cleanupThrows = false
    lateinit var callback: (PlatformLocationFix?) -> Unit
    lateinit var deadline: () -> Unit
    override fun enabledCoarseProvider() = provider
    override fun elapsedRealtimeNanos() = NOW
    fun fix() = PlatformLocationFix(point, NOW)
    override fun scheduleTimeout(delayMillis: Long, action: () -> Unit): LocationCancellation {
        assertEquals(20_000L, delayMillis)
        deadline = action
        return LocationCancellation { timerCleanup++ }
    }
    override fun currentLocation(provider: String, callback: (PlatformLocationFix?) -> Unit): LocationCancellation {
        branch = "current"
        return register(callback)
    }
    override fun singleUpdate(provider: String, callback: (PlatformLocationFix?) -> Unit): LocationCancellation {
        branch = "listener"
        return register(callback)
    }
    private fun register(callback: (PlatformLocationFix?) -> Unit): LocationCancellation {
        if (revoked) throw SecurityException()
        this.callback = callback
        if (immediate) callback(fix())
        return LocationCancellation {
            requestCleanup++
            if (cleanupThrows) throw SecurityException("Controlled revocation during cleanup")
        }
    }
}

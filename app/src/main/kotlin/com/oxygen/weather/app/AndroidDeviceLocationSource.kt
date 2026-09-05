package com.oxygen.weather.app

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.oxygen.weather.core.model.GeoPoint
import java.util.concurrent.Executor

class AndroidDeviceLocationSource(context: Context) : DeviceLocationSource {
    private val delegate = OneShotDeviceLocationSource(AndroidLocationPlatform(context.applicationContext))

    override fun locate(onResult: (DeviceLocationResult) -> Unit): LocationCancellation = delegate.locate(onResult)
}

@SuppressLint("MissingPermission") // MainActivity checks coarse foreground grant on every explicit action.
private class AndroidLocationPlatform(context: Context) : DeviceLocationPlatform {
    private val manager = context.getSystemService(LocationManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    override val apiLevel: Int get() = Build.VERSION.SDK_INT

    override fun enabledCoarseProvider(): String? =
        listOf(LocationManager.NETWORK_PROVIDER, "fused").firstOrNull {
            it in manager.getProviders(true) && manager.isProviderEnabled(it)
        }

    override fun elapsedRealtimeNanos(): Long = SystemClock.elapsedRealtimeNanos()

    override fun scheduleTimeout(delayMillis: Long, action: () -> Unit): LocationCancellation {
        val runnable = Runnable(action)
        handler.postDelayed(runnable, delayMillis)
        return LocationCancellation { handler.removeCallbacks(runnable) }
    }

    override fun currentLocation(provider: String, callback: (PlatformLocationFix?) -> Unit): LocationCancellation {
        check(Build.VERSION.SDK_INT >= 30)
        val signal = CancellationSignal()
        try {
            manager.getCurrentLocation(provider, signal, Executor { handler.post(it) }) { callback(it?.toFix()) }
        } catch (error: Exception) {
            signal.cancel()
            throw error
        }
        return LocationCancellation { signal.cancel() }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun singleUpdate(provider: String, callback: (PlatformLocationFix?) -> Unit): LocationCancellation {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) = callback(location.toFix())
            override fun onProviderDisabled(provider: String) = callback(null)
            override fun onProviderEnabled(provider: String) = Unit
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }
        try {
            manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        } catch (error: Exception) {
            manager.removeUpdates(listener)
            throw error
        }
        return LocationCancellation { manager.removeUpdates(listener) }
    }

    private fun Location.toFix() = PlatformLocationFix(GeoPoint(latitude, longitude), elapsedRealtimeNanos)
}

package com.oxygen.weather

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.oxygen.weather.app.DataStoreSelectedLocationStorage
import com.oxygen.weather.app.InstalledForecastRepositoryFactory
import com.oxygen.weather.app.OxygenApp
import com.oxygen.weather.app.OxygenAppStateHolder
import com.oxygen.weather.app.DataStoreUnitPreferenceStorage
import com.oxygen.weather.app.DataStoreEffectsPreferenceStorage
import com.oxygen.weather.app.DataStoreThemePreferenceStorage
import com.oxygen.weather.app.AndroidMotionPreferenceSource
import com.oxygen.weather.app.AndroidDeviceLocationSource
import com.oxygen.weather.app.LocationPermissionResult
import com.oxygen.weather.core.provider.cache.room.RoomForecastCacheStorageFactory
import com.oxygen.weather.core.provider.cache.room.RoomSavedLocationStorageFactory

class MainActivity : ComponentActivity() {
    private var appStateHolder: OxygenAppStateHolder? = null
    private var permissionAttempt: Long? = null
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val attempt = permissionAttempt
        permissionAttempt = null
        if (attempt != null) appStateHolder?.onLocationPermissionResult(
            attempt,
            if (granted) LocationPermissionResult.Granted else LocationPermissionResult.Denied,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val forecastCacheStorage = remember { RoomForecastCacheStorageFactory.create(this) }
            val savedLocationStorage = remember { RoomSavedLocationStorageFactory.create(this) }
            val selectedLocationStorage = remember { DataStoreSelectedLocationStorage(this) }
            val layoutPreferenceStorage = remember { com.oxygen.weather.app.DataStoreLayoutPreferenceStorage(this) }
            val unitPreferenceStorage = remember { DataStoreUnitPreferenceStorage(this) }
            val effectsPreferenceStorage = remember { DataStoreEffectsPreferenceStorage(this) }
            val themePreferenceStorage = remember { DataStoreThemePreferenceStorage(applicationContext) }
            val stateHolder = remember {
                OxygenAppStateHolder(
                    deviceLocationSource = AndroidDeviceLocationSource(this),
                    selectedLocationStorage = selectedLocationStorage,
                    unitPreferenceStorage = unitPreferenceStorage,
                    layoutPreferenceStorage = layoutPreferenceStorage,
                    effectsPreferenceStorage = effectsPreferenceStorage,
                    themePreferenceStorage = themePreferenceStorage,
                    savedLocationStorage = savedLocationStorage,
                    forecastCacheStorage = forecastCacheStorage,
                    weatherRepository = InstalledForecastRepositoryFactory.create(
                        storage = forecastCacheStorage,
                    ),
                )
            }
            appStateHolder = stateHolder
            OxygenApp(
                stateHolder = stateHolder,
                onRequestLocationPermission = { attempt ->
                    if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        stateHolder.onLocationPermissionResult(attempt, LocationPermissionResult.Granted)
                    } else if (permissionAttempt == null) {
                        permissionAttempt = attempt
                        permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    } else {
                        stateHolder.onLocationPermissionResult(attempt, LocationPermissionResult.Unavailable)
                    }
                },
                motionPreferenceSource = AndroidMotionPreferenceSource,
            )
        }
    }

    override fun onStop() {
        permissionAttempt = null
        appStateHolder?.cancelDeviceLocation()
        super.onStop()
    }

    override fun onDestroy() {
        permissionAttempt = null
        appStateHolder?.cancelDeviceLocation()
        appStateHolder?.setOnStateChanged { }
        appStateHolder = null
        super.onDestroy()
    }
}

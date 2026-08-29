package com.oxygen.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.oxygen.weather.app.DataStoreSelectedLocationStorage
import com.oxygen.weather.app.OxygenApp
import com.oxygen.weather.app.OxygenAppStateHolder
import com.oxygen.weather.core.provider.cache.CachedWeatherRepository
import com.oxygen.weather.core.provider.cache.room.RoomForecastCacheStorageFactory
import com.oxygen.weather.core.provider.openmeteo.OpenMeteoWeatherRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val forecastCacheStorage = remember { RoomForecastCacheStorageFactory.create(this) }
            val selectedLocationStorage = remember { DataStoreSelectedLocationStorage(this) }
            val stateHolder = remember {
                OxygenAppStateHolder(
                    selectedLocationStorage = selectedLocationStorage,
                    forecastCacheStorage = forecastCacheStorage,
                    weatherRepository = CachedWeatherRepository(
                        upstream = OpenMeteoWeatherRepository(),
                        storage = forecastCacheStorage,
                    ),
                )
            }
            OxygenApp(
                stateHolder = stateHolder,
            )
        }
    }
}

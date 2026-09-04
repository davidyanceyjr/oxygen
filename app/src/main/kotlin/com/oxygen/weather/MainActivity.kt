package com.oxygen.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.oxygen.weather.app.DataStoreSelectedLocationStorage
import com.oxygen.weather.app.InstalledForecastRepositoryFactory
import com.oxygen.weather.app.OxygenApp
import com.oxygen.weather.app.OxygenAppStateHolder
import com.oxygen.weather.core.provider.cache.room.RoomForecastCacheStorageFactory
import com.oxygen.weather.core.provider.cache.room.RoomSavedLocationStorageFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val forecastCacheStorage = remember { RoomForecastCacheStorageFactory.create(this) }
            val savedLocationStorage = remember { RoomSavedLocationStorageFactory.create(this) }
            val selectedLocationStorage = remember { DataStoreSelectedLocationStorage(this) }
            val stateHolder = remember {
                OxygenAppStateHolder(
                    selectedLocationStorage = selectedLocationStorage,
                    savedLocationStorage = savedLocationStorage,
                    forecastCacheStorage = forecastCacheStorage,
                    weatherRepository = InstalledForecastRepositoryFactory.create(
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

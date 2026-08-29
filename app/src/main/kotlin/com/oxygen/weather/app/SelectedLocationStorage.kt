package com.oxygen.weather.app

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherLocation
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface SelectedLocationStorage {
    fun readSelectedLocation(): WeatherLocation?
    fun writeSelectedLocation(location: WeatherLocation)
}

object EmptySelectedLocationStorage : SelectedLocationStorage {
    override fun readSelectedLocation(): WeatherLocation? = null
    override fun writeSelectedLocation(location: WeatherLocation) = Unit
}

class DataStoreSelectedLocationStorage(
    context: Context,
) : SelectedLocationStorage {
    private val dataStore = context.applicationContext.selectedLocationDataStore

    override fun readSelectedLocation(): WeatherLocation? = runBlocking {
        try {
            val preferences = dataStore.data.first()
            val id = preferences[SelectedLocationKeys.Id]?.takeIf { it.isNotBlank() } ?: return@runBlocking null
            val displayName = preferences[SelectedLocationKeys.DisplayName]?.takeIf { it.isNotBlank() }
                ?: return@runBlocking null
            val latitude = preferences[SelectedLocationKeys.Latitude] ?: return@runBlocking null
            val longitude = preferences[SelectedLocationKeys.Longitude] ?: return@runBlocking null
            val zoneIdValue = preferences[SelectedLocationKeys.ZoneId]?.takeIf { it.isNotBlank() }
                ?: return@runBlocking null

            WeatherLocation(
                id = LocationId(id),
                displayName = displayName,
                point = GeoPoint(latitude, longitude),
                elevationMeters = preferences[SelectedLocationKeys.ElevationMeters],
                zoneId = ZoneId.of(zoneIdValue),
            )
        } catch (_: RuntimeException) {
            null
        }
    }

    override fun writeSelectedLocation(location: WeatherLocation) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[SelectedLocationKeys.Id] = location.id.value
                preferences[SelectedLocationKeys.DisplayName] = location.displayName
                preferences[SelectedLocationKeys.Latitude] = location.point.latitude
                preferences[SelectedLocationKeys.Longitude] = location.point.longitude
                preferences[SelectedLocationKeys.ZoneId] = location.zoneId.id
                location.elevationMeters?.let {
                    preferences[SelectedLocationKeys.ElevationMeters] = it
                } ?: preferences.remove(SelectedLocationKeys.ElevationMeters)
            }
        }
    }
}

private val Context.selectedLocationDataStore by preferencesDataStore(
    name = "oxygen_selected_location",
)

private object SelectedLocationKeys {
    val Id = stringPreferencesKey("location_id")
    val DisplayName = stringPreferencesKey("display_name")
    val Latitude = doublePreferencesKey("latitude")
    val Longitude = doublePreferencesKey("longitude")
    val ElevationMeters = doublePreferencesKey("elevation_meters")
    val ZoneId = stringPreferencesKey("zone_id")
}

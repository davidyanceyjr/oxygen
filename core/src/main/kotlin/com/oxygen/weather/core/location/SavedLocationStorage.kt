package com.oxygen.weather.core.location

import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherLocation

interface SavedLocationStorage {
    fun saveLocation(location: WeatherLocation)
    fun listLocations(): List<WeatherLocation>
    fun removeLocation(locationId: LocationId)
}

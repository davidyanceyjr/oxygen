package com.oxygen.weather.app

import com.oxygen.weather.core.model.WeatherLocation

class OxygenAppStateHolder(
    selectedLocation: WeatherLocation? = null,
) {
    var presentationState: OxygenAppPresentationState = if (selectedLocation == null) {
        OxygenAppPresentationState(
            screen = OxygenAppScreen.FirstRunLocationEntry(),
            selectedLocation = null,
        )
    } else {
        OxygenAppPresentationState(
            screen = OxygenAppScreen.Home,
            selectedLocation = selectedLocation,
        )
    }
        private set

    private var pendingCommand: OxygenAppCommand? = null

    fun onManualLocationQueryChanged(query: String) {
        updateFirstRunState { it.copy(query = query, message = null) }
    }

    fun onManualLocationSearchSubmitted() {
        val firstRun = presentationState.screen as? OxygenAppScreen.FirstRunLocationEntry ?: return
        val submittedQuery = firstRun.query.trim()
        updateFirstRunState {
            if (submittedQuery.isEmpty()) {
                it.copy(
                    submittedQuery = null,
                    message = FirstRunLocationMessage.EnterPlaceName,
                    isSearching = false,
                    hasResults = false,
                )
            } else {
                it.copy(
                    submittedQuery = submittedQuery,
                    message = FirstRunLocationMessage.SearchNotConnected,
                    isSearching = false,
                    hasResults = false,
                )
            }
        }
    }

    fun onUseMyLocation() {
        pendingCommand = OxygenAppCommand.RequestLocationPermission
    }

    fun onLocationPermissionResult(result: LocationPermissionResult) {
        when (result) {
            LocationPermissionResult.Granted -> updateFirstRunState {
                it.copy(message = FirstRunLocationMessage.LocationLookupNotConnected)
            }
            LocationPermissionResult.Denied,
            LocationPermissionResult.Unavailable -> updateFirstRunState {
                it.copy(message = FirstRunLocationMessage.LocationPermissionOptional)
            }
        }
    }

    fun consumeNextCommand(): OxygenAppCommand? {
        val command = pendingCommand
        pendingCommand = null
        return command
    }

    private fun updateFirstRunState(update: (OxygenAppScreen.FirstRunLocationEntry) -> OxygenAppScreen.FirstRunLocationEntry) {
        val firstRun = presentationState.screen as? OxygenAppScreen.FirstRunLocationEntry
            ?: OxygenAppScreen.FirstRunLocationEntry()
        presentationState = presentationState.copy(
            screen = update(firstRun),
            selectedLocation = null,
        )
    }
}

data class OxygenAppPresentationState(
    val screen: OxygenAppScreen,
    val selectedLocation: WeatherLocation?,
) {
    val isShowingHome: Boolean = screen is OxygenAppScreen.Home && selectedLocation != null
    val usesSampleWeather: Boolean = false
}

sealed interface OxygenAppScreen {
    data class FirstRunLocationEntry(
        val query: String = "",
        val submittedQuery: String? = null,
        val message: FirstRunLocationMessage? = null,
        val isSearching: Boolean = false,
        val hasResults: Boolean = false,
        val title: String = "Choose a location",
        val searchLabel: String = "Search for a location",
        val searchActionLabel: String = "Search",
        val useMyLocationLabel: String = "Use my location",
    ) : OxygenAppScreen

    data object Home : OxygenAppScreen
}

enum class FirstRunLocationMessage(
    val text: String,
) {
    EnterPlaceName("Enter a place name to search."),
    SearchNotConnected("Location search is not connected yet in this slice."),
    LocationPermissionOptional("Location permission is optional. You can search for a place instead."),
    LocationLookupNotConnected("Device location lookup is not connected yet in this slice."),
}

enum class LocationPermissionResult {
    Granted,
    Denied,
    Unavailable,
}

enum class OxygenAppCommand {
    RequestLocationPermission,
}

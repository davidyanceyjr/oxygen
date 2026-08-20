package com.oxygen.weather.app

import com.oxygen.weather.core.model.GeocodingLocationCandidate
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.GeocodingError
import com.oxygen.weather.core.provider.GeocodingRepository
import com.oxygen.weather.core.provider.GeocodingRepositoryResult
import com.oxygen.weather.core.provider.openmeteo.OpenMeteoGeocodingRepository
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class OxygenAppStateHolder(
    selectedLocation: WeatherLocation? = null,
    private val geocodingRepository: GeocodingRepository = OpenMeteoGeocodingRepository(),
    private val searchExecutor: Executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "oxygen-geocoding-search").apply { isDaemon = true }
    },
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
    private var onStateChanged: ((OxygenAppPresentationState) -> Unit)? = null

    fun setOnStateChanged(listener: (OxygenAppPresentationState) -> Unit) {
        onStateChanged = listener
    }

    fun onManualLocationQueryChanged(query: String) {
        updateFirstRunState {
            it.copy(
                query = query,
                message = null,
                searchState = ManualLocationSearchState.Idle,
            )
        }
    }

    fun onManualLocationSearchSubmitted() {
        val firstRun = presentationState.screen as? OxygenAppScreen.FirstRunLocationEntry ?: return
        val submittedQuery = firstRun.query.trim()
        startManualLocationSearch(submittedQuery)
    }

    fun onManualLocationSearchRetry() {
        val firstRun = presentationState.screen as? OxygenAppScreen.FirstRunLocationEntry ?: return
        startManualLocationSearch(firstRun.submittedQuery.orEmpty())
    }

    fun onManualLocationCandidateSelected(candidateId: LocationId) {
        val firstRun = presentationState.screen as? OxygenAppScreen.FirstRunLocationEntry ?: return
        val results = firstRun.searchState as? ManualLocationSearchState.Results ?: return
        val selected = results.candidates.firstOrNull { it.id == candidateId } ?: return
        updateFirstRunState {
            it.copy(message = FirstRunLocationMessage.LocationSelectedForNextSlice)
        }
        presentationState = presentationState.copy(selectedLocation = selected.location)
        publishState()
    }

    private fun startManualLocationSearch(submittedQuery: String) {
        if (submittedQuery.isEmpty()) {
            updateFirstRunState {
                it.copy(
                    submittedQuery = null,
                    message = FirstRunLocationMessage.EnterPlaceName,
                    searchState = ManualLocationSearchState.Idle,
                )
            }
            return
        }

        updateFirstRunState {
            it.copy(
                submittedQuery = submittedQuery,
                message = null,
                searchState = ManualLocationSearchState.Loading(submittedQuery),
            )
        }

        searchExecutor.execute {
            geocodingRepository.search(query = submittedQuery).forEach { result ->
                applyManualLocationSearchResult(submittedQuery, result)
            }
        }
    }

    private fun applyManualLocationSearchResult(
        query: String,
        result: GeocodingRepositoryResult,
    ) {
        updateFirstRunState {
            when (result) {
                GeocodingRepositoryResult.Loading -> it.copy(
                    submittedQuery = query,
                    message = null,
                    searchState = ManualLocationSearchState.Loading(query),
                )
                GeocodingRepositoryResult.Empty -> it.copy(
                    submittedQuery = query,
                    message = FirstRunLocationMessage.SearchNoResults,
                    searchState = ManualLocationSearchState.Empty(query),
                )
                is GeocodingRepositoryResult.Success -> it.copy(
                    submittedQuery = query,
                    message = null,
                    searchState = ManualLocationSearchState.Results(
                        query = query,
                        candidates = result.candidates.map { candidate -> candidate.toManualLocationCandidate() },
                    ),
                )
                is GeocodingRepositoryResult.Failure -> {
                    val message = result.error.toFirstRunLocationMessage()
                    it.copy(
                        submittedQuery = query,
                        message = message,
                        searchState = ManualLocationSearchState.Failure(
                            query = query,
                            message = message,
                            canRetry = result.error !is GeocodingError.InvalidQuery,
                        ),
                    )
                }
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
        )
        publishState()
    }

    private fun publishState() {
        onStateChanged?.invoke(presentationState)
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
        val searchState: ManualLocationSearchState = ManualLocationSearchState.Idle,
        val title: String = "Choose a location",
        val searchLabel: String = "Search for a location",
        val searchActionLabel: String = "Search",
        val useMyLocationLabel: String = "Use my location",
        val retryLabel: String = "Retry",
        val geocodingDisclosure: String = "Location search by Open-Meteo, based on GeoNames data.",
        val geocodingPrivacyNote: String = "Your typed search is sent to Open-Meteo to find matching places.",
    ) : OxygenAppScreen

    data object Home : OxygenAppScreen
}

sealed interface ManualLocationSearchState {
    data object Idle : ManualLocationSearchState

    data class Loading(
        val query: String,
    ) : ManualLocationSearchState

    data class Results(
        val query: String,
        val candidates: List<ManualLocationCandidate>,
    ) : ManualLocationSearchState

    data class Empty(
        val query: String,
        val canRetry: Boolean = true,
    ) : ManualLocationSearchState

    data class Failure(
        val query: String,
        val message: FirstRunLocationMessage,
        val canRetry: Boolean,
    ) : ManualLocationSearchState
}

data class ManualLocationCandidate(
    val id: LocationId,
    val title: String,
    val subtitle: String,
    val coordinateText: String,
    val timezoneText: String,
    val location: WeatherLocation,
)

enum class FirstRunLocationMessage(
    val text: String,
) {
    EnterPlaceName("Enter a place name to search."),
    SearchNoResults("No matching places were found. Check the spelling or try a nearby city."),
    SearchNetworkUnavailable("Location search is offline or the network is unavailable."),
    SearchRateLimited("Location search is temporarily rate-limited. Try again shortly."),
    SearchProviderUnavailable("Location search is temporarily unavailable."),
    SearchInvalidResponse("Location search returned a response Oxygen could not read."),
    SearchRejected("Location search rejected that request. Try a more specific place name."),
    SearchUnexpectedFailure("Location search failed unexpectedly. Try again."),
    LocationPermissionOptional("Location permission is optional. You can search for a place instead."),
    LocationLookupNotConnected("Device location lookup is not connected yet in this slice."),
    LocationSelectedForNextSlice("Location selected. Home handoff starts in the next slice."),
}

enum class LocationPermissionResult {
    Granted,
    Denied,
    Unavailable,
}

enum class OxygenAppCommand {
    RequestLocationPermission,
}

private fun GeocodingLocationCandidate.toManualLocationCandidate(): ManualLocationCandidate {
    val subtitleParts = administrativeAreas + country
    return ManualLocationCandidate(
        id = locationId,
        title = displayName,
        subtitle = subtitleParts.distinct().joinToString(", "),
        coordinateText = "${point.latitude.formatCoordinate()}, ${point.longitude.formatCoordinate()}",
        timezoneText = zoneId.id,
        location = location,
    )
}

private fun GeocodingError.toFirstRunLocationMessage(): FirstRunLocationMessage =
    when (this) {
        GeocodingError.InvalidQuery -> FirstRunLocationMessage.EnterPlaceName
        GeocodingError.NetworkUnavailable -> FirstRunLocationMessage.SearchNetworkUnavailable
        is GeocodingError.RateLimited -> FirstRunLocationMessage.SearchRateLimited
        is GeocodingError.ProviderUnavailable -> FirstRunLocationMessage.SearchProviderUnavailable
        is GeocodingError.InvalidResponse -> FirstRunLocationMessage.SearchInvalidResponse
        is GeocodingError.ProviderRejectedRequest -> FirstRunLocationMessage.SearchRejected
        is GeocodingError.UnexpectedProviderFailure -> FirstRunLocationMessage.SearchUnexpectedFailure
    }

private fun Double.formatCoordinate(): String = String.format(Locale.US, "%.4f", this)

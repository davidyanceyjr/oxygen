package com.oxygen.weather.app

import com.oxygen.weather.core.model.GeocodingLocationCandidate
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.GeocodingError
import com.oxygen.weather.core.provider.GeocodingRepository
import com.oxygen.weather.core.provider.GeocodingRepositoryResult
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import com.oxygen.weather.core.provider.openmeteo.OpenMeteoGeocodingRepository
import com.oxygen.weather.core.provider.openmeteo.OpenMeteoWeatherRepository
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class OxygenAppStateHolder(
    selectedLocation: WeatherLocation? = null,
    private val geocodingRepository: GeocodingRepository = OpenMeteoGeocodingRepository(),
    private val weatherRepository: WeatherRepository = OpenMeteoWeatherRepository(),
    private val searchExecutor: Executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "oxygen-geocoding-search").apply { isDaemon = true }
    },
    private val forecastExecutor: Executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "oxygen-forecast-refresh").apply { isDaemon = true }
    },
) {
    @Volatile
    var presentationState: OxygenAppPresentationState = if (selectedLocation == null) {
        OxygenAppPresentationState(
            screen = OxygenAppScreen.FirstRunLocationEntry(),
            selectedLocation = null,
        )
    } else {
        OxygenAppPresentationState(
            screen = OxygenAppScreen.Home(
                forecast = HomeForecastPresentationState.Loading.from(selectedLocation),
            ),
            selectedLocation = selectedLocation,
        )
    }
        private set

    private var pendingCommand: OxygenAppCommand? = null
    private var onStateChanged: ((OxygenAppPresentationState) -> Unit)? = null
    private var activeForecastRequestId = 0L

    init {
        selectedLocation?.let(::startHomeForecastLoad)
    }

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
        startHomeForecastLoad(selected.location)
    }

    fun onHomeForecastRetry() {
        val selectedLocation = presentationState.selectedLocation ?: return
        startHomeForecastLoad(selectedLocation)
    }

    fun onOpenAbout() {
        val currentScreen = presentationState.screen
        if (currentScreen is OxygenAppScreen.About) return

        presentationState = presentationState.copy(
            screen = OxygenAppScreen.About(
                returnScreen = currentScreen,
                selectedSurface = null,
            ),
        )
        publishState()
    }

    fun onAboutSurfaceSelected(surfaceId: AboutSurfaceId) {
        updateAboutState {
            it.copy(selectedSurface = surfaceId)
        }
    }

    fun onAboutBack() {
        val about = presentationState.screen as? OxygenAppScreen.About ?: return
        if (about.selectedSurface != null) {
            presentationState = presentationState.copy(
                screen = about.copy(selectedSurface = null),
            )
        } else {
            presentationState = presentationState.copy(screen = about.returnScreen)
        }
        publishState()
    }

    @Synchronized
    private fun startHomeForecastLoad(location: WeatherLocation) {
        val requestId = nextForecastRequestId()
        presentationState = OxygenAppPresentationState(
            screen = OxygenAppScreen.Home(
                forecast = HomeForecastPresentationState.Loading.from(location),
            ),
            selectedLocation = location,
        )
        publishState()

        forecastExecutor.execute {
            weatherRepository.refresh(location).forEach { result ->
                applyHomeForecastResult(
                    requestId = requestId,
                    location = location,
                    result = result,
                )
            }
        }
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
        val screen = presentationState.screen
        val firstRun = screen.visibleOrReturnScreen() as? OxygenAppScreen.FirstRunLocationEntry ?: return
        if (firstRun.submittedQuery != query) return

        val nextFirstRun = when (result) {
            GeocodingRepositoryResult.Loading -> firstRun.copy(
                submittedQuery = query,
                message = null,
                searchState = ManualLocationSearchState.Loading(query),
            )
            GeocodingRepositoryResult.Empty -> firstRun.copy(
                submittedQuery = query,
                message = FirstRunLocationMessage.SearchNoResults,
                searchState = ManualLocationSearchState.Empty(query),
            )
            is GeocodingRepositoryResult.Success -> firstRun.copy(
                submittedQuery = query,
                message = null,
                searchState = ManualLocationSearchState.Results(
                    query = query,
                    candidates = result.candidates.map { candidate -> candidate.toManualLocationCandidate() },
                ),
            )
            is GeocodingRepositoryResult.Failure -> {
                val message = result.error.toFirstRunLocationMessage()
                firstRun.copy(
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
        presentationState = presentationState.copy(
            screen = screen.withVisibleOrReturnScreen(nextFirstRun),
        )
        publishState()
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
        val screen = presentationState.screen
        val firstRun = screen.visibleOrReturnScreen() as? OxygenAppScreen.FirstRunLocationEntry
            ?: return
        presentationState = presentationState.copy(
            screen = screen.withVisibleOrReturnScreen(update(firstRun)),
        )
        publishState()
    }

    private fun updateAboutState(update: (OxygenAppScreen.About) -> OxygenAppScreen.About) {
        val about = presentationState.screen as? OxygenAppScreen.About ?: return
        presentationState = presentationState.copy(screen = update(about))
        publishState()
    }

    private fun publishState() {
        onStateChanged?.invoke(presentationState)
    }

    @Synchronized
    private fun nextForecastRequestId(): Long {
        activeForecastRequestId += 1
        return activeForecastRequestId
    }

    @Synchronized
    private fun applyHomeForecastResult(
        requestId: Long,
        location: WeatherLocation,
        result: WeatherRepositoryResult,
    ) {
        if (requestId != activeForecastRequestId) return
        if (presentationState.selectedLocation != location) return

        val forecast = when (result) {
            WeatherRepositoryResult.Loading -> HomeForecastPresentationState.Loading.from(location)
            is WeatherRepositoryResult.Failure -> HomeForecastPresentationState.NoCacheError.from(
                location = location,
                message = result.error.toHomeForecastMessage(),
            )
            is WeatherRepositoryResult.Success -> HomeForecastPresentationState.ForecastReady.from(
                location = location,
                weather = result.weather,
            )
        }

        val nextHome = OxygenAppScreen.Home(forecast = forecast)
        val currentScreen = presentationState.screen
        presentationState = OxygenAppPresentationState(
            screen = currentScreen.withVisibleOrReturnScreen(nextHome),
            selectedLocation = location,
        )
        publishState()
    }
}

data class OxygenAppPresentationState(
    val screen: OxygenAppScreen,
    val selectedLocation: WeatherLocation?,
) {
    val isShowingHome: Boolean = screen is OxygenAppScreen.Home && selectedLocation != null
    val usesScaffoldWeather: Boolean = false
}

sealed interface HomeForecastPresentationState {
    val location: WeatherLocation
    val title: String
    val subtitle: String
    val forecastDisclosure: String
    val forecastPrivacyNote: String

    data class Loading(
        override val location: WeatherLocation,
        override val title: String,
        override val subtitle: String,
        val statusText: String,
        override val forecastDisclosure: String = FORECAST_DISCLOSURE,
        override val forecastPrivacyNote: String = FORECAST_PRIVACY_NOTE,
    ) : HomeForecastPresentationState {
        companion object {
            fun from(location: WeatherLocation): Loading =
                Loading(
                    location = location,
                    title = location.displayName,
                    subtitle = location.forecastSubtitle(),
                    statusText = "Loading weather for ${location.displayName}",
                )
        }
    }

    data class NoCacheError(
        override val location: WeatherLocation,
        override val title: String,
        override val subtitle: String,
        val message: HomeForecastMessage,
        val retryLabel: String = "Retry",
        val canRetry: Boolean = true,
        override val forecastDisclosure: String = FORECAST_DISCLOSURE,
        override val forecastPrivacyNote: String = FORECAST_PRIVACY_NOTE,
    ) : HomeForecastPresentationState {
        companion object {
            fun from(
                location: WeatherLocation,
                message: HomeForecastMessage,
            ): NoCacheError =
                NoCacheError(
                    location = location,
                    title = location.displayName,
                    subtitle = location.forecastSubtitle(),
                    message = message,
                )
        }
    }

    data class ForecastReady(
        override val location: WeatherLocation,
        override val title: String,
        override val subtitle: String,
        val dashboard: HomeSuccessPresentation,
        override val forecastDisclosure: String = FORECAST_DISCLOSURE,
        override val forecastPrivacyNote: String = FORECAST_PRIVACY_NOTE,
    ) : HomeForecastPresentationState {
        companion object {
            fun from(
                location: WeatherLocation,
                weather: WeatherBundle,
            ): ForecastReady {
                val dashboard = weather.toHomeSuccessPresentation(selectedLocation = location)
                return ForecastReady(
                    location = location,
                    title = location.displayName,
                    subtitle = location.forecastSubtitle(),
                    dashboard = dashboard,
                    forecastDisclosure = dashboard.source.toForecastDisclosure(),
                    forecastPrivacyNote = dashboard.source.toForecastPrivacyNote(),
                )
            }
        }
    }
}

enum class HomeForecastMessage(
    val text: String,
) {
    NetworkUnavailable("Weather is offline or the network is unavailable. No cached forecast is available yet."),
    RateLimited("Weather updates are temporarily rate-limited. Try again shortly."),
    ProviderUnavailable("Weather updates are temporarily unavailable. Try again shortly."),
    InvalidResponse("Weather data returned in a form Oxygen could not read. Try again later."),
    RejectedRequest("Weather updates rejected that location request. Try again later."),
    LocalCacheFailure("Weather data was received but could not be saved locally. Try again."),
    UnexpectedFailure("Weather update failed unexpectedly. Try again."),
}

private const val FORECAST_DISCLOSURE = "Weather data by Open-Meteo."
private const val FORECAST_PRIVACY_NOTE =
    "Forecast requests send this location's coordinates and timezone to Open-Meteo."
private const val UNAVAILABLE_SOURCE_NAME = "Source unavailable"
private const val UNAVAILABLE_FORECAST_DISCLOSURE = "Weather data source unavailable."
private const val UNAVAILABLE_FORECAST_PRIVACY_NOTE =
    "Forecast request destination is unavailable from the returned data."

private fun HomeSourcePresentation.toForecastDisclosure(): String {
    val source = sourceName.trim()
    return if (source.isEmpty() || source == UNAVAILABLE_SOURCE_NAME) {
        UNAVAILABLE_FORECAST_DISCLOSURE
    } else {
        "Weather data by $source."
    }
}

private fun HomeSourcePresentation.toForecastPrivacyNote(): String {
    val source = sourceName.trim()
    return if (source.isEmpty() || source == UNAVAILABLE_SOURCE_NAME) {
        UNAVAILABLE_FORECAST_PRIVACY_NOTE
    } else {
        "Forecast requests send this location's coordinates and timezone to $source."
    }
}

internal fun WeatherLocation.forecastSubtitle(): String =
    listOf(
        "${point.latitude.formatCoordinate()}, ${point.longitude.formatCoordinate()}",
        zoneId.id,
    ).joinToString(" | ")

private fun ForecastError.toHomeForecastMessage(): HomeForecastMessage =
    when (this) {
        ForecastError.NetworkUnavailable -> HomeForecastMessage.NetworkUnavailable
        is ForecastError.RateLimited -> HomeForecastMessage.RateLimited
        is ForecastError.ProviderUnavailable -> HomeForecastMessage.ProviderUnavailable
        is ForecastError.InvalidResponse -> HomeForecastMessage.InvalidResponse
        is ForecastError.ProviderRejectedRequest -> HomeForecastMessage.RejectedRequest
        ForecastError.LocalCacheFailure -> HomeForecastMessage.LocalCacheFailure
        is ForecastError.UnexpectedProviderFailure -> HomeForecastMessage.UnexpectedFailure
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

    data class Home(
        val forecast: HomeForecastPresentationState,
    ) : OxygenAppScreen

    data class About(
        val returnScreen: OxygenAppScreen,
        val selectedSurface: AboutSurfaceId? = null,
        val title: String = "Settings / About",
        val surfaceOptions: List<AboutSurfaceId> = aboutSurfaceOptions,
    ) : OxygenAppScreen {
        val surfaceState: AboutSurfaceState
            get() = aboutSurfaceState(selectedSurface)
    }
}

private fun OxygenAppScreen.visibleOrReturnScreen(): OxygenAppScreen =
    when (this) {
        is OxygenAppScreen.About -> returnScreen
        else -> this
    }

private fun OxygenAppScreen.withVisibleOrReturnScreen(nextScreen: OxygenAppScreen): OxygenAppScreen =
    when (this) {
        is OxygenAppScreen.About -> copy(returnScreen = nextScreen)
        else -> nextScreen
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

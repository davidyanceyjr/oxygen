package com.oxygen.weather.app.ui.home

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.pressBack
import com.oxygen.weather.app.HomeForecastMessage
import com.oxygen.weather.app.HomeForecastPresentationState
import com.oxygen.weather.app.HomeMetricIdentity
import com.oxygen.weather.app.HomeSuccessSection
import com.oxygen.weather.app.SettingsDestination
import com.oxygen.weather.app.ManualLocationCandidate
import com.oxygen.weather.app.ManualLocationSearchState
import com.oxygen.weather.app.OxygenApp
import com.oxygen.weather.app.OxygenAppScreen
import com.oxygen.weather.app.OxygenAppStateHolder
import com.oxygen.weather.app.DeviceLocationProgress
import com.oxygen.weather.app.DeviceLocationSource
import com.oxygen.weather.app.DeviceLocationResult
import com.oxygen.weather.app.LocationCancellation
import com.oxygen.weather.app.LocationPermissionResult
import com.oxygen.weather.app.FirstRunLocationMessage
import com.oxygen.weather.app.SavedLocationsMessage
import com.oxygen.weather.app.SavedLocationsPresentationState
import com.oxygen.weather.app.ui.settings.SettingsScreen
import com.oxygen.weather.app.ui.components.WeatherConditionMark
import com.oxygen.weather.app.ui.firstrun.FirstRunLocationEntryScreen
import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.app.ui.theme.LayoutPreset
import com.oxygen.weather.app.ui.theme.OxygenAppearance
import com.oxygen.weather.app.ui.theme.OxygenTheme
import com.oxygen.weather.app.ui.theme.OxygenThemeId
import com.oxygen.weather.core.location.SavedLocationStorage
import com.oxygen.weather.core.model.AlertSeverity
import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherAlert
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.model.Wind
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.ForecastFreshness
import com.oxygen.weather.core.provider.AlertLookupStatus
import com.oxygen.weather.core.provider.AlertSuccessMetadata
import com.oxygen.weather.core.provider.GeocodingRepository
import com.oxygen.weather.core.provider.GeocodingRepositoryResult
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Executor
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeDashboardUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun paperBaselineNowEffectsOff() {
        val location = weatherLocation(name = "Baseline Paper Rendering City")
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = location,
            weather = fullWeatherBundle(location),
        )

        composeRule.setHomeContent(
            state = state,
            widthDp = 360,
            heightDp = 640,
            fontScale = 1.3f,
            appearance = OxygenAppearance(
                theme = OxygenThemeId.PAPER,
                effects = EffectsLevel.OFF,
            ),
            themeId = OxygenThemeId.PAPER,
        )

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 4")
        composeRule.onNodeWithText("Baseline Paper Rendering City").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("65 deg F").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithTag("home-weather-scene").assertCountEquals(0)
        composeRule.writeSemanticsArtifact("paper-baseline-now-effects-off-semantics.txt")
        composeRule.writeScreenshotArtifact("paper-baseline-now-effects-off.png")
    }

    @Test
    fun paperStandardHomePreservesMeaningAcrossPagesEffectsOff() {
        val location = weatherLocation(
            name = "A Very Long Paper Location Name Near The Lakefront, Wisconsin, United States",
        )
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = location,
            weather = fullWeatherBundle(location).copy(
                alerts = listOf(fullWeatherBundle(location).alerts.single().copy(severity = AlertSeverity.SEVERE)),
            ),
            freshness = ForecastFreshness.StaleAfterFailedRefresh(
                staleAge = Duration.ofMinutes(45),
                refreshFailure = ForecastError.NetworkUnavailable,
            ),
        )
        val appearance = OxygenAppearance(
            theme = OxygenThemeId.PAPER,
            effects = EffectsLevel.OFF,
        )

        val themeState = mutableStateOf(OxygenThemeId.OXYGEN)
        composeRule.setThemedHomeContent(
            state = state,
            appearance = appearance.copy(theme = OxygenThemeId.OXYGEN),
            themeState = themeState,
        )
        composeRule.waitForIdle()
        val oxygenContract = composeRule.homeSemanticContract()
        composeRule.runOnIdle { themeState.value = OxygenThemeId.PAPER }
        composeRule.waitForIdle()
        val paperContract = composeRule.homeSemanticContract()
        assertEquals(oxygenContract, paperContract)
        composeRule.assertPaperContrastRoles()

        composeRule.onNodeWithTag("home-section-location").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("65 deg F").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home-current-mark").assertIsDisplayed()
        composeRule.assertPaperMarkHasMeasuredContrast()
        composeRule.onNodeWithText("Severity: Severe").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Refresh failed: Refresh could not reach the weather service or network.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onAllNodesWithTag("home-weather-scene").assertCountEquals(0)
        composeRule.assertMinimumTouchTarget(
            "home-page-tab-now",
            "home-page-tab-hourly",
            "home-page-tab-daily",
            "home-page-tab-details",
            "home-refresh",
            "home-change-location",
            "home-about-entry",
        )

        composeRule.onNodeWithTag("home-page-tab-hourly").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Hourly")
        composeRule.onNodeWithTag("home-page-tab-daily").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Daily")
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Details")
        composeRule.onNodeWithText("Open-Meteo").performScrollTo().assertIsDisplayed()
        composeRule.writeSemanticsArtifact("paper-standard-effects-off-semantics.txt")
        composeRule.writeScreenshotArtifact("paper-standard-effects-off.png")
    }

    @Test
    fun paperSimpleHomePreservesMeaningAndForecastChoicesEffectsOff() {
        val location = weatherLocation(name = "Paper Simple Forecast City")
        val repository = RecordingWeatherRepository(
            listOf(
                WeatherRepositoryResult.Success(
                    weather = fullWeatherBundle(location).copy(
                        alerts = listOf(fullWeatherBundle(location).alerts.single().copy(severity = AlertSeverity.SEVERE)),
                    ),
                    freshness = ForecastFreshness.StaleAfterFailedRefresh(
                        staleAge = Duration.ofMinutes(45),
                        refreshFailure = ForecastError.NetworkUnavailable,
                    ),
                    alertStatus = AlertLookupStatus.Available(
                        AlertSuccessMetadata(
                            requestPoint = location.point,
                            providerId = "nws",
                            fetchedAt = Instant.parse("2026-08-22T15:05:00Z"),
                        ),
                    ),
                ),
            ),
        )
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            initialLayout = LayoutPreset.SIMPLE,
            forecastExecutor = DirectExecutor,
        )

        composeRule.setCompactOxygenAppContent(
            stateHolder = holder,
            appearance = OxygenAppearance(
                theme = OxygenThemeId.PAPER,
                layout = LayoutPreset.SIMPLE,
                effects = EffectsLevel.OFF,
            ),
        )
        composeRule.waitForIdle()
        val requestCountAfterReady = repository.locations.size
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 2")
        composeRule.onNodeWithText("Paper Simple Forecast City").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Severity: Severe").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Open-Meteo").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home-page-tab-forecast").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-simple-forecast-hourly").assertIsSelected()
        composeRule.onNodeWithContentDescription("6 AM, Rain, 64 deg F, 60%").assertIsDisplayed()
        composeRule.onNodeWithTag("home-simple-forecast-daily").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-simple-forecast-daily").assertIsSelected()
        composeRule.onNodeWithContentDescription("Sat, Aug 22, Rain showers, High 73 deg F, Low 54 deg F, 40%")
            .assertIsDisplayed()
        composeRule.assertMinimumTouchTarget(
            "home-page-tab-now",
            "home-page-tab-forecast",
            "home-simple-forecast-hourly",
            "home-simple-forecast-daily",
        )
        assertEquals(requestCountAfterReady, repository.locations.size)
        composeRule.onNodeWithTag("home-page-tab-now").performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("home-weather-scene").assertCountEquals(0)
        composeRule.writeSemanticsArtifact("paper-simple-effects-off-semantics.txt")
        composeRule.writeScreenshotArtifact("paper-simple-effects-off.png")
    }

    @Test
    fun paperOperationalHomeStatesRemainReadableEffectsOff() {
        val location = weatherLocation(name = "Paper Operational City")
        var retryCount = 0
        val operationalState = mutableStateOf<HomeForecastPresentationState>(HomeForecastPresentationState.Loading.from(location))
        composeRule.setDynamicHomeContent(
            state = operationalState,
            appearance = OxygenAppearance(theme = OxygenThemeId.PAPER, effects = EffectsLevel.OFF),
            onRetry = { retryCount++ },
        )
        composeRule.onNodeWithText("Loading weather for Paper Operational City").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Weather data by Open-Meteo.").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithTag("home-weather-scene").assertCountEquals(0)

        composeRule.runOnIdle {
            operationalState.value = HomeForecastPresentationState.NoCacheError.from(
                location = location,
                message = HomeForecastMessage.NetworkUnavailable,
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(HomeForecastMessage.NetworkUnavailable.text).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performScrollTo().performClick()
        composeRule.onNodeWithText("Settings").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Change location").performScrollTo().assertIsDisplayed()
        composeRule.assertMinimumTouchTarget("home-change-location")
        assertEquals(1, retryCount)
        composeRule.writeSemanticsArtifact("paper-operational-effects-off-semantics.txt")
        composeRule.writeScreenshotArtifact("paper-operational-effects-off.png")
    }

    @Test
    fun deviceLookupShowsProgressDisablesDuplicatesAndCancelLeavesManualSearchUsable() {
        var pointCallback: ((DeviceLocationResult) -> Unit)? = null
        var sourceCalls = 0
        val holder = OxygenAppStateHolder(
            deviceLocationSource = DeviceLocationSource { callback ->
                sourceCalls++
                pointCallback = callback
                LocationCancellation { }
            },
        )
        composeRule.setCompactContent {
            OxygenApp(holder, onRequestLocationPermission = {
                holder.onLocationPermissionResult(it, LocationPermissionResult.Granted)
            })
        }
        composeRule.onNodeWithTag("location-entry-use-my-location").performClick()
        composeRule.onNodeWithTag("location-entry-use-my-location").assertIsNotEnabled()
        composeRule.onNodeWithText(DeviceLocationProgress.Locating.text).performScrollTo().assertIsDisplayed()
        composeRule.assertMinimumTouchTarget("location-entry-device-cancel", "location-entry-use-my-location")
        composeRule.onNodeWithTag("location-entry-device-cancel").performClick()
        composeRule.runOnIdle { pointCallback!!(DeviceLocationResult.Success(GeoPoint(43.0, -89.0))) }
        composeRule.onAllNodesWithTag("location-entry-device-progress").assertCountEquals(0)
        composeRule.onNodeWithTag("location-entry-search-field").performScrollTo().performTextInput("Madison")
        composeRule.runOnIdle {
            assertEquals("Madison", (holder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry).query)
            assertEquals(null, holder.presentationState.selectedLocation)
            assertEquals(1, sourceCalls)
        }
    }

    @Test
    fun deviceResolvingAndErrorRemainReadableAtLargeFontWithEffectsOff() {
        val state = mutableStateOf(OxygenAppScreen.FirstRunLocationEntry(
            deviceProgress = DeviceLocationProgress.Resolving,
        ))
        composeRule.setCompactContent(fontScale = 2f) {
            FirstRunLocationEntryScreen(
                state = state.value,
                selectedLocation = null,
                savedLocations = SavedLocationsPresentationState.NotLoaded,
                onQueryChanged = {}, onSearch = {}, onRetry = {}, onCandidateSelected = {},
                onSavedLocationSelected = {}, onUseMyLocation = {
                    state.value = state.value.copy(message = null, deviceProgress = DeviceLocationProgress.Locating)
                },
                onCancelDeviceLocation = {
                    state.value = state.value.copy(deviceProgress = null, message = FirstRunLocationMessage.DeviceTimezoneUnavailable)
                },
                onBack = {}, onOpenSettings = {},
            )
        }
        composeRule.onNodeWithText(DeviceLocationProgress.Resolving.text).performScrollTo().assertIsDisplayed()
        composeRule.assertMinimumTouchTarget("location-entry-device-cancel", "location-entry-search")
        composeRule.assertWithinRootBounds("location-entry-actions")
        composeRule.onNodeWithTag("location-entry-device-cancel").performClick()
        composeRule.onNodeWithText(FirstRunLocationMessage.DeviceTimezoneUnavailable.text).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("location-entry-use-my-location").performClick()
        composeRule.onNodeWithText(DeviceLocationProgress.Locating.text).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun freshSuccessRendersSemanticHomePagesAndPreservesDashboardContent() {
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = weatherLocation(),
            weather = fullWeatherBundle(weatherLocation()),
        )

        composeRule.setHomeContent(state)

        composeRule.onNodeWithTag("home-weather-scene").assertExists()
        listOf(
            "home-page-tab-now",
            "home-page-tab-hourly",
            "home-page-tab-daily",
            "home-page-tab-details",
            "home-section-location",
            "home-section-alert",
            "home-section-current",
            "home-section-precipitation",
        ).forEach { composeRule.onNodeWithTag(it).assertExists() }

        composeRule.assertSemanticsTreeOrder(
            "home-section-location",
            "home-section-current",
            "home-section-alert",
            "home-section-precipitation",
        )
        composeRule.assertNowHeroDominatesLocationChrome()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 4")
        composeRule.onAllNodesWithTag("home-page-previous").assertCountEquals(0)
        composeRule.onAllNodesWithTag("home-page-next").assertCountEquals(0)
        composeRule.onNodeWithText("65 deg F").assertIsDisplayed()
        composeRule.onNodeWithText("Rain showers").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Rain showers").assertExists()
        composeRule.onNodeWithText("Feels like 63 deg F").assertIsDisplayed()
        composeRule.onNodeWithText("H 73 deg F   L 54 deg F").assertIsDisplayed()
        composeRule.onNodeWithText("Updated 5:30 AM | Model estimate").assertExists()
        composeRule.onNodeWithTag("home-section-alert").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home-refresh").assertIsDisplayed()
        composeRule.onNodeWithText("Refresh").assertIsDisplayed()
        composeRule.onAllNodesWithText("Retry").assertCountEquals(0)

        composeRule.onNodeWithTag("home-page-tab-hourly").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Hourly")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 2 of 4")
        composeRule.onNodeWithTag("home-hourly-grid").assertIsDisplayed()
        composeRule.onNodeWithTag("home-page-tab-daily").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Daily")
        composeRule.onNodeWithTag("home-section-daily").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Sat, Aug 22, Rain showers, High 73 deg F, Low 54 deg F, 40%").assertExists()
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Details")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 4 of 4")
        composeRule.onAllNodesWithTag("home-page-next").assertCountEquals(0)
        composeRule.onNodeWithText("Fetched Aug 22, 7:00 AM CDT").assertExists()
        composeRule.onNodeWithText("Issued Aug 22, 6:45 AM CDT").assertExists()
        composeRule.onNodeWithText("Weather data by Open-Meteo.").assertExists()
        composeRule.onNodeWithTag("home-section-metrics").assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-sun").performScrollTo().assertIsDisplayed()
        assertEquals(
            listOf(
                HomeSuccessSection.LocationHeader,
                HomeSuccessSection.Current,
                HomeSuccessSection.Alerts,
                HomeSuccessSection.NearTermPrecipitation,
                HomeSuccessSection.Hourly,
                HomeSuccessSection.Daily,
                HomeSuccessSection.Metrics,
                HomeSuccessSection.Sun,
                HomeSuccessSection.Source,
                HomeSuccessSection.ProvenanceFooter,
            ),
            state.dashboard.sectionOrder,
        )
        composeRule.writeSemanticsArtifact("fresh-dashboard-semantics.txt")
    }

    @Test
    fun simpleLayoutIsReachableAndPreservesRequiredMeaning() {
        val location = weatherLocation(name = "Simple Reachable City")
        val repository = RecordingWeatherRepository(
            listOf(
                WeatherRepositoryResult.Success(
                    weather = fullWeatherBundle(location),
                    freshness = ForecastFreshness.StaleAfterFailedRefresh(
                        staleAge = Duration.ofMinutes(45),
                        refreshFailure = ForecastError.NetworkUnavailable,
                    ),
                    alertStatus = AlertLookupStatus.Available(
                        AlertSuccessMetadata(
                            requestPoint = location.point,
                            providerId = "nws",
                            fetchedAt = Instant.parse("2026-08-22T15:05:00Z"),
                        ),
                    ),
                ),
            ),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            forecastExecutor = DirectExecutor,
        )

        composeRule.setCompactContent(heightDp = 900, fontScale = 1.3f) {
            OxygenApp(
                stateHolder = stateHolder,
                appearance = OxygenAppearance(effects = EffectsLevel.OFF),
            )
        }
        composeRule.waitForIdle()
        val requestCountAfterReady = repository.locations.size

        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-destination-appearance").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-layout-standard").assertIsSelected()
        composeRule.onNodeWithTag("settings-layout-simple").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-layout-simple").assertIsSelected()
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 2")
        composeRule.onNodeWithTag("home-page-tab-now").assertIsDisplayed()
        composeRule.onNodeWithTag("home-page-tab-forecast").assertIsDisplayed()
        composeRule.onAllNodesWithTag("home-page-tab-hourly").assertCountEquals(0)
        composeRule.onAllNodesWithTag("home-page-tab-daily").assertCountEquals(0)
        composeRule.onAllNodesWithTag("home-page-tab-details").assertCountEquals(0)
        composeRule.onNodeWithText("Simple Reachable City").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("65 deg F").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Rain showers").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Feels like 63 deg F").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("H 73 deg F   L 54 deg F").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-stale").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Refresh failed: Refresh could not reach the weather service or network.")
            .assertExists()
        composeRule.onNodeWithTag("home-section-alert").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Severity: Moderate").assertIsDisplayed()
        composeRule.onNodeWithTag("home-alert-details").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home-alert-source-link").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-precipitation").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Open-Meteo").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Fetched Aug 22, 7:00 AM CDT").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Model estimate").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Forecast requests send this location's coordinates and timezone to Open-Meteo.")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("home-page-tab-forecast").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Forecast")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 2 of 2")
        composeRule.onNodeWithTag("home-simple-forecast-hourly").assertIsSelected()
        composeRule.onNodeWithTag("home-simple-forecast-daily").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("6 AM, Rain, 64 deg F, 60%").assertIsDisplayed()
        composeRule.onNodeWithTag("home-simple-forecast-daily").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-simple-forecast-daily").assertIsSelected()
        composeRule.onNodeWithContentDescription("Sat, Aug 22, Rain showers, High 73 deg F, Low 54 deg F, 40%")
            .assertIsDisplayed()
        assertEquals(requestCountAfterReady, repository.locations.size)
    }

    @Test
    fun simpleLayoutReplacementResetsPagesAndPreservesReadyState() {
        val location = weatherLocation(name = "Simple Replacement City")
        val repository = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(fullWeatherBundle(location))),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            forecastExecutor = DirectExecutor,
        )

        composeRule.setCompactContent(heightDp = 900) {
            OxygenApp(
                stateHolder = stateHolder,
                appearance = OxygenAppearance(effects = EffectsLevel.OFF),
            )
        }
        composeRule.waitForIdle()
        val requestCountAfterReady = repository.locations.size

        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Details")
        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-destination-appearance").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-layout-simple").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 2")
        composeRule.onNodeWithText("Simple Replacement City").assertIsDisplayed()
        composeRule.onNodeWithText("65 deg F").assertIsDisplayed()
        composeRule.onNodeWithTag("home-page-tab-forecast").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Forecast")
        composeRule.onNodeWithTag("home-simple-forecast-hourly").assertIsSelected()
        composeRule.onNodeWithTag("home-simple-forecast-daily").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-simple-forecast-daily").assertIsSelected()

        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-destination-appearance").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-layout-standard").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 4")
        composeRule.onNodeWithTag("home-page-tab-details").assertIsDisplayed()
        composeRule.onNodeWithText("Simple Replacement City").assertIsDisplayed()
        composeRule.onNodeWithText("65 deg F").assertIsDisplayed()
        assertEquals(requestCountAfterReady, repository.locations.size)
    }

    @Test
    fun simpleSparseAndOperationalStatesRemainHonest() {
        val location = weatherLocation(name = "Simple Sparse City")
        val noReturnedData = fullWeatherBundle(location).copy(
            current = null,
            hourly = emptyList(),
            daily = emptyList(),
            alerts = emptyList(),
        )
        val renderedState = mutableStateOf(
            HomeForecastPresentationState.ForecastReady.fromRestoredCache(
                location = location,
                weather = noReturnedData,
                staleAge = Duration.ofMinutes(45),
            ),
        )

        composeRule.setCompactContent(heightDp = 900) {
            HomeLoadingScreen(
                state = renderedState.value,
                appearance = OxygenAppearance(layout = LayoutPreset.SIMPLE, effects = EffectsLevel.OFF),
            )
        }

        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithText("Current conditions").assertIsDisplayed()
        composeRule.onNodeWithText("Provider returned no current, hourly, or daily weather data for this location.")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("0 deg F").assertCountEquals(0)
        composeRule.onAllNodesWithTag("home-section-precipitation").assertCountEquals(0)
        composeRule.onAllNodesWithTag("home-section-alert").assertCountEquals(0)
        composeRule.onNodeWithText("Showing cached forecast from 45 minutes ago while Oxygen refreshes this location.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Source unavailable").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Data type unavailable").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("home-page-tab-forecast").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-simple-forecast-hourly").assertIsSelected()
        composeRule.onNodeWithText("Hourly forecast").assertIsDisplayed()
        composeRule.onNodeWithText("Provider returned no current, hourly, or daily weather data for this location.")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-simple-forecast-daily").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Daily forecast").assertIsDisplayed()

        renderedState.value = HomeForecastPresentationState.ForecastReady.from(
            location = location,
            weather = fullWeatherBundle(location).copy(
                current = fullWeatherBundle(location).current?.copy(precipitationMm = null),
                hourly = fullWeatherBundle(location).hourly.map {
                    it.copy(precipitationProbabilityPercent = null, precipitationMm = null)
                },
                alerts = emptyList(),
            ),
            freshness = ForecastFreshness.StaleAfterFailedRefresh(
                staleAge = Duration.ofMinutes(45),
                refreshFailure = ForecastError.NetworkUnavailable,
            ),
        )
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Forecast")
        composeRule.onNodeWithTag("home-simple-forecast-hourly").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("6 AM, Rain, 64 deg F, Precipitation unavailable")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-page-tab-now").performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("home-section-alert").assertCountEquals(0)
        composeRule.onAllNodesWithTag("home-section-precipitation").assertCountEquals(0)
        composeRule.onNodeWithText("Refresh failed: Refresh could not reach the weather service or network.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun simpleLayoutIsReadableCompactLargeFontEffectsOff() {
        val location = weatherLocation(
            name = "A Very Long Simple Selected Location Name Near The Lakefront, Wisconsin, United States",
        )
        val baseAlert = fullWeatherBundle(location).alerts.single()
        val alerts = listOf(
            baseAlert.copy(event = "Flash Flood Warning", severity = AlertSeverity.SEVERE),
            baseAlert.copy(id = "simple-alert-2", event = "Heat Advisory"),
            baseAlert.copy(id = "simple-alert-3", event = "Wind Advisory"),
        )
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = location,
            weather = fullWeatherBundle(
                location = location,
                provenance = forecastProvenance(
                    sourceName = "Open-Meteo Long Provider Attribution Name",
                    licenseId = "Creative Commons Attribution 4.0 International",
                ),
            ).copy(alerts = alerts),
            freshness = ForecastFreshness.StaleAfterFailedRefresh(
                staleAge = Duration.ofMinutes(95),
                refreshFailure = ForecastError.ProviderUnavailable("open-meteo"),
            ),
            alertStatus = AlertLookupStatus.Available(
                AlertSuccessMetadata(
                    requestPoint = location.point,
                    providerId = "nws",
                    fetchedAt = Instant.parse("2026-08-22T15:05:00Z"),
                ),
            ),
        )
        composeRule.setCompactContent(fontScale = 1.3f) {
            HomeLoadingScreen(
                state = state,
                appearance = OxygenAppearance(layout = LayoutPreset.SIMPLE, effects = EffectsLevel.OFF),
            )
        }

        composeRule.onAllNodesWithTag("home-weather-scene").assertCountEquals(0)
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onAllNodesWithContentDescription("Now, Page 1 of 2").assertCountEquals(2)
        composeRule.writeSemanticsArtifact("simple-now-semantics.txt")
        composeRule.writeScreenshotArtifact("simple-now.png")
        composeRule.assertMinimumTouchTarget(
            "home-page-tab-now",
            "home-page-tab-forecast",
            "home-about-entry",
            "home-refresh",
            "home-change-location",
        )
        composeRule.assertReadableBoundsAfterScroll(
            "home-section-location",
            "home-section-current",
            "home-section-stale",
            "home-section-alert",
            "home-section-precipitation",
        )
        composeRule.assertSemanticsTreeOrder(
            "home-section-current",
            "home-section-stale",
            "home-section-alert",
            "home-alert-source-link",
            "home-alert-count",
            "home-section-precipitation",
        )
        composeRule.onNodeWithTag("home-alert-details").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")

        composeRule.onNodeWithTag("home-page-tab-forecast").performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithContentDescription("Forecast, Page 2 of 2").assertCountEquals(2)
        composeRule.assertMinimumTouchTarget(
            "home-simple-forecast-hourly",
            "home-simple-forecast-daily",
        )
        composeRule.onNodeWithTag("home-simple-forecast-hourly").assertIsSelected()
        composeRule.onNodeWithContentDescription("6 AM, Rain, 64 deg F, 60%").assertIsDisplayed()
        composeRule.writeSemanticsArtifact("simple-forecast-hourly-semantics.txt")
        composeRule.writeScreenshotArtifact("simple-forecast-hourly.png")

        composeRule.onNodeWithTag("home-simple-forecast-daily").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-simple-forecast-daily").assertIsSelected()
        composeRule.onNodeWithContentDescription("Sat, Aug 22, Rain showers, High 73 deg F, Low 54 deg F, 40%")
            .assertIsDisplayed()
        composeRule.assertReadableBoundsAfterScroll(
            "home-section-daily",
            "home-daily-entry-0",
            "home-daily-entry-1",
        )
        composeRule.writeSemanticsArtifact("simple-forecast-daily-semantics.txt")
        composeRule.writeScreenshotArtifact("simple-forecast-daily.png")
    }

    @Test
    fun officialAlertSummaryIsReadableEffectsOffAndOpensValidatedSourceLinks() {
        val location = weatherLocation(name = "Alert Summary City")
        val baseAlert = fullWeatherBundle(location).alerts.single()
        val alerts = listOf(
            baseAlert.copy(
                event = "Flash Flood Warning",
                severity = AlertSeverity.SEVERE,
                issuer = "National Weather Service",
                web = "https://alerts.weather.gov/example",
            ),
            baseAlert.copy(id = "alert-2", event = "Heat Advisory"),
            baseAlert.copy(id = "alert-3", event = "Wind Advisory"),
        )
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = location,
            weather = fullWeatherBundle(location).copy(alerts = alerts),
            freshness = ForecastFreshness.StaleAfterFailedRefresh(
                staleAge = Duration.ofMinutes(45),
                refreshFailure = ForecastError.NetworkUnavailable,
            ),
            alertStatus = AlertLookupStatus.Available(
                AlertSuccessMetadata(
                    requestPoint = location.point,
                    providerId = "nws",
                    fetchedAt = Instant.parse("2026-08-22T15:05:00Z"),
                ),
            ),
        )
        val openedUris = mutableListOf<String>()
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                openedUris += uri
            }
        }
        val renderedState = mutableStateOf(state)

        composeRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 1.3f),
                LocalUriHandler provides uriHandler,
            ) {
                OxygenTheme {
                    Box(Modifier.width(360.dp).height(640.dp)) {
                        HomeLoadingScreen(
                            state = renderedState.value,
                            appearance = OxygenAppearance(effects = EffectsLevel.OFF),
                        )
                    }
                }
            }
        }

        composeRule.assertSemanticsTreeOrder(
            "home-section-current",
            "home-section-stale",
            "home-section-alert",
            "home-alert-source-link",
            "home-alert-count",
            "home-section-precipitation",
        )
        composeRule.onNodeWithTag("home-section-alert").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Official alert").assertIsDisplayed()
        composeRule.onNodeWithText("Flash Flood Warning").assertIsDisplayed()
        composeRule.onNodeWithText("Severity: Severe").assertIsDisplayed()
        composeRule.onNodeWithText("Issuer: National Weather Service").assertIsDisplayed()
        composeRule.onNodeWithText("Expires 1:00 PM").assertIsDisplayed()
        composeRule.onNodeWithText("Alert source checked Aug 22, 10:05 AM CDT").assertIsDisplayed()
        composeRule.writeSemanticsArtifact("alert-summary-effects-off-360x640-font-1.3-semantics.txt")
        val screenshot = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val screenshotFile = File(
            InstrumentationRegistry.getInstrumentation().targetContext.filesDir,
            "alert-summary-effects-off-360x640-font-1.3.png",
        )
        screenshotFile.outputStream().use { output ->
            assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
        composeRule.onNodeWithTag("home-alert-source-link")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag("home-alert-count").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("3 active alerts").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Open official NOAA/National Weather Service alert source")
            .assertExists()
        assertEquals(listOf("https://alerts.weather.gov/example"), openedUris)

        val invalidState = HomeForecastPresentationState.ForecastReady.from(
            location = location,
            weather = fullWeatherBundle(location).copy(
                alerts = listOf(baseAlert.copy(web = "http://alerts.weather.gov/example")),
            ),
            alertStatus = AlertLookupStatus.Available(
                AlertSuccessMetadata(location.point, "nws", Instant.parse("2026-08-22T15:05:00Z")),
            ),
        )
        composeRule.runOnIdle { renderedState.value = invalidState }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-alert-source-link").performScrollTo().performClick()
        assertEquals("https://www.weather.gov/", openedUris.last())
    }

    @Test
    fun officialAlertDetailFlowSelectsSecondAlertPreservesVerbatimTextAndReturnsHome() {
        val location = weatherLocation(name = "Alert Detail City")
        val baseAlert = fullWeatherBundle(location).alerts.single()
        val alerts = listOf(
            baseAlert.copy(
                id = "detail-alert-1",
                event = "Flash Flood Warning",
                headline = "Flooding is possible",
                issuer = "Madison Warning Office",
                urgency = com.oxygen.weather.core.model.AlertUrgency.IMMEDIATE,
                certainty = com.oxygen.weather.core.model.AlertCertainty.LIKELY,
                effective = Instant.parse("2026-08-22T12:00:00Z"),
                sent = Instant.parse("2026-08-22T11:30:00Z"),
                onset = Instant.parse("2026-08-22T13:00:00Z"),
                ends = Instant.parse("2026-08-22T20:00:00Z"),
                affectedArea = com.oxygen.weather.core.model.AlertAffectedArea(areaDescription = "Dane County"),
                description = "First alert line one\nFirst alert line two",
                instruction = "Move to higher ground.\nDo not drive.",
                web = "https://alerts.weather.gov/detail-one",
            ),
            baseAlert.copy(
                id = "detail-alert-2",
                event = "Heat Advisory",
                issuer = "Central Forecast Office",
                effective = Instant.parse("2026-08-22T14:00:00Z"),
                expires = Instant.parse("2026-08-22T21:00:00Z"),
                description = "Second alert description",
                instruction = "Drink water.\nTake breaks.",
                web = "https://alerts.weather.gov/detail-two",
            ),
        )
        val checkedAt = Instant.parse("2026-08-22T15:05:00Z")
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = RecordingWeatherRepository(
                listOf(
                    WeatherRepositoryResult.Success(
                        weather = fullWeatherBundle(location).copy(alerts = alerts),
                        alertStatus = AlertLookupStatus.Available(
                            AlertSuccessMetadata(location.point, "nws", checkedAt),
                        ),
                    ),
                ),
            ),
            forecastExecutor = DirectExecutor,
        )
        val openedUris = mutableListOf<String>()
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                openedUris += uri
            }
        }

        composeRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 1.3f),
                LocalUriHandler provides uriHandler,
            ) {
                Box(Modifier.width(360.dp).height(640.dp)) {
                    OxygenApp(
                        stateHolder = holder,
                        appearance = OxygenAppearance(effects = EffectsLevel.OFF),
                    )
                }
            }
        }

        composeRule.onNodeWithTag("home-alert-details").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home-alert-source-link").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home-alert-details").performClick()
        composeRule.onNodeWithTag("alert-detail-title").assertIsDisplayed()
        composeRule.onNodeWithTag("alert-detail-selector-0").assertIsSelected()
        composeRule.onNodeWithText("Issuer: Madison Warning Office").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Urgency: Immediate").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Certainty: Likely").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Effective: Aug 22, 7:00 AM CDT").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Expires: Aug 22, 1:00 PM CDT").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Affected area: Dane County").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("First alert line one\nFirst alert line two").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Move to higher ground.\nDo not drive.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Alert source checked Aug 22, 10:05 AM CDT").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Official alerts from NOAA/National Weather Service").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("alert-detail-selector-1").performScrollTo().performClick()
        composeRule.onNodeWithTag("alert-detail-selector-1").assertIsSelected()
        composeRule.onNodeWithText("Issuer: Central Forecast Office").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Second alert description").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Drink water.\nTake breaks.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("alert-detail-source-link").performScrollTo().performClick()
        assertEquals(listOf("https://alerts.weather.gov/detail-two"), openedUris)

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.filesDir.resolve("alert-detail-effects-off-360x640-font-1.3-semantics.txt")
            .writeText(composeRule.onRoot(useUnmergedTree = true).printToString(maxDepth = 120))
        val screenshot = composeRule.onRoot().captureToImage().asAndroidBitmap()
        context.filesDir.resolve("alert-detail-effects-off-360x640-font-1.3.png").outputStream().use {
            assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG, 100, it))
        }

        composeRule.onNodeWithTag("alert-detail-back").performScrollTo().performClick()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-section-alert").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun representativeWeatherMarksRenderGoldLineTreatmentForProviderNeutralConditions() {
        composeRule.setContent {
            OxygenTheme {
                Row(
                    modifier = Modifier
                        .background(Color.Black)
                        .testTag("weather-mark-strip"),
                ) {
                    listOf(
                        "weather-mark-clear" to WeatherCondition.CLEAR,
                        "weather-mark-rain" to WeatherCondition.RAIN_SHOWERS,
                        "weather-mark-snow" to WeatherCondition.SNOW,
                        "weather-mark-storm" to WeatherCondition.THUNDERSTORM_HAIL,
                        "weather-mark-unknown" to WeatherCondition.UNKNOWN,
                    ).forEach { (tag, condition) ->
                        WeatherConditionMark(
                            condition = condition,
                            modifier = Modifier
                                .size(64.dp)
                                .testTag(tag),
                        )
                    }
                }
            }
        }

        listOf(
            "weather-mark-clear",
            "weather-mark-rain",
            "weather-mark-snow",
            "weather-mark-storm",
            "weather-mark-unknown",
        ).forEach { tag ->
            composeRule.onNodeWithTag(tag).assertHasGoldLinePixels(tag)
        }
    }

    @Test
    fun terminalEffectsOffReadyMarkSmoke() {
        val location = weatherLocation(name = "Terminal Mark City")
        composeRule.setHomeContent(
            state = HomeForecastPresentationState.ForecastReady.from(
                location = location,
                weather = fullWeatherBundle(location),
            ),
            widthDp = 360,
            heightDp = 640,
            appearance = OxygenAppearance(
                theme = OxygenThemeId.TERMINAL,
                effects = EffectsLevel.OFF,
            ),
        )
        composeRule.onAllNodesWithTag("home-weather-scene").assertCountEquals(0)
        composeRule.onNodeWithTag("home-current-mark").assertIsDisplayed().assertHasGoldLinePixels("terminal")
    }

    @Test
    fun homePageNavigationSupportsDirectTabsAndHorizontalSwipeWithoutRedundantButtons() {
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = weatherLocation(),
            weather = fullWeatherBundle(weatherLocation()),
        )

        composeRule.setHomeContent(state)

        composeRule.onAllNodesWithTag("home-page-previous").assertCountEquals(0)
        composeRule.onAllNodesWithTag("home-page-next").assertCountEquals(0)

        composeRule.onNodeWithTag("home-page-tab-hourly").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Hourly")
        composeRule.onNodeWithTag("home-page-container").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Daily")

        composeRule.onNodeWithTag("home-page-container").performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Hourly")

        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Details")
        composeRule.onNodeWithTag("home-page-container").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Details")
    }

    @Test
    fun compactHomeKeepsDataPagerAboveBottomNavigationAndSecondaryActions() {
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = weatherLocation(),
            weather = fullWeatherBundle(weatherLocation()),
        )

        composeRule.setHomeContent(state = state, widthDp = 360, heightDp = 640)

        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithText("65 deg F").assertIsDisplayed()
        composeRule.assertVerticalOrder(
            "home-page-container",
            "home-footer-navigation",
        )
        composeRule.assertVerticalOrder(
            "home-page-selector",
            "home-secondary-actions",
        )
        composeRule.assertWithinRootBounds(
            "home-page-container",
            "home-footer-navigation",
        )
        composeRule.assertSecondaryActionsUseLessWidthThanPageTabs()
    }

    @Test
    fun firstRunLocationActionsStayBottomReachableOnCompactPhone() {
        composeRule.setCompactContent {
            FirstRunLocationEntryScreen(
                state = OxygenAppScreen.FirstRunLocationEntry(query = "Madison"),
                selectedLocation = null,
                savedLocations = SavedLocationsPresentationState.NotLoaded,
                onQueryChanged = {},
                onSearch = {},
                onRetry = {},
                onCandidateSelected = {},
                onSavedLocationSelected = {},
                onUseMyLocation = {},
                onBack = {},
                onOpenSettings = {},
            )
        }

        composeRule.onNodeWithTag("location-entry-actions").assertIsDisplayed()
        composeRule.assertMinimumTouchTarget(
            "location-entry-search",
            "location-entry-use-my-location",
            "location-entry-about",
        )
        composeRule.assertInLowerReachZone(
            "location-entry-actions",
            rootHeight = 640f,
        )
        composeRule.assertVerticalOrder(
            "location-entry-search-field",
            "location-entry-actions",
        )
    }

    @Test
    fun savedLocationRowsDisambiguateCurrentSelectionAndExposeSelectControls() {
        val madisonWisconsin = weatherLocation(
            id = "saved-madison-wi",
            name = "Madison, Wisconsin, United States",
        )
        val madisonAlabama = weatherLocation(
            id = "saved-madison-al",
            name = "Madison, Alabama, United States",
        ).copy(point = GeoPoint(34.6993, -86.7483))
        val selectedIds = mutableListOf<LocationId>()

        composeRule.setCompactContent(heightDp = 900) {
            FirstRunLocationEntryScreen(
                state = OxygenAppScreen.FirstRunLocationEntry(query = "Madison"),
                selectedLocation = madisonWisconsin,
                savedLocations = SavedLocationsPresentationState.Loaded(
                    listOf(madisonWisconsin, madisonAlabama),
                ),
                onQueryChanged = {},
                onSearch = {},
                onRetry = {},
                onCandidateSelected = {},
                onSavedLocationSelected = { selectedIds += it },
                onUseMyLocation = {},
                onBack = {},
                onOpenSettings = {},
            )
        }

        composeRule.onNodeWithTag("location-entry-saved-locations").assertIsDisplayed()
        composeRule.onNodeWithTag("location-entry-saved-location-0").assertIsDisplayed()
        composeRule.onNodeWithTag("location-entry-saved-location-1").assertIsDisplayed()
        composeRule.onAllNodesWithText("Madison").assertCountEquals(3)
        composeRule.onNodeWithText("Wisconsin, United States").assertIsDisplayed()
        composeRule.onNodeWithText("Alabama, United States").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("43.0731, -89.4012 | America/Chicago").assertExists()
        composeRule.onNodeWithTag("location-entry-saved-current-0").assertIsDisplayed()
        composeRule.onAllNodesWithTag("location-entry-saved-current-1").assertCountEquals(0)
        composeRule.assertMinimumTouchTargetAfterScroll(
            "location-entry-saved-location-select-0",
            "location-entry-saved-location-select-1",
        )

        composeRule.onNodeWithTag("location-entry-saved-location-select-1")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(madisonAlabama.id), selectedIds)
    }

    @Test
    fun savedLocationRowsExposeRemoveConfirmationCancelAndConfirmControls() {
        val madison = weatherLocation(
            id = "saved-remove-ui-madison",
            name = "Madison, Wisconsin, United States",
        )
        val confirmedIds = mutableListOf<LocationId>()
        val pendingRemoval = mutableStateOf<LocationId?>(null)

        composeRule.setCompactContent(heightDp = 920, fontScale = 1.35f) {
            FirstRunLocationEntryScreen(
                state = OxygenAppScreen.FirstRunLocationEntry(query = "Madison"),
                selectedLocation = madison,
                savedLocations = SavedLocationsPresentationState.Loaded(
                    locations = listOf(madison),
                    pendingRemovalLocationId = pendingRemoval.value,
                ),
                onQueryChanged = {},
                onSearch = {},
                onRetry = {},
                onCandidateSelected = {},
                onSavedLocationSelected = {},
                onSavedLocationRemoveRequested = { pendingRemoval.value = it },
                onSavedLocationRemoveCanceled = { pendingRemoval.value = null },
                onSavedLocationRemoveConfirmed = { confirmedIds += it },
                onUseMyLocation = {},
                onBack = {},
                onOpenSettings = {},
            )
        }

        composeRule.assertMinimumTouchTargetAfterScroll(
            "location-entry-saved-location-select-0",
            "location-entry-saved-location-remove-0",
        )
        composeRule.onNodeWithTag("location-entry-saved-location-remove-0")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("location-entry-saved-remove-confirmation-0")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.assertMinimumTouchTargetAfterScroll(
            "location-entry-saved-remove-cancel-0",
            "location-entry-saved-remove-confirm-0",
        )
        composeRule.onNodeWithTag("location-entry-saved-remove-cancel-0")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("location-entry-saved-remove-confirmation-0").assertCountEquals(0)
        composeRule.onNodeWithTag("location-entry-saved-location-0").performScrollTo().assertIsDisplayed()
        assertEquals(emptyList<LocationId>(), confirmedIds)

        composeRule.onNodeWithTag("location-entry-saved-location-remove-0")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("location-entry-saved-remove-confirm-0")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(madison.id), confirmedIds)
    }

    @Test
    fun emptySavedLocationListDoesNotRenderFakeRowsAndKeepsManualSearchUsable() {
        composeRule.setCompactContent(heightDp = 760) {
            FirstRunLocationEntryScreen(
                state = OxygenAppScreen.FirstRunLocationEntry(query = "Milwaukee"),
                selectedLocation = null,
                savedLocations = SavedLocationsPresentationState.Loaded(emptyList()),
                onQueryChanged = {},
                onSearch = {},
                onRetry = {},
                onCandidateSelected = {},
                onSavedLocationSelected = {},
                onUseMyLocation = {},
                onBack = {},
                onOpenSettings = {},
            )
        }

        composeRule.onAllNodesWithTag("location-entry-saved-locations").assertCountEquals(0)
        composeRule.onAllNodesWithTag("location-entry-saved-location-0").assertCountEquals(0)
        composeRule.onNodeWithTag("location-entry-search-field").assertIsDisplayed()
        composeRule.onNodeWithTag("location-entry-search").assertIsDisplayed()
        composeRule.onNodeWithText("Use my location").assertIsDisplayed()
    }

    @Test
    fun searchResultRowsExposeSeparateSaveAndUseNowControlsWithStableTags() {
        val madison = weatherLocation(
            id = "result-save-ui-madison",
            name = "Madison, Wisconsin, United States",
        )
        val savedIds = mutableListOf<LocationId>()
        val selectedIds = mutableListOf<LocationId>()

        composeRule.setCompactContent(heightDp = 900, fontScale = 1.4f) {
            FirstRunLocationEntryScreen(
                state = OxygenAppScreen.FirstRunLocationEntry(
                    query = "Madison",
                    submittedQuery = "Madison",
                    searchState = ManualLocationSearchState.Results(
                        query = "Madison",
                        candidates = listOf(manualCandidate(madison)),
                    ),
                ),
                selectedLocation = null,
                savedLocations = SavedLocationsPresentationState.Loaded(emptyList()),
                canSaveSearchResults = true,
                onQueryChanged = {},
                onSearch = {},
                onRetry = {},
                onCandidateSelected = { selectedIds += it },
                onCandidateSaved = { savedIds += it },
                onSavedLocationSelected = {},
                onUseMyLocation = {},
                onBack = {},
                onOpenSettings = {},
            )
        }

        composeRule.onNodeWithTag("location-entry-result-0").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Madison, Wisconsin, United States").assertIsDisplayed()
        composeRule.onNodeWithText("43.0731, -89.4012 | America/Chicago").assertExists()
        composeRule.assertMinimumTouchTargetAfterScroll(
            "location-entry-result-save-0",
            "location-entry-result-use-now-0",
        )

        composeRule.onNodeWithTag("location-entry-result-save-0").performScrollTo().performClick()
        composeRule.onNodeWithTag("location-entry-result-use-now-0").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(madison.id), savedIds)
        assertEquals(listOf(madison.id), selectedIds)
    }

    @Test
    fun searchResultRowsHideSaveControlWhenSavedStorageIsUnavailable() {
        val madison = weatherLocation(
            id = "result-no-save-ui-madison",
            name = "Madison, Wisconsin, United States",
        )

        composeRule.setCompactContent(heightDp = 820) {
            FirstRunLocationEntryScreen(
                state = OxygenAppScreen.FirstRunLocationEntry(
                    query = "Madison",
                    submittedQuery = "Madison",
                    searchState = ManualLocationSearchState.Results(
                        query = "Madison",
                        candidates = listOf(manualCandidate(madison)),
                    ),
                ),
                selectedLocation = null,
                savedLocations = SavedLocationsPresentationState.NotLoaded,
                canSaveSearchResults = false,
                onQueryChanged = {},
                onSearch = {},
                onRetry = {},
                onCandidateSelected = {},
                onCandidateSaved = {},
                onSavedLocationSelected = {},
                onUseMyLocation = {},
                onBack = {},
                onOpenSettings = {},
            )
        }

        composeRule.onNodeWithTag("location-entry-result-0").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithTag("location-entry-result-save-0").assertCountEquals(0)
        composeRule.onNodeWithTag("location-entry-result-use-now-0").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun savedLocationFailureIsVisibleAndManualSearchCanStillSelect() {
        val oldLocation = weatherLocation(id = "manual-old-after-saved-failure", name = "Old Saved Failure City")
        val searchedLocation = weatherLocation(id = "manual-new-after-saved-failure", name = "Manual Result City")
        val repository = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(fullWeatherBundle(oldLocation))),
            listOf(WeatherRepositoryResult.Loading),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = oldLocation,
            geocodingRepository = StaticGeocodingRepository(searchedLocation),
            weatherRepository = repository,
            savedLocationStorage = FailingSavedLocationStorage,
            searchExecutor = DirectExecutor,
            forecastExecutor = DirectExecutor,
        )

        composeRule.setContent {
            OxygenApp(stateHolder = stateHolder)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-change-location").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SavedLocationsMessage.LocalStateUnavailable.text).assertIsDisplayed()
        composeRule.onNodeWithTag("location-entry-search-field").performTextInput("Manual Result City")
        composeRule.onNodeWithTag("location-entry-search").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Use now").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(oldLocation, searchedLocation), repository.locations)
        composeRule.onNodeWithText("Loading weather for Manual Result City").assertIsDisplayed()
    }

    @Test
    fun oxygenAppLocationActionLoadsSavedRowsAndSavedSelectRequestsHomeForecast() {
        val oldLocation = weatherLocation(id = "old-saved-ui", name = "Old Saved UI City")
        val savedMadison = weatherLocation(id = "saved-ui-madison", name = "Madison, Wisconsin, United States")
        val savedChicago = weatherLocation(id = "saved-ui-chicago", name = "Chicago, Illinois, United States")
        val repository = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(fullWeatherBundle(oldLocation))),
            listOf(WeatherRepositoryResult.Loading),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = oldLocation,
            weatherRepository = repository,
            savedLocationStorage = RecordingSavedLocationStorage(listOf(savedMadison, savedChicago)),
            forecastExecutor = DirectExecutor,
        )

        composeRule.setContent {
            OxygenApp(stateHolder = stateHolder)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-change-location").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("location-entry-saved-locations").assertIsDisplayed()
        composeRule.onNodeWithText("Madison").assertIsDisplayed()
        composeRule.onNodeWithText("Wisconsin, United States").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Chicago").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Illinois, United States").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("location-entry-saved-location-select-1")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(oldLocation, savedChicago), repository.locations)
        composeRule.onNodeWithText("Loading weather for Chicago, Illinois, United States").assertIsDisplayed()
    }

    @Test
    fun oxygenAppSavedLocationRemoveCancelPreservesRowAndConfirmRefreshesSavedListOnly() {
        val oldLocation = weatherLocation(id = "old-saved-remove-ui", name = "Old Saved Remove UI City")
        val savedMadison = weatherLocation(id = "saved-remove-ui-app-madison", name = "Madison, Wisconsin, United States")
        val savedChicago = weatherLocation(id = "saved-remove-ui-app-chicago", name = "Chicago, Illinois, United States")
        val repository = RecordingWeatherRepository(listOf(WeatherRepositoryResult.Success(fullWeatherBundle(oldLocation))))
        val storage = RecordingSavedLocationStorage(listOf(savedMadison, savedChicago))
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = oldLocation,
            weatherRepository = repository,
            savedLocationStorage = storage,
            forecastExecutor = DirectExecutor,
        )

        composeRule.setContent {
            OxygenApp(stateHolder = stateHolder)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-change-location").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("location-entry-saved-location-remove-0")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("location-entry-saved-remove-cancel-0")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Madison").performScrollTo().assertIsDisplayed()
        assertEquals(emptyList<LocationId>(), storage.removals)

        composeRule.onNodeWithTag("location-entry-saved-location-remove-0")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("location-entry-saved-remove-confirm-0")
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("Madison").assertCountEquals(0)
        composeRule.onNodeWithText("Chicago").performScrollTo().assertIsDisplayed()
        assertEquals(listOf(savedMadison.id), storage.removals)
        assertEquals(listOf(oldLocation), repository.locations)
    }

    @Test
    fun changeLocationBackStaysBottomReachableAndReturnsWithoutRefreshing() {
        val oldLocation = weatherLocation(id = "manual-old-bottom", name = "Old Bottom City")
        val repository = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(fullWeatherBundle(oldLocation))),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = oldLocation,
            weatherRepository = repository,
            forecastExecutor = DirectExecutor,
        )

        composeRule.setCompactContent {
            OxygenApp(stateHolder = stateHolder)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-change-location").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("location-entry-back").assertIsDisplayed()
        composeRule.assertInLowerReachZone("location-entry-back", rootHeight = 640f)
        composeRule.onNodeWithTag("location-entry-back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Old Bottom City").assertIsDisplayed()
        assertEquals(listOf(oldLocation), repository.locations)
    }

    @Test
    fun aboutOverviewKeepsBackActionBottomReachable() {
        composeRule.setCompactContent {
            SettingsScreen(
                state = OxygenAppScreen.Settings(
                    returnScreen = OxygenAppScreen.FirstRunLocationEntry(),
                ),
                appearance = OxygenAppearance(effects = EffectsLevel.OFF),
                themeId = com.oxygen.weather.app.ui.theme.OxygenThemeId.OXYGEN,
                onDestinationSelected = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithTag("settings-bottom-actions").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-back").assertIsDisplayed()
        composeRule.assertInLowerReachZone("settings-bottom-actions", rootHeight = 640f)
        composeRule.assertMinimumTouchTarget("settings-back")
    }

    @Test
    fun aboutDetailKeepsBackActionBottomReachable() {
        composeRule.setCompactContent {
            SettingsScreen(
                state = OxygenAppScreen.Settings(
                    returnScreen = OxygenAppScreen.FirstRunLocationEntry(),
                    selectedDestination = SettingsDestination.Privacy,
                ),
                appearance = OxygenAppearance(effects = EffectsLevel.OFF),
                themeId = com.oxygen.weather.app.ui.theme.OxygenThemeId.OXYGEN,
                onDestinationSelected = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("Privacy Baseline").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-bottom-actions").assertIsDisplayed()
        composeRule.assertInLowerReachZone("settings-bottom-actions", rootHeight = 640f)
        composeRule.assertMinimumTouchTarget("settings-back")
    }

    @Test
    fun settingsRootReachesAllDestinationsAndLocationsBackWorksWithAndroidBack() {
        val location = weatherLocation(name = "Settings Fixture City")
        val saved = weatherLocation(name = "Saved Settings City")
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = RecordingWeatherRepository(listOf(WeatherRepositoryResult.Success(fullWeatherBundle(location)))),
            savedLocationStorage = RecordingSavedLocationStorage(listOf(saved)),
            forecastExecutor = DirectExecutor,
        )

        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1.3f)) {
                OxygenTheme {
                    Box(Modifier.width(360.dp).height(640.dp)) {
                        OxygenApp(
                            stateHolder = stateHolder,
                            appearance = OxygenAppearance(effects = EffectsLevel.OFF),
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.waitForIdle()

        listOf(
            SettingsDestination.Appearance,
            SettingsDestination.Units,
            SettingsDestination.DataSources,
            SettingsDestination.Privacy,
            SettingsDestination.OpenSourceLicenses,
            SettingsDestination.About,
        ).forEach { destination ->
            composeRule.onNodeWithTag("settings-destination-${destination.name.lowercase()}")
                .performScrollTo()
                .performClick()
            composeRule.onNodeWithText(destination.title).performScrollTo().assertIsDisplayed()
            if (destination == SettingsDestination.Appearance) {
                composeRule.onNodeWithText("Oxygen").assertIsDisplayed()
                composeRule.onNodeWithText("Standard").assertIsDisplayed()
                composeRule.onNodeWithTag("settings-effects-off").assertIsDisplayed()
                composeRule.onAllNodesWithTag("unit-preferences").assertCountEquals(0)
            }
            composeRule.onNodeWithTag("settings-back").performClick()
            composeRule.waitForIdle()
        }

        composeRule.onNodeWithTag("settings-destination-locations").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("location-entry-saved-locations").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithTag("location-entry-about").assertCountEquals(0)
        composeRule.onNodeWithTag("location-entry-back").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-content").assertIsDisplayed()

        composeRule.onNodeWithTag("settings-destination-locations").performScrollTo().performClick()
        composeRule.waitForIdle()
        pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-content").assertIsDisplayed()
        composeRule.writeSemanticsArtifact("settings-root-and-locations-360x640-font-1.3-semantics.txt")
    }

    @Test
    fun settingsDisclosuresShowActiveProviderLicenseAndPrivacyBaseline() {
        val location = weatherLocation(name = "Disclosure Fixture City")
        val repository = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(fullWeatherBundle(location))),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            forecastExecutor = DirectExecutor,
        )
        val openedUris = mutableListOf<String>()
        var permissionRequests = 0
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                openedUris += uri
            }
        }

        composeRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 1.3f),
                LocalUriHandler provides uriHandler,
            ) {
                OxygenTheme {
                    Box(Modifier.width(360.dp).height(640.dp)) {
                        OxygenApp(
                            stateHolder = stateHolder,
                            appearance = OxygenAppearance(effects = EffectsLevel.OFF),
                            onRequestLocationPermission = { permissionRequests++ },
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        val repositoryCallsAfterHome = repository.locations.size

        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("settings-destination-datasources").performScrollTo().performClick()
        composeRule.onNodeWithText("Open-Meteo forecast and timezone data: CC BY 4.0.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("MET Norway data: NLOD 2.0 and CC BY 4.0.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("NWS information is public information; requested credits apply and third-party page content may have separate terms.")
            .performScrollTo()
            .assertIsDisplayed()
        val disclosureLinks = listOf(
            "Open-Meteo forecast and timezone documentation" to "https://open-meteo.com/en/docs",
            "MET Norway licensing and attribution" to "https://api.met.no/doc/License",
            "Open-Meteo geocoding documentation" to "https://open-meteo.com/en/docs/geocoding-api",
            "GeoNames licensing and attribution" to "https://www.geonames.org/about.html",
            "NOAA/National Weather Service information" to "https://www.weather.gov/",
        )
        disclosureLinks.forEach { (label, _) ->
            composeRule.onNodeWithContentDescription(label).performScrollTo().assertIsDisplayed().performClick()
        }
        composeRule.writeSemanticsArtifact("data-sources-360x640-font-1.3-semantics.txt")
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("settings-destination-privacy").performScrollTo().performClick()
        composeRule.onNodeWithText("Manual search works without Android location permission", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Foreground selected-point NWS alert requests", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.writeSemanticsArtifact("privacy-360x640-font-1.3-semantics.txt")
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("settings-destination-opensourcelicenses").performScrollTo().performClick()
        composeRule.onNodeWithText("Oxygen source code is licensed under GPL-3.0-or-later; see the repository LICENSE file.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Weather-data attribution and licensing are separate from Oxygen source-code licensing.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.writeSemanticsArtifact("open-source-licenses-360x640-font-1.3-semantics.txt")
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.waitForIdle()

        assertEquals(disclosureLinks.map { it.second }, openedUris)
        assertEquals(repositoryCallsAfterHome, repository.locations.size)
        assertEquals(0, permissionRequests)
        composeRule.onNodeWithTag("settings-content").assertIsDisplayed()
    }

    @Test
    fun oxygenAppUnitsSelectionReturnsHomeWithAlternateUnitsAndKeepsPagesReachable() {
        val location = weatherLocation(name = "Units Fixture City")
        val repository = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(fullWeatherBundle(location))),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            forecastExecutor = DirectExecutor,
        )

        composeRule.setCompactContent(heightDp = 900) {
            OxygenApp(stateHolder = stateHolder)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Units").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("unit-choice-default").assertIsSelected()
        composeRule.assertMinimumTouchTargetAfterScroll(
            "unit-choice-default",
            "unit-choice-metric",
            "unit-choice-us",
            "unit-choice-uk",
        )
        composeRule.assertMinimumTouchTarget("settings-back")

        composeRule.onNodeWithTag("unit-choice-metric").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("unit-choice-metric").assertIsSelected()
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("18 deg C").assertIsDisplayed()
        composeRule.onNodeWithText("Open-Meteo | Fetched Aug 22, 7:00 AM CDT").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Details")
        assertEquals(listOf(location), repository.locations)
        composeRule.writeSemanticsArtifact("units-selection-home-semantics.txt")
        val screenshot = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val screenshotFile = File(
            InstrumentationRegistry.getInstrumentation().targetContext.filesDir,
            "units-selection-home.png",
        )
        screenshotFile.outputStream().use { output ->
            assertTrue(screenshot.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }

    @Test
    fun homePagerExposesNamedAccessibilityPageMovementActions() {
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = weatherLocation(),
            weather = fullWeatherBundle(weatherLocation()),
        )

        composeRule.setHomeContent(state)

        composeRule.onNodeWithTag("home-page-container")
            .assertCustomActions("Show next page: Hourly")
        composeRule.performPagerCustomAction("Show next page: Hourly")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Hourly")
        composeRule.onNodeWithTag("home-page-container")
            .assertCustomActions("Show previous page: Now", "Show next page: Daily")

        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-container")
            .assertCustomActions("Show previous page: Daily")
        composeRule.performPagerCustomAction("Show previous page: Daily")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Daily")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 3 of 4")
    }

    @Test
    fun homeInteractiveControlsExposeMinimumTouchTargetsAndDoNotPageAccidentally() {
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = weatherLocation(),
            weather = fullWeatherBundle(weatherLocation()),
        )

        composeRule.setHomeContent(state = state, widthDp = 360, heightDp = 640)

        composeRule.assertMinimumTouchTarget(
            "home-page-tab-now",
            "home-page-tab-hourly",
            "home-page-tab-daily",
            "home-page-tab-details",
            "home-about-entry",
            "home-refresh",
            "home-change-location",
        )

        listOf("home-about-entry", "home-refresh", "home-change-location").forEach { tag ->
            composeRule.onNodeWithTag(tag).performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
            composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 4")
        }
    }

    @Test
    fun effectsDisabledHomePathKeepsCompleteWeatherMeaningReachable() {
        val location = weatherLocation(name = "Effects Disabled City")
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = location,
            weather = fullWeatherBundle(location),
            freshness = ForecastFreshness.StaleAfterFailedRefresh(
                staleAge = Duration.ofMinutes(45),
                refreshFailure = ForecastError.NetworkUnavailable,
            ),
        )

        composeRule.setHomeContent(
            state = state,
            widthDp = 360,
            heightDp = 900,
            appearance = OxygenAppearance(effects = EffectsLevel.OFF),
        )

        composeRule.onAllNodesWithTag("home-weather-scene").assertCountEquals(0)
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithText("65 deg F").assertIsDisplayed()
        composeRule.onNodeWithText("Rain showers").assertIsDisplayed()
        composeRule.onNodeWithText("H 73 deg F   L 54 deg F").assertIsDisplayed()
        composeRule.onNodeWithText("Open-Meteo | Fetched Aug 22, 7:00 AM CDT").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-stale").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Showing cached forecast from 45 minutes ago because refresh failed.").assertExists()
        composeRule.onNodeWithText("Refresh failed: Refresh could not reach the weather service or network.").assertExists()
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Weather data by Open-Meteo.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Forecast requests send this location's coordinates and timezone to Open-Meteo.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.writeSemanticsArtifact("effects-off-dashboard-semantics.txt")
    }

    @Test
    fun compactHourlyPageShowsFourChronologicalEntriesWithHonestPrecipitation() {
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = weatherLocation(
                name = "A Very Long Selected Location Name Near The Lakefront, Wisconsin, United States",
            ),
            weather = fullWeatherBundle(weatherLocation()),
        )

        composeRule.setHomeContent(state = state, widthDp = 360, heightDp = 640)

        composeRule.onNodeWithTag("home-page-tab-hourly").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-page-title").assertTextContains("Hourly")
        composeRule.onNodeWithTag("home-hourly-grid").assertIsDisplayed()
        listOf(
            "6 AM, Rain, 64 deg F, 60%",
            "7 AM, Cloudy, 67 deg F, Precipitation unavailable",
            "8 AM, Partly cloudy, 68 deg F, 20%",
            "9 AM, Mostly clear, 70 deg F, 10%",
        ).forEach { description ->
            composeRule.onNodeWithContentDescription(description).assertIsDisplayed()
        }
        composeRule.assertWithinRootBounds(
            "home-hourly-entry-0",
            "home-hourly-entry-1",
            "home-hourly-entry-2",
            "home-hourly-entry-3",
        )
        composeRule.assertCheckedSiblingSpacing(
            "home-hourly-entry-0",
            "home-hourly-entry-2",
        )
        composeRule.writeSemanticsArtifact("hourly-compact-semantics.txt")
    }

    @Test
    fun compactDailyPageShowsFourChronologicalEntriesWithHonestPrecipitation() {
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = weatherLocation(
                name = "A Very Long Selected Location Name Near The Lakefront, Wisconsin, United States",
            ),
            weather = fullWeatherBundle(weatherLocation()),
        )

        composeRule.setHomeContent(state = state, widthDp = 360, heightDp = 640)

        composeRule.onNodeWithTag("home-page-tab-daily").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-page-title").assertTextContains("Daily")
        composeRule.onNodeWithTag("home-daily-list").assertIsDisplayed()
        listOf(
            "Sat, Aug 22, Rain showers, High 73 deg F, Low 54 deg F, 40%",
            "Sun, Aug 23, Cloudy, High 70 deg F, Low 52 deg F, Precipitation unavailable",
            "Mon, Aug 24, Partly cloudy, High 77 deg F, Low 57 deg F, 20%",
            "Tue, Aug 25, Mostly clear, High 82 deg F, Low 61 deg F, 10%",
        ).forEach { description ->
            composeRule.onNodeWithContentDescription(description).assertIsDisplayed()
        }
        composeRule.assertWithinRootBounds(
            "home-daily-entry-0",
            "home-daily-entry-1",
            "home-daily-entry-2",
            "home-daily-entry-3",
        )
        composeRule.assertCheckedSiblingSpacing(
            "home-daily-entry-0",
            "home-daily-entry-1",
            "home-daily-entry-2",
            "home-daily-entry-3",
        )
        composeRule.writeSemanticsArtifact("daily-compact-semantics.txt")
    }

    @Test
    fun compactDetailsPageShowsStructuredGroupsAndSourceSummaryInFirstViewport() {
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = weatherLocation(
                name = "A Very Long Selected Location Name Near The Lakefront, Wisconsin, United States",
            ),
            weather = fullWeatherBundle(weatherLocation()),
        )

        composeRule.setHomeContent(state = state, widthDp = 360, heightDp = 640)

        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-page-title").assertTextContains("Details")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 4 of 4")
        composeRule.onNodeWithTag("home-section-metrics").assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-comfort").assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-wind").assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-atmosphere").assertExists()
        composeRule.onNodeWithContentDescription("Comfort, Feels like, 63 deg F, Humidity, 72%, Dew point, 53 deg F").assertExists()
        composeRule.onNodeWithContentDescription("Wind, Wind, 14 km/h, gust 25 km/h, 225 deg").assertExists()
        composeRule.onNodeWithContentDescription("Atmosphere, Pressure, 1012 hPa, Visibility, 9.5 km, Cloud cover, 88%, Precipitation, 0.4 mm").assertExists()
        composeRule.onNodeWithTag("home-section-source").assertIsDisplayed()
        composeRule.onNodeWithText("Source and updates").assertIsDisplayed()
        composeRule.onNodeWithText("Open-Meteo").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Fetched Aug 22, 7:00 AM CDT").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Issued Aug 22, 6:45 AM CDT").assertExists()
        composeRule.assertWithinRootBounds(
            "home-page-title",
            "home-section-comfort",
            "home-section-wind",
        )
        composeRule.assertCheckedSiblingSpacing(
            "home-section-metrics",
            "home-section-source",
        )
        composeRule.onNodeWithText("Weather data by Open-Meteo.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Issued Aug 22, 6:45 AM CDT").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Forecast requests send this location's coordinates and timezone to Open-Meteo.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.writeSemanticsArtifact("details-compact-semantics.txt")
    }

    @Test
    fun detailsPageOmitsMissingMetricGroupsWithoutInventingValues() {
        val location = weatherLocation(name = "Sparse Details City")
        val weather = fullWeatherBundle(location).copy(
            current = CurrentConditions(
                time = Instant.parse("2026-08-22T10:30:00Z"),
                temperatureC = 18.4,
                apparentTemperatureC = null,
                dewPointC = null,
                humidityPercent = null,
                pressureHpa = null,
                visibilityMeters = null,
                cloudCoverPercent = null,
                wind = Wind(
                    speedMetersPerSecond = 4.0,
                    gustMetersPerSecond = null,
                    directionDegrees = null,
                ),
                precipitationMm = null,
                condition = WeatherCondition.RAIN_SHOWERS,
                provenance = forecastProvenance(),
            ),
        )
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = location,
            weather = weather,
        )

        composeRule.setHomeContent(state = state)

        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-section-comfort").assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-wind").assertIsDisplayed()
        composeRule.onAllNodesWithTag("home-section-atmosphere").assertCountEquals(0)
        composeRule.onAllNodesWithText("Pressure").assertCountEquals(0)
        composeRule.onAllNodesWithText("Visibility").assertCountEquals(0)
        composeRule.onAllNodesWithText("Cloud cover").assertCountEquals(0)
        composeRule.onAllNodesWithText("Precipitation").assertCountEquals(0)
        composeRule.onAllNodesWithText("0 deg F").assertCountEquals(0)
        composeRule.onAllNodesWithText("0%").assertCountEquals(0)
    }

    @Test
    fun detailsAndNowContextUseMetricIdentityWhenDisplayLabelsChange() {
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = weatherLocation(),
            weather = fullWeatherBundle(weatherLocation()),
        ).let { ready ->
            ready.copy(
                dashboard = ready.dashboard.copy(
                    metrics = ready.dashboard.metrics.map { metric ->
                        when (metric.identity) {
                            HomeMetricIdentity.Humidity -> metric.copy(label = "Relative humidity")
                            HomeMetricIdentity.Wind -> metric.copy(label = "Breeze")
                            HomeMetricIdentity.ApparentTemperature -> metric.copy(label = "Feels")
                            HomeMetricIdentity.DewPoint -> metric.copy(label = "Dew")
                            HomeMetricIdentity.Pressure -> metric.copy(label = "Barometer")
                            HomeMetricIdentity.Visibility -> metric.copy(label = "Sightline")
                            HomeMetricIdentity.CloudCover -> metric.copy(label = "Clouds")
                            HomeMetricIdentity.Precipitation -> metric.copy(label = "Recent rain")
                        }
                    },
                ),
            )
        }

        composeRule.setHomeContent(state)

        composeRule.onNodeWithText("Relative humidity").assertIsDisplayed()
        composeRule.onNodeWithText("Breeze").assertIsDisplayed()
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-section-comfort").assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-wind").assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-atmosphere").assertExists()
        composeRule.onNodeWithContentDescription("Comfort, Feels, 63 deg F, Relative humidity, 72%, Dew, 53 deg F").assertExists()
        composeRule.onNodeWithContentDescription("Wind, Breeze, 14 km/h, gust 25 km/h, 225 deg").assertExists()
        composeRule.onNodeWithContentDescription("Atmosphere, Barometer, 1012 hPa, Sightline, 9.5 km, Clouds, 88%, Recent rain, 0.4 mm").assertExists()
    }

    @Test
    fun staleSuccessKeepsForecastContentRefreshAndRefreshFailureVisible() {
        val location = weatherLocation(name = "Stale Cache City")
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = location,
            weather = fullWeatherBundle(location),
            freshness = ForecastFreshness.StaleAfterFailedRefresh(
                staleAge = Duration.ofMinutes(45),
                refreshFailure = ForecastError.NetworkUnavailable,
            ),
        )

        composeRule.setHomeContent(state)

        composeRule.onNodeWithTag("home-section-stale").assertIsDisplayed()
        composeRule.onNodeWithText("Cached forecast").assertIsDisplayed()
        composeRule.onNodeWithText("Showing cached forecast from 45 minutes ago because refresh failed.").assertExists()
        composeRule.onNodeWithText("Refresh failed: Refresh could not reach the weather service or network.").assertExists()
        composeRule.onNodeWithTag("home-refresh").assertIsDisplayed()
        composeRule.onNodeWithText("Refresh").assertIsDisplayed()
        composeRule.onAllNodesWithText("Retry").assertCountEquals(0)
        composeRule.onNodeWithText("65 deg F").assertIsDisplayed()
        composeRule.assertVerticalOrder(
            "home-section-location",
            "home-section-current",
            "home-section-stale",
            "home-section-alert",
        )
        composeRule.assertNowHeroDominatesLocationChrome()
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-section-status").assertIsDisplayed()
        composeRule.onNodeWithText("Showing cached forecast from 45 minutes ago because refresh failed.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Refresh failed: Refresh could not reach the weather service or network.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("Open-Meteo").assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-source").assertIsDisplayed()
        composeRule.assertVerticalOrder(
            "home-section-metrics",
            "home-section-source",
            "home-section-status",
        )
        composeRule.writeSemanticsArtifact("stale-dashboard-semantics.txt")
    }

    @Test
    fun restoredCacheSuccessKeepsForecastContentAndStatusReachableAcrossPages() {
        val location = weatherLocation(name = "Restored Cache City")
        val state = HomeForecastPresentationState.ForecastReady.fromRestoredCache(
            location = location,
            weather = fullWeatherBundle(location),
            staleAge = Duration.ofMinutes(45),
        )

        composeRule.setHomeContent(state)

        composeRule.onNodeWithTag("home-section-stale").assertIsDisplayed()
        composeRule.onNodeWithText("Cached forecast").assertIsDisplayed()
        composeRule.onNodeWithText("Showing cached forecast from 45 minutes ago while Oxygen refreshes this location.")
            .assertExists()
        composeRule.onNodeWithText("65 deg F").assertIsDisplayed()
        composeRule.onNodeWithTag("home-page-tab-hourly").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-hourly-grid").assertIsDisplayed()
        composeRule.onNodeWithTag("home-page-tab-daily").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-daily-list").assertIsDisplayed()
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-section-status").assertIsDisplayed()
        composeRule.onNodeWithText("Showing cached forecast from 45 minutes ago while Oxygen refreshes this location.")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-source").assertIsDisplayed()
        composeRule.onNodeWithText("Weather data by Open-Meteo.").performScrollTo().assertIsDisplayed()
        composeRule.writeSemanticsArtifact("restored-cache-dashboard-semantics.txt")
    }

    @Test
    fun loadingKeepsAboutAndDisclosureReachable() {
        val location = weatherLocation(name = "Retry City")

        composeRule.setHomeContent(HomeForecastPresentationState.Loading.from(location))
        composeRule.onNodeWithText("Loading weather for Retry City").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.onNodeWithText("Weather data by Open-Meteo.").assertIsDisplayed()
        composeRule.writeSemanticsArtifact("loading-semantics.txt")
    }

    @Test
    fun noCacheErrorKeepsAboutDisclosureAndRetryReachable() {
        val location = weatherLocation(name = "Retry City")

        composeRule.setHomeContent(
            HomeForecastPresentationState.NoCacheError.from(
                location = location,
                message = HomeForecastMessage.NetworkUnavailable,
            ),
        )
        composeRule.onNodeWithText(HomeForecastMessage.NetworkUnavailable.text).assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
        composeRule.writeSemanticsArtifact("no-cache-error-semantics.txt")
    }

    @Test
    fun compactLargeFontDashboardSectionsHaveReadableRenderedBounds() {
        val location = weatherLocation(
            name = "A Very Long Selected Location Name Near The Lakefront, Wisconsin, United States",
        )
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = location,
            weather = fullWeatherBundle(
                location = location,
                provenance = forecastProvenance(
                    sourceName = "Open-Meteo Long Provider Attribution Name",
                    licenseId = "Creative Commons Attribution 4.0 International",
                ),
            ),
            freshness = ForecastFreshness.StaleAfterFailedRefresh(
                staleAge = Duration.ofMinutes(95),
                refreshFailure = ForecastError.ProviderUnavailable("open-meteo"),
            ),
        )

        composeRule.setHomeContent(state = state, widthDp = 360, fontScale = 1.3f)

        composeRule.onNodeWithText(location.displayName).assertIsDisplayed()
        composeRule.onNodeWithText("Refresh").assertIsDisplayed()
        composeRule.assertReadableBoundsAfterScroll(
            "home-section-location",
            "home-section-stale",
            "home-section-current",
        )
        composeRule.assertCheckedSiblingSpacing(
            "home-section-location",
            "home-section-current",
            "home-section-stale",
        )
        composeRule.onNodeWithTag("home-page-tab-hourly").performClick()
        composeRule.waitForIdle()
        composeRule.assertReadableBoundsAfterScroll(
            "home-section-hourly",
        )
        composeRule.onNodeWithTag("home-page-tab-daily").performClick()
        composeRule.waitForIdle()
        composeRule.assertReadableBoundsAfterScroll(
            "home-daily-entry-0",
            "home-daily-entry-1",
            "home-daily-entry-2",
            "home-daily-entry-3",
        )
        composeRule.assertCheckedSiblingSpacing(
            "home-daily-entry-0",
            "home-daily-entry-1",
            "home-daily-entry-2",
            "home-daily-entry-3",
        )
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Open-Meteo Long Provider Attribution Name").assertExists()
        composeRule.assertReadableBoundsAfterScroll(
            "home-section-metrics",
            "home-section-comfort",
            "home-section-wind",
            "home-section-atmosphere",
            "home-section-source",
            "home-section-sun",
            "home-section-provenance-footer",
        )
        composeRule.assertCheckedSiblingSpacing(
            "home-section-metrics",
            "home-section-source",
            "home-section-sun",
            "home-section-provenance-footer",
        )
    }

    @Test
    fun refreshInProgressKeepsDashboardAccessibleAndRefreshDisabled() {
        val location = weatherLocation(name = "Refresh Progress City")
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = location,
            weather = fullWeatherBundle(location),
        ).copy(
            isRefreshInProgress = true,
            refreshInProgressText = "Refreshing weather for Refresh Progress City",
        )

        composeRule.setHomeContent(state)

        composeRule.onNodeWithTag("home-refreshing").assertIsDisplayed()
        composeRule.onNodeWithText("Refreshing weather for Refresh Progress City").assertIsDisplayed()
        composeRule.onNodeWithTag("home-refresh").assertIsNotEnabled()
        composeRule.onNodeWithText("65 deg F").assertIsDisplayed()
        composeRule.onAllNodesWithText("Retry").assertCountEquals(0)
        composeRule.assertVerticalOrder(
            "home-section-location",
            "home-section-current",
            "home-refreshing",
            "home-section-alert",
        )
        composeRule.assertNowHeroDominatesLocationChrome()
        composeRule.writeSemanticsArtifact("refresh-in-progress-semantics.txt")
    }

    @Test
    fun oxygenAppRefreshClickRequestsExactSelectedLocationOnce() {
        val location = weatherLocation(id = "manual-click-refresh", name = "Click Refresh City")
        val repository = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(fullWeatherBundle(location))),
            listOf(WeatherRepositoryResult.Success(fullWeatherBundle(location))),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            forecastExecutor = DirectExecutor,
        )

        composeRule.setContent {
            OxygenApp(stateHolder = stateHolder)
        }
        composeRule.waitForIdle()

        assertEquals(listOf(location), repository.locations)
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Details")
        composeRule.onNodeWithTag("home-refresh").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(location, location), repository.locations)
    }

    @Test
    fun oxygenAppStaleRefreshClickRequestsExactSelectedLocationOnce() {
        val location = weatherLocation(id = "manual-click-stale-refresh", name = "Click Stale City")
        val repository = RecordingWeatherRepository(
            listOf(
                WeatherRepositoryResult.Success(
                    weather = fullWeatherBundle(location),
                    freshness = ForecastFreshness.StaleAfterFailedRefresh(
                        staleAge = Duration.ofMinutes(45),
                        refreshFailure = ForecastError.NetworkUnavailable,
                    ),
                ),
            ),
            listOf(WeatherRepositoryResult.Success(fullWeatherBundle(location))),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            forecastExecutor = DirectExecutor,
        )

        composeRule.setContent {
            OxygenApp(stateHolder = stateHolder)
        }
        composeRule.waitForIdle()

        assertEquals(listOf(location), repository.locations)
        composeRule.onNodeWithTag("home-refresh").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(location, location), repository.locations)
        composeRule.onAllNodesWithText("Retry").assertCountEquals(0)
    }

    @Test
    fun oxygenAppChangeLocationReturnsToManualSearchAndSelectsNewResult() {
        val oldLocation = weatherLocation(id = "manual-old-compose", name = "Old Compose City")
        val newLocation = weatherLocation(id = "manual-new-compose", name = "New Compose City")
        val repository = RecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(fullWeatherBundle(oldLocation))),
            listOf(WeatherRepositoryResult.Loading),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = oldLocation,
            geocodingRepository = StaticGeocodingRepository(newLocation),
            weatherRepository = repository,
            searchExecutor = DirectExecutor,
            forecastExecutor = DirectExecutor,
        )

        composeRule.setContent {
            OxygenApp(stateHolder = stateHolder)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home-change-location").assertIsDisplayed()
        composeRule.onNodeWithTag("home-change-location").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Choose a location").assertIsDisplayed()
        composeRule.onNodeWithTag("location-entry-back").assertIsDisplayed()
        composeRule.onNodeWithText("Back").assertIsDisplayed()
        composeRule.onNodeWithTag("location-entry-back").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Old Compose City").assertIsDisplayed()
        composeRule.onNodeWithTag("home-change-location").assertIsDisplayed()

        composeRule.onNodeWithTag("home-change-location").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Choose a location").assertIsDisplayed()
        composeRule.onNodeWithText("Search for a location").performTextInput("New Compose City")
        composeRule.onNodeWithText("Search").performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("New Compose City").assertCountEquals(2)
        composeRule.onNodeWithText("Use now").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(oldLocation, newLocation), repository.locations)
        composeRule.onNodeWithText("Loading weather for New Compose City").assertIsDisplayed()
    }
}

private fun ComposeContentTestRule.setHomeContent(
    state: HomeForecastPresentationState,
    widthDp: Int? = null,
    heightDp: Int = 3200,
    fontScale: Float = 1f,
    appearance: OxygenAppearance = OxygenAppearance(),
    themeId: OxygenThemeId = appearance.theme,
    onRetry: () -> Unit = {},
) {
    setContent {
        CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = fontScale)) {
            OxygenTheme(themeId = themeId) {
                if (widthDp == null) {
                    HomeLoadingScreen(state = state, appearance = appearance, onRetry = onRetry)
                } else {
                    Box(
                        Modifier
                            .width(widthDp.dp)
                            .height(heightDp.dp),
                    ) {
                        HomeLoadingScreen(state = state, appearance = appearance, onRetry = onRetry)
                    }
                }
            }
        }
    }
}

private fun ComposeContentTestRule.setCompactOxygenAppContent(
    stateHolder: OxygenAppStateHolder,
    appearance: OxygenAppearance,
    widthDp: Int = 360,
    heightDp: Int = 640,
    fontScale: Float = 1.3f,
) {
    setContent {
        CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = fontScale)) {
            androidx.compose.foundation.layout.Box(
                Modifier
                    .width(widthDp.dp)
                    .height(heightDp.dp),
            ) {
                OxygenApp(stateHolder = stateHolder, appearance = appearance)
            }
        }
    }
}

private fun ComposeContentTestRule.setThemedHomeContent(
    state: HomeForecastPresentationState,
    appearance: OxygenAppearance,
    themeState: androidx.compose.runtime.MutableState<OxygenThemeId>,
) {
    setContent {
        CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1.3f)) {
            OxygenTheme(themeId = themeState.value) {
                Box(Modifier.width(360.dp).height(640.dp)) {
                    HomeLoadingScreen(state = state, appearance = appearance)
                }
            }
        }
    }
}

private fun ComposeContentTestRule.setDynamicHomeContent(
    state: androidx.compose.runtime.MutableState<HomeForecastPresentationState>,
    appearance: OxygenAppearance,
    onRetry: () -> Unit,
) {
    setContent {
        CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1.3f)) {
            OxygenTheme(themeId = appearance.theme) {
                Box(Modifier.width(360.dp).height(640.dp)) {
                    HomeLoadingScreen(state = state.value, appearance = appearance, onRetry = onRetry)
                }
            }
        }
    }
}

private fun ComposeTestRule.homeSemanticContract(): String = onRoot(useUnmergedTree = true)
    .printToString(maxDepth = 120)
    .lineSequence()
    .filter { line ->
        line.contains("Tag:") ||
            line.contains("Text =") ||
            line.contains("ContentDescription =") ||
            line.contains("CollectionInfo =") ||
            line.contains("CustomActions =")
    }
    .map { line ->
        line
            .replace(Regex("Node #\\d+ at \\([^\\n]*?\\)px"), "Node")
            .replace(Regex("action=[^)]*"), "action")
    }
    .joinToString("\n")

private data class NamedContrastPair(
    val name: String,
    val foreground: Color,
    val background: Color,
)

private fun ComposeTestRule.assertPaperContrastRoles() {
    val palette = com.oxygen.weather.app.ui.theme.oxygenThemeSpec(OxygenThemeId.PAPER).palette
    val pairs = listOf(
        NamedContrastPair("normal-on-background", Color(0xFF2A2722), palette.skyTop),
        NamedContrastPair("normal-on-strong-surface", Color(0xFF2A2722), palette.glassStrong),
        NamedContrastPair("supporting-on-background", palette.supportingContent, palette.skyTop),
        NamedContrastPair("supporting-on-strong-surface", palette.supportingContent, palette.glassStrong),
        NamedContrastPair("warning-on-strong-surface", palette.warning, palette.glassStrong),
    )
    pairs.forEach { pair ->
        assertTrue("${pair.name} must use opaque role colors", pair.foreground.alpha == 1f && pair.background.alpha == 1f)
        assertTrue(
            "${pair.name} contrast ${"%.2f".format(contrastRatio(pair.foreground, pair.background))} < 4.5",
            contrastRatio(pair.foreground, pair.background) >= 4.5,
        )
    }
}

private fun contrastRatio(first: Color, second: Color): Double {
    val firstLuminance = relativeLuminance(first)
    val secondLuminance = relativeLuminance(second)
    val lighter = maxOf(firstLuminance, secondLuminance)
    val darker = minOf(firstLuminance, secondLuminance)
    return (lighter + 0.05) / (darker + 0.05)
}

private fun relativeLuminance(color: Color): Double {
    fun linear(channel: Float): Double {
        val value = channel.toDouble()
        return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)
}

private fun ComposeTestRule.assertPaperMarkHasMeasuredContrast() {
    val surfacePixels = onRoot().captureToImage().toPixelMap()
    val surface = surfacePixels[0, 0]
    val markPixels = onNodeWithTag("home-current-mark").captureToImage().toPixelMap()
    val samples = mutableListOf<String>()
    var measuredPixelCount = 0
    for (y in 0 until markPixels.height) {
        for (x in 0 until markPixels.width) {
            val pixel = markPixels[x, y]
            if (pixel.alpha <= 0.05f) continue
            val composite = Color(
                red = pixel.red * pixel.alpha + surface.red * (1f - pixel.alpha),
                green = pixel.green * pixel.alpha + surface.green * (1f - pixel.alpha),
                blue = pixel.blue * pixel.alpha + surface.blue * (1f - pixel.alpha),
                alpha = 1f,
            )
            val distance = kotlin.math.abs(composite.red - surface.red) +
                kotlin.math.abs(composite.green - surface.green) +
                kotlin.math.abs(composite.blue - surface.blue)
            if (distance > 0.08f && kotlin.math.abs(relativeLuminance(composite) - relativeLuminance(surface)) > 0.02) {
                measuredPixelCount++
                if (samples.size < 8) samples += "($x,$y)"
            }
        }
    }
    assertTrue("Paper condition mark must contrast its sampled surface", measuredPixelCount > 0)
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    context.filesDir.resolve("paper-mark-sampling.txt").writeText(
        "surface=${surface.red},${surface.green},${surface.blue}; " +
            "sampledCoordinates=${samples.joinToString()}; measuredPixels=$measuredPixelCount",
    )
}

private fun ComposeContentTestRule.setCompactContent(
    widthDp: Int = 360,
    heightDp: Int = 640,
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    setContent {
        CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = fontScale)) {
            OxygenTheme {
                Box(
                    Modifier
                        .width(widthDp.dp)
                        .height(heightDp.dp),
                ) {
                    content()
                }
            }
        }
    }
}

private fun manualCandidate(location: WeatherLocation): ManualLocationCandidate =
    ManualLocationCandidate(
        id = location.id,
        title = location.displayName,
        subtitle = "Wisconsin, United States",
        coordinateText = "43.0731, -89.4012",
        timezoneText = location.zoneId.id,
        location = location,
    )

private object DirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class RecordingWeatherRepository(
    private vararg val responses: List<WeatherRepositoryResult>,
) : WeatherRepository {
    val locations = mutableListOf<WeatherLocation>()
    private var callIndex = 0

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        locations += location
        val response = responses.getOrElse(callIndex) { responses.last() }
        callIndex += 1
        return response.asSequence()
    }
}

private class StaticGeocodingRepository(
    private val location: WeatherLocation,
) : GeocodingRepository {
    override fun search(
        query: String,
        count: Int,
        language: String?,
        countryCode: String?,
    ): Sequence<GeocodingRepositoryResult> =
        sequenceOf(
            GeocodingRepositoryResult.Success(
                listOf(
                    com.oxygen.weather.core.model.GeocodingLocationCandidate(
                        locationId = location.id,
                        displayName = location.displayName,
                        point = location.point,
                        zoneId = location.zoneId,
                        country = "United States",
                        countryCode = "US",
                    ),
                ),
            ),
        )
}

private class RecordingSavedLocationStorage(
    initialLocations: List<WeatherLocation>,
) : SavedLocationStorage {
    private val locations = initialLocations.toMutableList()
    val removals = mutableListOf<LocationId>()

    override fun saveLocation(location: WeatherLocation) {
        locations.removeAll { it.id == location.id }
        locations += location
    }

    override fun listLocations(): List<WeatherLocation> = locations.toList()

    override fun removeLocation(locationId: LocationId) {
        removals += locationId
        locations.removeAll { it.id == locationId }
    }
}

private object FailingSavedLocationStorage : SavedLocationStorage {
    override fun saveLocation(location: WeatherLocation) = error("saved-location save failed")

    override fun listLocations(): List<WeatherLocation> = error("saved-location list failed")

    override fun removeLocation(locationId: LocationId) = error("saved-location remove failed")
}

private fun ComposeTestRule.assertVerticalOrder(vararg tags: String) {
    val tops = tags.map { tag ->
        tag to onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.top
    }
    tops.zipWithNext().forEach { (before, after) ->
        assertTrue("${before.first} should render above ${after.first}", before.second < after.second)
    }
}

private fun ComposeTestRule.assertSemanticsTreeOrder(vararg tags: String) {
    val tree = onRoot().printToString()
    tags.toList().zipWithNext().forEach { (before, after) ->
        assertTrue(
            "$before should precede $after in the rendered Home tree",
            tree.indexOf("Tag: '$before'") < tree.indexOf("Tag: '$after'"),
        )
    }
}

private fun ComposeTestRule.assertReadableBoundsAfterScroll(vararg tags: String) {
    tags.forEach { tag ->
        onNodeWithTag(tag).performScrollTo()
        val rect = onAllNodesWithTag(tag).fetchSemanticsNodes().single().boundsInRoot
        assertTrue("$tag should have positive width", rect.width > 0f)
        assertTrue("$tag should have positive height", rect.height > 0f)
        assertTrue("$tag should fit compact root width", rect.left >= 0f && rect.right <= 360f)
    }
}

private fun ComposeTestRule.assertCheckedSiblingSpacing(vararg tags: String) {
    val bounds = tags.associateWith { tag ->
        onAllNodesWithTag(tag).fetchSemanticsNodes().single().boundsInRoot
    }
    tags.toList().zipWithNext().forEach { (beforeTag, afterTag) ->
        val before = requireNotNull(bounds[beforeTag])
        val after = requireNotNull(bounds[afterTag])
        assertTrue("$beforeTag should not overlap $afterTag", before.bottom <= after.top)
    }
}

private fun ComposeTestRule.assertWithinRootBounds(vararg tags: String) {
    tags.forEach { tag ->
        val rect = onAllNodesWithTag(tag).fetchSemanticsNodes().single().boundsInRoot
        assertTrue("$tag should have positive width", rect.width > 0f)
        assertTrue("$tag should have positive height", rect.height > 0f)
        assertTrue("$tag should stay inside compact root width", rect.left >= 0f && rect.right <= 360f)
        assertTrue("$tag should stay inside first compact viewport", rect.top >= 0f && rect.bottom <= 640f)
    }
}

private fun ComposeTestRule.assertMinimumTouchTarget(vararg tags: String) {
    tags.forEach { tag ->
        val rect = onAllNodesWithTag(tag).fetchSemanticsNodes().single().boundsInRoot
        assertTrue("$tag should be at least 48dp wide", rect.width >= 48f)
        assertTrue("$tag should be at least 48dp tall", rect.height >= 48f)
    }
}

private fun ComposeTestRule.assertMinimumTouchTargetAfterScroll(vararg tags: String) {
    tags.forEach { tag ->
        onNodeWithTag(tag).performScrollTo()
        val rect = onAllNodesWithTag(tag).fetchSemanticsNodes().single().boundsInRoot
        assertTrue("$tag should be at least 48dp wide", rect.width >= 48f)
        assertTrue("$tag should be at least 48dp tall", rect.height >= 48f)
    }
}

private fun ComposeTestRule.assertInLowerReachZone(tag: String, rootHeight: Float) {
    val rect = onAllNodesWithTag(tag).fetchSemanticsNodes().single().boundsInRoot
    assertTrue("$tag should have positive height", rect.height > 0f)
    assertTrue("$tag should be in lower half of compact phone", rect.top >= rootHeight * 0.48f)
    assertTrue("$tag should stay visible above bottom edge", rect.bottom <= rootHeight)
}

private fun ComposeTestRule.assertSecondaryActionsUseLessWidthThanPageTabs() {
    val pageSelector = onNodeWithTag("home-page-selector").fetchSemanticsNode().boundsInRoot
    val secondaryActionWidth = listOf(
        "home-change-location",
        "home-refresh",
        "home-about-entry",
    ).sumOf { tag ->
        onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.width.toDouble()
    }.toFloat()

    assertTrue(
        "Secondary actions should not take the same visual width as primary Home page navigation",
        secondaryActionWidth < pageSelector.width,
    )
}

private fun SemanticsNodeInteraction.assertCustomActions(vararg labels: String) {
    val actions = fetchSemanticsNode().config.getOrElse(SemanticsActions.CustomActions) { emptyList() }
    val actualLabels = actions.map { it.label }
    labels.forEach { label ->
        assertTrue("Expected custom action '$label' in $actualLabels", actualLabels.contains(label))
    }
}

private fun ComposeTestRule.performPagerCustomAction(label: String) {
    val actions = onNodeWithTag("home-page-container")
        .fetchSemanticsNode()
        .config
        .getOrElse(SemanticsActions.CustomActions) { emptyList() }
    val action = actions.singleOrNull { it.label == label }
    assertTrue("Expected exactly one custom action '$label'", action != null)
    runOnIdle {
        assertTrue("Custom action '$label' should report handled", action!!.action.invoke())
    }
}

private fun ComposeTestRule.assertNowHeroDominatesLocationChrome() {
    val location = onNodeWithTag("home-section-location").fetchSemanticsNode().boundsInRoot
    val current = onNodeWithTag("home-section-current").fetchSemanticsNode().boundsInRoot
    assertTrue("Now current hero should render below location chrome", location.bottom <= current.top)
    assertTrue(
        "Now current hero should occupy more vertical space than location chrome",
        current.height > location.height,
    )
}

private fun SemanticsNodeInteraction.assertHasGoldLinePixels(tag: String) {
    val pixels = captureToImage().toPixelMap()
    var goldPixelCount = 0
    for (x in 0 until pixels.width) {
        for (y in 0 until pixels.height) {
            val color = pixels[x, y]
            if (color.red > 0.70f && color.green > 0.46f && color.blue < 0.68f && color.alpha > 0.45f) {
                goldPixelCount += 1
            }
        }
    }
    assertTrue("$tag should render visible art-sheet gold line pixels", goldPixelCount > 24)
}

private fun ComposeTestRule.writeSemanticsArtifact(fileName: String) {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val artifact = File(context.filesDir, fileName)
    artifact.writeText(onRoot(useUnmergedTree = true).printToString(maxDepth = 120))
    assertTrue("$fileName should contain the Home semantics tree", artifact.readText().contains("OXYGEN"))
}

private fun ComposeTestRule.writeScreenshotArtifact(fileName: String) {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val screenshot = onRoot().captureToImage().asAndroidBitmap()
    context.filesDir.resolve(fileName).outputStream().use { output ->
        assertTrue("$fileName should be written", screenshot.compress(Bitmap.CompressFormat.PNG, 100, output))
    }
}

private fun weatherLocation(
    id: String = "manual-madison",
    name: String = "Madison, Wisconsin, United States",
): WeatherLocation =
    WeatherLocation(
        id = LocationId(id),
        displayName = name,
        point = GeoPoint(43.0731, -89.4012),
        zoneId = ZoneId.of("America/Chicago"),
    )

private fun fullWeatherBundle(
    location: WeatherLocation,
    provenance: DataProvenance = forecastProvenance(),
): WeatherBundle =
    WeatherBundle(
        location = location,
        current = CurrentConditions(
            time = Instant.parse("2026-08-22T10:30:00Z"),
            temperatureC = 18.4,
            apparentTemperatureC = 17.2,
            dewPointC = 11.6,
            humidityPercent = 72,
            pressureHpa = 1012.4,
            visibilityMeters = 9500.0,
            cloudCoverPercent = 88,
            wind = Wind(
                speedMetersPerSecond = 4.0,
                gustMetersPerSecond = 7.0,
                directionDegrees = 225.0,
            ),
            precipitationMm = 0.4,
            condition = WeatherCondition.RAIN_SHOWERS,
            provenance = provenance,
        ),
        hourly = listOf(
            HourlyForecast(
                time = Instant.parse("2026-08-22T11:00:00Z"),
                temperatureC = 18.0,
                precipitationProbabilityPercent = 60,
                precipitationMm = 1.2,
                condition = WeatherCondition.RAIN,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            HourlyForecast(
                time = Instant.parse("2026-08-22T12:00:00Z"),
                temperatureC = 19.2,
                precipitationProbabilityPercent = null,
                precipitationMm = null,
                condition = WeatherCondition.CLOUDY,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            HourlyForecast(
                time = Instant.parse("2026-08-22T13:00:00Z"),
                temperatureC = 20.0,
                precipitationProbabilityPercent = 20,
                precipitationMm = 0.2,
                condition = WeatherCondition.PARTLY_CLOUDY,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            HourlyForecast(
                time = Instant.parse("2026-08-22T14:00:00Z"),
                temperatureC = 21.1,
                precipitationProbabilityPercent = 10,
                precipitationMm = 0.0,
                condition = WeatherCondition.MOSTLY_CLEAR,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            HourlyForecast(
                time = Instant.parse("2026-08-22T15:00:00Z"),
                temperatureC = 22.0,
                precipitationProbabilityPercent = null,
                precipitationMm = null,
                condition = WeatherCondition.THUNDERSTORM,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            HourlyForecast(
                time = Instant.parse("2026-08-22T16:00:00Z"),
                temperatureC = 21.5,
                precipitationProbabilityPercent = 40,
                precipitationMm = 0.8,
                condition = WeatherCondition.RAIN_SHOWERS,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
        ),
        daily = listOf(
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-22").toEpochDay(),
                highC = 22.7,
                lowC = 12.3,
                precipitationProbabilityPercent = 40,
                condition = WeatherCondition.RAIN_SHOWERS,
                sunrise = Instant.parse("2026-08-22T10:15:00Z"),
                sunset = Instant.parse("2026-08-23T01:01:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-23").toEpochDay(),
                highC = 21.1,
                lowC = 11.2,
                precipitationProbabilityPercent = null,
                condition = WeatherCondition.CLOUDY,
                sunrise = Instant.parse("2026-08-23T10:16:00Z"),
                sunset = Instant.parse("2026-08-24T00:59:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-24").toEpochDay(),
                highC = 24.8,
                lowC = 14.1,
                precipitationProbabilityPercent = 20,
                condition = WeatherCondition.PARTLY_CLOUDY,
                sunrise = Instant.parse("2026-08-24T10:17:00Z"),
                sunset = Instant.parse("2026-08-25T00:57:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-25").toEpochDay(),
                highC = 27.6,
                lowC = 16.1,
                precipitationProbabilityPercent = 10,
                condition = WeatherCondition.MOSTLY_CLEAR,
                sunrise = Instant.parse("2026-08-25T10:18:00Z"),
                sunset = Instant.parse("2026-08-26T00:55:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-26").toEpochDay(),
                highC = null,
                lowC = 15.0,
                precipitationProbabilityPercent = 50,
                condition = WeatherCondition.THUNDERSTORM,
                sunrise = Instant.parse("2026-08-26T10:19:00Z"),
                sunset = Instant.parse("2026-08-27T00:53:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-27").toEpochDay(),
                highC = 19.5,
                lowC = null,
                precipitationProbabilityPercent = null,
                condition = WeatherCondition.RAIN,
                sunrise = Instant.parse("2026-08-27T10:20:00Z"),
                sunset = Instant.parse("2026-08-28T00:51:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
        ),
        alerts = listOf(
            WeatherAlert(
                id = "alert-1",
                event = "Flood Watch",
                headline = "Flooding possible near rivers",
                severity = AlertSeverity.MODERATE,
                effective = Instant.parse("2026-08-22T12:00:00Z"),
                expires = Instant.parse("2026-08-22T18:00:00Z"),
                issuer = "Test Weather Office",
                provenance = provenance.copy(type = DataType.OFFICIAL_ALERT),
            ),
        ),
        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
    )

private fun forecastProvenance(
    type: DataType = DataType.MODEL_ESTIMATE,
    sourceName: String = "Open-Meteo",
    licenseId: String = "CC BY 4.0",
): DataProvenance =
    DataProvenance(
        providerId = "internal-provider-id",
        sourceName = sourceName,
        issuedAt = Instant.parse("2026-08-22T11:45:00Z"),
        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
        type = type,
        licenseId = licenseId,
    )

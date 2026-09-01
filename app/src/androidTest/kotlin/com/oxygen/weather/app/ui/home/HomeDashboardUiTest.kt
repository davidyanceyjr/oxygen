package com.oxygen.weather.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
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
import com.oxygen.weather.app.HomeForecastMessage
import com.oxygen.weather.app.HomeForecastPresentationState
import com.oxygen.weather.app.HomeSuccessSection
import com.oxygen.weather.app.OxygenApp
import com.oxygen.weather.app.OxygenAppStateHolder
import com.oxygen.weather.app.ui.components.WeatherConditionMark
import com.oxygen.weather.app.ui.theme.OxygenTheme
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeDashboardUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun freshSuccessRendersSemanticHomePagesAndPreservesDashboardContent() {
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = weatherLocation(),
            weather = fullWeatherBundle(weatherLocation()),
        )

        composeRule.setHomeContent(state)

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

        composeRule.assertVerticalOrder(
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
        composeRule.onNodeWithTag("home-section-alert").assertIsDisplayed()
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
        composeRule.onNodeWithTag("home-section-sun").assertIsDisplayed()
        assertEquals(
            listOf(
                HomeSuccessSection.LocationHeader,
                HomeSuccessSection.Alerts,
                HomeSuccessSection.Current,
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
        composeRule.onNodeWithText("Open-Meteo").assertIsDisplayed()
        composeRule.onNodeWithText("Fetched Aug 22, 7:00 AM CDT").assertIsDisplayed()
        composeRule.onNodeWithText("Issued Aug 22, 6:45 AM CDT").assertExists()
        composeRule.assertWithinRootBounds(
            "home-page-title",
            "home-section-comfort",
            "home-section-wind",
            "home-section-source",
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
            "home-section-stale",
            "home-section-current",
            "home-section-alert",
        )
        composeRule.assertNowHeroDominatesLocationChrome()
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-section-status").assertIsDisplayed()
        composeRule.onNodeWithText("Showing cached forecast from 45 minutes ago because refresh failed.").assertIsDisplayed()
        composeRule.onNodeWithText("Refresh failed: Refresh could not reach the weather service or network.").assertIsDisplayed()
        composeRule.onNodeWithText("Open-Meteo").assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-source").assertIsDisplayed()
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
        composeRule.onNodeWithText("Settings / About").assertIsDisplayed()
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
        composeRule.onNodeWithText("Settings / About").assertIsDisplayed()
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
            "home-section-stale",
            "home-section-current",
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
            "home-refreshing",
            "home-section-current",
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
        composeRule.onNodeWithText("Search for a location").performTextInput("New Compose City")
        composeRule.onNodeWithText("Search").performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("New Compose City").assertCountEquals(2)
        composeRule.onNodeWithText("Select").performClick()
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
) {
    setContent {
        CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = fontScale)) {
            OxygenTheme {
                if (widthDp == null) {
                    HomeLoadingScreen(state = state)
                } else {
                    Box(
                        Modifier
                            .width(widthDp.dp)
                            .height(heightDp.dp),
                    ) {
                        HomeLoadingScreen(state = state)
                    }
                }
            }
        }
    }
}

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

private fun ComposeTestRule.assertVerticalOrder(vararg tags: String) {
    val tops = tags.map { tag ->
        tag to onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.top
    }
    tops.zipWithNext().forEach { (before, after) ->
        assertTrue("${before.first} should render above ${after.first}", before.second < after.second)
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

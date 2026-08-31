package com.oxygen.weather.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
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
import androidx.compose.ui.test.printToString
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
        composeRule.onNodeWithTag("home-page-previous").assertIsNotEnabled()
        composeRule.onNodeWithTag("home-page-next").assertIsEnabled()
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
        composeRule.onNodeWithText("Sunrise 5:15 AM | Sunset 8:01 PM").assertExists()
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Details")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 4 of 4")
        composeRule.onNodeWithTag("home-page-next").assertIsNotEnabled()
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
    fun homePageNavigationSupportsBoundedButtonsAndHorizontalSwipe() {
        val state = HomeForecastPresentationState.ForecastReady.from(
            location = weatherLocation(),
            weather = fullWeatherBundle(weatherLocation()),
        )

        composeRule.setHomeContent(state)

        composeRule.onNodeWithTag("home-page-next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Hourly")
        composeRule.onNodeWithTag("home-page-previous").assertIsEnabled()

        composeRule.onNodeWithTag("home-page-container").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Daily")

        composeRule.onNodeWithTag("home-page-container").performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Hourly")

        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Details")
        composeRule.onNodeWithTag("home-page-next").assertIsNotEnabled()
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
        composeRule.onNodeWithText("Open-Meteo").assertIsDisplayed()
        composeRule.onNodeWithTag("home-section-source").assertIsDisplayed()
        composeRule.writeSemanticsArtifact("stale-dashboard-semantics.txt")
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
            "home-section-daily",
        )
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Open-Meteo Long Provider Attribution Name").assertExists()
        composeRule.assertReadableBoundsAfterScroll(
            "home-section-metrics",
            "home-section-source",
            "home-section-provenance-footer",
        )
        composeRule.assertCheckedSiblingSpacing(
            "home-section-metrics",
            "home-section-source",
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

package com.oxygen.weather.app.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oxygen.weather.app.OxygenApp
import com.oxygen.weather.app.OxygenAppStateHolder
import com.oxygen.weather.app.ThemePreferenceReadResult
import com.oxygen.weather.app.ThemePreferenceStorage
import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.app.ui.theme.OxygenAppearance
import com.oxygen.weather.app.ui.theme.OxygenThemeId
import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.time.Instant
import java.time.ZoneId
import java.util.ArrayDeque
import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemePreferenceUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun choicesCommitWithoutForecastRefetchAndUseConfirmedSelection() {
        val location = themeUiFixtureLocation("commit")
        val repository = ThemeUiRecordingRepository(location)
        val executor = ThemeUiControlledExecutor()
        val storage = ThemeUiStorage(stored = OxygenThemeId.OXYGEN)
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            themePreferenceStorage = storage,
            forecastExecutor = executor,
        )

        setContent(holder)
        drainUi(executor)
        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()

        composeRule.onNodeWithTag("settings-theme-oxygen").assertIsSelected()
        composeRule.onNodeWithTag("theme_preference_saved").performScrollTo().assertIsDisplayed()
        assertTouchHeightAtLeast("settings-theme-oxygen")
        assertTouchHeightAtLeast("settings-theme-paper")
        assertTouchHeightAtLeast("settings-theme-terminal")
        assertAbove("settings-theme-oxygen", "settings-theme-paper")
        assertAbove("settings-theme-paper", "settings-theme-terminal")

        composeRule.onNodeWithTag("settings-theme-paper").performClick()
        composeRule.onNodeWithTag("theme_preference_loading").assertTextContains("Saving Paper...")
        composeRule.onNodeWithTag("settings-theme-oxygen").assertIsSelected()
        composeRule.onNodeWithTag("settings-theme-paper").assertIsNotEnabled()
        composeRule.onNodeWithTag("settings-theme-terminal").assertIsNotEnabled()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-theme-paper").assertIsSelected()
        composeRule.onNodeWithTag("theme_preference_saved").assertIsDisplayed()
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.theme)
        assertEquals(1, repository.refreshCount)

        composeRule.onNodeWithTag("settings-theme-terminal").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-theme-terminal").assertIsSelected()
        composeRule.onNodeWithTag("settings-theme-oxygen").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-theme-oxygen").assertIsSelected()
        assertEquals(1, repository.refreshCount)
        assertEquals(listOf(OxygenThemeId.PAPER, OxygenThemeId.TERMINAL, OxygenThemeId.OXYGEN), storage.writes)
    }

    @Test
    fun readAndWriteFailuresRetainConfirmedThemeAndRetryTheSameTarget() {
        val location = themeUiFixtureLocation("failure")
        val executor = ThemeUiControlledExecutor()
        val storage = ThemeUiStorage(stored = OxygenThemeId.OXYGEN, readFails = true)
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = ThemeUiRecordingRepository(location),
            themePreferenceStorage = storage,
            forecastExecutor = executor,
        )

        setContent(holder)
        drainUi(executor)
        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        composeRule.onNodeWithTag("theme_preference_error")
            .assertTextContains("Theme load failed; using Oxygen; retry")
        composeRule.onNodeWithTag("theme_preference_retry").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings-theme-oxygen").assertIsNotEnabled()

        storage.readFails = false
        composeRule.onNodeWithTag("theme_preference_retry").performClick()
        composeRule.onNodeWithTag("theme_preference_loading").assertIsDisplayed()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-theme-oxygen").assertIsSelected()

        storage.writeFails = true
        composeRule.onNodeWithTag("settings-theme-paper").performClick()
        composeRule.onNodeWithTag("theme_preference_loading").assertTextContains("Saving Paper...")
        drainUi(executor)
        composeRule.onNodeWithTag("settings-theme-oxygen").assertIsSelected()
        composeRule.onNodeWithTag("theme_preference_error").assertTextContains("Theme save failed; retry")
        composeRule.onNodeWithTag("theme_preference_retry").performScrollTo().assertIsDisplayed()

        storage.writeFails = false
        composeRule.onNodeWithTag("theme_preference_retry").performClick()
        composeRule.onNodeWithTag("theme_preference_loading").assertTextContains("Saving Paper...")
        drainUi(executor)
        composeRule.onNodeWithTag("settings-theme-paper").assertIsSelected()
        assertEquals(listOf(OxygenThemeId.PAPER), storage.writes)
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.theme)
    }

    private fun setContent(holder: OxygenAppStateHolder) {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.3f)) {
                Box(modifier = Modifier.width(360.dp).height(640.dp)) {
                    OxygenApp(
                        stateHolder = holder,
                        appearance = OxygenAppearance(effects = EffectsLevel.OFF),
                    )
                }
            }
        }
    }

    private fun drainUi(executor: ThemeUiControlledExecutor) {
        executor.drainAll()
        composeRule.waitForIdle()
    }

    private fun assertTouchHeightAtLeast(tag: String, minHeightDp: Float = 48f) {
        val bounds = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        assertTrue("Expected $tag height >= $minHeightDp dp but was ${bounds.height}", bounds.height >= minHeightDp)
    }

    private fun assertAbove(upperTag: String, lowerTag: String) {
        val upper = composeRule.onNodeWithTag(upperTag).fetchSemanticsNode().boundsInRoot
        val lower = composeRule.onNodeWithTag(lowerTag).fetchSemanticsNode().boundsInRoot
        assertTrue("Expected $upperTag above $lowerTag", upper.top <= lower.top)
    }
}

private class ThemeUiControlledExecutor : Executor {
    private val tasks = ArrayDeque<Runnable>()

    override fun execute(command: Runnable) {
        synchronized(tasks) { tasks.addLast(command) }
    }

    fun drainAll() {
        while (true) {
            val task = synchronized(tasks) {
                if (tasks.isEmpty()) null else tasks.removeFirst()
            } ?: return
            task.run()
        }
    }
}

private class ThemeUiStorage(
    var stored: OxygenThemeId,
    var readFails: Boolean = false,
    var writeFails: Boolean = false,
) : ThemePreferenceStorage {
    val writes = mutableListOf<OxygenThemeId>()

    override fun readThemePreference(): ThemePreferenceReadResult {
        if (readFails) error("theme read failed")
        return ThemePreferenceReadResult.Supported(stored)
    }

    override fun writeThemePreference(theme: OxygenThemeId) {
        if (writeFails) error("theme write failed")
        writes += theme
        stored = theme
    }
}

private class ThemeUiRecordingRepository(
    private val location: WeatherLocation,
) : WeatherRepository {
    var refreshCount = 0

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        refreshCount += 1
        return sequenceOf(
            WeatherRepositoryResult.Success(
                WeatherBundle(
                    location = this.location,
                    current = CurrentConditions(
                        time = Instant.parse("2026-09-08T12:00:00Z"),
                        temperatureC = 21.0,
                        condition = WeatherCondition.CLEAR,
                        provenance = DataProvenance(
                            providerId = "theme-ui-test",
                            sourceName = "Theme UI Test",
                            fetchedAt = Instant.parse("2026-09-08T12:00:00Z"),
                            type = DataType.FORECAST,
                        ),
                    ),
                    fetchedAt = Instant.parse("2026-09-08T12:00:00Z"),
                ),
            ),
        )
    }
}

private fun themeUiFixtureLocation(id: String): WeatherLocation = WeatherLocation(
    id = LocationId("theme-ui-$id"),
    displayName = "Theme UI Fixture $id",
    point = GeoPoint(43.0731, -89.4012),
    zoneId = ZoneId.of("America/Chicago"),
)

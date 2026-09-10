package com.oxygen.weather.app.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oxygen.weather.app.ContrastPreferenceReadResult
import com.oxygen.weather.app.ContrastPreferenceStorage
import com.oxygen.weather.app.OxygenApp
import com.oxygen.weather.app.OxygenAppStateHolder
import com.oxygen.weather.app.SettingsDestination
import com.oxygen.weather.app.ui.theme.ContrastLevel
import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.app.ui.theme.LayoutPreset
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
class ContrastPreferenceUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectionCommitsConfirmedContrastAndPreservesAppearanceWithoutRefetch() {
        val location = contrastUiLocation("selection")
        val executor = ContrastUiControlledExecutor()
        val repository = ContrastUiRepository(location)
        val storage = ContrastUiStorage(stored = ContrastLevel.STANDARD)
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            initialLayout = LayoutPreset.SIMPLE,
            initialTheme = OxygenThemeId.PAPER,
            contrastPreferenceStorage = storage,
            forecastExecutor = executor,
        )
        setContent(holder)
        drain(executor)
        openAppearance()

        composeRule.onNodeWithTag("settings-contrast-standard").performScrollTo().assertIsSelected()
        composeRule.onNodeWithTag("settings-contrast-high").assertIsNotSelected()
        assertTouchHeight("settings-contrast-standard")
        assertTouchHeight("settings-contrast-high")
        composeRule.onNodeWithTag("settings-contrast-high").performClick()
        composeRule.onNodeWithTag("contrast_preference_loading")
            .assertTextContains("Saving High...")
        composeRule.onNodeWithTag("settings-contrast-standard").assertIsSelected()
        composeRule.onNodeWithTag("settings-contrast-high").assertIsNotEnabled()

        drain(executor)
        composeRule.onNodeWithTag("settings-contrast-high").performScrollTo().assertIsSelected()
        composeRule.onNodeWithTag("contrast_preference_saved").performScrollTo().assertIsDisplayed()
        assertEquals(ContrastLevel.HIGH, holder.presentationState.contrast)
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.theme)
        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layout)
        assertEquals(1, repository.refreshCount)

        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").assertIsDisplayed()
    }

    @Test
    fun readAndWriteFailuresRetainConfirmedContrastAndRetry() {
        val location = contrastUiLocation("failure")
        val executor = ContrastUiControlledExecutor()
        val storage = ContrastUiStorage(readFails = true)
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = ContrastUiRepository(location),
            contrastPreferenceStorage = storage,
            forecastExecutor = executor,
        )
        setContent(holder)
        drain(executor)
        openAppearance()

        composeRule.onNodeWithTag("contrast_preference_error")
            .assertTextContains("Contrast load failed; using Standard; retry")
        composeRule.onNodeWithTag("settings-contrast-standard").assertIsNotEnabled()
        storage.readFails = false
        storage.stored = ContrastLevel.HIGH
        composeRule.onNodeWithTag("contrast_preference_retry").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(
                com.oxygen.weather.app.ContrastPreferenceReadState.Loading,
                holder.presentationState.contrastPreference.readState,
            )
        }
        composeRule.onNodeWithTag("settings-contrast-standard").performScrollTo().assertIsNotEnabled()
        drain(executor)
        composeRule.onNodeWithTag("settings-contrast-high").performScrollTo().assertIsSelected()

        storage.writeFails = true
        composeRule.onNodeWithTag("settings-contrast-standard").performScrollTo().performClick()
        drain(executor)
        composeRule.onNodeWithTag("settings-contrast-high").performScrollTo().assertIsSelected()
        composeRule.onNodeWithTag("contrast_preference_error").performScrollTo()
            .assertTextContains("Contrast save failed; retry")
        storage.writeFails = false
        composeRule.onNodeWithTag("contrast_preference_retry").performScrollTo().performClick()
        drain(executor)
        composeRule.onNodeWithTag("settings-contrast-standard").performScrollTo().assertIsSelected()
        assertEquals(listOf(ContrastLevel.STANDARD, ContrastLevel.STANDARD), storage.writes)
    }

    private fun setContent(holder: OxygenAppStateHolder) {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1.3f)) {
                Box(Modifier.width(360.dp).height(640.dp)) {
                    OxygenApp(
                        stateHolder = holder,
                        appearance = OxygenAppearance(effects = EffectsLevel.OFF),
                    )
                }
            }
        }
    }

    private fun openAppearance() {
        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
    }

    private fun drain(executor: ContrastUiControlledExecutor) {
        executor.drainAll()
        composeRule.waitForIdle()
    }

    private fun assertTouchHeight(tag: String) {
        assertTrue(
            "Expected $tag to be at least 48dp",
            composeRule.onNodeWithTag(tag).performScrollTo().fetchSemanticsNode().boundsInRoot.height >= 48f,
        )
    }
}

private class ContrastUiControlledExecutor : Executor {
    private val tasks = ArrayDeque<Runnable>()
    override fun execute(command: Runnable) = synchronized(tasks) { tasks.addLast(command) }
    fun drainAll() {
        while (true) {
            val task = synchronized(tasks) { if (tasks.isEmpty()) null else tasks.removeFirst() } ?: return
            task.run()
        }
    }
}

private class ContrastUiStorage(
    var stored: ContrastLevel? = null,
    var readFails: Boolean = false,
    var writeFails: Boolean = false,
) : ContrastPreferenceStorage {
    val writes = mutableListOf<ContrastLevel>()
    override fun readContrastPreference(): ContrastPreferenceReadResult {
        if (readFails) error("contrast read failed")
        return stored?.let(ContrastPreferenceReadResult::Supported)
            ?: ContrastPreferenceReadResult.NoSupportedChoice
    }
    override fun writeContrastPreference(contrast: ContrastLevel) {
        writes += contrast
        if (writeFails) error("contrast write failed")
        stored = contrast
    }
}

private class ContrastUiRepository(
    private val location: WeatherLocation,
) : WeatherRepository {
    var refreshCount = 0
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        refreshCount += 1
        return sequenceOf(WeatherRepositoryResult.Success(
            WeatherBundle(
                location = this.location,
                current = CurrentConditions(
                    time = Instant.parse("2026-09-08T12:00:00Z"),
                    temperatureC = 21.0,
                    condition = WeatherCondition.CLEAR,
                    provenance = DataProvenance(
                        providerId = "contrast-ui-test",
                        sourceName = "Contrast UI Test",
                        fetchedAt = Instant.parse("2026-09-08T12:00:00Z"),
                        type = DataType.FORECAST,
                    ),
                ),
                fetchedAt = Instant.parse("2026-09-08T12:00:00Z"),
            ),
        ))
    }
}

private fun contrastUiLocation(id: String) = WeatherLocation(
    id = LocationId(id),
    displayName = "Contrast UI $id",
    point = GeoPoint(43.0731, -89.4012),
    zoneId = ZoneId.of("America/Chicago"),
)

package com.oxygen.weather.app.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oxygen.weather.app.ContrastPreferenceReadResult
import com.oxygen.weather.app.ContrastPreferenceStorage
import com.oxygen.weather.app.EffectsPreferenceStorage
import com.oxygen.weather.app.LayoutPreferenceReadResult
import com.oxygen.weather.app.LayoutPreferenceStorage
import com.oxygen.weather.app.OxygenApp
import com.oxygen.weather.app.OxygenAppStateHolder
import com.oxygen.weather.app.ThemePreferenceReadResult
import com.oxygen.weather.app.ThemePreferenceStorage
import com.oxygen.weather.app.ui.theme.ContrastLevel
import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.app.ui.theme.LayoutPreset
import com.oxygen.weather.app.ui.theme.OxygenAppearance
import com.oxygen.weather.app.ui.theme.OxygenThemeId
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppearanceSemanticsUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun appearanceGroupsAndChoicesExposeMeaningfulSemantics() {
        val location = fixtureLocation()
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = FixtureRepository(location),
            themePreferenceStorage = object : ThemePreferenceStorage {
                override fun readThemePreference() = ThemePreferenceReadResult.Supported(OxygenThemeId.OXYGEN)
                override fun writeThemePreference(theme: OxygenThemeId) = Unit
            },
            layoutPreferenceStorage = object : LayoutPreferenceStorage {
                override fun readLayoutPreference() = LayoutPreferenceReadResult.Supported(LayoutPreset.STANDARD)
                override fun writeLayoutPreference(layout: LayoutPreset) = Unit
            },
            effectsPreferenceStorage = object : EffectsPreferenceStorage {
                override fun readEffectsPreference() = EffectsLevel.SUBTLE
                override fun writeEffectsPreference(effects: EffectsLevel) = Unit
            },
            contrastPreferenceStorage = object : ContrastPreferenceStorage {
                override fun readContrastPreference() = ContrastPreferenceReadResult.Supported(ContrastLevel.STANDARD)
                override fun writeContrastPreference(contrast: ContrastLevel) = Unit
            },
            forecastExecutor = DirectExecutor,
        )

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
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()

        listOf(
            "settings-theme-heading" to "Theme",
            "settings-contrast-heading" to "Contrast",
            "settings-layout-heading" to "Layout mode",
            "settings-effects-heading" to "Effects mode",
        ).forEach { (tag, label) ->
            composeRule.onNodeWithTag(tag)
                .assertTextContains(label)
                .assertHeading()
        }

        listOf(
            "settings-theme-oxygen" to "Oxygen",
            "settings-theme-paper" to "Paper",
            "settings-theme-terminal" to "Terminal",
            "settings-contrast-standard" to "Standard",
            "settings-contrast-high" to "High",
            "settings-layout-simple" to "Simple",
            "settings-layout-standard" to "Standard",
            "settings-effects-off" to "Off",
            "settings-effects-subtle" to "Subtle",
        ).forEach { (tag, label) ->
            composeRule.onNodeWithTag(tag)
                .assertTextContains(label)
                .assertHasClickAction()
                .assertRadioChoice()
        }
        composeRule.onNodeWithTag("settings-theme-oxygen").assertIsSelected()
        composeRule.onNodeWithTag("settings-theme-paper").assertIsNotSelected()
        composeRule.onNodeWithTag("settings-back")
            .assertTextContains("Back to Settings")
            .assertHasClickAction()
        assertAtLeast48Dp("settings-back")
    }

    private fun assertAtLeast48Dp(tag: String) {
        val bounds = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        assertTrue("Expected $tag height >= 48dp but was ${bounds.height}", bounds.height >= 48f)
    }
}

private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertHeading(): androidx.compose.ui.test.SemanticsNodeInteraction {
    val node = fetchSemanticsNode()
    assertTrue("Expected heading semantics", SemanticsProperties.Heading in node.config)
    return this
}

private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertRadioChoice(): androidx.compose.ui.test.SemanticsNodeInteraction {
    val node = fetchSemanticsNode()
    assertEquals(Role.RadioButton, node.config[SemanticsProperties.Role])
    assertTrue("Expected selected semantics", SemanticsProperties.Selected in node.config)
    return this
}

private object DirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class FixtureRepository(
    private val location: WeatherLocation,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = sequenceOf(
        WeatherRepositoryResult.Success(
            WeatherBundle(
                location = this.location,
                fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
            ),
        ),
    )
}

private fun fixtureLocation(): WeatherLocation = WeatherLocation(
    id = LocationId("appearance-semantics"),
    displayName = "Appearance Semantics",
    point = GeoPoint(43.0, -89.0),
    zoneId = ZoneId.of("America/Chicago"),
)

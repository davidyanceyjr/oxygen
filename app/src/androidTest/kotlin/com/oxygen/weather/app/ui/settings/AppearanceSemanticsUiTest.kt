package com.oxygen.weather.app.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oxygen.weather.app.ContrastPreferenceReadResult
import com.oxygen.weather.app.ContrastPreferenceStorage
import com.oxygen.weather.app.EffectsPreferenceStorage
import com.oxygen.weather.app.LayoutPreferenceReadResult
import com.oxygen.weather.app.LayoutPreferenceStorage
import com.oxygen.weather.app.OxygenApp
import com.oxygen.weather.app.OxygenAppStateHolder
import com.oxygen.weather.app.SettingsDestination
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

    @Test
    fun appearanceCompactControlsRemainScrollReachable() {
        val repository = RecordingFixtureRepository(fixtureLocation())
        val holder = failedManagedAppearanceHolder(repository)
        holder.onOpenSettings()
        holder.onSettingsDestinationSelected(SettingsDestination.Appearance)
        setCompactAppearanceContent(holder)

        assertReachableAppearanceErrorControls()
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun appearanceFontScale13ControlsRemainReadableAndReachable() {
        val repository = RecordingFixtureRepository(fixtureLocation())
        val holder = managedAppearanceHolder(repository)
        setCompactAppearanceContent(holder, fontScale = 1.3f)
        openAppearance()

        assertReachableAppearanceControls()
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun appearanceFontScale20ControlsRemainReadableAndReachable() {
        val repository = RecordingFixtureRepository(fixtureLocation())
        val holder = managedAppearanceHolder(repository)
        setCompactAppearanceContent(holder, fontScale = 2f)
        openAppearance()

        assertReachableAppearanceControls()
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun appearanceRtlPreservesLogicalLabelControlOrder() {
        val repository = RecordingFixtureRepository(fixtureLocation())
        val holder = managedAppearanceHolder(repository)
        setCompactAppearanceContent(holder, layoutDirection = LayoutDirection.Rtl)
        openAppearance()

        assertReachableAppearanceControls()
        val simple = composeRule.onNodeWithTag("settings-layout-simple").fetchSemanticsNode().boundsInRoot
        val standard = composeRule.onNodeWithTag("settings-layout-standard").fetchSemanticsNode().boundsInRoot
        assertTrue("RTL should place the first layout choice on the right", simple.left > standard.left)
        composeRule.onNodeWithTag("settings-layout-standard").assertIsSelected()
        composeRule.onNodeWithTag("settings-effects-subtle").assertIsSelected()
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun appearanceReducedMotionOffPreservesEffectsMeaning() {
        val repository = RecordingFixtureRepository(fixtureLocation())
        val holder = managedAppearanceHolder(repository, effects = EffectsLevel.SUBTLE)
        setCompactAppearanceContent(
            holder,
            motionPreferenceSource = com.oxygen.weather.app.MotionPreferenceSource { false },
        )
        openAppearance()

        composeRule.onNodeWithTag("settings-effects-off").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-effects-off").assertIsSelected()
        composeRule.onNodeWithText("Your effects choice is saved on this device.")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("settings-effects-subtle").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-effects-subtle").assertIsSelected()
        composeRule.onNodeWithTag("effects_preference_disabled_motion")
            .performScrollTo()
            .assertIsDisplayed()
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun appearanceThemeContrastPairsPreserveMeaning() {
        val repository = RecordingFixtureRepository(fixtureLocation())
        val holder = managedAppearanceHolder(repository)
        setCompactAppearanceContent(holder)
        openAppearance()

        listOf(
            OxygenThemeId.OXYGEN to ContrastLevel.STANDARD,
            OxygenThemeId.PAPER to ContrastLevel.HIGH,
            OxygenThemeId.TERMINAL to ContrastLevel.STANDARD,
        ).forEach { (theme, contrast) ->
            composeRule.onNodeWithTag("settings-theme-${theme.name.lowercase()}")
                .performScrollTo()
                .performClick()
            composeRule.onNodeWithTag("settings-contrast-${contrast.name.lowercase()}")
                .performScrollTo()
                .performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("settings-theme-${theme.name.lowercase()}").assertIsSelected()
            composeRule.onNodeWithTag("settings-contrast-${contrast.name.lowercase()}").assertIsSelected()
            composeRule.onNodeWithTag("settings-layout-standard").assertIsSelected()
            composeRule.onNodeWithTag("settings-effects-subtle").assertIsSelected()
        }

        assertEquals(1, repository.requests.size)
        assertEquals(holder.presentationState.selectedLocation, fixtureLocation())
    }

    private fun setCompactAppearanceContent(
        holder: OxygenAppStateHolder,
        fontScale: Float = 1f,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        motionPreferenceSource: com.oxygen.weather.app.MotionPreferenceSource =
            com.oxygen.weather.app.EnabledMotionPreferenceSource,
    ) {
        composeRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(1f, fontScale),
                LocalLayoutDirection provides layoutDirection,
            ) {
                Box(Modifier.width(360.dp).height(640.dp)) {
                    OxygenApp(
                        stateHolder = holder,
                        appearance = OxygenAppearance(effects = EffectsLevel.OFF),
                        motionPreferenceSource = motionPreferenceSource,
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun openAppearance() {
        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        composeRule.waitForIdle()
    }

    private fun assertReachableAppearanceControls() {
        listOf(
            "settings-theme-heading" to "Theme",
            "settings-contrast-heading" to "Contrast",
            "settings-layout-heading" to "Layout mode",
            "settings-effects-heading" to "Effects mode",
        ).forEach { (tag, label) ->
            composeRule.onNodeWithTag(tag)
                .performScrollTo()
                .assertIsDisplayed()
                .assertTextContains(label)
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
                .performScrollTo()
                .assertIsDisplayed()
                .assertTextContains(label)
            val bounds = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
            assertTrue("$tag should retain a usable width", bounds.width >= 48f)
            assertTrue("$tag should retain a usable height", bounds.height >= 48f)
            assertTrue("$tag should stay inside the compact root", bounds.left >= 0f && bounds.right <= 360f)
        }
        listOf(
            "theme_preference_saved",
            "contrast_preference_saved",
            "layout_preference_saved",
            "effects_preference_saved",
        ).forEach { tag ->
            composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        }
        composeRule.onNodeWithTag("settings-back").assertIsDisplayed().assertHasClickAction()
        assertAtLeast48Dp("settings-back")
    }

    private fun assertReachableAppearanceErrorControls() {
        listOf(
            "theme_preference_error",
            "contrast_preference_error",
            "layout_preference_error",
            "effects_preference_error",
        ).forEach { tag ->
            composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        }
        listOf(
            "theme_preference_retry",
            "contrast_preference_retry",
            "layout_preference_retry",
            "settings-effects-retry",
        ).forEach { tag ->
            composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
            assertAtLeast48Dp(tag)
        }
        listOf(
            "settings-theme-oxygen",
            "settings-theme-paper",
            "settings-theme-terminal",
            "settings-contrast-standard",
            "settings-contrast-high",
            "settings-layout-simple",
            "settings-layout-standard",
            "settings-effects-off",
            "settings-effects-subtle",
        ).forEach { tag ->
            composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().assertIsNotEnabled()
        }
        composeRule.onNodeWithTag("settings-back").assertIsDisplayed()
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

private class RecordingFixtureRepository(
    private val location: WeatherLocation,
) : WeatherRepository {
    val requests = mutableListOf<WeatherLocation>()

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        requests += location
        return sequenceOf(
            WeatherRepositoryResult.Success(
                WeatherBundle(
                    location = this.location,
                    fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
                ),
            ),
        )
    }
}

private fun managedAppearanceHolder(
    repository: WeatherRepository,
    effects: EffectsLevel = EffectsLevel.SUBTLE,
): OxygenAppStateHolder {
    return OxygenAppStateHolder(
        selectedLocation = fixtureLocation(),
        weatherRepository = repository,
        themePreferenceStorage = object : ThemePreferenceStorage {
            override fun readThemePreference() = ThemePreferenceReadResult.Supported(OxygenThemeId.OXYGEN)
            override fun writeThemePreference(theme: OxygenThemeId) = Unit
        },
        layoutPreferenceStorage = object : LayoutPreferenceStorage {
            override fun readLayoutPreference() = LayoutPreferenceReadResult.Supported(LayoutPreset.STANDARD)
            override fun writeLayoutPreference(layout: LayoutPreset) = Unit
        },
        effectsPreferenceStorage = object : EffectsPreferenceStorage {
            override fun readEffectsPreference() = effects
            override fun writeEffectsPreference(effects: EffectsLevel) = Unit
        },
        contrastPreferenceStorage = object : ContrastPreferenceStorage {
            override fun readContrastPreference() = ContrastPreferenceReadResult.Supported(ContrastLevel.STANDARD)
            override fun writeContrastPreference(contrast: ContrastLevel) = Unit
        },
        forecastExecutor = DirectExecutor,
    )
}

private fun failedManagedAppearanceHolder(repository: WeatherRepository): OxygenAppStateHolder {
    return OxygenAppStateHolder(
        selectedLocation = fixtureLocation(),
        weatherRepository = repository,
        themePreferenceStorage = object : ThemePreferenceStorage {
            override fun readThemePreference(): ThemePreferenceReadResult = error("theme read failed")
            override fun writeThemePreference(theme: OxygenThemeId) = Unit
        },
        layoutPreferenceStorage = object : LayoutPreferenceStorage {
            override fun readLayoutPreference(): LayoutPreferenceReadResult = error("layout read failed")
            override fun writeLayoutPreference(layout: LayoutPreset) = Unit
        },
        effectsPreferenceStorage = object : EffectsPreferenceStorage {
            override fun readEffectsPreference(): EffectsLevel = error("effects read failed")
            override fun writeEffectsPreference(effects: EffectsLevel) = Unit
        },
        contrastPreferenceStorage = object : ContrastPreferenceStorage {
            override fun readContrastPreference(): ContrastPreferenceReadResult = error("contrast read failed")
            override fun writeContrastPreference(contrast: ContrastLevel) = Unit
        },
        forecastExecutor = DirectExecutor,
    )
}

private fun fixtureLocation(): WeatherLocation = WeatherLocation(
    id = LocationId("appearance-semantics"),
    displayName = "Appearance Semantics",
    point = GeoPoint(43.0, -89.0),
    zoneId = ZoneId.of("America/Chicago"),
)

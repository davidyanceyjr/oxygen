package com.oxygen.weather.app.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oxygen.weather.app.EffectsPreferenceStorage
import com.oxygen.weather.app.LayoutPreferenceReadState
import com.oxygen.weather.app.LayoutPreferenceStorage
import com.oxygen.weather.app.OxygenApp
import com.oxygen.weather.app.OxygenAppStateHolder
import com.oxygen.weather.app.UnitPreferenceStorage
import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.app.ui.theme.LayoutPreset
import com.oxygen.weather.app.ui.theme.OxygenAppearance
import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.UnitPreference
import com.oxygen.weather.core.model.UnitPreferencePreset
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
class LayoutPreferenceUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectionCommitsWithoutForecastRefetchAndRemainsIndependent() {
        val location = fixtureLocation("ui-commit")
        val repository = RecordingWeatherRepository(location)
        val executor = ControlledExecutor()
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            layoutPreferenceStorage = ControlledLayoutPreferenceStorage(stored = LayoutPreset.SIMPLE),
            unitPreferenceStorage = RecordingUnitPreferenceStorage(UnitPreference.Preset(UnitPreferencePreset.METRIC)),
            effectsPreferenceStorage = RecordingEffectsPreferenceStorage(EffectsLevel.SUBTLE),
            forecastExecutor = executor,
        )

        setContent(holder)
        drainUi(executor)

        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 2")
        composeRule.onNodeWithTag("home-about-entry").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-layout-simple").assertIsSelected()
        composeRule.onNodeWithTag("layout_preference_saved").assertIsDisplayed()
        assertEquals(1, repository.refreshCount)
        assertEquals(UnitPreference.Preset(UnitPreferencePreset.METRIC), holder.presentationState.unitPreference)
        assertEquals(EffectsLevel.SUBTLE, holder.presentationState.effectsPreference.confirmed)

        composeRule.onNodeWithTag("settings-layout-standard").performClick()
        composeRule.onNodeWithTag("layout_preference_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-layout-simple").assertIsNotEnabled()
        composeRule.onNodeWithTag("settings-layout-standard").assertIsNotEnabled()
        drainUi(executor)

        composeRule.onNodeWithTag("layout_preference_saved").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-layout-standard").assertIsSelected()
        assertEquals(LayoutPreset.STANDARD, holder.presentationState.layout)
        assertEquals(1, repository.refreshCount)
        assertEquals(location, holder.presentationState.selectedLocation)
        assertEquals(UnitPreference.Preset(UnitPreferencePreset.METRIC), holder.presentationState.unitPreference)
        assertEquals(EffectsLevel.SUBTLE, holder.presentationState.effectsPreference.confirmed)
        assertEquals(listOf(location), repository.locations)

        composeRule.onNodeWithTag("settings-back").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-back").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 4")

        composeRule.onNodeWithTag("home-about-entry").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-layout-standard").assertIsSelected()
        composeRule.onNodeWithTag("settings-layout-simple").performClick()
        composeRule.onNodeWithTag("layout_preference_loading").assertIsDisplayed()
        drainUi(executor)

        composeRule.onNodeWithTag("layout_preference_saved").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-layout-simple").assertIsSelected()
        composeRule.onNodeWithTag("settings-back").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-back").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 2")
        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun readAndWriteFailuresKeepStandardOrConfirmedLayoutAndRecover() {
        val location = fixtureLocation("ui-failures")
        val repository = RecordingWeatherRepository(location)
        val executor = ControlledExecutor()
        val storage = ControlledLayoutPreferenceStorage(
            stored = LayoutPreset.SIMPLE,
            readFails = true,
        )
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            layoutPreferenceStorage = storage,
            unitPreferenceStorage = RecordingUnitPreferenceStorage(UnitPreference.Preset(UnitPreferencePreset.METRIC)),
            effectsPreferenceStorage = RecordingEffectsPreferenceStorage(EffectsLevel.SUBTLE),
            forecastExecutor = executor,
        )

        setContent(holder)
        drainUi(executor)

        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 4")
        composeRule.onNodeWithTag("home-about-entry").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-layout-standard").assertIsSelected()
        composeRule.onNodeWithTag("layout_preference_error").assertTextContains("Layout load failed; using Standard; retry")
        composeRule.onNodeWithTag("layout_preference_retry").assertIsDisplayed()
        assertTouchHeightAtLeast("settings-layout-simple")
        assertTouchHeightAtLeast("settings-layout-standard")
        composeRule.onNodeWithTag("layout_preference_retry").performScrollTo()
        assertTouchHeightAtLeast("layout_preference_retry")
        assertLeftToRight("settings-layout-simple", "settings-layout-standard")
        assertAbove("layout_preference_error", "layout_preference_retry")

        storage.readFails = false
        composeRule.onNodeWithTag("layout_preference_retry").performClick()
        composeRule.onNodeWithTag("layout_preference_loading").assertIsDisplayed()
        drainUi(executor)

        composeRule.onNodeWithTag("settings-layout-simple").assertIsSelected()
        composeRule.onNodeWithTag("layout_preference_saved").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-back").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-back").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 2")

        composeRule.onNodeWithTag("home-about-entry").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        drainUi(executor)
        storage.readFails = true
        holder.onLayoutPreferenceRetry()
        composeRule.onNodeWithTag("layout_preference_loading").assertIsDisplayed()
        drainUi(executor)

        composeRule.onNodeWithTag("settings-layout-simple").assertIsSelected()
        composeRule.onNodeWithTag("layout_preference_error").assertTextContains("Layout load failed; using Simple; retry")
        composeRule.onNodeWithTag("settings-back").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-back").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 2")

        composeRule.onNodeWithTag("home-about-entry").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        drainUi(executor)
        storage.readFails = false
        holder.onLayoutPreferenceRetry()
        composeRule.onNodeWithTag("layout_preference_loading").assertIsDisplayed()
        drainUi(executor)

        composeRule.onNodeWithTag("settings-layout-simple").assertIsSelected()
        composeRule.onNodeWithTag("layout_preference_saved").assertIsDisplayed()
        storage.writeFails = true
        composeRule.onNodeWithTag("settings-layout-standard").performClick()
        composeRule.onNodeWithTag("layout_preference_loading").assertTextContains("Saving Standard...")
        composeRule.onNodeWithTag("settings-layout-simple").assertIsNotEnabled()
        composeRule.onNodeWithTag("settings-layout-standard").assertIsNotEnabled()
        drainUi(executor)

        composeRule.onNodeWithTag("settings-layout-simple").assertIsSelected()
        composeRule.onNodeWithTag("layout_preference_error").assertTextContains("Layout save failed; retry")
        composeRule.onNodeWithTag("layout_preference_retry").assertIsDisplayed()

        storage.writeFails = false
        composeRule.onNodeWithTag("layout_preference_retry").performClick()
        composeRule.onNodeWithTag("layout_preference_loading").assertIsDisplayed()
        drainUi(executor)

        composeRule.onNodeWithTag("settings-layout-standard").assertIsSelected()
        composeRule.onNodeWithTag("layout_preference_saved").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-back").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-back").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 4")
    }

    private fun setContent(holder: OxygenAppStateHolder) {
        composeRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 1.3f),
            ) {
                Box(
                    modifier = Modifier.width(360.dp).height(640.dp),
                ) {
                    OxygenApp(
                        stateHolder = holder,
                        appearance = OxygenAppearance(effects = EffectsLevel.OFF),
                    )
                }
            }
        }
    }

    private fun drainUi(executor: ControlledExecutor) {
        executor.drainAll()
        composeRule.waitForIdle()
    }

    private fun assertTouchHeightAtLeast(tag: String, minHeightDp: Float = 48f) {
        val bounds = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        assertTrue("Expected $tag height >= $minHeightDp dp but was ${bounds.height}", bounds.height >= minHeightDp)
    }

    private fun assertLeftToRight(leftTag: String, rightTag: String) {
        val left = composeRule.onNodeWithTag(leftTag).fetchSemanticsNode().boundsInRoot
        val right = composeRule.onNodeWithTag(rightTag).fetchSemanticsNode().boundsInRoot
        assertTrue("Expected $leftTag to be left of $rightTag", left.left <= right.left)
    }

    private fun assertAbove(upperTag: String, lowerTag: String) {
        val upper = composeRule.onNodeWithTag(upperTag).fetchSemanticsNode().boundsInRoot
        val lower = composeRule.onNodeWithTag(lowerTag).fetchSemanticsNode().boundsInRoot
        assertTrue("Expected $upperTag above $lowerTag", upper.top <= lower.top)
    }
}

private class ControlledExecutor : Executor {
    private val tasks = ArrayDeque<Runnable>()

    override fun execute(command: Runnable) {
        synchronized(tasks) {
            tasks.addLast(command)
        }
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

private class ControlledLayoutPreferenceStorage(
    var stored: LayoutPreset? = null,
    var readFails: Boolean = false,
    var writeFails: Boolean = false,
) : LayoutPreferenceStorage {
    override fun readLayoutPreference(): com.oxygen.weather.app.LayoutPreferenceReadResult {
        if (readFails) error("layout read failed")
        return when (stored) {
            null -> com.oxygen.weather.app.LayoutPreferenceReadResult.NoSupportedChoice
            else -> com.oxygen.weather.app.LayoutPreferenceReadResult.Supported(stored!!)
        }
    }

    override fun writeLayoutPreference(layout: LayoutPreset) {
        if (writeFails) error("layout write failed")
        stored = layout
    }
}

private class RecordingWeatherRepository(
    private val location: WeatherLocation,
) : WeatherRepository {
    val locations = mutableListOf<WeatherLocation>()
    var refreshCount = 0
        private set

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        refreshCount += 1
        locations += location
        return sequenceOf(
            WeatherRepositoryResult.Success(fullWeatherBundle(this.location)),
        )
    }
}

private class RecordingUnitPreferenceStorage(
    private var stored: UnitPreference? = null,
) : UnitPreferenceStorage {
    override fun readUnitPreference(): UnitPreference? = stored

    override fun writeUnitPreference(preference: UnitPreference?) {
        stored = preference
    }
}

private class RecordingEffectsPreferenceStorage(
    private var stored: EffectsLevel = EffectsLevel.SUBTLE,
) : EffectsPreferenceStorage {
    override fun readEffectsPreference(): EffectsLevel? = stored

    override fun writeEffectsPreference(effects: EffectsLevel) {
        stored = effects
    }
}

private fun fixtureLocation(id: String): WeatherLocation = WeatherLocation(
    id = LocationId(id),
    displayName = "UI Fixture $id",
    point = GeoPoint(43.0731, -89.4012),
    zoneId = ZoneId.of("America/Chicago"),
)

private fun fullWeatherBundle(location: WeatherLocation): WeatherBundle =
    WeatherBundle(
        location = location,
        current = CurrentConditions(
            time = Instant.parse("2026-09-08T12:00:00Z"),
            temperatureC = 21.0,
            condition = WeatherCondition.CLEAR,
            provenance = DataProvenance(
                providerId = "open-meteo",
                sourceName = "Open-Meteo",
                fetchedAt = Instant.parse("2026-09-08T12:00:00Z"),
                type = DataType.FORECAST,
            ),
        ),
        fetchedAt = Instant.parse("2026-09-08T12:00:00Z"),
    )

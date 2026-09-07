package com.oxygen.weather.app.ui.settings

import android.graphics.Bitmap
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.oxygen.weather.app.EffectsPreferenceStorage
import com.oxygen.weather.app.EffectsPreferenceReadState
import com.oxygen.weather.app.OxygenApp
import com.oxygen.weather.app.OxygenAppStateHolder
import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class EffectsPreferenceUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectionPreservesWeatherAndDoesNotRequestAnotherForecast() {
        val location = fixtureLocation()
        val repository = CountingRepository(location)
        val storage = TestEffectsStorage(stored = EffectsLevel.SUBTLE)
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            effectsPreferenceStorage = storage,
            forecastExecutor = UiDirectExecutor,
        )
        composeRule.setContent { OxygenApp(stateHolder = holder) }
        composeRule.waitForIdle()
        val requestsBefore = repository.refreshCount

        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        composeRule.onNodeWithTag("settings-effects-off").performClick()
        composeRule.onNodeWithTag("settings-effects-off").assertIsSelected()
        assertEquals(requestsBefore, repository.refreshCount)

        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.onNodeWithTag("settings-back").performClick()
        composeRule.onAllNodesWithTag("home-weather-scene").assertCountEquals(0)
        composeRule.onNodeWithTag("home-page-title").assertIsDisplayed()

        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        composeRule.onNodeWithTag("settings-effects-subtle").performClick()
        composeRule.onNodeWithTag("settings-effects-subtle").assertIsSelected()
        assertEquals(requestsBefore, repository.refreshCount)
    }

    @Test
    fun initialReadFailureUsesConservativeOffAndRecovers() {
        val storage = TestEffectsStorage(stored = EffectsLevel.SUBTLE, readFails = true)
        val holder = OxygenAppStateHolder(
            selectedLocation = fixtureLocation(),
            weatherRepository = CountingRepository(fixtureLocation()),
            effectsPreferenceStorage = storage,
            forecastExecutor = UiDirectExecutor,
        )
        composeRule.setContent { OxygenApp(stateHolder = holder) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        composeRule.onNodeWithText("could not read the saved effects choice", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("settings-effects-retry").performScrollTo().assertIsDisplayed()
        saveEvidence(composeRule, "appearance-read-failure")

        storage.readFails = false
        composeRule.onNodeWithTag("settings-effects-retry").performClick()
        composeRule.onNodeWithTag("settings-effects-subtle").assertIsSelected()
        assertEquals(EffectsPreferenceReadState.Loaded, holder.presentationState.effectsPreference.readState)
    }

    @Test
    fun firstRunWriteFailureRetainsConfirmedChoiceAndRetries() {
        val storage = TestEffectsStorage(stored = EffectsLevel.SUBTLE, writeFails = true)
        val holder = OxygenAppStateHolder(
            effectsPreferenceStorage = storage,
            forecastExecutor = UiDirectExecutor,
        )
        composeRule.setContent { OxygenApp(stateHolder = holder) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("location-entry-about").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        composeRule.onNodeWithTag("settings-effects-off").performClick()
        composeRule.onNodeWithText("could not save this choice", substring = true).assertIsDisplayed()
        composeRule.onNodeWithTag("settings-effects-subtle").assertIsSelected()

        storage.writeFails = false
        composeRule.onNodeWithTag("settings-effects-off").performClick()
        composeRule.onNodeWithTag("settings-effects-off").assertIsSelected()
    }

    private fun saveEvidence(rule: ComposeTestRule, name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = context.getExternalFilesDir(null) ?: return
        FileOutputStream(File(directory, "$name.png")).use { output ->
            rule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        File(directory, "$name-semantics.txt").writeText(rule.onRoot().printToString())
    }
}

private object UiDirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class TestEffectsStorage(
    var stored: EffectsLevel? = null,
    var readFails: Boolean = false,
    var writeFails: Boolean = false,
) : EffectsPreferenceStorage {
    override fun readEffectsPreference(): EffectsLevel? {
        if (readFails) error("read failed")
        return stored
    }

    override fun writeEffectsPreference(effects: EffectsLevel) {
        if (writeFails) error("write failed")
        stored = effects
    }
}

private class CountingRepository(
    private val location: WeatherLocation,
) : WeatherRepository {
    var refreshCount = 0

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        refreshCount += 1
        return sequenceOf(
            WeatherRepositoryResult.Success(
                WeatherBundle(location = this.location, fetchedAt = Instant.parse("2026-08-22T12:00:00Z")),
            ),
        )
    }
}

private fun fixtureLocation(): WeatherLocation = WeatherLocation(
    id = LocationId("effects-ui-fixture"),
    displayName = "Effects UI Fixture",
    point = GeoPoint(43.0, -89.0),
    zoneId = ZoneId.of("America/Chicago"),
)

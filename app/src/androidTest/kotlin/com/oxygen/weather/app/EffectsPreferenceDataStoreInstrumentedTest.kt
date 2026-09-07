package com.oxygen.weather.app

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.oxygen.weather.MainActivity
import com.oxygen.weather.app.ui.theme.EffectsLevel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EffectsPreferenceDataStoreInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private lateinit var storage: DataStoreEffectsPreferenceStorage
    private var previous: EffectsLevel? = null

    @Before
    fun savePreviousPreference() {
        storage = DataStoreEffectsPreferenceStorage(composeRule.activity)
        previous = storage.readEffectsPreference()
        storage.writeEffectsPreference(EffectsLevel.OFF)
        composeRule.activityRule.scenario.recreate()
    }

    @After
    fun restorePreviousPreference() {
        storage.writeEffectsPreference(previous ?: EffectsLevel.SUBTLE)
    }

    @Test
    fun dataStoreReadbackAndActivityRecreationKeepEffects() {
        composeRule.waitForIdle()
        assertEquals(EffectsLevel.OFF, storage.readEffectsPreference())
        composeRule.onNodeWithTag("location-entry-about").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        composeRule.onNodeWithTag("settings-effects-off").assertIsSelected()

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("location-entry-about").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        composeRule.onNodeWithTag("settings-effects-off").assertIsSelected()
        composeRule.onNodeWithText("Your effects choice is saved on this device.").assertExists()
    }
}

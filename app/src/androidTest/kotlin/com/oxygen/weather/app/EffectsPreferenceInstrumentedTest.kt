package com.oxygen.weather.app

import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Executor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class EffectsPreferenceInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var originalAnimatorScale: String? = null

    @After
    fun restoreAnimatorScale() {
        originalAnimatorScale?.let { scale ->
            val command = if (scale == "null" || scale.isBlank()) {
                "settings delete global animator_duration_scale"
            } else {
                "settings put global animator_duration_scale $scale"
            }
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).close()
        }
    }

    @Test
    fun systemMotionOverrideForcesOffAndPagerChangesPageImmediately() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        originalAnimatorScale = ParcelFileDescriptor.AutoCloseInputStream(
            instrumentation.uiAutomation.executeShellCommand("settings get global animator_duration_scale"),
        ).use { it.bufferedReader().readText().trim() }
        instrumentation.uiAutomation.executeShellCommand("settings put global animator_duration_scale 0").close()

        val location = fixtureLocation()
        val repository = ImmediateRepository(location)
        val effectsStorage = StoredEffectsPreferenceStorage(EffectsLevel.SUBTLE)
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            effectsPreferenceStorage = effectsStorage,
            forecastExecutor = InstrumentedDirectExecutor,
        )
        composeRule.setContent {
            OxygenApp(
                stateHolder = holder,
                motionPreferenceSource = AndroidMotionPreferenceSource,
            )
        }
        composeRule.waitForIdle()

        assertEquals(false, AndroidMotionPreferenceSource.areAnimationsEnabled())
        assertEquals(EffectsLevel.SUBTLE, holder.presentationState.effectsPreference.confirmed)
        assertEquals(emptyList<EffectsLevel>(), effectsStorage.writes)
        val requests = repository.locations.toList()
        assertEquals(listOf(location), requests)
        composeRule.onAllNodesWithTag("home-weather-scene").assertCountEquals(0)
        composeRule.onAllNodesWithTag("home-current-halo").assertCountEquals(0)
        composeRule.onNodeWithTag("home-page-tab-hourly").performClick()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Hourly")
        composeRule.onNodeWithTag("home-page-tab-details").performClick()
        composeRule.onNodeWithTag("home-page-title").assertTextContains("Details")
        assertEquals(requests, repository.locations)
        assertEquals(emptyList<EffectsLevel>(), effectsStorage.writes)
    }
}

private object InstrumentedDirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class StoredEffectsPreferenceStorage(
    private var stored: EffectsLevel,
) : EffectsPreferenceStorage {
    val writes = mutableListOf<EffectsLevel>()

    override fun readEffectsPreference(): EffectsLevel = stored

    override fun writeEffectsPreference(effects: EffectsLevel) {
        writes += effects
        stored = effects
    }
}

private class ImmediateRepository(
    private val location: WeatherLocation,
) : WeatherRepository {
    val locations = mutableListOf<WeatherLocation>()

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = sequenceOf(
        WeatherRepositoryResult.Success(
            WeatherBundle(location = this.location, fetchedAt = Instant.parse("2026-08-22T12:00:00Z")),
        ).also { locations += location },
    )
}

private fun fixtureLocation(): WeatherLocation = WeatherLocation(
    id = LocationId("effects-instrumented-fixture"),
    displayName = "Effects Instrumented Fixture",
    point = GeoPoint(43.0, -89.0),
    zoneId = ZoneId.of("America/Chicago"),
)

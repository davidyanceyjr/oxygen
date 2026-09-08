package com.oxygen.weather.app

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.mutableStateOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.oxygen.weather.app.ui.theme.LayoutPreset
import com.oxygen.weather.app.ui.theme.OxygenAppearance
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
import java.util.ArrayDeque
import java.util.concurrent.Executor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

@RunWith(AndroidJUnit4::class)
class LayoutPreferenceDataStoreInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var layoutStorage: DataStoreLayoutPreferenceStorage
    private var previousLayout: LayoutPreferenceReadResult = LayoutPreferenceReadResult.NoSupportedChoice

    @After
    fun restoreFixture() {
        restoreLayoutRecord()
        composeRule.waitForIdle()
    }

    @Test
    fun selectionSurvivesActivityRecreationThroughProductionDataStore() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        layoutStorage = DataStoreLayoutPreferenceStorage(context)
        previousLayout = layoutStorage.readLayoutPreference()
        layoutStorage.writeLayoutPreference(LayoutPreset.STANDARD)

        val location = layoutDataStoreFixtureLocation("layout-data-store")
        val executor = ControlledExecutor()
        val repository = RecordingWeatherRepository(location)
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            layoutPreferenceStorage = layoutStorage,
            forecastExecutor = executor,
        )

        val holderState = setContent(holder)
        drainUi(executor)
        waitForHomeReady()

        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 4")
        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        composeRule.onNodeWithTag("settings-layout-standard").assertIsSelected()
        composeRule.onNodeWithTag("layout_preference_saved").assertIsDisplayed()

        composeRule.onNodeWithTag("settings-layout-simple").performClick()
        composeRule.onNodeWithTag("layout_preference_loading").assertTextContains("Saving Simple...")
        composeRule.onNodeWithTag("settings-layout-simple").assertIsNotEnabled()
        composeRule.onNodeWithTag("settings-layout-standard").assertIsNotEnabled()
        drainUi(executor)

        assertEquals(LayoutPreferenceReadResult.Supported(LayoutPreset.SIMPLE), layoutStorage.readLayoutPreference())
        composeRule.onNodeWithTag("settings-layout-simple").assertIsSelected()
        composeRule.onNodeWithTag("layout_preference_saved").assertIsDisplayed()

        val restartedExecutor = ControlledExecutor()
        val restartedRepository = RecordingWeatherRepository(location)
        val restartedHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = restartedRepository,
            layoutPreferenceStorage = layoutStorage,
            forecastExecutor = restartedExecutor,
        )

        composeRule.runOnIdle {
            holderState.value = restartedHolder
        }
        drainUi(restartedExecutor)
        waitForHomeReady()

        composeRule.onNodeWithTag("home-page-title").assertTextContains("Now")
        composeRule.onNodeWithTag("home-page-position").assertTextContains("Page 1 of 2")
        composeRule.onNodeWithTag("home-about-entry").performClick()
        composeRule.onNodeWithTag("settings-destination-appearance").performClick()
        composeRule.onNodeWithTag("settings-layout-simple").assertIsSelected()
        composeRule.onNodeWithTag("layout_preference_saved").assertIsDisplayed()
    }

    private fun restoreLayoutRecord() {
        when (val layout = previousLayout) {
            LayoutPreferenceReadResult.NoSupportedChoice -> layoutStorage.writeLayoutPreference(LayoutPreset.STANDARD)
            is LayoutPreferenceReadResult.Supported -> layoutStorage.writeLayoutPreference(layout.layout)
        }
    }

    private fun setContent(holder: OxygenAppStateHolder): MutableState<OxygenAppStateHolder> {
        val holderState = mutableStateOf(holder)
        composeRule.setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalDensity provides androidx.compose.ui.unit.Density(density = 1f, fontScale = 1.3f),
            ) {
                Box(
                    modifier = Modifier.width(360.dp).height(640.dp),
                ) {
                    OxygenApp(
                        stateHolder = holderState.value,
                        appearance = OxygenAppearance(),
                    )
                }
            }
        }
        return holderState
    }

    private fun waitForHomeReady() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("home-page-position").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun drainUi(executor: ControlledExecutor) {
        executor.drainAll()
        composeRule.waitForIdle()
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

private class RecordingWeatherRepository(
    private val location: WeatherLocation,
) : WeatherRepository {
    var refreshCount = 0
        private set

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        refreshCount += 1
        return sequenceOf(
            WeatherRepositoryResult.Success(layoutDataStoreFullWeatherBundle(this.location)),
        )
    }
}

private fun layoutDataStoreFixtureLocation(id: String): WeatherLocation = WeatherLocation(
    id = LocationId(id),
    displayName = "Installed Fixture $id",
    point = GeoPoint(43.0731, -89.4012),
    zoneId = java.time.ZoneId.of("America/Chicago"),
)

private fun layoutDataStoreFullWeatherBundle(location: WeatherLocation): WeatherBundle =
    WeatherBundle(
        location = location,
        current = CurrentConditions(
            time = java.time.Instant.parse("2026-09-08T12:00:00Z"),
            temperatureC = 21.0,
            condition = WeatherCondition.CLEAR,
            provenance = DataProvenance(
                providerId = "open-meteo",
                sourceName = "Open-Meteo",
                fetchedAt = java.time.Instant.parse("2026-09-08T12:00:00Z"),
                type = DataType.FORECAST,
            ),
        ),
        fetchedAt = java.time.Instant.parse("2026-09-08T12:00:00Z"),
    )

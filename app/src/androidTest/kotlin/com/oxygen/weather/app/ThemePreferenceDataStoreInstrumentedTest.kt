package com.oxygen.weather.app

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemePreferenceDataStoreInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var storage: DataStoreThemePreferenceStorage
    private var previous: ThemePreferenceReadResult = ThemePreferenceReadResult.NoSupportedChoice

    @Before
    fun prepareTestOwnedThemeRecord() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        storage = DataStoreThemePreferenceStorage(context)
        previous = storage.readThemePreference()
        storage.writeThemePreference(OxygenThemeId.OXYGEN)
    }

    @After
    fun restoreTestOwnedThemeRecord() {
        val restore = (previous as? ThemePreferenceReadResult.Supported)?.theme
            ?: OxygenThemeId.OXYGEN
        storage.writeThemePreference(restore)
        composeRule.waitForIdle()
    }

    @Test
    fun supportedThemeSurvivesStorageAndStateHolderRecreation() {
        val location = themeDataStoreFixtureLocation("theme-data-store")
        val executor = ThemeDataStoreControlledExecutor()
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = ThemeDataStoreWeatherRepository(location),
            themePreferenceStorage = storage,
            forecastExecutor = executor,
        )
        val holderState = setContent(holder)
        drainUi(executor)

        holder.onOpenSettings()
        holder.onSettingsDestinationSelected(SettingsDestination.Appearance)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-appearance-summary").assertIsDisplayed()

        composeRule.onNodeWithTag("settings-theme-paper").performClick()
        drainUi(executor)
        composeRule.onNodeWithTag("settings-theme-paper").assertIsSelected()
        assertEquals(
            ThemePreferenceReadResult.Supported(OxygenThemeId.PAPER),
            storage.readThemePreference(),
        )

        val restartedStorage = DataStoreThemePreferenceStorage(context)
        val restartedExecutor = ThemeDataStoreControlledExecutor()
        val restartedHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = ThemeDataStoreWeatherRepository(location),
            themePreferenceStorage = restartedStorage,
            forecastExecutor = restartedExecutor,
        )
        composeRule.runOnIdle { holderState.value = restartedHolder }
        drainUi(restartedExecutor)

        restartedHolder.onOpenSettings()
        restartedHolder.onSettingsDestinationSelected(SettingsDestination.Appearance)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings-appearance-summary").assertIsDisplayed()
        composeRule.onNodeWithTag("settings-theme-paper").assertIsSelected()
    }

    private fun setContent(holder: OxygenAppStateHolder): MutableState<OxygenAppStateHolder> {
        val holderState = mutableStateOf(holder)
        composeRule.setContent {
            Box(
                modifier = Modifier.width(360.dp).height(640.dp),
            ) {
                OxygenApp(
                    stateHolder = holderState.value,
                    appearance = OxygenAppearance(),
                )
            }
        }
        return holderState
    }

    private fun drainUi(executor: ThemeDataStoreControlledExecutor) {
        executor.drainAll()
        composeRule.waitForIdle()
    }
}

private class ThemeDataStoreControlledExecutor : Executor {
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

private class ThemeDataStoreWeatherRepository(
    private val fixture: WeatherLocation,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = sequenceOf(
        WeatherRepositoryResult.Success(
            WeatherBundle(
                location = fixture,
                current = CurrentConditions(
                    time = Instant.parse("2026-09-08T12:00:00Z"),
                    temperatureC = 21.0,
                    condition = WeatherCondition.CLEAR,
                    provenance = DataProvenance(
                        providerId = "theme-data-store-test",
                        sourceName = "Theme DataStore Test",
                        fetchedAt = Instant.parse("2026-09-08T12:00:00Z"),
                        type = DataType.FORECAST,
                    ),
                ),
                fetchedAt = Instant.parse("2026-09-08T12:00:00Z"),
            ),
        ),
    )
}

private fun themeDataStoreFixtureLocation(id: String): WeatherLocation = WeatherLocation(
    id = LocationId(id),
    displayName = "Theme DataStore Fixture",
    point = GeoPoint(43.0731, -89.4012),
    zoneId = ZoneId.of("America/Chicago"),
)

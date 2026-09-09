package com.oxygen.weather.app

import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.app.ui.theme.LayoutPreset
import com.oxygen.weather.app.ui.theme.OxygenThemeId
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePreferenceStateHolderTest {
    @Test
    fun `managed startup exposes loading then restores each supported theme`() {
        listOf(OxygenThemeId.OXYGEN, OxygenThemeId.PAPER, OxygenThemeId.TERMINAL).forEach { storedTheme ->
            val executor = ThemeControlledExecutor()
            val holder = newHolder(
                storage = FakeThemePreferenceStorage(stored = storedTheme),
                executor = executor,
            )

            assertEquals(ThemePreferenceReadState.Loading, holder.presentationState.themePreference.readState)
            assertEquals(OxygenThemeId.OXYGEN, holder.presentationState.theme)

            executor.drainAll()

            assertEquals(ThemePreferenceReadState.Loaded, holder.presentationState.themePreference.readState)
            assertEquals(storedTheme, holder.presentationState.theme)
            assertEquals(storedTheme, holder.presentationState.themePreference.confirmed)
        }

        val missingRecordExecutor = ThemeControlledExecutor()
        val missingRecordHolder = newHolder(
            storage = FakeThemePreferenceStorage(),
            executor = missingRecordExecutor,
        )
        assertEquals(ThemePreferenceReadState.Loading, missingRecordHolder.presentationState.themePreference.readState)
        missingRecordExecutor.drainAll()
        assertEquals(ThemePreferenceReadState.Loaded, missingRecordHolder.presentationState.themePreference.readState)
        assertEquals(OxygenThemeId.OXYGEN, missingRecordHolder.presentationState.theme)
        assertEquals(OxygenThemeId.OXYGEN, missingRecordHolder.presentationState.themePreference.confirmed)

        val unmanagedHolder = OxygenAppStateHolder(
            initialTheme = OxygenThemeId.TERMINAL,
            forecastExecutor = ThemeDirectExecutor,
        )

        assertEquals(ThemePreferenceReadState.NotConfigured, unmanagedHolder.presentationState.themePreference.readState)
        assertEquals(OxygenThemeId.TERMINAL, unmanagedHolder.presentationState.theme)
        assertEquals(OxygenThemeId.TERMINAL, unmanagedHolder.presentationState.themePreference.confirmed)
    }

    @Test
    fun `pending selection keeps confirmed theme and ignores duplicate and pending input`() {
        val executor = ThemeControlledExecutor()
        val storage = FakeThemePreferenceStorage(stored = OxygenThemeId.OXYGEN)
        val holder = newHolder(storage = storage, executor = executor)
        executor.drainAll()
        openAppearance(holder)

        holder.onThemeSelected(OxygenThemeId.PAPER)

        assertEquals(OxygenThemeId.OXYGEN, holder.presentationState.theme)
        assertEquals(OxygenThemeId.OXYGEN, holder.presentationState.themePreference.confirmed)
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.themePreference.pending)
        assertTrue(storage.writes.isEmpty())

        holder.onThemeSelected(OxygenThemeId.PAPER)
        holder.onThemeSelected(OxygenThemeId.TERMINAL)
        assertTrue(storage.writes.isEmpty())
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.themePreference.pending)

        executor.drainAll()

        assertEquals(listOf(OxygenThemeId.PAPER), storage.writes)
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.theme)
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.themePreference.confirmed)
        assertEquals(null, holder.presentationState.themePreference.pending)
        assertFalse(holder.presentationState.themePreference.writeError)

        holder.onThemeSelected(OxygenThemeId.PAPER)
        assertEquals(listOf(OxygenThemeId.PAPER), storage.writes)
    }

    @Test
    fun `read failure is observable and retry preserves confirmed theme`() {
        val executor = ThemeControlledExecutor()
        val storage = FakeThemePreferenceStorage(
            stored = OxygenThemeId.PAPER,
            readFails = true,
        )
        val holder = newHolder(storage = storage, executor = executor)

        assertEquals(ThemePreferenceReadState.Loading, holder.presentationState.themePreference.readState)
        executor.drainAll()

        assertEquals(ThemePreferenceReadState.Failed, holder.presentationState.themePreference.readState)
        assertEquals(OxygenThemeId.OXYGEN, holder.presentationState.theme)
        assertEquals(null, holder.presentationState.themePreference.confirmed)

        openAppearance(holder)
        storage.readFails = false
        holder.onThemePreferenceRetry()

        assertEquals(ThemePreferenceReadState.Loading, holder.presentationState.themePreference.readState)
        assertEquals(OxygenThemeId.OXYGEN, holder.presentationState.theme)
        executor.drainAll()
        assertEquals(ThemePreferenceReadState.Loaded, holder.presentationState.themePreference.readState)
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.theme)

        storage.readFails = true
        holder.onThemePreferenceRetry()
        assertEquals(ThemePreferenceReadState.Loading, holder.presentationState.themePreference.readState)
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.theme)
        executor.drainAll()
        assertEquals(ThemePreferenceReadState.Failed, holder.presentationState.themePreference.readState)
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.theme)
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.themePreference.confirmed)
    }

    @Test
    fun `write failure retains confirmed theme and retry commits the failed target`() {
        val executor = ThemeControlledExecutor()
        val storage = FakeThemePreferenceStorage(
            stored = OxygenThemeId.OXYGEN,
            writeFails = true,
        )
        val holder = newHolder(storage = storage, executor = executor)
        executor.drainAll()
        openAppearance(holder)

        holder.onThemeSelected(OxygenThemeId.TERMINAL)
        assertEquals(OxygenThemeId.TERMINAL, holder.presentationState.themePreference.pending)
        executor.drainAll()

        assertEquals(listOf(OxygenThemeId.TERMINAL), storage.writes)
        assertEquals(OxygenThemeId.OXYGEN, holder.presentationState.theme)
        assertEquals(OxygenThemeId.OXYGEN, holder.presentationState.themePreference.confirmed)
        assertEquals(null, holder.presentationState.themePreference.pending)
        assertTrue(holder.presentationState.themePreference.writeError)

        storage.writeFails = false
        holder.onThemePreferenceRetry()
        assertEquals(OxygenThemeId.TERMINAL, holder.presentationState.themePreference.pending)
        executor.drainAll()

        assertEquals(listOf(OxygenThemeId.TERMINAL, OxygenThemeId.TERMINAL), storage.writes)
        assertEquals(OxygenThemeId.TERMINAL, holder.presentationState.theme)
        assertEquals(OxygenThemeId.TERMINAL, holder.presentationState.themePreference.confirmed)
        assertFalse(holder.presentationState.themePreference.writeError)
    }

    @Test
    fun `theme transaction preserves other preferences screen state and forecast`() {
        val location = themeFixtureLocation("theme-independence")
        val repository = ThemeCountingWeatherRepository(location)
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            unitPreferenceStorage = ThemeFakeUnitPreferenceStorage(
                UnitPreference.Preset(UnitPreferencePreset.METRIC),
            ),
            layoutPreferenceStorage = ThemeFakeLayoutPreferenceStorage(LayoutPreset.SIMPLE),
            effectsPreferenceStorage = ThemeFakeEffectsPreferenceStorage(EffectsLevel.SUBTLE),
            themePreferenceStorage = FakeThemePreferenceStorage(OxygenThemeId.OXYGEN),
            forecastExecutor = ThemeDirectExecutor,
        )
        val before = (holder.presentationState.screen as OxygenAppScreen.Home)
        val beforeReady = before.forecast as HomeForecastPresentationState.ForecastReady

        openAppearance(holder)
        holder.onThemeSelected(OxygenThemeId.TERMINAL)

        assertEquals(1, repository.refreshCount)
        assertEquals(location, holder.presentationState.selectedLocation)
        assertEquals(UnitPreference.Preset(UnitPreferencePreset.METRIC), holder.presentationState.unitPreference)
        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layout)
        assertEquals(EffectsLevel.SUBTLE, holder.presentationState.effectsPreference.confirmed)
        assertEquals(OxygenThemeId.TERMINAL, holder.presentationState.theme)

        val settings = holder.presentationState.screen as OxygenAppScreen.Settings
        assertEquals(SettingsDestination.Appearance, settings.selectedDestination)
        val afterHome = settings.returnScreen as OxygenAppScreen.Home
        val afterReady = afterHome.forecast as HomeForecastPresentationState.ForecastReady
        assertEquals(beforeReady.dashboard, afterReady.dashboard)
        assertEquals(beforeReady.freshness, afterReady.freshness)
    }

    private fun newHolder(
        storage: FakeThemePreferenceStorage,
        executor: Executor,
    ): OxygenAppStateHolder = OxygenAppStateHolder(
        selectedLocation = themeFixtureLocation("theme-state"),
        weatherRepository = ThemeCountingWeatherRepository(themeFixtureLocation("theme-state")),
        themePreferenceStorage = storage,
        forecastExecutor = executor,
    )
}

private fun openAppearance(holder: OxygenAppStateHolder) {
    holder.onOpenSettings()
    holder.onSettingsDestinationSelected(SettingsDestination.Appearance)
}

private class ThemeControlledExecutor : Executor {
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

private object ThemeDirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class FakeThemePreferenceStorage(
    var stored: OxygenThemeId? = null,
    var readFails: Boolean = false,
    var writeFails: Boolean = false,
) : ThemePreferenceStorage {
    val writes = mutableListOf<OxygenThemeId>()

    override fun readThemePreference(): ThemePreferenceReadResult {
        if (readFails) error("theme read failed")
        return stored?.let(ThemePreferenceReadResult::Supported)
            ?: ThemePreferenceReadResult.NoSupportedChoice
    }

    override fun writeThemePreference(theme: OxygenThemeId) {
        writes += theme
        if (writeFails) error("theme write failed")
        stored = theme
    }
}

private class ThemeCountingWeatherRepository(
    private val fixture: WeatherLocation,
) : WeatherRepository {
    var refreshCount = 0
        private set

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        refreshCount += 1
        return sequenceOf(
            WeatherRepositoryResult.Success(
                WeatherBundle(
                    location = fixture,
                    current = CurrentConditions(
                        time = Instant.parse("2026-09-08T12:00:00Z"),
                        temperatureC = 21.0,
                        condition = WeatherCondition.CLEAR,
                        provenance = DataProvenance(
                            providerId = "theme-test",
                            sourceName = "Theme Test",
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

private class ThemeFakeUnitPreferenceStorage(
    private var stored: UnitPreference?,
) : UnitPreferenceStorage {
    override fun readUnitPreference(): UnitPreference? = stored

    override fun writeUnitPreference(preference: UnitPreference?) {
        stored = preference
    }
}

private class ThemeFakeLayoutPreferenceStorage(
    private var stored: LayoutPreset,
) : LayoutPreferenceStorage {
    override fun readLayoutPreference(): LayoutPreferenceReadResult =
        LayoutPreferenceReadResult.Supported(stored)

    override fun writeLayoutPreference(layout: LayoutPreset) {
        stored = layout
    }
}

private class ThemeFakeEffectsPreferenceStorage(
    private var stored: EffectsLevel,
) : EffectsPreferenceStorage {
    override fun readEffectsPreference(): EffectsLevel = stored

    override fun writeEffectsPreference(effects: EffectsLevel) {
        stored = effects
    }
}

private fun themeFixtureLocation(id: String): WeatherLocation = WeatherLocation(
    id = LocationId(id),
    displayName = "Theme Fixture $id",
    point = GeoPoint(43.0731, -89.4012),
    zoneId = ZoneId.of("America/Chicago"),
)

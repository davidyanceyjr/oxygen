package com.oxygen.weather.app

import com.oxygen.weather.app.ui.theme.ContrastLevel
import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.app.ui.theme.LayoutPreset
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastPreferenceStateHolderTest {
    @Test
    fun `managed startup restores contrast and selection commits only after write`() {
        val executor = ContrastControlledExecutor()
        val storage = FakeContrastPreferenceStorage(stored = ContrastLevel.HIGH)
        val holder = newHolder(storage, executor)

        assertEquals(ContrastPreferenceReadState.Loading, holder.presentationState.contrastPreference.readState)
        assertEquals(ContrastLevel.STANDARD, holder.presentationState.contrast)
        executor.drainAll()
        assertEquals(ContrastLevel.HIGH, holder.presentationState.contrast)
        assertEquals(ContrastLevel.HIGH, holder.presentationState.contrastPreference.confirmed)

        openAppearance(holder)
        holder.onContrastPreferenceSelected(ContrastLevel.STANDARD)
        assertEquals(ContrastLevel.HIGH, holder.presentationState.contrast)
        assertEquals(ContrastLevel.HIGH, holder.presentationState.contrastPreference.confirmed)
        assertEquals(ContrastLevel.STANDARD, holder.presentationState.contrastPreference.pending)
        holder.onContrastPreferenceSelected(ContrastLevel.HIGH)
        assertTrue(storage.writes.isEmpty())

        executor.drainAll()
        assertEquals(listOf(ContrastLevel.STANDARD), storage.writes)
        assertEquals(ContrastLevel.STANDARD, holder.presentationState.contrast)
        assertEquals(ContrastLevel.STANDARD, holder.presentationState.contrastPreference.confirmed)
        assertFalse(holder.presentationState.contrastPreference.writeError)
    }

    @Test
    fun `read and write failures retain confirmed contrast and retry`() {
        val executor = ContrastControlledExecutor()
        val storage = FakeContrastPreferenceStorage(readFails = true)
        val holder = newHolder(storage, executor)
        executor.drainAll()
        assertEquals(ContrastPreferenceReadState.Failed, holder.presentationState.contrastPreference.readState)
        assertEquals(ContrastLevel.STANDARD, holder.presentationState.contrast)

        openAppearance(holder)
        storage.readFails = false
        storage.stored = ContrastLevel.HIGH
        holder.onContrastPreferenceRetry()
        executor.drainAll()
        assertEquals(ContrastLevel.HIGH, holder.presentationState.contrast)

        storage.readFails = true
        holder.onContrastPreferenceRetry()
        executor.drainAll()
        assertEquals(ContrastPreferenceReadState.Failed, holder.presentationState.contrastPreference.readState)
        assertEquals(ContrastLevel.HIGH, holder.presentationState.contrast)
        assertEquals(ContrastLevel.HIGH, holder.presentationState.contrastPreference.confirmed)

        storage.readFails = false
        holder.onContrastPreferenceRetry()
        executor.drainAll()
        storage.writeFails = true
        holder.onContrastPreferenceSelected(ContrastLevel.STANDARD)
        executor.drainAll()
        assertEquals(ContrastLevel.HIGH, holder.presentationState.contrast)
        assertTrue(holder.presentationState.contrastPreference.writeError)
        storage.writeFails = false
        holder.onContrastPreferenceRetry()
        executor.drainAll()
        assertEquals(ContrastLevel.STANDARD, holder.presentationState.contrast)
        assertEquals(listOf(ContrastLevel.STANDARD, ContrastLevel.STANDARD), storage.writes)
    }

    @Test
    fun `contrast transaction preserves other presentation and forecast request count`() {
        val location = contrastFixtureLocation("independence")
        val repository = CountingContrastWeatherRepository(location)
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            layoutPreferenceStorage = ContrastLayoutStorage,
            effectsPreferenceStorage = ContrastEffectsStorage,
            themePreferenceStorage = ContrastThemeStorage,
            contrastPreferenceStorage = FakeContrastPreferenceStorage(),
            forecastExecutor = ContrastDirectExecutor,
        )
        val before = (holder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady
        openAppearance(holder)
        holder.onContrastPreferenceSelected(ContrastLevel.HIGH)

        assertEquals(1, repository.refreshCount)
        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layout)
        assertEquals(EffectsLevel.SUBTLE, holder.presentationState.effectsPreference.confirmed)
        assertEquals(OxygenThemeId.PAPER, holder.presentationState.theme)
        assertEquals(before.dashboard, ((holder.presentationState.screen as OxygenAppScreen.Settings)
            .returnScreen as OxygenAppScreen.Home).forecast.let { it as HomeForecastPresentationState.ForecastReady }.dashboard)
    }

    private fun newHolder(
        storage: FakeContrastPreferenceStorage,
        executor: Executor,
    ) = OxygenAppStateHolder(
        selectedLocation = contrastFixtureLocation("state"),
        weatherRepository = CountingContrastWeatherRepository(contrastFixtureLocation("state")),
        contrastPreferenceStorage = storage,
        forecastExecutor = executor,
    )
}

private fun openAppearance(holder: OxygenAppStateHolder) {
    holder.onOpenSettings()
    holder.onSettingsDestinationSelected(SettingsDestination.Appearance)
}

private class ContrastControlledExecutor : Executor {
    private val tasks = ArrayDeque<Runnable>()
    override fun execute(command: Runnable) = synchronized(tasks) { tasks.addLast(command) }
    fun drainAll() {
        while (true) {
            val task = synchronized(tasks) { if (tasks.isEmpty()) null else tasks.removeFirst() } ?: return
            task.run()
        }
    }
}

private object ContrastDirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class FakeContrastPreferenceStorage(
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

private object ContrastLayoutStorage : LayoutPreferenceStorage {
    override fun readLayoutPreference() = LayoutPreferenceReadResult.Supported(LayoutPreset.SIMPLE)
    override fun writeLayoutPreference(layout: LayoutPreset) = Unit
}

private object ContrastEffectsStorage : EffectsPreferenceStorage {
    override fun readEffectsPreference() = EffectsLevel.SUBTLE
    override fun writeEffectsPreference(effects: EffectsLevel) = Unit
}

private object ContrastThemeStorage : ThemePreferenceStorage {
    override fun readThemePreference() = ThemePreferenceReadResult.Supported(OxygenThemeId.PAPER)
    override fun writeThemePreference(theme: OxygenThemeId) = Unit
}

private class CountingContrastWeatherRepository(
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
                        providerId = "contrast-test",
                        sourceName = "Contrast Test",
                        fetchedAt = Instant.parse("2026-09-08T12:00:00Z"),
                        type = DataType.FORECAST,
                    ),
                ),
                fetchedAt = Instant.parse("2026-09-08T12:00:00Z"),
            ),
        ))
    }
}

private fun contrastFixtureLocation(id: String) = WeatherLocation(
    id = LocationId(id),
    displayName = "Contrast Fixture $id",
    point = GeoPoint(43.0731, -89.4012),
    zoneId = ZoneId.of("America/Chicago"),
)

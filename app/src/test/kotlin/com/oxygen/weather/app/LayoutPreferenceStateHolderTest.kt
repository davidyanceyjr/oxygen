package com.oxygen.weather.app

import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.app.ui.theme.LayoutPreset
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
import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutPreferenceStateHolderTest {
    @Test
    fun `missing storage record defaults to Standard and selection persists`() {
        val location = fixtureLocation("layout-default")
        val storage = LayoutFakePreferenceStorage()
        val repository = LayoutCountingWeatherRepository(location)
        val holder = newHolder(
            location = location,
            storage = storage,
            repository = repository,
        )

        assertEquals(LayoutPreset.STANDARD, holder.presentationState.layout)
        assertEquals(LayoutPreferenceReadState.Loaded, holder.presentationState.layoutPreference.readState)
        assertEquals(LayoutPreset.STANDARD, holder.presentationState.layoutPreference.confirmed)
        assertEquals(1, repository.refreshCount)

        holder.onOpenSettings()
        holder.onSettingsDestinationSelected(SettingsDestination.Appearance)
        holder.onLayoutSelected(LayoutPreset.SIMPLE)

        assertEquals(listOf(LayoutPreset.SIMPLE), storage.writes)
        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layout)
        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layoutPreference.confirmed)
        assertEquals(LayoutPreferenceReadState.Loaded, holder.presentationState.layoutPreference.readState)
        assertEquals(1, repository.refreshCount)

        holder.onLayoutSelected(LayoutPreset.SIMPLE)

        assertEquals(listOf(LayoutPreset.SIMPLE), storage.writes)
        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layout)
    }

    @Test
    fun `read failure is conservative and retry restores the stored choice`() {
        val location = fixtureLocation("layout-read-failure")
        val storage = LayoutFakePreferenceStorage(
            stored = LayoutPreset.SIMPLE,
            readFails = true,
        )
        val holder = newHolder(
            location = location,
            storage = storage,
        )

        assertEquals(LayoutPreset.STANDARD, holder.presentationState.layout)
        assertEquals(LayoutPreferenceReadState.Failed, holder.presentationState.layoutPreference.readState)
        assertEquals(null, holder.presentationState.layoutPreference.confirmed)

        holder.onOpenSettings()
        holder.onSettingsDestinationSelected(SettingsDestination.Appearance)
        storage.readFails = false
        holder.onLayoutPreferenceRetry()

        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layout)
        assertEquals(LayoutPreferenceReadState.Loaded, holder.presentationState.layoutPreference.readState)
        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layoutPreference.confirmed)

        storage.readFails = true
        holder.onLayoutPreferenceRetry()

        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layout)
        assertEquals(LayoutPreferenceReadState.Failed, holder.presentationState.layoutPreference.readState)
        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layoutPreference.confirmed)
    }

    @Test
    fun `failed write retains confirmed choice and a later retry succeeds`() {
        val location = fixtureLocation("layout-write-failure")
        val storage = LayoutFakePreferenceStorage(
            stored = LayoutPreset.SIMPLE,
            writeFails = true,
        )
        val holder = newHolder(
            location = location,
            storage = storage,
        )

        holder.onOpenSettings()
        holder.onSettingsDestinationSelected(SettingsDestination.Appearance)
        holder.onLayoutSelected(LayoutPreset.STANDARD)

        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layout)
        assertTrue(holder.presentationState.layoutPreference.writeError)
        assertEquals(LayoutPreset.SIMPLE, holder.presentationState.layoutPreference.confirmed)
        assertEquals(listOf(LayoutPreset.STANDARD), storage.writes)

        storage.writeFails = false
        holder.onLayoutPreferenceRetry()

        assertEquals(LayoutPreset.STANDARD, holder.presentationState.layout)
        assertFalse(holder.presentationState.layoutPreference.writeError)
        assertEquals(LayoutPreset.STANDARD, holder.presentationState.layoutPreference.confirmed)
        assertEquals(listOf(LayoutPreset.STANDARD, LayoutPreset.STANDARD), storage.writes)
    }

    @Test
    fun `layout changes preserve the forecast units effects and selected location`() {
        val location = fixtureLocation("layout-independence")
        val repository = LayoutCountingWeatherRepository(location)
        val layoutStorage = LayoutFakePreferenceStorage(stored = LayoutPreset.SIMPLE)
        val unitStorage = LayoutFakeUnitPreferenceStorage(UnitPreference.Preset(UnitPreferencePreset.METRIC))
        val effectsStorage = LayoutFakeEffectsPreferenceStorage(EffectsLevel.SUBTLE)
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            layoutPreferenceStorage = layoutStorage,
            unitPreferenceStorage = unitStorage,
            effectsPreferenceStorage = effectsStorage,
            forecastExecutor = LayoutDirectExecutor,
        )

        val before = holder.presentationState.screen as OxygenAppScreen.Home
        val beforeReady = before.forecast as HomeForecastPresentationState.ForecastReady

        holder.onOpenSettings()
        holder.onSettingsDestinationSelected(SettingsDestination.Appearance)
        holder.onLayoutSelected(LayoutPreset.STANDARD)

        assertEquals(1, repository.refreshCount)
        assertEquals(location, holder.presentationState.selectedLocation)
        assertEquals(UnitPreference.Preset(UnitPreferencePreset.METRIC), holder.presentationState.unitPreference)
        assertEquals(EffectsLevel.SUBTLE, holder.presentationState.effectsPreference.confirmed)
        assertEquals(LayoutPreset.STANDARD, holder.presentationState.layout)

        val settings = holder.presentationState.screen as OxygenAppScreen.Settings
        val afterReady = (settings.returnScreen as OxygenAppScreen.Home).forecast as HomeForecastPresentationState.ForecastReady
        assertEquals(beforeReady.dashboard, afterReady.dashboard)
        assertEquals(beforeReady.freshness, afterReady.freshness)
    }

    private fun newHolder(
        location: WeatherLocation = fixtureLocation("layout-fixture"),
        storage: LayoutFakePreferenceStorage,
        repository: LayoutCountingWeatherRepository = LayoutCountingWeatherRepository(location),
    ): OxygenAppStateHolder = OxygenAppStateHolder(
        selectedLocation = location,
        weatherRepository = repository,
        layoutPreferenceStorage = storage,
        forecastExecutor = LayoutDirectExecutor,
    )
}

private object LayoutDirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class LayoutFakePreferenceStorage(
    var stored: LayoutPreset? = null,
    var readFails: Boolean = false,
    var writeFails: Boolean = false,
) : LayoutPreferenceStorage {
    val writes = mutableListOf<LayoutPreset>()

    override fun readLayoutPreference(): LayoutPreferenceReadResult {
        if (readFails) error("layout read failed")
        return when (stored) {
            null -> LayoutPreferenceReadResult.NoSupportedChoice
            else -> LayoutPreferenceReadResult.Supported(stored!!)
        }
    }

    override fun writeLayoutPreference(layout: LayoutPreset) {
        writes += layout
        if (writeFails) error("layout write failed")
        stored = layout
    }
}

private class LayoutCountingWeatherRepository(
    private val location: WeatherLocation,
) : WeatherRepository {
    var refreshCount = 0
        private set

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        refreshCount += 1
        return sequenceOf(
            WeatherRepositoryResult.Success(
                WeatherBundle(
                    location = this.location,
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
                ),
            ),
        )
    }
}

private class LayoutFakeUnitPreferenceStorage(
    private var stored: UnitPreference? = null,
) : UnitPreferenceStorage {
    override fun readUnitPreference(): UnitPreference? = stored

    override fun writeUnitPreference(preference: UnitPreference?) {
        stored = preference
    }
}

private class LayoutFakeEffectsPreferenceStorage(
    private var stored: EffectsLevel = EffectsLevel.SUBTLE,
) : EffectsPreferenceStorage {
    override fun readEffectsPreference(): EffectsLevel? = stored

    override fun writeEffectsPreference(effects: EffectsLevel) {
        stored = effects
    }
}

private fun fixtureLocation(id: String): WeatherLocation = WeatherLocation(
    id = LocationId(id),
    displayName = "Layout Fixture $id",
    point = GeoPoint(43.0731, -89.4012),
    zoneId = ZoneId.of("America/Chicago"),
)

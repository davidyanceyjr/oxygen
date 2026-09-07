package com.oxygen.weather.app

import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.util.concurrent.Executor
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EffectsPreferenceStateHolderTest {
    @Test
    fun `missing storage record defaults to Subtle and selection persists`() {
        val storage = FakeEffectsPreferenceStorage()
        val holder = newHolder(storage)

        assertEquals(EffectsLevel.SUBTLE, holder.presentationState.effectsPreference.confirmed)
        holder.onOpenSettings()
        holder.onSettingsDestinationSelected(SettingsDestination.Appearance)
        holder.onEffectsPreferenceSelected(EffectsLevel.OFF)

        assertEquals(EffectsLevel.OFF, storage.stored)
        assertEquals(EffectsLevel.OFF, holder.presentationState.effectsPreference.confirmed)
        assertFalse(holder.presentationState.effectsPreference.writeError)
    }

    @Test
    fun `read failure is conservative and retry restores the stored choice`() {
        val storage = FakeEffectsPreferenceStorage(readFails = true)
        val holder = newHolder(storage)

        assertEquals(EffectsPreferenceReadState.Failed, holder.presentationState.effectsPreference.readState)
        assertEquals(EffectsLevel.OFF, holder.presentationState.effectsPreference.effectiveRequested)
        holder.onOpenSettings()
        holder.onSettingsDestinationSelected(SettingsDestination.Appearance)
        storage.readFails = false
        storage.stored = EffectsLevel.SUBTLE
        holder.onEffectsPreferenceRetry()

        assertEquals(EffectsLevel.SUBTLE, holder.presentationState.effectsPreference.confirmed)
        assertEquals(EffectsPreferenceReadState.Loaded, holder.presentationState.effectsPreference.readState)
    }

    @Test
    fun `failed write retains confirmed choice and a later choice retries`() {
        val storage = FakeEffectsPreferenceStorage(stored = EffectsLevel.SUBTLE, writeFails = true)
        val holder = newHolder(storage)
        holder.onOpenSettings()
        holder.onSettingsDestinationSelected(SettingsDestination.Appearance)

        holder.onEffectsPreferenceSelected(EffectsLevel.OFF)

        assertEquals(EffectsLevel.SUBTLE, holder.presentationState.effectsPreference.confirmed)
        assertTrue(holder.presentationState.effectsPreference.writeError)
        storage.writeFails = false
        holder.onEffectsPreferenceSelected(EffectsLevel.OFF)

        assertEquals(EffectsLevel.OFF, holder.presentationState.effectsPreference.confirmed)
        assertEquals(listOf(EffectsLevel.OFF, EffectsLevel.OFF), storage.writes)
    }

    @Test
    fun `preference changes preserve the forecast and do not refresh weather`() {
        val location = fixtureLocation()
        val repository = CountingWeatherRepository(location)
        val holder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            effectsPreferenceStorage = FakeEffectsPreferenceStorage(stored = EffectsLevel.SUBTLE),
            forecastExecutor = EffectsDirectExecutor,
        )
        assertEquals(1, repository.refreshCount)

        holder.onOpenSettings()
        holder.onSettingsDestinationSelected(SettingsDestination.Appearance)
        holder.onEffectsPreferenceSelected(EffectsLevel.OFF)

        assertEquals(1, repository.refreshCount)
        assertEquals(location, holder.presentationState.selectedLocation)
        assertTrue(holder.presentationState.screen is OxygenAppScreen.Settings)
        assertEquals(EffectsLevel.OFF, holder.presentationState.effectsPreference.confirmed)
    }

    private fun newHolder(storage: FakeEffectsPreferenceStorage): OxygenAppStateHolder = OxygenAppStateHolder(
        effectsPreferenceStorage = storage,
        forecastExecutor = EffectsDirectExecutor,
    )
}

private object EffectsDirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class FakeEffectsPreferenceStorage(
    var stored: EffectsLevel? = null,
    var readFails: Boolean = false,
    var writeFails: Boolean = false,
) : EffectsPreferenceStorage {
    val writes = mutableListOf<EffectsLevel>()

    override fun readEffectsPreference(): EffectsLevel? {
        if (readFails) error("effects read failed")
        return stored
    }

    override fun writeEffectsPreference(effects: EffectsLevel) {
        if (writeFails) {
            writes += effects
            error("effects write failed")
        }
        writes += effects
        stored = effects
    }
}

private class CountingWeatherRepository(
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
    id = com.oxygen.weather.core.model.LocationId("effects-fixture"),
    displayName = "Effects Fixture",
    point = com.oxygen.weather.core.model.GeoPoint(43.0, -89.0),
    zoneId = java.time.ZoneId.of("America/Chicago"),
)

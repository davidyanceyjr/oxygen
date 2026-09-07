package com.oxygen.weather.app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oxygen.weather.app.ui.theme.EffectsLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface EffectsPreferenceStorage {
    fun readEffectsPreference(): EffectsLevel?

    fun writeEffectsPreference(effects: EffectsLevel)
}

object EmptyEffectsPreferenceStorage : EffectsPreferenceStorage {
    override fun readEffectsPreference(): EffectsLevel? = null

    override fun writeEffectsPreference(effects: EffectsLevel) = Unit
}

class DataStoreEffectsPreferenceStorage(
    context: Context,
) : EffectsPreferenceStorage {
    private val dataStore = context.applicationContext.effectsPreferenceDataStore

    override fun readEffectsPreference(): EffectsLevel? = runBlocking {
        val preferences = dataStore.data.first()
        EffectsPreferenceStorageCodec.decode(
            version = preferences[EffectsPreferenceKeys.Version],
            value = preferences[EffectsPreferenceKeys.Value],
        )
    }

    override fun writeEffectsPreference(effects: EffectsLevel) {
        require(effects != EffectsLevel.FULL) { "Full effects are not persisted by this slice" }
        runBlocking {
            dataStore.edit { preferences ->
                preferences[EffectsPreferenceKeys.Version] = EffectsPreferenceStorageCodec.VERSION
                preferences[EffectsPreferenceKeys.Value] = EffectsPreferenceStorageCodec.encode(effects)
            }
        }
    }
}

internal object EffectsPreferenceStorageCodec {
    const val VERSION = "1"

    fun encode(effects: EffectsLevel): String = when (effects) {
        EffectsLevel.OFF -> "off"
        EffectsLevel.SUBTLE -> "subtle"
        EffectsLevel.FULL -> error("Full effects are not persisted by this slice")
    }

    fun decode(version: String?, value: String?): EffectsLevel? {
        if (version != VERSION) return null
        return when (value) {
            "off" -> EffectsLevel.OFF
            "subtle" -> EffectsLevel.SUBTLE
            else -> null
        }
    }
}

private val Context.effectsPreferenceDataStore by preferencesDataStore(
    name = "oxygen_effects_preferences",
)

private object EffectsPreferenceKeys {
    val Version = stringPreferencesKey("version")
    val Value = stringPreferencesKey("effects")
}

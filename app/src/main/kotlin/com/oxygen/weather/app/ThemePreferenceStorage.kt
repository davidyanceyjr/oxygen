package com.oxygen.weather.app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oxygen.weather.app.ui.theme.OxygenThemeId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface ThemePreferenceStorage {
    fun readThemePreference(): ThemePreferenceReadResult

    fun writeThemePreference(theme: OxygenThemeId)
}

sealed interface ThemePreferenceReadResult {
    data object NoSupportedChoice : ThemePreferenceReadResult

    data class Supported(
        val theme: OxygenThemeId,
    ) : ThemePreferenceReadResult
}

object EmptyThemePreferenceStorage : ThemePreferenceStorage {
    override fun readThemePreference(): ThemePreferenceReadResult = ThemePreferenceReadResult.NoSupportedChoice

    override fun writeThemePreference(theme: OxygenThemeId) = Unit
}

class DataStoreThemePreferenceStorage(
    context: Context,
) : ThemePreferenceStorage {
    private val dataStore = context.applicationContext.themePreferenceDataStore

    override fun readThemePreference(): ThemePreferenceReadResult = runBlocking {
        val preferences = dataStore.data.first()
        ThemePreferenceStorageCodec.decode(
            version = preferences[ThemePreferenceKeys.Version],
            value = preferences[ThemePreferenceKeys.Value],
        )
    }

    override fun writeThemePreference(theme: OxygenThemeId) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[ThemePreferenceKeys.Version] = ThemePreferenceStorageCodec.VERSION
                preferences[ThemePreferenceKeys.Value] = ThemePreferenceStorageCodec.encode(theme)
            }
        }
    }
}

internal object ThemePreferenceStorageCodec {
    const val VERSION = 1

    fun encode(theme: OxygenThemeId): String = when (theme) {
        OxygenThemeId.OXYGEN -> "oxygen"
        OxygenThemeId.PAPER -> "paper"
        OxygenThemeId.TERMINAL -> "terminal"
    }

    fun decode(version: Int?, value: String?): ThemePreferenceReadResult {
        if (version != VERSION) return ThemePreferenceReadResult.NoSupportedChoice
        return when (value) {
            "oxygen" -> ThemePreferenceReadResult.Supported(OxygenThemeId.OXYGEN)
            "paper" -> ThemePreferenceReadResult.Supported(OxygenThemeId.PAPER)
            "terminal" -> ThemePreferenceReadResult.Supported(OxygenThemeId.TERMINAL)
            else -> ThemePreferenceReadResult.NoSupportedChoice
        }
    }
}

private val Context.themePreferenceDataStore by preferencesDataStore(
    name = "oxygen_theme_preferences",
)

private object ThemePreferenceKeys {
    val Version = intPreferencesKey("theme_preference_version")
    val Value = stringPreferencesKey("theme_preference_value")
}

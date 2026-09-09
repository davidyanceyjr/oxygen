package com.oxygen.weather.app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oxygen.weather.app.ui.theme.ContrastLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface ContrastPreferenceStorage {
    fun readContrastPreference(): ContrastPreferenceReadResult

    fun writeContrastPreference(contrast: ContrastLevel)
}

sealed interface ContrastPreferenceReadResult {
    data object NoSupportedChoice : ContrastPreferenceReadResult

    data class Supported(
        val contrast: ContrastLevel,
    ) : ContrastPreferenceReadResult
}

object EmptyContrastPreferenceStorage : ContrastPreferenceStorage {
    override fun readContrastPreference(): ContrastPreferenceReadResult =
        ContrastPreferenceReadResult.NoSupportedChoice

    override fun writeContrastPreference(contrast: ContrastLevel) = Unit
}

class DataStoreContrastPreferenceStorage(
    context: Context,
) : ContrastPreferenceStorage {
    private val dataStore = context.applicationContext.contrastPreferenceDataStore

    override fun readContrastPreference(): ContrastPreferenceReadResult = runBlocking {
        val preferences = dataStore.data.first()
        ContrastPreferenceStorageCodec.decode(
            version = preferences[ContrastPreferenceKeys.Version],
            value = preferences[ContrastPreferenceKeys.Value],
        )
    }

    override fun writeContrastPreference(contrast: ContrastLevel) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[ContrastPreferenceKeys.Version] = ContrastPreferenceStorageCodec.VERSION
                preferences[ContrastPreferenceKeys.Value] = ContrastPreferenceStorageCodec.encode(contrast)
            }
        }
    }
}

internal object ContrastPreferenceStorageCodec {
    const val VERSION = 1

    fun encode(contrast: ContrastLevel): String = when (contrast) {
        ContrastLevel.STANDARD -> "standard"
        ContrastLevel.HIGH -> "high"
    }

    fun decode(version: Int?, value: String?): ContrastPreferenceReadResult {
        if (version != VERSION) return ContrastPreferenceReadResult.NoSupportedChoice
        return when (value) {
            "standard" -> ContrastPreferenceReadResult.Supported(ContrastLevel.STANDARD)
            "high" -> ContrastPreferenceReadResult.Supported(ContrastLevel.HIGH)
            else -> ContrastPreferenceReadResult.NoSupportedChoice
        }
    }
}

private val Context.contrastPreferenceDataStore by preferencesDataStore(
    name = "oxygen_contrast_preferences",
)

private object ContrastPreferenceKeys {
    val Version = intPreferencesKey("contrast_preference_version")
    val Value = stringPreferencesKey("contrast_preference_value")
}

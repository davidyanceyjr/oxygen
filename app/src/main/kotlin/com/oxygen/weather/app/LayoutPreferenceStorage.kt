package com.oxygen.weather.app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oxygen.weather.app.ui.theme.LayoutPreset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface LayoutPreferenceStorage {
    fun readLayoutPreference(): LayoutPreferenceReadResult

    fun writeLayoutPreference(layout: LayoutPreset)
}

sealed interface LayoutPreferenceReadResult {
    data object NoSupportedChoice : LayoutPreferenceReadResult

    data class Supported(
        val layout: LayoutPreset,
    ) : LayoutPreferenceReadResult
}

object EmptyLayoutPreferenceStorage : LayoutPreferenceStorage {
    override fun readLayoutPreference(): LayoutPreferenceReadResult = LayoutPreferenceReadResult.NoSupportedChoice

    override fun writeLayoutPreference(layout: LayoutPreset) = Unit
}

class DataStoreLayoutPreferenceStorage(
    context: Context,
) : LayoutPreferenceStorage {
    private val dataStore = context.applicationContext.layoutPreferenceDataStore

    override fun readLayoutPreference(): LayoutPreferenceReadResult = runBlocking {
        val preferences = dataStore.data.first()
        LayoutPreferenceStorageCodec.decode(
            version = preferences[LayoutPreferenceKeys.Version],
            value = preferences[LayoutPreferenceKeys.Value],
        )
    }

    override fun writeLayoutPreference(layout: LayoutPreset) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences.remove(LayoutPreferenceKeys.Version)
                preferences.remove(LayoutPreferenceKeys.Value)
                val encoded = LayoutPreferenceStorageCodec.encode(layout)
                preferences[LayoutPreferenceKeys.Version] = encoded.version
                preferences[LayoutPreferenceKeys.Value] = encoded.value
            }
        }
    }
}

internal object LayoutPreferenceStorageCodec {
    const val VERSION = "1"
    const val SIMPLE = "simple"
    const val STANDARD = "standard"

    fun encode(layout: LayoutPreset): LayoutPreferenceEncodedValues = when (layout) {
        LayoutPreset.SIMPLE -> LayoutPreferenceEncodedValues(
            version = 1,
            value = SIMPLE,
        )
        LayoutPreset.STANDARD -> LayoutPreferenceEncodedValues(
            version = 1,
            value = STANDARD,
        )
        LayoutPreset.DETAILED,
        LayoutPreset.METEOROLOGIST -> error("Unsupported layout preset cannot be persisted by this slice")
    }

    fun decode(version: Int?, value: String?): LayoutPreferenceReadResult {
        if (version != 1) return LayoutPreferenceReadResult.NoSupportedChoice
        return when (value?.trim()?.lowercase()) {
            SIMPLE -> LayoutPreferenceReadResult.Supported(LayoutPreset.SIMPLE)
            STANDARD -> LayoutPreferenceReadResult.Supported(LayoutPreset.STANDARD)
            else -> LayoutPreferenceReadResult.NoSupportedChoice
        }
    }
}

internal data class LayoutPreferenceEncodedValues(
    val version: Int,
    val value: String,
)

private val Context.layoutPreferenceDataStore by preferencesDataStore(
    name = "oxygen_layout_preferences",
)

private object LayoutPreferenceKeys {
    val Version = intPreferencesKey("layout_preference_version")
    val Value = stringPreferencesKey("layout_preference_value")
}

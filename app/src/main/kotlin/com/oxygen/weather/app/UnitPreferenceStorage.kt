package com.oxygen.weather.app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oxygen.weather.core.model.PrecipitationUnit
import com.oxygen.weather.core.model.PressureUnit
import com.oxygen.weather.core.model.TemperatureUnit
import com.oxygen.weather.core.model.UnitPreference
import com.oxygen.weather.core.model.UnitPreferencePreset
import com.oxygen.weather.core.model.VisibilityUnit
import com.oxygen.weather.core.model.WindSpeedUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface UnitPreferenceStorage {
    fun readUnitPreference(): UnitPreference?
    fun writeUnitPreference(preference: UnitPreference?)
}

object EmptyUnitPreferenceStorage : UnitPreferenceStorage {
    override fun readUnitPreference(): UnitPreference? = null

    override fun writeUnitPreference(preference: UnitPreference?) = Unit
}

class DataStoreUnitPreferenceStorage(
    context: Context,
) : UnitPreferenceStorage {
    private val dataStore = context.applicationContext.unitPreferenceDataStore

    override fun readUnitPreference(): UnitPreference? = runBlocking {
        try {
            val preferences = dataStore.data.first()
            UnitPreferenceStorageCodec.decode(
                buildMap {
                    preferences[UnitPreferenceKeys.Version]?.let { put(UnitPreferenceStorageCodec.VERSION, it) }
                    preferences[UnitPreferenceKeys.Kind]?.let { put(UnitPreferenceStorageCodec.KIND, it) }
                    preferences[UnitPreferenceKeys.Preset]?.let { put(UnitPreferenceStorageCodec.PRESET, it) }
                    preferences[UnitPreferenceKeys.Temperature]?.let { put(UnitPreferenceStorageCodec.TEMPERATURE, it) }
                    preferences[UnitPreferenceKeys.WindSpeed]?.let { put(UnitPreferenceStorageCodec.WIND_SPEED, it) }
                    preferences[UnitPreferenceKeys.Pressure]?.let { put(UnitPreferenceStorageCodec.PRESSURE, it) }
                    preferences[UnitPreferenceKeys.Precipitation]?.let { put(UnitPreferenceStorageCodec.PRECIPITATION, it) }
                    preferences[UnitPreferenceKeys.Visibility]?.let { put(UnitPreferenceStorageCodec.VISIBILITY, it) }
                },
            )
        } catch (_: Exception) {
            null
        }
    }

    override fun writeUnitPreference(preference: UnitPreference?) {
        runBlocking {
            dataStore.edit { preferences ->
                UnitPreferenceKeys.all.forEach(preferences::remove)
                UnitPreferenceStorageCodec.encode(preference).forEach { (key, value) ->
                    preferences[UnitPreferenceKeys.keyFor(key)] = value
                }
            }
        }
    }
}

internal object UnitPreferenceStorageCodec {
    const val VERSION = "version"
    const val KIND = "kind"
    const val PRESET = "preset"
    const val TEMPERATURE = "temperature"
    const val WIND_SPEED = "wind_speed"
    const val PRESSURE = "pressure"
    const val PRECIPITATION = "precipitation"
    const val VISIBILITY = "visibility"

    fun encode(preference: UnitPreference?): Map<String, String> = when (preference) {
        null -> emptyMap()
        is UnitPreference.Preset -> mapOf(
            VERSION to "1",
            KIND to "preset",
            PRESET to preference.preset.name,
        )
        is UnitPreference.Custom -> mapOf(
            VERSION to "1",
            KIND to "custom",
            TEMPERATURE to preference.temperature.name,
            WIND_SPEED to preference.windSpeed.name,
            PRESSURE to preference.pressure.name,
            PRECIPITATION to preference.precipitation.name,
            VISIBILITY to preference.visibility.name,
        )
    }

    fun decode(values: Map<String, String>): UnitPreference? {
        if (values[VERSION] != "1") return null
        return when (values[KIND]) {
            "preset" -> values[PRESET]?.let { name ->
                runCatching { UnitPreference.Preset(UnitPreferencePreset.valueOf(name)) }.getOrNull()
            }
            "custom" -> runCatching {
                UnitPreference.Custom(
                    temperature = TemperatureUnit.valueOf(values.required(TEMPERATURE)),
                    windSpeed = WindSpeedUnit.valueOf(values.required(WIND_SPEED)),
                    pressure = PressureUnit.valueOf(values.required(PRESSURE)),
                    precipitation = PrecipitationUnit.valueOf(values.required(PRECIPITATION)),
                    visibility = VisibilityUnit.valueOf(values.required(VISIBILITY)),
                )
            }.getOrNull()
            else -> null
        }
    }

    private fun Map<String, String>.required(key: String): String = get(key)?.takeIf(String::isNotBlank)
        ?: error("missing unit preference field: $key")
}

private val Context.unitPreferenceDataStore by preferencesDataStore(
    name = "oxygen_unit_preferences",
)

private object UnitPreferenceKeys {
    val Version = stringPreferencesKey(UnitPreferenceStorageCodec.VERSION)
    val Kind = stringPreferencesKey(UnitPreferenceStorageCodec.KIND)
    val Preset = stringPreferencesKey(UnitPreferenceStorageCodec.PRESET)
    val Temperature = stringPreferencesKey(UnitPreferenceStorageCodec.TEMPERATURE)
    val WindSpeed = stringPreferencesKey(UnitPreferenceStorageCodec.WIND_SPEED)
    val Pressure = stringPreferencesKey(UnitPreferenceStorageCodec.PRESSURE)
    val Precipitation = stringPreferencesKey(UnitPreferenceStorageCodec.PRECIPITATION)
    val Visibility = stringPreferencesKey(UnitPreferenceStorageCodec.VISIBILITY)

    val all = listOf(Version, Kind, Preset, Temperature, WindSpeed, Pressure, Precipitation, Visibility)

    fun keyFor(key: String) = when (key) {
        UnitPreferenceStorageCodec.VERSION -> Version
        UnitPreferenceStorageCodec.KIND -> Kind
        UnitPreferenceStorageCodec.PRESET -> Preset
        UnitPreferenceStorageCodec.TEMPERATURE -> Temperature
        UnitPreferenceStorageCodec.WIND_SPEED -> WindSpeed
        UnitPreferenceStorageCodec.PRESSURE -> Pressure
        UnitPreferenceStorageCodec.PRECIPITATION -> Precipitation
        UnitPreferenceStorageCodec.VISIBILITY -> Visibility
        else -> error("unknown unit preference key: $key")
    }
}

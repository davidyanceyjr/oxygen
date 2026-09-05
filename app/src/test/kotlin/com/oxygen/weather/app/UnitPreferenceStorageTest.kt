package com.oxygen.weather.app

import com.oxygen.weather.core.model.PrecipitationUnit
import com.oxygen.weather.core.model.PressureUnit
import com.oxygen.weather.core.model.TemperatureUnit
import com.oxygen.weather.core.model.UnitPreference
import com.oxygen.weather.core.model.UnitPreferencePreset
import com.oxygen.weather.core.model.VisibilityUnit
import com.oxygen.weather.core.model.WindSpeedUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UnitPreferenceStorageTest {
    @Test
    fun `codec round trips every preset and a fully populated custom preference`() {
        val preferences = listOf(
            UnitPreference.Preset(UnitPreferencePreset.METRIC),
            UnitPreference.Preset(UnitPreferencePreset.US),
            UnitPreference.Preset(UnitPreferencePreset.UK),
            UnitPreference.Custom(
                temperature = TemperatureUnit.FAHRENHEIT,
                windSpeed = WindSpeedUnit.KNOTS,
                pressure = PressureUnit.MILLIMETERS_OF_MERCURY,
                precipitation = PrecipitationUnit.INCHES,
                visibility = VisibilityUnit.MILES,
            ),
        )

        preferences.forEach { expected ->
            assertEquals(expected, UnitPreferenceStorageCodec.decode(UnitPreferenceStorageCodec.encode(expected)))
        }
    }

    @Test
    fun `codec rejects absent incomplete unknown and invalid records without changing stored values`() {
        val absent = emptyMap<String, String>()
        assertNull(UnitPreferenceStorageCodec.decode(absent))
        assertEquals(emptyMap<String, String>(), absent)

        val metric = UnitPreferenceStorageCodec.encode(UnitPreference.Preset(UnitPreferencePreset.METRIC))
        listOf(
            metric - UnitPreferenceStorageCodec.PRESET,
            metric + (UnitPreferenceStorageCodec.PRESET to "UNKNOWN"),
            metric + (UnitPreferenceStorageCodec.VERSION to "99"),
        ).forEach { stored ->
            val before = stored.toMap()
            assertNull(UnitPreferenceStorageCodec.decode(stored))
            assertEquals(before, stored)
        }

        val custom = UnitPreferenceStorageCodec.encode(
            UnitPreference.Custom(
                temperature = TemperatureUnit.CELSIUS,
                windSpeed = WindSpeedUnit.METERS_PER_SECOND,
                pressure = PressureUnit.HECTOPASCALS,
                precipitation = PrecipitationUnit.MILLIMETERS,
                visibility = VisibilityUnit.KILOMETERS,
            ),
        )
        val invalidCustom = custom + (UnitPreferenceStorageCodec.WIND_SPEED to "not-a-wind-unit")
        assertNull(UnitPreferenceStorageCodec.decode(invalidCustom))
        assertEquals(invalidCustom, invalidCustom.toMap())
    }
}

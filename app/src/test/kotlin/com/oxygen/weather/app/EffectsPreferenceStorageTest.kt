package com.oxygen.weather.app

import com.oxygen.weather.app.ui.theme.EffectsLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EffectsPreferenceStorageTest {
    @Test
    fun `codec accepts only the versioned Off and Subtle records`() {
        assertEquals(EffectsLevel.OFF, EffectsPreferenceStorageCodec.decode("1", "off"))
        assertEquals(EffectsLevel.SUBTLE, EffectsPreferenceStorageCodec.decode("1", "subtle"))
        assertNull(EffectsPreferenceStorageCodec.decode(null, "off"))
        assertNull(EffectsPreferenceStorageCodec.decode("2", "subtle"))
        assertNull(EffectsPreferenceStorageCodec.decode("1", "full"))
    }

    @Test
    fun `codec writes stable values without enum ordinals`() {
        assertEquals("off", EffectsPreferenceStorageCodec.encode(EffectsLevel.OFF))
        assertEquals("subtle", EffectsPreferenceStorageCodec.encode(EffectsLevel.SUBTLE))
    }
}

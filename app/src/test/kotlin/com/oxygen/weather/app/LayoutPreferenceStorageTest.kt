package com.oxygen.weather.app

import com.oxygen.weather.app.ui.theme.LayoutPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LayoutPreferenceStorageTest {
    @Test
    fun `codec round trips only the supported Simple and Standard records`() {
        assertEquals(
            LayoutPreferenceReadResult.Supported(LayoutPreset.SIMPLE),
            LayoutPreferenceStorageCodec.decode(1, LayoutPreferenceStorageCodec.SIMPLE),
        )
        assertEquals(
            LayoutPreferenceReadResult.Supported(LayoutPreset.STANDARD),
            LayoutPreferenceStorageCodec.decode(1, LayoutPreferenceStorageCodec.STANDARD),
        )
        assertEquals(
            LayoutPreferenceEncodedValues(1, LayoutPreferenceStorageCodec.SIMPLE),
            LayoutPreferenceStorageCodec.encode(LayoutPreset.SIMPLE),
        )
        assertEquals(
            LayoutPreferenceEncodedValues(1, LayoutPreferenceStorageCodec.STANDARD),
            LayoutPreferenceStorageCodec.encode(LayoutPreset.STANDARD),
        )
    }

    @Test
    fun `codec rejects missing blank unknown versioned and future values`() {
        val rejectedInputs = listOf(
            null to null,
            1 to null,
            null to LayoutPreferenceStorageCodec.SIMPLE,
            2 to LayoutPreferenceStorageCodec.STANDARD,
            1 to "",
            1 to "unknown",
            1 to "detailed",
            1 to "meteorologist",
        )

        rejectedInputs.forEach { (version, value) ->
            assertEquals(
                LayoutPreferenceReadResult.NoSupportedChoice,
                LayoutPreferenceStorageCodec.decode(version, value),
            )
        }
    }

    @Test
    fun `codec rejects unsupported future layout presets when encoding`() {
        try {
            LayoutPreferenceStorageCodec.encode(LayoutPreset.DETAILED)
            fail("Expected Detailed to be rejected")
        } catch (_: IllegalStateException) {
        }

        try {
            LayoutPreferenceStorageCodec.encode(LayoutPreset.METEOROLOGIST)
            fail("Expected Meteorologist to be rejected")
        } catch (_: IllegalStateException) {
        }
    }
}

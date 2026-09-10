package com.oxygen.weather.app

import com.oxygen.weather.app.ui.theme.ContrastLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class ContrastPreferenceStorageTest {
    @Test
    fun `supported contrast values encode canonically and round trip`() {
        mapOf(
            ContrastLevel.STANDARD to "standard",
            ContrastLevel.HIGH to "high",
        ).forEach { (contrast, value) ->
            assertEquals(value, ContrastPreferenceStorageCodec.encode(contrast))
            assertEquals(
                ContrastPreferenceReadResult.Supported(contrast),
                ContrastPreferenceStorageCodec.decode(ContrastPreferenceStorageCodec.VERSION, value),
            )
        }
    }

    @Test
    fun `codec rejects missing partial malformed noncanonical and unsupported records`() {
        listOf(
            null to null,
            ContrastPreferenceStorageCodec.VERSION to null,
            null to "high",
            0 to "standard",
            2 to "high",
            ContrastPreferenceStorageCodec.VERSION to "",
            ContrastPreferenceStorageCodec.VERSION to " high ",
            ContrastPreferenceStorageCodec.VERSION to "HIGH",
            ContrastPreferenceStorageCodec.VERSION to "unknown",
        ).forEach { (version, value) ->
            assertEquals(
                ContrastPreferenceReadResult.NoSupportedChoice,
                ContrastPreferenceStorageCodec.decode(version, value),
            )
        }
    }
}

package com.oxygen.weather.app

import com.oxygen.weather.app.ui.theme.OxygenThemeId
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemePreferenceStorageTest {
    @Test
    fun `supported themes encode to stable values and round trip`() {
        val expected = mapOf(
            OxygenThemeId.OXYGEN to "oxygen",
            OxygenThemeId.PAPER to "paper",
            OxygenThemeId.TERMINAL to "terminal",
        )

        expected.forEach { (theme, value) ->
            assertEquals(value, ThemePreferenceStorageCodec.encode(theme))
            assertEquals(
                ThemePreferenceReadResult.Supported(theme),
                ThemePreferenceStorageCodec.decode(ThemePreferenceStorageCodec.VERSION, value),
            )
        }
    }

    @Test
    fun `codec rejects missing partial malformed and noncanonical records`() {
        val rejectedRecords = listOf(
            null to null,
            ThemePreferenceStorageCodec.VERSION to null,
            null to "paper",
            0 to "oxygen",
            2 to "terminal",
            ThemePreferenceStorageCodec.VERSION to "",
            ThemePreferenceStorageCodec.VERSION to " ",
            ThemePreferenceStorageCodec.VERSION to " Paper ",
            ThemePreferenceStorageCodec.VERSION to "PAPER",
            ThemePreferenceStorageCodec.VERSION to "unknown",
        )

        rejectedRecords.forEach { (version, value) ->
            assertEquals(
                ThemePreferenceReadResult.NoSupportedChoice,
                ThemePreferenceStorageCodec.decode(version, value),
            )
        }
    }
}

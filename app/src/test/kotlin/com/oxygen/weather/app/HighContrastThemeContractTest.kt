package com.oxygen.weather.app

import androidx.compose.ui.graphics.Color
import com.oxygen.weather.app.ui.theme.ContrastLevel
import com.oxygen.weather.app.ui.theme.OxygenAppearance
import com.oxygen.weather.app.ui.theme.OxygenThemeId
import com.oxygen.weather.app.ui.theme.resolveOxygenTheme
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HighContrastThemeContractTest {
    @Test
    fun `appearance defaults to standard contrast`() {
        assertEquals(ContrastLevel.STANDARD, OxygenAppearance().contrast)
    }

    @Test
    fun `high contrast retains theme identity and typography while resolving opaque roles`() {
        OxygenThemeId.values().forEach { themeId ->
            val standard = resolveOxygenTheme(themeId, ContrastLevel.STANDARD)
            val high = resolveOxygenTheme(themeId, ContrastLevel.HIGH)
            val roles = high.homeDesign

            assertEquals(themeId, high.spec.id)
            assertEquals(standard.spec.typography, high.spec.typography)
            listOf(
                roles.normalContent,
                roles.supportingContent,
                roles.warningContent,
                roles.ambientGlassSurface,
                roles.strongGlassSurface,
                roles.outlineStrong,
                roles.outlineQuiet,
                roles.weatherMarkGold,
                roles.weatherMarkQuiet,
            ).forEach { color -> assertEquals(1f, color.alpha) }
            assertTrue(roles.normalBorderWidth >= roles.selectedBorderWidth / 2f)
            assertTrue(roles.selectedBorderWidth >= roles.normalBorderWidth)

            assertContrastAtLeast("normal", roles.normalContent, roles.strongGlassSurface, 7.0)
            assertContrastAtLeast("supporting", roles.supportingContent, roles.strongGlassSurface, 7.0)
            assertContrastAtLeast("warning", roles.warningContent, roles.strongGlassSurface, 7.0)
            assertContrastAtLeast("strong outline", roles.outlineStrong, roles.strongGlassSurface, 3.0)
            assertContrastAtLeast("quiet outline", roles.outlineQuiet, roles.ambientGlassSurface, 3.0)
            assertContrastAtLeast("weather mark", roles.weatherMarkGold, roles.strongGlassSurface, 3.0)
        }
    }

    @Test
    fun `standard resolution preserves existing Oxygen roles`() {
        val resolved = resolveOxygenTheme(OxygenThemeId.OXYGEN, ContrastLevel.STANDARD)
        assertEquals(Color(0x5523414D), resolved.homeDesign.ambientGlassSurface)
        assertEquals(Color(0xAA17313C), resolved.homeDesign.strongGlassSurface)
        assertEquals(Color(0x667FC1CE), resolved.homeDesign.outlineAccent)
        assertEquals(Color.Unspecified, resolved.homeDesign.warningContent)
    }

    private fun assertContrastAtLeast(name: String, foreground: Color, background: Color, minimum: Double) {
        assertTrue("$name contrast ${contrastRatio(foreground, background)} < $minimum", contrastRatio(foreground, background) >= minimum)
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val firstLuminance = relativeLuminance(first)
        val secondLuminance = relativeLuminance(second)
        return (maxOf(firstLuminance, secondLuminance) + 0.05) /
            (minOf(firstLuminance, secondLuminance) + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        fun linear(channel: Float): Double {
            val value = channel.toDouble()
            return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(color.red) + 0.7152 * linear(color.green) + 0.0722 * linear(color.blue)
    }
}

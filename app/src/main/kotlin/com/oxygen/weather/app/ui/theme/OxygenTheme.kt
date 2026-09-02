package com.oxygen.weather.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class OxygenPalette(
    val skyTop: Color,
    val skyBottom: Color,
    val atmosphericGlow: Color,
    val glass: Color,
    val glassStrong: Color,
    val outline: Color,
    val chartAccent: Color,
    val precipitation: Color,
    val warning: Color,
)

@Immutable
data class OxygenHomeDesignRoles(
    val pageMarginHorizontal: Dp,
    val pageMarginVertical: Dp,
    val pageGap: Dp,
    val sectionGap: Dp,
    val tileGap: Dp,
    val cardPadding: Dp,
    val compactCardPadding: Dp,
    val homeCardCorner: Dp,
    val weatherMarkGold: Color,
    val weatherMarkQuiet: Color,
    val ambientGlassSurface: Color,
    val strongGlassSurface: Color,
    val outlineAccent: Color,
    val displayWeatherValue: TextStyle,
    val sectionHeading: TextStyle,
    val supportingLabel: TextStyle,
    val compactWeatherValue: TextStyle,
)

enum class OxygenThemeId(val displayName: String) {
    OXYGEN("Oxygen"),
    PAPER("Paper"),
    TERMINAL("Terminal"),
}

data class OxygenThemeSpec(
    val id: OxygenThemeId,
    val dark: Boolean,
    val palette: OxygenPalette,
    val typography: Typography,
)

val LocalOxygenPalette = staticCompositionLocalOf {
    OxygenPalette(
        skyTop = Color.Black,
        skyBottom = Color.Black,
        atmosphericGlow = Color.Transparent,
        glass = Color.Black,
        glassStrong = Color.Black,
        outline = Color.Gray,
        chartAccent = Color.White,
        precipitation = Color.White,
        warning = Color.Red,
    )
}

val LocalOxygenHomeDesign = staticCompositionLocalOf {
    OxygenHomeDesignRoles(
        pageMarginHorizontal = 18.dp,
        pageMarginVertical = 18.dp,
        pageGap = 10.dp,
        sectionGap = 16.dp,
        tileGap = 10.dp,
        cardPadding = 16.dp,
        compactCardPadding = 10.dp,
        homeCardCorner = 8.dp,
        weatherMarkGold = Color(0xFFFFD28A),
        weatherMarkQuiet = Color(0xFFE8F8FB),
        ambientGlassSurface = Color(0x5523414D),
        strongGlassSurface = Color(0xAA17313C),
        outlineAccent = Color(0x667FC1CE),
        displayWeatherValue = Typography().displayMedium.copy(fontWeight = FontWeight.Light),
        sectionHeading = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
        supportingLabel = Typography().labelSmall.copy(fontWeight = FontWeight.SemiBold),
        compactWeatherValue = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val OxygenSpec = OxygenThemeSpec(
    id = OxygenThemeId.OXYGEN,
    dark = true,
    palette = OxygenPalette(
        skyTop = Color(0xFF07151D),
        skyBottom = Color(0xFF153444),
        atmosphericGlow = Color(0xFF86E4F0),
        glass = Color(0x5523414D),
        glassStrong = Color(0xAA17313C),
        outline = Color(0x667FC1CE),
        chartAccent = Color(0xFF8DE7F1),
        precipitation = Color(0xFF79BFFF),
        warning = Color(0xFFFFB4AB),
    ),
    typography = Typography(),
)

private val PaperSpec = OxygenThemeSpec(
    id = OxygenThemeId.PAPER,
    dark = false,
    palette = OxygenPalette(
        skyTop = Color(0xFFF4F0E7),
        skyBottom = Color(0xFFE7E0D2),
        atmosphericGlow = Color(0xFFFFD28A),
        glass = Color(0xCCFAF7F0),
        glassStrong = Color(0xFFF5F0E7),
        outline = Color(0x55817161),
        chartAccent = Color(0xFF345A67),
        precipitation = Color(0xFF356C91),
        warning = Color(0xFF9C3A32),
    ),
    typography = Typography(),
)

private val TerminalSpec = OxygenThemeSpec(
    id = OxygenThemeId.TERMINAL,
    dark = true,
    palette = OxygenPalette(
        skyTop = Color(0xFF020806),
        skyBottom = Color(0xFF06110B),
        atmosphericGlow = Color(0xFF77FF9D),
        glass = Color(0xAA06130C),
        glassStrong = Color(0xEE06130C),
        outline = Color(0x6677FF9D),
        chartAccent = Color(0xFF77FF9D),
        precipitation = Color(0xFF78D6FF),
        warning = Color(0xFFFF847C),
    ),
    typography = Typography(
        bodyLarge = Typography().bodyLarge.copy(fontFamily = FontFamily.Monospace),
        bodyMedium = Typography().bodyMedium.copy(fontFamily = FontFamily.Monospace),
        bodySmall = Typography().bodySmall.copy(fontFamily = FontFamily.Monospace),
        titleLarge = Typography().titleLarge.copy(fontFamily = FontFamily.Monospace),
        titleMedium = Typography().titleMedium.copy(fontFamily = FontFamily.Monospace),
        headlineLarge = Typography().headlineLarge.copy(fontFamily = FontFamily.Monospace),
        displayLarge = Typography().displayLarge.copy(fontFamily = FontFamily.Monospace),
    ),
)

fun oxygenThemeSpec(id: OxygenThemeId): OxygenThemeSpec = when (id) {
    OxygenThemeId.OXYGEN -> OxygenSpec
    OxygenThemeId.PAPER -> PaperSpec
    OxygenThemeId.TERMINAL -> TerminalSpec
}

@Composable
fun OxygenTheme(
    themeId: OxygenThemeId = OxygenThemeId.OXYGEN,
    content: @Composable () -> Unit,
) {
    val spec = oxygenThemeSpec(themeId)
    val palette = spec.palette
    val typography = spec.typography
    val homeDesign = OxygenHomeDesignRoles(
        pageMarginHorizontal = 18.dp,
        pageMarginVertical = 18.dp,
        pageGap = 10.dp,
        sectionGap = 16.dp,
        tileGap = 10.dp,
        cardPadding = 16.dp,
        compactCardPadding = 10.dp,
        homeCardCorner = 8.dp,
        weatherMarkGold = if (spec.dark) Color(0xFFFFD28A) else Color(0xFF8A5D18),
        weatherMarkQuiet = if (spec.dark) Color(0xFFE8F8FB) else Color(0xFF244954),
        ambientGlassSurface = palette.glass,
        strongGlassSurface = palette.glassStrong,
        outlineAccent = palette.outline,
        displayWeatherValue = typography.displayMedium.copy(fontWeight = FontWeight.Light),
        sectionHeading = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        supportingLabel = typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        compactWeatherValue = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
    val scheme = if (spec.dark) {
        darkColorScheme(
            primary = palette.chartAccent,
            secondary = palette.precipitation,
            background = palette.skyTop,
            surface = palette.glassStrong,
            surfaceVariant = palette.glass,
            outline = palette.outline,
            onPrimary = Color(0xFF062126),
            onSecondary = Color(0xFF072033),
            onBackground = Color(0xFFF2FAFC),
            onSurface = Color(0xFFF2FAFC),
            error = palette.warning,
        )
    } else {
        lightColorScheme(
            primary = palette.chartAccent,
            secondary = palette.precipitation,
            background = palette.skyTop,
            surface = palette.glassStrong,
            surfaceVariant = palette.glass,
            outline = palette.outline,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF2A2722),
            onSurface = Color(0xFF2A2722),
            error = palette.warning,
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalOxygenPalette provides palette,
        LocalOxygenHomeDesign provides homeDesign,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = spec.typography,
            content = content,
        )
    }
}

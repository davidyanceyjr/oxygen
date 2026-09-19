package com.oxygen.weather.app.ui.weather

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.oxygen.weather.app.ui.theme.LocalOxygenPalette
import com.oxygen.weather.core.model.WeatherCondition

internal enum class WeatherSceneProfile {
    CLEAR,
    CLOUDY,
    FOG,
    RAIN,
    RAIN_SHOWERS,
    FROZEN_PRECIPITATION,
    THUNDERSTORM,
    NEUTRAL,
}

internal fun weatherSceneProfile(condition: WeatherCondition): WeatherSceneProfile = when (condition) {
    WeatherCondition.CLEAR,
    WeatherCondition.MOSTLY_CLEAR,
    -> WeatherSceneProfile.CLEAR

    WeatherCondition.PARTLY_CLOUDY,
    WeatherCondition.CLOUDY,
    -> WeatherSceneProfile.CLOUDY

    WeatherCondition.FOG -> WeatherSceneProfile.FOG

    WeatherCondition.DRIZZLE,
    WeatherCondition.FREEZING_DRIZZLE,
    WeatherCondition.RAIN,
    WeatherCondition.FREEZING_RAIN,
    -> WeatherSceneProfile.RAIN

    WeatherCondition.RAIN_SHOWERS -> WeatherSceneProfile.RAIN_SHOWERS

    WeatherCondition.SNOW,
    WeatherCondition.SNOW_SHOWERS,
    WeatherCondition.SLEET,
    WeatherCondition.HAIL,
    -> WeatherSceneProfile.FROZEN_PRECIPITATION

    WeatherCondition.THUNDERSTORM,
    WeatherCondition.THUNDERSTORM_HAIL,
    -> WeatherSceneProfile.THUNDERSTORM

    WeatherCondition.UNKNOWN -> WeatherSceneProfile.NEUTRAL
}

@Composable
fun WeatherScene(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
) {
    val palette = LocalOxygenPalette.current
    val profile = weatherSceneProfile(condition)
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(palette.skyTop, palette.skyBottom),
                startY = 0f,
                endY = size.height,
            ),
        )

        when (profile) {
            WeatherSceneProfile.CLEAR -> drawClearAtmosphere(palette.atmosphericGlow)
            WeatherSceneProfile.CLOUDY -> drawCloudyAtmosphere()
            WeatherSceneProfile.FOG -> drawFogAtmosphere(palette.atmosphericGlow)
            WeatherSceneProfile.RAIN -> drawRainAtmosphere(palette.atmosphericGlow, showers = false)
            WeatherSceneProfile.RAIN_SHOWERS -> drawRainAtmosphere(palette.atmosphericGlow, showers = true)
            WeatherSceneProfile.FROZEN_PRECIPITATION -> drawFrozenAtmosphere(palette.atmosphericGlow)
            WeatherSceneProfile.THUNDERSTORM -> drawThunderstormAtmosphere(palette.atmosphericGlow)
            WeatherSceneProfile.NEUTRAL -> drawNeutralAtmosphere(palette.atmosphericGlow)
        }
    }
}

private fun DrawScope.drawClearAtmosphere(glow: Color) {
    drawCircle(
        color = glow.copy(alpha = 0.24f),
        radius = size.minDimension * 0.42f,
        center = Offset(size.width * 0.82f, size.height * 0.16f),
    )
    drawCircle(
        color = glow.copy(alpha = 0.12f),
        radius = size.minDimension * 0.21f,
        center = Offset(size.width * 0.82f, size.height * 0.16f),
    )
}

private fun DrawScope.drawCloudyAtmosphere() {
    drawCloud(
        center = Offset(size.width * 0.72f, size.height * 0.18f),
        scale = size.width / 420f,
        color = Color.White.copy(alpha = 0.10f),
    )
    drawCloud(
        center = Offset(size.width * 0.18f, size.height * 0.30f),
        scale = size.width / 520f,
        color = Color.White.copy(alpha = 0.07f),
    )
}

private fun DrawScope.drawFogAtmosphere(glow: Color) {
    val bandColor = glow.copy(alpha = 0.10f)
    listOf(0.20f, 0.35f, 0.50f).forEachIndexed { index, fraction ->
        drawRoundRect(
            color = bandColor.copy(alpha = 0.06f + index * 0.02f),
            topLeft = Offset(-size.width * 0.08f, size.height * fraction),
            size = Size(size.width * 1.16f, size.height * 0.09f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height * 0.04f),
        )
    }
}

private fun DrawScope.drawRainAtmosphere(glow: Color, showers: Boolean) {
    drawCloud(
        center = Offset(size.width * 0.68f, size.height * 0.16f),
        scale = size.width / 400f,
        color = Color.White.copy(alpha = 0.12f),
    )
    val dropColor = glow.copy(alpha = if (showers) 0.22f else 0.16f)
    val offset = if (showers) size.width * 0.05f else 0f
    repeat(if (showers) 8 else 6) { index ->
        val x = size.width * (0.16f + index * 0.11f) + offset
        val y = size.height * (0.27f + (index % 3) * 0.07f)
        drawLine(
            color = dropColor,
            start = Offset(x, y),
            end = Offset(x - size.width * 0.018f, y + size.height * 0.09f),
            strokeWidth = 1.5f,
        )
    }
}

private fun DrawScope.drawFrozenAtmosphere(glow: Color) {
    drawCloud(
        center = Offset(size.width * 0.72f, size.height * 0.17f),
        scale = size.width / 430f,
        color = Color.White.copy(alpha = 0.10f),
    )
    val flakeColor = glow.copy(alpha = 0.22f)
    repeat(7) { index ->
        val x = size.width * (0.14f + index * 0.12f)
        val y = size.height * (0.30f + (index % 2) * 0.08f)
        drawCircle(flakeColor, radius = 2.5f, center = Offset(x, y))
        drawLine(flakeColor, Offset(x - 5f, y), Offset(x + 5f, y), strokeWidth = 1f)
        drawLine(flakeColor, Offset(x, y - 5f), Offset(x, y + 5f), strokeWidth = 1f)
    }
}

private fun DrawScope.drawThunderstormAtmosphere(glow: Color) {
    drawCloud(
        center = Offset(size.width * 0.62f, size.height * 0.18f),
        scale = size.width / 360f,
        color = Color.White.copy(alpha = 0.14f),
    )
    val lightningColor = glow.copy(alpha = 0.28f)
    val centerX = size.width * 0.62f
    val top = size.height * 0.27f
    drawLine(lightningColor, Offset(centerX, top), Offset(centerX - 12f, top + 34f), strokeWidth = 2f)
    drawLine(lightningColor, Offset(centerX - 12f, top + 34f), Offset(centerX + 3f, top + 30f), strokeWidth = 2f)
    drawLine(lightningColor, Offset(centerX + 3f, top + 30f), Offset(centerX - 8f, top + 68f), strokeWidth = 2f)
}

private fun DrawScope.drawNeutralAtmosphere(glow: Color) {
    drawCircle(
        color = glow.copy(alpha = 0.08f),
        radius = size.minDimension * 0.28f,
        center = Offset(size.width * 0.82f, size.height * 0.18f),
        style = Stroke(width = 1.5f),
    )
}

private fun DrawScope.drawCloud(center: Offset, scale: Float, color: Color) {
    val s = scale.coerceAtLeast(0.65f)
    drawOval(
        color = color,
        topLeft = Offset(center.x - 92f * s, center.y - 24f * s),
        size = Size(184f * s, 62f * s),
    )
    drawCircle(color, 42f * s, Offset(center.x - 42f * s, center.y - 24f * s))
    drawCircle(color, 54f * s, Offset(center.x + 16f * s, center.y - 38f * s))
    drawCircle(color, 34f * s, Offset(center.x + 60f * s, center.y - 18f * s))
}

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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.oxygen.weather.app.ui.theme.LocalOxygenPalette
import com.oxygen.weather.core.model.WeatherCondition

@Composable
fun WeatherScene(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
) {
    val palette = LocalOxygenPalette.current
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(palette.skyTop, palette.skyBottom),
                startY = 0f,
                endY = size.height,
            )
        )

        drawCircle(
            color = palette.atmosphericGlow.copy(alpha = 0.22f),
            radius = size.minDimension * 0.42f,
            center = Offset(size.width * 0.86f, size.height * 0.08f),
        )

        if (condition != WeatherCondition.CLEAR) {
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
    }
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

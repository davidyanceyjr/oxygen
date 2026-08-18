package com.oxygen.weather.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oxygen.weather.app.ui.theme.LocalOxygenPalette
import com.oxygen.weather.core.model.WeatherCondition

@Composable
fun WeatherConditionMark(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
) {
    val palette = LocalOxygenPalette.current
    Canvas(modifier = modifier.size(56.dp)) {
        val sun = palette.atmosphericGlow.copy(alpha = 0.95f)
        drawCircle(sun, radius = size.minDimension * 0.22f, center = Offset(size.width * 0.68f, size.height * 0.34f))
        if (condition != WeatherCondition.CLEAR && condition != WeatherCondition.MOSTLY_CLEAR) {
            drawCircle(Color.White.copy(alpha = 0.76f), radius = size.minDimension * 0.20f, center = Offset(size.width * 0.38f, size.height * 0.56f))
            drawCircle(Color.White.copy(alpha = 0.70f), radius = size.minDimension * 0.24f, center = Offset(size.width * 0.55f, size.height * 0.51f))
            drawOval(
                color = Color.White.copy(alpha = 0.76f),
                topLeft = Offset(size.width * 0.20f, size.height * 0.50f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.60f, size.height * 0.27f),
            )
        }
    }
}

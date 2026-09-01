package com.oxygen.weather.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.oxygen.weather.app.ui.theme.LocalOxygenHomeDesign
import com.oxygen.weather.core.model.WeatherCondition
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WeatherConditionMark(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
) {
    val roles = LocalOxygenHomeDesign.current
    Canvas(modifier = modifier.size(56.dp)) {
        val gold = roles.weatherMarkGold
        val quiet = roles.weatherMarkQuiet.copy(alpha = 0.76f)
        val precipitation = roles.weatherMarkQuiet.copy(alpha = 0.56f)
        val stroke = size.minDimension * 0.045f
        val fineStroke = size.minDimension * 0.034f

        when (condition) {
            WeatherCondition.CLEAR -> drawSun(gold, stroke)
            WeatherCondition.MOSTLY_CLEAR -> {
                drawSun(gold, fineStroke, center = Offset(size.width * 0.38f, size.height * 0.38f), radius = 0.18f)
                drawCloud(gold, stroke, Offset(size.width * 0.56f, size.height * 0.61f), 0.88f)
            }
            WeatherCondition.PARTLY_CLOUDY -> {
                drawSun(gold, fineStroke, center = Offset(size.width * 0.34f, size.height * 0.36f), radius = 0.18f)
                drawCloud(gold, stroke, Offset(size.width * 0.55f, size.height * 0.60f), 1f)
            }
            WeatherCondition.CLOUDY -> drawCloud(gold, stroke, Offset(size.width * 0.52f, size.height * 0.58f), 1.08f)
            WeatherCondition.FOG -> drawFog(gold, stroke)
            WeatherCondition.DRIZZLE -> {
                drawCloud(gold, stroke, Offset(size.width * 0.52f, size.height * 0.47f), 1f)
                drawRain(precipitation, fineStroke, count = 3, short = true)
            }
            WeatherCondition.FREEZING_DRIZZLE -> {
                drawCloud(gold, stroke, Offset(size.width * 0.52f, size.height * 0.45f), 1f)
                drawRain(precipitation, fineStroke, count = 2, short = true)
                drawSnow(gold, fineStroke, Offset(size.width * 0.70f, size.height * 0.75f), 0.12f)
            }
            WeatherCondition.RAIN -> {
                drawCloud(gold, stroke, Offset(size.width * 0.52f, size.height * 0.45f), 1f)
                drawRain(gold, fineStroke, count = 4, short = false)
            }
            WeatherCondition.FREEZING_RAIN -> {
                drawCloud(gold, stroke, Offset(size.width * 0.52f, size.height * 0.45f), 1f)
                drawRain(gold, fineStroke, count = 3, short = false)
                drawSnow(quiet, fineStroke, Offset(size.width * 0.76f, size.height * 0.75f), 0.12f)
            }
            WeatherCondition.RAIN_SHOWERS -> {
                drawCloud(gold, stroke, Offset(size.width * 0.52f, size.height * 0.45f), 1f)
                drawRain(gold, fineStroke, count = 3, short = false)
                drawLine(
                    color = quiet,
                    start = Offset(size.width * 0.66f, size.height * 0.70f),
                    end = Offset(size.width * 0.77f, size.height * 0.61f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
            WeatherCondition.SNOW -> {
                drawSnow(gold, stroke, Offset(size.width * 0.50f, size.height * 0.50f), 0.30f)
            }
            WeatherCondition.SNOW_SHOWERS -> {
                drawCloud(gold, stroke, Offset(size.width * 0.52f, size.height * 0.42f), 1f)
                drawSnow(gold, fineStroke, Offset(size.width * 0.37f, size.height * 0.74f), 0.11f)
                drawSnow(gold, fineStroke, Offset(size.width * 0.62f, size.height * 0.78f), 0.11f)
            }
            WeatherCondition.SLEET -> {
                drawCloud(gold, stroke, Offset(size.width * 0.52f, size.height * 0.42f), 1f)
                drawSnow(gold, fineStroke, Offset(size.width * 0.34f, size.height * 0.74f), 0.10f)
                drawRain(gold, fineStroke, count = 2, short = true, startX = 0.52f)
            }
            WeatherCondition.HAIL -> {
                drawCloud(gold, stroke, Offset(size.width * 0.52f, size.height * 0.42f), 1f)
                drawHail(gold, fineStroke)
            }
            WeatherCondition.THUNDERSTORM -> {
                drawCloud(gold, stroke, Offset(size.width * 0.52f, size.height * 0.38f), 1f)
                drawLightning(gold, stroke)
                drawRain(quiet, fineStroke, count = 2, short = true)
            }
            WeatherCondition.THUNDERSTORM_HAIL -> {
                drawCloud(gold, stroke, Offset(size.width * 0.52f, size.height * 0.38f), 1f)
                drawLightning(gold, stroke)
                drawHail(quiet, fineStroke)
            }
            WeatherCondition.UNKNOWN -> drawUnknown(gold, stroke)
        }
    }
}

private fun DrawScope.drawSun(
    color: androidx.compose.ui.graphics.Color,
    strokeWidth: Float,
    center: Offset = Offset(size.width * 0.50f, size.height * 0.50f),
    radius: Float = 0.22f,
) {
    val r = size.minDimension * radius
    drawCircle(color = color, radius = r, center = center, style = Stroke(strokeWidth, cap = StrokeCap.Round))
    repeat(8) { index ->
        val angle = Math.toRadians((index * 45).toDouble())
        val start = Offset(
            x = center.x + cos(angle).toFloat() * r * 1.45f,
            y = center.y + sin(angle).toFloat() * r * 1.45f,
        )
        val end = Offset(
            x = center.x + cos(angle).toFloat() * r * 1.95f,
            y = center.y + sin(angle).toFloat() * r * 1.95f,
        )
        drawLine(color, start, end, strokeWidth, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawCloud(
    color: androidx.compose.ui.graphics.Color,
    strokeWidth: Float,
    center: Offset,
    scale: Float,
) {
    val w = size.minDimension * 0.66f * scale
    val h = size.minDimension * 0.34f * scale
    val left = center.x - w / 2f
    val top = center.y - h / 2f
    val path = Path().apply {
        moveTo(left + w * 0.14f, top + h * 0.78f)
        cubicTo(left + w * 0.08f, top + h * 0.50f, left + w * 0.24f, top + h * 0.36f, left + w * 0.38f, top + h * 0.45f)
        cubicTo(left + w * 0.43f, top + h * 0.18f, left + w * 0.74f, top + h * 0.10f, left + w * 0.82f, top + h * 0.43f)
        cubicTo(left + w * 0.98f, top + h * 0.45f, left + w * 1.03f, top + h * 0.78f, left + w * 0.82f, top + h * 0.78f)
        close()
    }
    drawPath(path, color = color, style = Stroke(strokeWidth, cap = StrokeCap.Round))
}

private fun DrawScope.drawFog(color: androidx.compose.ui.graphics.Color, strokeWidth: Float) {
    listOf(0.34f, 0.50f, 0.66f).forEach { y ->
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * y),
            end = Offset(size.width * 0.82f, size.height * y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawRain(
    color: androidx.compose.ui.graphics.Color,
    strokeWidth: Float,
    count: Int,
    short: Boolean,
    startX: Float = 0.30f,
) {
    repeat(count) { index ->
        val x = size.width * (startX + index * 0.14f)
        val top = size.height * 0.68f
        val bottom = size.height * if (short) 0.78f else 0.86f
        drawLine(color, Offset(x, top), Offset(x - size.width * 0.05f, bottom), strokeWidth, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawSnow(
    color: androidx.compose.ui.graphics.Color,
    strokeWidth: Float,
    center: Offset,
    radius: Float,
) {
    val r = size.minDimension * radius
    repeat(3) { index ->
        val angle = Math.toRadians((index * 60).toDouble())
        val dx = cos(angle).toFloat() * r
        val dy = sin(angle).toFloat() * r
        drawLine(color, Offset(center.x - dx, center.y - dy), Offset(center.x + dx, center.y + dy), strokeWidth, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawHail(color: androidx.compose.ui.graphics.Color, strokeWidth: Float) {
    listOf(0.34f to 0.72f, 0.50f to 0.82f, 0.66f to 0.72f).forEach { (x, y) ->
        drawCircle(
            color = color,
            radius = size.minDimension * 0.055f,
            center = Offset(size.width * x, size.height * y),
            style = Stroke(strokeWidth * 0.8f),
        )
    }
}

private fun DrawScope.drawLightning(color: androidx.compose.ui.graphics.Color, strokeWidth: Float) {
    val path = Path().apply {
        moveTo(size.width * 0.55f, size.height * 0.54f)
        lineTo(size.width * 0.43f, size.height * 0.78f)
        lineTo(size.width * 0.56f, size.height * 0.76f)
        lineTo(size.width * 0.48f, size.height * 0.94f)
    }
    drawPath(path, color = color, style = Stroke(strokeWidth, cap = StrokeCap.Round))
}

private fun DrawScope.drawUnknown(color: androidx.compose.ui.graphics.Color, strokeWidth: Float) {
    drawArc(
        color = color,
        startAngle = 210f,
        sweepAngle = 250f,
        useCenter = false,
        topLeft = Offset(size.width * 0.28f, size.height * 0.16f),
        size = Size(size.width * 0.44f, size.height * 0.44f),
        style = Stroke(strokeWidth, cap = StrokeCap.Round),
    )
    drawLine(
        color = color,
        start = Offset(size.width * 0.50f, size.height * 0.58f),
        end = Offset(size.width * 0.50f, size.height * 0.68f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
    drawCircle(color, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.50f, size.height * 0.82f))
}

package com.oxygen.weather.app.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oxygen.weather.app.ui.components.GlassPanel
import com.oxygen.weather.app.ui.components.WeatherConditionMark
import com.oxygen.weather.app.ui.theme.LocalOxygenPalette
import com.oxygen.weather.app.ui.theme.OxygenThemeId
import com.oxygen.weather.app.ui.weather.WeatherScene
import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    weather: WeatherBundle,
    selectedTheme: OxygenThemeId,
    onThemeSelected: (OxygenThemeId) -> Unit,
) {
    val condition = weather.current?.condition ?: WeatherCondition.UNKNOWN

    Box(Modifier.fillMaxSize()) {
        WeatherScene(condition = condition)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Header(weather)
            ThemeChooser(selectedTheme, onThemeSelected)
            weather.alerts.firstOrNull()?.let { alert ->
                AlertPreview(alert.event, alert.headline ?: "Weather alert", alert.issuer)
            }
            weather.current?.let { CurrentHero(it) }
            HourlyStrip(weather.hourly, weather.location.zoneId)
            DailyPanel(weather.daily)
            weather.current?.let { MetricsGrid(it) }
            SourceFooter(weather)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Header(weather: WeatherBundle) {
    Column {
        Text(
            text = "OXYGEN",
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = weather.location.displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Beautiful by default · decoration-independent weather",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun ThemeChooser(selected: OxygenThemeId, onSelected: (OxygenThemeId) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OxygenThemeId.entries.forEach { theme ->
            OutlinedButton(onClick = { onSelected(theme) }) {
                Text(if (theme == selected) "${theme.displayName} ✓" else theme.displayName)
            }
        }
    }
}

@Composable
private fun AlertPreview(event: String, headline: String, issuer: String) {
    val palette = LocalOxygenPalette.current
    Surface(
        color = palette.warning.copy(alpha = 0.13f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.warning.copy(alpha = 0.55f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(event.uppercase(), color = palette.warning, fontWeight = FontWeight.Bold)
            Text(headline, style = MaterialTheme.typography.bodyMedium)
            Text(issuer, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
        }
    }
}

@Composable
private fun CurrentHero(current: CurrentConditions) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = current.temperatureC?.let { "${cToF(it)}°" } ?: "—",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 76.sp,
                )
                Text(conditionName(current.condition), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Feels like ${current.apparentTemperatureC?.let { "${cToF(it)}°" } ?: "unavailable"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
            WeatherConditionMark(current.condition)
        }
    }
}

@Composable
private fun HourlyStrip(hourly: List<HourlyForecast>, zoneId: ZoneId) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Next hours", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                hourly.forEach { hour ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            DateTimeFormatter.ofPattern("ha").format(hour.time.atZone(zoneId)).lowercase(),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(hour.temperatureC?.let { "${cToF(it)}°" } ?: "—", style = MaterialTheme.typography.titleMedium)
                        Text(
                            hour.precipitationProbabilityPercent?.let { "$it%" } ?: "—",
                            color = LocalOxygenPalette.current.precipitation,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyPanel(daily: List<DailyForecast>) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("7-day outlook", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            daily.forEachIndexed { index, day ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (index == 0) "Today" else LocalDate.ofEpochDay(day.dateEpochDay).dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.width(62.dp),
                    )
                    Text(
                        text = day.precipitationProbabilityPercent?.let { "$it%" } ?: "—",
                        modifier = Modifier.width(48.dp),
                        color = LocalOxygenPalette.current.precipitation,
                    )
                    Text(conditionShort(day.condition), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f))
                    Text(day.lowC?.let { "${cToF(it)}°" } ?: "—", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
                    Text(day.highC?.let { "  ${cToF(it)}°" } ?: "  —", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MetricsGrid(current: CurrentConditions) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric("Wind", current.wind?.speedMetersPerSecond?.let { "SW ${msToMph(it)} mph" } ?: "—", Modifier.weight(1f))
            Metric("Humidity", current.humidityPercent?.let { "$it%" } ?: "—", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric("Pressure", current.pressureHpa?.let { "${it.roundToInt()} hPa" } ?: "—", Modifier.weight(1f))
            Metric("Dew point", current.dewPointC?.let { "${cToF(it)}°" } ?: "—", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric("Visibility", current.visibilityMeters?.let { "${(it / 1609.344).roundToInt()} mi" } ?: "—", Modifier.weight(1f))
            Metric("Cloud cover", current.cloudCoverPercent?.let { "$it%" } ?: "—", Modifier.weight(1f))
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    GlassPanel(modifier = modifier) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SourceFooter(weather: WeatherBundle) {
    Text(
        text = "Scaffold data: ${weather.current?.provenance?.sourceName ?: "none"}. Replace with provider adapters before release.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.60f),
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

private fun cToF(c: Double): Int = (c * 9.0 / 5.0 + 32.0).roundToInt()
private fun msToMph(ms: Double): Int = (ms * 2.236936).roundToInt()

private fun conditionName(condition: WeatherCondition): String = when (condition) {
    WeatherCondition.CLEAR -> "Clear"
    WeatherCondition.MOSTLY_CLEAR -> "Mostly clear"
    WeatherCondition.PARTLY_CLOUDY -> "Partly cloudy"
    WeatherCondition.CLOUDY -> "Cloudy"
    WeatherCondition.FOG -> "Fog"
    WeatherCondition.DRIZZLE -> "Drizzle"
    WeatherCondition.FREEZING_DRIZZLE -> "Freezing drizzle"
    WeatherCondition.RAIN -> "Rain"
    WeatherCondition.FREEZING_RAIN -> "Freezing rain"
    WeatherCondition.RAIN_SHOWERS -> "Rain showers"
    WeatherCondition.SNOW -> "Snow"
    WeatherCondition.SNOW_SHOWERS -> "Snow showers"
    WeatherCondition.SLEET -> "Sleet"
    WeatherCondition.HAIL -> "Hail"
    WeatherCondition.THUNDERSTORM -> "Thunderstorm"
    WeatherCondition.THUNDERSTORM_HAIL -> "Thunderstorm with hail"
    WeatherCondition.UNKNOWN -> "Unknown"
}

private fun conditionShort(condition: WeatherCondition): String = when (condition) {
    WeatherCondition.PARTLY_CLOUDY -> "Partly cloudy"
    WeatherCondition.MOSTLY_CLEAR -> "Mostly clear"
    WeatherCondition.RAIN_SHOWERS -> "Showers"
    else -> conditionName(condition)
}

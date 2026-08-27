package com.oxygen.weather.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oxygen.weather.app.HomeMetricPresentation
import com.oxygen.weather.app.HomeForecastFreshness
import com.oxygen.weather.app.HomeForecastPresentationState

@Composable
fun HomeLoadingScreen(
    state: HomeForecastPresentationState,
    onRetry: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "OXYGEN",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = state.title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = state.subtitle,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            OutlinedButton(
                onClick = onOpenAbout,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Settings / About")
            }
            when (state) {
                is HomeForecastPresentationState.Loading -> LoadingContent(state)
                is HomeForecastPresentationState.NoCacheError -> ErrorContent(
                    state = state,
                    onRetry = onRetry,
                )
                is HomeForecastPresentationState.ForecastReady -> ReadyContent(
                    state = state,
                    onRetry = onRetry,
                )
            }
            ProviderDisclosure(state)
        }
    }
}

@Composable
private fun LoadingContent(state: HomeForecastPresentationState.Loading) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(
            text = state.statusText,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
        )
    }
}

@Composable
private fun ErrorContent(
    state: HomeForecastPresentationState.NoCacheError,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = state.message.text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
        )
        if (state.canRetry) {
            Button(onClick = onRetry) {
                Text(text = state.retryLabel)
            }
        }
    }
}

@Composable
private fun ReadyContent(
    state: HomeForecastPresentationState.ForecastReady,
    onRetry: () -> Unit,
) {
    val dashboard = state.dashboard
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        state.refreshInProgressText?.let { refreshText ->
            DashboardCard {
                Text(refreshText, style = MaterialTheme.typography.bodyMedium)
            }
        }
        when (val freshness = state.freshness) {
            HomeForecastFreshness.Fresh -> Unit
            is HomeForecastFreshness.StaleAfterFailedRefresh -> {
                DashboardCard {
                    Text("Cached forecast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(freshness.statusText, style = MaterialTheme.typography.bodyMedium)
                    Text("Refresh failed: ${freshness.refreshFailureMessage.text}", style = MaterialTheme.typography.bodySmall)
                    if (state.canRetry) {
                        Button(onClick = onRetry) {
                            Text(text = state.retryLabel)
                        }
                    }
                }
            }
        }

        dashboard.alerts.forEach { alert ->
            DashboardCard {
                Text(alert.event, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(alert.headline, style = MaterialTheme.typography.bodyMedium)
                Text("${alert.severity} | ${alert.issuer}", style = MaterialTheme.typography.bodySmall)
                alert.effective?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                alert.expires?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }

        DashboardCard {
            Text("Current conditions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (dashboard.current == null) {
                Text(
                    text = dashboard.currentUnavailableText ?: dashboard.returnedDataUnavailableText.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Text(
                    text = dashboard.current.temperature,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Light,
                )
                Text(dashboard.current.condition, style = MaterialTheme.typography.titleLarge)
                Text(dashboard.current.apparentTemperature, style = MaterialTheme.typography.bodyMedium)
                Text("${dashboard.current.updatedTime} | ${dashboard.current.dataTypeLabel}", style = MaterialTheme.typography.bodySmall)
            }
        }

        dashboard.precipitationSummary?.let {
            DashboardCard {
                Text("Near-term precipitation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (dashboard.hourly.isNotEmpty()) {
            DashboardCard {
                Text("Hourly forecast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                dashboard.hourly.forEach { hour ->
                    ForecastRow(
                        primary = hour.time,
                        secondary = hour.condition,
                        value = listOfNotNull(hour.temperature, hour.precipitationProbability).joinToString(" | "),
                    )
                }
            }
        }

        if (dashboard.daily.isNotEmpty()) {
            DashboardCard {
                Text("Daily forecast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                dashboard.daily.forEach { day ->
                    ForecastRow(
                        primary = day.date,
                        secondary = listOfNotNull(day.condition, day.precipitationProbability).joinToString(" | "),
                        value = "${day.low} | ${day.high}",
                    )
                    if (day.sunrise != null || day.sunset != null) {
                        Text(
                            text = "Sunrise ${day.sunrise ?: "unavailable"} | Sunset ${day.sunset ?: "unavailable"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                        )
                    }
                }
            }
        }

        if (dashboard.metrics.isNotEmpty()) {
            MetricGrid(dashboard.metrics)
        }

        dashboard.sun?.let {
            DashboardCard {
                Text("Sun", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Sunrise ${it.sunrise}", style = MaterialTheme.typography.bodyMedium)
                Text("Sunset ${it.sunset}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        DashboardCard {
            Text("Source", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(dashboard.source.sourceName, style = MaterialTheme.typography.bodyMedium)
            Text(dashboard.source.dataType, style = MaterialTheme.typography.bodyMedium)
            Text(dashboard.source.fetchedAt, style = MaterialTheme.typography.bodySmall)
            dashboard.source.issuedAt?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            dashboard.source.license?.let { Text("License $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun DashboardCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun ForecastRow(
    primary: String,
    secondary: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                secondary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
            )
        }
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MetricGrid(metrics: List<HomeMetricPresentation>) {
    DashboardCard {
        Text("Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        metrics.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { metric ->
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = metric.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                        Text(metric.value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ProviderDisclosure(state: HomeForecastPresentationState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = state.forecastDisclosure,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )
        Text(
            text = state.forecastPrivacyNote,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
        )
    }
}

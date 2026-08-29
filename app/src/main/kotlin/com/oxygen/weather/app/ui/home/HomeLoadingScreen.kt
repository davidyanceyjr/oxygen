package com.oxygen.weather.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.oxygen.weather.app.HomeMetricPresentation
import com.oxygen.weather.app.HomeForecastFreshness
import com.oxygen.weather.app.HomeForecastPresentationState
import com.oxygen.weather.app.ui.components.WeatherConditionMark

@Composable
fun HomeLoadingScreen(
    state: HomeForecastPresentationState,
    onRetry: () -> Unit = {},
    onRefresh: () -> Unit = {},
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
            if (state !is HomeForecastPresentationState.ForecastReady) {
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
            }
            when (state) {
                is HomeForecastPresentationState.Loading -> LoadingContent(state)
                is HomeForecastPresentationState.NoCacheError -> ErrorContent(
                    state = state,
                    onRetry = onRetry,
                )
                is HomeForecastPresentationState.ForecastReady -> ReadyContent(
                    state = state,
                    onRefresh = onRefresh,
                    onOpenAbout = onOpenAbout,
                )
            }
            if (state !is HomeForecastPresentationState.ForecastReady) {
                ProviderDisclosure(state)
            }
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
    onRefresh: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val dashboard = state.dashboard
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home-dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DashboardSection(tag = "home-section-location") {
            Text(
                text = dashboard.locationName,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = dashboard.locationSubtitle,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            OutlinedButton(
                onClick = onOpenAbout,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("home-about-entry"),
            ) {
                Text("Settings / About")
            }
            Button(
                onClick = onRefresh,
                enabled = state.canRefresh && !state.isRefreshInProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("home-refresh"),
            ) {
                Text(text = state.refreshLabel)
            }
        }

        state.refreshInProgressText?.let { refreshText ->
            DashboardCard(tag = "home-refreshing") {
                Text(refreshText, style = MaterialTheme.typography.bodyMedium)
            }
        }
        when (val freshness = state.freshness) {
            HomeForecastFreshness.Fresh -> Unit
            is HomeForecastFreshness.RestoredFromCache -> {
                DashboardCard(tag = "home-section-stale") {
                    Text("Cached forecast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(freshness.statusText, style = MaterialTheme.typography.bodyMedium)
                }
            }
            is HomeForecastFreshness.StaleAfterFailedRefresh -> {
                DashboardCard(tag = "home-section-stale") {
                    Text("Cached forecast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(freshness.statusText, style = MaterialTheme.typography.bodyMedium)
                    Text("Refresh failed: ${freshness.refreshFailureMessage.text}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        dashboard.alerts.forEach { alert ->
            DashboardCard(tag = "home-section-alert") {
                Text(alert.event, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(alert.headline, style = MaterialTheme.typography.bodyMedium)
                Text("${alert.severity} | ${alert.issuer}", style = MaterialTheme.typography.bodySmall)
                alert.effective?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                alert.expires?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }

        DashboardCard(tag = "home-section-current") {
            if (dashboard.current == null) {
                Text("Current conditions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = dashboard.currentUnavailableText ?: dashboard.returnedDataUnavailableText.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .semantics {
                                contentDescription = dashboard.current.condition
                            },
                    ) {
                        WeatherConditionMark(
                            condition = dashboard.current.conditionIdentity,
                            modifier = Modifier.size(88.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Current conditions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(dashboard.current.condition, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Text(
                    text = dashboard.current.temperature,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Light,
                )
                Text(dashboard.current.apparentTemperature, style = MaterialTheme.typography.bodyMedium)
                val range = listOfNotNull(
                    dashboard.current.highTemperature,
                    dashboard.current.lowTemperature,
                ).joinToString("   ")
                if (range.isNotEmpty()) {
                    Text(range, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Text("${dashboard.current.updatedTime} | ${dashboard.current.dataTypeLabel}", style = MaterialTheme.typography.bodySmall)
                Text("${dashboard.source.sourceName} | ${dashboard.source.fetchedAt}", style = MaterialTheme.typography.bodySmall)
            }
        }

        dashboard.precipitationSummary?.let {
            DashboardCard(tag = "home-section-precipitation") {
                Text("Near-term precipitation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (dashboard.hourly.isNotEmpty()) {
            DashboardCard(tag = "home-section-hourly") {
                Text("Hourly forecast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home-hourly-row"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(dashboard.hourly) { hour ->
                        HourlyTile(
                            time = hour.time,
                            condition = hour.condition,
                            temperature = hour.temperature,
                            precipitation = hour.precipitationProbability,
                        )
                    }
                }
            }
        }

        if (dashboard.daily.isNotEmpty()) {
            DashboardCard(tag = "home-section-daily") {
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
            DashboardCard(tag = "home-section-sun") {
                Text("Sun", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Sunrise ${it.sunrise}", style = MaterialTheme.typography.bodyMedium)
                Text("Sunset ${it.sunset}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        DashboardCard(tag = "home-section-source") {
            Text("Source", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(dashboard.source.sourceName, style = MaterialTheme.typography.bodyMedium)
            Text(dashboard.source.dataType, style = MaterialTheme.typography.bodyMedium)
            Text(dashboard.source.fetchedAt, style = MaterialTheme.typography.bodySmall)
            dashboard.source.issuedAt?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            dashboard.source.license?.let { Text("License $it", style = MaterialTheme.typography.bodySmall) }
        }
        ProviderDisclosure(
            state = state,
            modifier = Modifier.testTag("home-section-provenance-footer"),
        )
    }
}

@Composable
private fun DashboardSection(
    tag: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
private fun DashboardCard(
    tag: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)),
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
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
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
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun HourlyTile(
    time: String,
    condition: String,
    temperature: String,
    precipitation: String?,
) {
    Column(
        modifier = Modifier
            .width(108.dp)
            .heightIn(min = 128.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = condition,
            style = MaterialTheme.typography.bodySmall,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(temperature, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(precipitation ?: "Precip unavailable", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MetricGrid(metrics: List<HomeMetricPresentation>) {
    DashboardCard(tag = "home-section-metrics") {
        Text("Metrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        metrics.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { metric ->
                    Column(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 64.dp),
                    ) {
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
private fun ProviderDisclosure(
    state: HomeForecastPresentationState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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

package com.oxygen.weather.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import com.oxygen.weather.app.HomeHourlyPresentation
import com.oxygen.weather.app.HomeMetricPresentation
import com.oxygen.weather.app.HomeForecastFreshness
import com.oxygen.weather.app.HomeForecastPresentationState
import com.oxygen.weather.app.ui.components.WeatherConditionMark
import kotlinx.coroutines.launch

@Composable
fun HomeLoadingScreen(
    state: HomeForecastPresentationState,
    onRetry: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
) {
    Surface(Modifier.fillMaxSize()) {
        if (state is HomeForecastPresentationState.ForecastReady) {
            ReadyContent(
                state = state,
                onRefresh = onRefresh,
                onOpenAbout = onOpenAbout,
            )
            return@Surface
        }
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
                is HomeForecastPresentationState.ForecastReady -> Unit
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReadyContent(
    state: HomeForecastPresentationState.ForecastReady,
    onRefresh: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val dashboard = state.dashboard
    val pages = HomePage.entries
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val currentPage = pages[pagerState.currentPage]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 18.dp, vertical = 18.dp)
            .testTag("home-dashboard"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ReadyHeader(
            currentPage = currentPage,
            pageIndex = pagerState.currentPage,
            pageCount = pages.size,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home-page-selector"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            pages.forEachIndexed { index, page ->
                FilterChip(
                    selected = index == pagerState.currentPage,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    label = { Text(page.title) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag(page.tabTag)
                        .semantics {
                            contentDescription = "${page.title} page"
                        },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                enabled = pagerState.currentPage > 0,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("home-page-previous"),
            ) {
                Text("Previous")
            }
            Button(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                enabled = pagerState.currentPage < pages.lastIndex,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("home-page-next"),
            ) {
                Text("Next")
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("home-page-container")
                .semantics {
                    contentDescription = "${currentPage.title}, Page ${pagerState.currentPage + 1} of ${pages.size}"
                },
        ) { pageIndex ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
                    .testTag(pages[pageIndex].pageTag),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (pages[pageIndex]) {
                    HomePage.Now -> NowPage(
                        state = state,
                        onRefresh = onRefresh,
                        onOpenAbout = onOpenAbout,
                    )
                    HomePage.Hourly -> HourlyPage(state)
                    HomePage.Daily -> DailyPage(state)
                    HomePage.Details -> DetailsPage(state)
                }
            }
        }
    }
}

@Composable
private fun ReadyHeader(
    currentPage: HomePage,
    pageIndex: Int,
    pageCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "OXYGEN",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = currentPage.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home-page-title")
                    .semantics {
                        contentDescription = "${currentPage.title}, Page ${pageIndex + 1} of $pageCount"
                    },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = "Page ${pageIndex + 1} of $pageCount",
            modifier = Modifier
                .widthIn(min = 72.dp)
                .testTag("home-page-position"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun NowPage(
    state: HomeForecastPresentationState.ForecastReady,
    onRefresh: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val dashboard = state.dashboard

    DashboardSection(tag = "home-section-location") {
        Text(
            text = dashboard.locationName,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = dashboard.locationSubtitle,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onOpenAbout,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("home-about-entry"),
            ) {
                Text("Settings / About")
            }
            Button(
                onClick = onRefresh,
                enabled = state.canRefresh && !state.isRefreshInProgress,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("home-refresh"),
            ) {
                Text(text = state.refreshLabel)
            }
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
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .semantics {
                            contentDescription = dashboard.current.condition
                        },
                ) {
                    WeatherConditionMark(
                        condition = dashboard.current.conditionIdentity,
                        modifier = Modifier.size(112.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = dashboard.current.condition,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = dashboard.current.temperature,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Light,
                    )
                }
            }
            val range = listOfNotNull(
                dashboard.current.highTemperature,
                dashboard.current.lowTemperature,
            ).joinToString("   ").ifEmpty { null }
            Text(
                text = dashboard.current.apparentTemperature,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            NowContextGrid(
                items = listOfNotNull(
                    range?.let { "Today" to it },
                    dashboard.metrics.firstOrNull { it.label == "Humidity" }?.let { it.label to it.value },
                    dashboard.metrics.firstOrNull { it.label == "Wind" }?.let { it.label to it.value },
                ),
            )
            Text("${dashboard.current.updatedTime} | ${dashboard.current.dataTypeLabel}", style = MaterialTheme.typography.bodySmall)
            Text("${dashboard.source.sourceName} | ${dashboard.source.fetchedAt}", style = MaterialTheme.typography.bodySmall)
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

    dashboard.precipitationSummary?.let {
        DashboardCard(tag = "home-section-precipitation") {
            Text("Near-term precipitation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NowContextGrid(items: List<Pair<String, String>>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 54.dp),
                    ) {
                        Text(
                            text = item.first,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                        Text(
                            text = item.second,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
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
private fun HourlyPage(state: HomeForecastPresentationState.ForecastReady) {
    val dashboard = state.dashboard

    if (dashboard.hourly.isNotEmpty()) {
        DashboardSection(tag = "home-section-hourly") {
            Text("Next hours", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home-hourly-grid"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                dashboard.hourly.take(6).chunked(2).forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        row.forEachIndexed { columnIndex, hour ->
                            HourlyTile(
                                hour = hour,
                                index = rowIndex * 2 + columnIndex,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    } else {
        UnavailablePageCard("Hourly forecast", dashboard.returnedDataUnavailableText)
    }
}

@Composable
private fun DailyPage(state: HomeForecastPresentationState.ForecastReady) {
    val dashboard = state.dashboard

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
    } else {
        UnavailablePageCard("Daily forecast", dashboard.returnedDataUnavailableText)
    }
}

@Composable
private fun DetailsPage(state: HomeForecastPresentationState.ForecastReady) {
    val dashboard = state.dashboard

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

@Composable
private fun UnavailablePageCard(title: String, message: String?) {
    DashboardCard(tag = "home-section-unavailable") {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            text = message ?: "This forecast information is unavailable from the selected source.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private enum class HomePage(
    val title: String,
    val tabTag: String,
    val pageTag: String,
) {
    Now("Now", "home-page-tab-now", "home-page-now"),
    Hourly("Hourly", "home-page-tab-hourly", "home-page-hourly"),
    Daily("Daily", "home-page-tab-daily", "home-page-daily"),
    Details("Details", "home-page-tab-details", "home-page-details"),
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
    hour: HomeHourlyPresentation,
    index: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .heightIn(min = 116.dp)
            .testTag("home-hourly-entry-$index")
            .semantics {
                contentDescription = listOf(
                    hour.time,
                    hour.condition,
                    hour.temperature,
                    hour.precipitationProbability ?: "Precipitation unavailable",
                ).joinToString(", ")
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .semantics {
                            contentDescription = hour.condition
                        },
                ) {
                    WeatherConditionMark(
                        condition = hour.conditionIdentity,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = hour.time,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = hour.condition,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = hour.temperature,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                text = hour.precipitationProbability?.let { "Precip $it" } ?: "Precipitation unavailable",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
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

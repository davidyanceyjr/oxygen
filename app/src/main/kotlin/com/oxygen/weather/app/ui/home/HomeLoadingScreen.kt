package com.oxygen.weather.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.oxygen.weather.app.HomeDailyPresentation
import com.oxygen.weather.app.HomeAlertSummaryPresentation
import com.oxygen.weather.app.HomeHourlyPresentation
import com.oxygen.weather.app.HomeMetricIdentity
import com.oxygen.weather.app.HomeMetricPresentation
import com.oxygen.weather.app.HomeForecastFreshness
import com.oxygen.weather.app.HomeForecastPresentationState
import com.oxygen.weather.app.HomeSourcePresentation
import com.oxygen.weather.app.HomeSunPresentation
import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.app.ui.theme.LayoutPreset
import com.oxygen.weather.app.ui.theme.LocalOxygenHomeDesign
import com.oxygen.weather.app.ui.theme.OxygenAppearance
import com.oxygen.weather.app.ui.components.WeatherConditionMark
import com.oxygen.weather.app.ui.weather.WeatherScene
import com.oxygen.weather.core.model.WeatherCondition
import kotlinx.coroutines.launch

@Composable
fun HomeLoadingScreen(
    state: HomeForecastPresentationState,
    appearance: OxygenAppearance = OxygenAppearance(),
    animationsEnabled: Boolean = true,
    onRetry: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onChangeLocation: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onAlertDetailsRequested: () -> Unit = {},
) {
    val baseRoles = LocalOxygenHomeDesign.current
    val roles = if (appearance.effects == EffectsLevel.OFF) {
        baseRoles.copy(
            ambientGlassSurface = MaterialTheme.colorScheme.surface,
            strongGlassSurface = MaterialTheme.colorScheme.surface,
            outlineAccent = MaterialTheme.colorScheme.outline,
            outlineStrong = MaterialTheme.colorScheme.outline,
            outlineQuiet = if (appearance.contrast == com.oxygen.weather.app.ui.theme.ContrastLevel.HIGH) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.46f)
            },
            normalContent = MaterialTheme.colorScheme.onSurface,
        )
    } else {
        baseRoles
    }
    Surface(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalOxygenHomeDesign provides roles) {
            if (state is HomeForecastPresentationState.ForecastReady) {
                ReadyContent(
                    state = state,
                    appearance = appearance,
                    animationsEnabled = animationsEnabled,
                    onRefresh = onRefresh,
                    onChangeLocation = onChangeLocation,
                    onOpenSettings = onOpenSettings,
                    onAlertDetailsRequested = onAlertDetailsRequested,
                )
                return@CompositionLocalProvider
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = roles.pageMarginHorizontal, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(roles.sectionGap),
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
                        color = homeSupportingContent(0.72f),
                    )
                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                    ) {
                        Text("Settings")
                    }
                    OutlinedButton(
                        onClick = onChangeLocation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("home-change-location"),
                    ) {
                        Text("Change location")
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
            color = homeSupportingContent(0.78f),
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
            color = homeSupportingContent(0.82f),
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
    appearance: OxygenAppearance,
    animationsEnabled: Boolean,
    onRefresh: () -> Unit,
    onChangeLocation: () -> Unit,
    onOpenSettings: () -> Unit,
    onAlertDetailsRequested: () -> Unit,
) {
    val roles = LocalOxygenHomeDesign.current
    val dashboard = state.dashboard
    val pages = when (appearance.layout) {
        LayoutPreset.SIMPLE -> listOf(HomePage.Now, HomePage.Forecast)
        LayoutPreset.STANDARD,
        LayoutPreset.DETAILED,
        LayoutPreset.METEOROLOGIST,
        -> listOf(HomePage.Now, HomePage.Hourly, HomePage.Daily, HomePage.Details)
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    var simpleForecast by remember(appearance.layout) { mutableStateOf(SimpleForecastChoice.Hourly) }
    val currentPageIndex = pagerState.currentPage.coerceIn(0, pages.lastIndex)
    val currentPage = pages[currentPageIndex]

    LaunchedEffect(appearance.layout) {
        simpleForecast = SimpleForecastChoice.Hourly
        pagerState.scrollToPage(0)
    }

    Box(Modifier.fillMaxSize()) {
        if (appearance.effects != EffectsLevel.OFF) {
            WeatherScene(
                condition = dashboard.current?.conditionIdentity ?: WeatherCondition.UNKNOWN,
                modifier = Modifier.testTag("home-weather-scene"),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = roles.pageMarginHorizontal, vertical = roles.pageMarginVertical)
                .testTag("home-dashboard"),
            verticalArrangement = Arrangement.spacedBy(roles.pageGap),
        ) {
            ReadyHeader(
                currentPage = currentPage,
                pageIndex = currentPageIndex,
                pageCount = pages.size,
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("home-page-container")
                    .semantics {
                        contentDescription = "${currentPage.title}, Page ${currentPageIndex + 1} of ${pages.size}"
                        customActions = buildList {
                            if (currentPageIndex > 0) {
                                val previousPage = pages[currentPageIndex - 1]
                                add(
                                    CustomAccessibilityAction("Show previous page: ${previousPage.title}") {
                                        scope.launch {
                                            if (animationsEnabled) {
                                                pagerState.animateScrollToPage(currentPageIndex - 1)
                                            } else {
                                                pagerState.scrollToPage(currentPageIndex - 1)
                                            }
                                        }
                                        true
                                    },
                                )
                            }
                            if (currentPageIndex < pages.lastIndex) {
                                val nextPage = pages[currentPageIndex + 1]
                                add(
                                    CustomAccessibilityAction("Show next page: ${nextPage.title}") {
                                        scope.launch {
                                            if (animationsEnabled) {
                                                pagerState.animateScrollToPage(currentPageIndex + 1)
                                            } else {
                                                pagerState.scrollToPage(currentPageIndex + 1)
                                            }
                                        }
                                        true
                                    },
                                )
                            }
                        }
                    },
            ) { pageIndex ->
                HomePageContainer(page = pages[pageIndex]) {
                    when (pages[pageIndex]) {
                        HomePage.Now -> {
                            if (appearance.layout == LayoutPreset.SIMPLE) {
                                SimpleNowPage(
                                    state = state,
                                    onAlertDetailsRequested = onAlertDetailsRequested,
                                )
                            } else {
                                NowPage(
                                    state = state,
                                    onAlertDetailsRequested = onAlertDetailsRequested,
                                )
                            }
                        }
                        HomePage.Forecast -> SimpleForecastPage(
                            state = state,
                            selectedChoice = simpleForecast,
                            onChoiceSelected = { simpleForecast = it },
                        )
                        HomePage.Hourly -> HourlyPage(state)
                        HomePage.Daily -> DailyPage(state)
                        HomePage.Details -> DetailsPage(state)
                    }
                }
            }
            HomeFooterNavigation(
                pages = pages,
                selectedPageIndex = currentPageIndex,
                isRefreshEnabled = state.canRefresh && !state.isRefreshInProgress,
                refreshLabel = state.refreshLabel,
                onPageSelected = { pageIndex ->
                    scope.launch {
                        if (animationsEnabled) {
                            pagerState.animateScrollToPage(pageIndex)
                        } else {
                            pagerState.scrollToPage(pageIndex)
                        }
                    }
                },
                onRefresh = onRefresh,
                onChangeLocation = onChangeLocation,
                onOpenSettings = onOpenSettings,
            )
        }
    }
}

@Composable
private fun HomeFooterNavigation(
    pages: List<HomePage>,
    selectedPageIndex: Int,
    isRefreshEnabled: Boolean,
    refreshLabel: String,
    onPageSelected: (Int) -> Unit,
    onRefresh: () -> Unit,
    onChangeLocation: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val roles = LocalOxygenHomeDesign.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home-footer-navigation"),
        verticalArrangement = Arrangement.spacedBy(roles.tileGap - 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home-page-selector"),
            horizontalArrangement = Arrangement.spacedBy(roles.tileGap - 2.dp),
        ) {
            pages.forEachIndexed { index, page ->
                val isSelected = index == selectedPageIndex
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag(page.tabTag)
                        .clickable { onPageSelected(index) }
                        .semantics {
                            contentDescription = "${page.title} page"
                            selected = isSelected
                        },
                    shape = RoundedCornerShape(roles.homeCardCorner),
                    color = if (isSelected) roles.strongGlassSurface else roles.ambientGlassSurface,
                    border = BorderStroke(
                        width = if (isSelected) roles.selectedBorderWidth else roles.normalBorderWidth,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else roles.outlineQuiet,
                    ),
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = page.title,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
                        style = roles.supportingLabel,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else homeSupportingContent(0.78f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home-secondary-actions"),
            horizontalArrangement = Arrangement.spacedBy(roles.tileGap - 2.dp),
        ) {
            TextButton(
                onClick = onChangeLocation,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("home-change-location"),
            ) {
                Text("Location")
            }
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = onRefresh,
                enabled = isRefreshEnabled,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("home-refresh"),
            ) {
                Text(text = refreshLabel)
            }
            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("home-about-entry"),
            ) {
                Text("Settings")
            }
        }
    }
}

@Composable
private fun HomePageContainer(
    page: HomePage,
    content: @Composable ColumnScope.() -> Unit,
) {
    val roles = LocalOxygenHomeDesign.current
    val useOverflowScroll = page != HomePage.Daily || LocalDensity.current.fontScale > 1f
    val bottomClearance = if (useOverflowScroll) roles.sectionGap + 24.dp else roles.sectionGap
    val baseModifier = Modifier
        .fillMaxSize()
        .padding(bottom = bottomClearance)
        .testTag(page.pageTag)
    val pageModifier = if (useOverflowScroll) {
        baseModifier.verticalScroll(rememberScrollState())
    } else {
        baseModifier
    }
    Column(
        modifier = pageModifier,
        verticalArrangement = Arrangement.spacedBy(roles.sectionGap),
        content = content,
    )
}

@Composable
private fun ReadyHeader(
    currentPage: HomePage,
    pageIndex: Int,
    pageCount: Int,
) {
    val roles = LocalOxygenHomeDesign.current
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
                style = roles.sectionHeading,
            )
        }
        Text(
            text = "Page ${pageIndex + 1} of $pageCount",
            modifier = Modifier
                .widthIn(min = 72.dp)
                .testTag("home-page-position"),
            style = roles.supportingLabel,
            color = homeSupportingContent(0.68f),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun NowPage(
    state: HomeForecastPresentationState.ForecastReady,
    onAlertDetailsRequested: () -> Unit,
) {
    val roles = LocalOxygenHomeDesign.current
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
            color = homeSupportingContent(0.68f),
        )
    }

    DashboardHero(tag = "home-section-current") {
        if (dashboard.current == null) {
            Text("Current conditions", style = roles.sectionHeading)
            Text(
                text = dashboard.currentUnavailableText ?: dashboard.returnedDataUnavailableText.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            val compactLargeFont = LocalDensity.current.fontScale > 1.2f
            val markSize = if (compactLargeFont) 96.dp else 132.dp
            val temperatureStyle = if (compactLargeFont) {
                roles.displayWeatherValue
            } else {
                roles.displayWeatherValue.copy(
                    fontSize = roles.displayWeatherValue.fontSize * 1.22,
                    lineHeight = roles.displayWeatherValue.lineHeight * 1.16,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(markSize)
                        .clip(RoundedCornerShape(8.dp))
                        .testTag("home-current-mark")
                        .semantics {
                            contentDescription = dashboard.current.spokenDescription
                        },
                ) {
                    WeatherConditionMark(
                        condition = dashboard.current.conditionIdentity,
                        modifier = Modifier.size(markSize),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = dashboard.current.condition,
                        modifier = Modifier.semantics { hideFromAccessibility() },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = dashboard.current.temperature,
                        modifier = Modifier.semantics { hideFromAccessibility() },
                        style = temperatureStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            val range = listOfNotNull(
                dashboard.current.highTemperature,
                dashboard.current.lowTemperature,
            ).joinToString("   ").ifEmpty { null }
            Text(
                text = dashboard.current.apparentTemperature,
                modifier = Modifier.semantics { hideFromAccessibility() },
                style = roles.compactWeatherValue,
            )
            NowContextGrid(
                items = listOfNotNull(
                    range?.let { NowContextItem("Today", it, redundantFromCurrentSummary = true) },
                    dashboard.metrics.firstOrNull { it.identity == HomeMetricIdentity.Humidity }
                        ?.let { NowContextItem(it.label, it.value) },
                    dashboard.metrics.firstOrNull { it.identity == HomeMetricIdentity.Wind }
                        ?.let { NowContextItem(it.label, it.value) },
                ),
            )
            Text("${dashboard.current.updatedTime} | ${dashboard.current.dataTypeLabel}", style = roles.supportingLabel)
            Text("${dashboard.source.sourceName} | ${dashboard.source.fetchedAt}", style = roles.supportingLabel)
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
                Text("Cached forecast", style = roles.sectionHeading)
                Text(freshness.statusText, style = MaterialTheme.typography.bodyMedium)
            }
        }
        is HomeForecastFreshness.StaleAfterFailedRefresh -> {
            DashboardCard(tag = "home-section-stale") {
                Text("Cached forecast", style = roles.sectionHeading)
                Text(freshness.statusText, style = MaterialTheme.typography.bodyMedium)
                Text("Refresh failed: ${freshness.refreshFailureMessage.text}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    dashboard.alertSummary?.let { alert ->
        DashboardCard(tag = "home-section-alert") {
            OfficialAlertSummary(
                summary = alert,
                onAlertDetailsRequested = onAlertDetailsRequested,
            )
        }
    }

    dashboard.precipitationSummary?.let {
        DashboardCard(tag = "home-section-precipitation") {
            Text("Near-term precipitation", style = roles.sectionHeading)
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SimpleNowPage(
    state: HomeForecastPresentationState.ForecastReady,
    onAlertDetailsRequested: () -> Unit,
) {
    NowPage(
        state = state,
        onAlertDetailsRequested = onAlertDetailsRequested,
    )
    DetailsSourceBlock(state.dashboard.source)
    ProviderDisclosure(
        state = state,
        modifier = Modifier.testTag("home-section-provenance-footer"),
    )
}

@Composable
private fun OfficialAlertSummary(
    summary: HomeAlertSummaryPresentation,
    onAlertDetailsRequested: () -> Unit,
) {
    val roles = LocalOxygenHomeDesign.current
    val uriHandler = LocalUriHandler.current
    Text("Official alert", style = roles.sectionHeading)
    Text(summary.event, style = MaterialTheme.typography.titleMedium)
    Text(
        text = "Severity: ${summary.severity}",
        style = MaterialTheme.typography.bodyMedium,
        color = roles.warningContent,
    )
    Text("Issuer: ${summary.issuer}", style = MaterialTheme.typography.bodyMedium)
    Text(summary.expires, style = MaterialTheme.typography.bodyMedium)
    Text(summary.sourceCheckedAt, style = MaterialTheme.typography.bodySmall)
    OutlinedButton(
        onClick = onAlertDetailsRequested,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .testTag("home-alert-details")
            .semantics { contentDescription = summary.detailActionContentDescription },
    ) {
        Text(summary.detailActionLabel)
    }
    TextButton(
        onClick = { uriHandler.openUri(summary.sourceLink) },
        modifier = Modifier
            .heightIn(min = 48.dp)
            .testTag("home-alert-source-link")
            .semantics { contentDescription = summary.sourceLinkLabel },
    ) {
        Text(summary.attribution)
    }
    if (summary.activeAlertCount > 1) {
        Text(
            text = "${summary.activeAlertCount} active alerts",
            modifier = Modifier.testTag("home-alert-count"),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private data class NowContextItem(
    val label: String,
    val value: String,
    val redundantFromCurrentSummary: Boolean = false,
)

@Composable
private fun NowContextGrid(items: List<NowContextItem>) {
    val roles = LocalOxygenHomeDesign.current
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
                            .heightIn(min = 54.dp)
                            .then(
                                if (item.redundantFromCurrentSummary) {
                                    Modifier.semantics { hideFromAccessibility() }
                                } else {
                                    Modifier
                                },
                            ),
                    ) {
                        Text(
                            text = item.label,
                            style = roles.supportingLabel,
                            color = homeSupportingContent(0.68f),
                        )
                        Text(
                            text = item.value,
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
    val roles = LocalOxygenHomeDesign.current
    val dashboard = state.dashboard

    if (dashboard.hourly.isNotEmpty()) {
        DashboardSection(tag = "home-section-hourly") {
            Text("Next hours", style = roles.sectionHeading)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home-hourly-grid"),
                verticalArrangement = Arrangement.spacedBy(roles.tileGap),
            ) {
                dashboard.hourly.take(6).chunked(2).forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(roles.tileGap),
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
private fun SimpleForecastPage(
    state: HomeForecastPresentationState.ForecastReady,
    selectedChoice: SimpleForecastChoice,
    onChoiceSelected: (SimpleForecastChoice) -> Unit,
) {
    val roles = LocalOxygenHomeDesign.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(roles.sectionGap),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home-simple-forecast-choice"),
            horizontalArrangement = Arrangement.spacedBy(roles.tileGap),
        ) {
            SimpleForecastChoiceChip(
                choice = SimpleForecastChoice.Hourly,
                selected = selectedChoice == SimpleForecastChoice.Hourly,
                onClick = { onChoiceSelected(SimpleForecastChoice.Hourly) },
                modifier = Modifier.weight(1f),
            )
            SimpleForecastChoiceChip(
                choice = SimpleForecastChoice.Daily,
                selected = selectedChoice == SimpleForecastChoice.Daily,
                onClick = { onChoiceSelected(SimpleForecastChoice.Daily) },
                modifier = Modifier.weight(1f),
            )
        }
        when (selectedChoice) {
            SimpleForecastChoice.Hourly -> HourlyPage(state)
            SimpleForecastChoice.Daily -> DailyPage(state)
        }
    }
}

@Composable
private fun SimpleForecastChoiceChip(
    choice: SimpleForecastChoice,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(choice.title) },
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag(choice.tag),
    )
}

@Composable
private fun DailyPage(state: HomeForecastPresentationState.ForecastReady) {
    val roles = LocalOxygenHomeDesign.current
    val dashboard = state.dashboard

    if (dashboard.daily.isNotEmpty()) {
        DashboardSection(tag = "home-section-daily") {
            Text("Daily forecast", style = roles.sectionHeading)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home-daily-list"),
                verticalArrangement = Arrangement.spacedBy(roles.tileGap - 2.dp),
            ) {
                dashboard.daily.take(6).forEachIndexed { index, day ->
                    DailyEntry(
                        day = day,
                        index = index,
                    )
                }
            }
            val sunSummary = dashboard.sun
            if (sunSummary != null) {
                Text(
                    text = "Sun ${sunSummary.sunrise} / ${sunSummary.sunset}",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = homeSupportingContent(0.70f),
                )
            }
        }
    } else {
        UnavailablePageCard("Daily forecast", dashboard.returnedDataUnavailableText)
    }
}

@Composable
private fun DetailsPage(state: HomeForecastPresentationState.ForecastReady) {
    val dashboard = state.dashboard
    val groups = dashboard.metrics.toDetailsGroups()

    if (groups.isNotEmpty()) {
        DetailsMetricGroups(groups.take(2))
    } else if (dashboard.returnedDataUnavailableText != null) {
        UnavailablePageCard("Details", dashboard.returnedDataUnavailableText)
    }

    DetailsSourceBlock(dashboard.source)

    DetailsStatusBlock(state.freshness)

    if (groups.size > 2) {
        DetailsMetricGroups(groups.drop(2), includeContainerTag = false)
    }

    dashboard.sun?.let {
        DetailsSunBlock(it)
    }

    ProviderDisclosure(
        state = state,
        modifier = Modifier.testTag("home-section-provenance-footer"),
    )
}

@Composable
private fun DetailsStatusBlock(freshness: HomeForecastFreshness) {
    val roles = LocalOxygenHomeDesign.current
    when (freshness) {
        HomeForecastFreshness.Fresh -> Unit
        is HomeForecastFreshness.RestoredFromCache -> {
            DashboardCard(tag = "home-section-status") {
                Text("Cached forecast", style = roles.sectionHeading)
                Text(freshness.statusText, style = MaterialTheme.typography.bodyMedium)
            }
        }
        is HomeForecastFreshness.StaleAfterFailedRefresh -> {
            DashboardCard(tag = "home-section-status") {
                Text("Cached forecast", style = roles.sectionHeading)
                Text(freshness.statusText, style = MaterialTheme.typography.bodyMedium)
                Text("Refresh failed: ${freshness.refreshFailureMessage.text}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DetailsMetricGroups(
    groups: List<DetailsMetricGroup>,
    includeContainerTag: Boolean = true,
) {
    val roles = LocalOxygenHomeDesign.current
    val modifier = if (includeContainerTag) {
        Modifier
            .fillMaxWidth()
            .testTag("home-section-metrics")
    } else {
        Modifier.fillMaxWidth()
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(roles.tileGap),
    ) {
        groups.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(roles.tileGap),
            ) {
                row.forEach { group ->
                    DetailsMetricGroupCard(
                        group = group,
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

@Composable
private fun DetailsMetricGroupCard(
    group: DetailsMetricGroup,
    modifier: Modifier = Modifier,
) {
    val roles = LocalOxygenHomeDesign.current
    Card(
        modifier = modifier
            .heightIn(min = 96.dp)
            .testTag(group.tag)
            .semantics {
                contentDescription = group.contentDescription
            },
        shape = RoundedCornerShape(roles.homeCardCorner),
        border = BorderStroke(roles.normalBorderWidth, roles.outlineQuiet),
        colors = CardDefaults.cardColors(
            containerColor = roles.ambientGlassSurface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            group.metrics.forEach { metric ->
                DetailsMetricLine(metric)
            }
        }
    }
}

@Composable
private fun DetailsMetricLine(metric: HomeMetricPresentation) {
    val roles = LocalOxygenHomeDesign.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = metric.label,
            style = roles.supportingLabel,
            color = homeSupportingContent(0.68f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = metric.value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DetailsSunBlock(sun: HomeSunPresentation) {
    val roles = LocalOxygenHomeDesign.current
    DashboardCard(tag = "home-section-sun") {
        Text("Sun", style = roles.sectionHeading)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(roles.tileGap),
        ) {
            DetailsValueColumn("Sunrise", sun.sunrise, Modifier.weight(1f))
            DetailsValueColumn("Sunset", sun.sunset, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DetailsSourceBlock(source: HomeSourcePresentation) {
    val roles = LocalOxygenHomeDesign.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home-section-source"),
        shape = RoundedCornerShape(roles.homeCardCorner),
        border = BorderStroke(roles.normalBorderWidth, roles.outlineQuiet),
        colors = CardDefaults.cardColors(
            containerColor = roles.strongGlassSurface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(roles.compactCardPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Source and updates", style = roles.sectionHeading)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(roles.tileGap),
            ) {
                DetailsValueColumn("Provider", source.sourceName, Modifier.weight(1f))
                DetailsValueColumn("Fetched", source.fetchedAt, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(roles.tileGap),
            ) {
                DetailsValueColumn("Data type", source.dataType, Modifier.weight(1f))
                DetailsValueColumn("Issued", source.issuedAt ?: "Unavailable", Modifier.weight(1f))
            }
            source.license?.let { Text("License $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun DetailsValueColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val roles = LocalOxygenHomeDesign.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = label,
            style = roles.supportingLabel,
            color = homeSupportingContent(0.68f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun homeSupportingContent(alpha: Float): androidx.compose.ui.graphics.Color {
    val roles = LocalOxygenHomeDesign.current
    return if (roles.supportingContent != androidx.compose.ui.graphics.Color.Unspecified) {
        roles.supportingContent
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
    }
}

private data class DetailsMetricGroup(
    val title: String,
    val tag: String,
    val metrics: List<HomeMetricPresentation>,
) {
    val contentDescription: String =
        (listOf(title) + metrics.flatMap { listOf(it.label, it.value) }).joinToString(", ")
}

private fun List<HomeMetricPresentation>.toDetailsGroups(): List<DetailsMetricGroup> {
    fun metricsFor(vararg identities: HomeMetricIdentity): List<HomeMetricPresentation> =
        identities.mapNotNull { identity -> firstOrNull { it.identity == identity } }

    return listOf(
        DetailsMetricGroup(
            title = "Comfort",
            tag = "home-section-comfort",
            metrics = metricsFor(
                HomeMetricIdentity.ApparentTemperature,
                HomeMetricIdentity.Humidity,
                HomeMetricIdentity.DewPoint,
            ),
        ),
        DetailsMetricGroup(
            title = "Wind",
            tag = "home-section-wind",
            metrics = metricsFor(HomeMetricIdentity.Wind),
        ),
        DetailsMetricGroup(
            title = "Atmosphere",
            tag = "home-section-atmosphere",
            metrics = metricsFor(
                HomeMetricIdentity.Pressure,
                HomeMetricIdentity.Visibility,
                HomeMetricIdentity.CloudCover,
                HomeMetricIdentity.Precipitation,
            ),
        ),
    ).filter { it.metrics.isNotEmpty() }
}

@Composable
private fun DailyEntry(
    day: HomeDailyPresentation,
    index: Int,
) {
    val roles = LocalOxygenHomeDesign.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .testTag("home-daily-entry-$index")
            .clearAndSetSemantics {
                contentDescription = day.spokenDescription
            },
        shape = RoundedCornerShape(roles.homeCardCorner),
        border = BorderStroke(roles.normalBorderWidth, roles.outlineQuiet),
        colors = CardDefaults.cardColors(
            containerColor = roles.ambientGlassSurface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .semantics {
                        contentDescription = day.condition
                    },
            ) {
                WeatherConditionMark(
                    condition = day.conditionIdentity,
                    modifier = Modifier.size(30.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = day.date,
                    style = roles.compactWeatherValue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = day.condition,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = day.precipitationProbability?.let { "Precip $it" } ?: "Precip n/a",
                    style = roles.supportingLabel,
                    maxLines = 2,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DailyTemperatureColumn("Low", day.low)
                DailyTemperatureColumn("High", day.high)
            }
        }
    }
}

@Composable
private fun DailyTemperatureColumn(
    label: String,
    value: String,
) {
    val roles = LocalOxygenHomeDesign.current
    Column(
        modifier = Modifier.widthIn(min = 64.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = label,
            style = roles.supportingLabel,
            color = homeSupportingContent(0.68f),
            maxLines = 1,
        )
        Text(
            text = value.removePrefix("$label "),
            style = roles.compactWeatherValue,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun UnavailablePageCard(title: String, message: String?) {
    DashboardCard(tag = "home-section-unavailable") {
        Text(title, style = LocalOxygenHomeDesign.current.sectionHeading)
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
    Forecast("Forecast", "home-page-tab-forecast", "home-page-forecast"),
    Hourly("Hourly", "home-page-tab-hourly", "home-page-hourly"),
    Daily("Daily", "home-page-tab-daily", "home-page-daily"),
    Details("Details", "home-page-tab-details", "home-page-details"),
}

private enum class SimpleForecastChoice(
    val title: String,
    val tag: String,
) {
    Hourly("Hourly", "home-simple-forecast-hourly"),
    Daily("Daily", "home-simple-forecast-daily"),
}

@Composable
private fun DashboardSection(
    tag: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val roles = LocalOxygenHomeDesign.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        verticalArrangement = Arrangement.spacedBy(roles.tileGap - 2.dp),
        content = content,
    )
}

@Composable
private fun DashboardCard(
    tag: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val roles = LocalOxygenHomeDesign.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        shape = RoundedCornerShape(roles.homeCardCorner),
        border = BorderStroke(roles.normalBorderWidth, roles.outlineQuiet),
        colors = CardDefaults.cardColors(
            containerColor = roles.strongGlassSurface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(roles.cardPadding),
            verticalArrangement = Arrangement.spacedBy(roles.tileGap - 2.dp),
            content = content,
        )
    }
}

@Composable
private fun DashboardHero(
    tag: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val roles = LocalOxygenHomeDesign.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        verticalArrangement = Arrangement.spacedBy(roles.tileGap),
        content = content,
    )
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
                color = homeSupportingContent(0.70f),
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
    val roles = LocalOxygenHomeDesign.current
    Card(
        modifier = modifier
            .heightIn(min = 116.dp)
            .testTag("home-hourly-entry-$index")
            .clearAndSetSemantics {
                contentDescription = hour.spokenDescription
            },
        shape = RoundedCornerShape(roles.homeCardCorner),
        border = BorderStroke(roles.normalBorderWidth, roles.outlineQuiet),
        colors = CardDefaults.cardColors(
            containerColor = roles.ambientGlassSurface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(roles.compactCardPadding),
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
                style = roles.compactWeatherValue,
                maxLines = 1,
            )
            Text(
                text = hour.precipitationProbability?.let { "Precip $it" } ?: "Precip n/a",
                style = roles.supportingLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MetricGrid(metrics: List<HomeMetricPresentation>) {
    DashboardCard(tag = "home-section-metrics") {
        Text("Metrics", style = LocalOxygenHomeDesign.current.sectionHeading)
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
                            style = LocalOxygenHomeDesign.current.supportingLabel,
                            color = homeSupportingContent(0.68f),
                        )
                        Text(metric.value, style = LocalOxygenHomeDesign.current.compactWeatherValue)
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
            color = homeSupportingContent(0.72f),
        )
        Text(
            text = state.forecastPrivacyNote,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = homeSupportingContent(0.68f),
        )
    }
}

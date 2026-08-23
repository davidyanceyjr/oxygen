package com.oxygen.weather.app.ui.home

import androidx.compose.runtime.Composable
import com.oxygen.weather.app.HomeForecastPresentationState
import com.oxygen.weather.app.ui.theme.OxygenThemeId

@Composable
@Suppress("UNUSED_PARAMETER")
fun HomeScreen(
    state: HomeForecastPresentationState.ForecastReady,
    selectedTheme: OxygenThemeId,
    onThemeSelected: (OxygenThemeId) -> Unit,
) {
    HomeLoadingScreen(state = state)
}

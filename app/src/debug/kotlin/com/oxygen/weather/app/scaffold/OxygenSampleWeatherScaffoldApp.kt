package com.oxygen.weather.app.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.oxygen.weather.app.HomeForecastPresentationState
import com.oxygen.weather.app.sample.SampleWeather
import com.oxygen.weather.app.ui.home.HomeScreen
import com.oxygen.weather.app.ui.theme.OxygenTheme
import com.oxygen.weather.app.ui.theme.OxygenThemeId

@Composable
fun OxygenSampleWeatherScaffoldApp() {
    var themeId by remember { mutableStateOf(OxygenThemeId.OXYGEN) }

    OxygenTheme(themeId = themeId) {
        HomeScreen(
            state = HomeForecastPresentationState.ForecastReady.from(
                location = SampleWeather.bundle.location,
                weather = SampleWeather.bundle,
            ),
            selectedTheme = themeId,
            onThemeSelected = { themeId = it },
        )
    }
}

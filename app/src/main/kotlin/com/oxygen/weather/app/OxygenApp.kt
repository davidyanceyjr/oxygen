package com.oxygen.weather.app

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.oxygen.weather.app.ui.firstrun.FirstRunLocationEntryScreen
import com.oxygen.weather.app.ui.home.HomeLoadingScreen
import com.oxygen.weather.app.ui.theme.OxygenTheme
import com.oxygen.weather.app.ui.theme.OxygenThemeId

@Composable
fun OxygenApp(
    stateHolder: OxygenAppStateHolder = remember { OxygenAppStateHolder() },
    locationPermissionResult: LocationPermissionResult? = null,
    onRequestLocationPermission: () -> Unit = {},
) {
    var themeId by remember { mutableStateOf(OxygenThemeId.OXYGEN) }
    var appState by remember(stateHolder) { mutableStateOf(stateHolder.presentationState) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    stateHolder.setOnStateChanged { state ->
        mainHandler.post {
            appState = state
        }
    }

    LaunchedEffect(locationPermissionResult) {
        locationPermissionResult?.let {
            stateHolder.onLocationPermissionResult(it)
            appState = stateHolder.presentationState
        }
    }

    OxygenTheme(themeId = themeId) {
        when (val screen = appState.screen) {
            is OxygenAppScreen.FirstRunLocationEntry -> FirstRunLocationEntryScreen(
                state = screen,
                onQueryChanged = {
                    stateHolder.onManualLocationQueryChanged(it)
                    appState = stateHolder.presentationState
                },
                onSearch = {
                    stateHolder.onManualLocationSearchSubmitted()
                    appState = stateHolder.presentationState
                },
                onRetry = {
                    stateHolder.onManualLocationSearchRetry()
                    appState = stateHolder.presentationState
                },
                onCandidateSelected = {
                    stateHolder.onManualLocationCandidateSelected(it)
                    appState = stateHolder.presentationState
                },
                onUseMyLocation = {
                    stateHolder.onUseMyLocation()
                    stateHolder.consumeNextCommand()?.let { command ->
                        when (command) {
                            OxygenAppCommand.RequestLocationPermission -> onRequestLocationPermission()
                        }
                    }
                    appState = stateHolder.presentationState
                },
            )
            is OxygenAppScreen.Home -> HomeLoadingScreen(
                state = screen.forecast,
                onRetry = {
                    stateHolder.onHomeForecastRetry()
                    appState = stateHolder.presentationState
                },
            )
        }
    }
}

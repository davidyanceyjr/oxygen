package com.oxygen.weather.app

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalContext
import com.oxygen.weather.app.ui.alerts.AlertDetailScreen
import com.oxygen.weather.app.ui.firstrun.FirstRunLocationEntryScreen
import com.oxygen.weather.app.ui.home.HomeLoadingScreen
import com.oxygen.weather.app.ui.settings.SettingsScreen
import com.oxygen.weather.app.ui.theme.LayoutPreset
import com.oxygen.weather.app.ui.theme.OxygenAppearance
import com.oxygen.weather.app.ui.theme.OxygenTheme

@Composable
fun OxygenApp(
    stateHolder: OxygenAppStateHolder? = null,
    onRequestLocationPermission: (Long) -> Unit = {},
    appearance: OxygenAppearance = OxygenAppearance(),
    motionPreferenceSource: MotionPreferenceSource = EnabledMotionPreferenceSource,
) {
    val appStateHolder = stateHolder ?: remember(appearance.layout) {
        OxygenAppStateHolder(initialLayout = appearance.layout)
    }
    var appState by remember(appStateHolder) { mutableStateOf(appStateHolder.presentationState) }
    var animationsEnabled by remember(motionPreferenceSource) {
        mutableStateOf(motionPreferenceSource.areAnimationsEnabled())
    }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val lifecycleOwner = LocalContext.current as? androidx.lifecycle.LifecycleOwner

    DisposableEffect(lifecycleOwner, motionPreferenceSource) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                animationsEnabled = motionPreferenceSource.areAnimationsEnabled()
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose { lifecycleOwner?.lifecycle?.removeObserver(observer) }
    }

    DisposableEffect(appStateHolder) {
        appStateHolder.setOnStateChanged { state ->
            mainHandler.post { appState = state }
        }
        appState = appStateHolder.presentationState
        onDispose {
            appStateHolder.setOnStateChanged { }
            appStateHolder.cancelDeviceLocation()
            mainHandler.removeCallbacksAndMessages(null)
        }
    }
    val entry = appState.screen as? OxygenAppScreen.FirstRunLocationEntry
    BackHandler(
        enabled = entry?.deviceProgress != null ||
            entry?.canReturn == true ||
            appState.screen is OxygenAppScreen.Settings ||
            appState.screen is OxygenAppScreen.AlertDetail,
    ) {
        when (appState.screen) {
            is OxygenAppScreen.FirstRunLocationEntry -> appStateHolder.onLocationEntryBack()
            is OxygenAppScreen.Settings -> appStateHolder.onSettingsBack()
            is OxygenAppScreen.AlertDetail -> appStateHolder.onAlertDetailBack()
            is OxygenAppScreen.Home -> Unit
        }
        appState = appStateHolder.presentationState
    }

    val themeId = appearance.theme
    val sessionAppearance = appearance.copy(layout = appState.layout)
    val requestedAppearance = if (appState.effectsPreference.isManaged) {
        sessionAppearance.copy(effects = appState.effectsPreference.effectiveRequested)
    } else {
        sessionAppearance
    }
    val effectiveAppearance = requestedAppearance.copy(
        effects = if (animationsEnabled) requestedAppearance.effects else com.oxygen.weather.app.ui.theme.EffectsLevel.OFF,
    )

    OxygenTheme(themeId = themeId) {
        when (val screen = appState.screen) {
            is OxygenAppScreen.FirstRunLocationEntry -> FirstRunLocationEntryScreen(
                state = screen,
                selectedLocation = appState.selectedLocation,
                savedLocations = appState.savedLocations,
                canSaveSearchResults = appStateHolder.canSaveSearchResults,
                onQueryChanged = {
                    appStateHolder.onManualLocationQueryChanged(it)
                    appState = appStateHolder.presentationState
                },
                onSearch = {
                    appStateHolder.onManualLocationSearchSubmitted()
                    appState = appStateHolder.presentationState
                },
                onRetry = {
                    appStateHolder.onManualLocationSearchRetry()
                    appState = appStateHolder.presentationState
                },
                onCandidateSelected = {
                    appStateHolder.onManualLocationCandidateSelected(it)
                    appState = appStateHolder.presentationState
                },
                onCandidateSaved = {
                    appStateHolder.onManualLocationCandidateSaved(it)
                    appState = appStateHolder.presentationState
                },
                onSavedLocationSelected = {
                    appStateHolder.onSavedLocationSelected(it)
                    appState = appStateHolder.presentationState
                },
                onSavedLocationRemoveRequested = {
                    appStateHolder.onSavedLocationRemoveRequested(it)
                    appState = appStateHolder.presentationState
                },
                onSavedLocationRemoveCanceled = {
                    appStateHolder.onSavedLocationRemoveCanceled(it)
                    appState = appStateHolder.presentationState
                },
                onSavedLocationRemoveConfirmed = {
                    appStateHolder.onSavedLocationRemoveConfirmed(it)
                    appState = appStateHolder.presentationState
                },
                onUseMyLocation = {
                    appStateHolder.onUseMyLocation()
                    appStateHolder.consumeNextCommand()?.let { command ->
                        when (command) {
                            is OxygenAppCommand.RequestLocationPermission -> onRequestLocationPermission(command.attempt)
                        }
                    }
                    appState = appStateHolder.presentationState
                },
                onCancelDeviceLocation = { appStateHolder.cancelDeviceLocation() },
                onBack = {
                    appStateHolder.onLocationEntryBack()
                    appState = appStateHolder.presentationState
                },
                onOpenSettings = {
                    appStateHolder.onOpenSettings()
                    appState = appStateHolder.presentationState
                },
                showSettingsEntry = screen.returnScreen !is OxygenAppScreen.Settings,
            )
            is OxygenAppScreen.Home -> HomeLoadingScreen(
                state = screen.forecast,
                appearance = effectiveAppearance,
                animationsEnabled = animationsEnabled,
                onRetry = {
                    appStateHolder.onHomeForecastRetry()
                    appState = appStateHolder.presentationState
                },
                onRefresh = {
                    appStateHolder.onHomeForecastRefresh()
                    appState = appStateHolder.presentationState
                },
                onChangeLocation = {
                    appStateHolder.onChangeLocation()
                    appState = appStateHolder.presentationState
                },
                onOpenSettings = {
                    appStateHolder.onOpenSettings()
                    appState = appStateHolder.presentationState
                },
                onAlertDetailsRequested = {
                    appStateHolder.onHomeAlertDetailsRequested()
                    appState = appStateHolder.presentationState
                },
            )
            is OxygenAppScreen.Settings -> SettingsScreen(
                state = screen,
                appearance = effectiveAppearance,
                themeId = themeId,
                effectsPreference = appState.effectsPreference,
                layoutPreference = appState.layoutPreference,
                animationsEnabled = animationsEnabled,
                onDestinationSelected = {
                    appStateHolder.onSettingsDestinationSelected(it)
                    appState = appStateHolder.presentationState
                },
                onBack = {
                    appStateHolder.onSettingsBack()
                    appState = appStateHolder.presentationState
                },
                selectedUnitPreference = appState.unitPreference,
                unitPreferenceMessage = screen.unitPreferenceMessage,
                onUnitPreferenceSelected = {
                    appStateHolder.onUnitPreferenceSelected(it)
                    appState = appStateHolder.presentationState
                },
                onEffectsPreferenceSelected = {
                    appStateHolder.onEffectsPreferenceSelected(it)
                    appState = appStateHolder.presentationState
                },
                onEffectsPreferenceRetry = {
                    appStateHolder.onEffectsPreferenceRetry()
                    appState = appStateHolder.presentationState
                },
                onLayoutSelected = { layout: LayoutPreset ->
                    appStateHolder.onLayoutSelected(layout)
                    appState = appStateHolder.presentationState
                },
                onLayoutPreferenceRetry = {
                    appStateHolder.onLayoutPreferenceRetry()
                    appState = appStateHolder.presentationState
                },
            )
            is OxygenAppScreen.AlertDetail -> AlertDetailScreen(
                state = screen,
                onAlertSelected = {
                    appStateHolder.onAlertDetailSelected(it)
                    appState = appStateHolder.presentationState
                },
                onBack = {
                    appStateHolder.onAlertDetailBack()
                    appState = appStateHolder.presentationState
                },
            )
        }
    }
}

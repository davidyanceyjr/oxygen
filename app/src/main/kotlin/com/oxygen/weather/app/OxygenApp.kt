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
    stateHolder: OxygenAppStateHolder = remember { OxygenAppStateHolder() },
    onRequestLocationPermission: (Long) -> Unit = {},
    appearance: OxygenAppearance = OxygenAppearance(),
    motionPreferenceSource: MotionPreferenceSource = EnabledMotionPreferenceSource,
) {
    var appState by remember(stateHolder) { mutableStateOf(stateHolder.presentationState) }
    var animationsEnabled by remember(motionPreferenceSource) {
        mutableStateOf(motionPreferenceSource.areAnimationsEnabled())
    }
    var sessionLayout by remember { mutableStateOf(appearance.layout) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val lifecycleOwner = LocalContext.current as? androidx.lifecycle.LifecycleOwner

    LaunchedEffect(appearance.layout) {
        sessionLayout = appearance.layout
    }

    DisposableEffect(lifecycleOwner, motionPreferenceSource) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                animationsEnabled = motionPreferenceSource.areAnimationsEnabled()
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose { lifecycleOwner?.lifecycle?.removeObserver(observer) }
    }

    DisposableEffect(stateHolder) {
        stateHolder.setOnStateChanged { state ->
            mainHandler.post { appState = state }
        }
        appState = stateHolder.presentationState
        onDispose {
            stateHolder.setOnStateChanged { }
            stateHolder.cancelDeviceLocation()
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
            is OxygenAppScreen.FirstRunLocationEntry -> stateHolder.onLocationEntryBack()
            is OxygenAppScreen.Settings -> stateHolder.onSettingsBack()
            is OxygenAppScreen.AlertDetail -> stateHolder.onAlertDetailBack()
            is OxygenAppScreen.Home -> Unit
        }
        appState = stateHolder.presentationState
    }

    val themeId = appearance.theme
    val sessionAppearance = appearance.copy(layout = sessionLayout)
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
                canSaveSearchResults = stateHolder.canSaveSearchResults,
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
                onCandidateSaved = {
                    stateHolder.onManualLocationCandidateSaved(it)
                    appState = stateHolder.presentationState
                },
                onSavedLocationSelected = {
                    stateHolder.onSavedLocationSelected(it)
                    appState = stateHolder.presentationState
                },
                onSavedLocationRemoveRequested = {
                    stateHolder.onSavedLocationRemoveRequested(it)
                    appState = stateHolder.presentationState
                },
                onSavedLocationRemoveCanceled = {
                    stateHolder.onSavedLocationRemoveCanceled(it)
                    appState = stateHolder.presentationState
                },
                onSavedLocationRemoveConfirmed = {
                    stateHolder.onSavedLocationRemoveConfirmed(it)
                    appState = stateHolder.presentationState
                },
                onUseMyLocation = {
                    stateHolder.onUseMyLocation()
                    stateHolder.consumeNextCommand()?.let { command ->
                        when (command) {
                            is OxygenAppCommand.RequestLocationPermission -> onRequestLocationPermission(command.attempt)
                        }
                    }
                    appState = stateHolder.presentationState
                },
                onCancelDeviceLocation = { stateHolder.cancelDeviceLocation() },
                onBack = {
                    stateHolder.onLocationEntryBack()
                    appState = stateHolder.presentationState
                },
                onOpenSettings = {
                    stateHolder.onOpenSettings()
                    appState = stateHolder.presentationState
                },
                showSettingsEntry = screen.returnScreen !is OxygenAppScreen.Settings,
            )
            is OxygenAppScreen.Home -> HomeLoadingScreen(
                state = screen.forecast,
                appearance = effectiveAppearance,
                animationsEnabled = animationsEnabled,
                onRetry = {
                    stateHolder.onHomeForecastRetry()
                    appState = stateHolder.presentationState
                },
                onRefresh = {
                    stateHolder.onHomeForecastRefresh()
                    appState = stateHolder.presentationState
                },
                onChangeLocation = {
                    stateHolder.onChangeLocation()
                    appState = stateHolder.presentationState
                },
                onOpenSettings = {
                    stateHolder.onOpenSettings()
                    appState = stateHolder.presentationState
                },
                onAlertDetailsRequested = {
                    stateHolder.onHomeAlertDetailsRequested()
                    appState = stateHolder.presentationState
                },
            )
            is OxygenAppScreen.Settings -> SettingsScreen(
                state = screen,
                appearance = effectiveAppearance,
                themeId = themeId,
                effectsPreference = appState.effectsPreference,
                animationsEnabled = animationsEnabled,
                onDestinationSelected = {
                    stateHolder.onSettingsDestinationSelected(it)
                    appState = stateHolder.presentationState
                },
                onBack = {
                    stateHolder.onSettingsBack()
                    appState = stateHolder.presentationState
                },
                selectedUnitPreference = appState.unitPreference,
                unitPreferenceMessage = screen.unitPreferenceMessage,
                onUnitPreferenceSelected = {
                    stateHolder.onUnitPreferenceSelected(it)
                    appState = stateHolder.presentationState
                },
                onEffectsPreferenceSelected = {
                    stateHolder.onEffectsPreferenceSelected(it)
                    appState = stateHolder.presentationState
                },
                onEffectsPreferenceRetry = {
                    stateHolder.onEffectsPreferenceRetry()
                    appState = stateHolder.presentationState
                },
                onLayoutSelected = { layout: LayoutPreset ->
                    sessionLayout = layout
                },
            )
            is OxygenAppScreen.AlertDetail -> AlertDetailScreen(
                state = screen,
                onAlertSelected = {
                    stateHolder.onAlertDetailSelected(it)
                    appState = stateHolder.presentationState
                },
                onBack = {
                    stateHolder.onAlertDetailBack()
                    appState = stateHolder.presentationState
                },
            )
        }
    }
}

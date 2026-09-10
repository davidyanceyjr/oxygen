package com.oxygen.weather.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oxygen.weather.app.AboutSection
import com.oxygen.weather.R
import com.oxygen.weather.app.ContrastPreferencePresentationState
import com.oxygen.weather.app.ContrastPreferenceReadState
import com.oxygen.weather.app.EffectsPreferencePresentationState
import com.oxygen.weather.app.EffectsPreferenceReadState
import com.oxygen.weather.app.LayoutPreferencePresentationState
import com.oxygen.weather.app.LayoutPreferenceReadState
import com.oxygen.weather.app.OxygenAppScreen
import com.oxygen.weather.app.SettingsDestination
import com.oxygen.weather.app.ThemePreferencePresentationState
import com.oxygen.weather.app.ThemePreferenceReadState
import com.oxygen.weather.app.UnitPreferenceMessage
import com.oxygen.weather.app.ui.theme.EffectsLevel
import com.oxygen.weather.app.ui.theme.ContrastLevel
import com.oxygen.weather.app.ui.theme.LayoutPreset
import com.oxygen.weather.app.ui.theme.OxygenAppearance
import com.oxygen.weather.app.ui.theme.OxygenThemeId
import com.oxygen.weather.app.ui.theme.displayName
import com.oxygen.weather.app.ui.units.UnitPreferencesScreen
import com.oxygen.weather.core.model.UnitPreference

@Composable
fun SettingsScreen(
    state: OxygenAppScreen.Settings,
    appearance: OxygenAppearance,
    themeId: OxygenThemeId,
    onDestinationSelected: (SettingsDestination) -> Unit,
    onBack: () -> Unit,
    selectedUnitPreference: UnitPreference? = null,
    unitPreferenceMessage: UnitPreferenceMessage? = null,
    onUnitPreferenceSelected: (UnitPreference?) -> Unit = {},
    layoutPreference: LayoutPreferencePresentationState = LayoutPreferencePresentationState.notConfigured(),
    effectsPreference: EffectsPreferencePresentationState = EffectsPreferencePresentationState.notConfigured(),
    themePreference: ThemePreferencePresentationState = ThemePreferencePresentationState.notConfigured(),
    animationsEnabled: Boolean = true,
    onEffectsPreferenceSelected: (EffectsLevel) -> Unit = {},
    onEffectsPreferenceRetry: () -> Unit = {},
    onThemeSelected: (OxygenThemeId) -> Unit = {},
    onThemePreferenceRetry: () -> Unit = {},
    onLayoutSelected: (LayoutPreset) -> Unit = {},
    onLayoutPreferenceRetry: () -> Unit = {},
    contrastPreference: ContrastPreferencePresentationState = ContrastPreferencePresentationState.notConfigured(),
    onContrastPreferenceSelected: (ContrastLevel) -> Unit = {},
    onContrastPreferenceRetry: () -> Unit = {},
) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .testTag("settings-content"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.app_name).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = state.title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                state.selectedDestination?.let { destination ->
                    Text(
                        text = destination.title,
                        modifier = Modifier.fillMaxWidth().testTag("settings-destination-title"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                when (state.selectedDestination) {
                    null -> SettingsRoot(
                        destinations = state.destinationOptions,
                        onDestinationSelected = onDestinationSelected,
                    )
                    SettingsDestination.Appearance -> AppearanceSummary(
                        themeId = themeId,
                        layout = appearance.layout,
                        effects = appearance.effects,
                        themePreference = themePreference,
                        layoutPreference = layoutPreference,
                        preference = effectsPreference,
                        animationsEnabled = animationsEnabled,
                        onLayoutSelected = onLayoutSelected,
                        onLayoutRetry = onLayoutPreferenceRetry,
                        onEffectsSelected = onEffectsPreferenceSelected,
                        onRetry = onEffectsPreferenceRetry,
                        onThemeSelected = onThemeSelected,
                        onThemeRetry = onThemePreferenceRetry,
                        contrast = appearance.contrast,
                        contrastPreference = contrastPreference,
                        onContrastSelected = onContrastPreferenceSelected,
                        onContrastRetry = onContrastPreferenceRetry,
                    )
                    SettingsDestination.Units -> UnitPreferencesScreen(
                        selectedPreference = selectedUnitPreference,
                        message = unitPreferenceMessage,
                        onPreferenceSelected = onUnitPreferenceSelected,
                    )
                    SettingsDestination.Locations -> Text(
                        text = stringResource(R.string.settings_locations_surface),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    else -> state.destinationState.sections.forEach { section -> AboutSectionView(section) }
                }
            }
            SettingsBottomActions(
                state = state,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun SettingsRoot(
    destinations: List<SettingsDestination>,
    onDestinationSelected: (SettingsDestination) -> Unit,
) {
    SettingsGroup(stringResource(R.string.settings_group_appearance), destinations, setOf(SettingsDestination.Appearance), onDestinationSelected)
    SettingsGroup(stringResource(R.string.settings_group_weather), destinations, setOf(SettingsDestination.Units), onDestinationSelected)
    SettingsGroup(stringResource(R.string.settings_group_places), destinations, setOf(SettingsDestination.Locations), onDestinationSelected)
    SettingsGroup(
        stringResource(R.string.settings_group_information),
        destinations,
        setOf(
            SettingsDestination.DataSources,
            SettingsDestination.Privacy,
            SettingsDestination.OpenSourceLicenses,
            SettingsDestination.About,
        ),
        onDestinationSelected,
    )
}

@Composable
private fun SettingsGroup(
    heading: String,
    destinations: List<SettingsDestination>,
    members: Set<SettingsDestination>,
    onDestinationSelected: (SettingsDestination) -> Unit,
) {
    val visible = destinations.filter { it in members }
    if (visible.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        visible.forEach { destination ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable { onDestinationSelected(destination) }
                    .semantics { role = Role.Button }
                    .testTag("settings-destination-${destination.name.lowercase()}"),
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppearanceSummary(
    themeId: OxygenThemeId,
    layout: LayoutPreset,
    effects: EffectsLevel,
    layoutPreference: LayoutPreferencePresentationState,
    preference: EffectsPreferencePresentationState,
    themePreference: ThemePreferencePresentationState,
    animationsEnabled: Boolean,
    onLayoutSelected: (LayoutPreset) -> Unit,
    onLayoutRetry: () -> Unit,
    onEffectsSelected: (EffectsLevel) -> Unit,
    onRetry: () -> Unit,
    onThemeSelected: (OxygenThemeId) -> Unit,
    onThemeRetry: () -> Unit,
    contrast: ContrastLevel,
    contrastPreference: ContrastPreferencePresentationState,
    onContrastSelected: (ContrastLevel) -> Unit,
    onContrastRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings-appearance-summary"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_effective_appearance),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        AppearanceValue(stringResource(R.string.settings_theme), themeId.displayName)
        if (themePreference.isManaged) {
            Text(stringResource(R.string.settings_theme))
            val themeChoicesEnabled = themePreference.readState == ThemePreferenceReadState.Loaded &&
                themePreference.pending == null &&
                !themePreference.writeError
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeChoice(
                    label = stringResource(R.string.theme_oxygen),
                    theme = OxygenThemeId.OXYGEN,
                    selected = themeId == OxygenThemeId.OXYGEN,
                    enabled = themeChoicesEnabled,
                    onClick = { onThemeSelected(OxygenThemeId.OXYGEN) },
                )
                ThemeChoice(
                    label = stringResource(R.string.theme_paper),
                    theme = OxygenThemeId.PAPER,
                    selected = themeId == OxygenThemeId.PAPER,
                    enabled = themeChoicesEnabled,
                    onClick = { onThemeSelected(OxygenThemeId.PAPER) },
                )
                ThemeChoice(
                    label = stringResource(R.string.theme_terminal),
                    theme = OxygenThemeId.TERMINAL,
                    selected = themeId == OxygenThemeId.TERMINAL,
                    enabled = themeChoicesEnabled,
                    onClick = { onThemeSelected(OxygenThemeId.TERMINAL) },
                )
            }
            when {
                themePreference.pending != null -> Text(
                    text = stringResource(R.string.preference_saving, themePreference.pending.displayName),
                    modifier = Modifier.testTag("theme_preference_loading"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                themePreference.readState == ThemePreferenceReadState.Loading -> Text(
                    text = stringResource(R.string.preference_loading, stringResource(R.string.settings_theme).lowercase()),
                    modifier = Modifier.testTag("theme_preference_loading"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                themePreference.writeError -> {
                    Text(
                        text = stringResource(R.string.preference_save_failed, stringResource(R.string.settings_theme)),
                        modifier = Modifier.testTag("theme_preference_error"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    OutlinedButton(
                        onClick = onThemeRetry,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("theme_preference_retry"),
                    ) {
                        Text(stringResource(R.string.preference_retry))
                    }
                }
                themePreference.readState == ThemePreferenceReadState.Failed -> {
                    Text(
                        text = stringResource(R.string.preference_load_failed, stringResource(R.string.settings_theme), themeId.displayName),
                        modifier = Modifier.testTag("theme_preference_error"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    OutlinedButton(
                        onClick = onThemeRetry,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("theme_preference_retry"),
                    ) {
                        Text(stringResource(R.string.preference_retry))
                    }
                }
                else -> Text(
                    text = stringResource(R.string.preference_saved, stringResource(R.string.settings_theme)),
                    modifier = Modifier.testTag("theme_preference_saved"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
        }
        AppearanceValue(stringResource(R.string.settings_contrast), contrast.displayName())
        if (contrastPreference.isManaged) {
            Text(stringResource(R.string.settings_contrast))
            val contrastChoicesEnabled = contrastPreference.readState == ContrastPreferenceReadState.Loaded &&
                contrastPreference.pending == null &&
                !contrastPreference.writeError
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ContrastChoice(
                    label = stringResource(R.string.contrast_standard),
                    level = ContrastLevel.STANDARD,
                    selected = contrast == ContrastLevel.STANDARD,
                    enabled = contrastChoicesEnabled,
                    onClick = { onContrastSelected(ContrastLevel.STANDARD) },
                )
                ContrastChoice(
                    label = stringResource(R.string.contrast_high),
                    level = ContrastLevel.HIGH,
                    selected = contrast == ContrastLevel.HIGH,
                    enabled = contrastChoicesEnabled,
                    onClick = { onContrastSelected(ContrastLevel.HIGH) },
                )
            }
            when {
                contrastPreference.pending != null -> Text(
                    text = stringResource(R.string.preference_saving, contrastPreference.pending.displayName()),
                    modifier = Modifier.testTag("contrast_preference_loading"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                contrastPreference.readState == ContrastPreferenceReadState.Loading -> Text(
                    text = stringResource(R.string.preference_loading, stringResource(R.string.settings_contrast).lowercase()),
                    modifier = Modifier.testTag("contrast_preference_loading"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                contrastPreference.writeError -> {
                    Text(
                        text = stringResource(R.string.preference_save_failed, stringResource(R.string.settings_contrast)),
                        modifier = Modifier.testTag("contrast_preference_error"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    OutlinedButton(
                        onClick = onContrastRetry,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("contrast_preference_retry"),
                    ) {
                        Text(stringResource(R.string.preference_retry))
                    }
                }
                contrastPreference.readState == ContrastPreferenceReadState.Failed -> {
                    Text(
                        text = stringResource(R.string.preference_load_failed, stringResource(R.string.settings_contrast), contrast.displayName()),
                        modifier = Modifier.testTag("contrast_preference_error"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    OutlinedButton(
                        onClick = onContrastRetry,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("contrast_preference_retry"),
                    ) {
                        Text(stringResource(R.string.preference_retry))
                    }
                }
                else -> Text(
                    text = stringResource(R.string.preference_saved, stringResource(R.string.settings_contrast)),
                    modifier = Modifier.testTag("contrast_preference_saved"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
        }
        AppearanceValue(stringResource(R.string.settings_layout), stringResource(R.string.settings_layout_summary, layout.displayName()))
        AppearanceValue(stringResource(R.string.settings_effects), effects.displayName())
        Text(stringResource(R.string.settings_layout_mode))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LayoutChoice(
                label = stringResource(R.string.layout_simple),
                preset = LayoutPreset.SIMPLE,
                selected = layout == LayoutPreset.SIMPLE,
                enabled = layoutPreference.pending == null,
                onClick = { onLayoutSelected(LayoutPreset.SIMPLE) },
                modifier = Modifier.weight(1f),
            )
            LayoutChoice(
                label = stringResource(R.string.layout_standard),
                preset = LayoutPreset.STANDARD,
                selected = layout == LayoutPreset.STANDARD,
                enabled = layoutPreference.pending == null,
                onClick = { onLayoutSelected(LayoutPreset.STANDARD) },
                modifier = Modifier.weight(1f),
            )
        }
        when {
            layoutPreference.pending != null -> Text(
                text = stringResource(R.string.preference_saving, layoutPreference.pending.displayName()),
                modifier = Modifier.testTag("layout_preference_loading"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            layoutPreference.readState == LayoutPreferenceReadState.Loading -> Text(
                text = stringResource(R.string.preference_loading, stringResource(R.string.settings_layout).lowercase()),
                modifier = Modifier.testTag("layout_preference_loading"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            layoutPreference.writeError -> {
                Text(
                    text = stringResource(R.string.preference_save_failed, stringResource(R.string.settings_layout)),
                    modifier = Modifier.testTag("layout_preference_error"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                OutlinedButton(
                    onClick = onLayoutRetry,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("layout_preference_retry"),
                ) {
                    Text(stringResource(R.string.preference_retry))
                }
            }
            layoutPreference.readState == LayoutPreferenceReadState.Failed -> {
                Text(
                    text = stringResource(R.string.preference_load_failed, stringResource(R.string.settings_layout), layout.displayName()),
                    modifier = Modifier.testTag("layout_preference_error"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                OutlinedButton(
                    onClick = onLayoutRetry,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("layout_preference_retry"),
                ) {
                    Text(stringResource(R.string.preference_retry))
                }
            }
            layoutPreference.readState == LayoutPreferenceReadState.NotConfigured -> Text(
                text = stringResource(R.string.layout_session_only),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            else -> Text(
                text = stringResource(R.string.preference_saved, stringResource(R.string.settings_layout)),
                modifier = Modifier.testTag("layout_preference_saved"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }
        Text(stringResource(R.string.settings_effects_mode))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EffectsChoice(
                label = stringResource(R.string.effects_off),
                level = EffectsLevel.OFF,
                selected = preference.selectedForUi == EffectsLevel.OFF,
                enabled = preference.pending == null,
                onClick = { onEffectsSelected(EffectsLevel.OFF) },
                modifier = Modifier.weight(1f),
            )
            EffectsChoice(
                label = stringResource(R.string.effects_subtle),
                level = EffectsLevel.SUBTLE,
                selected = preference.selectedForUi == EffectsLevel.SUBTLE,
                enabled = preference.pending == null,
                onClick = { onEffectsSelected(EffectsLevel.SUBTLE) },
                modifier = Modifier.weight(1f),
            )
        }
        when {
            preference.readState == EffectsPreferenceReadState.Loading ->
                Text(stringResource(R.string.effects_restoring))
            preference.readState == EffectsPreferenceReadState.Failed -> {
                Text(stringResource(R.string.effects_read_failed))
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("settings-effects-retry"),
                ) {
                    Text(stringResource(R.string.preference_retry))
                }
            }
            preference.pending != null -> Text(stringResource(R.string.preference_saving, preference.pending.displayName()))
            preference.writeError -> Text(stringResource(R.string.effects_save_failed))
            !animationsEnabled && preference.confirmed != null && preference.confirmed != EffectsLevel.OFF ->
                Text(stringResource(R.string.effects_android_disabled))
            else -> Text(stringResource(R.string.effects_saved))
        }
    }
}

@Composable
private fun ThemeChoice(
    label: String,
    theme: OxygenThemeId,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(48.dp)
            .semantics {
                this.selected = selected
                if (!enabled) disabled()
            }
            .testTag("settings-theme-${theme.name.lowercase()}"),
    )
}

@Composable
private fun ContrastChoice(
    label: String,
    level: ContrastLevel,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(48.dp)
            .semantics {
                this.selected = selected
                if (!enabled) disabled()
            }
            .testTag("settings-contrast-${level.name.lowercase()}"),
    )
}

@Composable
private fun LayoutChoice(
    label: String,
    preset: LayoutPreset,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        modifier = modifier
            .requiredHeight(48.dp)
            .semantics {
                this.selected = selected
                if (!enabled) disabled()
            }
            .testTag("settings-layout-${preset.name.lowercase()}"),
    )
}

@Composable
private fun EffectsChoice(
    label: String,
    level: EffectsLevel,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .semantics { this.selected = selected }
            .testTag("settings-effects-${level.name.lowercase()}"),
    ) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            label = { Text(label) },
            modifier = Modifier.requiredHeight(48.dp),
        )
    }
}

@Composable
private fun AppearanceValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

private fun EffectsLevel.displayName(): String = when (this) {
    EffectsLevel.OFF -> "Off"
    EffectsLevel.SUBTLE -> "Subtle"
    EffectsLevel.FULL -> "Full"
}

private fun ContrastLevel.displayName(): String = when (this) {
    ContrastLevel.STANDARD -> "Standard"
    ContrastLevel.HIGH -> "High"
}

@Composable
private fun SettingsBottomActions(
    state: OxygenAppScreen.Settings,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings-bottom-actions"),
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("settings-back"),
        ) {
            Text(if (state.selectedDestination == null) "Back" else "Back to Settings")
        }
    }
}

@Composable
private fun AboutSectionView(section: AboutSection) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = section.heading,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        section.body.forEach { paragraph ->
            Text(
                text = paragraph,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
            )
        }
        section.links.forEach { link ->
            OutlinedButton(
                onClick = { uriHandler.openUri(link.uri) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = link.label }
                    .testTag("disclosure-link-${link.label.toTestTagSuffix()}"),
            ) {
                Text(link.label)
            }
        }
    }
}

private fun String.toTestTagSuffix(): String =
    lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')

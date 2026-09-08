package com.oxygen.weather.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oxygen.weather.app.AboutSection
import com.oxygen.weather.app.EffectsPreferencePresentationState
import com.oxygen.weather.app.EffectsPreferenceReadState
import com.oxygen.weather.app.OxygenAppScreen
import com.oxygen.weather.app.SettingsDestination
import com.oxygen.weather.app.UnitPreferenceMessage
import com.oxygen.weather.app.ui.theme.EffectsLevel
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
    effectsPreference: EffectsPreferencePresentationState = EffectsPreferencePresentationState.notConfigured(),
    animationsEnabled: Boolean = true,
    onEffectsPreferenceSelected: (EffectsLevel) -> Unit = {},
    onEffectsPreferenceRetry: () -> Unit = {},
    onLayoutSelected: (LayoutPreset) -> Unit = {},
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
                        preference = effectsPreference,
                        animationsEnabled = animationsEnabled,
                        onLayoutSelected = onLayoutSelected,
                        onEffectsSelected = onEffectsPreferenceSelected,
                        onRetry = onEffectsPreferenceRetry,
                    )
                    SettingsDestination.Units -> UnitPreferencesScreen(
                        selectedPreference = selectedUnitPreference,
                        message = unitPreferenceMessage,
                        onPreferenceSelected = onUnitPreferenceSelected,
                    )
                    SettingsDestination.Locations -> Text(
                        text = "Locations opens the saved-location surface.",
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
    SettingsGroup("Appearance", destinations, setOf(SettingsDestination.Appearance), onDestinationSelected)
    SettingsGroup("Weather", destinations, setOf(SettingsDestination.Units), onDestinationSelected)
    SettingsGroup("Places", destinations, setOf(SettingsDestination.Locations), onDestinationSelected)
    SettingsGroup(
        "Information",
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
    preference: EffectsPreferencePresentationState,
    animationsEnabled: Boolean,
    onLayoutSelected: (LayoutPreset) -> Unit,
    onEffectsSelected: (EffectsLevel) -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings-appearance-summary"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Effective appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        AppearanceValue("Theme", themeId.displayName)
        AppearanceValue("Layout", "${layout.displayName()} layout")
        AppearanceValue("Effects", effects.displayName())
        Text("Layout mode")
        Text(
            text = "This choice lasts until Oxygen is closed. Standard remains the launch default.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LayoutChoice(
                label = "Simple",
                preset = LayoutPreset.SIMPLE,
                selected = layout == LayoutPreset.SIMPLE,
                onClick = { onLayoutSelected(LayoutPreset.SIMPLE) },
                modifier = Modifier.weight(1f),
            )
            LayoutChoice(
                label = "Standard",
                preset = LayoutPreset.STANDARD,
                selected = layout == LayoutPreset.STANDARD,
                onClick = { onLayoutSelected(LayoutPreset.STANDARD) },
                modifier = Modifier.weight(1f),
            )
        }
        Text("Effects mode")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EffectsChoice(
                label = "Off",
                level = EffectsLevel.OFF,
                selected = preference.selectedForUi == EffectsLevel.OFF,
                enabled = preference.pending == null,
                onClick = { onEffectsSelected(EffectsLevel.OFF) },
                modifier = Modifier.weight(1f),
            )
            EffectsChoice(
                label = "Subtle",
                level = EffectsLevel.SUBTLE,
                selected = preference.selectedForUi == EffectsLevel.SUBTLE,
                enabled = preference.pending == null,
                onClick = { onEffectsSelected(EffectsLevel.SUBTLE) },
                modifier = Modifier.weight(1f),
            )
        }
        when {
            preference.readState == EffectsPreferenceReadState.Loading ->
                Text("Restoring your saved effects choice. Effects are temporarily Off.")
            preference.readState == EffectsPreferenceReadState.Failed -> {
                Text("Oxygen could not read the saved effects choice. Effects are temporarily Off.")
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("settings-effects-retry"),
                ) {
                    Text("Retry")
                }
            }
            preference.pending != null -> Text("Saving ${preference.pending.displayName()}...")
            preference.writeError -> Text("Oxygen could not save this choice. Choose it again to retry.")
            !animationsEnabled && preference.confirmed != null && preference.confirmed != EffectsLevel.OFF ->
                Text("Android animations are disabled, so effects are temporarily Off. Your saved choice is unchanged.")
            else -> Text("Your effects choice is saved on this device.")
        }
    }
}

@Composable
private fun LayoutChoice(
    label: String,
    preset: LayoutPreset,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier
            .heightIn(min = 48.dp)
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
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag("settings-effects-${level.name.lowercase()}"),
    )
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

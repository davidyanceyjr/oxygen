package com.oxygen.weather.app.ui.units

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.oxygen.weather.app.UnitPreferenceMessage
import com.oxygen.weather.core.model.UnitPreference
import com.oxygen.weather.core.model.UnitPreferencePreset

@Composable
fun UnitPreferencesScreen(
    selectedPreference: UnitPreference?,
    message: UnitPreferenceMessage?,
    onPreferenceSelected: (UnitPreference?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("unit-preferences"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Choose how weather units appear in Oxygen.",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
        )
        UnitPreferenceChoice(
            tag = "unit-choice-default",
            title = "Oxygen default",
            description = "Fahrenheit, km/h, hPa, mm, and km",
            selected = selectedPreference == null,
            onClick = { onPreferenceSelected(null) },
        )
        UnitPreferenceChoice(
            tag = "unit-choice-metric",
            title = "Metric",
            description = "Celsius, km/h, hPa, mm, and km",
            selected = selectedPreference == UnitPreference.Preset(UnitPreferencePreset.METRIC),
            onClick = { onPreferenceSelected(UnitPreference.Preset(UnitPreferencePreset.METRIC)) },
        )
        UnitPreferenceChoice(
            tag = "unit-choice-us",
            title = "US",
            description = "Fahrenheit, mph, inHg, inches, and miles",
            selected = selectedPreference == UnitPreference.Preset(UnitPreferencePreset.US),
            onClick = { onPreferenceSelected(UnitPreference.Preset(UnitPreferencePreset.US)) },
        )
        UnitPreferenceChoice(
            tag = "unit-choice-uk",
            title = "UK",
            description = "Celsius, mph, hPa, mm, and miles",
            selected = selectedPreference == UnitPreference.Preset(UnitPreferencePreset.UK),
            onClick = { onPreferenceSelected(UnitPreference.Preset(UnitPreferencePreset.UK)) },
        )
        message?.let {
            Text(
                text = it.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("unit-preference-error"),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun UnitPreferenceChoice(
    tag: String,
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { this.selected = selected }
            .testTag(tag)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }
    }
}

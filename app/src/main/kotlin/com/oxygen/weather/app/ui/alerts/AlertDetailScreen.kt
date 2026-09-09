package com.oxygen.weather.app.ui.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oxygen.weather.app.HomeAlertDetailPresentation
import com.oxygen.weather.app.OxygenAppScreen
import com.oxygen.weather.app.ui.components.GlassPanel
import com.oxygen.weather.app.ui.theme.LocalOxygenHomeDesign

@Composable
fun AlertDetailScreen(
    state: OxygenAppScreen.AlertDetail,
    onAlertSelected: (String) -> Unit,
    onBack: () -> Unit,
) {
    val selected = state.returnHome.forecast
        .let { it as? com.oxygen.weather.app.HomeForecastPresentationState.ForecastReady }
        ?.dashboard
        ?.alertDetails
        ?.firstOrNull { it.id == state.selectedAlertId }
    val details = (state.returnHome.forecast as? com.oxygen.weather.app.HomeForecastPresentationState.ForecastReady)
        ?.dashboard
        ?.alertDetails
        .orEmpty()
    val roles = LocalOxygenHomeDesign.current

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = roles.pageMarginHorizontal, vertical = roles.pageMarginVertical),
            verticalArrangement = Arrangement.spacedBy(roles.sectionGap),
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("alert-detail-back")
                    .semantics { contentDescription = "Back to weather" },
            ) {
                Text("Back to weather")
            }
            Text(
                text = "Official alert details",
                modifier = Modifier.testTag("alert-detail-title"),
                style = roles.sectionHeading,
            )

            if (details.size > 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alert-detail-selector"),
                    verticalArrangement = Arrangement.spacedBy(roles.tileGap),
                ) {
                    details.forEachIndexed { index, detail ->
                        AlertSelectorRow(
                            detail = detail,
                            selected = detail.id == state.selectedAlertId,
                            index = index,
                            onClick = { onAlertSelected(detail.id) },
                        )
                    }
                }
            }

            if (selected == null) {
                GlassPanel(modifier = Modifier.testTag("alert-detail-content")) {
                    Text("Alert unavailable", style = roles.sectionHeading)
                    Text("This official alert is no longer available.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                AlertDetailBody(detail = selected)
            }
        }
    }
}

@Composable
private fun AlertSelectorRow(
    detail: HomeAlertDetailPresentation,
    selected: Boolean,
    index: Int,
    onClick: () -> Unit,
) {
    val roles = LocalOxygenHomeDesign.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .testTag("alert-detail-selector-$index")
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .semantics {
                contentDescription = "${detail.event}, ${detail.severity}, ${detail.expires}. " +
                    if (selected) "Current alert" else "Select alert"
                this.selected = selected
            },
        color = if (selected) roles.strongGlassSurface else roles.ambientGlassSurface,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) roles.selectedBorderWidth else roles.normalBorderWidth,
            color = if (selected) roles.outlineStrong else roles.outlineQuiet,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(detail.event, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${detail.severity} · ${detail.expires}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = if (selected) "Current alert" else "Select alert",
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AlertDetailBody(detail: HomeAlertDetailPresentation) {
    val roles = LocalOxygenHomeDesign.current
    val uriHandler = LocalUriHandler.current

    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert-detail-content"),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Severity: ${detail.severity}", style = roles.sectionHeading, color = roles.warningContent)
            Text(detail.event, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            detail.headline?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
            AlertDetailField("Issuer", detail.issuer)
            AlertDetailField("Urgency", detail.urgency)
            AlertDetailField("Certainty", detail.certainty)
            AlertDetailField("Effective", detail.effective)
            AlertDetailField("Expires", detail.expires)
            detail.sent?.let { AlertDetailField("Sent", it) }
            detail.onset?.let { AlertDetailField("Onset", it) }
            detail.ends?.let { AlertDetailField("Ends", it) }
            AlertDetailField("Affected area", detail.affectedArea)
            AlertTextSection("Description", detail.description)
            AlertTextSection("Instructions", detail.instruction)
            Spacer(Modifier.heightIn(min = 4.dp))
            Text(detail.sourceCheckedAt, style = MaterialTheme.typography.bodySmall)
            Text(detail.attribution, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(
                onClick = { uriHandler.openUri(detail.sourceLink) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("alert-detail-source-link")
                    .semantics { contentDescription = detail.sourceLinkLabel },
            ) {
                Text("Open official alert source")
            }
        }
    }
}

@Composable
private fun AlertDetailField(label: String, value: String) {
    Text("$label: $value", style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun AlertTextSection(label: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

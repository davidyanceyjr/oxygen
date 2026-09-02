package com.oxygen.weather.app.ui.firstrun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.oxygen.weather.app.FirstRunLocationMessage
import com.oxygen.weather.app.ManualLocationCandidate
import com.oxygen.weather.app.ManualLocationSearchState
import com.oxygen.weather.app.OxygenAppScreen
import com.oxygen.weather.core.model.LocationId

@Composable
fun FirstRunLocationEntryScreen(
    state: OxygenAppScreen.FirstRunLocationEntry,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onCandidateSelected: (LocationId) -> Unit,
    onUseMyLocation: () -> Unit,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
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
                    .testTag("location-entry-content"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "OXYGEN",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Search for a city or place. Location permission is optional.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("location-entry-search-field"),
                    label = { Text(state.searchLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                )
                state.message?.let { message ->
                    FirstRunMessage(message)
                }
                ManualLocationSearchContent(
                    searchState = state.searchState,
                    retryLabel = state.retryLabel,
                    onRetry = onRetry,
                    onCandidateSelected = onCandidateSelected,
                )
                SearchDisclosure(
                    disclosure = state.geocodingDisclosure,
                    privacyNote = state.geocodingPrivacyNote,
                )
            }
            LocationEntryBottomActions(
                state = state,
                onSearch = onSearch,
                onUseMyLocation = onUseMyLocation,
                onBack = onBack,
                onOpenAbout = onOpenAbout,
            )
        }
    }
}

@Composable
private fun LocationEntryBottomActions(
    state: OxygenAppScreen.FirstRunLocationEntry,
    onSearch: () -> Unit,
    onUseMyLocation: () -> Unit,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("location-entry-actions"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Button(
            onClick = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("location-entry-search"),
        ) {
            Text(state.searchActionLabel)
        }
        OutlinedButton(
            onClick = onUseMyLocation,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("location-entry-use-my-location"),
        ) {
            Text(state.useMyLocationLabel)
        }
        OutlinedButton(
            onClick = onOpenAbout,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("location-entry-about"),
        ) {
            Text("Settings / About")
        }
        if (state.canReturnHome) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("location-entry-back"),
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun ManualLocationSearchContent(
    searchState: ManualLocationSearchState,
    retryLabel: String,
    onRetry: () -> Unit,
    onCandidateSelected: (LocationId) -> Unit,
) {
    when (searchState) {
        ManualLocationSearchState.Idle -> Unit
        is ManualLocationSearchState.Loading -> Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator()
            Text(
                text = "Searching for ${searchState.query}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        is ManualLocationSearchState.Results -> Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            searchState.candidates.forEach { candidate ->
                ManualLocationCandidateRow(
                    candidate = candidate,
                    onSelected = { onCandidateSelected(candidate.id) },
                )
            }
        }
        is ManualLocationSearchState.Empty -> SearchStatusWithRetry(
            text = "No results for ${searchState.query}.",
            canRetry = searchState.canRetry,
            retryLabel = retryLabel,
            onRetry = onRetry,
        )
        is ManualLocationSearchState.Failure -> SearchStatusWithRetry(
            text = searchState.message.text,
            canRetry = searchState.canRetry,
            retryLabel = retryLabel,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun ManualLocationCandidateRow(
    candidate: ManualLocationCandidate,
    onSelected: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = candidate.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = candidate.subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${candidate.coordinateText} | ${candidate.timezoneText}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            )
            Button(
                onClick = onSelected,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Select")
            }
        }
    }
}

@Composable
private fun SearchStatusWithRetry(
    text: String,
    canRetry: Boolean,
    retryLabel: String,
    onRetry: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f),
        )
        if (canRetry) {
            OutlinedButton(onClick = onRetry) {
                Text(retryLabel)
            }
        }
    }
}

@Composable
private fun FirstRunMessage(message: FirstRunLocationMessage) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    ) {
        Text(
            text = message.text,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SearchDisclosure(
    disclosure: String,
    privacyNote: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = disclosure,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
        )
        Text(
            text = privacyNote,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
        )
    }
}

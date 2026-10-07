package app.simetris.simapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.domain.model.ChecklistSummary
import app.simetris.simapp.ui.components.*
import app.simetris.simapp.ui.navigation.*
import app.simetris.simapp.ui.theme.SimAppTheme

@Composable
fun HomeScreen(vm: HomeViewModel, open: (AppRoute) -> Unit) {
    val state by vm.state.collectAsState()
    HomeContent(state, open)
}

@Composable
private fun HomeContent(state: HomeUiState, open: (AppRoute) -> Unit) {
    Column {
        AppTopBar(R.string.dashboard, onPreferences = { open(PreferencesRoute) })
        ScreenColumn {
            SectionCard {
                Text(stringResource(R.string.dashboard_overview), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.dashboard_title), style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryCount(state.total, R.string.registered_checklists, Modifier.weight(1f))
                    SummaryCount(state.finalized, R.string.finalized_checklists, Modifier.weight(1f))
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryCount(state.drafts, R.string.drafts_label, Modifier.weight(1f))
                    SummaryCount(state.pendingUploads, R.string.pending_sync_label, Modifier.weight(1f))
                }
            }
            SectionCard {
                Text(stringResource(R.string.recommended_action), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.create_checklist), style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Text(stringResource(if (state.modelsLoaded) R.string.checklist_action_description
                    else R.string.initial_load), style = MaterialTheme.typography.bodyMedium)
                PrimaryActionButton(R.string.create_checklist) { open(TemplatesRoute) }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.recent_checklists), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                TextButton(onClick = { open(HistoryRoute) }) { Text(stringResource(R.string.view_history)) }
            }
            if (state.loading) CircularProgressIndicator()
            else if (state.recent.isEmpty()) SectionCard { Text(stringResource(R.string.empty_history)) }
            else state.recent.forEach { summary -> RecentChecklistCard(summary) { open(ChecklistDetailRoute(summary.localId)) } }
        }
    }
}

@Composable
private fun SummaryCount(count: Int, label: Int, modifier: Modifier = Modifier) {
    Column(modifier) { Text(count.toString(), style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(label), style = MaterialTheme.typography.bodySmall) }
}

@Composable
private fun RecentChecklistCard(summary: ChecklistSummary, open: () -> Unit) {
    SectionCard(Modifier.clickable(onClick = open)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(summary.templateName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(summary.vehicleName ?: stringResource(R.string.vehicle_missing),
                    style = MaterialTheme.typography.bodySmall)
            }
            StatusBadge(statusText(summary.completionStatus),
                highlight = summary.completionStatus == "FINALIZADO")
        }
        Text(statusText(summary.uploadStatus),
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable private fun EmptyHomePreview() { SimAppTheme { HomeContent(HomeUiState(false, true), {}) } }

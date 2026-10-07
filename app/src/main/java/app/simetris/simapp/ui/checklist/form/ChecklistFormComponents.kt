package app.simetris.simapp.ui.checklist.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.ui.components.SearchInput
import app.simetris.simapp.ui.theme.DarkTeal
import app.simetris.simapp.ui.theme.LightTurquoise

internal data class SelectionOption(val id: Long, val title: String, val detail: String = "")

@Composable
internal fun StepBadge(step: Int) {
    Surface(color = LightTurquoise, shape = RoundedCornerShape(8.dp)) {
        Text(stringResource(R.string.step_of_three, step), color = DarkTeal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun SelectionField(label: String, value: String, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Icon(painterResource(R.drawable.ic_arrow_drop_down), contentDescription = null,
                    tint = DarkTeal)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectionSheet(
    title: String, search: String, onSearchChange: (String) -> Unit,
    options: List<SelectionOption>, initialSelection: SelectionOption?,
    onApply: (SelectionOption) -> Unit, onDismiss: () -> Unit,
) {
    var pendingSelection by remember { mutableStateOf(initialSelection) }
    val canApply = pendingSelection != null && options.any { it.id == pendingSelection?.id }
    LaunchedEffect(initialSelection?.id) {
        if (pendingSelection == null) pendingSelection = initialSelection
    }
    val sheetHeight = minOf(LocalConfiguration.current.screenHeightDp.dp * 0.78f, 560.dp)
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().height(sheetHeight).imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
            SearchInput(search, onSearchChange, stringResource(R.string.search),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (options.isEmpty()) item {
                    Text(stringResource(R.string.no_selection_options),
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(options, key = { it.id }) { option ->
                    val selected = pendingSelection?.id == option.id
                    Surface(Modifier.fillMaxWidth().clickable { pendingSelection = option },
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(if (selected) 2.dp else 1.dp,
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 1.dp) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 72.dp)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(option.title, style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold)
                                if (option.detail.isNotBlank()) Text(option.detail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            RadioButton(selected = selected, onClick = null)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.close))
                }
                Button(onClick = { pendingSelection?.let(onApply) }, shape = RoundedCornerShape(8.dp),
                    enabled = canApply, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.apply_selection))
                }
            }
        }
    }
}

package app.simetris.simapp.ui.checklist.list

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import app.simetris.simapp.R
import app.simetris.simapp.domain.model.ChecklistFilters
import app.simetris.simapp.domain.model.ChecklistSummary
import app.simetris.simapp.ui.components.*
import app.simetris.simapp.ui.theme.SimAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ChecklistHistoryScreen(vm: ChecklistHistoryViewModel, open: (String) -> Unit, create: () -> Unit) {
    val state by vm.state.collectAsState()
    val page = vm.pages.collectAsLazyPagingItems()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val activeFilters = state.filters.activeCount()

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        ModalDrawerSheet {
            HistoryFilterDrawer(state,
                onFiltersChange = vm::updateFilters,
                onDateRangeChange = { fromDate, toDate ->
                    vm.updateFilters { it.copy(fromDate = fromDate, toDate = toDate) }
                },
                onClear = {
                    vm.updateFilters { ChecklistFilters(search = it.search) }
                },
                onClose = { coroutineScope.launch { drawerState.close() } })
        }
    }) {
        Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(R.string.history_title)
            Column(Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SearchInput(state.filters.search,
                    onValueChange = { value -> vm.updateFilters { it.copy(search = value) } },
                    placeholder = stringResource(R.string.search_checklists),
                    onFilters = { coroutineScope.launch { drawerState.open() } },
                    activeFilters = activeFilters)
                Text(stringResource(R.string.total_results, state.total),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (page.loadState.refresh) {
                    is LoadState.Loading -> item { CircularProgressIndicator() }
                    is LoadState.Error -> item { SectionCard { Text(stringResource(R.string.history_error)) } }
                    else -> if (page.itemCount == 0) item { SectionCard { Text(stringResource(R.string.empty_history)) } }
                }
                items(page.itemCount) { index ->
                    page[index]?.let { HistoryCard(it) { open(it.localId) } }
                }
            }
        }
        FloatingActionButton(onClick = create, shape = RoundedCornerShape(12.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary) {
            Icon(painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.create_checklist),
                tint = MaterialTheme.colorScheme.onPrimary)
        }
        }
    }
}

private fun ChecklistFilters.activeCount(): Int = listOf(
    completion, upload, template, vehicle, result,
).count { it != null } + if (fromDate != null || toDate != null) 1 else 0

@Composable
private fun HistoryFilterDrawer(state: ChecklistHistoryUiState,
    onFiltersChange: ((ChecklistFilters) -> ChecklistFilters) -> Unit,
    onDateRangeChange: (Long?, Long?) -> Unit,
    onClear: () -> Unit, onClose: () -> Unit,
) {
    Column(Modifier.width(320.dp).fillMaxHeight()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.filters), style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            TextButton(onClick = onClear) { Text(stringResource(R.string.clear_filters)) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterDateRange(state.filters.fromDate, state.filters.toDate, onDateRangeChange)
            FilterSelector(stringResource(R.string.completion_filter), state.filters.completion,
                listOf(null, "RASCUNHO", "FINALIZADO"), { statusText(it ?: "") }) { selected ->
                onFiltersChange { it.copy(completion = selected) }
            }
            FilterSelector(stringResource(R.string.upload_filter), state.filters.upload,
                listOf(null, "PENDENTE", "ENVIANDO", "FALHOU", "CONFIRMADO"), { statusText(it ?: "") }) { selected ->
                onFiltersChange { it.copy(upload = selected) }
            }
            FilterSelector(stringResource(R.string.result_filter), state.filters.result,
                listOf(null, "CONFORME", "NAO_CONFORME"), { statusText(it ?: "") }) { selected ->
                onFiltersChange { it.copy(result = selected) }
            }
            val allLabel = stringResource(R.string.all)
            FilterSelector(stringResource(R.string.templates), state.filters.template?.toString(),
                listOf(null) + state.templates.map { it.remoteId.toString() },
                { value -> state.templates.firstOrNull { it.remoteId.toString() == value }?.name ?: allLabel }) { selected ->
                onFiltersChange { it.copy(template = selected?.toLongOrNull()) }
            }
            FilterSelector(stringResource(R.string.vehicle), state.filters.vehicle?.toString(),
                listOf(null) + state.vehicles.map { it.remoteId.toString() },
                { value -> state.vehicles.firstOrNull { it.remoteId.toString() == value }?.name ?: allLabel }) { selected ->
                onFiltersChange { it.copy(vehicle = selected?.toLongOrNull()) }
            }
        }
        Button(onClick = onClose, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(stringResource(R.string.show_results, state.total), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun HistoryCard(summary: ChecklistSummary, open: () -> Unit) {
    SectionCard(Modifier.clickable(onClick = open)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(summary.vehicleName ?: summary.templateName,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(summary.templateName, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusBadge(statusText(summary.completionStatus),
                highlight = summary.completionStatus == "FINALIZADO")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatDate(summary.recordedAtUtc), style = MaterialTheme.typography.bodySmall)
            Text(statusText(summary.uploadStatus), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSelector(title: String, current: String?, values: List<String?>,
    label: @Composable (String?) -> String, onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = if (current == null) stringResource(R.string.all) else label(current),
            onValueChange = {}, readOnly = true,
            label = { Text(title) }, singleLine = true, shape = RoundedCornerShape(8.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { value ->
                DropdownMenuItem(text = {
                    Text(if (value == null) stringResource(R.string.all) else label(value),
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }, onClick = { onSelect(value); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDateRange(fromDate: Long?, toDate: Long?,
    onDateRangeChange: (Long?, Long?) -> Unit,
) {
    var pickerOpen by remember { mutableStateOf(false) }
    val dateLabel = when {
        fromDate != null && toDate != null -> stringResource(R.string.date_range_value,
            formatLocalDay(fromDate), formatLocalDay(toDate))
        fromDate != null -> stringResource(R.string.date_range_start_only, formatLocalDay(fromDate))
        toDate != null -> stringResource(R.string.date_range_end_only, formatLocalDay(toDate))
        else -> stringResource(R.string.all_dates)
    }
    val title = stringResource(R.string.date_range)
    val pickDateLabel = stringResource(R.string.pick_date_range)
    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(value = dateLabel, onValueChange = {}, readOnly = true,
            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(8.dp),
            label = { Text(title) },
            trailingIcon = { Icon(painterResource(R.drawable.ic_calendar_today), contentDescription = null) })
        Box(Modifier.matchParentSize()
            .semantics { contentDescription = "$title: $dateLabel" }
            .clickable(role = Role.Button, onClickLabel = pickDateLabel) { pickerOpen = true })
    }
    if (pickerOpen) {
        val pickerState = remember(fromDate, toDate) {
            DateRangePickerState(locale = Locale.forLanguageTag("pt-BR"),
                initialSelectedStartDateMillis = fromDate?.let(::localDateToPickerUtc),
                initialSelectedEndDateMillis = if (fromDate == null) null else toDate?.let(::localDateToPickerUtc))
        }
        val context = LocalContext.current
        val configuration = LocalConfiguration.current
        val portugueseConfiguration = remember(configuration) {
            Configuration(configuration).apply { setLocale(Locale.forLanguageTag("pt-BR")) }
        }
        val portugueseContext = remember(context, portugueseConfiguration) {
            context.createConfigurationContext(portugueseConfiguration)
        }
        CompositionLocalProvider(LocalContext provides portugueseContext,
            LocalConfiguration provides portugueseConfiguration) {
            DatePickerDialog(onDismissRequest = { pickerOpen = false },
                confirmButton = {
                    TextButton(onClick = {
                        val start = pickerState.selectedStartDateMillis!!
                        val end = pickerState.selectedEndDateMillis!!
                        onDateRangeChange(pickerUtcToLocalDate(start, false),
                            pickerUtcToLocalDate(end, true))
                        pickerOpen = false
                    }, enabled = pickerState.selectedStartDateMillis != null &&
                        pickerState.selectedEndDateMillis != null) {
                        Text(stringResource(R.string.apply_date))
                    }
                },
                dismissButton = {
                    Row {
                        if (fromDate != null || toDate != null) {
                            TextButton(onClick = {
                                onDateRangeChange(null, null)
                                pickerOpen = false
                            }) { Text(stringResource(R.string.clear_date_range)) }
                        }
                        TextButton(onClick = { pickerOpen = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }) {
                DateRangePicker(state = pickerState, showModeToggle = false,
                    title = { Text(stringResource(R.string.pick_date_range), Modifier.padding(start = 24.dp, top = 16.dp)) },
                    headline = {
                        val start = pickerState.selectedStartDateMillis
                        val end = pickerState.selectedEndDateMillis
                        val headline = when {
                            start != null && end != null -> stringResource(R.string.date_range_value,
                                formatPickerUtcDay(start), formatPickerUtcDay(end))
                            start != null -> stringResource(R.string.date_range_start_only,
                                formatPickerUtcDay(start))
                            else -> stringResource(R.string.pick_date_range)
                        }
                        Text(headline, Modifier.padding(start = 24.dp, bottom = 16.dp),
                            maxLines = 2)
                    })
            }
        }
    }
}

private fun formatLocalDay(value: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).format(Date(value))

private fun formatPickerUtcDay(value: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(value))

private fun localDateToPickerUtc(value: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = value }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun pickerUtcToLocalDate(value: Long, endOfDay: Boolean): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = value }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
        if (endOfDay) add(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis - if (endOfDay) 1 else 0
}

@Preview(showBackground = true, widthDp = 360)
@Composable private fun EmptyHistoryPreview() {
    SimAppTheme { Column { AppTopBar(R.string.history_title)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SearchInput("", {}, stringResource(R.string.search_checklists))
            SectionCard { Text(stringResource(R.string.empty_history)) }
        }
    } }
}



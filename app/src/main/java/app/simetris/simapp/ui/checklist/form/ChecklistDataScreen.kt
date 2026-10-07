package app.simetris.simapp.ui.checklist.form

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.domain.MissingFieldType
import app.simetris.simapp.domain.model.AvailableDriver
import app.simetris.simapp.domain.model.AvailableTemplate
import app.simetris.simapp.domain.model.AvailableVehicle
import app.simetris.simapp.ui.components.*

private enum class SelectionKind { TEMPLATE, VEHICLE, DRIVER }

@Composable
fun ChecklistDataScreen(templateVm: TemplateViewModel, vm: ChecklistFormViewModel,
    id: String?, next: (String) -> Unit, saveDraft: (String) -> Unit, goBack: () -> Unit) {
    val templateState by templateVm.state.collectAsState()
    val creating by templateVm.creating.collectAsState()
    val formState = id?.let { vm.state(it).collectAsState().value }
    val checklist = formState?.checklist
    var selectedTemplate by remember(id) { mutableStateOf<AvailableTemplate?>(null) }
    var selectedVehicle by remember(id) { mutableStateOf<AvailableVehicle?>(null) }
    var selectedDriver by remember(id) { mutableStateOf<AvailableDriver?>(null) }
    var odometer by remember(id) { mutableStateOf("") }
    var note by remember(id) { mutableStateOf("") }
    var selectionSheet by remember { mutableStateOf<SelectionKind?>(null) }
    var search by remember { mutableStateOf("") }
    val vehicles by remember(search) { vm.vehicles(search) }.collectAsState(initial = emptyList())
    val drivers by remember(search) { vm.drivers(search) }.collectAsState(initial = emptyList())
    val focus by vm.dataFocus.collectAsState()
    val odometerFocus = remember { FocusRequester() }

    LaunchedEffect(checklist?.localId) {
        checklist?.let { odometer = it.odometerKm?.toString() ?: ""; note = it.note }
    }
    LaunchedEffect(focus) {
        if (id != null && focus?.first == id) {
            when (focus?.second) {
                MissingFieldType.VEHICLE -> { search = ""; selectionSheet = SelectionKind.VEHICLE }
                MissingFieldType.DRIVER -> { search = ""; selectionSheet = SelectionKind.DRIVER }
                MissingFieldType.ODOMETER -> odometerFocus.requestFocus()
                else -> Unit
            }
            vm.clearDataFocus()
        }
    }

    fun persist(advance: Boolean) {
        val afterward: (String) -> Unit = { checklistId ->
            if (advance) next(checklistId) else saveDraft(checklistId)
        }
        if (id == null) selectedTemplate?.let {
            templateVm.create(it.remoteId, selectedVehicle?.remoteId, selectedDriver?.remoteId,
                odometer.toLongOrNull(), note, afterward)
        } else vm.saveData(id, selectedVehicle, selectedDriver, odometer.toLongOrNull(), note) {
            afterward(id)
        }
    }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(R.string.launch_checklist)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.checklist_parameters),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    StepBadge(1)
                }
                if (id == null) SelectionField(stringResource(R.string.checklist_template),
                    selectedTemplate?.name ?: stringResource(R.string.select_template)) {
                    search = ""; selectionSheet = SelectionKind.TEMPLATE
                } else checklist?.let {
                    Text(it.templateName, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                }
                SelectionField(stringResource(R.string.vehicle),
                    selectedVehicle?.name ?: checklist?.vehicleName ?: stringResource(R.string.select_vehicle)) {
                    search = ""; selectionSheet = SelectionKind.VEHICLE
                }
                SelectionField(stringResource(R.string.driver),
                    selectedDriver?.name ?: checklist?.driverName ?: stringResource(R.string.select_driver)) {
                    search = ""; selectionSheet = SelectionKind.DRIVER
                }
                OutlinedTextField(odometer, { odometer = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.odometer)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp), singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(odometerFocus))
                OutlinedTextField(note, { note = it },
                    label = { Text(stringResource(R.string.note)) }, minLines = 4,
                    shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth())
            }
            if (id == null && templateState.templates.isEmpty()) SectionCard {
                Text(stringResource(if (templateState.initialLoadPending) R.string.initial_load
                    else R.string.no_templates))
            }
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val hasTemplate = id != null && checklist != null || id == null && selectedTemplate != null
                val editable = id == null || checklist?.completionStatus == "RASCUNHO"
                if (editable) {
                    PrimaryActionButton(R.string.continue_to_items,
                        enabled = !creating && hasTemplate &&
                            (selectedVehicle != null || checklist?.vehicleName != null) &&
                            (selectedDriver != null || checklist?.driverName != null) &&
                            odometer.toLongOrNull() != null) { persist(true) }
                    OutlinedButton(onClick = { persist(false) }, enabled = !creating && hasTemplate &&
                        (selectedVehicle != null || checklist?.vehicleName != null) &&
                        (selectedDriver != null || checklist?.driverName != null),
                        shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.save_draft))
                    }
                }
                BackButton(goBack)
            }
        }
    }

    selectionSheet?.let { kind ->
        val options = when (kind) {
            SelectionKind.TEMPLATE -> templateState.templates.filter {
                it.name.contains(search, ignoreCase = true) || it.description.contains(search, ignoreCase = true)
            }.map { SelectionOption(it.remoteId, it.name, it.description) }
            SelectionKind.VEHICLE -> vehicles.map { SelectionOption(it.remoteId, it.name) }
            SelectionKind.DRIVER -> drivers.map { SelectionOption(it.remoteId, it.name) }
        }
        val initialSelection = when (kind) {
            SelectionKind.TEMPLATE -> selectedTemplate?.let { SelectionOption(it.remoteId, it.name, it.description) }
            SelectionKind.VEHICLE -> (selectedVehicle ?:
                vehicles.firstOrNull { it.name == checklist?.vehicleName })?.let { SelectionOption(it.remoteId, it.name) }
            SelectionKind.DRIVER -> (selectedDriver ?:
                drivers.firstOrNull { it.name == checklist?.driverName })?.let { SelectionOption(it.remoteId, it.name) }
        }
        SelectionSheet(title = stringResource(when (kind) {
            SelectionKind.TEMPLATE -> R.string.select_template
            SelectionKind.VEHICLE -> R.string.select_vehicle
            SelectionKind.DRIVER -> R.string.select_driver
        }), search = search, onSearchChange = { search = it }, options = options,
            initialSelection = initialSelection,
            onApply = { option ->
                when (kind) {
                    SelectionKind.TEMPLATE -> selectedTemplate = templateState.templates.firstOrNull { it.remoteId == option.id }
                    SelectionKind.VEHICLE -> selectedVehicle = AvailableVehicle(option.id, option.title)
                    SelectionKind.DRIVER -> selectedDriver = AvailableDriver(option.id, option.title)
                }
                selectionSheet = null
            }, onDismiss = { selectionSheet = null })
    }
}

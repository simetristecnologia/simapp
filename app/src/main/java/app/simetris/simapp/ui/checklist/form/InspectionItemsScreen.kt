package app.simetris.simapp.ui.checklist.form

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.domain.Answer
import app.simetris.simapp.domain.ChecklistRules
import app.simetris.simapp.domain.MissingFieldType
import app.simetris.simapp.domain.model.ChecklistPhoto
import app.simetris.simapp.domain.model.InspectionItem
import app.simetris.simapp.media.picker.rememberPhotoPicker
import app.simetris.simapp.ui.components.*
import app.simetris.simapp.ui.theme.*

@Composable
fun InspectionItemsScreen(vm: ChecklistFormViewModel, id: String, next: () -> Unit,
    save: () -> Unit, openCamera: (String) -> Unit, goBack: () -> Unit) {
    val state by vm.state(id).collectAsState()
    val items = state.items
    val photos = state.photos
    val focus by vm.itemFocus.collectAsState()
    val savingAnswers by vm.savingAnswers.collectAsState()
    val listState = rememberLazyListState()
    LaunchedEffect(focus, items) {
        if (focus?.first == id) {
            val position = items.indexOfFirst { it.localId == focus?.second }
            if (position >= 0) { listState.animateScrollToItem(position + 2); vm.clearItemFocus() }
        }
    }
    var photoItemId by remember { mutableStateOf<String?>(null) }
    var expandedPhoto by remember { mutableStateOf<ChecklistPhoto?>(null) }
    val context = LocalContext.current
    val selectPhotos = rememberPhotoPicker { uris ->
        photoItemId?.let { itemId -> uris.forEach { uri ->
            vm.addPhoto(id, itemId, uri, context.contentResolver.getType(uri) ?: "application/octet-stream")
        } }
    }
    val conforming = items.count { it.answer == Answer.CONFORMING.storageValue }
    val nonconforming = items.count { it.answer == Answer.NONCONFORMING.storageValue }
    val pending = items.size - conforming - nonconforming
    val missingBeforeSignature = state.missingFields.count { it.type != MissingFieldType.SIGNATURE }
    val readyForSignature = state.checklist != null && items.isNotEmpty() &&
        ChecklistRules.readyForSignature(state.missingFields)

    Column(Modifier.fillMaxSize()) {
        AppTopBar(R.string.launch_checklist)
        LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), state = listState,
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                SectionCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.items_progress_title),
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        StepBadge(2)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.items_progress, conforming + nonconforming, items.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${if (items.isEmpty()) 0 else (conforming + nonconforming) * 100 / items.size}%",
                            color = DarkTeal, fontWeight = FontWeight.Bold)
                    }
                    ItemProgressBar(conforming, nonconforming, pending)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProgressCount(conforming, R.string.conforming_count, DarkTeal, Modifier.weight(1f))
                        ProgressCount(nonconforming, R.string.nonconforming_count, ErrorColor, Modifier.weight(1f))
                        ProgressCount(pending, R.string.pending_count, MutedTextColor, Modifier.weight(1f))
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.inspection_items),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.required_for_release),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(items, key = { it.localId }) { item ->
                InspectionItemCard(item, photos.filter { it.itemId == item.localId },
                    state.checklist?.completionStatus == "RASCUNHO",
                    respond = { answer, reason -> vm.saveAnswer(item, answer, reason) },
                    select = { photoItemId = item.localId; selectPhotos() },
                    takePhoto = { openCamera(item.localId) },
                    remove = vm::removePhoto, expand = { expandedPhoto = it })
            }
            item(key = "actions") {
                Column(Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (readyForSignature) PrimaryActionButton(R.string.sign,
                        enabled = savingAnswers == 0) { vm.proceedToSignature(id, next) }
                    else Text(stringResource(R.string.remaining_requirements, missingBeforeSignature),
                        modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = save, shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.save_draft))
                    }
                    BackButton(goBack)
                }
            }
        }
    }
    expandedPhoto?.let { photo -> AlertDialog(onDismissRequest = { expandedPhoto = null },
        confirmButton = { TextButton(onClick = { expandedPhoto = null }) { Text(stringResource(R.string.close)) } },
        text = { ImageThumbnail(photo.privatePath, Modifier.fillMaxWidth().height(340.dp)) }) }
}

@Composable
private fun ItemProgressBar(conforming: Int, nonconforming: Int, pending: Int) {
    val total = conforming + nonconforming + pending
    Row(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(20.dp))) {
        if (total == 0) Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        else {
            if (conforming > 0) Box(Modifier.weight(conforming.toFloat()).fillMaxHeight().background(DarkTeal))
            if (nonconforming > 0) Box(Modifier.weight(nonconforming.toFloat()).fillMaxHeight().background(ErrorColor))
            if (pending > 0) Box(Modifier.weight(pending.toFloat()).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant))
        }
    }
}

@Composable
private fun ProgressCount(count: Int, label: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier, color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), color = color, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
            Text(stringResource(label), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun InspectionItemCard(
    item: InspectionItem, photos: List<ChecklistPhoto>, editable: Boolean,
    respond: (Answer, String) -> Unit, select: () -> Unit, takePhoto: () -> Unit,
    remove: (ChecklistPhoto) -> Unit, expand: (ChecklistPhoto) -> Unit,
) {
    var reason by remember(item.localId) { mutableStateOf(item.reason) }
    val isConforming = item.answer == Answer.CONFORMING.storageValue
    val isNonconforming = item.answer == Answer.NONCONFORMING.storageValue
    SectionCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top) {
            Surface(color = if (isNonconforming) LightErrorColor else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(30.dp)) {
                Text(item.order.toString().padStart(2, '0'), Modifier.padding(7.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isNonconforming) ErrorColor else TextColor)
            }
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (item.description.isNotBlank()) Text(item.description,
                    style = MaterialTheme.typography.bodySmall, color = MutedTextColor)
            }
            StatusBadge(
                text = stringResource(when {
                    isConforming -> R.string.ok_status
                    isNonconforming -> R.string.attention_status
                    else -> R.string.pending
                }), highlight = isConforming, error = isNonconforming)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AnswerButton(stringResource(R.string.conforming), isConforming, DarkTeal, R.drawable.ic_check,
                Modifier.weight(1f), editable) { respond(Answer.CONFORMING, reason) }
            AnswerButton(stringResource(R.string.nonconforming), isNonconforming, ErrorColor, R.drawable.ic_close,
                Modifier.weight(1f), editable) { respond(Answer.NONCONFORMING, reason) }
        }
        if (isNonconforming) {
            NonconformityReasonField(reason, editable) {
                reason = it
                respond(Answer.NONCONFORMING, it)
            }
            PhotoEvidenceSection(photos, item.maxPhotos, editable,
                expand, remove, select, takePhoto)
        } else if (photos.isNotEmpty()) {
            PhotoStrip(photos, item.maxPhotos, editable, expand, remove, select)
        }
    }
}

@Composable
private fun AnswerButton(text: String, selected: Boolean, selectedColor: Color, selectedIcon: Int,
    modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.heightIn(min = 46.dp), enabled = enabled,
        shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)) {
        Icon(painterResource(if (selected) selectedIcon else R.drawable.ic_radio_unchecked),
            contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun NonconformingItemPreview() {
    SimAppTheme {
        InspectionItemCard(
            InspectionItem("item", "checklist", 3, "Palhetas e limpador de para-brisa",
                "Verificar desgaste e funcionamento", true, true, 1, 3,
                "NAO_CONFORME", "Borracha rasgada"),
            emptyList(), true, { _, _ -> }, {}, {}, {}, {})
    }
}

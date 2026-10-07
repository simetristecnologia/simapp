package app.simetris.simapp.ui.checklist.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.domain.model.ChecklistPhoto
import app.simetris.simapp.domain.model.ChecklistRun
import app.simetris.simapp.domain.model.InspectionItem
import app.simetris.simapp.ui.components.*
import app.simetris.simapp.ui.theme.DarkTeal
import app.simetris.simapp.ui.theme.ErrorColor
import app.simetris.simapp.ui.theme.LightErrorColor
import app.simetris.simapp.ui.theme.LightTurquoise
import app.simetris.simapp.ui.theme.MutedTextColor

@Composable
fun ChecklistDetailScreen(vm: ChecklistDetailViewModel, id: String, edit: () -> Unit, delete: () -> Unit, goBack: () -> Unit) {
    val state by vm.state(id).collectAsState()
    var confirm by remember { mutableStateOf(false) }
    var expandedPhoto by remember { mutableStateOf<String?>(null) }
    val conforming = state.items.filter { it.answer == "CONFORME" }
    val nonconforming = state.items.filter { it.answer == "NAO_CONFORME" }
    val pending = state.items.filter { it.answer != "CONFORME" && it.answer != "NAO_CONFORME" }

    Column {
        AppTopBar(R.string.detail)
        ScreenColumn {
            state.checklist?.let { checklist ->
                DetailSummary(checklist)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailCount(conforming.size, R.string.conforming_count, DarkTeal, Modifier.weight(1f))
                    DetailCount(nonconforming.size, R.string.nonconforming_count, ErrorColor, Modifier.weight(1f))
                    DetailCount(pending.size, R.string.pending_count, MutedTextColor, Modifier.weight(1f))
                }
                nonconforming.forEach { item ->
                    NonconformingDetail(item, state.photos.filter { it.itemId == item.localId }) {
                        expandedPhoto = it
                    }
                }
                if (conforming.isNotEmpty()) SectionCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.detail_approved_items, conforming.size),
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.detail_checked_count,
                            conforming.size + nonconforming.size, state.items.size),
                            style = MaterialTheme.typography.labelSmall, color = DarkTeal)
                    }
                    conforming.forEachIndexed { index, item ->
                        DetailItemRow(item, R.string.ok_status, DarkTeal)
                        DetailPhotos(state.photos.filter { it.itemId == item.localId }) {
                            expandedPhoto = it
                        }
                        if (index < conforming.lastIndex) HorizontalDivider()
                    }
                }
                if (pending.isNotEmpty()) SectionCard {
                    Text(stringResource(R.string.detail_pending_items, pending.size),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    pending.forEachIndexed { index, item ->
                        DetailItemRow(item, R.string.pending, MutedTextColor)
                        DetailPhotos(state.photos.filter { it.itemId == item.localId }) {
                            expandedPhoto = it
                        }
                        if (index < pending.lastIndex) HorizontalDivider()
                    }
                }
                SectionCard {
                    Text(stringResource(R.string.detail_signature), style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    val signature = state.signature
                    if (signature != null) {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)) {
                            ImageThumbnail(signature.privatePath,
                                Modifier.fillMaxWidth().height(132.dp).padding(8.dp),
                                label = R.string.signature)
                        }
                        Text(stringResource(R.string.detail_signed_at, formatDate(signature.recordedAtUtc)),
                            style = MaterialTheme.typography.bodySmall, color = MutedTextColor)
                    } else {
                        Text(stringResource(R.string.detail_signature_pending),
                            style = MaterialTheme.typography.bodyMedium, color = MutedTextColor)
                    }
                }
                if (checklist.completionStatus == "RASCUNHO") {
                    PrimaryActionButton(R.string.edit, onClick = edit)
                    if (!checklist.uploadAttempted) {
                        OutlinedButton(onClick = { confirm = true }, shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.delete))
                        }
                    }
                }
            }
            if (state.loading) CircularProgressIndicator()
            BackButton(goBack)
        }
    }
    expandedPhoto?.let { path -> AlertDialog(onDismissRequest = { expandedPhoto = null },
        confirmButton = { TextButton(onClick = { expandedPhoto = null }) { Text(stringResource(R.string.close)) } },
        text = { ImageThumbnail(path, Modifier.fillMaxWidth().height(340.dp)) }) }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false },
        title = { Text(stringResource(R.string.delete)) },
        text = { Text(stringResource(R.string.confirm_deletion)) },
        confirmButton = { TextButton(onClick = { confirm = false; vm.delete(id, delete) }) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } })
}

@Composable
private fun DetailSummary(checklist: ChecklistRun) {
    SectionCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.detail_checklist), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.detail_recorded_at, formatDate(checklist.recordedAtUtc)),
                    style = MaterialTheme.typography.bodySmall, color = MutedTextColor)
            }
            StatusBadge(statusText(checklist.completionStatus), checklist.completionStatus == "FINALIZADO")
        }
        if (checklist.completionStatus == "FINALIZADO" || checklist.completionStatus == "RASCUNHO") {
            StatusBadge(stringResource(R.string.detail_upload_status, statusText(checklist.uploadStatus)),
                checklist.synchronizedAtUtc != null, checklist.uploadStatus == "FALHOU")
        }
        HorizontalDivider()
        DetailField(R.string.checklist_template, checklist.templateName)
        DetailField(R.string.driver, checklist.driverName ?: stringResource(R.string.not_provided))
        DetailField(R.string.vehicle, checklist.vehicleName ?: stringResource(R.string.vehicle_missing))
        checklist.odometerKm?.let { DetailField(R.string.odometer, stringResource(R.string.detail_kilometers, it)) }
        if (checklist.note.isNotBlank()) DetailField(R.string.note, checklist.note)
    }
}

@Composable
private fun DetailField(label: Int, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top) {
        Text(stringResource(label), Modifier.width(88.dp),
            style = MaterialTheme.typography.labelSmall, color = MutedTextColor)
        Text(value, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DetailCount(count: Int, label: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp),
        shadowElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), color = color, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Text(stringResource(label), style = MaterialTheme.typography.labelSmall,
                color = MutedTextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun NonconformingDetail(item: InspectionItem, photos: List<ChecklistPhoto>, expand: (String) -> Unit) {
    SectionCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top) {
            Surface(color = LightErrorColor, shape = RoundedCornerShape(8.dp)) {
                Icon(painterResource(R.drawable.ic_warning), contentDescription = null,
                    modifier = Modifier.padding(4.dp).size(18.dp), tint = ErrorColor)
            }
            Text(item.name, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold)
            StatusBadge(stringResource(R.string.attention_status), error = true)
        }
        if (item.description.isNotBlank()) Text(item.description,
            style = MaterialTheme.typography.bodySmall, color = MutedTextColor)
        if (item.reason.isNotBlank()) {
            Text(stringResource(R.string.reason), style = MaterialTheme.typography.labelSmall,
                color = MutedTextColor)
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                Text(item.reason, Modifier.fillMaxWidth().padding(10.dp),
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
        DetailPhotos(photos, expand)
    }
}

@Composable
private fun DetailPhotos(photos: List<ChecklistPhoto>, expand: (String) -> Unit) {
    if (photos.isEmpty()) return
    Text(stringResource(R.string.photo_evidence), style = MaterialTheme.typography.labelSmall,
        color = MutedTextColor)
    photos.chunked(2).forEach { rowPhotos ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rowPhotos.forEach { photo ->
                ImageThumbnail(photo.privatePath,
                    Modifier.weight(1f).height(112.dp).clip(RoundedCornerShape(8.dp))
                        .clickable { expand(photo.privatePath) }, ContentScale.Crop)
            }
            if (rowPhotos.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun DetailItemRow(item: InspectionItem, status: Int, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Surface(color = if (status == R.string.ok_status) LightTurquoise else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)) {
            Icon(painterResource(if (status == R.string.ok_status) R.drawable.ic_check_circle
                else R.drawable.ic_radio_unchecked), contentDescription = null,
                modifier = Modifier.padding(3.dp).size(18.dp), tint = color)
        }
        Text(item.name, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(stringResource(status), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

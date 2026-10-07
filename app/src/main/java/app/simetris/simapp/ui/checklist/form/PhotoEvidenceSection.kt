package app.simetris.simapp.ui.checklist.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.simetris.simapp.R
import app.simetris.simapp.domain.model.ChecklistPhoto
import app.simetris.simapp.ui.components.ImageThumbnail
import app.simetris.simapp.ui.theme.MutedTextColor
import app.simetris.simapp.ui.theme.TextColor

private val EvidencePanelColor = Color(0xFFF3F2F2)

@Composable
fun PhotoEvidenceSection(photos: List<ChecklistPhoto>, maxPhotos: Int, editable: Boolean,
    expand: (ChecklistPhoto) -> Unit, remove: (ChecklistPhoto) -> Unit,
    select: () -> Unit, takePhoto: () -> Unit) {
    Surface(color = EvidencePanelColor, shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.fillMaxWidth().padding(9.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.attached_photos, photos.size, maxPhotos),
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                color = MutedTextColor)
            PhotoStrip(photos, maxPhotos, editable, expand, remove, select)
            if (editable && photos.size < maxPhotos) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PhotoActionButton(R.string.take_photo, R.drawable.ic_evidence_camera,
                        Modifier.weight(1f), takePhoto)
                    PhotoActionButton(R.string.gallery, R.drawable.ic_evidence_gallery,
                        Modifier.weight(1f), select)
                }
            }
        }
    }
}

@Composable
private fun PhotoActionButton(label: Int, icon: Int, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = 44.dp),
        color = Color.White, shape = RoundedCornerShape(9.dp)) {
        Row(Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), contentDescription = null,
                modifier = Modifier.size(21.dp), tint = TextColor)
            Text(stringResource(label), Modifier.padding(start = 7.dp),
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                color = TextColor, maxLines = 1)
        }
    }
}

@Composable
internal fun PhotoStrip(photos: List<ChecklistPhoto>, maxPhotos: Int, editable: Boolean,
    expand: (ChecklistPhoto) -> Unit, remove: (ChecklistPhoto) -> Unit, select: () -> Unit) {
    val removeDescription = stringResource(R.string.remove_photo)
    val addDescription = stringResource(R.string.add_photo)
    val borderColor = MutedTextColor
    Row(Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        photos.forEach { photo ->
            Box(Modifier.size(90.dp)) {
                ImageThumbnail(photo.privatePath,
                    Modifier.align(Alignment.BottomStart).size(82.dp)
                        .clip(RoundedCornerShape(9.dp)).clickable { expand(photo) },
                    ContentScale.Crop)
                if (editable) Box(
                    Modifier.align(Alignment.TopEnd).size(32.dp)
                        .clip(CircleShape).background(Color.Black.copy(alpha = 0.72f))
                        .clickable { remove(photo) }
                        .semantics { contentDescription = removeDescription },
                    contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
        if (editable && photos.size < maxPhotos) {
            Box(Modifier.size(82.dp)
                .clip(RoundedCornerShape(9.dp))
                .clickable(onClick = select)
                .semantics { contentDescription = addDescription }
                .drawBehind {
                    val stroke = 1.5.dp.toPx()
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size(size.width - stroke, size.height - stroke),
                        cornerRadius = CornerRadius(9.dp.toPx()),
                        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(5.dp.toPx(), 4.dp.toPx())))
                    )
                }, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Icon(painterResource(R.drawable.ic_evidence_add), contentDescription = null,
                        modifier = Modifier.size(23.dp), tint = TextColor)
                    Text(stringResource(R.string.extra_photo),
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                        color = MutedTextColor)
                }
            }
        }
    }
}

package app.simetris.simapp.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.domain.model.StorageSnapshot
import app.simetris.simapp.ui.components.SectionCard
import app.simetris.simapp.ui.theme.*
import java.util.Locale

@Composable
internal fun StorageCard(snapshot: StorageSnapshot?, clearing: Boolean, onClear: () -> Unit) {
    SectionCard {
        PreferencesCardHeader(R.drawable.ic_storage, R.string.local_storage)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.device_occupancy), style = MaterialTheme.typography.bodySmall,
                color = MutedTextColor)
            Text(if (snapshot == null) stringResource(R.string.loading)
                else stringResource(R.string.storage_fraction, storageSize(snapshot.occupiedBytes),
                    storageSize(snapshot.deviceBytes)),
                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        val fraction = snapshot?.let { if (it.deviceBytes > 0) (it.occupiedBytes.toFloat() / it.deviceBytes).coerceIn(0f, 1f) else 0f } ?: 0f
        BoxWithConstraints(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(6.dp))
            .background(BackgroundColor)) {
            if (fraction > 0f) Box(Modifier.width((maxWidth * fraction).coerceAtLeast(3.dp))
                .fillMaxHeight().background(DeepTeal))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StorageLegend(DeepTeal, R.string.checklist_storage,
                snapshot?.let { storageSize(it.checklistBytes) } ?: stringResource(R.string.loading))
            StorageLegend(Turquoise, R.string.media_storage,
                snapshot?.let { storageSize(it.mediaBytes) } ?: stringResource(R.string.loading))
        }
        Button(onClick = onClear, enabled = snapshot != null && !clearing,
            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BackgroundColor, contentColor = ErrorColor)) {
            Icon(painterResource(R.drawable.ic_clear_cache), contentDescription = null,
                modifier = Modifier.size(18.dp))
            Text(stringResource(if (clearing) R.string.clearing_cache else R.string.clear_cache),
                fontWeight = FontWeight.SemiBold)
        }
        Text(stringResource(R.string.cache_size, snapshot?.let { storageSize(it.cacheBytes) }
            ?: stringResource(R.string.loading)), style = MaterialTheme.typography.bodySmall,
            color = MutedTextColor)
    }
}

@Composable
private fun StorageLegend(color: androidx.compose.ui.graphics.Color, label: Int, size: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(color, RoundedCornerShape(50)))
        Text(stringResource(label, size), modifier = Modifier.padding(start = 5.dp),
            style = MaterialTheme.typography.labelSmall, color = MutedTextColor)
    }
}

@Composable
private fun storageSize(bytes: Long): String {
    val value = when {
        bytes >= 1_073_741_824L -> bytes / 1_073_741_824.0
        bytes >= 1_048_576L -> bytes / 1_048_576.0
        bytes >= 1_024L -> bytes / 1_024.0
        else -> return stringResource(R.string.storage_bytes, bytes)
    }
    val formatted = String.format(Locale.forLanguageTag("pt-BR"), "%.1f", value)
    return when {
        bytes >= 1_073_741_824L -> stringResource(R.string.storage_gigabytes, formatted)
        bytes >= 1_048_576L -> stringResource(R.string.storage_megabytes, formatted)
        else -> stringResource(R.string.storage_kilobytes, formatted)
    }
}

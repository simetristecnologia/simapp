package app.simetris.simapp.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.ui.components.SectionCard
import app.simetris.simapp.ui.theme.BackgroundColor
import app.simetris.simapp.ui.theme.MutedTextColor
import app.simetris.simapp.ui.theme.Turquoise
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun SynchronizationCard(state: PreferencesUiState, onSynchronize: () -> Unit) {
    val required = setOf("modelos", "veiculos", "motoristas")
    val catalogs = state.referenceStatuses.filter { it.type in required }
    val lastUpdate = catalogs.takeIf { it.size == required.size && it.all { status ->
        status.loaded && status.updatedAtUtc != null && status.error == null
    } }?.minOf { requireNotNull(it.updatedAtUtc) }
    SectionCard {
        PreferencesCardHeader(R.drawable.ic_sync, R.string.cloud_sync)
        Column(Modifier.fillMaxWidth().background(BackgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(if (lastUpdate == null) stringResource(R.string.demo_data_not_updated)
                else stringResource(R.string.demo_catalogs_updated,
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
                        Locale.forLanguageTag("pt-BR")).format(Date(lastUpdate))),
                style = MaterialTheme.typography.bodySmall, color = MutedTextColor)
            Text(stringResource(R.string.pending_uploads, state.pendingUploads),
                style = MaterialTheme.typography.bodySmall, color = MutedTextColor)
        }
        Button(onClick = onSynchronize, enabled = !state.syncing,
            modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)) {
            if (state.syncing) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Icon(painterResource(R.drawable.ic_sync), contentDescription = null,
                modifier = Modifier.padding(end = 8.dp).size(20.dp))
            Text(stringResource(if (state.syncing) R.string.synchronizing else R.string.sync_now),
                fontWeight = FontWeight.SemiBold)
        }
    }
}

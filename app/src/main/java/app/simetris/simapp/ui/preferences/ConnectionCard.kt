package app.simetris.simapp.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.ui.components.SectionCard
import app.simetris.simapp.ui.theme.MutedTextColor

@Composable
internal fun ConnectionCard(wifiOnly: Boolean, onWifiOnlyChange: (Boolean) -> Unit) {
    SectionCard {
        PreferencesCardHeader(R.drawable.ic_wifi, R.string.connection_and_offline)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.data_saver), style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.data_saver_description),
                    style = MaterialTheme.typography.bodySmall, color = MutedTextColor)
            }
            Switch(checked = wifiOnly, onCheckedChange = onWifiOnlyChange)
        }
    }
}

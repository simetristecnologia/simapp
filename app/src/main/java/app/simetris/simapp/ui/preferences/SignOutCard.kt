package app.simetris.simapp.ui.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.ui.components.SectionCard
import app.simetris.simapp.ui.theme.ErrorColor
import app.simetris.simapp.ui.theme.MutedTextColor

@Composable
internal fun SignOutCard(onSignOut: () -> Unit) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(painterResource(R.drawable.ic_warning), contentDescription = null,
                tint = ErrorColor, modifier = Modifier.size(24.dp))
            Text(stringResource(R.string.sign_out_section),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Text(stringResource(R.string.sign_out_description),
            style = MaterialTheme.typography.bodyMedium, color = MutedTextColor)
        Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)) {
            Icon(painterResource(R.drawable.ic_logout), contentDescription = null,
                modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.sign_out), fontWeight = FontWeight.SemiBold)
        }
    }
}

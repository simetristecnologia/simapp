package app.simetris.simapp.ui.preferences

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.simetris.simapp.ui.theme.DeepTeal
import app.simetris.simapp.ui.theme.LightTurquoise

@Composable
internal fun PreferencesCardHeader(@DrawableRes icon: Int, @StringRes title: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(30.dp).background(LightTurquoise, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = null,
                tint = DeepTeal, modifier = Modifier.size(20.dp))
        }
        Text(stringResource(title), modifier = Modifier.padding(start = 10.dp),
            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

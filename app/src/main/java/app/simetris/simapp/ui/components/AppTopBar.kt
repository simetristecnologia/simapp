package app.simetris.simapp.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R

val LocalGoHome = compositionLocalOf<(() -> Unit)?> { null }
val LocalOpenPreferences = compositionLocalOf<(() -> Unit)?> { null }
val LocalUserName = compositionLocalOf { "" }

@Composable
fun AppTopBar(@StringRes title: Int, onPreferences: (() -> Unit)? = null) {
    val goHome = LocalGoHome.current
    val openPreferences = onPreferences ?: LocalOpenPreferences.current
    val initial = LocalUserName.current.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val logo = painterResource(R.drawable.simapp_logo)
            val logoDescription = stringResource(R.string.app_name)
            if (goHome == null) Image(logo, contentDescription = logoDescription,
                modifier = Modifier.width(96.dp).height(27.dp))
            else Box(Modifier.width(96.dp).heightIn(min = 48.dp)
                .clickable(onClickLabel = stringResource(R.string.go_to_dashboard), onClick = goHome),
                contentAlignment = Alignment.Center) {
                Image(logo, contentDescription = logoDescription,
                    modifier = Modifier.width(96.dp).height(27.dp))
            }
            Text(stringResource(title), modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (openPreferences != null) Surface(
                modifier = Modifier.size(38.dp).clickable(
                    onClickLabel = stringResource(R.string.open_preferences), onClick = openPreferences),
                shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                Box(contentAlignment = Alignment.Center) {
                    Text(initial, color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

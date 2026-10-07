package app.simetris.simapp.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SectionTitle(@StringRes id: Int) {
    Text(stringResource(id), style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
}

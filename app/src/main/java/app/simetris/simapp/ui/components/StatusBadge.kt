package app.simetris.simapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.simetris.simapp.ui.theme.DarkTeal
import app.simetris.simapp.ui.theme.ErrorColor
import app.simetris.simapp.ui.theme.LightErrorColor
import app.simetris.simapp.ui.theme.LightTurquoise

@Composable
fun StatusBadge(text: String, highlight: Boolean = false, error: Boolean = false) {
    val background = if (error) LightErrorColor else if (highlight) LightTurquoise else MaterialTheme.colorScheme.surfaceVariant
    val color = if (error) ErrorColor else if (highlight) DarkTeal else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(color = background, shape = MaterialTheme.shapes.extraLarge) {
        Text(text, color = color, style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

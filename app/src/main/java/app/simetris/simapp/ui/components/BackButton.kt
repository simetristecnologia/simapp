package app.simetris.simapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.simetris.simapp.R
import androidx.compose.ui.unit.dp

@Composable
fun BackButton(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.back))
    }
}

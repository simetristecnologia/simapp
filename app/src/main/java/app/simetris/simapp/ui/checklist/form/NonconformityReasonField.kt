package app.simetris.simapp.ui.checklist.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.ui.theme.DarkTeal
import app.simetris.simapp.ui.theme.ErrorColor
import app.simetris.simapp.ui.theme.MutedTextColor
import app.simetris.simapp.ui.theme.TextColor

private val EvidencePanelColor = Color(0xFFF3F2F2)

@Composable
fun NonconformityReasonField(reason: String, editable: Boolean, onReasonChange: (String) -> Unit) {
    Surface(color = EvidencePanelColor, shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.fillMaxWidth().padding(9.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row {
                Text(stringResource(R.string.nonconformity_reason),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    color = TextColor)
                Text(" *", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = ErrorColor)
            }
            OutlinedTextField(reason, onReasonChange,
                enabled = editable,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(9.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextColor),
                placeholder = {
                    Text(stringResource(R.string.describe_nonconformity),
                        style = MaterialTheme.typography.bodyLarge, color = MutedTextColor)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedBorderColor = DarkTeal,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                ))
        }
    }
}

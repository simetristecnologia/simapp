package app.simetris.simapp.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R

@Composable
fun SearchInput(value: String, onValueChange: (String) -> Unit, placeholder: String,
    modifier: Modifier = Modifier, onFilters: (() -> Unit)? = null, activeFilters: Int = 0,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val voicePrompt = stringResource(R.string.voice_search_prompt)
    var voiceUnavailable by remember { mutableStateOf(false) }
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let(onValueChange)
        }
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_search), contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                BasicTextField(value = value, onValueChange = onValueChange,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                    decorationBox = { inner ->
                        Box {
                            if (value.isEmpty()) Text(placeholder,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            inner()
                        }
                    })
                IconButton(onClick = {
                    voiceUnavailable = false
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, voicePrompt)
                    }
                    runCatching { voiceLauncher.launch(intent) }.onFailure { voiceUnavailable = true }
                }) {
                    Icon(painterResource(R.drawable.ic_microphone),
                        contentDescription = stringResource(R.string.voice_search))
                }
                if (onFilters != null) IconButton(onClick = onFilters) {
                    BadgedBox(badge = {
                        if (activeFilters > 0) Badge { Text(activeFilters.toString()) }
                    }) {
                        Icon(painterResource(R.drawable.ic_filter_list),
                            contentDescription = stringResource(R.string.open_filters))
                    }
                }
            }
        }
        if (voiceUnavailable) Text(stringResource(R.string.voice_search_unavailable),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall)
    }
}

package app.simetris.simapp.ui.auth

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import app.simetris.simapp.R
import app.simetris.simapp.ui.theme.MutedTextColor

@Composable
fun LoginScreen(vm: LoginViewModel) {
    val state by vm.state.collectAsState()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    )

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Image(painterResource(R.drawable.simapp_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.align(Alignment.CenterHorizontally).width(220.dp).height(62.dp))
            Text(stringResource(R.string.login_description),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodySmall, color = MutedTextColor)
            Spacer(Modifier.height(8.dp))

            LoginField(R.string.login_company_code) {
                TextField(state.companyCode, vm::companyCode,
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    placeholder = { Text(stringResource(R.string.login_company_hint)) },
                    leadingIcon = { LoginIcon(R.drawable.ic_login_company) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next), singleLine = true,
                    shape = RoundedCornerShape(16.dp), colors = fieldColors)
            }
            LoginField(R.string.login_user) {
                TextField(state.login, vm::login,
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    placeholder = { Text(stringResource(R.string.login_user_hint)) },
                    leadingIcon = { LoginIcon(R.drawable.ic_login_user) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), singleLine = true,
                    shape = RoundedCornerShape(16.dp), colors = fieldColors)
            }
            LoginField(R.string.login_password) {
                TextField(state.password, vm::password,
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    placeholder = { Text(stringResource(R.string.login_password_hint)) },
                    leadingIcon = { LoginIcon(R.drawable.ic_login_lock) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(painterResource(if (passwordVisible) R.drawable.ic_login_visibility_off
                                else R.drawable.ic_login_visibility),
                                contentDescription = stringResource(if (passwordVisible)
                                    R.string.hide_password else R.string.show_password), tint = MutedTextColor)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { vm.submit() }),
                    singleLine = true, shape = RoundedCornerShape(16.dp), colors = fieldColors)
            }
            state.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall) }
            Button(onClick = vm::submit, enabled = !state.submitting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp)) {
                if (state.submitting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.login_action), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LoginField(@StringRes label: Int, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(stringResource(label).uppercase(), style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold, color = MutedTextColor)
        content()
    }
}

@Composable
private fun LoginIcon(@DrawableRes icon: Int) {
    Icon(painterResource(icon), contentDescription = null,
        tint = MutedTextColor, modifier = Modifier.size(24.dp))
}

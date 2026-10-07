package app.simetris.simapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.simetris.simapp.R
import app.simetris.simapp.data.remote.AuthHttpException
import app.simetris.simapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(LoginUiState())
    val state = mutableState.asStateFlow()

    fun companyCode(value: String) = mutableState.update { it.copy(companyCode = value.filter(Char::isDigit), error = null) }
    fun login(value: String) = mutableState.update { it.copy(login = value, error = null) }
    fun password(value: String) = mutableState.update { it.copy(password = value, error = null) }

    fun submit() {
        if (mutableState.value.submitting) return
        val form = mutableState.value
        if (form.companyCode.isBlank() || form.login.isBlank() || form.password.isBlank()) {
            mutableState.update { it.copy(error = R.string.login_required) }
            return
        }
        viewModelScope.launch {
            mutableState.update { it.copy(submitting = true, error = null) }
            try {
                auth.login(form.companyCode, form.login, form.password)
                mutableState.value = LoginUiState(companyCode = form.companyCode, login = form.login)
            } catch (error: Exception) {
                val message = when (error) {
                    is IllegalArgumentException -> R.string.login_invalid_code
                    is AuthHttpException -> if (error.statusCode == 401 || error.statusCode == 403)
                        R.string.login_invalid_credentials else R.string.login_failed
                    else -> R.string.login_connection_failed
                }
                mutableState.update { it.copy(error = message) }
            } finally { mutableState.update { it.copy(submitting = false) } }
        }
    }
}

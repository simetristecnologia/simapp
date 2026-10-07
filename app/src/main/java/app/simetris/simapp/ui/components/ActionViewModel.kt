package app.simetris.simapp.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.simetris.simapp.R
import kotlinx.coroutines.launch

abstract class ActionViewModel(private val messages: UiMessages) : ViewModel() {
    protected fun execute(action: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { action() }.onFailure { error ->
                messages.show(when (error.message) {
                    "Preenchimento incompleto" -> R.string.incomplete_checklist
                    "Limite de fotos atingido" -> R.string.photo_limit
                    "Imagem vazia ou maior que 15 MB" -> R.string.invalid_image_size
                    else -> R.string.operation_failed
                })
            }
        }
    }
}



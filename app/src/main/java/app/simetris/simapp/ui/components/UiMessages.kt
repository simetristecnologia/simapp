package app.simetris.simapp.ui.components

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class UiMessages {
    private val channel = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messages = channel.asSharedFlow()
    fun show(recurso: Int) { channel.tryEmit(recurso) }
}

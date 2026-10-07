package app.simetris.simapp.ui.checklist.detail

import androidx.lifecycle.viewModelScope
import app.simetris.simapp.domain.repository.PreferencesRepository
import app.simetris.simapp.domain.repository.ChecklistRepository
import app.simetris.simapp.ui.components.ActionViewModel
import app.simetris.simapp.ui.components.UiMessages
import app.simetris.simapp.ui.di.uiScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class ChecklistDetailViewModel(private val repository: ChecklistRepository, preferences: PreferencesRepository,
    messages: UiMessages,
) : ActionViewModel(messages) {
    private val scope = uiScope(preferences)
    private val states = mutableMapOf<String, StateFlow<ChecklistDetailUiState>>()
    fun state(id: String): StateFlow<ChecklistDetailUiState> = states.getOrPut(id) {
        scope.flatMapLatest { (_, profile) ->
            combine(repository.checklist(id, profile), repository.items(id, profile),
                repository.photos(id, profile), repository.signature(id, profile)) { checklist, items, photos, signature ->
                ChecklistDetailUiState(false, checklist, items, photos, signature)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChecklistDetailUiState())
    }
    fun delete(id: String, afterward: () -> Unit) = execute { repository.deleteDraft(id); afterward() }
}


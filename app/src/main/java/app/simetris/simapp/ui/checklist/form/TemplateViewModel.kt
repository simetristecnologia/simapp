package app.simetris.simapp.ui.checklist.form

import androidx.lifecycle.viewModelScope
import app.simetris.simapp.domain.repository.PreferencesRepository
import app.simetris.simapp.domain.repository.ChecklistRepository
import app.simetris.simapp.domain.repository.ReferenceDataRepository
import app.simetris.simapp.ui.components.ActionViewModel
import app.simetris.simapp.ui.components.UiMessages
import app.simetris.simapp.ui.di.uiScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class TemplateViewModel(private val repository: ChecklistRepository,
    referenceData: ReferenceDataRepository, preferences: PreferencesRepository,
    messages: UiMessages,
    private val scheduleUpload: suspend () -> Unit,
) : ActionViewModel(messages) {
    private val scope = uiScope(preferences)
    private val _creating = MutableStateFlow(false)
    val creating = _creating.asStateFlow()
    val state: StateFlow<TemplateSelectionUiState> = scope.flatMapLatest { (company, profile) ->
        combine(referenceData.templates(company, profile), referenceData.cursors(company, profile)) { templates, cursors ->
            TemplateSelectionUiState(false, cursors.none { it.type == "modelos" && it.loaded }, templates)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TemplateSelectionUiState())
    fun create(templateId: Long, vehicleId: Long?, driverId: Long?, odometer: Long?,
        note: String, onCreated: (String) -> Unit) {
        if (_creating.value) return
        _creating.value = true
        execute {
            try {
                val id = repository.create(templateId)
                try { repository.saveData(id, vehicleId, driverId, odometer, note) }
                catch (error: Exception) { repository.deleteDraft(id); throw error }
                scheduleUpload()
                onCreated(id)
            } finally {
                _creating.value = false
            }
        }
    }
}


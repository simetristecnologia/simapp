package app.simetris.simapp.ui.checklist.form

import android.net.Uri
import androidx.lifecycle.viewModelScope
import app.simetris.simapp.domain.repository.PreferencesRepository
import app.simetris.simapp.domain.repository.ChecklistRepository
import app.simetris.simapp.domain.repository.ReferenceDataRepository
import app.simetris.simapp.domain.*
import app.simetris.simapp.domain.model.*
import app.simetris.simapp.ui.components.ActionViewModel
import app.simetris.simapp.ui.components.UiMessages
import app.simetris.simapp.ui.di.uiScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class ChecklistFormViewModel(
    private val repository: ChecklistRepository,
    private val referenceData: ReferenceDataRepository,
    preferences: PreferencesRepository,
    messages: UiMessages,
    private val scheduleUpload: suspend () -> Unit,
) : ActionViewModel(messages) {
    private val scope = uiScope(preferences)
    private val states = mutableMapOf<String, StateFlow<ChecklistFormUiState>>()
    private val _itemFocus = MutableStateFlow<Pair<String, String>?>(null)
    val itemFocus = _itemFocus.asStateFlow()
    private val _dataFocus = MutableStateFlow<Pair<String, MissingFieldType>?>(null)
    val dataFocus = _dataFocus.asStateFlow()
    private val _savingAnswers = MutableStateFlow(0)
    val savingAnswers = _savingAnswers.asStateFlow()
    private val _finishing = MutableStateFlow(false)
    val finishing = _finishing.asStateFlow()

    fun state(id: String): StateFlow<ChecklistFormUiState> = states.getOrPut(id) {
        scope.flatMapLatest { (_, profile) ->
            combine(repository.checklist(id, profile), repository.items(id, profile),
                repository.photos(id, profile), repository.signature(id, profile)) { checklist, items, photos, signature ->
                val missingFields = checklist?.let { c ->
                    ChecklistRules.missingFields(c.vehicleName, c.driverName, c.odometerKm,
                        items.map { item -> ItemValidationData(item.name, Answer.fromStorage(item.answer),
                            item.reason, item.reasonRequired, item.photoRequired,
                            item.minPhotos, item.maxPhotos,
                            photos.count { it.itemId == item.localId }, item.localId) }, signature != null)
                }.orEmpty()
                ChecklistFormUiState(false, checklist, items, photos, signature, missingFields)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChecklistFormUiState())
    }

    fun vehicles(search: String) = scope.flatMapLatest { (company, profile) -> referenceData.vehicles(company, profile, search) }
    fun drivers(search: String) = scope.flatMapLatest { (company, profile) -> referenceData.drivers(company, profile, search) }
    fun focusItem(checklistId: String, itemId: String) { _itemFocus.value = checklistId to itemId }
    fun clearItemFocus() { _itemFocus.value = null }
    fun focusData(checklistId: String, type: MissingFieldType) { _dataFocus.value = checklistId to type }
    fun clearDataFocus() { _dataFocus.value = null }

    fun saveData(id: String, vehicle: AvailableVehicle?, driver: AvailableDriver?,
                    odometer: Long?, note: String, afterward: () -> Unit) = execute {
        repository.saveData(id, vehicle?.remoteId, driver?.remoteId, odometer, note)
        scheduleUpload()
        afterward()
    }
    fun saveAnswer(item: InspectionItem, answer: Answer, reason: String) {
        _savingAnswers.value += 1
        execute {
            try {
                repository.saveAnswer(item.checklistId, item.localId, answer, reason)
                scheduleUpload()
            } finally {
                _savingAnswers.value -= 1
            }
        }
    }
    fun proceedToSignature(id: String, afterward: () -> Unit) {
        if (_savingAnswers.value != 0) return
        execute {
            require(ChecklistRules.readyForSignature(repository.missingFields(id))) {
                "Preenchimento incompleto"
            }
            afterward()
        }
    }
    fun addPhoto(id: String, itemId: String, uri: Uri, mime: String) = execute {
        repository.addPhoto(id, itemId, uri, mime)
        scheduleUpload()
    }
    fun addPhotoBytes(id: String, itemId: String, bytes: ByteArray) = execute {
        repository.addPhotoBytes(id, itemId, bytes)
        scheduleUpload()
    }
    fun removePhoto(photo: ChecklistPhoto) = execute {
        repository.removePhoto(photo.checklistId, photo.localId)
        scheduleUpload()
    }
    fun finishWithSignature(id: String, bytes: ByteArray?, afterward: () -> Unit) {
        if (_finishing.value) return
        _finishing.value = true
        execute {
            try {
                if (bytes != null) repository.saveSignature(id, bytes)
                repository.finalizeChecklist(id)
                scheduleUpload()
                afterward()
            } finally {
                _finishing.value = false
            }
        }
    }
}


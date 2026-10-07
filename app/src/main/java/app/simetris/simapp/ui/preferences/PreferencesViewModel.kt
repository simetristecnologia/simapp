package app.simetris.simapp.ui.preferences

import androidx.lifecycle.viewModelScope
import app.simetris.simapp.R
import app.simetris.simapp.domain.model.StorageSnapshot
import app.simetris.simapp.domain.repository.PreferencesRepository
import app.simetris.simapp.domain.repository.ChecklistRepository
import app.simetris.simapp.domain.repository.ReferenceDataRepository
import app.simetris.simapp.domain.repository.StorageRepository
import app.simetris.simapp.ui.components.ActionViewModel
import app.simetris.simapp.ui.components.UiMessages
import app.simetris.simapp.ui.di.uiScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesViewModel(private val preferences: PreferencesRepository,
    repository: ChecklistRepository, referenceData: ReferenceDataRepository,
    private val storageRepository: StorageRepository,
    private val messages: UiMessages,
    private val refresh: suspend () -> Boolean,
    private val upload: suspend () -> Boolean,
    private val scheduleUpload: suspend () -> Unit,
    private val rescheduleUpload: suspend () -> Unit,
) : ActionViewModel(messages) {
    private val scope = uiScope(preferences)
    private val storage = MutableStateFlow<StorageSnapshot?>(null)
    private val syncing = MutableStateFlow(false)
    private val clearingCache = MutableStateFlow(false)
    private val base = combine(
        preferences.wifiOnlyUpload,
        scope.flatMapLatest { repository.pendingUploadTotal(it.second) },
        scope.flatMapLatest { referenceData.cursors(it.first, it.second) },
    ) { wifi, pendingUploads, referenceStatuses ->
        PreferencesUiState(false, wifi, pendingUploads, referenceStatuses)
    }
    val state: StateFlow<PreferencesUiState> = combine(base, storage, syncing, clearingCache) {
        current, snapshot, isSyncing, isClearing -> current.copy(
            storage = snapshot, syncing = isSyncing, clearingCache = isClearing)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PreferencesUiState())

    init { viewModelScope.launch { storage.value = storageRepository.snapshot() } }

    fun saveNetworkPreference(value: Boolean) = execute {
        preferences.saveNetworkPreference(value)
        rescheduleUpload()
    }
    fun synchronize() = execute {
        if (syncing.value) return@execute
        syncing.value = true
        try {
            val refreshed = refresh()
            val uploaded = upload()
            scheduleUpload()
            storage.value = storageRepository.snapshot()
            if (!refreshed) messages.show(R.string.reference_data_failure)
            if (!uploaded) messages.show(R.string.generic_upload_failure)
        } finally { syncing.value = false }
    }
    fun clearCache() = execute {
        if (clearingCache.value) return@execute
        clearingCache.value = true
        try { storage.value = storageRepository.clearCache() }
        finally { clearingCache.value = false }
    }
}


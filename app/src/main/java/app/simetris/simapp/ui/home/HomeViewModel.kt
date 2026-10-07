package app.simetris.simapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.simetris.simapp.domain.repository.PreferencesRepository
import app.simetris.simapp.domain.repository.ChecklistRepository
import app.simetris.simapp.domain.repository.ReferenceDataRepository
import app.simetris.simapp.domain.model.ChecklistFilters
import app.simetris.simapp.ui.di.uiScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(repository: ChecklistRepository, referenceData: ReferenceDataRepository,
    preferences: PreferencesRepository) : ViewModel() {
    private val scope = uiScope(preferences)
    private val counts = scope.flatMapLatest { (_, profile) ->
        combine(repository.draftTotal(profile), repository.pendingUploadTotal(profile),
            repository.count(profile, ChecklistFilters()),
            repository.count(profile, ChecklistFilters(completion = "FINALIZADO"))) {
                drafts, pendingUploads, total, finalized ->
            HomeCounts(drafts, pendingUploads, total, finalized)
        }
    }
    val state: StateFlow<HomeUiState> = combine(
        scope.flatMapLatest { referenceData.cursors(it.first, it.second) },
        counts,
        scope.flatMapLatest { repository.recent(it.second) },
    ) { cursors, counts, recent ->
        HomeUiState(false, cursors.any { it.type == "modelos" && it.loaded },
            counts.drafts, counts.pendingUploads, counts.total, counts.finalized, recent)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

private data class HomeCounts(val drafts: Int, val pendingUploads: Int, val total: Int, val finalized: Int)


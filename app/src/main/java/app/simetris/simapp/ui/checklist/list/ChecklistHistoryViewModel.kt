package app.simetris.simapp.ui.checklist.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import app.simetris.simapp.domain.repository.PreferencesRepository
import app.simetris.simapp.domain.repository.ChecklistRepository
import app.simetris.simapp.domain.repository.ReferenceDataRepository
import app.simetris.simapp.domain.model.ChecklistFilters
import app.simetris.simapp.ui.di.uiScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChecklistHistoryViewModel(private val repository: ChecklistRepository,
    referenceData: ReferenceDataRepository,
    private val preferences: PreferencesRepository,
) : ViewModel() {
    private val scope = uiScope(preferences)
    private val filters = MutableStateFlow(ChecklistFilters())
    val pages = combine(scope, filters) { current, filter -> current.second to filter }
        .flatMapLatest { (profile, filter) -> repository.page(profile, filter) }
        .cachedIn(viewModelScope)
    val state: StateFlow<ChecklistHistoryUiState> = combine(
        filters,
        scope.flatMapLatest { referenceData.templates(it.first, it.second) },
        scope.flatMapLatest { referenceData.vehicles(it.first, it.second, "") },
        combine(scope, filters) { current, filter -> current.second to filter }
            .flatMapLatest { (profile, filter) -> repository.count(profile, filter) },
    ) { filter, templates, vehicles, total ->
        ChecklistHistoryUiState(false, filter, total, templates, vehicles)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChecklistHistoryUiState())

    init {
        viewModelScope.launch {
            preferences.completionFilter.first()?.let { value ->
                filters.value = filters.value.copy(completion = value)
            }
        }
    }
    fun updateFilters(change: (ChecklistFilters) -> ChecklistFilters) {
        val previous = filters.value
        val value = change(previous)
        if (value == previous) return
        filters.value = value
        if (previous.completion != value.completion) viewModelScope.launch {
            preferences.saveFilter(value.completion)
        }
    }
}


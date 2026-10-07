package app.simetris.simapp.ui.checklist.list

import app.simetris.simapp.domain.model.*

data class ChecklistHistoryUiState(
    val loading: Boolean = true, val filters: ChecklistFilters = ChecklistFilters(),
    val total: Int = 0, val templates: List<AvailableTemplate> = emptyList(),
    val vehicles: List<AvailableVehicle> = emptyList(),
)

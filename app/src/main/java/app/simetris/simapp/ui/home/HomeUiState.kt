package app.simetris.simapp.ui.home

import app.simetris.simapp.domain.model.ChecklistSummary

data class HomeUiState(
    val loading: Boolean = true, val modelsLoaded: Boolean = false, val drafts: Int = 0,
    val pendingUploads: Int = 0, val total: Int = 0, val finalized: Int = 0,
    val recent: List<ChecklistSummary> = emptyList(),
)

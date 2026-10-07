package app.simetris.simapp.ui.checklist.detail

import app.simetris.simapp.domain.model.*

data class ChecklistDetailUiState(
    val loading: Boolean = true, val checklist: ChecklistRun? = null,
    val items: List<InspectionItem> = emptyList(), val photos: List<ChecklistPhoto> = emptyList(),
    val signature: ChecklistSignature? = null,
)

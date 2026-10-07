package app.simetris.simapp.ui.checklist.form

import app.simetris.simapp.domain.MissingField
import app.simetris.simapp.domain.model.*

data class ChecklistFormUiState(
    val loading: Boolean = true,
    val checklist: ChecklistRun? = null,
    val items: List<InspectionItem> = emptyList(),
    val photos: List<ChecklistPhoto> = emptyList(),
    val signature: ChecklistSignature? = null,
    val missingFields: List<MissingField> = emptyList(),
)

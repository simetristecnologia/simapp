package app.simetris.simapp.ui.checklist.form

import app.simetris.simapp.domain.model.AvailableTemplate

data class TemplateSelectionUiState(
    val loading: Boolean = true, val initialLoadPending: Boolean = true,
    val templates: List<AvailableTemplate> = emptyList(),
)

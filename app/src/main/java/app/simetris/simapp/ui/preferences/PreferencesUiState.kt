package app.simetris.simapp.ui.preferences

import app.simetris.simapp.domain.model.CatalogState
import app.simetris.simapp.domain.model.StorageSnapshot

data class PreferencesUiState(
    val loading: Boolean = true,
    val wifiOnly: Boolean = false, val pendingUploads: Int = 0,
    val referenceStatuses: List<CatalogState> = emptyList(),
    val storage: StorageSnapshot? = null,
    val syncing: Boolean = false,
    val clearingCache: Boolean = false,
)

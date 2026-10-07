package app.simetris.simapp.domain.model

data class ChecklistFilters(
    val search: String = "", val completion: String? = null, val upload: String? = null,
    val fromDate: Long? = null, val toDate: Long? = null, val template: Long? = null,
    val vehicle: Long? = null, val result: String? = null,
)


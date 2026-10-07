package app.simetris.simapp.domain.model

data class CatalogState(
    val type: String, val loaded: Boolean, val updatedAtUtc: Long?, val error: String?,
)


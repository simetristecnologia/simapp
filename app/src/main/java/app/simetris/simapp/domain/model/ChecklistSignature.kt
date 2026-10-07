package app.simetris.simapp.domain.model

data class ChecklistSignature(
    val localId: String, val checklistId: String, val privatePath: String,
    val recordedAtUtc: Long,
)


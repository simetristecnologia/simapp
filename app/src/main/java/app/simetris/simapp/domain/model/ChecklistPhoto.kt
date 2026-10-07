package app.simetris.simapp.domain.model

data class ChecklistPhoto(
    val localId: String, val checklistId: String, val itemId: String,
    val privatePath: String, val order: Int, val recordedAtUtc: Long,
)


package app.simetris.simapp.domain.model

data class PendingChecklist(
    val checklistId: String, val templateName: String, val stage: String,
    val attempts: Int, val error: String?, val updatedAtUtc: Long,
)


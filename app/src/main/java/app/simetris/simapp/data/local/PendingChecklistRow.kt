package app.simetris.simapp.data.local

import androidx.room.ColumnInfo

data class PendingChecklistRow(
    val checklistId: String,
    @ColumnInfo(name = "modeloNome") val templateName: String,
    @ColumnInfo(name = "etapa") val stage: String,
    @ColumnInfo(name = "tentativas") val attempts: Int,
    @ColumnInfo(name = "erro") val error: String?,
    @ColumnInfo(name = "atualizadoEmUtc") val updatedAtUtc: Long,
)

package app.simetris.simapp.data.local

import androidx.room.*

@Entity(tableName = "assinaturas", foreignKeys = [ForeignKey(entity = Checklist::class, parentColumns = ["idLocal"], childColumns = ["checklistId"], onDelete = ForeignKey.CASCADE)], indices = [Index(value = ["checklistId"], unique = true)])
data class Assinatura(
    @PrimaryKey val idLocal: String, val checklistId: String,
    val caminhoRelativo: String, val mime: String, val tamanhoBytes: Long, val sha256: String,
    val dataHoraUtc: Long, val perfilLocalId: String, val estadoEnvio: String = "PENDENTE",
)



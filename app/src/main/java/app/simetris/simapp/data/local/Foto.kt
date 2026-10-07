package app.simetris.simapp.data.local

import androidx.room.*

@Entity(tableName = "fotos", indices = [Index("checklistId"), Index("itemId")], foreignKeys = [ForeignKey(entity = Checklist::class, parentColumns = ["idLocal"], childColumns = ["checklistId"], onDelete = ForeignKey.CASCADE), ForeignKey(entity = ChecklistItem::class, parentColumns = ["idLocal"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE)])
data class Foto(
    @PrimaryKey val idLocal: String, val checklistId: String,
    val itemId: String, val caminhoRelativo: String, val ordem: Int, val mime: String,
    val tamanhoBytes: Long, val sha256: String, val dataHoraUtc: Long,
    val estadoEnvio: String = "PENDENTE",
)



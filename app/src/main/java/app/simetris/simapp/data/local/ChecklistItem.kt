package app.simetris.simapp.data.local

import androidx.room.*

@Entity(tableName = "checklist_itens", indices = [Index("checklistId", "ordem")], foreignKeys = [ForeignKey(entity = Checklist::class, parentColumns = ["idLocal"], childColumns = ["checklistId"], onDelete = ForeignKey.CASCADE)])
data class ChecklistItem(
    @PrimaryKey val idLocal: String, val checklistId: String,
    val ordem: Int, val nome: String, val descricao: String,
    val motivoObrigatorio: Boolean, val fotoObrigatorio: Boolean,
    val fotoQuantidadeMinima: Int, val fotoQuantidadeMaxima: Int,
    val resposta: String = "PENDENTE", val motivo: String = "",
)


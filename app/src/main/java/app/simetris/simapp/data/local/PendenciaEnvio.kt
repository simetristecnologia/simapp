package app.simetris.simapp.data.local

import androidx.room.*

@Entity(tableName = "pendencias", foreignKeys = [ForeignKey(entity = Checklist::class, parentColumns = ["idLocal"], childColumns = ["checklistId"], onDelete = ForeignKey.CASCADE)])
data class PendenciaEnvio(
    @PrimaryKey val checklistId: String, val etapa: String = "CHECKLIST", val tentativas: Int = 0,
    val erro: String? = null, val atualizadoEmUtc: Long = System.currentTimeMillis(),
)


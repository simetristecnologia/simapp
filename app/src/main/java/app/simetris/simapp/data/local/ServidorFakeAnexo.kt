package app.simetris.simapp.data.local

import androidx.room.*

@Entity(tableName = "servidor_fake_anexos", indices = [Index("checklistId")])
data class ServidorFakeAnexo(@PrimaryKey val idLocal: String, val checklistId: String, val recebido: Boolean = false)


package app.simetris.simapp.data.local

import androidx.room.*

@Entity(tableName = "servidor_fake_checklists")
data class ServidorFakeChecklist(@PrimaryKey val idLocal: String)


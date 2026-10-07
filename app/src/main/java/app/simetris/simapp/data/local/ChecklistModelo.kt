package app.simetris.simapp.data.local

import androidx.room.*

@Entity(tableName = "modelos", primaryKeys = ["empresaId", "perfilLocalId", "idServidor"], indices = [Index("empresaId", "perfilLocalId", "ativo")])
data class ChecklistModelo(
    val empresaId: String, val idServidor: Long, val versao: String, val nome: String,
    val descricao: String, val layout: String = "padrao", val ativo: Boolean = true,
    val perfilLocalId: String = "perfil-demonstracao", val motivoObrigatorio: Boolean = false,
    val fotoObrigatorio: Boolean = false, val fotoQuantidadeMinima: Int = 0,
    val fotoQuantidadeMaxima: Int = 0,
)


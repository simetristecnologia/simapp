package app.simetris.simapp.data.local

import androidx.room.*

@Entity(tableName = "modelo_itens", primaryKeys = ["empresaId", "perfilLocalId", "modeloId", "idServidor"], indices = [Index("empresaId", "perfilLocalId", "modeloId")])
data class ChecklistModeloItem(
    val empresaId: String, val modeloId: Long, val idServidor: Long, val ordem: Int,
    val nome: String, val descricao: String, val motivoObrigatorio: Boolean,
    val fotoObrigatorio: Boolean, val fotoQuantidadeMinima: Int, val fotoQuantidadeMaxima: Int,
    val perfilLocalId: String = "perfil-demonstracao",
)


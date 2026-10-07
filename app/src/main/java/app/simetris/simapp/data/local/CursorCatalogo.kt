package app.simetris.simapp.data.local

import androidx.room.*

@Entity(tableName = "cursores", primaryKeys = ["empresaId", "perfilLocalId", "tipo"])
data class CursorCatalogo(
    val empresaId: String, val tipo: String, val cursor: String?, val carregado: Boolean,
    val atualizadoEmUtc: Long?, val erro: String? = null,
    val perfilLocalId: String = "perfil-demonstracao",
)


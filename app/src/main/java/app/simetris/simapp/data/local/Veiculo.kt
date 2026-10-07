package app.simetris.simapp.data.local

import androidx.room.*

@Entity(tableName = "veiculos", primaryKeys = ["empresaId", "perfilLocalId", "idServidor"], indices = [Index("empresaId", "perfilLocalId", "ativo", "nome")])
data class Veiculo(val empresaId: String, val idServidor: Long, val nome: String, val ativo: Boolean = true,
    val perfilLocalId: String = "perfil-demonstracao")


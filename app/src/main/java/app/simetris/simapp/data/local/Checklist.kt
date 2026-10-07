package app.simetris.simapp.data.local

import androidx.room.*

@Entity(tableName = "checklists", indices = [Index("perfilLocalId", "dataHoraUtc"), Index("empresaId", "modeloIdOrigem"), Index("estadoPreenchimento", "estadoEnvio")])
data class Checklist(
    @PrimaryKey val idLocal: String,
    val empresaId: String, val perfilLocalId: String,
    val modeloIdOrigem: Long, val modeloVersaoOrigem: String,
    val modeloNome: String, val modeloLayout: String, val modeloDescricao: String = "",
    val dataHoraUtc: Long, val veiculoIdOrigem: Long? = null, val veiculoNome: String? = null,
    val motoristaIdOrigem: Long? = null, val motoristaNome: String? = null,
    val contadorKm: Long? = null, val observacao: String = "",
    val estadoPreenchimento: String = "RASCUNHO", val estadoEnvio: String = "PENDENTE",
    val resultado: String = "PENDENTE",
    val dataHoraSincronizado: Long? = null, val revisaoLocal: Long = 0,
    val criadoRemotamente: Boolean = false, val envioIniciado: Boolean = false,
    val motivoObrigatorio: Boolean = false, val fotoObrigatorio: Boolean = false,
    val fotoQuantidadeMinima: Int = 0, val fotoQuantidadeMaxima: Int = 0,
)


package app.simetris.simapp.data.repository

import app.simetris.simapp.data.local.*
import app.simetris.simapp.domain.model.*
import app.simetris.simapp.media.PrivateFiles

internal fun ChecklistModelo.toDomain() = AvailableTemplate(idServidor, nome, descricao)
internal fun Veiculo.toDomain() = AvailableVehicle(idServidor, nome)
internal fun Parceiro.toDomain() = AvailableDriver(idServidor, nome)
internal fun CursorCatalogo.toDomain() = CatalogState(tipo, carregado, atualizadoEmUtc, erro)
internal fun Checklist.toSummary() = ChecklistSummary(idLocal, modeloIdOrigem, modeloNome,
    veiculoIdOrigem, veiculoNome, motoristaNome, dataHoraUtc, estadoPreenchimento,
    displayUploadStatus(), resultado, contadorKm)
internal fun Checklist.toRun() = ChecklistRun(idLocal, modeloNome, dataHoraUtc,
    veiculoNome, motoristaNome, contadorKm, observacao, estadoPreenchimento, displayUploadStatus(),
    resultado, dataHoraSincronizado, envioIniciado)
private fun Checklist.displayUploadStatus() = when {
    dataHoraSincronizado != null -> "CONFIRMADO"
    estadoEnvio == "FALHOU" -> "FALHOU"
    estadoEnvio == "ENVIANDO" -> "ENVIANDO"
    else -> "PENDENTE"
}
internal fun ChecklistItem.toDomain() = InspectionItem(idLocal, checklistId, ordem, nome,
    descricao, motivoObrigatorio, fotoObrigatorio, fotoQuantidadeMinima,
    fotoQuantidadeMaxima, resposta, motivo)
internal fun Foto.toDomain(files: PrivateFiles) = ChecklistPhoto(idLocal,
    checklistId, itemId, files.file(caminhoRelativo).absolutePath, ordem, dataHoraUtc)
internal fun Assinatura.toDomain(files: PrivateFiles) = ChecklistSignature(idLocal,
    checklistId, files.file(caminhoRelativo).absolutePath, dataHoraUtc)
internal fun PendingChecklistRow.toDomain() = PendingChecklist(checklistId, templateName,
    stage, attempts, error, updatedAtUtc)


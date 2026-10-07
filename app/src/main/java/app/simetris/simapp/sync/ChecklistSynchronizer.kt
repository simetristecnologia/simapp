package app.simetris.simapp.sync

import android.util.Base64
import androidx.room.withTransaction
import app.simetris.simapp.data.local.*
import app.simetris.simapp.data.remote.*
import app.simetris.simapp.media.PrivateFiles
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChecklistSynchronizer(private val database: AppDatabase, private val files: PrivateFiles,
    private val api: ChecklistApi, private val identity: LocalIdentity) {
    private val profile = identity.localProfileId
    private val dao = database.checklistDao()
    private val uploadDao = database.pendingUploadDao()

    suspend fun synchronizePendingUploads(): Boolean {
        var success = true
        uploadDao.findAllPendingUploads(profile).forEach { pending ->
            try { if (!synchronize(pending.checklistId)) success = false }
            catch (_: Exception) {
                success = false
                database.withTransaction {
                    val current = uploadDao.findPendingUpload(pending.checklistId) ?: return@withTransaction
                    uploadDao.updatePendingUpload(current.copy(erro = "FALHA_ENVIO",
                        tentativas = current.tentativas + 1, atualizadoEmUtc = System.currentTimeMillis()))
                    dao.findChecklist(pending.checklistId)?.let {
                        if (it.dataHoraSincronizado == null) dao.updateChecklist(it.copy(estadoEnvio = "FALHOU"))
                    }
                }
            }
        }
        return success
    }

    private suspend fun synchronize(id: String): Boolean {
        val snapshot = database.withTransaction {
            val checklist = dao.findChecklist(id) ?: return@withTransaction null
            if (checklist.perfilLocalId != profile || checklist.empresaId != identity.companyId ||
                checklist.dataHoraSincronizado != null) return@withTransaction null
            dao.updateChecklist(checklist.copy(envioIniciado = true, estadoEnvio = "ENVIANDO"))
            Snapshot(checklist, dao.findItems(id), database.photoDao().findPhotos(id),
                database.signatureDao().findSignature(id))
        } ?: return true
        val checklist = snapshot.checklist
        val attachments = snapshot.photos.map { photo ->
            attachment(photo.idLocal, "FOTO", photo.itemId, photo.dataHoraUtc, photo.mime,
                photo.tamanhoBytes, photo.caminhoRelativo, photo.sha256)
        } + listOfNotNull(snapshot.signature?.let { signature ->
            attachment(signature.idLocal, "ASSINATURA", null, signature.dataHoraUtc,
                signature.mime, signature.tamanhoBytes, signature.caminhoRelativo, signature.sha256)
        })
        val payload = ChecklistPayload(checklist.idLocal, checklist.empresaId.toLong(), identity.userId,
            checklist.estadoPreenchimento, checklist.modeloNome, checklist.modeloDescricao,
            date(checklist.dataHoraUtc), null, checklist.modeloIdOrigem, checklist.modeloNome,
            requireNotNull(checklist.veiculoIdOrigem) { "Veículo obrigatório para envio" },
            requireNotNull(checklist.veiculoNome),
            requireNotNull(checklist.motoristaIdOrigem) { "Motorista obrigatório para envio" },
            requireNotNull(checklist.motoristaNome),
            checklist.motivoObrigatorio, checklist.fotoObrigatorio,
            checklist.fotoQuantidadeMinima, checklist.fotoQuantidadeMaxima,
            checklist.observacao.ifBlank { null },
            snapshot.items.map { item -> ChecklistItemPayload(item.idLocal, item.ordem,
                item.nome, item.descricao, item.resposta, item.motivo.ifBlank { null }) })
        if (checklist.criadoRemotamente) api.update(id, payload) else {
            api.insert(payload)
            database.withTransaction {
                dao.findChecklist(id)?.let { current ->
                    if (!current.criadoRemotamente) dao.updateChecklist(current.copy(criadoRemotamente = true))
                }
            }
        }
        uploadDao.findPendingUpload(id)?.let { uploadDao.updatePendingUpload(it.copy(etapa = "ANEXOS")) }
        api.sendAttachments(id, attachments)
        return database.withTransaction {
            val current = dao.findChecklist(id) ?: return@withTransaction true
            if (current.revisaoLocal == checklist.revisaoLocal) {
                dao.updateChecklist(current.copy(dataHoraSincronizado = System.currentTimeMillis(),
                    estadoEnvio = "CONFIRMADO"))
                uploadDao.deletePendingUpload(id)
                true
            } else {
                dao.updateChecklist(current.copy(estadoEnvio = "PENDENTE"))
                false
            }
        }
    }

    private suspend fun attachment(id: String, type: String, itemId: String?, recordedAt: Long,
        mime: String, size: Long, path: String, hash: String): ChecklistAttachmentPayload {
        val bytes = files.readValidated(path, hash)
        require(bytes.size.toLong() == size)
        return ChecklistAttachmentPayload(id, type, itemId, date(recordedAt),
            files.file(path).name, mime, size, Base64.encodeToString(bytes, Base64.NO_WRAP))
    }

    private fun date(value: Long): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(value))

    private data class Snapshot(val checklist: Checklist, val items: List<ChecklistItem>,
        val photos: List<Foto>, val signature: Assinatura?)
}

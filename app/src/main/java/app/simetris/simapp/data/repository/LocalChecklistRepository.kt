package app.simetris.simapp.data.repository

import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import androidx.room.withTransaction
import app.simetris.simapp.data.local.*
import app.simetris.simapp.data.remote.*
import app.simetris.simapp.domain.*
import app.simetris.simapp.domain.model.ChecklistFilters
import app.simetris.simapp.domain.repository.ChecklistRepository
import app.simetris.simapp.media.PrivateFiles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class LocalChecklistRepository(
    private val database: AppDatabase, private val files: PrivateFiles,
    private val identity: IdentityProvider,
) : ChecklistRepository {
    private val dao = database.checklistDao()
    private val modelDao = database.checklistModeloDao()
    private val vehicleDao = database.vehicleDao()
    private val driverDao = database.driverDao()
    private val photoDao = database.photoDao()
    private val signatureDao = database.signatureDao()
    private val uploadDao = database.pendingUploadDao()
    private val photoMutex = Mutex()
    private val answerMutex = Mutex()
    suspend fun scope(): LocalIdentity = identity.identity() ?: error("Perfil local indisponível")
    override fun checklist(id: String, profile: String) = dao.observeProfileChecklist(id, profile).map { it?.toRun() }
    override fun items(id: String, profile: String) = dao.observeProfileItems(id, profile).map { values -> values.map { it.toDomain() } }
    override fun photos(id: String, profile: String) = photoDao.observeProfilePhotos(id, profile).map { values -> values.map { it.toDomain(files) } }
    override fun signature(id: String, profile: String) = signatureDao.observeProfileSignature(id, profile).map { it?.toDomain(files) }
    override fun pendingUploads(profile: String) = uploadDao.observePendingUploadsWithTitle(profile).map { values -> values.map { it.toDomain() } }
    override fun recent(profile: String) = dao.observeRecent(profile).map { values -> values.map { it.toSummary() } }
    override fun draftTotal(profile: String) = dao.observeDraftTotal(profile)
    override fun pendingUploadTotal(profile: String) = uploadDao.observePendingUploadTotal(profile)
    override fun page(profile: String, filters: ChecklistFilters) = Pager(PagingConfig(pageSize = 20, enablePlaceholders = false)) {
        dao.pageChecklists(profile, filters.search, filters.completion, filters.upload,
            filters.fromDate, filters.toDate, filters.template, filters.vehicle, filters.result)
    }.flow.map { dataSet -> dataSet.map { it.toSummary() } }
    override fun count(profile: String, filters: ChecklistFilters) = dao.observeChecklistCount(profile, filters.search,
        filters.completion, filters.upload, filters.fromDate, filters.toDate, filters.template, filters.vehicle, filters.result)

    override suspend fun create(templateId: Long): String {
        val scope = scope()
        val template = modelDao.findTemplate(scope.companyId, scope.localProfileId, templateId) ?: error("Modelo indisponível")
        require(template.ativo) { "Modelo desativado" }
        val templateItems = modelDao.findTemplateItems(scope.companyId, scope.localProfileId, templateId)
        require(templateItems.isNotEmpty()) { "Modelo sem itens" }
        val id = UUID.randomUUID().toString()
        database.withTransaction {
            dao.insertChecklist(Checklist(idLocal = id, empresaId = scope.companyId,
                perfilLocalId = scope.localProfileId, modeloIdOrigem = template.idServidor,
                modeloVersaoOrigem = template.versao, modeloNome = template.nome,
                modeloLayout = template.layout, modeloDescricao = template.descricao,
                motivoObrigatorio = template.motivoObrigatorio,
                fotoObrigatorio = template.fotoObrigatorio,
                fotoQuantidadeMinima = template.fotoQuantidadeMinima,
                fotoQuantidadeMaxima = template.fotoQuantidadeMaxima,
                dataHoraUtc = System.currentTimeMillis()))
            dao.insertItems(templateItems.map { item -> ChecklistItem(
                idLocal = UUID.randomUUID().toString(), checklistId = id,
                ordem = item.ordem, nome = item.nome,
                descricao = item.descricao, motivoObrigatorio = item.motivoObrigatorio,
                fotoObrigatorio = item.fotoObrigatorio,
                fotoQuantidadeMinima = item.fotoQuantidadeMinima,
                fotoQuantidadeMaxima = item.fotoQuantidadeMaxima,
            ) })
        }
        return id
    }

    override suspend fun saveData(id: String, vehicleId: Long?, driverId: Long?, odometer: Long?, note: String) {
        require(odometer == null || odometer >= 0) { "Hodômetro inválido" }
        val current = findScopedChecklist(id)
        requireDraft(current)
        val vehicle = vehicleId?.let { vehicleDao.findVehicle(current.empresaId, current.perfilLocalId, it) ?: error("Veículo fora do catálogo ativo") }
        val driver = driverId?.let { driverDao.findDriver(current.empresaId, current.perfilLocalId, it) ?: error("Motorista fora do catálogo ativo") }
        require(vehicle == null || vehicle.ativo) { "Veículo fora do catálogo ativo" }
        require(driver == null || driver.ativo) { "Motorista fora do catálogo ativo" }
        require(vehicle != null || current.veiculoIdOrigem != null) { "Veículo obrigatório" }
        require(driver != null || current.motoristaIdOrigem != null) { "Motorista obrigatório" }
        database.withTransaction {
            val latest = findScopedChecklist(id)
            requireDraft(latest)
            dao.updateChecklist(latest.copy(
                veiculoIdOrigem = vehicle?.idServidor ?: latest.veiculoIdOrigem,
                veiculoNome = vehicle?.nome ?: latest.veiculoNome,
                motoristaIdOrigem = driver?.idServidor ?: latest.motoristaIdOrigem,
                motoristaNome = driver?.nome ?: latest.motoristaNome,
                contadorKm = odometer, observacao = note,
                dataHoraSincronizado = null, revisaoLocal = latest.revisaoLocal + 1,
                estadoEnvio = "PENDENTE",
            ))
            uploadDao.insertPendingUpload(PendenciaEnvio(id))
        }
    }

    override suspend fun saveAnswer(checklistId: String, itemId: String, answer: Answer, reason: String) = answerMutex.withLock {
        val current = findScopedChecklist(checklistId)
        requireDraft(current)
        database.withTransaction {
            val currentItem = dao.findItems(checklistId).firstOrNull { it.idLocal == itemId } ?: error("Item fora da checklist")
            dao.updateItem(currentItem.copy(resposta = answer.storageValue, motivo = reason))
            markDirty(checklistId)
        }
    }

    override suspend fun addPhoto(checklistId: String, itemId: String, uri: Uri, mime: String) = photoMutex.withLock {
        val checklist = findScopedChecklist(checklistId)
        requireDraft(checklist)
        val item = dao.findItems(checklistId).first { it.idLocal == itemId }
        val existing = photoDao.findPhotos(checklistId).filter { it.itemId == itemId }
        require(existing.size < item.fotoQuantidadeMaxima) { "Limite de fotos atingido" }
        val saved = files.copy(uri, mime)
        try {
            database.withTransaction {
                photoDao.insertPhoto(Foto(UUID.randomUUID().toString(), checklistId = checklistId, itemId = itemId,
                    caminhoRelativo = saved.relativePath, ordem = existing.size, mime = saved.mime,
                    tamanhoBytes = saved.sizeBytes, sha256 = saved.sha256, dataHoraUtc = System.currentTimeMillis()))
                markDirty(checklistId)
            }
        } catch (error: Exception) { files.delete(saved.relativePath); throw error }
    }

    override suspend fun addPhotoBytes(checklistId: String, itemId: String, bytes: ByteArray) = photoMutex.withLock {
        val checklist = findScopedChecklist(checklistId)
        requireDraft(checklist)
        val item = dao.findItems(checklistId).first { it.idLocal == itemId }
        val existing = photoDao.findPhotos(checklistId).filter { it.itemId == itemId }
        require(existing.size < item.fotoQuantidadeMaxima) { "Limite de fotos atingido" }
        val saved = files.save(bytes, "image/jpeg")
        try {
            database.withTransaction {
                photoDao.insertPhoto(Foto(UUID.randomUUID().toString(), checklistId = checklistId, itemId = itemId,
                    caminhoRelativo = saved.relativePath, ordem = existing.size, mime = saved.mime,
                    tamanhoBytes = saved.sizeBytes, sha256 = saved.sha256, dataHoraUtc = System.currentTimeMillis()))
                markDirty(checklistId)
            }
        } catch (error: Exception) { files.delete(saved.relativePath); throw error }
    }

    override suspend fun removePhoto(checklistId: String, photoId: String): Unit = photoMutex.withLock {
        requireDraft(findScopedChecklist(checklistId))
        val photo = photoDao.findPhoto(checklistId, photoId) ?: error("Foto não encontrada")
        database.withTransaction { photoDao.deletePhoto(photo.idLocal); markDirty(checklistId) }
        Unit
    }

    override suspend fun saveSignature(id: String, bytes: ByteArray) {
        val checklist = findScopedChecklist(id)
        requireDraft(checklist)
        val previous = signatureDao.findSignature(id)
        val saved = files.save(bytes, "image/png")
        try {
            database.withTransaction {
                signatureDao.saveSignature(Assinatura(UUID.randomUUID().toString(),
                    checklistId = id, caminhoRelativo = saved.relativePath, mime = saved.mime,
                    tamanhoBytes = saved.sizeBytes, sha256 = saved.sha256,
                    dataHoraUtc = System.currentTimeMillis(), perfilLocalId = checklist.perfilLocalId))
                markDirty(id)
            }
        } catch (error: Exception) { files.delete(saved.relativePath); throw error }
    }

    override suspend fun missingFields(id: String): List<MissingField> {
        val checklist = findScopedChecklist(id)
        val photos = photoDao.findPhotos(id)
        val items = dao.findItems(id).map { item -> ItemValidationData(item.nome, Answer.fromStorage(item.resposta),
            item.motivo, item.motivoObrigatorio, item.fotoObrigatorio, item.fotoQuantidadeMinima,
            item.fotoQuantidadeMaxima, photos.count { it.itemId == item.idLocal }, item.idLocal) }
        return ChecklistRules.missingFields(checklist.veiculoNome, checklist.motoristaNome,
            checklist.contadorKm, items, signatureDao.findSignature(id) != null)
    }

    override suspend fun finalizeChecklist(id: String): Unit = photoMutex.withLock {
        database.withTransaction {
            val current = findScopedChecklist(id)
            requireDraft(current)
            val missingFields = missingFields(id)
            require(missingFields.isEmpty()) { "Preenchimento incompleto" }
            val result = if (dao.findItems(id).any { it.resposta == "NAO_CONFORME" }) "NAO_CONFORME" else "CONFORME"
            dao.updateChecklist(current.copy(estadoPreenchimento = "FINALIZADO", resultado = result,
                dataHoraSincronizado = null, revisaoLocal = current.revisaoLocal + 1))
            uploadDao.insertPendingUpload(PendenciaEnvio(id))
        }
        Unit
    }

    override suspend fun deleteDraft(id: String) {
        findScopedChecklist(id)
        val paths = photoDao.findPhotos(id).map { it.caminhoRelativo } + listOfNotNull(signatureDao.findSignature(id)?.caminhoRelativo)
        require(dao.deleteDraft(id) == 1) { "Rascunho já enviado não pode ser excluído" }
        paths.forEach { files.delete(it) }
    }

    private fun requireDraft(checklist: Checklist) = require(checklist.estadoPreenchimento == "RASCUNHO") { "Checklist finalizada é somente leitura" }
    private suspend fun markDirty(id: String) {
        val current = dao.findChecklist(id) ?: return
        dao.updateChecklist(current.copy(dataHoraSincronizado = null, revisaoLocal = current.revisaoLocal + 1,
            estadoEnvio = "PENDENTE"))
        uploadDao.insertPendingUpload(PendenciaEnvio(id))
    }
    private suspend fun findScopedChecklist(id: String): Checklist {
        val checklist = dao.findChecklist(id) ?: error("Checklist não encontrada")
        val scope = scope()
        require(checklist.perfilLocalId == scope.localProfileId && checklist.empresaId == scope.companyId) { "Checklist fora do perfil atual" }
        return checklist
    }
}







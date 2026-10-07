package app.simetris.simapp.data.repository

import androidx.room.withTransaction
import app.simetris.simapp.data.local.*
import app.simetris.simapp.data.remote.ChecklistApi
import app.simetris.simapp.data.remote.LocalIdentity

class CatalogRepository(private val database: AppDatabase, private val api: ChecklistApi) {
    suspend fun refresh(identity: LocalIdentity): Boolean {
        val company = identity.companyId
        val profile = identity.localProfileId
        val cursorDao = database.catalogCursorDao()
        return try {
            val received = api.registrations(identity)
            require(received.templates.distinctBy { it.id }.size == received.templates.size)
            require(received.vehicles.distinctBy { it.id }.size == received.vehicles.size)
            require(received.drivers.distinctBy { it.id }.size == received.drivers.size)
            require(received.templates.all { template ->
                template.items.isNotEmpty() && template.items.distinctBy { it.id }.size == template.items.size &&
                    template.items.distinctBy { it.order }.size == template.items.size &&
                    template.minPhotos >= 0 && template.maxPhotos >= template.minPhotos
            })
            database.withTransaction {
                database.checklistModeloDao().apply {
                    deleteTemplateItems(company, profile)
                    deleteTemplates(company, profile)
                    saveTemplates(received.templates.map { ChecklistModelo(company, it.id, "", it.name,
                        it.description, perfilLocalId = profile, motivoObrigatorio = it.reasonRequired,
                        fotoObrigatorio = it.photoRequired, fotoQuantidadeMinima = it.minPhotos,
                        fotoQuantidadeMaxima = it.maxPhotos) })
                    saveTemplateItems(received.templates.flatMap { template -> template.items.map { item ->
                        ChecklistModeloItem(company, template.id, item.id, item.order, item.name,
                            item.description, template.reasonRequired, template.photoRequired,
                            template.minPhotos, template.maxPhotos, profile)
                    } })
                }
                database.vehicleDao().apply {
                    deleteVehicles(company, profile)
                    saveVehicles(received.vehicles.map { Veiculo(company, it.id, it.plate, perfilLocalId = profile) })
                }
                database.driverDao().apply {
                    deleteDrivers(company, profile)
                    saveDrivers(received.drivers.map { Parceiro(company, it.id, it.name, perfilLocalId = profile) })
                }
                listOf("modelos", "veiculos", "motoristas").forEach { type ->
                    cursorDao.saveCursor(CursorCatalogo(company, type, null, true,
                        System.currentTimeMillis(), perfilLocalId = profile))
                }
            }
            true
        } catch (_: Exception) {
            listOf("modelos", "veiculos", "motoristas").forEach { type ->
                val previous = cursorDao.findCursor(company, profile, type)
                cursorDao.saveCursor((previous ?: CursorCatalogo(company, type, null, false, null,
                    perfilLocalId = profile)).copy(erro = "FALHA_CATALOGO"))
            }
            false
        }
    }
}

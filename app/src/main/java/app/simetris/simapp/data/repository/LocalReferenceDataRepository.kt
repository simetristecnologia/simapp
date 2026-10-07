package app.simetris.simapp.data.repository

import app.simetris.simapp.data.local.AppDatabase
import app.simetris.simapp.domain.repository.ReferenceDataRepository
import kotlinx.coroutines.flow.map

class LocalReferenceDataRepository(database: AppDatabase) : ReferenceDataRepository {
    private val modelDao = database.checklistModeloDao()
    private val vehicleDao = database.vehicleDao()
    private val driverDao = database.driverDao()
    private val cursorDao = database.catalogCursorDao()

    override fun templates(company: String, profile: String) = modelDao.observeTemplates(company, profile)
        .map { values -> values.map { it.toDomain() } }

    override fun vehicles(company: String, profile: String, search: String) = vehicleDao.observeVehicles(company, profile, search)
        .map { values -> values.map { it.toDomain() } }

    override fun drivers(company: String, profile: String, search: String) = driverDao.observeDrivers(company, profile, search)
        .map { values -> values.map { it.toDomain() } }

    override fun cursors(company: String, profile: String) = cursorDao.observeCursors(company, profile)
        .map { values -> values.map { it.toDomain() } }
}

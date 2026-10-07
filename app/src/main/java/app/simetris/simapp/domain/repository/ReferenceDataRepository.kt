package app.simetris.simapp.domain.repository

import app.simetris.simapp.domain.model.AvailableDriver
import app.simetris.simapp.domain.model.AvailableTemplate
import app.simetris.simapp.domain.model.AvailableVehicle
import app.simetris.simapp.domain.model.CatalogState
import kotlinx.coroutines.flow.Flow

interface ReferenceDataRepository {
    fun templates(company: String, profile: String): Flow<List<AvailableTemplate>>
    fun vehicles(company: String, profile: String, search: String): Flow<List<AvailableVehicle>>
    fun drivers(company: String, profile: String, search: String): Flow<List<AvailableDriver>>
    fun cursors(company: String, profile: String): Flow<List<CatalogState>>
}

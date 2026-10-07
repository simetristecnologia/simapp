package app.simetris.simapp.domain.repository

import app.simetris.simapp.domain.model.StorageSnapshot

interface StorageRepository {
    suspend fun snapshot(): StorageSnapshot
    suspend fun clearCache(): StorageSnapshot
}

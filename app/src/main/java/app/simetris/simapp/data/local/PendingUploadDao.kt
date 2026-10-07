package app.simetris.simapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingUploadDao {
    @Query("SELECT p.* FROM pendencias p JOIN checklists c ON c.idLocal=p.checklistId WHERE c.perfilLocalId=:profile ORDER BY p.atualizadoEmUtc DESC")
    fun observePendingUploads(profile: String): Flow<List<PendenciaEnvio>>

    @Query("SELECT p.checklistId, c.modeloNome, p.etapa, p.tentativas, p.erro, p.atualizadoEmUtc FROM pendencias p JOIN checklists c ON c.idLocal=p.checklistId WHERE c.perfilLocalId=:profile ORDER BY p.atualizadoEmUtc DESC")
    fun observePendingUploadsWithTitle(profile: String): Flow<List<PendingChecklistRow>>

    @Query("SELECT * FROM pendencias WHERE checklistId=:id")
    suspend fun findPendingUpload(id: String): PendenciaEnvio?

    @Query("SELECT p.* FROM pendencias p JOIN checklists c ON c.idLocal=p.checklistId WHERE c.perfilLocalId=:profile ORDER BY p.atualizadoEmUtc ASC")
    suspend fun findAllPendingUploads(profile: String): List<PendenciaEnvio>

    @Query("SELECT COUNT(*) FROM pendencias p JOIN checklists c ON c.idLocal=p.checklistId WHERE c.perfilLocalId=:profile")
    fun observePendingUploadTotal(profile: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertPendingUpload(value: PendenciaEnvio): Long
    @Update suspend fun updatePendingUpload(value: PendenciaEnvio)
    @Query("DELETE FROM pendencias WHERE checklistId=:id") suspend fun deletePendingUpload(id: String)
}

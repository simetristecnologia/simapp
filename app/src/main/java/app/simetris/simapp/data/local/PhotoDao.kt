package app.simetris.simapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM fotos WHERE checklistId=:id ORDER BY itemId, ordem")
    fun observePhotos(id: String): Flow<List<Foto>>

    @Query("SELECT f.* FROM fotos f JOIN checklists c ON c.idLocal=f.checklistId WHERE f.checklistId=:id AND c.perfilLocalId=:profile ORDER BY f.itemId, f.ordem")
    fun observeProfilePhotos(id: String, profile: String): Flow<List<Foto>>

    @Query("SELECT * FROM fotos WHERE checklistId=:id ORDER BY itemId, ordem")
    suspend fun findPhotos(id: String): List<Foto>

    @Query("SELECT * FROM fotos WHERE idLocal=:id AND checklistId=:checklistId")
    suspend fun findPhoto(checklistId: String, id: String): Foto?

    @Update suspend fun updatePhoto(value: Foto)
    @Insert suspend fun insertPhoto(value: Foto)
    @Query("DELETE FROM fotos WHERE idLocal=:id") suspend fun deletePhoto(id: String)
}

package app.simetris.simapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SignatureDao {
    @Query("SELECT * FROM assinaturas WHERE checklistId=:id LIMIT 1")
    fun observeSignature(id: String): Flow<Assinatura?>

    @Query("SELECT a.* FROM assinaturas a JOIN checklists c ON c.idLocal=a.checklistId WHERE a.checklistId=:id AND c.perfilLocalId=:profile LIMIT 1")
    fun observeProfileSignature(id: String, profile: String): Flow<Assinatura?>

    @Query("SELECT * FROM assinaturas WHERE checklistId=:id LIMIT 1")
    suspend fun findSignature(id: String): Assinatura?

    @Update suspend fun updateSignature(value: Assinatura)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveSignature(value: Assinatura)
}

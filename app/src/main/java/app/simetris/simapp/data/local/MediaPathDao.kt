package app.simetris.simapp.data.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MediaPathDao {
    @Query("SELECT caminhoRelativo FROM fotos UNION SELECT caminhoRelativo FROM assinaturas")
    suspend fun usedPaths(): List<String>
}

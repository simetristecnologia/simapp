package app.simetris.simapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogCursorDao {
    @Query("SELECT * FROM cursores WHERE empresaId=:company AND perfilLocalId=:profile ORDER BY tipo")
    fun observeCursors(company: String, profile: String): Flow<List<CursorCatalogo>>

    @Query("SELECT * FROM cursores WHERE empresaId=:company AND perfilLocalId=:profile AND tipo=:type")
    suspend fun findCursor(company: String, profile: String, type: String): CursorCatalogo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCursor(value: CursorCatalogo)
}

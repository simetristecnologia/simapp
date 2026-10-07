package app.simetris.simapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {
    @Query("SELECT * FROM parceiros WHERE empresaId=:company AND perfilLocalId=:profile AND ativo=1 AND nome LIKE '%' || :search || '%' ORDER BY nome")
    fun observeDrivers(company: String, profile: String, search: String): Flow<List<Parceiro>>

    @Query("SELECT * FROM parceiros WHERE empresaId=:company AND perfilLocalId=:profile AND idServidor=:id")
    suspend fun findDriver(company: String, profile: String, id: Long): Parceiro?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDrivers(values: List<Parceiro>)

    @Query("DELETE FROM parceiros WHERE empresaId=:company AND perfilLocalId=:profile")
    suspend fun deleteDrivers(company: String, profile: String)
}

package app.simetris.simapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM veiculos WHERE empresaId=:company AND perfilLocalId=:profile AND ativo=1 AND nome LIKE '%' || :search || '%' ORDER BY nome")
    fun observeVehicles(company: String, profile: String, search: String): Flow<List<Veiculo>>

    @Query("SELECT * FROM veiculos WHERE empresaId=:company AND perfilLocalId=:profile AND idServidor=:id")
    suspend fun findVehicle(company: String, profile: String, id: Long): Veiculo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVehicles(values: List<Veiculo>)

    @Query("DELETE FROM veiculos WHERE empresaId=:company AND perfilLocalId=:profile")
    suspend fun deleteVehicles(company: String, profile: String)
}

package app.simetris.simapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistModeloDao {
    @Query("SELECT * FROM modelos WHERE empresaId=:company AND perfilLocalId=:profile AND ativo=1 ORDER BY nome")
    fun observeTemplates(company: String, profile: String): Flow<List<ChecklistModelo>>

    @Query("SELECT * FROM modelo_itens WHERE empresaId=:company AND perfilLocalId=:profile AND modeloId=:template ORDER BY ordem")
    suspend fun findTemplateItems(company: String, profile: String, template: Long): List<ChecklistModeloItem>

    @Query("SELECT * FROM modelos WHERE empresaId=:company AND perfilLocalId=:profile AND idServidor=:id")
    suspend fun findTemplate(company: String, profile: String, id: Long): ChecklistModelo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTemplates(values: List<ChecklistModelo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTemplateItems(values: List<ChecklistModeloItem>)

    @Query("DELETE FROM modelo_itens WHERE empresaId=:company AND perfilLocalId=:profile")
    suspend fun deleteTemplateItems(company: String, profile: String)

    @Query("DELETE FROM modelos WHERE empresaId=:company AND perfilLocalId=:profile")
    suspend fun deleteTemplates(company: String, profile: String)
}

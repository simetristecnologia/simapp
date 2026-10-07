package app.simetris.simapp.data.local

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklists WHERE idLocal=:id") fun observeChecklist(id: String): Flow<Checklist?>
    @Query("SELECT * FROM checklists WHERE idLocal=:id AND perfilLocalId=:profile") fun observeProfileChecklist(id: String, profile: String): Flow<Checklist?>
    @Query("SELECT * FROM checklists WHERE idLocal=:id") suspend fun findChecklist(id: String): Checklist?
    @Query("SELECT * FROM checklist_itens WHERE checklistId=:id ORDER BY ordem") fun observeItems(id: String): Flow<List<ChecklistItem>>
    @Query("SELECT i.* FROM checklist_itens i JOIN checklists c ON c.idLocal=i.checklistId WHERE i.checklistId=:id AND c.perfilLocalId=:profile ORDER BY i.ordem") fun observeProfileItems(id: String, profile: String): Flow<List<ChecklistItem>>
    @Query("SELECT * FROM checklist_itens WHERE checklistId=:id ORDER BY ordem") suspend fun findItems(id: String): List<ChecklistItem>
    @Query("SELECT COUNT(*) FROM checklists WHERE perfilLocalId=:profile AND estadoPreenchimento='RASCUNHO'") fun observeDraftTotal(profile: String): Flow<Int>
    @Query("SELECT * FROM checklists WHERE perfilLocalId=:profile ORDER BY dataHoraUtc DESC, idLocal DESC LIMIT 3") fun observeRecent(profile: String): Flow<List<Checklist>>

    @Query("""SELECT * FROM checklists WHERE perfilLocalId=:profile
        AND (:search='' OR modeloNome LIKE '%' || :search || '%' OR veiculoNome LIKE '%' || :search || '%' OR motoristaNome LIKE '%' || :search || '%')
        AND (:completion IS NULL OR estadoPreenchimento=:completion)
        AND (:upload IS NULL OR estadoEnvio=:upload)
        AND (:fromDate IS NULL OR dataHoraUtc>=:fromDate) AND (:toDate IS NULL OR dataHoraUtc<=:toDate)
        AND (:template IS NULL OR modeloIdOrigem=:template)
        AND (:vehicle IS NULL OR veiculoIdOrigem=:vehicle)
        AND (:result IS NULL OR resultado=:result)
        ORDER BY dataHoraUtc DESC, idLocal DESC""")
    fun pageChecklists(profile: String, search: String, completion: String?, upload: String?, fromDate: Long?, toDate: Long?, template: Long?, vehicle: Long?, result: String?): PagingSource<Int, Checklist>

    @Query("""SELECT COUNT(*) FROM checklists WHERE perfilLocalId=:profile
        AND (:search='' OR modeloNome LIKE '%' || :search || '%' OR veiculoNome LIKE '%' || :search || '%' OR motoristaNome LIKE '%' || :search || '%')
        AND (:completion IS NULL OR estadoPreenchimento=:completion)
        AND (:upload IS NULL OR estadoEnvio=:upload)
        AND (:fromDate IS NULL OR dataHoraUtc>=:fromDate) AND (:toDate IS NULL OR dataHoraUtc<=:toDate)
        AND (:template IS NULL OR modeloIdOrigem=:template)
        AND (:vehicle IS NULL OR veiculoIdOrigem=:vehicle)
        AND (:result IS NULL OR resultado=:result)""")
    fun observeChecklistCount(profile: String, search: String, completion: String?, upload: String?, fromDate: Long?, toDate: Long?, template: Long?, vehicle: Long?, result: String?): Flow<Int>

    @Insert suspend fun insertChecklist(value: Checklist)
    @Insert suspend fun insertItems(values: List<ChecklistItem>)
    @Update suspend fun updateChecklist(value: Checklist)
    @Update suspend fun updateItem(value: ChecklistItem)
    @Query("DELETE FROM checklists WHERE idLocal=:id AND estadoPreenchimento='RASCUNHO' AND envioIniciado=0") suspend fun deleteDraft(id: String): Int
    @Query("SELECT * FROM checklists WHERE perfilLocalId=:profile AND dataHoraSincronizado IS NULL ORDER BY dataHoraUtc")
    suspend fun findUnsynchronized(profile: String): List<Checklist>
}


package app.simetris.simapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FakeServerDao {
    @Query("SELECT * FROM servidor_fake_checklists WHERE idLocal=:id")
    suspend fun findFakeServerChecklist(id: String): ServidorFakeChecklist?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFakeServerChecklist(value: ServidorFakeChecklist)

    @Update suspend fun updateFakeServerChecklist(value: ServidorFakeChecklist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFakeAttachments(values: List<ServidorFakeAnexo>)

    @Query("SELECT * FROM servidor_fake_anexos WHERE checklistId=:id")
    suspend fun findFakeAttachments(id: String): List<ServidorFakeAnexo>

    @Query("DELETE FROM servidor_fake_anexos WHERE checklistId=:id")
    suspend fun deleteFakeAttachments(id: String)

    @Query("UPDATE servidor_fake_anexos SET recebido=1 WHERE checklistId=:checklist AND idLocal=:attachmentId")
    suspend fun receiveFakeAttachment(checklist: String, attachmentId: String): Int
}

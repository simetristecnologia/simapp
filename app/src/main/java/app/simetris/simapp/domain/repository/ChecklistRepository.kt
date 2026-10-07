package app.simetris.simapp.domain.repository

import android.net.Uri
import androidx.paging.PagingData
import app.simetris.simapp.domain.Answer
import app.simetris.simapp.domain.MissingField
import app.simetris.simapp.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ChecklistRepository {
    fun checklist(id: String, profile: String): Flow<ChecklistRun?>
    fun items(id: String, profile: String): Flow<List<InspectionItem>>
    fun photos(id: String, profile: String): Flow<List<ChecklistPhoto>>
    fun signature(id: String, profile: String): Flow<ChecklistSignature?>
    fun pendingUploads(profile: String): Flow<List<PendingChecklist>>
    fun recent(profile: String): Flow<List<ChecklistSummary>>
    fun draftTotal(profile: String): Flow<Int>
    fun pendingUploadTotal(profile: String): Flow<Int>
    fun page(profile: String, filters: ChecklistFilters): Flow<PagingData<ChecklistSummary>>
    fun count(profile: String, filters: ChecklistFilters): Flow<Int>
    suspend fun create(templateId: Long): String
    suspend fun saveData(id: String, vehicleId: Long?, driverId: Long?, odometer: Long?, note: String)
    suspend fun saveAnswer(checklistId: String, itemId: String, answer: Answer, reason: String)
    suspend fun missingFields(id: String): List<MissingField>
    suspend fun addPhoto(checklistId: String, itemId: String, uri: Uri, mime: String)
    suspend fun addPhotoBytes(checklistId: String, itemId: String, bytes: ByteArray)
    suspend fun removePhoto(checklistId: String, photoId: String)
    suspend fun saveSignature(id: String, bytes: ByteArray)
    suspend fun finalizeChecklist(id: String)
    suspend fun deleteDraft(id: String)
}

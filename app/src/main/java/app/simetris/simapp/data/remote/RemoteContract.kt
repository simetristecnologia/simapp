package app.simetris.simapp.data.remote

data class LocalIdentity(val companyId: String, val localProfileId: String, val userId: Long)

interface IdentityProvider {
    suspend fun identity(): LocalIdentity?
    suspend fun credential(): String?
}

data class RemoteDriver(val id: Long, val name: String)
data class RemoteVehicle(val id: Long, val plate: String)
data class RemoteTemplateItem(val id: Long, val order: Int, val name: String, val description: String)
data class RemoteTemplate(val id: Long, val name: String, val description: String,
    val items: List<RemoteTemplateItem>, val reasonRequired: Boolean, val photoRequired: Boolean,
    val minPhotos: Int, val maxPhotos: Int)
data class Registrations(val drivers: List<RemoteDriver>, val vehicles: List<RemoteVehicle>,
    val templates: List<RemoteTemplate>)

data class ChecklistItemPayload(val id: String, val order: Int, val name: String,
    val description: String, val answer: String, val reason: String?)
data class ChecklistAttachmentPayload(val id: String, val type: String, val itemId: String?,
    val dateTime: String, val fileName: String, val fileType: String,
    val fileSize: Long, val file: String)
data class ChecklistPayload(val id: String, val companyId: Long, val userId: Long,
    val situation: String, val name: String, val description: String,
    val dateTime: String, val synchronizedAt: String?, val templateId: Long,
    val templateName: String, val vehicleId: Long, val vehiclePlate: String,
    val driverId: Long, val driverName: String, val reasonRequired: Boolean,
    val photoRequired: Boolean, val minPhotos: Int, val maxPhotos: Int,
    val note: String?, val items: List<ChecklistItemPayload>,
    val attachments: List<ChecklistAttachmentPayload> = emptyList())

interface ChecklistApi {
    suspend fun registrations(identity: LocalIdentity): Registrations
    suspend fun insert(checklist: ChecklistPayload)
    suspend fun update(id: String, checklist: ChecklistPayload)
    suspend fun sendAttachments(id: String, attachments: List<ChecklistAttachmentPayload>)
}

class ApiNotConfiguredException : IllegalStateException("API ainda não configurada")

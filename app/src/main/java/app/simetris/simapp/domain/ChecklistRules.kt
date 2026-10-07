package app.simetris.simapp.domain

enum class Answer(val storageValue: String) {
    PENDING("PENDENTE"), CONFORMING("CONFORME"), NONCONFORMING("NAO_CONFORME");

    companion object {
        fun fromStorage(value: String): Answer = entries.first { it.storageValue == value }
    }
}
enum class CompletionStatus { DRAFT, FINALIZED }
enum class UploadStatus { PENDING, SENDING, FAILED, CONFIRMED }
enum class InspectionResult { PENDING, CONFORMING, NONCONFORMING }
enum class MissingFieldType { RESPONSE, REASON, MIN_PHOTOS, MAX_PHOTOS, VEHICLE, DRIVER, ODOMETER, ITEMS, SIGNATURE }
data class MissingField(val type: MissingFieldType, val itemName: String = "", val quantity: Int = 0, val itemId: String = "")

data class ItemValidationData(
    val name: String,
    val answer: Answer,
    val reason: String,
    val reasonRequired: Boolean,
    val photoRequired: Boolean,
    val minPhotos: Int,
    val maxPhotos: Int,
    val photoCount: Int,
    val localId: String = "",
)

object ChecklistRules {
    fun readyForSignature(missingFields: List<MissingField>): Boolean =
        missingFields.all { it.type == MissingFieldType.SIGNATURE }

    fun itemMissingFields(item: ItemValidationData): List<MissingField> = buildList {
        if (item.answer == Answer.PENDING) add(MissingField(MissingFieldType.RESPONSE, item.name, itemId = item.localId))
        if (item.answer == Answer.NONCONFORMING) {
            if (item.reason.isBlank()) add(MissingField(MissingFieldType.REASON, item.name, itemId = item.localId))
            val minimum = maxOf(1, item.minPhotos)
            if (item.photoCount < minimum) add(MissingField(MissingFieldType.MIN_PHOTOS, item.name, minimum, item.localId))
        }
        if (item.photoCount > item.maxPhotos) add(MissingField(MissingFieldType.MAX_PHOTOS, item.name, item.maxPhotos, item.localId))
    }

    fun missingFields(
        vehicle: String?, driver: String?, odometerKm: Long?,
        items: List<ItemValidationData>, signatureSaved: Boolean,
    ): List<MissingField> = buildList {
        if (vehicle.isNullOrBlank()) add(MissingField(MissingFieldType.VEHICLE))
        if (driver.isNullOrBlank()) add(MissingField(MissingFieldType.DRIVER))
        if (odometerKm == null || odometerKm < 0) add(MissingField(MissingFieldType.ODOMETER))
        if (items.isEmpty()) add(MissingField(MissingFieldType.ITEMS))
        items.forEach { addAll(itemMissingFields(it)) }
        if (!signatureSaved) add(MissingField(MissingFieldType.SIGNATURE))
    }
}

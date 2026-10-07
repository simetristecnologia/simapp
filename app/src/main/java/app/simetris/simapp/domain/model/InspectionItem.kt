package app.simetris.simapp.domain.model

data class InspectionItem(
    val localId: String, val checklistId: String, val order: Int, val name: String,
    val description: String, val reasonRequired: Boolean, val photoRequired: Boolean,
    val minPhotos: Int, val maxPhotos: Int,
    val answer: String, val reason: String,
)


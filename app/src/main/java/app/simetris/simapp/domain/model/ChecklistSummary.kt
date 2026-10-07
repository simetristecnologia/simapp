package app.simetris.simapp.domain.model

data class ChecklistSummary(
    val localId: String, val sourceTemplateId: Long, val templateName: String,
    val sourceVehicleId: Long?, val vehicleName: String?, val driverName: String?,
    val recordedAtUtc: Long, val completionStatus: String, val uploadStatus: String,
    val result: String, val odometerKm: Long?,
)


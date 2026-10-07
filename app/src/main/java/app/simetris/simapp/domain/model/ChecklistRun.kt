package app.simetris.simapp.domain.model

data class ChecklistRun(
    val localId: String, val templateName: String, val recordedAtUtc: Long,
    val vehicleName: String?, val driverName: String?, val odometerKm: Long?,
    val note: String, val completionStatus: String, val uploadStatus: String,
    val result: String, val synchronizedAtUtc: Long?, val uploadAttempted: Boolean,
)

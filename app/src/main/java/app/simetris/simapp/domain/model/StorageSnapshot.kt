package app.simetris.simapp.domain.model

data class StorageSnapshot(
    val checklistBytes: Long,
    val mediaBytes: Long,
    val cacheBytes: Long,
    val deviceBytes: Long,
) {
    val occupiedBytes: Long get() = checklistBytes + mediaBytes + cacheBytes
}

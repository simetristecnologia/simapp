package app.simetris.simapp.domain

object OfflineSessionPolicy {
    const val MAX_AGE_MS = 24L * 60 * 60 * 1000
    fun isValid(validatedAt: Long, now: Long): Boolean = now - validatedAt in 0 until MAX_AGE_MS
}

package app.simetris.simapp

import app.simetris.simapp.domain.OfflineSessionPolicy
import org.junit.Assert.*
import org.junit.Test

class OfflineSessionPolicyTest {
    @Test fun validOnlyWithin24HoursAfterServerValidation() {
        val validatedAt = 1_000_000L
        assertTrue(OfflineSessionPolicy.isValid(validatedAt, validatedAt))
        assertTrue(OfflineSessionPolicy.isValid(validatedAt, validatedAt + OfflineSessionPolicy.MAX_AGE_MS - 1))
        assertFalse(OfflineSessionPolicy.isValid(validatedAt, validatedAt + OfflineSessionPolicy.MAX_AGE_MS))
        assertFalse(OfflineSessionPolicy.isValid(validatedAt, validatedAt - 1))
    }
}

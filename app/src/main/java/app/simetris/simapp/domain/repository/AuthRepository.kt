package app.simetris.simapp.domain.repository

import kotlinx.coroutines.flow.StateFlow

data class AuthSession(
    val userId: Long,
    val companyId: String,
    val userName: String,
    val validatedAt: Long,
)

sealed interface AuthStatus {
    data object Loading : AuthStatus
    data object SignedOut : AuthStatus
    data class SignedIn(val session: AuthSession) : AuthStatus
}

interface AuthRepository {
    val status: StateFlow<AuthStatus>
    suspend fun restore()
    suspend fun login(companyCode: String, login: String, password: String)
    suspend fun signOut()
}

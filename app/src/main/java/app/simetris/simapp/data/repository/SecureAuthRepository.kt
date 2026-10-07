package app.simetris.simapp.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import app.simetris.simapp.data.remote.AuthApi
import app.simetris.simapp.data.remote.AuthHttpException
import app.simetris.simapp.data.remote.AuthResponse
import app.simetris.simapp.data.remote.IdentityProvider
import app.simetris.simapp.data.remote.LocalIdentity
import app.simetris.simapp.domain.repository.AuthRepository
import app.simetris.simapp.domain.repository.AuthSession
import app.simetris.simapp.domain.repository.AuthStatus
import app.simetris.simapp.domain.OfflineSessionPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureAuthRepository(private val context: Context, private val api: AuthApi = AuthApi()) :
    AuthRepository, IdentityProvider {
    private val preferences = context.getSharedPreferences("auth_session", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val mutableStatus = MutableStateFlow<AuthStatus>(AuthStatus.Loading)
    override val status = mutableStatus.asStateFlow()
    private var current: StoredSession? = null

    override suspend fun restore() = mutex.withLock {
        val saved = current ?: withContext(Dispatchers.IO) { read() }
        if (saved == null) {
            mutableStatus.value = AuthStatus.SignedOut
            return@withLock
        }
        if (mutableStatus.value is AuthStatus.SignedIn &&
            System.currentTimeMillis() - saved.validatedAt in 0..60_000L) return@withLock
        try {
            val response = api.refresh(saved.refreshToken)
            if (response.userId != saved.userId || response.companyId != saved.companyId) clear()
            else save(response)
        } catch (error: AuthHttpException) {
            if (error.statusCode == 401 || error.statusCode == 403) clear()
            else acceptOffline(saved)
        } catch (_: Exception) { acceptOffline(saved) }
    }

    override suspend fun login(companyCode: String, login: String, password: String) = mutex.withLock {
        val code = companyCode.toIntOrNull()?.takeIf { it > 0 }
            ?: throw IllegalArgumentException("Código inválido")
        require(login.isNotBlank() && password.isNotBlank()) { "Campos obrigatórios" }
        save(api.login(code, login.trim(), password))
    }

    override suspend fun signOut() = mutex.withLock { clear() }

    override suspend fun identity(): LocalIdentity? = activeSession()?.let {
        LocalIdentity(it.companyId, "usuario-${it.userId}", it.userId)
    }

    override suspend fun credential(): String? = if (activeSession() == null) null else current?.accessToken

    private fun activeSession(): AuthSession? = (status.value as? AuthStatus.SignedIn)?.session
        ?.takeIf { OfflineSessionPolicy.isValid(it.validatedAt, System.currentTimeMillis()) }

    private fun acceptOffline(saved: StoredSession) {
        current = saved
        if (OfflineSessionPolicy.isValid(saved.validatedAt, System.currentTimeMillis()))
            mutableStatus.value = AuthStatus.SignedIn(saved.publicSession())
        else mutableStatus.value = AuthStatus.SignedOut
    }

    private suspend fun save(response: AuthResponse) {
        require(response.accessToken.isNotBlank() && response.refreshToken.isNotBlank())
        val session = StoredSession(response.accessToken, response.refreshToken,
            response.userId, response.companyId, response.userName, System.currentTimeMillis())
        withContext(Dispatchers.IO) { write(session) }
        current = session
        mutableStatus.value = AuthStatus.SignedIn(session.publicSession())
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) { check(preferences.edit().remove("encrypted").commit()) }
        current = null
        mutableStatus.value = AuthStatus.SignedOut
    }

    private fun read(): StoredSession? = try {
        val encoded = preferences.getString("encrypted", null) ?: return null
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        val json = JSONObject(String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8))
        StoredSession(json.getString("access"), json.getString("refresh"), json.getLong("userId"),
            json.getString("companyId"), json.getString("userName"), json.getLong("validatedAt"))
    } catch (_: Exception) { null }

    private fun write(session: StoredSession) {
        val json = JSONObject().put("access", session.accessToken).put("refresh", session.refreshToken)
            .put("userId", session.userId).put("companyId", session.companyId)
            .put("userName", session.userName).put("validatedAt", session.validatedAt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.iv + cipher.doFinal(json.toString().toByteArray(Charsets.UTF_8))
        check(preferences.edit().putString("encrypted", Base64.encodeToString(encrypted, Base64.NO_WRAP)).commit())
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey("simapp_auth", null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder("simapp_auth",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }

    private data class StoredSession(val accessToken: String, val refreshToken: String,
        val userId: Long, val companyId: String, val userName: String, val validatedAt: Long) {
        fun publicSession() = AuthSession(userId, companyId, userName, validatedAt)
    }

}

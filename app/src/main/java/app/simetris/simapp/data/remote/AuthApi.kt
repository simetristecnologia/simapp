package app.simetris.simapp.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal const val API_BASE_URL = "https://api.simetris.app/v1"

data class AuthResponse(val accessToken: String, val refreshToken: String,
    val userId: Long, val companyId: String, val userName: String)

class AuthApi {
    suspend fun login(companyCode: Int, login: String, password: String): AuthResponse =
        post("login", JSONObject().put("codigo", companyCode).put("login", login).put("senha", password))

    suspend fun refresh(token: String): AuthResponse =
        post("refresh", JSONObject().put("tokenRefresh", token))

    private suspend fun post(path: String, body: JSONObject): AuthResponse = withContext(Dispatchers.IO) {
        val connection = (URL("$API_BASE_URL/$path").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            if (code !in 200..299) throw AuthHttpException(code)
            val json = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
            val user = json.getJSONObject("usuario")
            val company = user.getJSONObject("empresaConectada")
            AuthResponse(json.getString("token"), json.getString("tokenRefresh"),
                user.getLong("id"), company.getLong("empresaId").toString(), user.getString("nome"))
        } finally { connection.disconnect() }
    }
}

class AuthHttpException(val statusCode: Int) : Exception("HTTP $statusCode")

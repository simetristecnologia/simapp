package app.simetris.simapp.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** As rotas e os nomes JSON seguem novoplanejamento.ts. */
class HttpChecklistApi(private val identityProvider: IdentityProvider) : ChecklistApi {
    override suspend fun registrations(identity: LocalIdentity): Registrations {
        val json = request("GET", "cadastros")
        return Registrations(
            json.getJSONArray("motoristas").objects { RemoteDriver(getLong("id"), getString("nome")) },
            json.getJSONArray("veiculos").objects { RemoteVehicle(getLong("id"), getString("placa")) },
            json.getJSONArray("modelos").objects {
                RemoteTemplate(getLong("id"), getString("nome"), getString("descricao"),
                    getJSONArray("itens").objects { RemoteTemplateItem(getLong("id"), getInt("ordem"),
                        getString("nome"), getString("descricao")) },
                    getBoolean("motivoObrigatorio"), getBoolean("fotoObrigatorio"),
                    getInt("fotoQuantidadeMinima"), getInt("fotoQuantidadeMaxima"))
            })
    }

    override suspend fun insert(checklist: ChecklistPayload) {
        val response = request("POST", "checklist", checklist.json())
        if (response.has("id")) require(response.getString("id") == checklist.id)
    }

    override suspend fun update(id: String, checklist: ChecklistPayload) {
        require(id == checklist.id)
        val response = request("PUT", "checklist/$id", checklist.json())
        if (response.has("id")) require(response.getString("id") == id)
    }

    override suspend fun sendAttachments(id: String, attachments: List<ChecklistAttachmentPayload>) {
        val body = JSONArray().apply { attachments.forEach { put(it.json()) } }
        val response = request("POST", "checklist/$id/anexos", body)
        val received = response.getJSONArray("anexosRecebidos")
            .objects { getString("id") }.toSet()
        require(received == attachments.map { it.id }.toSet()) { "Confirmação incompleta dos anexos" }
    }

    private suspend fun request(method: String, path: String, body: Any? = null): JSONObject =
        withContext(Dispatchers.IO) {
            val token = identityProvider.credential() ?: throw ApiNotConfiguredException()
            val connection = URL("$API_BASE_URL/$path").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = method
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Accept", "application/json")
                if (body != null) {
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                }
                val code = connection.responseCode
                if (code !in 200..299) throw ChecklistHttpException(code)
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                if (response.isBlank()) JSONObject() else JSONObject(response)
            } finally { connection.disconnect() }
        }
}

class ChecklistHttpException(val statusCode: Int) : Exception("HTTP $statusCode")

private inline fun <T> JSONArray.objects(block: JSONObject.() -> T): List<T> =
    (0 until length()).map { getJSONObject(it).block() }

private fun ChecklistPayload.json() = JSONObject()
    .put("id", id).put("empresaId", companyId).put("usuarioId", userId)
    .put("situacao", situation).put("nome", name).put("descricao", description)
    .put("dataHora", dateTime).put("dataHoraSincronizado", synchronizedAt ?: JSONObject.NULL)
    .put("modeloId", templateId).put("modeloNome", templateName)
    .put("veiculoId", vehicleId).put("veiculoPlaca", vehiclePlate)
    .put("motoristaId", driverId).put("motoristaNome", driverName)
    .put("motivoObrigatorio", reasonRequired).put("fotoObrigatorio", photoRequired)
    .put("fotoQuantidadeMinima", minPhotos).put("fotoQuantidadeMaxima", maxPhotos)
    .put("observacao", note ?: JSONObject.NULL)
    .put("itens", JSONArray().apply { items.forEach { put(it.json()) } })
    .put("anexos", JSONArray().apply { attachments.forEach { put(it.json()) } })

private fun ChecklistItemPayload.json() = JSONObject().put("id", id).put("ordem", order)
    .put("nome", name).put("descricao", description).put("resposta", answer)
    .put("motivo", reason ?: JSONObject.NULL)

private fun ChecklistAttachmentPayload.json() = JSONObject().put("id", id).put("tipo", type)
    .put("itemId", itemId ?: JSONObject.NULL).put("dataHora", dateTime)
    .put("nomeArquivo", fileName).put("tipoArquivo", fileType)
    .put("tamanhoArquivo", fileSize).put("arquivo", file)

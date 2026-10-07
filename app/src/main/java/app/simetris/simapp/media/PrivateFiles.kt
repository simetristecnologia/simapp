package app.simetris.simapp.media

import android.content.Context
import android.net.Uri
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.UUID

data class SavedFile(val relativePath: String, val mime: String, val sizeBytes: Long, val sha256: String)

class PrivateFiles(private val context: Context) {
    private val root get() = File(context.filesDir, "anexos").apply { mkdirs() }
    suspend fun copy(uri: Uri, mime: String): SavedFile = withContext(Dispatchers.IO) {
        require(mime.startsWith("image/")) { "Selecione uma imagem válida" }
        val name = "${UUID.randomUUID()}.img"
        val destination = File(root, name)
        try {
            val source = context.contentResolver.openInputStream(uri) ?: error("Imagem indisponível")
            source.use { input -> destination.outputStream().use { output -> input.copyTo(output) } }
            validate(destination, mime)
        } catch (error: Exception) { destination.delete(); throw error }
    }
    suspend fun save(bytes: ByteArray, mime: String): SavedFile = withContext(Dispatchers.IO) {
        val destination = File(root, "${UUID.randomUUID()}.png")
        try { destination.writeBytes(bytes); validate(destination, mime) }
        catch (error: Exception) { destination.delete(); throw error }
    }
    private fun validate(file: File, mime: String): SavedFile {
        require(file.length() in 1..15_000_000) { "Imagem vazia ou maior que 15 MB" }
        require(BitmapFactory.decodeFile(file.absolutePath) != null) { "Imagem inválida" }
        val bytes = file.readBytes()
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        return SavedFile(file.name, mime, bytes.size.toLong(), hash)
    }
    fun file(path: String): File {
        require(path == File(path).name) { "Caminho de anexo inválido" }
        return File(root, path)
    }
    suspend fun read(path: String): ByteArray = withContext(Dispatchers.IO) { file(path).readBytes() }
    suspend fun readValidated(path: String, sha256: String): ByteArray = withContext(Dispatchers.IO) {
        val bytes = file(path).readBytes()
        val current = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        require(current == sha256) { "Anexo local alterado ou corrompido" }
        bytes
    }
    suspend fun delete(path: String) = withContext(Dispatchers.IO) { file(path).delete() }
    suspend fun clearOrphans(usedPaths: Set<String>) = withContext(Dispatchers.IO) {
        val cutoff = System.currentTimeMillis() - 60 * 60 * 1000
        root.listFiles()?.filter { it.isFile && it.name !in usedPaths && it.lastModified() < cutoff }?.forEach { it.delete() }
    }
}



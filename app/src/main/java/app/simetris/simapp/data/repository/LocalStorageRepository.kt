package app.simetris.simapp.data.repository

import android.content.Context
import android.os.StatFs
import app.simetris.simapp.domain.model.StorageSnapshot
import app.simetris.simapp.domain.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalStorageRepository(private val context: Context) : StorageRepository {
    private val databaseFile get() = context.getDatabasePath("simapp.db")
    private val attachments get() = File(context.filesDir, "anexos")

    override suspend fun snapshot(): StorageSnapshot = withContext(Dispatchers.IO) {
        val databaseBytes = listOf(databaseFile, File("${databaseFile.path}-wal"),
            File("${databaseFile.path}-shm")).sumOf { it.length() }
        StorageSnapshot(
            checklistBytes = databaseBytes,
            mediaBytes = attachments.sizeBytes(),
            cacheBytes = cameraCacheFiles().sumOf { it.length() },
            deviceBytes = StatFs(context.filesDir.path).totalBytes,
        )
    }

    override suspend fun clearCache(): StorageSnapshot = withContext(Dispatchers.IO) {
        cameraCacheFiles().forEach { file ->
            check(file.delete()) { "Não foi possível limpar o cache" }
        }
        snapshot()
    }

    private fun File.sizeBytes(): Long = when {
        isFile -> length()
        isDirectory -> listFiles()?.sumOf { it.sizeBytes() } ?: 0L
        else -> 0L
    }

    private fun cameraCacheFiles(): List<File> = context.cacheDir.listFiles()
        ?.filter { it.isFile && it.name.startsWith("captura") && it.extension == "jpg" }
        ?: emptyList()
}

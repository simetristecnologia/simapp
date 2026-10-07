package app.simetris.simapp.sync

import android.content.Context
import androidx.work.*
import app.simetris.simapp.SimAppApplication
import app.simetris.simapp.data.repository.CatalogRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class UploadWorker(context: Context, parametros: WorkerParameters) : CoroutineWorker(context, parametros) {
    override suspend fun doWork(): Result = try {
        val app = applicationContext as SimAppApplication
        val identity = app.auth.identity() ?: return Result.success()
        if (ChecklistSynchronizer(app.database, app.files, app.api(), identity).synchronizePendingUploads()) Result.success() else Result.retry()
    } catch (_: Exception) { Result.retry() }
}

class CatalogWorker(context: Context, parametros: WorkerParameters) : CoroutineWorker(context, parametros) {
    override suspend fun doWork(): Result = try {
        val app = applicationContext as SimAppApplication
        val identity = app.auth.identity() ?: return Result.success()
        if (CatalogRepository(app.database, app.api()).refresh(identity)) Result.success() else Result.retry()
    } catch (_: Exception) { Result.retry() }
}

object WorkScheduler {
    fun upload(context: Context, wifiOnly: Boolean = false, replace: Boolean = false) {
        val request = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build()
        WorkManager.getInstance(context).enqueueUniqueWork("envio-checklists",
            if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP, request)
    }
    fun catalogs(context: Context) {
        val request = OneTimeWorkRequestBuilder<CatalogWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        WorkManager.getInstance(context).enqueueUniqueWork("atualizar-catalogos", ExistingWorkPolicy.KEEP, request)
    }
    fun periodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<CatalogWorker>(12, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("catalogos-periodico", ExistingPeriodicWorkPolicy.KEEP, request)
    }
}



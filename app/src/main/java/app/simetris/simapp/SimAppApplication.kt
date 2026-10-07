package app.simetris.simapp

import android.app.Application
import androidx.room.Room
import app.simetris.simapp.data.local.AppDatabase
import app.simetris.simapp.data.local.PreferencesStore
import app.simetris.simapp.data.repository.SecureAuthRepository
import app.simetris.simapp.data.remote.*
import app.simetris.simapp.data.repository.LocalChecklistRepository
import app.simetris.simapp.data.repository.LocalReferenceDataRepository
import app.simetris.simapp.data.repository.LocalStorageRepository
import app.simetris.simapp.media.PrivateFiles
import app.simetris.simapp.sync.WorkScheduler
import app.simetris.simapp.ui.components.UiMessages
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.net.ConnectivityManager
import android.net.Network

class SimAppApplication : Application() {
    val database by lazy { Room.databaseBuilder(this, AppDatabase::class.java, "simapp.db")
        .fallbackToDestructiveMigration(true).build() }
    val files by lazy { PrivateFiles(this) }
    val auth by lazy { SecureAuthRepository(this) }
    val preferences by lazy { PreferencesStore(this, auth) }
    val uiMessages by lazy { UiMessages() }
    val repository by lazy { LocalChecklistRepository(database, files, auth) }
    val referenceData by lazy { LocalReferenceDataRepository(database) }
    val storage by lazy { LocalStorageRepository(this) }
    private val demoApi by lazy { DemoChecklistApi(database) }
    private val httpApi by lazy { HttpChecklistApi(auth) }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    suspend fun api(): ChecklistApi = if (preferences.demoMode.first()) demoApi else httpApi
    suspend fun scheduleUpload(replace: Boolean = false) =
        WorkScheduler.upload(this, preferences.wifiOnlyUpload.first(), replace)

    override fun onCreate() {
        super.onCreate()
        WorkScheduler.catalogs(this)
        WorkScheduler.periodic(this)
        appScope.launch { scheduleUpload() }
        val connection = getSystemService(ConnectivityManager::class.java)
        connection.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                appScope.launch {
                    auth.restore()
                    WorkScheduler.catalogs(this@SimAppApplication)
                    scheduleUpload()
                }
            }
        })
    }
}

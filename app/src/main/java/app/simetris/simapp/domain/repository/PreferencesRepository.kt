package app.simetris.simapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val activeScope: Flow<Pair<String, String>?>
    val completionFilter: Flow<String?>
    val wifiOnlyUpload: Flow<Boolean>
    val demoMode: Flow<Boolean>
    suspend fun saveFilter(value: String?)
    suspend fun saveNetworkPreference(value: Boolean)
}

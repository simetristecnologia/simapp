package app.simetris.simapp.data.local

import android.content.Context
import app.simetris.simapp.domain.repository.PreferencesRepository
import app.simetris.simapp.domain.repository.AuthRepository
import app.simetris.simapp.domain.repository.AuthStatus
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.storedPreferences by preferencesDataStore("preferencias")

class PreferencesStore(private val context: Context, auth: AuthRepository) : PreferencesRepository {
    private val filter = stringPreferencesKey("filtro_preenchimento")
    private val wifiOnly = booleanPreferencesKey("envio_somente_wifi")
    override val completionFilter: Flow<String?> = context.storedPreferences.data.map { it[filter] }
    override val wifiOnlyUpload: Flow<Boolean> = context.storedPreferences.data.map { it[wifiOnly] ?: false }
    override val demoMode: Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)
    override val activeScope: Flow<Pair<String, String>?> = auth.status.map { status ->
        (status as? AuthStatus.SignedIn)?.session?.let { it.companyId to "usuario-${it.userId}" }
    }
    override suspend fun saveFilter(value: String?) { context.storedPreferences.edit { if (value == null) it.remove(filter) else it[filter] = value } }
    override suspend fun saveNetworkPreference(value: Boolean) { context.storedPreferences.edit { it[wifiOnly] = value } }
}


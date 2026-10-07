package app.simetris.simapp.ui.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.simetris.simapp.R
import app.simetris.simapp.ui.components.AppTopBar
import app.simetris.simapp.ui.components.ScreenColumn

@Composable
fun PreferencesScreen(vm: PreferencesViewModel, signOut: () -> Unit) {
    val state by vm.state.collectAsState()
    Column {
        AppTopBar(R.string.preferences_title)
        ScreenColumn {
            ConnectionCard(state.wifiOnly, vm::saveNetworkPreference)
            SynchronizationCard(state, vm::synchronize)
            StorageCard(state.storage, state.clearingCache, vm::clearCache)
            SignOutCard(signOut)
        }
    }
}


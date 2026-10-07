package app.simetris.simapp.ui.di

import app.simetris.simapp.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filterNotNull

internal fun uiScope(preferences: PreferencesRepository) = preferences.activeScope.filterNotNull()


package app.simetris.simapp.ui.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simetris.simapp.SimAppApplication
import app.simetris.simapp.data.repository.CatalogRepository
import app.simetris.simapp.sync.ChecklistSynchronizer
import app.simetris.simapp.ui.checklist.detail.ChecklistDetailViewModel
import app.simetris.simapp.ui.checklist.form.ChecklistFormViewModel
import app.simetris.simapp.ui.checklist.form.TemplateViewModel
import app.simetris.simapp.ui.checklist.list.ChecklistHistoryViewModel
import app.simetris.simapp.ui.home.HomeViewModel
import app.simetris.simapp.ui.auth.LoginViewModel
import app.simetris.simapp.ui.preferences.PreferencesViewModel

object ViewModelFactories {
    private fun <T : ViewModel> factory(create: () -> T): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <V : ViewModel> create(modelClass: Class<V>): V = create() as V
        }

    private suspend fun refresh(app: SimAppApplication): Boolean =
        CatalogRepository(app.database, app.api()).refresh(app.repository.scope())
    private suspend fun upload(app: SimAppApplication): Boolean =
        ChecklistSynchronizer(app.database, app.files, app.api(),
            app.repository.scope()).synchronizePendingUploads()

    fun home(app: SimAppApplication) = factory { HomeViewModel(app.repository, app.referenceData, app.preferences) }
    fun login(app: SimAppApplication) = factory { LoginViewModel(app.auth) }
    fun templates(app: SimAppApplication) = factory { TemplateViewModel(app.repository, app.referenceData,
        app.preferences, app.uiMessages, { app.scheduleUpload() }) }
    fun form(app: SimAppApplication) = factory { ChecklistFormViewModel(app.repository, app.referenceData, app.preferences,
        app.uiMessages, { app.scheduleUpload() }) }
    fun history(app: SimAppApplication) = factory { ChecklistHistoryViewModel(app.repository, app.referenceData, app.preferences) }
    fun detail(app: SimAppApplication) = factory { ChecklistDetailViewModel(app.repository, app.preferences, app.uiMessages) }
    fun preferences(app: SimAppApplication) = factory { PreferencesViewModel(app.preferences, app.repository, app.referenceData,
        app.storage, app.uiMessages, { refresh(app) }, { upload(app) },
        { app.scheduleUpload() }, { app.scheduleUpload(replace = true) }) }
}




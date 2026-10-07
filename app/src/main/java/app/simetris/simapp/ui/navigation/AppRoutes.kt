package app.simetris.simapp.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable sealed interface AppRoute : NavKey
@Serializable data object HomeRoute : AppRoute
@Serializable data object HistoryRoute : AppRoute
@Serializable data object TemplatesRoute : AppRoute
@Serializable data class ChecklistDataRoute(val id: String) : AppRoute
@Serializable data class InspectionItemsRoute(val id: String) : AppRoute
@Serializable data class CameraRoute(val checklistId: String, val itemId: String) : AppRoute
@Serializable data class SignatureRoute(val id: String) : AppRoute
@Serializable data class ChecklistDetailRoute(val id: String) : AppRoute
@Serializable data object PreferencesRoute : AppRoute

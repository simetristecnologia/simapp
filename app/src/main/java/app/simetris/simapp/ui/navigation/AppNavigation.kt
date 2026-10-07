package app.simetris.simapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import app.simetris.simapp.domain.repository.AuthStatus
import app.simetris.simapp.domain.OfflineSessionPolicy
import app.simetris.simapp.ui.auth.LoginScreen
import app.simetris.simapp.ui.auth.LoginViewModel
import app.simetris.simapp.ui.components.LocalOpenPreferences
import app.simetris.simapp.ui.components.LocalUserName
import app.simetris.simapp.sync.WorkScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import app.simetris.simapp.SimAppApplication
import app.simetris.simapp.ui.checklist.detail.ChecklistDetailViewModel
import app.simetris.simapp.ui.checklist.detail.ChecklistDetailScreen
import app.simetris.simapp.ui.checklist.form.*
import app.simetris.simapp.ui.checklist.list.ChecklistHistoryViewModel
import app.simetris.simapp.ui.checklist.list.ChecklistHistoryScreen
import app.simetris.simapp.ui.checklist.signature.SignatureScreen
import app.simetris.simapp.ui.components.LocalGoHome
import app.simetris.simapp.media.camera.CameraScreen
import app.simetris.simapp.ui.di.ViewModelFactories
import app.simetris.simapp.ui.home.HomeViewModel
import app.simetris.simapp.ui.home.HomeScreen
import app.simetris.simapp.ui.preferences.PreferencesViewModel
import app.simetris.simapp.ui.preferences.PreferencesScreen
import app.simetris.simapp.ui.theme.SimAppTheme

@Composable
fun AppNavigation(app: SimAppApplication) {
    SimAppTheme {
        val authStatus by app.auth.status.collectAsState()
        val login: LoginViewModel = viewModel(factory = ViewModelFactories.login(app))
        LaunchedEffect(Unit) { app.auth.restore() }
        when (val current = authStatus) {
            AuthStatus.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            AuthStatus.SignedOut -> LoginScreen(login)
            is AuthStatus.SignedIn -> {
                LaunchedEffect(current.session.validatedAt) {
                    val remaining = OfflineSessionPolicy.MAX_AGE_MS -
                        (System.currentTimeMillis() - current.session.validatedAt)
                    delay(remaining.coerceAtLeast(1L))
                    app.auth.restore()
                }
                LaunchedEffect(current.session.userId) {
                    WorkScheduler.catalogs(app)
                    app.scheduleUpload()
                }
                key(current.session.companyId, current.session.userId) {
                    AuthenticatedNavigation(app, current.session.companyId,
                        current.session.userId, current.session.userName)
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedNavigation(app: SimAppApplication, companyId: String, userId: Long, userName: String) {
        val scopeKey = "$companyId-$userId"
        val home: HomeViewModel = viewModel(key = "home-$scopeKey", factory = ViewModelFactories.home(app))
        val templates: TemplateViewModel = viewModel(key = "templates-$scopeKey", factory = ViewModelFactories.templates(app))
        val form: ChecklistFormViewModel = viewModel(key = "form-$scopeKey", factory = ViewModelFactories.form(app))
        val history: ChecklistHistoryViewModel = viewModel(key = "history-$scopeKey", factory = ViewModelFactories.history(app))
        val detail: ChecklistDetailViewModel = viewModel(key = "detail-$scopeKey", factory = ViewModelFactories.detail(app))
        val preferences: PreferencesViewModel = viewModel(key = "preferences-$scopeKey", factory = ViewModelFactories.preferences(app))
        val backStack = rememberNavBackStack(HomeRoute)
        val snackbar = remember { SnackbarHostState() }
        LaunchedEffect(app.uiMessages) {
            app.uiMessages.messages.collect { snackbar.showSnackbar(app.getString(it)) }
        }
        val goBack: () -> Unit = { if (backStack.size > 1) backStack.removeLastOrNull(); Unit }
        val goHome: () -> Unit = { navigateTo(backStack, HomeRoute) }
        val openPreferences: () -> Unit = { navigateTo(backStack, PreferencesRoute) }
        val coroutineScope = rememberCoroutineScope()
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { paddingValues ->
            Box(if (backStack.lastOrNull() is CameraRoute) Modifier else Modifier.padding(paddingValues)) {
                CompositionLocalProvider(
                    LocalGoHome provides (if (backStack.size > 1) goHome else null),
                    LocalOpenPreferences provides openPreferences,
                    LocalUserName provides userName,
                ) {
                NavDisplay(backStack = backStack, onBack = goBack, entryProvider = entryProvider {
                    entry<HomeRoute> { HomeScreen(home, open = { backStack.add(it) }) }
                    entry<HistoryRoute> { ChecklistHistoryScreen(history, open = { backStack.add(ChecklistDetailRoute(it)) }, create = { backStack.add(TemplatesRoute) }) }
                    entry<TemplatesRoute> { ChecklistDataScreen(templates, form, null,
                        next = { id ->
                            backStack.removeLastOrNull()
                            backStack.add(ChecklistDetailRoute(id))
                            backStack.add(ChecklistDataRoute(id))
                            backStack.add(InspectionItemsRoute(id))
                        }, saveDraft = { id ->
                            backStack.removeLastOrNull()
                            backStack.add(ChecklistDetailRoute(id))
                        },
                        goBack = goBack) }
                    entry<ChecklistDataRoute> { route -> ChecklistDataScreen(templates, form, route.id,
                        next = { backStack.add(InspectionItemsRoute(route.id)) },
                        saveDraft = { navigateTo(backStack, ChecklistDetailRoute(route.id)) },
                        goBack = goBack) }
                    entry<InspectionItemsRoute> { route -> InspectionItemsScreen(form, route.id,
                        next = { backStack.add(SignatureRoute(route.id)) },
                        save = { navigateTo(backStack, ChecklistDetailRoute(route.id)) },
                        openCamera = { itemId -> backStack.add(CameraRoute(route.id, itemId)) },
                        goBack = goBack) }
                    entry<CameraRoute> { route -> CameraScreen(goBack = goBack,
                        onCaptured = { bytes ->
                            form.addPhotoBytes(route.checklistId, route.itemId, bytes)
                            goBack()
                        }) }
                    entry<SignatureRoute> { route -> SignatureScreen(form, route.id, cancel = goBack,
                        complete = { navigateTo(backStack, ChecklistDetailRoute(route.id)) }) }
                    entry<ChecklistDetailRoute> { route -> ChecklistDetailScreen(detail, route.id,
                        edit = { navigateTo(backStack, ChecklistDataRoute(route.id)) }, delete = { navigateTo(backStack, HistoryRoute) }, goBack = goBack) }
                    entry<PreferencesRoute> { PreferencesScreen(preferences) {
                        navigateTo(backStack, HomeRoute)
                        coroutineScope.launch { app.auth.signOut() }
                    } }
                })
                }
            }
        }
}

private fun navigateTo(backStack: MutableList<NavKey>, route: AppRoute) {
    val existing = backStack.indexOfLast { it == route }
    if (existing < 0) backStack.add(route)
    else while (backStack.lastIndex > existing) backStack.removeAt(backStack.lastIndex)
}


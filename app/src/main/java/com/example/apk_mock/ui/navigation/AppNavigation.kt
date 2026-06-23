package com.example.apk_mock.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.apk_mock.ui.forgotPassword.ForgotPasswordEmailScreen
import com.example.apk_mock.ui.forgotPassword.ForgotPasswordViewModel
import com.example.apk_mock.ui.home.HomeScreen
import com.example.apk_mock.ui.login.LoginScreen
import com.example.apk_mock.ui.login.LoginViewModel
import com.example.apk_mock.ui.onboarding.OnboardingScreen
import com.example.apk_mock.ui.profile.ChangePasswordScreen
import com.example.apk_mock.ui.profile.ProfileScreen
import com.example.apk_mock.ui.profile.ProfileViewModel
import com.example.apk_mock.ui.register.RegisterScreen
import com.example.apk_mock.ui.register.RegisterViewModel
import com.example.apk_mock.ui.rutinas.CrearRutinaScreen
import com.example.apk_mock.ui.rutinas.DetalleRutinaScreen
import com.example.apk_mock.ui.rutinas.EditarRutinaScreen
import com.example.apk_mock.ui.rutinas.RutinasScreen
import com.example.apk_mock.ui.rutinas.RutinasViewModel
import com.example.apk_mock.ui.session.SessionUiState
import com.example.apk_mock.ui.session.SessionViewModel
import com.example.apk_mock.ui.tareas.CameraCaptureScreen
import com.example.apk_mock.ui.tareas.CrearTareaScreen
import com.example.apk_mock.ui.tareas.DetalleTareaScreen
import com.example.apk_mock.ui.tareas.EditarTareaScreen
import com.example.apk_mock.ui.tareas.TareasScreen
import com.example.apk_mock.ui.tareas.TareasViewModel
import com.example.apk_mock.ui.theme.TaskPointTheme
import com.example.apk_mock.domain.model.ThemePreference

// ── Modelo de ítem del bottom bar ─────────────────────────────────────────────
private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.RUTINAS, "Rutinas", Icons.Default.DateRange),
    BottomNavItem(Routes.HOME,    "Inicio",  Icons.Default.Home),
    BottomNavItem(Routes.TAREAS,  "Tareas",  Icons.AutoMirrored.Filled.List)
)

// ── Rutas donde se muestra el bottom bar ─────────────────────────────────────
private val tabRoutes = setOf(Routes.HOME, Routes.RUTINAS, Routes.TAREAS)
private val profileRoutes = setOf(Routes.PROFILE, Routes.PROFILE_PASSWORD, Routes.PROFILE_GRAPH)
private val bottomBarRoutes = tabRoutes + profileRoutes + Routes.RUTINA_DETALLE_ROUTE + Routes.DETALLE_TAREA
private const val CAPTURED_PHOTO_PATH_KEY = "captured_photo_path"

@Composable
fun AppNavigation(
    onboardingCompleted: Boolean,
    onOnboardingCompleted: () -> Unit,
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit
) {
    val colors = TaskPointTheme.colors

    // Solo la sesion vive en el nivel global: decide el graph inicial.
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val sessionState by sessionViewModel.uiState.collectAsState()

    if (sessionState is SessionUiState.Checking) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colors.primary)
        }
        return
    }

    val startDestination = when {
        sessionState is SessionUiState.Authenticated -> Routes.MAIN_GRAPH
        onboardingCompleted -> Routes.LOGIN
        else -> Routes.ONBOARDING
    }
    val userName = (sessionState as? SessionUiState.Authenticated)?.user?.name.orEmpty()
    val navigationSessionKey = when (val state = sessionState) {
        is SessionUiState.Authenticated -> "authenticated:${state.user.id}"
        SessionUiState.Unauthenticated -> "unauthenticated"
        SessionUiState.Checking -> "checking"
    }
    val isInitialDataSyncInProgress =
        (sessionState as? SessionUiState.Authenticated)?.isInitialDataSyncInProgress == true

    key(navigationSessionKey) {
        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStack?.destination?.route

        Scaffold(
        containerColor = colors.background,
        bottomBar = {
            // El bottom bar aparece en tabs y en el flujo de perfil.
            if (currentRoute in bottomBarRoutes) {
                AppBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            // El NavHost respeta el innerPadding del Scaffold externo
            // Cada pantalla recibe este padding limpio sin duplicación
        ) {

            // ── Auth (sin bottom bar) ─────────────────────────────────────────

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        onOnboardingCompleted()
                        navController.navigate(Routes.REGISTER) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                    onSkip = {
                        onOnboardingCompleted()
                        navController.navigate(Routes.REGISTER) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                    onLogin = {
                        onOnboardingCompleted()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.REGISTER) {
                val vm: RegisterViewModel = hiltViewModel()
                RegisterScreen(
                    viewModel = vm,
                    onRegisterSuccess = { user ->
                        sessionViewModel.onAuthenticated(user)
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.LOGIN) {
                val vm: LoginViewModel = hiltViewModel()
                LoginScreen(
                    viewModel = vm,
                    onNavigateToHome = { user ->
                        sessionViewModel.onAuthenticated(user)
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    }
                )
            }

            composable(Routes.FORGOT_PASSWORD) {
                val vm: ForgotPasswordViewModel = hiltViewModel()
                ForgotPasswordEmailScreen(
                    viewModel = vm,
                    onCancel = { navController.popBackStack() }
                )
            }

            // ── Tabs principales (con bottom bar) ─────────────────────────────

            navigation(
                route = Routes.MAIN_GRAPH,
                startDestination = Routes.HOME
            ) {

            composable(Routes.HOME) {
                val rutinasViewModel = mainRutinasViewModel(navController)
                val tareasViewModel = mainTareasViewModel(navController)

                HomeScreen(
                    userName = userName,
                    rutinasViewModel = rutinasViewModel,
                    tareasViewModel = tareasViewModel,
                    isInitialDataSyncInProgress = isInitialDataSyncInProgress,
                    onCrearRutina = { navController.navigate(Routes.CREAR_RUTINA) },
                    onCrearTarea = {
                        tareasViewModel.resetCreateForm()
                        navController.navigate(Routes.CREAR_TAREA)
                    },
                    onProfile = { navController.navigate(Routes.PROFILE_GRAPH) },
                    onLogout = sessionViewModel::logout,
                    innerPadding = innerPadding
                )
            }

            composable(Routes.RUTINAS) {
                val rutinasViewModel = mainRutinasViewModel(navController)

                RutinasScreen(
                    viewModel = rutinasViewModel,
                    userName = userName,
                    isInitialDataSyncInProgress = isInitialDataSyncInProgress,
                    onNavigateToCrear = { navController.navigate(Routes.CREAR_RUTINA) },
                    onRutinaClick = { rutina -> navController.navigate(Routes.rutinaDetalle(rutina.id)) },
                    onProfile = { navController.navigate(Routes.PROFILE_GRAPH) },
                    onLogout = sessionViewModel::logout,
                    innerPadding = innerPadding
                )
            }

            composable(Routes.TAREAS) {
                val tareasViewModel = mainTareasViewModel(navController)

                val taskCreated by currentBackStack
                    ?.savedStateHandle
                    ?.getStateFlow("task_created", false)
                    ?.collectAsState()
                    ?: remember { mutableStateOf(false) }
                val taskDeleted by currentBackStack
                    ?.savedStateHandle
                    ?.getStateFlow("task_deleted", false)
                    ?.collectAsState()
                    ?: remember { mutableStateOf(false) }

                TareasScreen(
                    viewModel = tareasViewModel,
                    userName = userName,
                    isInitialDataSyncInProgress = isInitialDataSyncInProgress,
                    onNavigateToCrear = {
                        tareasViewModel.resetCreateForm()
                        navController.navigate(Routes.CREAR_TAREA)
                    },
                    onProfile = { navController.navigate(Routes.PROFILE_GRAPH) },
                    onLogout = sessionViewModel::logout,
                    onNavigateToDetalle = { taskId -> navController.navigate(Routes.detalleTarea(taskId)) },
                    showTaskCreatedMessage = taskCreated,
                    onTaskCreatedMessageShown = {
                        currentBackStack?.savedStateHandle?.set("task_created", false)
                    },
                    showTaskDeletedMessage = taskDeleted,
                    onTaskDeletedMessageShown = {
                        currentBackStack?.savedStateHandle?.set("task_deleted", false)
                    },
                    innerPadding = innerPadding
                )
            }

            // ── Flujos secundarios (sin bottom bar) ───────────────────────────

            composable(
                route = Routes.RUTINA_DETALLE_ROUTE,
                arguments = listOf(navArgument(Routes.ARG_RUTINA_ID) { type = NavType.StringType })
            ) { entry ->
                val rutinasViewModel = mainRutinasViewModel(navController)
                val tareasViewModel = mainTareasViewModel(navController)
                val rutinaId = entry.arguments?.getString(Routes.ARG_RUTINA_ID).orEmpty()
                DetalleRutinaScreen(
                    rutinaId = rutinaId,
                    rutinasViewModel = rutinasViewModel,
                    tareasViewModel = tareasViewModel,
                    onBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Routes.RUTINAS)
                        }
                    },
                    onDeleted = {
                        tareasViewModel.refreshTareas()
                        navController.popBackStack()
                    },
                    onEdit = {
                        rutinasViewModel.resetEditForm()
                        navController.navigate(Routes.editarRutina(rutinaId))
                    },
                    innerPadding = innerPadding
                )
            }

            composable(
                route = Routes.EDITAR_RUTINA_ROUTE,
                arguments = listOf(navArgument(Routes.ARG_RUTINA_ID) { type = NavType.StringType })
            ) { entry ->
                val rutinasViewModel = mainRutinasViewModel(navController)
                val tareasViewModel = mainTareasViewModel(navController)
                val rutinaId = entry.arguments?.getString(Routes.ARG_RUTINA_ID).orEmpty()
                EditarRutinaScreen(
                    rutinaId = rutinaId,
                    viewModel = rutinasViewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        tareasViewModel.refreshTareas()
                        val didReturnToDetail = navController.popBackStack()
                        rutinasViewModel.showDetalleSnackbar("Cambios guardados correctamente.")
                        if (!didReturnToDetail) {
                            navController.navigate(Routes.rutinaDetalle(rutinaId))
                        }
                    }
                )
            }

            navigation(
                route = Routes.PROFILE_GRAPH,
                startDestination = Routes.PROFILE
            ) {

            composable(Routes.PROFILE) {
                val profileViewModel = profileGraphViewModel(navController)
                val profileState by profileViewModel.uiState.collectAsState()

                LaunchedEffect(profileState.sessionEnded) {
                    if (profileState.sessionEnded) {
                        profileViewModel.onSessionEndedConsumed()
                        sessionViewModel.onSessionEnded()
                    }
                }

                ProfileScreen(
                    viewModel = profileViewModel,
                    userName = userName,
                    onBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Routes.HOME)
                        }
                    },
                    onChangePassword = { navController.navigate(Routes.PROFILE_PASSWORD) },
                    themePreference = themePreference,
                    onThemePreferenceChange = onThemePreferenceChange,
                    innerPadding = innerPadding
                )
            }

            composable(Routes.PROFILE_PASSWORD) {
                val profileViewModel = profileGraphViewModel(navController)

                ChangePasswordScreen(
                    viewModel = profileViewModel,
                    onBack = { navController.popBackStack() },
                    onPasswordChanged = {
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(Routes.PROFILE) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    innerPadding = innerPadding
                )
            }
            }

            composable(Routes.CREAR_RUTINA) {
                val rutinasViewModel = mainRutinasViewModel(navController)

                CrearRutinaScreen(
                    viewModel = rutinasViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.CREAR_TAREA) { backStackEntry ->
                val tareasViewModel = mainTareasViewModel(navController)

                val capturedPhotoPath by backStackEntry.savedStateHandle
                    .getStateFlow(CAPTURED_PHOTO_PATH_KEY, "")
                    .collectAsState()

                LaunchedEffect(capturedPhotoPath) {
                    if (capturedPhotoPath.isNotBlank()) {
                        tareasViewModel.onPhotoCaptured(capturedPhotoPath)
                        backStackEntry.savedStateHandle[CAPTURED_PHOTO_PATH_KEY] = ""
                    }
                }

                CrearTareaScreen(
                    viewModel = tareasViewModel,
                    onBack = { navController.popBackStack() },
                    onCapturePhoto = { navController.navigate(Routes.CAPTURAR_FOTO_TAREA) },
                    onTaskCreated = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("task_created", true)
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.DETALLE_TAREA) { backStackEntry ->
                val tareasViewModel = mainTareasViewModel(navController)

                val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
                val taskEdited by currentBackStack
                    ?.savedStateHandle
                    ?.getStateFlow("task_edited", false)
                    ?.collectAsState()
                    ?: remember { mutableStateOf(false) }

                DetalleTareaScreen(
                    taskId = taskId,
                    viewModel = tareasViewModel,
                    onBack = { navController.popBackStack() },
                    onEditTask = { id ->
                        tareasViewModel.resetEditForm()
                        navController.navigate(Routes.editarTarea(id))
                    },
                    onTaskDeleted = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("task_deleted", true)
                        navController.popBackStack()
                    },
                    showTaskEditedMessage = taskEdited,
                    onTaskEditedMessageShown = {
                        currentBackStack?.savedStateHandle?.set("task_edited", false)
                    },
                    innerPadding = innerPadding
                )
            }

            composable(Routes.EDITAR_TAREA) { backStackEntry ->
                val tareasViewModel = mainTareasViewModel(navController)

                val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
                val capturedPhotoPath by backStackEntry.savedStateHandle
                    .getStateFlow(CAPTURED_PHOTO_PATH_KEY, "")
                    .collectAsState()

                LaunchedEffect(capturedPhotoPath) {
                    if (capturedPhotoPath.isNotBlank()) {
                        tareasViewModel.onPhotoCaptured(capturedPhotoPath)
                        backStackEntry.savedStateHandle[CAPTURED_PHOTO_PATH_KEY] = ""
                    }
                }

                EditarTareaScreen(
                    taskId = taskId,
                    viewModel = tareasViewModel,
                    onBack = { navController.popBackStack() },
                    onCapturePhoto = { navController.navigate(Routes.CAPTURAR_FOTO_TAREA) },
                    onTaskEdited = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("task_edited", true)
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.CAPTURAR_FOTO_TAREA) {
                CameraCaptureScreen(
                    onBack = { navController.popBackStack() },
                    onPhotoCaptured = { photoPath ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(CAPTURED_PHOTO_PATH_KEY, photoPath)
                        navController.popBackStack()
                    }
                )
            }
            }
        }
    }
    }
}

// ── Bottom bar ────────────────────────────────────────────────────────────────
@Composable
private fun mainRutinasViewModel(navController: NavController): RutinasViewModel {
    val mainGraphEntry = remember(navController) {
        navController.getBackStackEntry(Routes.MAIN_GRAPH)
    }
    return hiltViewModel(mainGraphEntry)
}

@Composable
private fun mainTareasViewModel(navController: NavController): TareasViewModel {
    val mainGraphEntry = remember(navController) {
        navController.getBackStackEntry(Routes.MAIN_GRAPH)
    }
    return hiltViewModel(mainGraphEntry)
}

@Composable
private fun profileGraphViewModel(navController: NavController): ProfileViewModel {
    val profileGraphEntry = remember(navController) {
        navController.getBackStackEntry(Routes.PROFILE_GRAPH)
    }
    return hiltViewModel(profileGraphEntry)
}

@Composable
private fun AppBottomBar(navController: NavController, currentRoute: String?) {
    val colors = TaskPointTheme.colors
    val selectedRoute = when (currentRoute) {
        in profileRoutes -> Routes.HOME
        Routes.RUTINA_DETALLE_ROUTE -> Routes.RUTINAS
        else -> currentRoute
    }

    NavigationBar(
        containerColor = colors.bottomNavBackground,
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(92.dp)
            .drawBehind {
                drawLine(
                    color = colors.border,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        bottomNavItems.forEach { item ->
            val selected = selectedRoute == item.route
                || (currentRoute == Routes.DETALLE_TAREA && item.route == Routes.TAREAS)
            BottomNavButton(
                item = item,
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        // Evita apilar la misma pantalla múltiples veces
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun RowScope.BottomNavButton(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = TaskPointTheme.colors
    val itemShape = RoundedCornerShape(18.dp)
    val iconSize = 28.dp

    Box(
        modifier = Modifier
            .weight(1f)
            .height(86.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = item.label
                this.selected = selected
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClickLabel = "Abrir ${item.label}",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(itemShape)
                .background(if (selected) colors.primary else Color.Transparent)
                .padding(
                    horizontal = 14.dp,
                    vertical = 6.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = if (selected) Color.White else colors.bottomNavInactive,
                modifier = Modifier.size(iconSize)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                item.label,
                color = if (selected) Color.White else colors.bottomNavInactive,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

// ── Helper factory ────────────────────────────────────────────────────────────

package com.example.taskpoint.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taskpoint.data.MockAuthRepository
import com.example.taskpoint.data.MockRutinaRepository
import com.example.taskpoint.data.MockTareaRepository
import com.example.taskpoint.domain.useCase.*
import com.example.taskpoint.ui.forgotPassword.ForgotPasswordEmailScreen
import com.example.taskpoint.ui.forgotPassword.ForgotPasswordViewModel
import com.example.taskpoint.ui.home.HomeScreen
import com.example.taskpoint.ui.login.LoginScreen
import com.example.taskpoint.ui.login.LoginViewModel
import com.example.taskpoint.ui.register.RegisterScreen
import com.example.taskpoint.ui.register.RegisterViewModel
import com.example.taskpoint.ui.rutinas.CrearRutinaScreen
import com.example.taskpoint.ui.rutinas.RutinasScreen
import com.example.taskpoint.ui.rutinas.RutinasViewModel
import com.example.taskpoint.ui.tareas.CrearTareaScreen
import com.example.taskpoint.ui.tareas.TareasScreen
import com.example.taskpoint.ui.tareas.TareasViewModel
import com.example.taskpoint.ui.theme.*

// ── Repositorios y use cases: se inicializan con contexto dentro del Composable ─
// (ver AppNavigation() más abajo)

// ── Modelo de ítem del bottom bar ─────────────────────────────────────────────
private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.RUTINAS, "Rutinas", Icons.Default.DateRange),
    BottomNavItem(Routes.HOME,    "",        Icons.Default.Home),
    BottomNavItem(Routes.TAREAS,  "Tareas",  Icons.Default.List)
)

// ── Rutas donde se muestra el bottom bar ─────────────────────────────────────
private val tabRoutes = setOf(Routes.HOME, Routes.RUTINAS, Routes.TAREAS)

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // ── Repositorios: se crean una sola vez con el contexto de la app ────────
    val authRepository    = remember { MockAuthRepository(context) }
    val rutinaRepository  = remember { MockRutinaRepository(context) }
    val tareaRepository   = remember { MockTareaRepository(context) }

    // ── Use cases ─────────────────────────────────────────────────────────────
    val registerUseCase    = remember { RegisterUseCase(authRepository) }
    val loginUseCase       = remember { LoginUseCase(authRepository) }
    val sendCodeUseCase    = remember { SendResetCodeUseCase(authRepository) }
    val verifyCodeUseCase  = remember { VerifyResetCodeUseCase(authRepository) }
    val changePassUseCase  = remember { ChangePasswordUseCase(authRepository) }
    val getRutinasUseCase  = remember { GetRutinasUseCase(rutinaRepository) }
    val crearRutinaUseCase = remember { CrearRutinaUseCase(rutinaRepository) }
    val getTareasUseCase   = remember { GetTareasUseCase(tareaRepository) }
    val crearTareaUseCase  = remember { CrearTareaUseCase(tareaRepository) }

    // Estado del nombre del usuario: se setea al hacer login y persiste
    var userName by remember { mutableStateOf("") }

    // ViewModels de tabs: viven aquí para persistir al cambiar de tab
    val rutinasViewModel = viewModel<RutinasViewModel>(
        factory = vmFactory { RutinasViewModel(getRutinasUseCase, crearRutinaUseCase) }
    )
    val tareasViewModel = viewModel<TareasViewModel>(
        factory = vmFactory { TareasViewModel(getTareasUseCase, crearTareaUseCase, getRutinasUseCase) }
    )

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            // El bottom bar solo aparece en las 3 tabs principales
            if (currentRoute in tabRoutes) {
                AppBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.REGISTER,
            // El NavHost respeta el innerPadding del Scaffold externo
            // Cada pantalla recibe este padding limpio sin duplicación
        ) {

            // ── Auth (sin bottom bar) ─────────────────────────────────────────

            composable(Routes.REGISTER) {
                val vm = viewModel<RegisterViewModel>(
                    factory = vmFactory { RegisterViewModel(registerUseCase) }
                )
                RegisterScreen(
                    viewModel = vm,
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.LOGIN) {
                val vm = viewModel<LoginViewModel>(
                    factory = vmFactory { LoginViewModel(loginUseCase) }
                )
                LoginScreen(
                    viewModel = vm,
                    onNavigateToHome = { name ->
                        userName = name
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
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
                val vm = viewModel<ForgotPasswordViewModel>(
                    factory = vmFactory {
                        ForgotPasswordViewModel(sendCodeUseCase, verifyCodeUseCase, changePassUseCase)
                    }
                )
                ForgotPasswordEmailScreen(
                    viewModel = vm,
                    onCancel = { navController.popBackStack() }
                )
            }

            // ── Tabs principales (con bottom bar) ─────────────────────────────

            composable(Routes.HOME) {
                HomeScreen(
                    userName = userName,
                    onCrearTarea = { navController.navigate(Routes.CREAR_TAREA) },
                    innerPadding = innerPadding
                )
            }

            composable(Routes.RUTINAS) {
                RutinasScreen(
                    viewModel = rutinasViewModel,
                    userName = userName,
                    onNavigateToCrear = { navController.navigate(Routes.CREAR_RUTINA) },
                    innerPadding = innerPadding
                )
            }

            composable(Routes.TAREAS) {
                TareasScreen(
                    viewModel = tareasViewModel,
                    userName = userName,
                    onNavigateToCrear = { navController.navigate(Routes.CREAR_TAREA) },
                    innerPadding = innerPadding
                )
            }

            // ── Flujos secundarios (sin bottom bar) ───────────────────────────

            composable(Routes.CREAR_RUTINA) {
                CrearRutinaScreen(
                    viewModel = rutinasViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.CREAR_TAREA) {
                CrearTareaScreen(
                    viewModel = tareasViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// ── Bottom bar ────────────────────────────────────────────────────────────────
@Composable
private fun AppBottomBar(navController: NavController, currentRoute: String?) {
    NavigationBar(containerColor = SurfaceField, tonalElevation = 0.dp) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
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
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = SubtitleGray,
                    unselectedTextColor = SubtitleGray,
                    indicatorColor = AccentBlue
                )
            )
        }
    }
}

// ── Helper factory ────────────────────────────────────────────────────────────
inline fun <reified VM : androidx.lifecycle.ViewModel> vmFactory(
    crossinline create: () -> VM
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = create() as T
}
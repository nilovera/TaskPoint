package com.example.apk_mock.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.apk_mock.data.repository.JsonAuthRepository
import com.example.apk_mock.data.repository.JsonRutinaRepository
import com.example.apk_mock.data.repository.JsonTareaRepository
import com.example.apk_mock.domain.useCase.*
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
import com.example.apk_mock.ui.rutinas.RutinasScreen
import com.example.apk_mock.ui.rutinas.RutinasViewModel
import com.example.apk_mock.ui.tareas.CrearTareaScreen
import com.example.apk_mock.ui.tareas.TareasScreen
import com.example.apk_mock.ui.tareas.TareasViewModel
import com.example.apk_mock.ui.theme.*

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
private val profileRoutes = setOf(Routes.PROFILE, Routes.PROFILE_PASSWORD)
private val bottomBarRoutes = tabRoutes + profileRoutes

@Composable
fun AppNavigation() {
    val context = LocalContext.current.applicationContext
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val authRepository = remember(context) { JsonAuthRepository(context) }
    val rutinaRepository = remember(context, authRepository) { JsonRutinaRepository(context, authRepository) }
    val tareaRepository = remember(context, authRepository) { JsonTareaRepository(context, authRepository) }

    val registerUseCase = remember(authRepository) { RegisterUseCase(authRepository) }
    val loginUseCase = remember(authRepository) { LoginUseCase(authRepository) }
    val sendCodeUseCase = remember(authRepository) { SendResetCodeUseCase(authRepository) }
    val verifyCodeUseCase = remember(authRepository) { VerifyResetCodeUseCase(authRepository) }
    val changePassUseCase = remember(authRepository) { ChangePasswordUseCase(authRepository) }
    val getRutinasUseCase = remember(rutinaRepository) { GetRutinasUseCase(rutinaRepository) }
    val crearRutinaUseCase = remember(rutinaRepository) { CrearRutinaUseCase(rutinaRepository) }
    val getTareasUseCase = remember(tareaRepository) { GetTareasUseCase(tareaRepository) }
    val crearTareaUseCase = remember(tareaRepository) { CrearTareaUseCase(tareaRepository) }
    val getCurrentUserUseCase = remember(authRepository) { GetCurrentUserUseCase(authRepository) }
    val logoutUseCase = remember(authRepository) { LogoutUseCase(authRepository) }
    val changeCurrentPasswordUseCase = remember(authRepository) { ChangeCurrentPasswordUseCase(authRepository) }
    val deleteAccountUseCase = remember(authRepository) { DeleteAccountUseCase(authRepository) }

    // Estado del nombre del usuario: se setea al hacer login y persiste
    var userName by remember { mutableStateOf("") }

    // ViewModels de tabs: viven aquí para persistir al cambiar de tab
    val rutinasViewModel = viewModel<RutinasViewModel>(
        factory = vmFactory { RutinasViewModel(getRutinasUseCase, crearRutinaUseCase) }
    )
    val tareasViewModel = viewModel<TareasViewModel>(
        factory = vmFactory { TareasViewModel(getTareasUseCase, crearTareaUseCase, getRutinasUseCase) }
    )
    val profileViewModel = viewModel<ProfileViewModel>(
        factory = vmFactory {
            ProfileViewModel(
                getCurrentUserUseCase,
                logoutUseCase,
                changeCurrentPasswordUseCase,
                deleteAccountUseCase
            )
        }
    )

    val navigateToLoginAfterSessionEnd: () -> Unit = {
        authRepository.logout()
        userName = ""
        navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.HOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
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
            startDestination = Routes.ONBOARDING,
            // El NavHost respeta el innerPadding del Scaffold externo
            // Cada pantalla recibe este padding limpio sin duplicación
        ) {

            // ── Auth (sin bottom bar) ─────────────────────────────────────────

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Routes.REGISTER) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(Routes.REGISTER) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                    onLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

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
                    rutinasViewModel = rutinasViewModel,
                    tareasViewModel = tareasViewModel,
                    onCrearRutina = { navController.navigate(Routes.CREAR_RUTINA) },
                    onCrearTarea = { navController.navigate(Routes.CREAR_TAREA) },
                    onProfile = { navController.navigate(Routes.PROFILE) },
                    onLogout = {
                        navigateToLoginAfterSessionEnd()
                    },
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

            composable(Routes.PROFILE) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    userName = userName,
                    onBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Routes.HOME)
                        }
                    },
                    onChangePassword = { navController.navigate(Routes.PROFILE_PASSWORD) },
                    onSessionEnded = navigateToLoginAfterSessionEnd,
                    innerPadding = innerPadding
                )
            }

            composable(Routes.PROFILE_PASSWORD) {
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
    val selectedRoute = if (currentRoute in profileRoutes) Routes.HOME else currentRoute

    NavigationBar(containerColor = SurfaceField, tonalElevation = 0.dp) {
        bottomNavItems.forEach { item ->
            val selected = selectedRoute == item.route
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
    val isHome = item.route == Routes.HOME
    val itemShape = RoundedCornerShape(if (isHome) 24.dp else 18.dp)
    val iconSize = when {
        isHome && selected -> 27.dp
        isHome -> 23.dp
        else -> 18.dp
    }
    val horizontalPadding = when {
        isHome && selected -> 17.dp
        isHome -> 12.dp
        else -> 14.dp
    }
    val verticalPadding = when {
        isHome && selected -> 10.dp
        isHome -> 7.dp
        else -> 6.dp
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .height(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(itemShape)
                .background(if (selected) AccentBlue else Color.Transparent)
                .padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                item.icon,
                contentDescription = item.label.ifBlank { "Inicio" },
                tint = if (selected) Color.White else SubtitleGray,
                modifier = Modifier.size(iconSize)
            )
            if (item.label.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    item.label,
                    color = if (selected) Color.White else SubtitleGray,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
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

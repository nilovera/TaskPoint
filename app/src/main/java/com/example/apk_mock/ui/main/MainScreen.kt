package com.example.apk_mock.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.home.HomeScreen
import com.example.apk_mock.ui.rutinas.RutinasViewModel
import com.example.apk_mock.ui.rutinas.RutinasScreen
import com.example.apk_mock.ui.tareas.TareasViewModel
import com.example.apk_mock.ui.tareas.TareasScreen
import com.example.apk_mock.ui.theme.*

enum class MainTab { RUTINAS, HOME, TAREAS }

@Composable
fun MainScreen(
    userName: String,
    rutinasViewModel: RutinasViewModel,
    tareasViewModel: TareasViewModel,
    onNavigateToCrearRutina: () -> Unit,
    onNavigateToCrearTarea: () -> Unit,
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(containerColor = SurfaceField, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.RUTINAS,
                    onClick = { selectedTab = MainTab.RUTINAS },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Rutinas") },
                    label = { Text("Rutinas", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = SubtitleGray,
                        unselectedTextColor = SubtitleGray,
                        indicatorColor = AccentBlue
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.HOME,
                    onClick = { selectedTab = MainTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = SubtitleGray,
                        indicatorColor = AccentBlue
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.TAREAS,
                    onClick = { selectedTab = MainTab.TAREAS },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Tareas") },
                    label = { Text("Tareas", fontSize = 11.sp) },
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
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                MainTab.RUTINAS -> RutinasScreen(
                    viewModel = rutinasViewModel,
                    userName = userName,
                    onNavigateToCrear = onNavigateToCrearRutina
                )
                MainTab.HOME -> HomeScreen(
                    userName = userName,
                    rutinasViewModel = rutinasViewModel,
                    tareasViewModel = tareasViewModel,
                    onCrearRutina = onNavigateToCrearRutina,
                    onCrearTarea = onNavigateToCrearTarea,
                    onLogout = onLogout
                )
                MainTab.TAREAS -> TareasScreen(
                    viewModel = tareasViewModel,
                    userName = userName,
                    onNavigateToCrear = onNavigateToCrearTarea
                )
            }
        }
    }
}

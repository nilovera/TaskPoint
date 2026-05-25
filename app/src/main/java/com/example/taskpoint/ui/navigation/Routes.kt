package com.example.taskpoint.ui.navigation

object Routes {
    // Auth
    const val REGISTER        = "register"
    const val LOGIN           = "login"
    const val FORGOT_PASSWORD = "forgot_password"

    // Tabs principales
    const val HOME            = "home"
    const val RUTINAS         = "rutinas"
    const val TAREAS          = "tareas"

    // Flujos secundarios
    const val CREAR_RUTINA    = "crear_rutina"
    const val CREAR_TAREA     = "crear_tarea"

    // Arg para pasar el nombre del usuario a las tabs
    const val ARG_NAME        = "name"

    // Rutas con argumentos (solo para auth → tabs)
    // Usamos un wrapper para pasar el nombre via SavedStateHandle en el VM
    // En esta arquitectura el nombre se guarda en el ViewModel compartido
}
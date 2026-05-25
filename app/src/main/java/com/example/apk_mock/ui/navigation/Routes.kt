package com.example.apk_mock.ui.navigation

object Routes {
    // Onboarding
    const val ONBOARDING      = "onboarding"

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
    const val DETALLE_TAREA   = "detalle_tarea/{taskId}"

    fun detalleTarea(taskId: String): String = "detalle_tarea/$taskId"

    // Arg para pasar el nombre del usuario a las tabs
    const val ARG_NAME        = "name"

    // Rutas con argumentos (solo para auth → tabs)
    // Usamos un wrapper para pasar el nombre via SavedStateHandle en el VM
    // En esta arquitectura el nombre se guarda en el ViewModel compartido
}

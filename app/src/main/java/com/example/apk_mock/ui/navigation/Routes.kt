package com.example.apk_mock.ui.navigation

object Routes {
    // Graphs
    const val MAIN_GRAPH     = "main_graph"
    const val PROFILE_GRAPH  = "profile_graph"

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
    const val PROFILE         = "profile"
    const val PROFILE_PASSWORD = "profile_password"

    // Flujos secundarios
    const val CREAR_RUTINA    = "crear_rutina"
    const val CREAR_TAREA     = "crear_tarea"
    const val CAPTURAR_FOTO_TAREA = "capturar_foto_tarea"
    const val RUTINA_DETALLE  = "rutina_detalle"
    const val EDITAR_RUTINA   = "editar_rutina"
    const val ARG_RUTINA_ID   = "rutinaId"
    const val RUTINA_DETALLE_ROUTE = "$RUTINA_DETALLE/{$ARG_RUTINA_ID}"
    const val EDITAR_RUTINA_ROUTE = "$EDITAR_RUTINA/{$ARG_RUTINA_ID}"
    const val DETALLE_TAREA   = "detalle_tarea/{taskId}"
    const val EDITAR_TAREA    = "editar_tarea/{taskId}"

    fun detalleTarea(taskId: String): String = "detalle_tarea/$taskId"
    fun editarTarea(taskId: String): String = "editar_tarea/$taskId"

    fun rutinaDetalle(rutinaId: String) = "$RUTINA_DETALLE/$rutinaId"
    fun editarRutina(rutinaId: String) = "$EDITAR_RUTINA/$rutinaId"

    // Rutas con argumentos (solo para auth → tabs)
    // Usamos un wrapper para pasar el nombre via SavedStateHandle en el VM
    // En esta arquitectura el nombre se guarda en el ViewModel compartido
}

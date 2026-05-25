package com.example.apk_mock.domain.repository

import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea

sealed class TareaResult {
    data class Success(val tarea: Tarea) : TareaResult()
    data class Error(val message: String) : TareaResult()
}

interface TareaRepository {
    fun getTareas(): List<Tarea>
    fun actualizarNombreRutina(rutinaId: String, nuevoNombre: String): Int
    fun eliminarTareasDeRutina(rutinaId: String): Int
    fun crearTarea(
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String
    ): TareaResult

    fun editarTarea(
        taskId: String,
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String
    ): TareaResult

    fun eliminarTarea(taskId: String): TareaResult
}

package com.example.apk_mock.domain.repository

import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea
import kotlinx.coroutines.flow.Flow

sealed class TareaResult {
    data class Success(val tarea: Tarea) : TareaResult()
    data class Error(val message: String) : TareaResult()
}

interface TareaRepository {
    suspend fun getTareas(): List<Tarea>
    /**
     * Fuente observable de Room para que la UI se actualice cuando una
     * sincronizacion remota inserta o modifica tareas locales.
     */
    suspend fun observeTareas(): Flow<List<Tarea>>
    suspend fun actualizarNombreRutina(rutinaId: String, nuevoNombre: String): Int
    suspend fun eliminarTareasDeRutina(rutinaId: String): Int
    suspend fun crearTarea(
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String,
        photoPath: String?
    ): TareaResult

    suspend fun editarTarea(
        taskId: String,
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String,
        photoPath: String?
    ): TareaResult

    suspend fun eliminarTarea(taskId: String): TareaResult
}

package com.example.apk_mock.data.repository

import android.content.Context
import com.example.apk_mock.data.source.JsonDataSource
import com.example.apk_mock.data.source.StoredTarea
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.repository.TareaRepository
import com.example.apk_mock.domain.repository.TareaResult
import com.example.apk_mock.domain.repository.UserSessionProvider
import java.util.UUID

class JsonTareaRepository(
    context: Context,
    private val sessionProvider: UserSessionProvider
) : TareaRepository {

    private val dataSource = JsonDataSource(context)
    private val rutinas = dataSource.loadRutinas()
    private val tareas = dataSource.loadTareas(rutinas).toMutableList()

    override fun getTareas(): List<Tarea> {
        val userId = sessionProvider.currentUserId() ?: return emptyList()
        return tareas.filter { it.userId == userId }.map { it.tarea }
    }

    override fun actualizarNombreRutina(rutinaId: String, nuevoNombre: String): Int {
        val userId = sessionProvider.currentUserId() ?: return 0
        var updatedCount = 0
        tareas.replaceAll { stored ->
            if (stored.userId == userId && stored.tarea.rutinaId == rutinaId) {
                updatedCount++
                stored.copy(tarea = stored.tarea.copy(rutinaNombre = nuevoNombre))
            } else {
                stored
            }
        }
        if (updatedCount > 0) {
            dataSource.saveTareas(tareas)
        }
        return updatedCount
    }

    override fun eliminarTareasDeRutina(rutinaId: String): Int {
        val userId = sessionProvider.currentUserId() ?: return 0
        val before = tareas.size
        tareas.removeAll { it.userId == userId && it.tarea.rutinaId == rutinaId }
        val deletedCount = before - tareas.size
        if (deletedCount > 0) {
            dataSource.saveTareas(tareas)
        }
        return deletedCount
    }

    override fun crearTarea(
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String
    ): TareaResult {
        val userId = sessionProvider.currentUserId()
            ?: return TareaResult.Error("Inicia sesion para crear tareas.")

        val tarea = Tarea(
            id = UUID.randomUUID().toString(),
            titulo = titulo,
            categoria = categoria,
            rutinaId = rutinaId,
            rutinaNombre = rutinaNombre,
            dia = dia,
            horario = horario,
            notas = notas
        )
        tareas.add(StoredTarea(userId = userId, tarea = tarea))
        dataSource.saveTareas(tareas)
        return TareaResult.Success(tarea)
    }
}

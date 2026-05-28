package com.example.apk_mock.data.repository

import android.content.Context
import com.example.apk_mock.data.source.JsonDataSource
import com.example.apk_mock.data.source.StoredTarea
import com.example.apk_mock.data.source.TaskPhotoStorage
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
    private val photoStorage = TaskPhotoStorage(context)
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
        val deletedTasks = tareas.filter { it.userId == userId && it.tarea.rutinaId == rutinaId }
        tareas.removeAll { it.userId == userId && it.tarea.rutinaId == rutinaId }
        val deletedCount = before - tareas.size
        if (deletedCount > 0) {
            dataSource.saveTareas(tareas)
            deletedTasks.forEach { photoStorage.deletePhoto(it.tarea.photoPath) }
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
        notas: String,
        photoPath: String?
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
            notas = notas,
            photoPath = photoPath
        )
        tareas.add(StoredTarea(userId = userId, tarea = tarea))
        dataSource.saveTareas(tareas)
        return TareaResult.Success(tarea)
    }

    override fun editarTarea(
        taskId: String,
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String,
        photoPath: String?
    ): TareaResult {
        val userId = sessionProvider.currentUserId()
            ?: return TareaResult.Error("Inicia sesion para editar tareas.")

        val index = tareas.indexOfFirst { it.userId == userId && it.tarea.id == taskId }
        if (index == -1) return TareaResult.Error("No se encontro la tarea.")

        val currentPhotoPath = tareas[index].tarea.photoPath
        val updated = tareas[index].tarea.copy(
            titulo = titulo,
            categoria = categoria,
            rutinaId = rutinaId,
            rutinaNombre = rutinaNombre,
            dia = dia,
            horario = horario,
            notas = notas,
            photoPath = photoPath
        )
        tareas[index] = StoredTarea(userId = userId, tarea = updated)
        dataSource.saveTareas(tareas)
        if (currentPhotoPath != photoPath) {
            photoStorage.deletePhoto(currentPhotoPath)
        }
        return TareaResult.Success(updated)
    }

    override fun eliminarTarea(taskId: String): TareaResult {
        val userId = sessionProvider.currentUserId()
            ?: return TareaResult.Error("Inicia sesion para eliminar tareas.")

        val index = tareas.indexOfFirst { it.userId == userId && it.tarea.id == taskId }
        if (index == -1) return TareaResult.Error("No se encontro la tarea.")

        val removed = tareas.removeAt(index).tarea
        dataSource.saveTareas(tareas)
        photoStorage.deletePhoto(removed.photoPath)
        return TareaResult.Success(removed)
    }
}

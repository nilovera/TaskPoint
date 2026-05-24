package com.example.apk_mock.data.repository

import android.content.Context
import com.example.apk_mock.data.source.JsonDataSource
import com.example.apk_mock.data.source.StoredTarea
import com.example.apk_mock.domain.TareaRepository
import com.example.apk_mock.domain.TareaResult
import com.example.apk_mock.domain.UserSessionProvider
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea
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
        return TareaResult.Success(tarea)
    }
}

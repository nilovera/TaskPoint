package com.example.apk_mock.data

import com.example.apk_mock.domain.TareaRepository
import com.example.apk_mock.domain.TareaResult
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea
import java.util.UUID

class MockTareaRepository : TareaRepository {

    private val tareas = mutableListOf<Tarea>()

    override fun getTareas(): List<Tarea> = tareas.toList()

    override fun crearTarea(
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String
    ): TareaResult {
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
        tareas.add(tarea)
        return TareaResult.Success(tarea)
    }
}
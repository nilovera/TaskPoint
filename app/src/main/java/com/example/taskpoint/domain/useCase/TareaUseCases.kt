package com.example.taskpoint.domain.useCase

import com.example.taskpoint.domain.TareaRepository
import com.example.taskpoint.domain.TareaResult
import com.example.taskpoint.domain.model.CategoriaTarea
import com.example.taskpoint.domain.model.DiaSemana

class GetTareasUseCase(private val repository: TareaRepository) {
    operator fun invoke() = repository.getTareas()
}

class CrearTareaUseCase(private val repository: TareaRepository) {
    operator fun invoke(
        titulo: String,
        categoria: CategoriaTarea?,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String
    ): TareaResult {
        if (titulo.isBlank()) return TareaResult.Error("El título de la tarea es obligatorio.")
        if (categoria == null) return TareaResult.Error("Seleccioná una categoría.")
        if (rutinaId == null) return TareaResult.Error("Seleccioná una rutina asociada.")
        if (dia == null) return TareaResult.Error("Seleccioná un día.")
        if (horario.isNullOrBlank()) return TareaResult.Error("Seleccioná un horario.")
        return repository.crearTarea(titulo, categoria, rutinaId, rutinaNombre, dia, horario, notas)
    }
}
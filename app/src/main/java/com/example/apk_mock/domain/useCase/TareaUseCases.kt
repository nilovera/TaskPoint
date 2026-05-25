package com.example.apk_mock.domain.useCase

import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.repository.CategoriaRepository
import com.example.apk_mock.domain.repository.OfferRepository
import com.example.apk_mock.domain.repository.TareaRepository
import com.example.apk_mock.domain.repository.TareaResult

class GetTareasUseCase(private val repository: TareaRepository) {
    operator fun invoke() = repository.getTareas()
}

class GetCategoriasUseCase(private val repository: CategoriaRepository) {
    operator fun invoke() = repository.getCategorias()
}

class GetOffersByCategoryUseCase(private val repository: OfferRepository) {
    operator fun invoke(categoryCode: String) = repository.getOffersByCategory(categoryCode)
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

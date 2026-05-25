package com.example.apk_mock.domain.useCase

import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.repository.RutinaRepository
import com.example.apk_mock.domain.repository.RutinaResult

class GetRutinasUseCase(private val repository: RutinaRepository) {
    operator fun invoke() = repository.getRutinas()
}

class CrearRutinaUseCase(private val repository: RutinaRepository) {
    operator fun invoke(
        nombre: String,
        icono: RutinaIcono,
        direccion: String,
        dias: List<DiaSemana>,
        horarioInicio: String,
        horarioFin: String,
        descripcion: String
    ): RutinaResult {
        if (nombre.isBlank()) return RutinaResult.Error("El nombre de la rutina es obligatorio.")
        if (direccion.isBlank()) return RutinaResult.Error("La dirección es obligatoria.")
        if (dias.isEmpty()) return RutinaResult.Error("Seleccioná al menos un día.")
        if (horarioInicio.isBlank()) return RutinaResult.Error("El horario de inicio es obligatorio.")
        if (horarioFin.isBlank()) return RutinaResult.Error("El horario de fin es obligatorio.")
        if (descripcion.isBlank()) return RutinaResult.Error("La descripción es obligatoria.")
        return repository.crearRutina(nombre, icono, direccion, dias, horarioInicio, horarioFin, descripcion)
    }
}

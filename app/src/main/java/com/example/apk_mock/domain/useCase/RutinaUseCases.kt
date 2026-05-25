package com.example.apk_mock.domain.useCase

import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.repository.RutinaRepository
import com.example.apk_mock.domain.repository.RutinaResult
import com.example.apk_mock.domain.repository.TareaRepository
import java.time.LocalTime

class GetRutinasUseCase(private val repository: RutinaRepository) {
    operator fun invoke() = repository.getRutinas()
}

class GetRutinaByIdUseCase(private val repository: RutinaRepository) {
    operator fun invoke(id: String) = repository.getRutinaById(id.trim())
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
        val input = RutinaInput.from(nombre, direccion, dias, horarioInicio, horarioFin, descripcion)
        input.error?.let { return it }
        return repository.crearRutina(
            input.nombre,
            icono,
            input.direccion,
            input.dias,
            input.horarioInicio,
            input.horarioFin,
            input.descripcion
        )
    }
}

class EditarRutinaUseCase(
    private val rutinaRepository: RutinaRepository,
    private val tareaRepository: TareaRepository
) {
    operator fun invoke(
        id: String,
        nombre: String,
        icono: RutinaIcono,
        direccion: String,
        dias: List<DiaSemana>,
        horarioInicio: String,
        horarioFin: String,
        descripcion: String
    ): RutinaResult {
        val rutinaId = id.trim()
        if (rutinaId.isBlank()) return RutinaResult.Error("No se pudo identificar la rutina.")

        val input = RutinaInput.from(nombre, direccion, dias, horarioInicio, horarioFin, descripcion)
        input.error?.let { return it }

        val result = rutinaRepository.editarRutina(
            rutinaId,
            input.nombre,
            icono,
            input.direccion,
            input.dias,
            input.horarioInicio,
            input.horarioFin,
            input.descripcion
        )
        if (result is RutinaResult.Success) {
            tareaRepository.actualizarNombreRutina(result.rutina.id, result.rutina.nombre)
        }
        return result
    }
}

class EliminarRutinaUseCase(
    private val rutinaRepository: RutinaRepository,
    private val tareaRepository: TareaRepository
) {
    operator fun invoke(id: String): RutinaResult {
        val rutinaId = id.trim()
        if (rutinaId.isBlank()) return RutinaResult.Error("No se pudo identificar la rutina.")

        val result = rutinaRepository.eliminarRutina(rutinaId)
        if (result is RutinaResult.Success) {
            tareaRepository.eliminarTareasDeRutina(rutinaId)
        }
        return result
    }
}

private data class RutinaInput(
    val nombre: String,
    val direccion: String,
    val dias: List<DiaSemana>,
    val horarioInicio: String,
    val horarioFin: String,
    val descripcion: String,
    val error: RutinaResult.Error? = null
) {
    companion object {
        fun from(
            nombre: String,
            direccion: String,
            dias: List<DiaSemana>,
            horarioInicio: String,
            horarioFin: String,
            descripcion: String
        ): RutinaInput {
            val input = RutinaInput(
                nombre = nombre.trim(),
                direccion = direccion.trim(),
                dias = dias.distinct().sortedBy { it.ordinal },
                horarioInicio = horarioInicio.trim(),
                horarioFin = horarioFin.trim(),
                descripcion = descripcion.trim()
            )

            return input.copy(error = input.validate())
        }
    }

    private fun validate(): RutinaResult.Error? {
        if (nombre.isBlank()) return RutinaResult.Error("El nombre de la rutina es obligatorio.")
        if (direccion.isBlank()) return RutinaResult.Error("La dirección es obligatoria.")
        if (dias.isEmpty()) return RutinaResult.Error("Seleccioná al menos un día.")
        if (horarioInicio.isBlank()) return RutinaResult.Error("El horario de inicio es obligatorio.")
        if (horarioFin.isBlank()) return RutinaResult.Error("El horario de fin es obligatorio.")
        if (!horarioInicio.isValidHorario()) {
            return RutinaResult.Error("El horario de inicio debe tener formato HH:mm.")
        }
        if (!horarioFin.isValidHorario()) {
            return RutinaResult.Error("El horario de fin debe tener formato HH:mm.")
        }
        if (!LocalTime.parse(horarioFin).isAfter(LocalTime.parse(horarioInicio))) {
            return RutinaResult.Error("El horario de fin debe ser posterior al horario de inicio.")
        }
        if (descripcion.isBlank()) return RutinaResult.Error("La descripción es obligatoria.")
        return null
    }
}

private fun String.isValidHorario(): Boolean {
    return Regex("^([01]\\d|2[0-3]):[0-5]\\d$").matches(this)
}

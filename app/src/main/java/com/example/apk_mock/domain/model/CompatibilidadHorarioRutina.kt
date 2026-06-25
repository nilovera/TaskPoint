package com.example.apk_mock.domain.model

import java.time.Duration
import java.time.LocalTime

fun Tarea.coincideConHorario(
    diasRutina: Collection<DiaSemana>,
    horarioInicio: String,
    horarioFin: String
): Boolean {
    val diasTarea = dias.takeIf { it.isNotEmpty() } ?: return false
    val horarioTarea = horario?.let { value ->
        runCatching { LocalTime.parse(value) }.getOrNull()
    } ?: return false
    val inicio = runCatching { LocalTime.parse(horarioInicio) }.getOrNull() ?: return false
    val fin = runCatching { LocalTime.parse(horarioFin) }.getOrNull() ?: return false

    if (!diasRutina.containsAll(diasTarea) || horarioTarea.isBefore(inicio) || horarioTarea.isAfter(fin)) {
        return false
    }

    return Duration.between(inicio, horarioTarea).toMinutes() % TASK_SLOT_MINUTES == 0L
}

private const val TASK_SLOT_MINUTES = 30L

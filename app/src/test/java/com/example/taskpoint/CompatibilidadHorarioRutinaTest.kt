package com.example.taskpoint

import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.model.coincideConHorario
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompatibilidadHorarioRutinaTest {

    @Test
    fun taskInsideRoutineDayAndSlotIsCompatible() {
        assertTrue(
            tarea(DiaSemana.LUN, "10:30").coincideConHorario(
                diasRutina = listOf(DiaSemana.LUN, DiaSemana.MIE),
                horarioInicio = "09:00",
                horarioFin = "12:00"
            )
        )
    }

    @Test
    fun removedDayMakesTaskIncompatible() {
        assertFalse(
            tarea(DiaSemana.LUN, "10:30").coincideConHorario(
                diasRutina = listOf(DiaSemana.MAR),
                horarioInicio = "09:00",
                horarioFin = "12:00"
            )
        )
    }

    @Test
    fun timeOutsideRoutineMakesTaskIncompatible() {
        assertFalse(
            tarea(DiaSemana.LUN, "08:30").coincideConHorario(
                diasRutina = listOf(DiaSemana.LUN),
                horarioInicio = "09:00",
                horarioFin = "12:00"
            )
        )
    }

    @Test
    fun timeOutsideThirtyMinuteSlotsMakesTaskIncompatible() {
        assertFalse(
            tarea(DiaSemana.LUN, "10:15").coincideConHorario(
                diasRutina = listOf(DiaSemana.LUN),
                horarioInicio = "09:00",
                horarioFin = "12:00"
            )
        )
    }

    private fun tarea(dia: DiaSemana?, horario: String?) = Tarea(
        id = "tarea-1",
        titulo = "Tarea",
        categoria = CategoriaTarea(1, "Trabajo", "TRABAJO", "", false),
        rutinaId = "rutina-1",
        rutinaNombre = "Trabajo",
        dia = dia,
        horario = horario,
        notas = ""
    )
}

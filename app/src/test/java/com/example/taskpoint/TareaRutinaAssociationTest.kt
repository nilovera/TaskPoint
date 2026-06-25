package com.example.taskpoint

import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.model.perteneceARutina
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TareaRutinaAssociationTest {

    @Test
    fun taskUsesRoutineIdWhenAvailableEvenIfNameChanged() {
        val rutina = rutina(id = "rutina-1", nombre = "Trabajo nuevo")
        val tarea = tarea(rutinaId = "rutina-1", rutinaNombre = "Trabajo viejo")

        assertTrue(tarea.perteneceARutina(rutina))
    }

    @Test
    fun taskWithDifferentRoutineIdDoesNotFallbackToSameName() {
        val rutina = rutina(id = "rutina-1", nombre = "Trabajo")
        val tarea = tarea(rutinaId = "rutina-2", rutinaNombre = "Trabajo")

        assertFalse(tarea.perteneceARutina(rutina))
    }

    @Test
    fun taskWithoutRoutineIdFallsBackToRoutineName() {
        val rutina = rutina(id = "rutina-1", nombre = "Facultad")
        val tarea = tarea(rutinaId = null, rutinaNombre = "Facultad")

        assertTrue(tarea.perteneceARutina(rutina))
    }

    private fun rutina(
        id: String,
        nombre: String
    ) = Rutina(
        id = id,
        nombre = nombre,
        icono = RutinaIcono.TRABAJO,
        direccion = "Oficina",
        diasSemana = listOf(DiaSemana.LUN),
        horarioInicio = "09:00",
        horarioFin = "17:00",
        descripcion = ""
    )

    private fun tarea(
        rutinaId: String?,
        rutinaNombre: String?
    ) = Tarea(
        id = "tarea-1",
        titulo = "Tarea",
        categoria = CategoriaTarea(1, "Trabajo", "TRABAJO", "", false),
        rutinaId = rutinaId,
        rutinaNombre = rutinaNombre,
        dia = DiaSemana.LUN,
        horario = "10:00",
        notas = ""
    )
}

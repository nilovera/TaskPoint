package com.example.apk_mock.domain.useCase

import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.repository.RutinaRepository
import com.example.apk_mock.domain.repository.RutinaResult
import com.example.apk_mock.domain.repository.TareaRepository
import com.example.apk_mock.domain.repository.TareaResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditarRutinaUseCaseTest {

    @Test
    fun rechazaIdVacio() {
        val useCase = EditarRutinaUseCase(FakeEditRutinaRepository(), FakeEditTareaRepository())

        val result = useCase(
            id = " ",
            nombre = "Trabajo presencial",
            icono = RutinaIcono.TRABAJO,
            direccion = "Av. Santa Fe 3000",
            dias = listOf(DiaSemana.LUN),
            horarioInicio = "09:00",
            horarioFin = "18:00",
            descripcion = "Dias de oficina"
        )

        assertTrue(result is RutinaResult.Error)
        assertEquals("No se pudo identificar la rutina.", (result as RutinaResult.Error).message)
    }

    @Test
    fun actualizaRutinaYTareasAsociadas() {
        val rutinaRepository = FakeEditRutinaRepository()
        val tareaRepository = FakeEditTareaRepository()
        val useCase = EditarRutinaUseCase(rutinaRepository, tareaRepository)

        val result = useCase(
            id = "rutina-1",
            nombre = "Trabajo remoto",
            icono = RutinaIcono.TRABAJO,
            direccion = "Casa",
            dias = listOf(DiaSemana.LUN, DiaSemana.MIE),
            horarioInicio = "10:00",
            horarioFin = "18:00",
            descripcion = "Home office"
        )

        assertTrue(result is RutinaResult.Success)
        assertEquals("Trabajo remoto", rutinaRepository.getRutinaById("rutina-1")?.nombre)
        assertEquals("Trabajo remoto", tareaRepository.getTareas().first().rutinaNombre)
        assertEquals(1, tareaRepository.updatedCount)
    }

    @Test
    fun ordenaDiasAntesDeEditarRutina() {
        val rutinaRepository = FakeEditRutinaRepository()
        val useCase = EditarRutinaUseCase(rutinaRepository, FakeEditTareaRepository())

        val result = useCase(
            id = "rutina-1",
            nombre = "Facultad",
            icono = RutinaIcono.FACULTAD,
            direccion = "Campus",
            dias = listOf(DiaSemana.DOM, DiaSemana.MAR, DiaSemana.LUN, DiaSemana.DOM),
            horarioInicio = "12:00",
            horarioFin = "19:00",
            descripcion = "Clases"
        )

        assertTrue(result is RutinaResult.Success)
        assertEquals(
            listOf(DiaSemana.LUN, DiaSemana.MAR, DiaSemana.DOM),
            rutinaRepository.getRutinaById("rutina-1")?.diasSemana
        )
    }
}

private class FakeEditRutinaRepository : RutinaRepository {
    private val rutinas = mutableListOf(
        Rutina(
            id = "rutina-1",
            nombre = "Trabajo presencial",
            icono = RutinaIcono.TRABAJO,
            direccion = "Av. Santa Fe 3000",
            diasSemana = listOf(DiaSemana.LUN),
            horarioInicio = "09:00",
            horarioFin = "17:00",
            descripcion = "Dias de oficina"
        )
    )

    override fun getRutinas(): List<Rutina> = rutinas

    override fun getRutinaById(id: String): Rutina? = rutinas.firstOrNull { it.id == id }

    override fun crearRutina(
        nombre: String,
        icono: RutinaIcono,
        direccion: String,
        dias: List<DiaSemana>,
        horarioInicio: String,
        horarioFin: String,
        descripcion: String
    ): RutinaResult {
        error("No se usa en este test")
    }

    override fun editarRutina(
        id: String,
        nombre: String,
        icono: RutinaIcono,
        direccion: String,
        dias: List<DiaSemana>,
        horarioInicio: String,
        horarioFin: String,
        descripcion: String
    ): RutinaResult {
        val index = rutinas.indexOfFirst { it.id == id }
        if (index == -1) return RutinaResult.Error("La rutina no existe.")
        val updated = rutinas[index].copy(
            nombre = nombre,
            icono = icono,
            direccion = direccion,
            diasSemana = dias,
            horarioInicio = horarioInicio,
            horarioFin = horarioFin,
            descripcion = descripcion
        )
        rutinas[index] = updated
        return RutinaResult.Success(updated)
    }

    override fun eliminarRutina(id: String): RutinaResult {
        error("No se usa en este test")
    }
}

private class FakeEditTareaRepository : TareaRepository {
    private val tareas = mutableListOf(
        Tarea(
            id = "tarea-1",
            titulo = "Revisar agenda",
            categoria = CategoriaTarea.PERSONAL,
            rutinaId = "rutina-1",
            rutinaNombre = "Trabajo presencial",
            dia = DiaSemana.LUN,
            horario = "09:00",
            notas = ""
        )
    )
    var updatedCount = 0

    override fun getTareas(): List<Tarea> = tareas

    override fun actualizarNombreRutina(rutinaId: String, nuevoNombre: String): Int {
        tareas.replaceAll { tarea ->
            if (tarea.rutinaId == rutinaId) {
                updatedCount++
                tarea.copy(rutinaNombre = nuevoNombre)
            } else {
                tarea
            }
        }
        return updatedCount
    }

    override fun eliminarTareasDeRutina(rutinaId: String): Int = 0

    override fun crearTarea(
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String
    ): TareaResult {
        error("No se usa en este test")
    }
}

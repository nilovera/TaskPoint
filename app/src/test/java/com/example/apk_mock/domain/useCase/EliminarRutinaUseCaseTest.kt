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

class EliminarRutinaUseCaseTest {

    @Test
    fun rechazaIdVacio() {
        val useCase = EliminarRutinaUseCase(FakeDeleteRutinaRepository(), FakeDeleteTareaRepository())

        val result = useCase(" ")

        assertTrue(result is RutinaResult.Error)
        assertEquals("No se pudo identificar la rutina.", (result as RutinaResult.Error).message)
    }

    @Test
    fun eliminaRutinaYTareasAsociadas() {
        val rutinaRepository = FakeDeleteRutinaRepository()
        val tareaRepository = FakeDeleteTareaRepository()
        val useCase = EliminarRutinaUseCase(rutinaRepository, tareaRepository)

        val result = useCase("rutina-1")

        assertTrue(result is RutinaResult.Success)
        assertEquals(null, rutinaRepository.getRutinaById("rutina-1"))
        assertEquals(1, tareaRepository.deletedCount)
        assertEquals(1, tareaRepository.getTareas().size)
    }
}

private class FakeDeleteRutinaRepository : RutinaRepository {
    private val rutinas = mutableListOf(
        Rutina(
            id = "rutina-1",
            nombre = "Gimnasio",
            icono = RutinaIcono.GIMNASIO,
            direccion = "Av. Cordoba 3200",
            diasSemana = listOf(DiaSemana.LUN),
            horarioInicio = "18:00",
            horarioFin = "19:00",
            descripcion = "Entrenamiento"
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
        error("No se usa en este test")
    }

    override fun eliminarRutina(id: String): RutinaResult {
        val rutina = getRutinaById(id) ?: return RutinaResult.Error("La rutina no existe.")
        rutinas.remove(rutina)
        return RutinaResult.Success(rutina)
    }
}

private class FakeDeleteTareaRepository : TareaRepository {
    private val tareas = mutableListOf(
        Tarea(
            id = "tarea-1",
            titulo = "Llevar notebook",
            categoria = testCategory("PERSONAL"),
            rutinaId = "rutina-1",
            rutinaNombre = "Gimnasio",
            dia = DiaSemana.LUN,
            horario = "18:00",
            notas = ""
        ),
        Tarea(
            id = "tarea-2",
            titulo = "Tarea de otra rutina",
            categoria = testCategory("ESTUDIO"),
            rutinaId = "rutina-2",
            rutinaNombre = "Estudio",
            dia = DiaSemana.MAR,
            horario = "19:00",
            notas = ""
        )
    )
    var deletedCount = 0

    override fun getTareas(): List<Tarea> = tareas

    override fun actualizarNombreRutina(rutinaId: String, nuevoNombre: String): Int {
        return 0
    }

    override fun eliminarTareasDeRutina(rutinaId: String): Int {
        val before = tareas.size
        tareas.removeAll { it.rutinaId == rutinaId }
        deletedCount = before - tareas.size
        return deletedCount
    }

    override fun crearTarea(
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String,
        photoPath: String?
    ): TareaResult {
        error("No se usa en este test")
    }

    override fun editarTarea(
        taskId: String,
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String,
        photoPath: String?
    ): TareaResult {
        error("No se usa en este test")
    }

    override fun eliminarTarea(taskId: String): TareaResult {
        error("No se usa en este test")
    }
}

private fun testCategory(code: String): CategoriaTarea {
    return CategoriaTarea(
        id = -1,
        name = code.lowercase().replaceFirstChar { it.uppercase() },
        code = code,
        description = "",
        activatesOffers = false
    )
}

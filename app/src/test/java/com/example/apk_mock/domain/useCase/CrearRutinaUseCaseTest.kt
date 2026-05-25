package com.example.apk_mock.domain.useCase

import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.repository.RutinaRepository
import com.example.apk_mock.domain.repository.RutinaResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrearRutinaUseCaseTest {

    private val repository = FakeRutinaRepository()
    private val useCase = CrearRutinaUseCase(repository)

    @Test
    fun rechazaHorarioSinDosPuntos() {
        val result = useCase(
            nombre = "Gimnasio",
            icono = RutinaIcono.GIMNASIO,
            direccion = "Av. Cordoba 3200",
            dias = listOf(DiaSemana.LUN),
            horarioInicio = "1800",
            horarioFin = "19:00",
            descripcion = "Entrenamiento"
        )

        assertTrue(result is RutinaResult.Error)
        assertEquals("El horario de inicio debe tener formato HH:mm.", (result as RutinaResult.Error).message)
    }

    @Test
    fun rechazaHorarioFinAnteriorOIgualAlInicio() {
        val result = useCase(
            nombre = "Gimnasio",
            icono = RutinaIcono.GIMNASIO,
            direccion = "Av. Cordoba 3200",
            dias = listOf(DiaSemana.LUN),
            horarioInicio = "18:00",
            horarioFin = "18:00",
            descripcion = "Entrenamiento"
        )

        assertTrue(result is RutinaResult.Error)
        assertEquals(
            "El horario de fin debe ser posterior al horario de inicio.",
            (result as RutinaResult.Error).message
        )
    }

    @Test
    fun creaRutinaConHorarioValido() {
        val result = useCase(
            nombre = "Gimnasio",
            icono = RutinaIcono.GIMNASIO,
            direccion = "Av. Cordoba 3200",
            dias = listOf(DiaSemana.LUN),
            horarioInicio = "18:00",
            horarioFin = "19:00",
            descripcion = "Entrenamiento"
        )

        assertTrue(result is RutinaResult.Success)
        assertEquals("18:00", repository.createdRutina?.horarioInicio)
        assertEquals("19:00", repository.createdRutina?.horarioFin)
    }

    @Test
    fun ordenaDiasAntesDeCrearRutina() {
        val result = useCase(
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
            repository.createdRutina?.diasSemana
        )
    }
}

private class FakeRutinaRepository : RutinaRepository {
    var createdRutina: Rutina? = null

    override fun getRutinas(): List<Rutina> = emptyList()

    override fun getRutinaById(id: String): Rutina? = createdRutina?.takeIf { it.id == id }

    override fun crearRutina(
        nombre: String,
        icono: RutinaIcono,
        direccion: String,
        dias: List<DiaSemana>,
        horarioInicio: String,
        horarioFin: String,
        descripcion: String
    ): RutinaResult {
        val rutina = Rutina(
            id = "rutina-test",
            nombre = nombre,
            icono = icono,
            direccion = direccion,
            diasSemana = dias,
            horarioInicio = horarioInicio,
            horarioFin = horarioFin,
            descripcion = descripcion
        )
        createdRutina = rutina
        return RutinaResult.Success(rutina)
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
        val rutina = createdRutina ?: return RutinaResult.Error("La rutina no existe.")
        createdRutina = null
        return RutinaResult.Success(rutina)
    }
}

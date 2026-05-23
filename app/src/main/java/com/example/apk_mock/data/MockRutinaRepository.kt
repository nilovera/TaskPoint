package com.example.apk_mock.data

import com.example.apk_mock.domain.RutinaRepository
import com.example.apk_mock.domain.RutinaResult
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import java.util.UUID

class MockRutinaRepository : RutinaRepository {

    private val rutinas = mutableListOf<Rutina>()

    override fun getRutinas(): List<Rutina> = rutinas.toList()

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
            id = UUID.randomUUID().toString(),
            nombre = nombre,
            icono = icono,
            direccion = direccion,
            diasSemana = dias,
            horarioInicio = horarioInicio,
            horarioFin = horarioFin,
            descripcion = descripcion,
            cantidadTareas = 0
        )
        rutinas.add(rutina)
        return RutinaResult.Success(rutina)
    }
}
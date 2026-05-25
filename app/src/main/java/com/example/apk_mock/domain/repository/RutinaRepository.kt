package com.example.apk_mock.domain.repository

import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono

sealed class RutinaResult {
    data class Success(val rutina: Rutina) : RutinaResult()
    data class Error(val message: String) : RutinaResult()
}

interface RutinaRepository {
    fun getRutinas(): List<Rutina>
    fun crearRutina(
        nombre: String,
        icono: RutinaIcono,
        direccion: String,
        dias: List<DiaSemana>,
        horarioInicio: String,
        horarioFin: String,
        descripcion: String
    ): RutinaResult
}

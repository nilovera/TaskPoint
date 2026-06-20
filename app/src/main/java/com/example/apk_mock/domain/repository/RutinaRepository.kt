package com.example.apk_mock.domain.repository

import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono

sealed class RutinaResult {
    data class Success(val rutina: Rutina) : RutinaResult()
    data class Error(val message: String) : RutinaResult()
}

interface RutinaRepository {
    suspend fun getRutinas(): List<Rutina>
    suspend fun getRutinaById(id: String): Rutina?
    suspend fun crearRutina(
        nombre: String,
        icono: RutinaIcono,
        direccion: String,
        dias: List<DiaSemana>,
        horarioInicio: String,
        horarioFin: String,
        descripcion: String
    ): RutinaResult
    suspend fun editarRutina(
        id: String,
        nombre: String,
        icono: RutinaIcono,
        direccion: String,
        dias: List<DiaSemana>,
        horarioInicio: String,
        horarioFin: String,
        descripcion: String
    ): RutinaResult
    suspend fun eliminarRutina(id: String): RutinaResult
}

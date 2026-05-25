package com.example.apk_mock.data.repository

import android.content.Context
import com.example.apk_mock.data.source.JsonDataSource
import com.example.apk_mock.data.source.StoredRutina
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.repository.RutinaRepository
import com.example.apk_mock.domain.repository.RutinaResult
import com.example.apk_mock.domain.repository.UserSessionProvider
import java.util.UUID

class JsonRutinaRepository(
    context: Context,
    private val sessionProvider: UserSessionProvider
) : RutinaRepository {

    private val rutinas = JsonDataSource(context).loadRutinas().toMutableList()

    override fun getRutinas(): List<Rutina> {
        val userId = sessionProvider.currentUserId() ?: return emptyList()
        return rutinas.filter { it.userId == userId }.map { it.rutina }
    }

    override fun crearRutina(
        nombre: String,
        icono: RutinaIcono,
        direccion: String,
        dias: List<DiaSemana>,
        horarioInicio: String,
        horarioFin: String,
        descripcion: String
    ): RutinaResult {
        val userId = sessionProvider.currentUserId()
            ?: return RutinaResult.Error("Inicia sesion para crear rutinas.")

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
        rutinas.add(StoredRutina(userId = userId, rutina = rutina))
        return RutinaResult.Success(rutina)
    }
}

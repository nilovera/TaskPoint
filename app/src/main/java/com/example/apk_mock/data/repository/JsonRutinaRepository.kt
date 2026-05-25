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

    private val dataSource = JsonDataSource(context)
    private val rutinas = dataSource.loadRutinas().toMutableList()

    override fun getRutinas(): List<Rutina> {
        val userId = sessionProvider.currentUserId() ?: return emptyList()
        return rutinas.filter { it.userId == userId }.map { it.rutina }
    }

    override fun getRutinaById(id: String): Rutina? {
        val userId = sessionProvider.currentUserId() ?: return null
        return rutinas.firstOrNull { it.userId == userId && it.rutina.id == id }?.rutina
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
        dataSource.saveRutinas(rutinas)
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
        val userId = sessionProvider.currentUserId()
            ?: return RutinaResult.Error("Inicia sesion para editar rutinas.")

        val index = rutinas.indexOfFirst { it.userId == userId && it.rutina.id == id }
        if (index == -1) {
            return RutinaResult.Error("La rutina no existe o no pertenece a tu cuenta.")
        }

        val current = rutinas[index]
        val updated = current.rutina.copy(
            nombre = nombre,
            icono = icono,
            direccion = direccion,
            diasSemana = dias,
            horarioInicio = horarioInicio,
            horarioFin = horarioFin,
            descripcion = descripcion
        )
        rutinas[index] = current.copy(rutina = updated)
        dataSource.saveRutinas(rutinas)
        return RutinaResult.Success(updated)
    }

    override fun eliminarRutina(id: String): RutinaResult {
        val userId = sessionProvider.currentUserId()
            ?: return RutinaResult.Error("Inicia sesion para eliminar rutinas.")

        val index = rutinas.indexOfFirst { it.userId == userId && it.rutina.id == id }
        if (index == -1) {
            return RutinaResult.Error("La rutina no existe o no pertenece a tu cuenta.")
        }

        val rutina = rutinas.removeAt(index).rutina
        dataSource.saveRutinas(rutinas)
        return RutinaResult.Success(rutina)
    }
}

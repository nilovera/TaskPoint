package com.example.taskpoint.data

import android.content.Context
import com.example.taskpoint.domain.RutinaRepository
import com.example.taskpoint.domain.RutinaResult
import com.example.taskpoint.domain.model.DiaSemana
import com.example.taskpoint.domain.model.Rutina
import com.example.taskpoint.domain.model.RutinaIcono
import org.json.JSONArray
import java.util.UUID

/**
 * Implementación mockeada con JSON.
 * - Carga rutinas iniciales desde assets/rutinas.json
 * - Las rutinas nuevas se agregan en memoria durante la sesión
 */
class MockRutinaRepository(private val context: Context) : RutinaRepository {

    private val rutinas = mutableListOf<Rutina>()

    init {
        loadRutinasFromJson()
    }

    // ── Carga desde JSON ──────────────────────────────────────────────────────

    private fun loadRutinasFromJson() {
        try {
            val json = context.assets.open("rutinas.json")
                .bufferedReader()
                .use { it.readText() }
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                // Parsear lista de días
                val diasArray = obj.getJSONArray("diasSemana")
                val dias = (0 until diasArray.length()).map { j ->
                    DiaSemana.valueOf(diasArray.getString(j))
                }

                // Parsear ícono (fallback a OTRO si no existe)
                val icono = try {
                    RutinaIcono.valueOf(obj.getString("icono"))
                } catch (e: Exception) {
                    RutinaIcono.OTRO
                }

                rutinas.add(
                    Rutina(
                        id             = obj.getString("id"),
                        nombre         = obj.getString("nombre"),
                        icono          = icono,
                        direccion      = obj.getString("direccion"),
                        diasSemana     = dias,
                        horarioInicio  = obj.getString("horarioInicio"),
                        horarioFin     = obj.getString("horarioFin"),
                        descripcion    = obj.getString("descripcion"),
                        cantidadTareas = obj.optInt("cantidadTareas", 0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Repositorio ───────────────────────────────────────────────────────────

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
            id            = UUID.randomUUID().toString(),
            nombre        = nombre,
            icono         = icono,
            direccion     = direccion,
            diasSemana    = dias,
            horarioInicio = horarioInicio,
            horarioFin    = horarioFin,
            descripcion   = descripcion,
            cantidadTareas = 0
        )
        rutinas.add(rutina)
        return RutinaResult.Success(rutina)
    }
}
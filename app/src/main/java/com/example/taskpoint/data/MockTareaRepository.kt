package com.example.taskpoint.data

import android.content.Context
import com.example.taskpoint.domain.TareaRepository
import com.example.taskpoint.domain.TareaResult
import com.example.taskpoint.domain.model.CategoriaTarea
import com.example.taskpoint.domain.model.DiaSemana
import com.example.taskpoint.domain.model.Tarea
import org.json.JSONArray
import java.util.UUID

/**
 * Implementación mockeada con JSON.
 * - Carga tareas iniciales desde assets/tareas.json
 * - Las tareas nuevas se agregan en memoria durante la sesión
 */
class MockTareaRepository(private val context: Context) : TareaRepository {

    private val tareas = mutableListOf<Tarea>()

    init {
        loadTareasFromJson()
    }

    // ── Carga desde JSON ──────────────────────────────────────────────────────

    private fun loadTareasFromJson() {
        try {
            val json = context.assets.open("tareas.json")
                .bufferedReader()
                .use { it.readText() }
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)

                // Parsear categoría (fallback a PERSONAL)
                val categoria = try {
                    CategoriaTarea.valueOf(obj.getString("categoria"))
                } catch (e: Exception) {
                    CategoriaTarea.PERSONAL
                }

                // Parsear día (puede ser null)
                val dia = try {
                    val diaStr = obj.optString("dia", "")
                    if (diaStr.isBlank()) null else DiaSemana.valueOf(diaStr)
                } catch (e: Exception) {
                    null
                }

                tareas.add(
                    Tarea(
                        id           = obj.getString("id"),
                        titulo       = obj.getString("titulo"),
                        categoria    = categoria,
                        rutinaId     = obj.optString("rutinaId").ifBlank { null },
                        rutinaNombre = obj.optString("rutinaNombre").ifBlank { null },
                        dia          = dia,
                        horario      = obj.optString("horario").ifBlank { null },
                        notas        = obj.optString("notas", ""),
                        completada   = obj.optBoolean("completada", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Repositorio ───────────────────────────────────────────────────────────

    override fun getTareas(): List<Tarea> = tareas.toList()

    override fun crearTarea(
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String
    ): TareaResult {
        val tarea = Tarea(
            id           = UUID.randomUUID().toString(),
            titulo       = titulo,
            categoria    = categoria,
            rutinaId     = rutinaId,
            rutinaNombre = rutinaNombre,
            dia          = dia,
            horario      = horario,
            notas        = notas
        )
        tareas.add(tarea)
        return TareaResult.Success(tarea)
    }
}

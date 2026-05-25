package com.example.apk_mock.data.source

import android.content.Context
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.repository.User
import org.json.JSONArray
import org.json.JSONObject

data class StoredRutina(
    val userId: String,
    val rutina: Rutina
)

data class StoredTarea(
    val userId: String,
    val tarea: Tarea
)

class JsonDataSource(private val context: Context) {

    fun loadUsers(): List<User> {
        return readRows("seed/users.json").map { row ->
            User(
                id = row.getString("id"),
                name = row.getString("name"),
                email = row.getString("email"),
                password = row.getString("password")
            )
        }
    }

    fun loadRutinas(): List<StoredRutina> {
        return readRows("seed/rutinas.json").map { row ->
            StoredRutina(
                userId = row.getString("userId"),
                rutina = Rutina(
                    id = row.getString("id"),
                    nombre = row.getString("nombre"),
                    icono = RutinaIcono.valueOf(row.getString("icono")),
                    direccion = row.getString("direccion"),
                    diasSemana = row.getJSONArray("diasSemana").toStringList().map { DiaSemana.valueOf(it) },
                    horarioInicio = row.getString("horarioInicio"),
                    horarioFin = row.getString("horarioFin"),
                    descripcion = row.getString("descripcion"),
                    cantidadTareas = row.optInt("cantidadTareas", 0)
                )
            )
        }
    }

    fun loadTareas(rutinas: List<StoredRutina>): List<StoredTarea> {
        val rutinasById = rutinas.associateBy { it.rutina.id }

        return readRows("seed/tareas.json").map { row ->
            val rutinaId = row.optNullableString("rutinaId")
            val rutina = rutinaId?.let { rutinasById[it]?.rutina }

            StoredTarea(
                userId = row.getString("userId"),
                tarea = Tarea(
                    id = row.getString("id"),
                    titulo = row.getString("titulo"),
                    categoria = CategoriaTarea.valueOf(row.getString("categoria")),
                    rutinaId = rutinaId,
                    rutinaNombre = rutina?.nombre,
                    dia = row.optNullableString("dia")?.let { DiaSemana.valueOf(it) },
                    horario = row.optNullableString("horario"),
                    notas = row.optString("notas", ""),
                    completada = row.optBoolean("completada", false)
                )
            )
        }
    }

    private fun readRows(assetPath: String): List<JSONObject> {
        val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val rows = JSONObject(json).getJSONArray("rows")
        return rows.toObjectList()
    }
}

private fun JSONArray.toObjectList(): List<JSONObject> {
    return List(length()) { index -> getJSONObject(index) }
}

private fun JSONArray.toStringList(): List<String> {
    return List(length()) { index -> getString(index) }
}

private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key) || isNull(key)) return null
    return optString(key).takeIf { it.isNotBlank() }
}

package com.example.apk_mock.data.source

import android.content.Context
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.repository.User
import java.io.File
import java.time.Instant
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

    fun saveRutinas(rutinas: List<StoredRutina>) {
        val rows = JSONArray()
        val now = Instant.now().toString()

        rutinas.forEach { stored ->
            val rutina = stored.rutina
            rows.put(
                JSONObject()
                    .put("id", rutina.id)
                    .put("userId", stored.userId)
                    .put("nombre", rutina.nombre)
                    .put("icono", rutina.icono.name)
                    .put("direccion", rutina.direccion)
                    .put("diasSemana", JSONArray().apply {
                        rutina.diasSemana.forEach { put(it.name) }
                    })
                    .put("horarioInicio", rutina.horarioInicio)
                    .put("horarioFin", rutina.horarioFin)
                    .put("descripcion", rutina.descripcion)
                    .put("cantidadTareas", rutina.cantidadTareas)
                    .put("createdAt", now)
                    .put("updatedAt", now)
            )
        }

        val json = JSONObject()
            .put("schemaVersion", 1)
            .put("table", "rutinas")
            .put("primaryKey", "id")
            .put("foreignKeys", JSONObject().put("userId", "users.id"))
            .put("rows", rows)

        writableAssetCopy("seed/rutinas.json").writeText(json.toString(2), Charsets.UTF_8)
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

    fun saveTareas(tareas: List<StoredTarea>) {
        val rows = JSONArray()
        val now = Instant.now().toString()

        tareas.forEach { stored ->
            val tarea = stored.tarea
            rows.put(
                JSONObject()
                    .put("id", tarea.id)
                    .put("userId", stored.userId)
                    .put("rutinaId", tarea.rutinaId)
                    .put("titulo", tarea.titulo)
                    .put("categoria", tarea.categoria.name)
                    .put("dia", tarea.dia?.name)
                    .put("horario", tarea.horario)
                    .put("notas", tarea.notas)
                    .put("completada", tarea.completada)
                    .put("createdAt", now)
                    .put("updatedAt", now)
            )
        }

        val json = JSONObject()
            .put("schemaVersion", 1)
            .put("table", "tareas")
            .put("primaryKey", "id")
            .put(
                "foreignKeys",
                JSONObject()
                    .put("userId", "users.id")
                    .put("rutinaId", "rutinas.id")
            )
            .put("rows", rows)

        writableAssetCopy("seed/tareas.json").writeText(json.toString(2), Charsets.UTF_8)
    }

    private fun readRows(assetPath: String): List<JSONObject> {
        val json = readJson(assetPath)
        val rows = JSONObject(json).getJSONArray("rows")
        return rows.toObjectList()
    }

    private fun readJson(assetPath: String): String {
        if (assetPath == "seed/rutinas.json" || assetPath == "seed/tareas.json") {
            return writableAssetCopy(assetPath).readText(Charsets.UTF_8)
        }

        return context.assets.open(assetPath).bufferedReader().use { it.readText() }
    }

    private fun writableAssetCopy(assetPath: String): File {
        val file = File(context.filesDir, assetPath)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return file
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

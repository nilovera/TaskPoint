package com.example.apk_mock.data.sync

import com.example.apk_mock.data.local.entity.RutinaEntity
import com.example.apk_mock.data.local.entity.TareaEntity
import org.json.JSONArray
import org.json.JSONObject

internal fun RutinaEntity.toSyncPayloadJson(): String {
    return JSONObject()
        .put("id", id)
        .put("nombre", nombre)
        .put("icono", icono)
        .put("direccion", direccion)
        .put("latitude", latitude)
        .put("longitude", longitude)
        .put("diasSemana", JSONArray(diasSemana.split(",").filter { it.isNotBlank() }))
        .put("horarioInicio", horarioInicio)
        .put("horarioFin", horarioFin)
        .put("descripcion", descripcion)
        .put("updatedAt", updatedAt)
        .toString()
}

internal fun TareaEntity.toSyncPayloadJson(): String {
    return JSONObject()
        .put("id", id)
        .put("titulo", titulo)
        .put("categoriaCode", categoriaCode)
        .put("rutinaId", rutinaId)
        .put("rutinaNombre", rutinaNombre)
        .put("dia", dia)
        .put("horario", horario)
        .put("notas", notas)
        .put("photoPath", photoPath)
        .put("completada", completada)
        .put("requiereRevisionHorario", requiereRevisionHorario)
        .put("updatedAt", updatedAt)
        .toString()
}

internal fun deleteSyncPayloadJson(updatedAt: Long): String {
    return JSONObject()
        .put("updatedAt", updatedAt)
        .toString()
}

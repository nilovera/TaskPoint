package com.example.apk_mock.data.mapper

import com.example.apk_mock.data.local.SyncStatus
import com.example.apk_mock.data.local.entity.RutinaEntity
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono

private const val LIST_SEPARATOR = ","

fun RutinaEntity.toDomain(cantidadTareas: Int = 0): Rutina {
    return Rutina(
        id = id,
        nombre = nombre,
        icono = icono.toRutinaIcono(),
        direccion = direccion,
        diasSemana = diasSemana.toDiasSemanaList(),
        horarioInicio = horarioInicio,
        horarioFin = horarioFin,
        descripcion = descripcion,
        cantidadTareas = cantidadTareas
    )
}

fun Rutina.toEntity(
    userId: String,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    updatedAt: Long = System.currentTimeMillis()
): RutinaEntity {
    return RutinaEntity(
        id = id,
        userId = userId,
        nombre = nombre,
        icono = icono.name,
        direccion = direccion,
        diasSemana = diasSemana.toStorageString(),
        horarioInicio = horarioInicio,
        horarioFin = horarioFin,
        descripcion = descripcion,
        syncStatus = syncStatus,
        updatedAt = updatedAt
    )
}

fun List<RutinaEntity>.toRutinaDomainList(): List<Rutina> {
    return map { it.toDomain() }
}

private fun String.toRutinaIcono(): RutinaIcono {
    return RutinaIcono.entries.firstOrNull { it.name == this } ?: RutinaIcono.OTRO
}

private fun String.toDiasSemanaList(): List<DiaSemana> {
    if (isBlank()) return emptyList()
    return split(LIST_SEPARATOR)
        .mapNotNull { value -> DiaSemana.entries.firstOrNull { it.name == value.trim() } }
}

private fun List<DiaSemana>.toStorageString(): String {
    return joinToString(separator = LIST_SEPARATOR) { it.name }
}

package com.example.apk_mock.data.mapper

import com.example.apk_mock.data.local.SyncStatus
import com.example.apk_mock.data.local.entity.TareaEntity
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea

fun TareaEntity.toDomain(): Tarea {
    return Tarea(
        id = id,
        titulo = titulo,
        categoria = CategoriaTarea(
            id = categoriaId,
            name = categoriaName,
            code = categoriaCode,
            description = categoriaDescription,
            activatesOffers = categoriaActivatesOffers
        ),
        rutinaId = rutinaId,
        rutinaNombre = rutinaNombre,
        dias = dia.toDiasSemana(),
        horario = horario,
        notas = notas,
        photoPath = photoPath,
        completada = completada,
        requiereRevisionHorario = requiereRevisionHorario
    )
}

fun Tarea.toEntity(
    userId: String,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    updatedAt: Long = System.currentTimeMillis()
): TareaEntity {
    return TareaEntity(
        id = id,
        userId = userId,
        titulo = titulo,
        categoriaId = categoria.id,
        categoriaName = categoria.name,
        categoriaCode = categoria.code,
        categoriaDescription = categoria.description,
        categoriaActivatesOffers = categoria.activatesOffers,
        rutinaId = rutinaId,
        rutinaNombre = rutinaNombre,
        dia = dias.toStorageValue(),
        horario = horario,
        notas = notas,
        photoPath = photoPath,
        completada = completada,
        requiereRevisionHorario = requiereRevisionHorario,
        syncStatus = syncStatus,
        updatedAt = updatedAt
    )
}

fun List<TareaEntity>.toTareaDomainList(): List<Tarea> {
    return map { it.toDomain() }
}

private fun String.toDiaSemanaOrNull(): DiaSemana? {
    return enumValues<DiaSemana>().firstOrNull { it.name == this }
}

private fun String?.toDiasSemana(): List<DiaSemana> {
    if (isNullOrBlank()) return emptyList()
    return split(",")
        .mapNotNull { value -> value.trim().toDiaSemanaOrNull() }
        .distinct()
        .sortedBy { it.ordinal }
}

private fun List<DiaSemana>.toStorageValue(): String? {
    return distinct()
        .sortedBy { it.ordinal }
        .joinToString(separator = ",") { it.name }
        .takeIf { it.isNotBlank() }
}

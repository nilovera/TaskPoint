package com.example.apk_mock.data.remote.dto

data class TaskDto(
    val id: String,
    val titulo: String,
    val categoriaCode: String,
    val rutinaId: String?,
    val rutinaNombre: String?,
    val dia: String?,
    val horario: String?,
    val notas: String,
    val photoPath: String?,
    val completada: Boolean,
    val requiereRevisionHorario: Boolean = false,
    val updatedAt: Long? = null
)

data class TaskRequestDto(
    val id: String,
    val titulo: String,
    val categoriaCode: String,
    val rutinaId: String?,
    val rutinaNombre: String?,
    val dia: String?,
    val horario: String?,
    val notas: String,
    val photoPath: String?,
    val completada: Boolean,
    val requiereRevisionHorario: Boolean,
    val updatedAt: Long? = null
)

data class TaskSyncDto(
    val id: String,
    val deleted: Boolean = false,
    val titulo: String? = null,
    val categoriaCode: String? = null,
    val rutinaId: String? = null,
    val rutinaNombre: String? = null,
    val dia: String? = null,
    val horario: String? = null,
    val notas: String? = null,
    val photoPath: String? = null,
    val completada: Boolean? = null,
    val requiereRevisionHorario: Boolean? = null,
    val updatedAt: Long
)

data class DeleteRequestDto(
    val updatedAt: Long
)

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
    val updatedAt: Long? = null
)

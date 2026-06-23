package com.example.apk_mock.data.remote.dto

data class RoutineDto(
    val id: String,
    val nombre: String,
    val icono: String,
    val direccion: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val diasSemana: List<String>,
    val horarioInicio: String,
    val horarioFin: String,
    val descripcion: String,
    val updatedAt: Long? = null
)

data class RoutineSyncDto(
    val id: String,
    val deleted: Boolean = false,
    val nombre: String? = null,
    val icono: String? = null,
    val direccion: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val diasSemana: List<String>? = null,
    val horarioInicio: String? = null,
    val horarioFin: String? = null,
    val descripcion: String? = null,
    val updatedAt: Long
)

data class RoutineRequestDto(
    val id: String,
    val nombre: String,
    val icono: String,
    val direccion: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val diasSemana: List<String>,
    val horarioInicio: String,
    val horarioFin: String,
    val descripcion: String,
    val updatedAt: Long? = null
)

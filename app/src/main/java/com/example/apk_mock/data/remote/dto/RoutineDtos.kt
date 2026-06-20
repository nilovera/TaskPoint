package com.example.apk_mock.data.remote.dto

data class RoutineDto(
    val id: String,
    val nombre: String,
    val icono: String,
    val direccion: String,
    val diasSemana: List<String>,
    val horarioInicio: String,
    val horarioFin: String,
    val descripcion: String,
    val updatedAt: Long? = null
)

data class RoutineRequestDto(
    val id: String,
    val nombre: String,
    val icono: String,
    val direccion: String,
    val diasSemana: List<String>,
    val horarioInicio: String,
    val horarioFin: String,
    val descripcion: String,
    val updatedAt: Long? = null
)

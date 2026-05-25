package com.example.taskpoint.domain.model

data class Tarea(
    val id: String,
    val titulo: String,
    val categoria: CategoriaTarea,
    val rutinaId: String?,
    val rutinaNombre: String?,
    val dia: DiaSemana?,
    val horario: String?,
    val notas: String,
    val completada: Boolean = false
)

enum class CategoriaTarea(val label: String) {
    PERSONAL("PERSONAL"),
    SUPERMERCADO("SUPERMERCADO"),
    INDUMENTARIA("INDUMENTARIA"),
    FACULTAD("FACULTAD"),
    ESTUDIO("ESTUDIO")
}
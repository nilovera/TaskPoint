package com.example.taskpoint.domain.model

data class Rutina(
    val id: String,
    val nombre: String,
    val icono: RutinaIcono,
    val direccion: String,
    val diasSemana: List<DiaSemana>,
    val horarioInicio: String,
    val horarioFin: String,
    val descripcion: String,
    val cantidadTareas: Int = 0
)

enum class DiaSemana(val label: String) {
    LUN("LUN"), MAR("MAR"), MIE("MIÉ"), JUE("JUE"), VIE("VIE"), SAB("SÁB"), DOM("DOM")
}

enum class RutinaIcono(val emoji: String, val colorHex: Long) {
    TRABAJO(    "💼", 0xFF3B5FE0),
    GIMNASIO(   "🏋️", 0xFF2E7D32),
    FACULTAD(   "📚", 0xFF8B4513),
    ESTUDIO(    "✏️", 0xFF6A1B9A),
    HOGAR(      "🏠", 0xFFB8860B),
    SALUD(      "❤️", 0xFFB71C1C),
    DEPORTE(    "⚽", 0xFF1565C0),
    OTRO(       "⭐", 0xFF4A148C)
}
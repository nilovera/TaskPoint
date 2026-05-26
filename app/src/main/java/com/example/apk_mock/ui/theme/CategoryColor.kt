package com.example.apk_mock.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.apk_mock.domain.model.CategoriaTarea

fun CategoriaTarea.categoryColor(): Color = when (code.uppercase()) {
    "SUPERMERCADO" -> Color(0xFF34C759)
    "FARMACIA" -> Color(0xFFE85D75)
    "TRABAJO" -> Color(0xFFE5CF00)
    "FACULTAD" -> Color(0xFF8B5CF6)
    "GIMNASIO" -> Color(0xFF1F7735)
    "MEDICO" -> Color(0xFFEF4444)
    "BANCO" -> Color(0xFFF59E0B)
    "ESCUELA" -> Color(0xFF38BDF8)
    "VETERINARIA" -> Color(0xFF14B8A6)
    "FERRETERIA" -> Color(0xFFFF9F0A)
    "PANADERIA" -> Color(0xFFEAB308)
    "TRANSPORTE" -> Color(0xFF06B6D4)
    "LIBRERIA" -> Color(0xFFA855F7)
    "PELUQUERIA" -> Color(0xFFEC4899)
    "CASA" -> Color(0xFF84CC16)
    "PERSONAL" -> Color(0xFF818CF8)
    "INDUMENTARIA" -> Color(0xFFFF9F0A)
    "ESTUDIO" -> Color(0xFF06B6D4)
    else -> Color(0xFF8A8FA8)
}

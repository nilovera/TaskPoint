package com.example.apk_mock.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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

@Immutable
data class CategoryChipColors(
    val container: Color,
    val content: Color,
    val border: Color? = null
)

@Composable
fun CategoriaTarea.categoryChipColors(selected: Boolean = false): CategoryChipColors {
    val isLightTheme = TaskPointTheme.colors.background.luminance() > 0.5f
    val palette = categoryPalette()

    return when {
        isLightTheme && selected -> CategoryChipColors(
            container = palette.lightContent,
            content = Color.White
        )
        isLightTheme -> CategoryChipColors(
            container = palette.lightContainer,
            content = palette.lightContent,
            border = palette.lightContent.copy(alpha = 0.18f)
        )
        selected -> CategoryChipColors(
            container = palette.darkContent.copy(alpha = 0.95f),
            content = Color.White
        )
        else -> CategoryChipColors(
            container = palette.darkContent.copy(alpha = 0.22f),
            content = palette.darkContent,
            border = palette.darkContent.copy(alpha = 0.52f)
        )
    }
}

private data class CategoryPalette(
    val darkContent: Color,
    val lightContent: Color,
    val lightContainer: Color
)

private fun CategoriaTarea.categoryPalette(): CategoryPalette = when (code.uppercase()) {
    "SUPERMERCADO" -> CategoryPalette(Color(0xFF34C759), Color(0xFF1A8C4D), Color(0xFFD1F5DB))
    "FARMACIA" -> CategoryPalette(Color(0xFFE85D75), Color(0xFFB83255), Color(0xFFFFD6E1))
    "TRABAJO" -> CategoryPalette(Color(0xFFE5CF00), Color(0xFF806C00), Color(0xFFFFF5B8))
    "FACULTAD" -> CategoryPalette(Color(0xFF8B5CF6), Color(0xFFB85914), Color(0xFFFFE5C7))
    "GIMNASIO" -> CategoryPalette(Color(0xFF1F7735), Color(0xFFFF2024), Color(0xFFF2B7BE))
    "MEDICO" -> CategoryPalette(Color(0xFFEF4444), Color(0xFFC93050), Color(0xFFFFD5DC))
    "BANCO" -> CategoryPalette(Color(0xFFF59E0B), Color(0xFF9A5F00), Color(0xFFFFE6B8))
    "ESCUELA" -> CategoryPalette(Color(0xFF38BDF8), Color(0xFF177EA6), Color(0xFFD3F0FF))
    "VETERINARIA" -> CategoryPalette(Color(0xFF14B8A6), Color(0xFF087E72), Color(0xFFD1F3EF))
    "FERRETERIA" -> CategoryPalette(Color(0xFFFF9F0A), Color(0xFFB85914), Color(0xFFFFE1C2))
    "PANADERIA" -> CategoryPalette(Color(0xFFEAB308), Color(0xFF8A6500), Color(0xFFFFEFB8))
    "TRANSPORTE" -> CategoryPalette(Color(0xFF06B6D4), Color(0xFF007A91), Color(0xFFD1F3FF))
    "LIBRERIA" -> CategoryPalette(Color(0xFFA855F7), Color(0xFF7C3AC8), Color(0xFFEBD8FF))
    "PELUQUERIA" -> CategoryPalette(Color(0xFFEC4899), Color(0xFFB8327A), Color(0xFFFFD7EC))
    "CASA" -> CategoryPalette(Color(0xFF84CC16), Color(0xFF5D8F0F), Color(0xFFE4F6C9))
    "PERSONAL" -> CategoryPalette(Color(0xFF818CF8), Color(0xFF336BE0), Color(0xFFD1E3FF))
    "INDUMENTARIA" -> CategoryPalette(Color(0xFFFF9F0A), Color(0xFFD45A2A), Color(0xFFFFE1D6))
    "ESTUDIO" -> CategoryPalette(Color(0xFF06B6D4), Color(0xFF0086A8), Color(0xFFD1F3FF))
    else -> CategoryPalette(Color(0xFF8A8FA8), Color(0xFF6B7280), Color(0xFFE5E7EB))
}

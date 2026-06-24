package com.example.apk_mock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.ui.theme.TaskPointTheme

@Composable
fun FiltrosDias(seleccionado: DiaSemana?, onSelect: (DiaSemana?) -> Unit) {
    val opciones: List<DiaSemana?> = listOf(null) + DiaSemana.values().toList()
    val colors = TaskPointTheme.colors

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        opciones.forEach { dia ->
            val isSelected = dia == seleccionado
            Surface(
                onClick = { onSelect(dia) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) colors.primary else colors.surface,
                border = if (isSelected) null else BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .semantics {
                        contentDescription = if (dia == null) {
                            "Mostrar rutinas de todos los dias"
                        } else {
                            "Mostrar rutinas de ${dia.label}"
                        }
                        selected = isSelected
                        role = Role.RadioButton
                    }
            ) {
                Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = dia?.label ?: "Todas",
                        fontSize = 16.sp,
                        color = if (isSelected) Color.White else colors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

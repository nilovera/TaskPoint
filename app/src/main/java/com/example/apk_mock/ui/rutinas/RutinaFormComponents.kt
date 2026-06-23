package com.example.apk_mock.ui.rutinas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.ui.components.appTextFieldColors
import com.example.apk_mock.ui.theme.TaskPointTheme

internal enum class RutinaIconosLayout {
    Horizontal,
    Grid
}

@Composable
internal fun RutinaIconosGrid(
    seleccionado: RutinaIcono,
    onSelect: (RutinaIcono) -> Unit,
    layout: RutinaIconosLayout = RutinaIconosLayout.Horizontal,
    itemSize: Dp = 52.dp,
    cornerRadius: Dp = 12.dp,
    emojiFontSize: TextUnit = 22.sp,
    selectedBorderColor: Color = Color.White,
    spacing: Dp = 10.dp,
    columns: Int = 5,
    trailingSpace: Dp = 24.dp
) {
    val iconos = RutinaIcono.values().toList()
    val scrollState = rememberScrollState()

    when (layout) {
        RutinaIconosLayout.Horizontal -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            iconos.forEach { icono ->
                RutinaIconoOption(
                    icono = icono,
                    selected = icono == seleccionado,
                    onClick = { onSelect(icono) },
                    itemSize = itemSize,
                    cornerRadius = cornerRadius,
                    emojiFontSize = emojiFontSize,
                    selectedBorderColor = selectedBorderColor
                )
            }
            Spacer(Modifier.width(trailingSpace))
        }

        RutinaIconosLayout.Grid -> Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            iconos.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    row.forEach { icono ->
                        RutinaIconoOption(
                            icono = icono,
                            selected = icono == seleccionado,
                            onClick = { onSelect(icono) },
                            itemSize = itemSize,
                            cornerRadius = cornerRadius,
                            emojiFontSize = emojiFontSize,
                            selectedBorderColor = selectedBorderColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RutinaIconoOption(
    icono: RutinaIcono,
    selected: Boolean,
    onClick: () -> Unit,
    itemSize: Dp,
    cornerRadius: Dp,
    emojiFontSize: TextUnit,
    selectedBorderColor: Color
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = Modifier
            .size(itemSize)
            .clip(shape)
            .background(Color(icono.colorHex))
            .then(
                if (selected) Modifier.border(2.dp, selectedBorderColor, shape)
                else Modifier
            )
            .semantics {
                contentDescription = "Icono ${icono.name.lowercase()}"
                this.selected = selected
                role = Role.RadioButton
            }
            .clickable(
                role = Role.RadioButton,
                onClickLabel = "Seleccionar icono ${icono.name.lowercase()}",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(icono.emoji, fontSize = emojiFontSize)
    }
}

@Composable
internal fun RutinaDiasSelector(
    seleccionados: Set<DiaSemana>,
    onToggle: (DiaSemana) -> Unit,
    spacing: Dp = 6.dp,
    horizontalPadding: Dp = 10.dp,
    selectedFontWeight: FontWeight = FontWeight.SemiBold,
    scrollable: Boolean = false
) {
    val scrollState = rememberScrollState()
    val colors = TaskPointTheme.colors

    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = if (scrollable) Modifier.horizontalScroll(scrollState) else Modifier
    ) {
        DiaSemana.values().forEach { dia ->
            val selected = dia in seleccionados
            Surface(
                onClick = { onToggle(dia) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) colors.primary else colors.surface,
                border = if (selected) null else BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .semantics {
                        contentDescription = "Día ${dia.label}"
                        stateDescription = if (selected) "Seleccionado" else "No seleccionado"
                        role = Role.Checkbox
                    }
            ) {
                Box(
                    Modifier.padding(horizontal = horizontalPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        dia.label,
                        fontSize = 12.sp,
                        color = if (selected) Color.White else colors.textSecondary,
                        fontWeight = if (selected) selectedFontWeight else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
internal fun RutinaHorarioField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    cornerRadius: Dp = 12.dp
) {
    val colors = TaskPointTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = colors.placeholder) },
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(cornerRadius),
        colors = appTextFieldColors()
    )
}

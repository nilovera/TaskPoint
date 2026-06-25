package com.example.apk_mock.ui.rutinas

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.apk_mock.ui.theme.TaskPointTheme
import java.util.Locale

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RutinaDiasSelector(
    seleccionados: Set<DiaSemana>,
    onToggle: (DiaSemana) -> Unit,
    spacing: Dp = 6.dp,
    horizontalPadding: Dp = 10.dp,
    selectedFontWeight: FontWeight = FontWeight.SemiBold
) {
    val colors = TaskPointTheme.colors

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        maxItemsInEachRow = 4,
        modifier = Modifier.fillMaxWidth()
    ) {
        DiaSemana.values().forEach { dia ->
            val selected = dia in seleccionados
            Surface(
                onClick = { onToggle(dia) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) colors.primary else colors.surface,
                border = if (selected) null else BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .width(64.dp)
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
    val context = LocalContext.current
    val onTimeSelected by rememberUpdatedState(onValueChange)
    val (initialHour, initialMinute) = remember(value) { value.toTimeParts() }
    val timePickerDialog = remember(context, initialHour, initialMinute) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                onTimeSelected(String.format(Locale.US, "%02d:%02d", hour, minute))
            },
            initialHour,
            initialMinute,
            true
        )
    }

    Surface(
        onClick = timePickerDialog::show,
        shape = RoundedCornerShape(cornerRadius),
        color = colors.surface,
        border = BorderStroke(1.dp, if (isError) colors.destructive else colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics {
                contentDescription = "${if (value.isBlank()) placeholder else value}. Abrir selector de hora"
                role = Role.Button
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.ifBlank { placeholder },
                color = if (value.isBlank()) colors.placeholder else colors.textPrimary,
                fontSize = 16.sp
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = colors.textSecondary
            )
        }
    }
}

private fun String.toTimeParts(): Pair<Int, Int> {
    val pieces = split(":")
    val hour = pieces.getOrNull(0)?.toIntOrNull()?.takeIf { it in 0..23 } ?: 9
    val minute = pieces.getOrNull(1)?.toIntOrNull()?.takeIf { it in 0..59 } ?: 0
    return hour to minute
}

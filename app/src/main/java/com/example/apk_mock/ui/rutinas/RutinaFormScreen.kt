package com.example.apk_mock.ui.rutinas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.ui.components.AppTextArea
import com.example.apk_mock.ui.components.AppTextField
import com.example.apk_mock.ui.components.AppTopBar
import com.example.apk_mock.ui.components.AppTopBarSize
import com.example.apk_mock.ui.components.FormFieldLabel
import com.example.apk_mock.ui.theme.TaskPointTheme

internal data class RutinaFormStyle(
    val horizontalPadding: Dp,
    val fieldSpacing: Dp,
    val topBarTitleFontWeight: FontWeight,
    val iconLayout: RutinaIconosLayout,
    val iconItemSize: Dp,
    val iconCornerRadius: Dp,
    val iconEmojiFontSize: TextUnit,
    val daySpacing: Dp,
    val dayHorizontalPadding: Dp,
    val daySelectedFontWeight: FontWeight,
    val timeRowSpacing: Dp,
    val timeLabelFontSize: TextUnit,
    val timeStartPlaceholder: String,
    val timeEndPlaceholder: String,
    val timeCornerRadius: Dp,
    val descriptionCounterFontSize: TextUnit,
    val buttonHeight: Dp,
    val buttonCornerRadius: Dp,
    val buttonFontSize: TextUnit,
    val buttonFontWeight: FontWeight
)

internal object RutinaFormStyles {
    val Create = RutinaFormStyle(
        horizontalPadding = 20.dp,
        fieldSpacing = 20.dp,
        topBarTitleFontWeight = FontWeight.SemiBold,
        iconLayout = RutinaIconosLayout.Horizontal,
        iconItemSize = 52.dp,
        iconCornerRadius = 12.dp,
        iconEmojiFontSize = 22.sp,
        daySpacing = 6.dp,
        dayHorizontalPadding = 10.dp,
        daySelectedFontWeight = FontWeight.SemiBold,
        timeRowSpacing = 12.dp,
        timeLabelFontSize = 12.sp,
        timeStartPlaceholder = "18:00",
        timeEndPlaceholder = "19:00",
        timeCornerRadius = 12.dp,
        descriptionCounterFontSize = 11.sp,
        buttonHeight = 54.dp,
        buttonCornerRadius = 14.dp,
        buttonFontSize = 16.sp,
        buttonFontWeight = FontWeight.SemiBold
    )

    val Edit = RutinaFormStyle(
        horizontalPadding = 24.dp,
        fieldSpacing = 18.dp,
        topBarTitleFontWeight = FontWeight.Bold,
        iconLayout = RutinaIconosLayout.Grid,
        iconItemSize = 42.dp,
        iconCornerRadius = 9.dp,
        iconEmojiFontSize = 18.sp,
        daySpacing = 8.dp,
        dayHorizontalPadding = 12.dp,
        daySelectedFontWeight = FontWeight.Bold,
        timeRowSpacing = 46.dp,
        timeLabelFontSize = 16.sp,
        timeStartPlaceholder = "09:00",
        timeEndPlaceholder = "18:00",
        timeCornerRadius = 10.dp,
        descriptionCounterFontSize = 16.sp,
        buttonHeight = 50.dp,
        buttonCornerRadius = 10.dp,
        buttonFontSize = 15.sp,
        buttonFontWeight = FontWeight.Bold
    )
}

@Composable
internal fun RutinaFormScreen(
    title: String,
    submitText: String,
    nombre: String,
    iconoSeleccionado: RutinaIcono,
    direccion: String,
    diasSeleccionados: Set<DiaSemana>,
    horarioInicio: String,
    horarioFin: String,
    descripcion: String,
    nombreError: String?,
    direccionError: String?,
    diasError: String?,
    horarioInicioError: String?,
    horarioFinError: String?,
    descripcionError: String?,
    onBack: () -> Unit,
    onNombreChange: (String) -> Unit,
    onIconoChange: (RutinaIcono) -> Unit,
    onDireccionChange: (String) -> Unit,
    onDiaToggle: (DiaSemana) -> Unit,
    onHorarioInicioChange: (String) -> Unit,
    onHorarioFinChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    style: RutinaFormStyle,
    submitEnabled: Boolean = true,
    overlayContent: @Composable BoxScope.() -> Unit = {}
) {
    val colors = TaskPointTheme.colors
    val maxDescriptionLength = 120

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = style.horizontalPadding)
                .padding(bottom = 100.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            AppTopBar(
                title = title,
                onBack = onBack,
                size = AppTopBarSize.Compact,
                titleFontWeight = style.topBarTitleFontWeight
            )

            Spacer(Modifier.height(24.dp))

            FormFieldLabel("Nombre de la rutina", required = true)
            AppTextField(
                label = "",
                value = nombre,
                onValueChange = onNombreChange,
                placeholder = "Gimnasio",
                isError = nombreError != null,
                errorMessage = nombreError
            )

            Spacer(Modifier.height(style.fieldSpacing))

            FormFieldLabel("Ícono", required = true)
            RutinaIconosGrid(
                seleccionado = iconoSeleccionado,
                onSelect = onIconoChange,
                layout = style.iconLayout,
                itemSize = style.iconItemSize,
                cornerRadius = style.iconCornerRadius,
                emojiFontSize = style.iconEmojiFontSize,
                selectedBorderColor = colors.primary
            )

            Spacer(Modifier.height(style.fieldSpacing))

            FormFieldLabel("Dirección", required = true)
            AppTextField(
                label = "",
                value = direccion,
                onValueChange = onDireccionChange,
                placeholder = "Ingresá una dirección...",
                isError = direccionError != null,
                errorMessage = direccionError
            )

            Spacer(Modifier.height(style.fieldSpacing))

            FormFieldLabel("Días de la semana", required = true)
            RutinaDiasSelector(
                seleccionados = diasSeleccionados,
                onToggle = onDiaToggle,
                spacing = style.daySpacing,
                horizontalPadding = style.dayHorizontalPadding,
                selectedFontWeight = style.daySelectedFontWeight
            )
            diasError?.let { error ->
                Spacer(Modifier.height(4.dp))
                Text(error, color = colors.destructive, fontSize = 12.sp)
            }

            Spacer(Modifier.height(style.fieldSpacing))

            FormFieldLabel("Horario", required = true)
            Row(horizontalArrangement = Arrangement.spacedBy(style.timeRowSpacing)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Inicio", color = colors.textSecondary, fontSize = style.timeLabelFontSize)
                    Spacer(Modifier.height(4.dp))
                    RutinaHorarioField(
                        value = horarioInicio,
                        onValueChange = onHorarioInicioChange,
                        placeholder = style.timeStartPlaceholder,
                        isError = horarioInicioError != null,
                        cornerRadius = style.timeCornerRadius
                    )
                    horarioInicioError?.let { error ->
                        Text(error, color = colors.destructive, fontSize = 11.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fin", color = colors.textSecondary, fontSize = style.timeLabelFontSize)
                    Spacer(Modifier.height(4.dp))
                    RutinaHorarioField(
                        value = horarioFin,
                        onValueChange = onHorarioFinChange,
                        placeholder = style.timeEndPlaceholder,
                        isError = horarioFinError != null,
                        cornerRadius = style.timeCornerRadius
                    )
                    horarioFinError?.let { error ->
                        Text(error, color = colors.destructive, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(style.fieldSpacing))

            FormFieldLabel("Descripción", required = true)
            AppTextArea(
                value = descripcion,
                onValueChange = onDescripcionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = "Agrega una descripcion...",
                isError = descripcionError != null,
                errorMessage = descripcionError,
                maxLength = maxDescriptionLength,
                counterFontSize = style.descriptionCounterFontSize
            )

            Spacer(Modifier.height(28.dp))
        }

        Button(
            onClick = onSubmit,
            enabled = submitEnabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = style.horizontalPadding, vertical = 20.dp)
                .height(style.buttonHeight),
            shape = RoundedCornerShape(style.buttonCornerRadius),
            colors = ButtonDefaults.buttonColors(containerColor = colors.success)
        ) {
            Text(
                submitText,
                fontSize = style.buttonFontSize,
                fontWeight = style.buttonFontWeight,
                color = Color.White
            )
        }

        overlayContent()
    }
}

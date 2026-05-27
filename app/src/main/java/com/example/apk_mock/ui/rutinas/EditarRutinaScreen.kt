package com.example.apk_mock.ui.rutinas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.components.AppTextArea
import com.example.apk_mock.ui.components.AppTextField
import com.example.apk_mock.ui.components.AppTopBar
import com.example.apk_mock.ui.components.AppTopBarSize
import com.example.apk_mock.ui.components.FormFieldLabel
import com.example.apk_mock.ui.theme.AccentBlue
import com.example.apk_mock.ui.theme.BackgroundDark
import com.example.apk_mock.ui.theme.ErrorRed
import com.example.apk_mock.ui.theme.StrengthGreen
import com.example.apk_mock.ui.theme.SubtitleGray
import com.example.apk_mock.ui.theme.SurfaceField

@Composable
fun EditarRutinaScreen(
    rutinaId: String,
    viewModel: RutinasViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.editState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(rutinaId) {
        viewModel.loadEditarRutina(rutinaId)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeEditError()
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeEditSuccess()
            onSaved()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 100.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            AppTopBar(
                title = "Editar rutina",
                onBack = onBack,
                size = AppTopBarSize.Compact,
                titleFontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            FormFieldLabel("Nombre de la rutina", required = true)
            AppTextField(
                label = "",
                value = state.nombre,
                onValueChange = viewModel::onEditNombreChange,
                placeholder = "Gimnasio",
                isError = state.nombreError != null,
                errorMessage = state.nombreError
            )

            Spacer(Modifier.height(18.dp))

            FormFieldLabel("Ícono", required = true)
            RutinaIconosGrid(
                seleccionado = state.iconoSeleccionado,
                onSelect = viewModel::onEditIconoChange,
                layout = RutinaIconosLayout.Grid,
                itemSize = 42.dp,
                cornerRadius = 9.dp,
                emojiFontSize = 18.sp,
                selectedBorderColor = AccentBlue
            )

            Spacer(Modifier.height(18.dp))

            FormFieldLabel("Dirección", required = true)
            AppTextField(
                label = "",
                value = state.direccion,
                onValueChange = viewModel::onEditDireccionChange,
                placeholder = "Ingresá una dirección...",
                isError = state.direccionError != null,
                errorMessage = state.direccionError
            )

            Spacer(Modifier.height(18.dp))

            FormFieldLabel("Días de la semana", required = true)
            RutinaDiasSelector(
                seleccionados = state.diasSeleccionados,
                onToggle = viewModel::onEditDiaToggle,
                spacing = 8.dp,
                horizontalPadding = 12.dp,
                selectedFontWeight = FontWeight.Bold,
                scrollable = true
            )
            if (state.diasError != null) {
                Spacer(Modifier.height(4.dp))
                Text(state.diasError!!, color = ErrorRed, fontSize = 12.sp)
            }

            Spacer(Modifier.height(18.dp))

            FormFieldLabel("Horario", required = true)
            Row(horizontalArrangement = Arrangement.spacedBy(46.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Inicio", color = SubtitleGray, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    RutinaHorarioField(
                        value = state.horarioInicio,
                        onValueChange = viewModel::onEditHorarioInicioChange,
                        placeholder = "09:00",
                        isError = state.horarioInicioError != null,
                        cornerRadius = 10.dp
                    )
                    if (state.horarioInicioError != null) {
                        Text(state.horarioInicioError!!, color = ErrorRed, fontSize = 11.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fin", color = SubtitleGray, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    RutinaHorarioField(
                        value = state.horarioFin,
                        onValueChange = viewModel::onEditHorarioFinChange,
                        placeholder = "18:00",
                        isError = state.horarioFinError != null,
                        cornerRadius = 10.dp
                    )
                    if (state.horarioFinError != null) {
                        Text(state.horarioFinError!!, color = ErrorRed, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            FormFieldLabel("Descripción", required = true)
            val maxDesc = 120
            AppTextArea(
                value = state.descripcion,
                onValueChange = viewModel::onEditDescripcionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = "Agrega una descripcion...",
                isError = state.descripcionError != null,
                errorMessage = state.descripcionError,
                maxLength = maxDesc,
                counterFontSize = 11.sp
            )

            Spacer(Modifier.height(28.dp))
        }

        Button(
            onClick = viewModel::onGuardarCambiosRutina,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StrengthGreen)
        ) {
            Text("Guardar cambios", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 86.dp)
        ) { data ->
            Snackbar(
                containerColor = SurfaceField,
                contentColor = Color.White,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(data.visuals.message, fontSize = 13.sp)
            }
        }
    }
}


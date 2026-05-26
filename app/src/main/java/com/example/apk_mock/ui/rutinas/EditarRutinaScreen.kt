package com.example.apk_mock.ui.rutinas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.ui.components.FormFieldLabel
import com.example.apk_mock.ui.register.AppTextField
import com.example.apk_mock.ui.theme.AccentBlue
import com.example.apk_mock.ui.theme.BackgroundDark
import com.example.apk_mock.ui.theme.ErrorRed
import com.example.apk_mock.ui.theme.FieldBorder
import com.example.apk_mock.ui.theme.PlaceholderGray
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(SurfaceField, RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = SubtitleGray)
                }
                Text("Editar rutina", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(36.dp))
            }

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
            EditIconosGrid(
                seleccionado = state.iconoSeleccionado,
                onSelect = viewModel::onEditIconoChange
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
            EditDiasSelector(
                seleccionados = state.diasSeleccionados,
                onToggle = viewModel::onEditDiaToggle
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
                    EditHorarioField(
                        value = state.horarioInicio,
                        onValueChange = viewModel::onEditHorarioInicioChange,
                        placeholder = "09:00",
                        isError = state.horarioInicioError != null
                    )
                    if (state.horarioInicioError != null) {
                        Text(state.horarioInicioError!!, color = ErrorRed, fontSize = 11.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fin", color = SubtitleGray, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    EditHorarioField(
                        value = state.horarioFin,
                        onValueChange = viewModel::onEditHorarioFinChange,
                        placeholder = "18:00",
                        isError = state.horarioFinError != null
                    )
                    if (state.horarioFinError != null) {
                        Text(state.horarioFinError!!, color = ErrorRed, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            FormFieldLabel("Descripción", required = true)
            val maxDesc = 120
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = { if (it.length <= maxDesc) viewModel.onEditDescripcionChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = { Text("Agrega una descripcion...", color = PlaceholderGray, fontSize = 14.sp) },
                isError = state.descripcionError != null,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceField,
                    unfocusedContainerColor = SurfaceField,
                    errorContainerColor = Color(0xFF2A1515),
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = FieldBorder,
                    errorBorderColor = ErrorRed,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    errorTextColor = Color.White,
                    cursorColor = AccentBlue
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (state.descripcionError != null) {
                    Text(state.descripcionError!!, color = ErrorRed, fontSize = 12.sp)
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Text("${state.descripcion.length}/$maxDesc", color = SubtitleGray, fontSize = 11.sp)
            }

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

@Composable
private fun EditIconosGrid(seleccionado: RutinaIcono, onSelect: (RutinaIcono) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RutinaIcono.values().toList().chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { icono ->
                    val isSelected = icono == seleccionado
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(icono.colorHex))
                            .then(
                                if (isSelected) Modifier.border(2.dp, AccentBlue, RoundedCornerShape(9.dp))
                                else Modifier
                            )
                            .clickable { onSelect(icono) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icono.emoji, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditDiasSelector(seleccionados: Set<DiaSemana>, onToggle: (DiaSemana) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        DiaSemana.values().forEach { dia ->
            val selected = dia in seleccionados
            Surface(
                onClick = { onToggle(dia) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) AccentBlue else SurfaceField,
                modifier = Modifier.height(32.dp)
            ) {
                Box(Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                    Text(
                        dia.label,
                        fontSize = 12.sp,
                        color = if (selected) Color.White else SubtitleGray,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun EditHorarioField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = PlaceholderGray) },
        singleLine = true,
        isError = isError,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceField,
            unfocusedContainerColor = SurfaceField,
            errorContainerColor = Color(0xFF2A1515),
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = FieldBorder,
            errorBorderColor = ErrorRed,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = AccentBlue
        )
    )
}

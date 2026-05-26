package com.example.apk_mock.ui.rutinas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.ui.components.FormFieldLabel
import com.example.apk_mock.ui.register.AppTextField
import com.example.apk_mock.ui.theme.*

@Composable
fun CrearRutinaScreen(
    viewModel: RutinasViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.formState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            onBack()
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Header ────────────────────────────────────────────────────────
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
                Text("Nueva rutina", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(36.dp))
            }

            Spacer(Modifier.height(24.dp))

            // ── Nombre ────────────────────────────────────────────────────────
            FormFieldLabel("Nombre de la rutina", required = true)
            AppTextField(
                label = "",
                value = state.nombre,
                onValueChange = viewModel::onNombreChange,
                placeholder = "Gimnasio",
                isError = state.nombreError != null,
                errorMessage = state.nombreError
            )

            Spacer(Modifier.height(20.dp))

            // ── Ícono ─────────────────────────────────────────────────────────
            FormFieldLabel("Ícono", required = true)
            IconosGrid(
                seleccionado = state.iconoSeleccionado,
                onSelect = viewModel::onIconoChange
            )

            Spacer(Modifier.height(20.dp))

            // ── Dirección ─────────────────────────────────────────────────────
            FormFieldLabel("Dirección", required = true)
            AppTextField(
                label = "",
                value = state.direccion,
                onValueChange = viewModel::onDireccionChange,
                placeholder = "Ingresá una dirección...",
                isError = state.direccionError != null,
                errorMessage = state.direccionError
            )

            Spacer(Modifier.height(20.dp))

            // ── Días de la semana ─────────────────────────────────────────────
            FormFieldLabel("Días de la semana", required = true)
            DiasSelector(
                seleccionados = state.diasSeleccionados,
                onToggle = viewModel::onDiaToggle
            )
            if (state.diasError != null) {
                Spacer(Modifier.height(4.dp))
                Text(state.diasError!!, color = ErrorRed, fontSize = 12.sp)
            }

            Spacer(Modifier.height(20.dp))

            // ── Horario ───────────────────────────────────────────────────────
            FormFieldLabel("Horario", required = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Inicio", color = SubtitleGray, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    HorarioField(
                        value = state.horarioInicio,
                        onValueChange = viewModel::onHorarioInicioChange,
                        placeholder = "18:00",
                        isError = state.horarioInicioError != null
                    )
                    if (state.horarioInicioError != null) {
                        Text(state.horarioInicioError!!, color = ErrorRed, fontSize = 11.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fin", color = SubtitleGray, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    HorarioField(
                        value = state.horarioFin,
                        onValueChange = viewModel::onHorarioFinChange,
                        placeholder = "19:00",
                        isError = state.horarioFinError != null
                    )
                    if (state.horarioFinError != null) {
                        Text(state.horarioFinError!!, color = ErrorRed, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Descripción ───────────────────────────────────────────────────
            FormFieldLabel("Descripción", required = true)
            val maxDesc = 120
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = { if (it.length <= maxDesc) viewModel.onDescripcionChange(it) },
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
                if (state.descripcionError != null)
                    Text(state.descripcionError!!, color = ErrorRed, fontSize = 12.sp)
                else Spacer(Modifier.width(1.dp))
                Text("${state.descripcion.length}/$maxDesc", color = SubtitleGray, fontSize = 11.sp)
            }

            Spacer(Modifier.height(28.dp))
        }

        // ── Botón fijo abajo ──────────────────────────────────────────────────
        Button(
            onClick = viewModel::onCrearRutina,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StrengthGreen)
        ) {
            Text("Crear rutina", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

// ── Componentes internos ──────────────────────────────────────────────────────

@Composable
private fun IconosGrid(seleccionado: RutinaIcono, onSelect: (RutinaIcono) -> Unit) {
    val iconos = RutinaIcono.values()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        iconos.forEach { icono ->
            val isSelected = icono == seleccionado
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(icono.colorHex))
                    .then(
                        if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(12.dp))
                        else Modifier
                    )
                    .clickable { onSelect(icono) },
                contentAlignment = Alignment.Center
            ) {
                Text(icono.emoji, fontSize = 22.sp)
            }
        }
        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun DiasSelector(seleccionados: Set<DiaSemana>, onToggle: (DiaSemana) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DiaSemana.values().forEach { dia ->
            val selected = seleccionados.contains(dia)
            Surface(
                onClick = { onToggle(dia) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) AccentBlue else SurfaceField,
                modifier = Modifier.height(32.dp)
            ) {
                Box(Modifier.padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                    Text(
                        dia.label,
                        fontSize = 12.sp,
                        color = if (selected) Color.White else SubtitleGray,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun HorarioField(
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
            cursorColor = AccentBlue
        )
    )
}

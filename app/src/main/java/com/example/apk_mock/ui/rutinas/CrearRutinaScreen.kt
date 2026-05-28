package com.example.apk_mock.ui.rutinas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.apk_mock.ui.theme.TaskPointTheme

@Composable
fun CrearRutinaScreen(
    viewModel: RutinasViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.formState.collectAsState()
    val colors = TaskPointTheme.colors

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            onBack()
        }
    }

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Header ────────────────────────────────────────────────────────
            AppTopBar(
                title = "Nueva rutina",
                onBack = onBack,
                size = AppTopBarSize.Compact
            )

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
            RutinaIconosGrid(
                seleccionado = state.iconoSeleccionado,
                onSelect = viewModel::onIconoChange,
                selectedBorderColor = colors.primary
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
            RutinaDiasSelector(
                seleccionados = state.diasSeleccionados,
                onToggle = viewModel::onDiaToggle
            )
            if (state.diasError != null) {
                Spacer(Modifier.height(4.dp))
                Text(state.diasError!!, color = colors.destructive, fontSize = 12.sp)
            }

            Spacer(Modifier.height(20.dp))

            // ── Horario ───────────────────────────────────────────────────────
            FormFieldLabel("Horario", required = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Inicio", color = colors.textSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    RutinaHorarioField(
                        value = state.horarioInicio,
                        onValueChange = viewModel::onHorarioInicioChange,
                        placeholder = "18:00",
                        isError = state.horarioInicioError != null
                    )
                    if (state.horarioInicioError != null) {
                        Text(state.horarioInicioError!!, color = colors.destructive, fontSize = 11.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fin", color = colors.textSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    RutinaHorarioField(
                        value = state.horarioFin,
                        onValueChange = viewModel::onHorarioFinChange,
                        placeholder = "19:00",
                        isError = state.horarioFinError != null
                    )
                    if (state.horarioFinError != null) {
                        Text(state.horarioFinError!!, color = colors.destructive, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Descripción ───────────────────────────────────────────────────
            FormFieldLabel("Descripción", required = true)
            val maxDesc = 120
            AppTextArea(
                value = state.descripcion,
                onValueChange = viewModel::onDescripcionChange,
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

        // ── Botón fijo abajo ──────────────────────────────────────────────────
        Button(
            onClick = viewModel::onCrearRutina,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.success)
        ) {
            Text("Crear rutina", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

// ── Componentes internos ──────────────────────────────────────────────────────


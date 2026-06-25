package com.example.apk_mock.ui.rutinas

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apk_mock.ui.components.AppConfirmDialog
import com.example.apk_mock.ui.theme.TaskPointTheme

@Composable
fun EditarRutinaScreen(
    rutinaId: String,
    viewModel: RutinasViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.editState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = TaskPointTheme.colors

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

    RutinaFormScreen(
        title = "Editar rutina",
        submitText = "Guardar cambios",
        nombre = state.nombre,
        iconoSeleccionado = state.iconoSeleccionado,
        direccion = state.direccion,
        diasSeleccionados = state.diasSeleccionados,
        horarioInicio = state.horarioInicio,
        horarioFin = state.horarioFin,
        descripcion = state.descripcion,
        nombreError = state.nombreError,
        direccionError = state.direccionError,
        diasError = state.diasError,
        horarioInicioError = state.horarioInicioError,
        horarioFinError = state.horarioFinError,
        descripcionError = state.descripcionError,
        onBack = onBack,
        onNombreChange = viewModel::onEditNombreChange,
        onIconoChange = viewModel::onEditIconoChange,
        onDireccionChange = viewModel::onEditDireccionChange,
        onDiaToggle = viewModel::onEditDiaToggle,
        onHorarioInicioChange = viewModel::onEditHorarioInicioChange,
        onHorarioFinChange = viewModel::onEditHorarioFinChange,
        onDescripcionChange = viewModel::onEditDescripcionChange,
        onSubmit = viewModel::onGuardarCambiosRutina,
        style = RutinaFormStyles.Edit,
        submitEnabled = !state.isSaving
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 86.dp)
        ) { data ->
            Snackbar(
                containerColor = colors.surface,
                contentColor = colors.textPrimary,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(data.visuals.message, fontSize = 13.sp)
            }
        }
    }

    if (state.tareasConConflicto.isNotEmpty()) {
        val cantidad = state.tareasConConflicto.size
        val nombres = state.tareasConConflicto
            .take(3)
            .joinToString(separator = "\n") { tarea -> "• ${tarea.titulo}" }
        val restantes = cantidad - 3
        val resumenTareas = buildString {
            append(nombres)
            if (restantes > 0) append("\n• Y $restantes más")
        }

        AppConfirmDialog(
            title = "Tareas fuera de la rutina",
            message = if (cantidad == 1) {
                "Este cambio deja 1 tarea fuera de los nuevos días u horarios."
            } else {
                "Este cambio deja $cantidad tareas fuera de los nuevos días u horarios."
            },
            support = "$resumenTareas\n\nQuedarán deshabilitadas y marcadas para revisión hasta que las edites manualmente.",
            dismissText = "Volver",
            confirmText = "Guardar igual",
            confirmColor = colors.warningText,
            onDismiss = viewModel::cancelarAdvertenciaHorario,
            onConfirm = viewModel::confirmarGuardadoConTareasDeshabilitadas
        )
    }
}

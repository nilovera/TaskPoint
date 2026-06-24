package com.example.apk_mock.ui.rutinas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue

@Composable
fun CrearRutinaScreen(
    viewModel: RutinasViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.consumeSuccess()
            onBack()
        }
    }

    RutinaFormScreen(
        title = "Nueva rutina",
        submitText = "Crear rutina",
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
        onNombreChange = viewModel::onNombreChange,
        onIconoChange = viewModel::onIconoChange,
        onDireccionChange = viewModel::onDireccionChange,
        onDiaToggle = viewModel::onDiaToggle,
        onHorarioInicioChange = viewModel::onHorarioInicioChange,
        onHorarioFinChange = viewModel::onHorarioFinChange,
        onDescripcionChange = viewModel::onDescripcionChange,
        onSubmit = viewModel::onCrearRutina,
        style = RutinaFormStyles.Create
    )
}

package com.example.taskpoint.ui.rutinas

import androidx.lifecycle.ViewModel
import com.example.taskpoint.domain.RutinaResult
import com.example.taskpoint.domain.model.DiaSemana
import com.example.taskpoint.domain.model.Rutina
import com.example.taskpoint.domain.model.RutinaIcono
import com.example.taskpoint.domain.useCase.CrearRutinaUseCase
import com.example.taskpoint.domain.useCase.GetRutinasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ── UiState lista de rutinas ──────────────────────────────────────────────────
data class RutinasListUiState(
    val rutinas: List<Rutina> = emptyList(),
    val filtrodia: DiaSemana? = null,        // null = "Todas"
    val snackbarMessage: String? = null
)

// ── UiState formulario crear ──────────────────────────────────────────────────
data class CrearRutinaUiState(
    val nombre: String = "",
    val iconoSeleccionado: RutinaIcono = RutinaIcono.TRABAJO,
    val direccion: String = "",
    val diasSeleccionados: Set<DiaSemana> = emptySet(),
    val horarioInicio: String = "",
    val horarioFin: String = "",
    val descripcion: String = "",
    // errores por campo
    val nombreError: String? = null,
    val direccionError: String? = null,
    val diasError: String? = null,
    val horarioInicioError: String? = null,
    val horarioFinError: String? = null,
    val descripcionError: String? = null,
    // resultado
    val isSuccess: Boolean = false
)

class RutinasViewModel(
    private val getRutinas: GetRutinasUseCase,
    private val crearRutina: CrearRutinaUseCase
) : ViewModel() {

    // ── Lista ─────────────────────────────────────────────────────────────────
    private val _listState = MutableStateFlow(RutinasListUiState())
    val listState: StateFlow<RutinasListUiState> = _listState.asStateFlow()

    // ── Formulario ────────────────────────────────────────────────────────────
    private val _formState = MutableStateFlow(CrearRutinaUiState())
    val formState: StateFlow<CrearRutinaUiState> = _formState.asStateFlow()

    fun refreshRutinas() {
        _listState.update { it.copy(rutinas = getRutinas()) }
    }

    fun onFiltroDia(dia: DiaSemana?) {
        _listState.update { it.copy(filtrodia = dia) }
    }

    fun rutinasFiltradas(): List<Rutina> {
        val filtro = _listState.value.filtrodia ?: return _listState.value.rutinas
        return _listState.value.rutinas.filter { it.diasSemana.contains(filtro) }
    }

    fun consumeSnackbar() = _listState.update { it.copy(snackbarMessage = null) }

    // ── Formulario handlers ───────────────────────────────────────────────────
    fun onNombreChange(v: String) = _formState.update { it.copy(nombre = v, nombreError = null) }
    fun onIconoChange(v: RutinaIcono) = _formState.update { it.copy(iconoSeleccionado = v) }
    fun onDireccionChange(v: String) = _formState.update { it.copy(direccion = v, direccionError = null) }
    fun onDiaToggle(dia: DiaSemana) {
        val dias = _formState.value.diasSeleccionados.toMutableSet()
        if (dias.contains(dia)) dias.remove(dia) else dias.add(dia)
        _formState.update { it.copy(diasSeleccionados = dias, diasError = null) }
    }
    fun onHorarioInicioChange(v: String) = _formState.update { it.copy(horarioInicio = v, horarioInicioError = null) }
    fun onHorarioFinChange(v: String) = _formState.update { it.copy(horarioFin = v, horarioFinError = null) }
    fun onDescripcionChange(v: String) = _formState.update { it.copy(descripcion = v, descripcionError = null) }

    fun onCrearRutina() {
        val s = _formState.value
        when (val r = crearRutina(
            s.nombre, s.iconoSeleccionado, s.direccion,
            s.diasSeleccionados.toList(), s.horarioInicio, s.horarioFin, s.descripcion
        )) {
            is RutinaResult.Success -> {
                _formState.update { CrearRutinaUiState(isSuccess = true) }
                refreshRutinas()
                _listState.update { it.copy(snackbarMessage = "Rutina creada correctamente.") }
            }
            is RutinaResult.Error -> applyFieldError(r.message)
        }
    }

    fun consumeSuccess() = _formState.update { it.copy(isSuccess = false) }

    private fun applyFieldError(msg: String) {
        _formState.update {
            it.copy(
                nombreError      = if (msg.contains("nombre", true)) msg else it.nombreError,
                direccionError   = if (msg.contains("direcci", true)) msg else it.direccionError,
                diasError        = if (msg.contains("día", true)) msg else it.diasError,
                horarioInicioError = if (msg.contains("inicio", true)) msg else it.horarioInicioError,
                horarioFinError  = if (msg.contains("fin", true)) msg else it.horarioFinError,
                descripcionError = if (msg.contains("descrip", true)) msg else it.descripcionError,
            )
        }
    }
}
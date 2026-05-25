package com.example.apk_mock.ui.rutinas

import androidx.lifecycle.ViewModel
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.repository.RutinaResult
import com.example.apk_mock.domain.useCase.CrearRutinaUseCase
import com.example.apk_mock.domain.useCase.EditarRutinaUseCase
import com.example.apk_mock.domain.useCase.EliminarRutinaUseCase
import com.example.apk_mock.domain.useCase.GetRutinaByIdUseCase
import com.example.apk_mock.domain.useCase.GetRutinasUseCase
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

data class EditarRutinaUiState(
    val rutinaId: String = "",
    val nombre: String = "",
    val iconoSeleccionado: RutinaIcono = RutinaIcono.TRABAJO,
    val direccion: String = "",
    val diasSeleccionados: Set<DiaSemana> = emptySet(),
    val horarioInicio: String = "",
    val horarioFin: String = "",
    val descripcion: String = "",
    val nombreError: String? = null,
    val direccionError: String? = null,
    val diasError: String? = null,
    val horarioInicioError: String? = null,
    val horarioFinError: String? = null,
    val descripcionError: String? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

data class DetalleRutinaUiState(
    val rutina: Rutina? = null,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val isDeleted: Boolean = false
)

class RutinasViewModel(
    private val getRutinas: GetRutinasUseCase,
    private val crearRutina: CrearRutinaUseCase,
    private val getRutinaById: GetRutinaByIdUseCase,
    private val editarRutina: EditarRutinaUseCase,
    private val eliminarRutina: EliminarRutinaUseCase
) : ViewModel() {

    // ── Lista ─────────────────────────────────────────────────────────────────
    private val _listState = MutableStateFlow(RutinasListUiState())
    val listState: StateFlow<RutinasListUiState> = _listState.asStateFlow()

    // ── Formulario ────────────────────────────────────────────────────────────
    private val _formState = MutableStateFlow(CrearRutinaUiState())
    val formState: StateFlow<CrearRutinaUiState> = _formState.asStateFlow()

    private val _editState = MutableStateFlow(EditarRutinaUiState())
    val editState: StateFlow<EditarRutinaUiState> = _editState.asStateFlow()

    private val _detalleState = MutableStateFlow(DetalleRutinaUiState())
    val detalleState: StateFlow<DetalleRutinaUiState> = _detalleState.asStateFlow()

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

    fun loadDetalleRutina(rutinaId: String) {
        val rutina = getRutinaById(rutinaId)
        _detalleState.update {
            it.copy(
                rutina = rutina,
                errorMessage = if (rutina == null) "La rutina no existe o no pertenece a tu cuenta." else null,
                isDeleted = false
            )
        }
    }

    fun onEliminarRutina(rutinaId: String) {
        when (val result = eliminarRutina(rutinaId)) {
            is RutinaResult.Success -> {
                refreshRutinas()
                _listState.update { it.copy(snackbarMessage = "Rutina eliminada correctamente.") }
                _detalleState.update { DetalleRutinaUiState(isDeleted = true) }
            }
            is RutinaResult.Error -> {
                _detalleState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun consumeDetalleDeletion() {
        _detalleState.update { it.copy(isDeleted = false) }
    }

    fun consumeDetalleError() {
        _detalleState.update { it.copy(errorMessage = null) }
    }

    fun consumeDetalleSnackbar() {
        _detalleState.update { it.copy(snackbarMessage = null) }
    }

    fun showDetalleSnackbar(message: String) {
        _detalleState.update { it.copy(snackbarMessage = message) }
    }

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

    fun loadEditarRutina(rutinaId: String) {
        val rutina = getRutinaById(rutinaId)
        if (rutina == null) {
            _editState.update {
                EditarRutinaUiState(
                    rutinaId = rutinaId,
                    errorMessage = "La rutina no existe o no pertenece a tu cuenta."
                )
            }
            return
        }

        _editState.update {
            EditarRutinaUiState(
                rutinaId = rutina.id,
                nombre = rutina.nombre,
                iconoSeleccionado = rutina.icono,
                direccion = rutina.direccion,
                diasSeleccionados = rutina.diasSemana.toSet(),
                horarioInicio = rutina.horarioInicio,
                horarioFin = rutina.horarioFin,
                descripcion = rutina.descripcion
            )
        }
    }

    fun onEditNombreChange(v: String) = _editState.update { it.copy(nombre = v, nombreError = null) }
    fun onEditIconoChange(v: RutinaIcono) = _editState.update { it.copy(iconoSeleccionado = v) }
    fun onEditDireccionChange(v: String) = _editState.update { it.copy(direccion = v, direccionError = null) }
    fun onEditDiaToggle(dia: DiaSemana) {
        val dias = _editState.value.diasSeleccionados.toMutableSet()
        if (dias.contains(dia)) dias.remove(dia) else dias.add(dia)
        _editState.update { it.copy(diasSeleccionados = dias, diasError = null) }
    }
    fun onEditHorarioInicioChange(v: String) = _editState.update { it.copy(horarioInicio = v, horarioInicioError = null) }
    fun onEditHorarioFinChange(v: String) = _editState.update { it.copy(horarioFin = v, horarioFinError = null) }
    fun onEditDescripcionChange(v: String) = _editState.update { it.copy(descripcion = v, descripcionError = null) }

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

    fun onGuardarCambiosRutina() {
        val s = _editState.value
        when (val result = editarRutina(
            s.rutinaId,
            s.nombre,
            s.iconoSeleccionado,
            s.direccion,
            s.diasSeleccionados.toList(),
            s.horarioInicio,
            s.horarioFin,
            s.descripcion
        )) {
            is RutinaResult.Success -> {
                refreshRutinas()
                _detalleState.update {
                    it.copy(rutina = result.rutina)
                }
                _editState.update { it.copy(isSuccess = true) }
            }
            is RutinaResult.Error -> applyEditFieldError(result.message)
        }
    }

    fun consumeSuccess() = _formState.update { it.copy(isSuccess = false) }

    fun consumeEditSuccess() = _editState.update { it.copy(isSuccess = false) }

    fun consumeEditError() = _editState.update { it.copy(errorMessage = null) }

    private fun applyFieldError(msg: String) {
        val isHorarioOrderError = msg.contains("posterior", true)
        _formState.update {
            it.copy(
                nombreError      = if (msg.contains("nombre", true)) msg else it.nombreError,
                direccionError   = if (msg.contains("direcci", true)) msg else it.direccionError,
                diasError        = if (msg.contains("día", true)) msg else it.diasError,
                horarioInicioError = if (!isHorarioOrderError && msg.contains("inicio", true)) msg else it.horarioInicioError,
                horarioFinError  = if (isHorarioOrderError || msg.contains("fin", true)) msg else it.horarioFinError,
                descripcionError = if (msg.contains("descrip", true)) msg else it.descripcionError,
            )
        }
    }

    private fun applyEditFieldError(msg: String) {
        val isHorarioOrderError = msg.contains("posterior", true)
        _editState.update {
            it.copy(
                nombreError = if (msg.contains("nombre", true)) msg else it.nombreError,
                direccionError = if (msg.contains("direcci", true)) msg else it.direccionError,
                diasError = if (msg.contains("día", true)) msg else it.diasError,
                horarioInicioError = if (!isHorarioOrderError && msg.contains("inicio", true)) msg else it.horarioInicioError,
                horarioFinError = if (isHorarioOrderError || msg.contains("fin", true)) msg else it.horarioFinError,
                descripcionError = if (msg.contains("descrip", true)) msg else it.descripcionError,
                errorMessage = if (
                    msg.contains("identificar", true) ||
                    msg.contains("no existe", true) ||
                    msg.contains("sesion", true)
                ) msg else it.errorMessage
            )
        }
    }
}

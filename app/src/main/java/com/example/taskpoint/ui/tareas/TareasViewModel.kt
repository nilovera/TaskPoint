package com.example.taskpoint.ui.tareas

import androidx.lifecycle.ViewModel
import com.example.taskpoint.domain.TareaResult
import com.example.taskpoint.domain.model.CategoriaTarea
import com.example.taskpoint.domain.model.DiaSemana
import com.example.taskpoint.domain.model.Rutina
import com.example.taskpoint.domain.model.Tarea
import com.example.taskpoint.domain.useCase.CrearTareaUseCase
import com.example.taskpoint.domain.useCase.GetTareasUseCase
import com.example.taskpoint.domain.useCase.GetRutinasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ── UiState lista ─────────────────────────────────────────────────────────────
data class TareasListUiState(
    val tareas: List<Tarea> = emptyList(),
    val filtroDia: DiaSemana? = null
)

// ── UiState formulario ────────────────────────────────────────────────────────
data class CrearTareaUiState(
    val titulo: String = "",
    val categoriaSeleccionada: CategoriaTarea? = null,
    val rutinaSeleccionadaId: String? = null,
    val rutinaSeleccionadaNombre: String? = null,
    val diaSeleccionado: DiaSemana? = null,
    val horario: String? = null,
    val notas: String = "",
    val rutinasDisponibles: List<Rutina> = emptyList(),
    // errores
    val tituloError: String? = null,
    val categoriaError: String? = null,
    val rutinaError: String? = null,
    val diaError: String? = null,
    val horarioError: String? = null,
    // resultado
    val isSuccess: Boolean = false
)

class TareasViewModel(
    private val getTareas: GetTareasUseCase,
    private val crearTarea: CrearTareaUseCase,
    private val getRutinas: GetRutinasUseCase
) : ViewModel() {

    private val _listState = MutableStateFlow(TareasListUiState())
    val listState: StateFlow<TareasListUiState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(CrearTareaUiState())
    val formState: StateFlow<CrearTareaUiState> = _formState.asStateFlow()

    fun refreshTareas() = _listState.update { it.copy(tareas = getTareas()) }

    fun onFiltroDia(dia: DiaSemana?) = _listState.update { it.copy(filtroDia = dia) }

    fun tareasFiltradas(): List<Tarea> {
        val filtro = _listState.value.filtroDia ?: return _listState.value.tareas
        return _listState.value.tareas.filter { it.dia == filtro }
    }

    fun loadFormData() {
        _formState.update { CrearTareaUiState(rutinasDisponibles = getRutinas()) }
    }

    // Handlers formulario
    fun onTituloChange(v: String) = _formState.update { it.copy(titulo = v, tituloError = null) }
    fun onCategoriaSelect(c: CategoriaTarea) = _formState.update { it.copy(categoriaSeleccionada = c, categoriaError = null) }
    fun onRutinaSelect(r: Rutina?) = _formState.update {
        it.copy(rutinaSeleccionadaId = r?.id, rutinaSeleccionadaNombre = r?.nombre, rutinaError = null)
    }
    fun onDiaSelect(d: DiaSemana?) = _formState.update { it.copy(diaSeleccionado = d, diaError = null) }
    fun onHorarioChange(v: String) = _formState.update { it.copy(horario = v, horarioError = null) }
    fun onNotasChange(v: String) = _formState.update { it.copy(notas = v) }

    fun onCrearTarea() {
        val s = _formState.value
        when (val r = crearTarea(
            s.titulo, s.categoriaSeleccionada,
            s.rutinaSeleccionadaId, s.rutinaSeleccionadaNombre,
            s.diaSeleccionado, s.horario, s.notas
        )) {
            is TareaResult.Success -> {
                refreshTareas()
                _formState.update { CrearTareaUiState(isSuccess = true) }
            }
            is TareaResult.Error -> applyFieldError(r.message)
        }
    }

    fun consumeSuccess() = _formState.update { it.copy(isSuccess = false) }

    private fun applyFieldError(msg: String) {
        _formState.update {
            it.copy(
                tituloError    = if (msg.contains("título", true)) msg else it.tituloError,
                categoriaError = if (msg.contains("categoría", true)) msg else it.categoriaError,
                rutinaError    = if (msg.contains("rutina", true)) msg else it.rutinaError,
                diaError       = if (msg.contains("día", true)) msg else it.diaError,
                horarioError   = if (msg.contains("horario", true)) msg else it.horarioError,
            )
        }
    }
}
package com.example.apk_mock.ui.tareas

import androidx.lifecycle.ViewModel
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.StoreOffer
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.repository.TareaResult
import com.example.apk_mock.domain.useCase.CrearTareaUseCase
import com.example.apk_mock.domain.useCase.GetCategoriasUseCase
import com.example.apk_mock.domain.useCase.GetOffersByCategoryUseCase
import com.example.apk_mock.domain.useCase.GetTareasUseCase
import com.example.apk_mock.domain.useCase.GetRutinasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ── UiState lista ─────────────────────────────────────────────────────────────
data class TareasListUiState(
    val tareas: List<Tarea> = emptyList(),
    val filtroDia: DiaSemana? = null,
    val rutinasDisponibles: Int = 0
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
    val categoriasDisponibles: List<CategoriaTarea> = emptyList(),
    val rutinasDisponibles: List<Rutina> = emptyList(),
    val diasDisponibles: List<DiaSemana> = emptyList(),
    val horariosDisponibles: List<String> = emptyList(),
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
    private val getRutinas: GetRutinasUseCase,
    private val getCategorias: GetCategoriasUseCase,
    private val getOffersByCategory: GetOffersByCategoryUseCase
) : ViewModel() {

    private val _listState = MutableStateFlow(TareasListUiState())
    val listState: StateFlow<TareasListUiState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(CrearTareaUiState())
    val formState: StateFlow<CrearTareaUiState> = _formState.asStateFlow()

    fun refreshTareas() = _listState.update {
        it.copy(
            tareas = getTareas(),
            rutinasDisponibles = getRutinas().size
        )
    }

    fun onFiltroDia(dia: DiaSemana?) = _listState.update { it.copy(filtroDia = dia) }

    fun tareasFiltradas(): List<Tarea> {
        val filtro = _listState.value.filtroDia ?: return _listState.value.tareas
        return _listState.value.tareas.filter { it.dia == filtro }
    }

    fun getTareaById(taskId: String): Tarea? {
        return _listState.value.tareas.find { it.id == taskId }
            ?: getTareas().find { it.id == taskId }
    }

    fun getRutinaForTarea(tarea: Tarea): Rutina? {
        val rutinaId = tarea.rutinaId ?: return null
        return getRutinas().find { it.id == rutinaId }
    }

    fun getOffersForTarea(tarea: Tarea): List<StoreOffer> {
        if (!tarea.categoria.activatesOffers) return emptyList()
        return getOffersByCategory(tarea.categoria.code)
    }

    fun loadFormData() {
        _formState.update {
            CrearTareaUiState(
                categoriasDisponibles = getCategorias(),
                rutinasDisponibles = getRutinas()
            )
        }
    }

    // Handlers formulario
    fun onTituloChange(v: String) = _formState.update { it.copy(titulo = v, tituloError = null) }
    fun onCategoriaSelect(c: CategoriaTarea) = _formState.update { it.copy(categoriaSeleccionada = c, categoriaError = null) }
    fun onRutinaSelect(r: Rutina?) = _formState.update {
        it.copy(
            rutinaSeleccionadaId = r?.id,
            rutinaSeleccionadaNombre = r?.nombre,
            diaSeleccionado = null,
            horario = null,
            diasDisponibles = r?.diasSemana.orEmpty(),
            horariosDisponibles = r?.horariosDisponibles().orEmpty(),
            rutinaError = null,
            diaError = null,
            horarioError = null
        )
    }
    fun onDiaSelect(d: DiaSemana?) = _formState.update { it.copy(diaSeleccionado = d, diaError = null) }
    fun onHorarioChange(v: String) = _formState.update { it.copy(horario = v, horarioError = null) }
    fun onNotasChange(v: String) = _formState.update { it.copy(notas = v) }

    fun onCrearTarea() {
        val s = _formState.value
        if (s.diaSeleccionado != null && s.diaSeleccionado !in s.diasDisponibles) {
            _formState.update { it.copy(diaError = "Seleccioná un día de la rutina asociada.") }
            return
        }
        if (!s.horario.isNullOrBlank() && s.horario !in s.horariosDisponibles) {
            _formState.update { it.copy(horarioError = "Seleccioná un horario de la rutina asociada.") }
            return
        }
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

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun Rutina.horariosDisponibles(): List<String> {
    return runCatching {
        val inicio = LocalTime.parse(horarioInicio, timeFormatter)
        val fin = LocalTime.parse(horarioFin, timeFormatter)
        if (fin.isBefore(inicio)) return emptyList()

        generateSequence(inicio) { current ->
            current.plusMinutes(30).takeIf { !it.isAfter(fin) }
        }.map { it.format(timeFormatter) }.toList()
    }.getOrDefault(emptyList())
}

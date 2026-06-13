package com.example.apk_mock.ui.tareas

import androidx.lifecycle.ViewModel
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.StoreOffer
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.repository.CategoriaRepository
import com.example.apk_mock.domain.repository.OfferRepository
import com.example.apk_mock.domain.repository.RutinaRepository
import com.example.apk_mock.domain.repository.TareaRepository
import com.example.apk_mock.domain.repository.TareaResult
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
    val photoPath: String? = null,
    val categoriasDisponibles: List<CategoriaTarea> = emptyList(),
    val rutinasDisponibles: List<Rutina> = emptyList(),
    val diasDisponibles: List<DiaSemana> = emptyList(),
    val horariosDisponibles: List<String> = emptyList(),
    val isFormLoaded: Boolean = false,
    val loadedTaskId: String? = null,
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
    private val tareaRepository: TareaRepository,
    private val rutinaRepository: RutinaRepository,
    private val categoriaRepository: CategoriaRepository,
    private val offerRepository: OfferRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(TareasListUiState())
    val listState: StateFlow<TareasListUiState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(CrearTareaUiState())
    val formState: StateFlow<CrearTareaUiState> = _formState.asStateFlow()

    fun refreshTareas() = _listState.update {
        it.copy(
            tareas = tareaRepository.getTareas(),
            rutinasDisponibles = rutinaRepository.getRutinas().size
        )
    }

    fun onFiltroDia(dia: DiaSemana?) = _listState.update { it.copy(filtroDia = dia) }

    fun tareasFiltradas(): List<Tarea> {
        val filtro = _listState.value.filtroDia ?: return _listState.value.tareas
        return _listState.value.tareas.filter { it.dia == filtro }
    }

    fun getTareaById(taskId: String): Tarea? {
        return _listState.value.tareas.find { it.id == taskId }
            ?: tareaRepository.getTareas().find { it.id == taskId }
    }

    fun getRutinaForTarea(tarea: Tarea): Rutina? {
        val rutinaId = tarea.rutinaId ?: return null
        return rutinaRepository.getRutinas().find { it.id == rutinaId }
    }

    fun getOffersForTarea(tarea: Tarea): List<StoreOffer> {
        if (!tarea.categoria.activatesOffers) return emptyList()
        return offerRepository.getOffersByCategory(tarea.categoria.code)
    }

    fun onEliminarTarea(taskId: String): Boolean {
        if (taskId.isBlank()) return false
        return when (tareaRepository.eliminarTarea(taskId)) {
            is TareaResult.Success -> {
                refreshTareas()
                true
            }
            is TareaResult.Error -> false
        }
    }

    fun resetCreateForm() {
        _formState.value = CrearTareaUiState()
    }

    fun resetEditForm() {
        _formState.value = CrearTareaUiState()
    }

    fun loadFormData() {
        val categorias = categoriaRepository.getCategorias()
        val rutinas = rutinaRepository.getRutinas()
        _formState.update {
            if (it.isFormLoaded && it.loadedTaskId == null) {
                it.copy(
                    categoriasDisponibles = categorias,
                    rutinasDisponibles = rutinas
                )
            } else {
                CrearTareaUiState(
                    categoriasDisponibles = categorias,
                    rutinasDisponibles = rutinas,
                    isFormLoaded = true
                )
            }
        }
    }

    fun loadEditFormData(taskId: String) {
        val categorias = categoriaRepository.getCategorias()
        val rutinas = rutinaRepository.getRutinas()
        val tarea = getTareaById(taskId) ?: return
        val rutina = rutinas.find { it.id == tarea.rutinaId }
        val categoria = categorias.find { it.code == tarea.categoria.code } ?: tarea.categoria

        _formState.update {
            if (it.isFormLoaded && it.loadedTaskId == taskId) {
                it.copy(
                    categoriasDisponibles = categorias,
                    rutinasDisponibles = rutinas
                )
            } else {
                CrearTareaUiState(
                    titulo = tarea.titulo,
                    categoriaSeleccionada = categoria,
                    rutinaSeleccionadaId = rutina?.id ?: tarea.rutinaId,
                    rutinaSeleccionadaNombre = rutina?.nombre ?: tarea.rutinaNombre,
                    diaSeleccionado = tarea.dia,
                    horario = tarea.horario,
                    notas = tarea.notas,
                    photoPath = tarea.photoPath,
                    categoriasDisponibles = categorias,
                    rutinasDisponibles = rutinas,
                    diasDisponibles = rutina?.diasSemana.orEmpty(),
                    horariosDisponibles = rutina?.horariosDisponibles().orEmpty(),
                    isFormLoaded = true,
                    loadedTaskId = taskId
                )
            }
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
    fun onPhotoCaptured(photoPath: String) = _formState.update { it.copy(photoPath = photoPath) }
    fun onPhotoRemoved() = _formState.update { it.copy(photoPath = null) }

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
        val categoria = validateTaskInput(s) ?: return
        when (val r = tareaRepository.crearTarea(
            s.titulo,
            categoria,
            s.rutinaSeleccionadaId,
            s.rutinaSeleccionadaNombre,
            s.diaSeleccionado,
            s.horario,
            s.notas,
            s.photoPath
        )) {
            is TareaResult.Success -> {
                refreshTareas()
                _formState.update { CrearTareaUiState(isSuccess = true) }
            }
            is TareaResult.Error -> applyFieldError(r.message)
        }
    }

    fun onEditarTarea(taskId: String) {
        val s = _formState.value
        if (taskId.isBlank()) {
            applyFieldError("No se encontro la tarea.")
            return
        }
        if (s.diaSeleccionado != null && s.diaSeleccionado !in s.diasDisponibles) {
            _formState.update { it.copy(diaError = "Seleccioná un día de la rutina asociada.") }
            return
        }
        if (!s.horario.isNullOrBlank() && s.horario !in s.horariosDisponibles) {
            _formState.update { it.copy(horarioError = "Seleccioná un horario de la rutina asociada.") }
            return
        }
        val categoria = validateTaskInput(s) ?: return
        when (val r = tareaRepository.editarTarea(
            taskId,
            s.titulo,
            categoria,
            s.rutinaSeleccionadaId,
            s.rutinaSeleccionadaNombre,
            s.diaSeleccionado,
            s.horario,
            s.notas,
            s.photoPath
        )) {
            is TareaResult.Success -> {
                refreshTareas()
                _formState.update { CrearTareaUiState(isSuccess = true) }
            }
            is TareaResult.Error -> applyFieldError(r.message)
        }
    }

    fun consumeSuccess() = _formState.update { it.copy(isSuccess = false) }

    private fun validateTaskInput(state: CrearTareaUiState): CategoriaTarea? {
        if (state.titulo.isBlank()) {
            applyFieldError("El título de la tarea es obligatorio.")
            return null
        }
        val categoria = state.categoriaSeleccionada
        if (categoria == null) {
            applyFieldError("Seleccioná una categoría.")
            return null
        }
        if (state.rutinaSeleccionadaId == null) {
            applyFieldError("Seleccioná una rutina asociada.")
            return null
        }
        if (state.diaSeleccionado == null) {
            applyFieldError("Seleccioná un día.")
            return null
        }
        if (state.horario.isNullOrBlank()) {
            applyFieldError("Seleccioná un horario.")
            return null
        }
        return categoria
    }

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

package com.example.apk_mock.ui.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TareasListUiState(
    val tareas: List<Tarea> = emptyList(),
    val filtroDia: DiaSemana? = null,
    val rutinasDisponibles: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

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
    val isLoading: Boolean = false,
    val loadedTaskId: String? = null,
    val tituloError: String? = null,
    val categoriaError: String? = null,
    val rutinaError: String? = null,
    val diaError: String? = null,
    val horarioError: String? = null,
    val isSuccess: Boolean = false
)

data class TaskDetailUiState(
    val tarea: Tarea? = null,
    val rutina: Rutina? = null,
    val offers: List<StoreOffer> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class TareasViewModel @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val rutinaRepository: RutinaRepository,
    private val categoriaRepository: CategoriaRepository,
    private val offerRepository: OfferRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(TareasListUiState())
    val listState: StateFlow<TareasListUiState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(CrearTareaUiState())
    val formState: StateFlow<CrearTareaUiState> = _formState.asStateFlow()

    private val _detailState = MutableStateFlow(TaskDetailUiState())
    val detailState: StateFlow<TaskDetailUiState> = _detailState.asStateFlow()

    private var listObservationJob: Job? = null

    init {
        refreshTareas()
    }

    fun refreshTareas() {
        listObservationJob?.cancel()
        listObservationJob = viewModelScope.launch {
            _listState.update { it.copy(isLoading = true, errorMessage = null) }
            combine(
                tareaRepository.observeTareas(),
                rutinaRepository.observeRutinas()
            ) { tareas, rutinas ->
                tareas to rutinas.size
            }.catch {
                _listState.update { state ->
                    state.copy(isLoading = false, errorMessage = "No se pudieron cargar las tareas.")
                }
            }.collect { (tareas, rutinasDisponibles) ->
                _listState.update {
                    it.copy(
                        tareas = tareas,
                        rutinasDisponibles = rutinasDisponibles,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun onFiltroDia(dia: DiaSemana?) = _listState.update { it.copy(filtroDia = dia) }

    fun tareasFiltradas(): List<Tarea> {
        val state = _listState.value
        return state.filtroDia?.let { filtro -> state.tareas.filter { it.dia == filtro } } ?: state.tareas
    }

    fun loadTaskDetail(taskId: String) {
        viewModelScope.launch {
            _detailState.value = TaskDetailUiState(isLoading = true)
            runCatching {
                val tarea = tareaRepository.getTareas().find { it.id == taskId }
                    ?: return@runCatching null
                val rutina = tarea.rutinaId?.let { rutinaRepository.getRutinaById(it) }
                val offers = if (tarea.categoria.activatesOffers) {
                    offerRepository.getOffersByCategory(tarea.categoria.code)
                } else {
                    emptyList()
                }
                Triple(tarea, rutina, offers)
            }.onSuccess { result ->
                if (result == null) {
                    _detailState.value = TaskDetailUiState(errorMessage = "No se encontro la tarea.")
                } else {
                    _detailState.value = TaskDetailUiState(
                        tarea = result.first,
                        rutina = result.second,
                        offers = result.third
                    )
                }
            }.onFailure {
                _detailState.value = TaskDetailUiState(errorMessage = "No se pudo cargar la tarea.")
            }
        }
    }

    fun onEliminarTarea(taskId: String) {
        if (taskId.isBlank()) return
        viewModelScope.launch {
            when (val result = tareaRepository.eliminarTarea(taskId)) {
                is TareaResult.Success -> {
                    refreshTareas()
                    _detailState.update { it.copy(isDeleted = true) }
                }
                is TareaResult.Error -> _detailState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun consumeTaskDeleted() = _detailState.update { it.copy(isDeleted = false) }

    fun resetCreateForm() {
        _formState.value = CrearTareaUiState()
    }

    fun resetEditForm() {
        _formState.value = CrearTareaUiState()
    }

    fun loadFormData() {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            runCatching {
                categoriaRepository.getCategorias() to rutinaRepository.getRutinas()
            }.onSuccess { (categorias, rutinas) ->
                _formState.update {
                    if (it.isFormLoaded && it.loadedTaskId == null) {
                        it.copy(
                            categoriasDisponibles = categorias,
                            rutinasDisponibles = rutinas,
                            isLoading = false
                        )
                    } else {
                        CrearTareaUiState(
                            categoriasDisponibles = categorias,
                            rutinasDisponibles = rutinas,
                            isFormLoaded = true
                        )
                    }
                }
            }.onFailure {
                _formState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadEditFormData(taskId: String) {
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            runCatching {
                val categorias = categoriaRepository.getCategorias()
                val rutinas = rutinaRepository.getRutinas()
                val tarea = tareaRepository.getTareas().find { it.id == taskId }
                Triple(categorias, rutinas, tarea)
            }.onSuccess { (categorias, rutinas, tarea) ->
                if (tarea == null) {
                    _formState.update { it.copy(isLoading = false) }
                    return@onSuccess
                }
                val rutina = rutinas.find { it.id == tarea.rutinaId }
                val categoria = categorias.find { it.code == tarea.categoria.code } ?: tarea.categoria
                _formState.value = CrearTareaUiState(
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
            }.onFailure {
                _formState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTituloChange(value: String) = _formState.update { it.copy(titulo = value, tituloError = null) }
    fun onCategoriaSelect(category: CategoriaTarea) = _formState.update { it.copy(categoriaSeleccionada = category, categoriaError = null) }
    fun onRutinaSelect(rutina: Rutina?) = _formState.update {
        it.copy(
            rutinaSeleccionadaId = rutina?.id,
            rutinaSeleccionadaNombre = rutina?.nombre,
            diaSeleccionado = null,
            horario = null,
            diasDisponibles = rutina?.diasSemana.orEmpty(),
            horariosDisponibles = rutina?.horariosDisponibles().orEmpty(),
            rutinaError = null,
            diaError = null,
            horarioError = null
        )
    }
    fun onDiaSelect(dia: DiaSemana?) = _formState.update { it.copy(diaSeleccionado = dia, diaError = null) }
    fun onHorarioChange(value: String) = _formState.update { it.copy(horario = value, horarioError = null) }
    fun onNotasChange(value: String) = _formState.update { it.copy(notas = value) }
    fun onPhotoCaptured(photoPath: String) = _formState.update { it.copy(photoPath = photoPath) }
    fun onPhotoRemoved() = _formState.update { it.copy(photoPath = null) }

    fun onCrearTarea() {
        val state = _formState.value
        if (!validateSchedule(state)) return
        val categoria = validateTaskInput(state) ?: return

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            when (val result = tareaRepository.crearTarea(
                state.titulo,
                categoria,
                state.rutinaSeleccionadaId,
                state.rutinaSeleccionadaNombre,
                state.diaSeleccionado,
                state.horario,
                state.notas,
                state.photoPath
            )) {
                is TareaResult.Success -> {
                    refreshTareas()
                    _formState.value = CrearTareaUiState(isSuccess = true)
                }
                is TareaResult.Error -> {
                    _formState.update { it.copy(isLoading = false) }
                    applyFieldError(result.message)
                }
            }
        }
    }

    fun onEditarTarea(taskId: String) {
        val state = _formState.value
        if (taskId.isBlank()) {
            applyFieldError("No se encontro la tarea.")
            return
        }
        if (!validateSchedule(state)) return
        val categoria = validateTaskInput(state) ?: return

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            when (val result = tareaRepository.editarTarea(
                taskId,
                state.titulo,
                categoria,
                state.rutinaSeleccionadaId,
                state.rutinaSeleccionadaNombre,
                state.diaSeleccionado,
                state.horario,
                state.notas,
                state.photoPath
            )) {
                is TareaResult.Success -> {
                    refreshTareas()
                    _formState.value = CrearTareaUiState(isSuccess = true)
                }
                is TareaResult.Error -> {
                    _formState.update { it.copy(isLoading = false) }
                    applyFieldError(result.message)
                }
            }
        }
    }

    fun consumeSuccess() = _formState.update { it.copy(isSuccess = false) }

    private fun validateSchedule(state: CrearTareaUiState): Boolean {
        if (state.diaSeleccionado != null && state.diaSeleccionado !in state.diasDisponibles) {
            _formState.update { it.copy(diaError = "Selecciona un dia de la rutina asociada.") }
            return false
        }
        if (!state.horario.isNullOrBlank() && state.horario !in state.horariosDisponibles) {
            _formState.update { it.copy(horarioError = "Selecciona un horario de la rutina asociada.") }
            return false
        }
        return true
    }

    private fun validateTaskInput(state: CrearTareaUiState): CategoriaTarea? {
        if (state.titulo.isBlank()) {
            applyFieldError("El titulo de la tarea es obligatorio.")
            return null
        }
        val categoria = state.categoriaSeleccionada
        if (categoria == null) {
            applyFieldError("Selecciona una categoria.")
            return null
        }
        if (state.rutinaSeleccionadaId == null) {
            applyFieldError("Selecciona una rutina asociada.")
            return null
        }
        if (state.diaSeleccionado == null) {
            applyFieldError("Selecciona un dia.")
            return null
        }
        if (state.horario.isNullOrBlank()) {
            applyFieldError("Selecciona un horario.")
            return null
        }
        return categoria
    }

    private fun applyFieldError(message: String) {
        _formState.update {
            it.copy(
                tituloError = if (message.contains("titulo", true)) message else it.tituloError,
                categoriaError = if (message.contains("categoria", true)) message else it.categoriaError,
                rutinaError = if (message.contains("rutina", true)) message else it.rutinaError,
                diaError = if (message.contains("dia", true)) message else it.diaError,
                horarioError = if (message.contains("horario", true)) message else it.horarioError
            )
        }
    }
}

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun Rutina.horariosDisponibles(): List<String> {
    return runCatching {
        val inicio = LocalTime.parse(horarioInicio, timeFormatter)
        val fin = LocalTime.parse(horarioFin, timeFormatter)
        if (fin.isBefore(inicio)) {
            emptyList()
        } else {
            generateSequence(inicio) { current -> current.plusMinutes(30).takeIf { !it.isAfter(fin) } }
                .map { it.format(timeFormatter) }
                .toList()
        }
    }.getOrDefault(emptyList())
}

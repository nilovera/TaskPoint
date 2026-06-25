package com.example.taskpoint

import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.model.StoreOffer
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.repository.CategoriaRepository
import com.example.apk_mock.domain.repository.OfferRepository
import com.example.apk_mock.domain.repository.RutinaRepository
import com.example.apk_mock.domain.repository.RutinaResult
import com.example.apk_mock.domain.repository.TareaRepository
import com.example.apk_mock.domain.repository.TareaResult
import com.example.apk_mock.ui.tareas.TareasViewModel
import com.example.apk_mock.ui.rutinas.RutinasViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TareasViewModelObservationTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun roomFlowUpdatesTaskListAfterInitialEmptyValue() = runTest {
        val tareas = MutableStateFlow<List<Tarea>>(emptyList())
        val rutinas = MutableStateFlow<List<Rutina>>(emptyList())
        val viewModel = TareasViewModel(
            tareaRepository = FlowTareaRepository(tareas),
            rutinaRepository = FlowRutinaRepository(rutinas),
            categoriaRepository = EmptyCategoriaRepository,
            offerRepository = EmptyOfferRepository
        )

        advanceUntilIdle()
        assertEquals(0, viewModel.listState.value.tareas.size)
        assertEquals(0, viewModel.listState.value.rutinasDisponibles)

        rutinas.value = listOf(rutina())
        tareas.value = listOf(tarea())
        advanceUntilIdle()

        assertEquals(1, viewModel.listState.value.tareas.size)
        assertEquals(1, viewModel.listState.value.rutinasDisponibles)
    }

    @Test
    fun editingRoutineWarnsBeforeDisablingIncompatibleTasks() = runTest {
        val tareas = MutableStateFlow(listOf(tarea()))
        val rutinas = MutableStateFlow(listOf(rutina()))
        val viewModel = RutinasViewModel(
            rutinaRepository = FlowRutinaRepository(rutinas),
            tareaRepository = FlowTareaRepository(tareas)
        )

        viewModel.loadEditarRutina("rutina-1")
        advanceUntilIdle()
        viewModel.onEditDiaToggle(DiaSemana.LUN)
        viewModel.onEditDiaToggle(DiaSemana.MAR)
        viewModel.onGuardarCambiosRutina()
        advanceUntilIdle()

        assertEquals(listOf("tarea-1"), viewModel.editState.value.tareasConConflicto.map { it.id })
    }

    @Test
    fun dayFilterIncludesTasksAssignedToThatDayAmongMultipleDays() = runTest {
        val tareas = MutableStateFlow(listOf(tarea(dias = listOf(DiaSemana.LUN, DiaSemana.MIE))))
        val rutinas = MutableStateFlow(listOf(rutina(diasSemana = listOf(DiaSemana.LUN, DiaSemana.MIE))))
        val viewModel = TareasViewModel(
            tareaRepository = FlowTareaRepository(tareas),
            rutinaRepository = FlowRutinaRepository(rutinas),
            categoriaRepository = EmptyCategoriaRepository,
            offerRepository = EmptyOfferRepository
        )

        advanceUntilIdle()
        viewModel.onFiltroDia(DiaSemana.MIE)

        assertEquals(listOf("tarea-1"), viewModel.tareasFiltradas().map { it.id })
    }

    @Test
    fun creatingTaskWithMultipleDaysSendsAllSelectedDays() = runTest {
        val categoria = CategoriaTarea(1, "Trabajo", "TRABAJO", "", false)
        val rutina = rutina(diasSemana = listOf(DiaSemana.LUN, DiaSemana.MIE))
        val tareaRepository = CapturingTareaRepository()
        val viewModel = TareasViewModel(
            tareaRepository = tareaRepository,
            rutinaRepository = FlowRutinaRepository(MutableStateFlow(listOf(rutina))),
            categoriaRepository = StaticCategoriaRepository(listOf(categoria)),
            offerRepository = EmptyOfferRepository
        )

        viewModel.loadFormData()
        advanceUntilIdle()

        viewModel.onTituloChange("Comprar comida")
        viewModel.onCategoriaSelect(categoria)
        viewModel.onRutinaSelect(rutina)
        viewModel.onDiaToggle(DiaSemana.LUN)
        viewModel.onDiaToggle(DiaSemana.MIE)
        viewModel.onHorarioChange("10:30")
        viewModel.onCrearTarea()
        advanceUntilIdle()

        assertEquals(listOf(DiaSemana.LUN, DiaSemana.MIE), tareaRepository.createdDias)
    }

    @Test
    fun selectingSingleDayRoutineKeepsOnlyAvailableDaySelected() = runTest {
        val rutina = rutina(diasSemana = listOf(DiaSemana.LUN))
        val viewModel = TareasViewModel(
            tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList())),
            rutinaRepository = FlowRutinaRepository(MutableStateFlow(listOf(rutina))),
            categoriaRepository = EmptyCategoriaRepository,
            offerRepository = EmptyOfferRepository
        )

        viewModel.loadFormData()
        advanceUntilIdle()

        viewModel.onRutinaSelect(rutina)
        viewModel.onDiaToggle(DiaSemana.MAR)

        assertEquals(setOf(DiaSemana.LUN), viewModel.formState.value.diasSeleccionados)
    }

    private fun rutina(diasSemana: List<DiaSemana> = listOf(DiaSemana.LUN)) = Rutina(
        id = "rutina-1",
        nombre = "Trabajo",
        icono = RutinaIcono.TRABAJO,
        direccion = "Oficina",
        diasSemana = diasSemana,
        horarioInicio = "09:00",
        horarioFin = "17:00",
        descripcion = "Horario laboral"
    )

    private fun tarea(dias: List<DiaSemana> = listOf(DiaSemana.LUN)) = Tarea(
        id = "tarea-1",
        titulo = "Cambio de cinta",
        categoria = CategoriaTarea(1, "Trabajo", "TRABAJO", "", false),
        rutinaId = "rutina-1",
        rutinaNombre = "Trabajo",
        dias = dias,
        horario = "10:30",
        notas = ""
    )
}

private class FlowTareaRepository(
    private val tareas: MutableStateFlow<List<Tarea>>
) : TareaRepository {
    override suspend fun getTareas(): List<Tarea> = tareas.value
    override suspend fun observeTareas(): Flow<List<Tarea>> = tareas
    override suspend fun eliminarTareasDeRutina(rutinaId: String) = 0
    override suspend fun crearTarea(
        titulo: String, categoria: CategoriaTarea, rutinaId: String?, rutinaNombre: String?,
        dias: List<DiaSemana>, horario: String?, notas: String, photoPath: String?
    ): TareaResult = TareaResult.Error("No usado en este test.")

    override suspend fun editarTarea(
        taskId: String, titulo: String, categoria: CategoriaTarea, rutinaId: String?, rutinaNombre: String?,
        dias: List<DiaSemana>, horario: String?, notas: String, photoPath: String?
    ): TareaResult = TareaResult.Error("No usado en este test.")

    override suspend fun eliminarTarea(taskId: String): TareaResult = TareaResult.Error("No usado en este test.")
}

private class FlowRutinaRepository(
    private val rutinas: MutableStateFlow<List<Rutina>>
) : RutinaRepository {
    override suspend fun getRutinas(): List<Rutina> = rutinas.value
    override suspend fun observeRutinas(): Flow<List<Rutina>> = rutinas
    override suspend fun getRutinaById(id: String): Rutina? = rutinas.value.firstOrNull { it.id == id }
    override suspend fun crearRutina(
        nombre: String, icono: RutinaIcono, direccion: String, dias: List<DiaSemana>,
        horarioInicio: String, horarioFin: String, descripcion: String
    ): RutinaResult = RutinaResult.Error("No usado en este test.")

    override suspend fun editarRutina(
        id: String, nombre: String, icono: RutinaIcono, direccion: String, dias: List<DiaSemana>,
        horarioInicio: String, horarioFin: String, descripcion: String
    ): RutinaResult = RutinaResult.Error("No usado en este test.")

    override suspend fun eliminarRutina(id: String): RutinaResult = RutinaResult.Error("No usado en este test.")
}

private object EmptyCategoriaRepository : CategoriaRepository {
    override suspend fun getCategorias(): List<CategoriaTarea> = emptyList()
}

private class StaticCategoriaRepository(
    private val categorias: List<CategoriaTarea>
) : CategoriaRepository {
    override suspend fun getCategorias(): List<CategoriaTarea> = categorias
}

private class CapturingTareaRepository : TareaRepository {
    private val tareas = MutableStateFlow<List<Tarea>>(emptyList())
    var createdDias: List<DiaSemana>? = null
        private set

    override suspend fun getTareas(): List<Tarea> = tareas.value
    override suspend fun observeTareas(): Flow<List<Tarea>> = tareas
    override suspend fun eliminarTareasDeRutina(rutinaId: String) = 0

    override suspend fun crearTarea(
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dias: List<DiaSemana>,
        horario: String?,
        notas: String,
        photoPath: String?
    ): TareaResult {
        createdDias = dias
        val tarea = Tarea(
            id = "created-task",
            titulo = titulo,
            categoria = categoria,
            rutinaId = rutinaId,
            rutinaNombre = rutinaNombre,
            dias = dias,
            horario = horario,
            notas = notas,
            photoPath = photoPath
        )
        tareas.value = listOf(tarea)
        return TareaResult.Success(tarea)
    }

    override suspend fun editarTarea(
        taskId: String,
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dias: List<DiaSemana>,
        horario: String?,
        notas: String,
        photoPath: String?
    ): TareaResult = TareaResult.Error("No usado en este test.")

    override suspend fun eliminarTarea(taskId: String): TareaResult = TareaResult.Error("No usado en este test.")
}

private object EmptyOfferRepository : OfferRepository {
    override suspend fun getOffersByCategory(
        categoryCode: String,
        originLatitude: Double,
        originLongitude: Double
    ): List<StoreOffer> = emptyList()
}

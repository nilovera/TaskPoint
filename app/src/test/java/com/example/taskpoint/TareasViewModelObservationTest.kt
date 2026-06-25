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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun selectingRoutineIncludesEndTimeInAvailableSlots() = runTest {
        val rutina = rutina(horarioInicio = "22:30", horarioFin = "23:30")
        val viewModel = TareasViewModel(
            tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList())),
            rutinaRepository = FlowRutinaRepository(MutableStateFlow(listOf(rutina))),
            categoriaRepository = EmptyCategoriaRepository,
            offerRepository = EmptyOfferRepository
        )

        viewModel.loadFormData()
        advanceUntilIdle()
        viewModel.onRutinaSelect(rutina)

        assertEquals(
            listOf("22:30", "23:00", "23:30"),
            viewModel.formState.value.horariosDisponibles
        )
    }

    @Test
    fun creatingTaskOutsideRoutineScheduleShowsHorarioError() = runTest {
        val rutina = rutina(horarioInicio = "22:30", horarioFin = "23:30")
        val categoria = categoria()
        val tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        val viewModel = TareasViewModel(
            tareaRepository = tareaRepository,
            rutinaRepository = FlowRutinaRepository(MutableStateFlow(listOf(rutina))),
            categoriaRepository = EmptyCategoriaRepository,
            offerRepository = EmptyOfferRepository
        )

        viewModel.loadFormData()
        advanceUntilIdle()
        viewModel.onTituloChange("Comprar algo")
        viewModel.onCategoriaSelect(categoria)
        viewModel.onRutinaSelect(rutina)
        viewModel.onDiaSelect(DiaSemana.LUN)
        viewModel.onHorarioChange("23:59")
        viewModel.onCrearTarea()

        assertEquals(
            "Selecciona un horario de la rutina asociada.",
            viewModel.formState.value.horarioError
        )
        assertFalse(tareaRepository.createWasCalled)
    }

    @Test
    fun creatingValidTaskCallsRepositoryAndMarksSuccess() = runTest {
        val rutina = rutina(horarioInicio = "09:00", horarioFin = "10:00")
        val categoria = categoria()
        val createdTask = tarea().copy(titulo = "Comprar algo")
        val tareaRepository = FlowTareaRepository(
            tareas = MutableStateFlow(emptyList()),
            createResult = TareaResult.Success(createdTask)
        )
        val viewModel = TareasViewModel(
            tareaRepository = tareaRepository,
            rutinaRepository = FlowRutinaRepository(MutableStateFlow(listOf(rutina))),
            categoriaRepository = FixedCategoriaRepository(listOf(categoria)),
            offerRepository = EmptyOfferRepository
        )

        viewModel.loadFormData()
        advanceUntilIdle()
        viewModel.onTituloChange("Comprar algo")
        viewModel.onCategoriaSelect(categoria)
        viewModel.onRutinaSelect(rutina)
        viewModel.onDiaSelect(DiaSemana.LUN)
        viewModel.onHorarioChange("09:30")
        viewModel.onCrearTarea()
        advanceUntilIdle()

        assertTrue(tareaRepository.createWasCalled)
        assertEquals("Comprar algo", tareaRepository.lastCreatedTitle)
        assertEquals("rutina-1", tareaRepository.lastCreatedRutinaId)
        assertTrue(viewModel.formState.value.isSuccess)
    }

    @Test
    fun creatingTaskWithoutTitleShowsTitleErrorAndDoesNotCallRepository() = runTest {
        val categoria = categoria()
        val rutina = rutina()
        val tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        val viewModel = taskViewModel(
            tareaRepository = tareaRepository,
            rutinas = listOf(rutina),
            categorias = listOf(categoria)
        )

        viewModel.loadFormData()
        advanceUntilIdle()
        viewModel.onCategoriaSelect(categoria)
        viewModel.onRutinaSelect(rutina)
        viewModel.onDiaSelect(DiaSemana.LUN)
        viewModel.onHorarioChange("10:30")
        viewModel.onCrearTarea()

        assertTrue(viewModel.formState.value.tituloError?.contains("titulo", ignoreCase = true) == true)
        assertFalse(tareaRepository.createWasCalled)
    }

    @Test
    fun creatingTaskWithoutCategoryShowsCategoryErrorAndDoesNotCallRepository() = runTest {
        val rutina = rutina()
        val tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        val viewModel = taskViewModel(
            tareaRepository = tareaRepository,
            rutinas = listOf(rutina)
        )

        viewModel.loadFormData()
        advanceUntilIdle()
        viewModel.onTituloChange("Comprar algo")
        viewModel.onRutinaSelect(rutina)
        viewModel.onDiaSelect(DiaSemana.LUN)
        viewModel.onHorarioChange("10:30")
        viewModel.onCrearTarea()

        assertTrue(viewModel.formState.value.categoriaError?.contains("categoria", ignoreCase = true) == true)
        assertFalse(tareaRepository.createWasCalled)
    }

    @Test
    fun creatingTaskWithoutRoutineShowsRoutineErrorAndDoesNotCallRepository() = runTest {
        val categoria = categoria()
        val tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        val viewModel = taskViewModel(
            tareaRepository = tareaRepository,
            categorias = listOf(categoria)
        )

        viewModel.loadFormData()
        advanceUntilIdle()
        viewModel.onTituloChange("Comprar algo")
        viewModel.onCategoriaSelect(categoria)
        viewModel.onCrearTarea()

        assertTrue(viewModel.formState.value.rutinaError?.contains("rutina", ignoreCase = true) == true)
        assertFalse(tareaRepository.createWasCalled)
    }

    @Test
    fun creatingTaskWithoutDayShowsDayErrorAndDoesNotCallRepository() = runTest {
        val categoria = categoria()
        val rutina = rutina()
        val tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        val viewModel = taskViewModel(
            tareaRepository = tareaRepository,
            rutinas = listOf(rutina),
            categorias = listOf(categoria)
        )

        viewModel.loadFormData()
        advanceUntilIdle()
        viewModel.onTituloChange("Comprar algo")
        viewModel.onCategoriaSelect(categoria)
        viewModel.onRutinaSelect(rutina)
        viewModel.onCrearTarea()

        assertTrue(viewModel.formState.value.diaError?.contains("dia", ignoreCase = true) == true)
        assertFalse(tareaRepository.createWasCalled)
    }

    @Test
    fun creatingTaskWithoutScheduleShowsScheduleErrorAndDoesNotCallRepository() = runTest {
        val categoria = categoria()
        val rutina = rutina()
        val tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        val viewModel = taskViewModel(
            tareaRepository = tareaRepository,
            rutinas = listOf(rutina),
            categorias = listOf(categoria)
        )

        viewModel.loadFormData()
        advanceUntilIdle()
        viewModel.onTituloChange("Comprar algo")
        viewModel.onCategoriaSelect(categoria)
        viewModel.onRutinaSelect(rutina)
        viewModel.onDiaSelect(DiaSemana.LUN)
        viewModel.onCrearTarea()

        assertTrue(viewModel.formState.value.horarioError?.contains("horario", ignoreCase = true) == true)
        assertFalse(tareaRepository.createWasCalled)
    }

    @Test
    fun taskFilterReturnsOnlySelectedDay() = runTest {
        val tareas = MutableStateFlow(
            listOf(
                tarea().copy(id = "lunes", dia = DiaSemana.LUN),
                tarea().copy(id = "martes", dia = DiaSemana.MAR)
            )
        )
        val viewModel = TareasViewModel(
            tareaRepository = FlowTareaRepository(tareas),
            rutinaRepository = FlowRutinaRepository(MutableStateFlow(listOf(rutina()))),
            categoriaRepository = EmptyCategoriaRepository,
            offerRepository = EmptyOfferRepository
        )

        advanceUntilIdle()
        viewModel.onFiltroDia(DiaSemana.MAR)

        assertEquals(listOf("martes"), viewModel.tareasFiltradas().map { it.id })
    }

    @Test
    fun loadingMissingTaskDetailShowsError() = runTest {
        val viewModel = taskViewModel(
            tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList())),
            rutinas = listOf(rutina())
        )

        viewModel.loadTaskDetail("tarea-inexistente")
        advanceUntilIdle()

        assertNull(viewModel.detailState.value.tarea)
        assertTrue(viewModel.detailState.value.errorMessage?.contains("tarea", ignoreCase = true) == true)
    }

    @Test
    fun offersCategoryWithoutRoutineCoordinatesMarksOffersPending() = runTest {
        val tarea = tarea().copy(categoria = categoria(code = "SUPERMERCADO", activatesOffers = true))
        val viewModel = taskViewModel(
            tareaRepository = FlowTareaRepository(MutableStateFlow(listOf(tarea))),
            rutinas = listOf(rutina().copy(latitude = null, longitude = null))
        )

        viewModel.loadTaskDetail("tarea-1")
        advanceUntilIdle()

        assertEquals("tarea-1", viewModel.detailState.value.tarea?.id)
        assertTrue(viewModel.detailState.value.offersLocationPending)
    }

    @Test
    fun creatingRoutineWithEmptyNameDoesNotCallRepository() = runTest {
        val rutinaRepository = FlowRutinaRepository(MutableStateFlow(emptyList()))
        val viewModel = RutinasViewModel(
            rutinaRepository = rutinaRepository,
            tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        )

        viewModel.onCrearRutina()
        advanceUntilIdle()

        assertFalse(rutinaRepository.createWasCalled)
        assertTrue(viewModel.formState.value.nombreError?.contains("nombre", ignoreCase = true) == true)
    }

    @Test
    fun creatingRoutineWithoutSelectedDaysShowsDaysError() = runTest {
        val rutinaRepository = FlowRutinaRepository(MutableStateFlow(emptyList()))
        val viewModel = RutinasViewModel(
            rutinaRepository = rutinaRepository,
            tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        )

        viewModel.onNombreChange("Trabajo")
        viewModel.onDireccionChange("Oficina")
        viewModel.onHorarioInicioChange("09:00")
        viewModel.onHorarioFinChange("17:00")
        viewModel.onDescripcionChange("Jornada laboral")
        viewModel.onCrearRutina()
        advanceUntilIdle()

        assertFalse(rutinaRepository.createWasCalled)
        assertTrue(viewModel.formState.value.diasError != null)
    }

    @Test
    fun creatingRoutineWithEndBeforeStartShowsEndTimeError() = runTest {
        val rutinaRepository = FlowRutinaRepository(MutableStateFlow(emptyList()))
        val viewModel = RutinasViewModel(
            rutinaRepository = rutinaRepository,
            tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        )

        viewModel.onNombreChange("Trabajo")
        viewModel.onDireccionChange("Oficina")
        viewModel.onDiaToggle(DiaSemana.LUN)
        viewModel.onHorarioInicioChange("17:00")
        viewModel.onHorarioFinChange("09:00")
        viewModel.onDescripcionChange("Jornada laboral")
        viewModel.onCrearRutina()
        advanceUntilIdle()

        assertFalse(rutinaRepository.createWasCalled)
        assertTrue(viewModel.formState.value.horarioFinError?.contains("posterior", ignoreCase = true) == true)
    }

    @Test
    fun creatingRoutineWithValidInputCallsRepository() = runTest {
        val rutinaRepository = FlowRutinaRepository(
            rutinas = MutableStateFlow(emptyList()),
            createResult = RutinaResult.Success(rutina())
        )
        val viewModel = RutinasViewModel(
            rutinaRepository = rutinaRepository,
            tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        )

        viewModel.onNombreChange(" Trabajo ")
        viewModel.onDireccionChange(" Oficina ")
        viewModel.onDiaToggle(DiaSemana.LUN)
        viewModel.onHorarioInicioChange("09:00")
        viewModel.onHorarioFinChange("17:00")
        viewModel.onDescripcionChange(" Jornada laboral ")
        viewModel.onCrearRutina()
        advanceUntilIdle()

        assertTrue(rutinaRepository.createWasCalled)
        assertEquals("Trabajo", rutinaRepository.lastCreatedName)
        assertEquals(listOf(DiaSemana.LUN), rutinaRepository.lastCreatedDias)
        assertTrue(viewModel.formState.value.isSuccess)
    }

    @Test
    fun routineFilterReturnsOnlySelectedDay() = runTest {
        val rutinas = MutableStateFlow(
            listOf(
                rutina().copy(id = "lunes", diasSemana = listOf(DiaSemana.LUN)),
                rutina().copy(id = "viernes", diasSemana = listOf(DiaSemana.VIE))
            )
        )
        val viewModel = RutinasViewModel(
            rutinaRepository = FlowRutinaRepository(rutinas),
            tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        )

        advanceUntilIdle()
        viewModel.onFiltroDia(DiaSemana.VIE)

        assertEquals(listOf("viernes"), viewModel.rutinasFiltradas().map { it.id })
    }

    @Test
    fun loadingMissingRoutineShowsDetailError() = runTest {
        val viewModel = RutinasViewModel(
            rutinaRepository = FlowRutinaRepository(MutableStateFlow(emptyList())),
            tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        )

        viewModel.loadDetalleRutina("rutina-inexistente")
        advanceUntilIdle()

        assertNull(viewModel.detalleState.value.rutina)
        assertTrue(viewModel.detalleState.value.errorMessage?.contains("no existe", ignoreCase = true) == true)
    }

    @Test
    fun loadingMissingRoutineForEditShowsError() = runTest {
        val viewModel = RutinasViewModel(
            rutinaRepository = FlowRutinaRepository(MutableStateFlow(emptyList())),
            tareaRepository = FlowTareaRepository(MutableStateFlow(emptyList()))
        )

        viewModel.loadEditarRutina("rutina-inexistente")
        advanceUntilIdle()

        assertTrue(viewModel.editState.value.errorMessage?.contains("no existe", ignoreCase = true) == true)
    }

    @Test
    fun confirmingRoutineUpdateWithConflictsCallsRepository() = runTest {
        val updatedRoutine = rutina().copy(diasSemana = listOf(DiaSemana.MAR))
        val rutinaRepository = FlowRutinaRepository(
            rutinas = MutableStateFlow(listOf(rutina())),
            editResult = RutinaResult.Success(updatedRoutine)
        )
        val viewModel = RutinasViewModel(
            rutinaRepository = rutinaRepository,
            tareaRepository = FlowTareaRepository(MutableStateFlow(listOf(tarea())))
        )

        viewModel.loadEditarRutina("rutina-1")
        advanceUntilIdle()
        viewModel.onEditDiaToggle(DiaSemana.LUN)
        viewModel.onEditDiaToggle(DiaSemana.MAR)
        viewModel.onGuardarCambiosRutina()
        advanceUntilIdle()
        viewModel.confirmarGuardadoConTareasDeshabilitadas()
        advanceUntilIdle()

        assertTrue(rutinaRepository.editWasCalled)
        assertTrue(viewModel.editState.value.isSuccess)
    }

    private fun rutina(
        horarioInicio: String = "09:00",
        horarioFin: String = "17:00"
    ) = Rutina(
        id = "rutina-1",
        nombre = "Trabajo",
        icono = RutinaIcono.TRABAJO,
        direccion = "Oficina",
        diasSemana = listOf(DiaSemana.LUN),
        horarioInicio = horarioInicio,
        horarioFin = horarioFin,
        descripcion = "Horario laboral"
    )

    private fun tarea() = Tarea(
        id = "tarea-1",
        titulo = "Cambio de cinta",
        categoria = categoria(),
        rutinaId = "rutina-1",
        rutinaNombre = "Trabajo",
        dia = DiaSemana.LUN,
        horario = "10:30",
        notas = ""
    )

    private fun categoria(
        code: String = "TRABAJO",
        activatesOffers: Boolean = false
    ) = CategoriaTarea(1, "Trabajo", code, "", activatesOffers)

    private fun taskViewModel(
        tareaRepository: FlowTareaRepository = FlowTareaRepository(MutableStateFlow(emptyList())),
        rutinas: List<Rutina> = emptyList(),
        categorias: List<CategoriaTarea> = emptyList(),
        offerRepository: OfferRepository = EmptyOfferRepository
    ) = TareasViewModel(
        tareaRepository = tareaRepository,
        rutinaRepository = FlowRutinaRepository(MutableStateFlow(rutinas)),
        categoriaRepository = FixedCategoriaRepository(categorias),
        offerRepository = offerRepository
    )
}

private class FlowTareaRepository(
    private val tareas: MutableStateFlow<List<Tarea>>,
    private val createResult: TareaResult = TareaResult.Error("No usado en este test.")
) : TareaRepository {
    var createWasCalled = false
        private set
    var lastCreatedTitle: String? = null
        private set
    var lastCreatedRutinaId: String? = null
        private set

    override suspend fun getTareas(): List<Tarea> = tareas.value
    override suspend fun observeTareas(): Flow<List<Tarea>> = tareas
    override suspend fun eliminarTareasDeRutina(rutinaId: String) = 0
    override suspend fun crearTarea(
        titulo: String, categoria: CategoriaTarea, rutinaId: String?, rutinaNombre: String?,
        dia: DiaSemana?, horario: String?, notas: String, photoPath: String?
    ): TareaResult {
        createWasCalled = true
        lastCreatedTitle = titulo
        lastCreatedRutinaId = rutinaId
        return createResult
    }

    override suspend fun editarTarea(
        taskId: String, titulo: String, categoria: CategoriaTarea, rutinaId: String?, rutinaNombre: String?,
        dia: DiaSemana?, horario: String?, notas: String, photoPath: String?
    ): TareaResult = TareaResult.Error("No usado en este test.")

    override suspend fun eliminarTarea(taskId: String): TareaResult = TareaResult.Error("No usado en este test.")
}

private class FlowRutinaRepository(
    private val rutinas: MutableStateFlow<List<Rutina>>,
    private val createResult: RutinaResult = RutinaResult.Error("No usado en este test."),
    private val editResult: RutinaResult = RutinaResult.Error("No usado en este test.")
) : RutinaRepository {
    var createWasCalled = false
        private set
    var editWasCalled = false
        private set
    var lastCreatedName: String? = null
        private set
    var lastCreatedDias: List<DiaSemana>? = null
        private set

    override suspend fun getRutinas(): List<Rutina> = rutinas.value
    override suspend fun observeRutinas(): Flow<List<Rutina>> = rutinas
    override suspend fun getRutinaById(id: String): Rutina? = rutinas.value.firstOrNull { it.id == id }
    override suspend fun crearRutina(
        nombre: String, icono: RutinaIcono, direccion: String, dias: List<DiaSemana>,
        horarioInicio: String, horarioFin: String, descripcion: String
    ): RutinaResult {
        createWasCalled = true
        lastCreatedName = nombre
        lastCreatedDias = dias
        return createResult
    }

    override suspend fun editarRutina(
        id: String, nombre: String, icono: RutinaIcono, direccion: String, dias: List<DiaSemana>,
        horarioInicio: String, horarioFin: String, descripcion: String
    ): RutinaResult {
        editWasCalled = true
        return editResult
    }

    override suspend fun eliminarRutina(id: String): RutinaResult = RutinaResult.Error("No usado en este test.")
}

private object EmptyCategoriaRepository : CategoriaRepository {
    override suspend fun getCategorias(): List<CategoriaTarea> = emptyList()
}

private class FixedCategoriaRepository(
    private val categorias: List<CategoriaTarea>
) : CategoriaRepository {
    override suspend fun getCategorias(): List<CategoriaTarea> = categorias
}

private object EmptyOfferRepository : OfferRepository {
    override suspend fun getOffersByCategory(
        categoryCode: String,
        originLatitude: Double,
        originLongitude: Double
    ): List<StoreOffer> = emptyList()
}

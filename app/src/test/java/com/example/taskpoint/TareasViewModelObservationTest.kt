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

    private fun rutina() = Rutina(
        id = "rutina-1",
        nombre = "Trabajo",
        icono = RutinaIcono.TRABAJO,
        direccion = "Oficina",
        diasSemana = listOf(DiaSemana.LUN),
        horarioInicio = "09:00",
        horarioFin = "17:00",
        descripcion = ""
    )

    private fun tarea() = Tarea(
        id = "tarea-1",
        titulo = "Cambio de cinta",
        categoria = CategoriaTarea(1, "Trabajo", "TRABAJO", "", false),
        rutinaId = "rutina-1",
        rutinaNombre = "Trabajo",
        dia = DiaSemana.LUN,
        horario = "10:30",
        notas = ""
    )
}

private class FlowTareaRepository(
    private val tareas: MutableStateFlow<List<Tarea>>
) : TareaRepository {
    override suspend fun getTareas(): List<Tarea> = tareas.value
    override suspend fun observeTareas(): Flow<List<Tarea>> = tareas
    override suspend fun actualizarNombreRutina(rutinaId: String, nuevoNombre: String) = 0
    override suspend fun eliminarTareasDeRutina(rutinaId: String) = 0
    override suspend fun crearTarea(
        titulo: String, categoria: CategoriaTarea, rutinaId: String?, rutinaNombre: String?,
        dia: DiaSemana?, horario: String?, notas: String, photoPath: String?
    ): TareaResult = TareaResult.Error("No usado en este test.")

    override suspend fun editarTarea(
        taskId: String, titulo: String, categoria: CategoriaTarea, rutinaId: String?, rutinaNombre: String?,
        dia: DiaSemana?, horario: String?, notas: String, photoPath: String?
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

private object EmptyOfferRepository : OfferRepository {
    override suspend fun getOffersByCategory(categoryCode: String): List<StoreOffer> = emptyList()
}

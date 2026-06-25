package com.example.taskpoint

import com.example.apk_mock.data.local.SyncStatus
import com.example.apk_mock.data.local.entity.RutinaEntity
import com.example.apk_mock.data.local.entity.TareaEntity
import com.example.apk_mock.data.mapper.toDomain
import com.example.apk_mock.data.mapper.toEntity
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.model.Tarea
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    @Test
    fun tareaEntityMapsToDomainWithCategoryAndDay() {
        val entity = TareaEntity(
            id = "tarea-1",
            userId = "user-1",
            titulo = "Comprar algo",
            categoriaId = 2,
            categoriaName = "Supermercado",
            categoriaCode = "SUPERMERCADO",
            categoriaDescription = "Compras",
            categoriaActivatesOffers = true,
            rutinaId = "rutina-1",
            rutinaNombre = "Trabajo",
            dia = "LUN",
            horario = "09:30",
            notas = "Leche",
            photoPath = "foto.jpg",
            completada = false,
            requiereRevisionHorario = true,
            syncStatus = SyncStatus.PENDING_UPDATE,
            updatedAt = 100L
        )

        val domain = entity.toDomain()

        assertEquals("tarea-1", domain.id)
        assertEquals("Comprar algo", domain.titulo)
        assertEquals(DiaSemana.LUN, domain.dia)
        assertEquals("SUPERMERCADO", domain.categoria.code)
        assertTrue(domain.categoria.activatesOffers)
        assertTrue(domain.requiereRevisionHorario)
    }

    @Test
    fun tareaDomainMapsToEntityPreservingSyncMetadata() {
        val tarea = Tarea(
            id = "tarea-1",
            titulo = "Estudiar",
            categoria = CategoriaTarea(3, "Facultad", "FACULTAD", "Estudio", false),
            rutinaId = "rutina-1",
            rutinaNombre = "Facultad",
            dia = DiaSemana.MIE,
            horario = "19:30",
            notas = "Apuntes",
            photoPath = null,
            completada = false,
            requiereRevisionHorario = false
        )

        val entity = tarea.toEntity(
            userId = "user-1",
            syncStatus = SyncStatus.PENDING_CREATE,
            updatedAt = 200L
        )

        assertEquals("user-1", entity.userId)
        assertEquals("MIE", entity.dia)
        assertEquals(SyncStatus.PENDING_CREATE, entity.syncStatus)
        assertEquals(200L, entity.updatedAt)
        assertFalse(entity.categoriaActivatesOffers)
    }

    @Test
    fun rutinaEntityMapsInvalidIconToOtherAndParsesDays() {
        val entity = RutinaEntity(
            id = "rutina-1",
            userId = "user-1",
            nombre = "Leer",
            icono = "NO_EXISTE",
            direccion = "Casa",
            latitude = -34.0,
            longitude = -58.0,
            diasSemana = "LUN, MIE, INVALIDO",
            horarioInicio = "22:30",
            horarioFin = "23:30",
            descripcion = "Lectura",
            syncStatus = SyncStatus.SYNCED,
            updatedAt = 300L
        )

        val domain = entity.toDomain(cantidadTareas = 2)

        assertEquals(RutinaIcono.OTRO, domain.icono)
        assertEquals(listOf(DiaSemana.LUN, DiaSemana.MIE), domain.diasSemana)
        assertEquals(2, domain.cantidadTareas)
        assertEquals(-34.0, domain.latitude)
    }

    @Test
    fun rutinaDomainMapsDaysToStorageString() {
        val rutina = Rutina(
            id = "rutina-1",
            nombre = "Trabajo",
            icono = RutinaIcono.TRABAJO,
            direccion = "Oficina",
            latitude = null,
            longitude = null,
            diasSemana = listOf(DiaSemana.LUN, DiaSemana.VIE),
            horarioInicio = "09:00",
            horarioFin = "17:00",
            descripcion = "Laboral"
        )

        val entity = rutina.toEntity(
            userId = "user-1",
            syncStatus = SyncStatus.PENDING_UPDATE,
            updatedAt = 400L
        )

        assertEquals("TRABAJO", entity.icono)
        assertEquals("LUN,VIE", entity.diasSemana)
        assertEquals(SyncStatus.PENDING_UPDATE, entity.syncStatus)
        assertEquals(400L, entity.updatedAt)
    }
}

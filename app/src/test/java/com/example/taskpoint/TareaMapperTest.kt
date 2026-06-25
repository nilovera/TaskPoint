package com.example.taskpoint

import com.example.apk_mock.data.local.SyncStatus
import com.example.apk_mock.data.local.entity.TareaEntity
import com.example.apk_mock.data.mapper.toDomain
import com.example.apk_mock.data.mapper.toEntity
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea
import org.junit.Assert.assertEquals
import org.junit.Test

class TareaMapperTest {

    @Test
    fun readsLegacySingleDayValue() {
        val tarea = entity(dia = "LUN").toDomain()

        assertEquals(listOf(DiaSemana.LUN), tarea.dias)
    }

    @Test
    fun readsMultiDayValue() {
        val tarea = entity(dia = "LUN,MIE").toDomain()

        assertEquals(listOf(DiaSemana.LUN, DiaSemana.MIE), tarea.dias)
    }

    @Test
    fun writesMultiDayValueOrderedForStorage() {
        val entity = Tarea(
            id = "tarea-1",
            titulo = "Tarea",
            categoria = CategoriaTarea(1, "Trabajo", "TRABAJO", "", false),
            rutinaId = "rutina-1",
            rutinaNombre = "Trabajo",
            dias = listOf(DiaSemana.MIE, DiaSemana.LUN),
            horario = "10:30",
            notas = ""
        ).toEntity(userId = "user-1")

        assertEquals("LUN,MIE", entity.dia)
    }

    private fun entity(dia: String?) = TareaEntity(
        id = "tarea-1",
        userId = "user-1",
        titulo = "Tarea",
        categoriaId = 1,
        categoriaName = "Trabajo",
        categoriaCode = "TRABAJO",
        categoriaDescription = "",
        categoriaActivatesOffers = false,
        rutinaId = "rutina-1",
        rutinaNombre = "Trabajo",
        dia = dia,
        horario = "10:30",
        notas = "",
        photoPath = null,
        completada = false,
        requiereRevisionHorario = false,
        syncStatus = SyncStatus.SYNCED,
        updatedAt = 1L
    )
}

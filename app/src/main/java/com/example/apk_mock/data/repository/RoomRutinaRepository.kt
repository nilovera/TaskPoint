package com.example.apk_mock.data.repository

import androidx.room.withTransaction
import com.example.apk_mock.data.local.SyncEntityType
import com.example.apk_mock.data.local.SyncOperationStatus
import com.example.apk_mock.data.local.SyncOperationType
import com.example.apk_mock.data.local.SyncStatus
import com.example.apk_mock.data.local.TaskPointDatabase
import com.example.apk_mock.data.local.entity.SyncOperationEntity
import com.example.apk_mock.data.mapper.toDomain
import com.example.apk_mock.data.mapper.toEntity
import com.example.apk_mock.data.sync.SyncScheduler
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.repository.RutinaRepository
import com.example.apk_mock.domain.repository.RutinaResult
import com.example.apk_mock.domain.repository.UserSessionProvider
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class RoomRutinaRepository(
    private val database: TaskPointDatabase,
    private val sessionProvider: UserSessionProvider,
    private val syncScheduler: SyncScheduler
) : RutinaRepository {

    private val rutinaDao = database.rutinaDao()
    private val tareaDao = database.tareaDao()
    private val syncOperationDao = database.syncOperationDao()

    override suspend fun getRutinas(): List<Rutina> {
        val userId = sessionProvider.currentUserId() ?: return emptyList()
        return withContext(Dispatchers.IO) {
            rutinaDao.getRutinas(userId).map { entity ->
                entity.toDomain(
                    cantidadTareas = tareaDao.countTareasByRutina(entity.id, userId)
                )
            }
        }
    }

    override suspend fun observeRutinas(): Flow<List<Rutina>> {
        val userId = sessionProvider.currentUserId() ?: return flowOf(emptyList())
        return combine(
            rutinaDao.observeRutinas(userId),
            tareaDao.observeTareas(userId)
        ) { rutinas, tareas ->
            rutinas.map { rutina ->
                rutina.toDomain(
                    cantidadTareas = tareas.count { tarea -> tarea.rutinaId == rutina.id }
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getRutinaById(id: String): Rutina? {
        val userId = sessionProvider.currentUserId() ?: return null
        return withContext(Dispatchers.IO) {
            rutinaDao.getRutinaById(id, userId)?.let { entity ->
                entity.toDomain(
                    cantidadTareas = tareaDao.countTareasByRutina(entity.id, userId)
                )
            }
        }
    }

    override suspend fun crearRutina(
        nombre: String,
        icono: RutinaIcono,
        direccion: String,
        dias: List<DiaSemana>,
        horarioInicio: String,
        horarioFin: String,
        descripcion: String
    ): RutinaResult {
        val userId = sessionProvider.currentUserId()
            ?: return RutinaResult.Error("Inicia sesion para crear rutinas.")

        val rutina = Rutina(
            id = UUID.randomUUID().toString(),
            nombre = nombre,
            icono = icono,
            direccion = direccion,
            diasSemana = dias,
            horarioInicio = horarioInicio,
            horarioFin = horarioFin,
            descripcion = descripcion,
            cantidadTareas = 0
        )

        withContext(Dispatchers.IO) {
            database.withTransaction {
                val entity = rutina.toEntity(userId, SyncStatus.PENDING_CREATE)
                rutinaDao.upsertRutina(entity)
                syncOperationDao.upsertOperation(
                    syncOperation(
                        userId = userId,
                        entityId = rutina.id,
                        operationType = SyncOperationType.CREATE,
                        payloadJson = entity.toPayloadJson()
                    )
                )
            }
            syncScheduler.schedulePendingSync()
        }

        return RutinaResult.Success(rutina)
    }

    override suspend fun editarRutina(
        id: String,
        nombre: String,
        icono: RutinaIcono,
        direccion: String,
        dias: List<DiaSemana>,
        horarioInicio: String,
        horarioFin: String,
        descripcion: String
    ): RutinaResult {
        val userId = sessionProvider.currentUserId()
            ?: return RutinaResult.Error("Inicia sesion para editar rutinas.")

        return withContext(Dispatchers.IO) {
            val current = rutinaDao.getRutinaById(id, userId)
                ?: return@withContext RutinaResult.Error("La rutina no existe o no pertenece a tu cuenta.")

            val updated = current.toDomain(
                cantidadTareas = tareaDao.countTareasByRutina(current.id, userId)
            ).copy(
                nombre = nombre,
                icono = icono,
                direccion = direccion,
                diasSemana = dias,
                horarioInicio = horarioInicio,
                horarioFin = horarioFin,
                descripcion = descripcion
            )

            database.withTransaction {
                val entity = updated.toEntity(userId, SyncStatus.PENDING_UPDATE)
                rutinaDao.upsertRutina(entity)
                syncOperationDao.upsertOperation(
                    syncOperation(
                        userId = userId,
                        entityId = updated.id,
                        operationType = SyncOperationType.UPDATE,
                        payloadJson = entity.toPayloadJson()
                    )
                )
            }
            syncScheduler.schedulePendingSync()

            RutinaResult.Success(updated)
        }
    }

    override suspend fun eliminarRutina(id: String): RutinaResult {
        val userId = sessionProvider.currentUserId()
            ?: return RutinaResult.Error("Inicia sesion para eliminar rutinas.")

        return withContext(Dispatchers.IO) {
            val current = rutinaDao.getRutinaById(id, userId)
                ?: return@withContext RutinaResult.Error("La rutina no existe o no pertenece a tu cuenta.")
            val removed = current.toDomain(
                cantidadTareas = tareaDao.countTareasByRutina(current.id, userId)
            )
            val deletedAt = System.currentTimeMillis()

            database.withTransaction {
                rutinaDao.deleteRutina(id, userId)
                syncOperationDao.upsertOperation(
                    syncOperation(
                        userId = userId,
                        entityId = id,
                        operationType = SyncOperationType.DELETE,
                        payloadJson = deletePayloadJson(deletedAt)
                    )
                )
            }

            syncScheduler.schedulePendingSync()
            RutinaResult.Success(removed)
        }
    }

    private fun syncOperation(
        userId: String,
        entityId: String,
        operationType: SyncOperationType,
        payloadJson: String?
    ): SyncOperationEntity {
        return SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            entityType = SyncEntityType.RUTINA,
            entityId = entityId,
            operationType = operationType,
            payloadJson = payloadJson,
            status = SyncOperationStatus.PENDING
        )
    }

    private fun com.example.apk_mock.data.local.entity.RutinaEntity.toPayloadJson(): String {
        return JSONObject()
            .put("id", id)
            .put("nombre", nombre)
            .put("icono", icono)
            .put("direccion", direccion)
            .put("diasSemana", JSONArray(diasSemana.split(",").filter { it.isNotBlank() }))
            .put("horarioInicio", horarioInicio)
            .put("horarioFin", horarioFin)
            .put("descripcion", descripcion)
            .put("updatedAt", updatedAt)
            .toString()
    }

    private fun deletePayloadJson(updatedAt: Long): String {
        return JSONObject()
            .put("updatedAt", updatedAt)
            .toString()
    }
}

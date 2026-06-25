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
import com.example.apk_mock.data.geocoding.RoutineGeocodingScheduler
import com.example.apk_mock.data.source.TaskPhotoStorage
import com.example.apk_mock.data.sync.SyncScheduler
import com.example.apk_mock.data.sync.deleteSyncPayloadJson
import com.example.apk_mock.data.sync.toSyncPayloadJson
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Rutina
import com.example.apk_mock.domain.model.RutinaIcono
import com.example.apk_mock.domain.model.coincideConHorario
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

class RoomRutinaRepository(
    private val database: TaskPointDatabase,
    private val sessionProvider: UserSessionProvider,
    private val syncScheduler: SyncScheduler,
    private val geocodingScheduler: RoutineGeocodingScheduler,
    private val photoStorage: TaskPhotoStorage
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
                        payloadJson = entity.toSyncPayloadJson()
                    )
                )
            }
            syncScheduler.schedulePendingSync()
            geocodingScheduler.scheduleRoutine(userId, rutina.id, rutina.direccion)
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
            val result = database.withTransaction {
                val current = rutinaDao.getRutinaById(id, userId)
                    ?: return@withTransaction RutinaResult.Error(
                        "La rutina no existe o no pertenece a tu cuenta."
                    )
                val tareasAsociadas = tareaDao.getTareasByRutina(id, userId)
                val updatedAt = System.currentTimeMillis()
                val updated = current.toDomain(
                    cantidadTareas = tareasAsociadas.size
                ).copy(
                    nombre = nombre,
                    icono = icono,
                    direccion = direccion,
                    latitude = if (current.direccion == direccion) current.latitude else null,
                    longitude = if (current.direccion == direccion) current.longitude else null,
                    diasSemana = dias,
                    horarioInicio = horarioInicio,
                    horarioFin = horarioFin,
                    descripcion = descripcion
                )

                val entity = updated.toEntity(
                    userId = userId,
                    syncStatus = SyncStatus.PENDING_UPDATE,
                    updatedAt = updatedAt
                )
                rutinaDao.upsertRutina(entity)
                syncOperationDao.upsertOperation(
                    syncOperation(
                        userId = userId,
                        entityId = updated.id,
                        operationType = SyncOperationType.UPDATE,
                        payloadJson = entity.toSyncPayloadJson()
                    )
                )

                val tareasActualizadas = tareasAsociadas.mapNotNull { tarea ->
                    val requiereRevision = tarea.requiereRevisionHorario ||
                        !tarea.toDomain().coincideConHorario(dias, horarioInicio, horarioFin)
                    val cambioNombre = tarea.rutinaNombre != nombre
                    val cambioRevision = requiereRevision != tarea.requiereRevisionHorario

                    if (!cambioNombre && !cambioRevision) {
                        null
                    } else {
                        tarea.copy(
                            rutinaNombre = nombre,
                            requiereRevisionHorario = requiereRevision,
                            syncStatus = SyncStatus.PENDING_UPDATE,
                            updatedAt = updatedAt
                        )
                    }
                }

                if (tareasActualizadas.isNotEmpty()) {
                    tareaDao.upsertTareas(tareasActualizadas)
                    syncOperationDao.upsertOperations(
                        tareasActualizadas.map { tarea ->
                            syncOperation(
                                userId = userId,
                                entityType = SyncEntityType.TAREA,
                                entityId = tarea.id,
                                operationType = SyncOperationType.UPDATE,
                                payloadJson = tarea.toSyncPayloadJson()
                            )
                        }
                    )
                }

                RutinaResult.Success(updated)
            }

            if (result is RutinaResult.Success) {
                syncScheduler.schedulePendingSync()
                if (result.rutina.latitude == null || result.rutina.longitude == null) {
                    geocodingScheduler.scheduleRoutine(userId, result.rutina.id, result.rutina.direccion)
                }
            }

            result
        }
    }

    override suspend fun eliminarRutina(id: String): RutinaResult {
        val userId = sessionProvider.currentUserId()
            ?: return RutinaResult.Error("Inicia sesion para eliminar rutinas.")

        return withContext(Dispatchers.IO) {
            val deletion = database.withTransaction {
                val current = rutinaDao.getRutinaById(id, userId)
                    ?: return@withTransaction RutinaDeletion(
                        result = RutinaResult.Error("La rutina no existe o no pertenece a tu cuenta.")
                    )
                val tareasAsociadas = tareaDao.getTareasByRutina(id, userId)
                val removed = current.toDomain(
                    cantidadTareas = tareasAsociadas.size
                )
                val deletedAt = System.currentTimeMillis()

                tareasAsociadas.forEach { tarea ->
                    tareaDao.deleteTarea(tarea.id, userId)
                }
                rutinaDao.deleteRutina(id, userId)

                if (tareasAsociadas.isNotEmpty()) {
                    syncOperationDao.upsertOperations(
                        tareasAsociadas.map { tarea ->
                            syncOperation(
                                userId = userId,
                                entityType = SyncEntityType.TAREA,
                                entityId = tarea.id,
                                operationType = SyncOperationType.DELETE,
                                payloadJson = deleteSyncPayloadJson(deletedAt)
                            )
                        }
                    )
                }
                syncOperationDao.upsertOperation(
                    syncOperation(
                        userId = userId,
                        entityId = id,
                        operationType = SyncOperationType.DELETE,
                        payloadJson = deleteSyncPayloadJson(deletedAt)
                    )
                )

                RutinaDeletion(
                    result = RutinaResult.Success(removed),
                    photoPaths = tareasAsociadas.map { it.photoPath }
                )
            }

            if (deletion.result is RutinaResult.Success) {
                deletion.photoPaths.forEach { photoPath ->
                    photoStorage.deletePhoto(photoPath)
                }
                syncScheduler.schedulePendingSync()
            }

            deletion.result
        }
    }

    private fun syncOperation(
        userId: String,
        entityType: SyncEntityType = SyncEntityType.RUTINA,
        entityId: String,
        operationType: SyncOperationType,
        payloadJson: String?
    ): SyncOperationEntity {
        return SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            entityType = entityType,
            entityId = entityId,
            operationType = operationType,
            payloadJson = payloadJson,
            status = SyncOperationStatus.PENDING
        )
    }
}

private data class RutinaDeletion(
    val result: RutinaResult,
    val photoPaths: List<String?> = emptyList()
)

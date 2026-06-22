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
import com.example.apk_mock.data.mapper.toTareaDomainList
import com.example.apk_mock.data.source.TaskPhotoStorage
import com.example.apk_mock.data.sync.SyncScheduler
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.repository.TareaRepository
import com.example.apk_mock.domain.repository.TareaResult
import com.example.apk_mock.domain.repository.UserSessionProvider
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RoomTareaRepository(
    private val database: TaskPointDatabase,
    private val sessionProvider: UserSessionProvider,
    private val photoStorage: TaskPhotoStorage,
    private val syncScheduler: SyncScheduler
) : TareaRepository {

    private val tareaDao = database.tareaDao()
    private val syncOperationDao = database.syncOperationDao()

    override suspend fun getTareas(): List<Tarea> {
        val userId = sessionProvider.currentUserId() ?: return emptyList()
        return withContext(Dispatchers.IO) {
            tareaDao.getTareas(userId).toTareaDomainList()
        }
    }

    override suspend fun observeTareas(): Flow<List<Tarea>> {
        val userId = sessionProvider.currentUserId() ?: return flowOf(emptyList())
        return tareaDao.observeTareas(userId)
            .map { entities -> entities.toTareaDomainList() }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun actualizarNombreRutina(rutinaId: String, nuevoNombre: String): Int {
        val userId = sessionProvider.currentUserId() ?: return 0
        return withContext(Dispatchers.IO) {
            val updatedCount = database.withTransaction {
                val tareas = tareaDao.getTareasByRutina(rutinaId, userId)
                val updated = tareas.map { entity ->
                    entity.copy(
                        rutinaNombre = nuevoNombre,
                        syncStatus = SyncStatus.PENDING_UPDATE,
                        updatedAt = System.currentTimeMillis()
                    )
                }

                tareaDao.upsertTareas(updated)
                syncOperationDao.upsertOperations(
                    updated.map { entity ->
                        syncOperation(
                            userId = userId,
                            entityId = entity.id,
                            operationType = SyncOperationType.UPDATE,
                            payloadJson = entity.toPayloadJson()
                        )
                    }
                )
                updated.size
            }
            if (updatedCount > 0) syncScheduler.schedulePendingSync()
            updatedCount
        }
    }

    override suspend fun eliminarTareasDeRutina(rutinaId: String): Int {
        val userId = sessionProvider.currentUserId() ?: return 0
        return withContext(Dispatchers.IO) {
            val photoPaths = mutableListOf<String?>()
            val deletedCount = database.withTransaction {
                val tareas = tareaDao.getTareasByRutina(rutinaId, userId)
                photoPaths.addAll(tareas.map { it.photoPath })
                tareas.forEach { entity ->
                    val deletedAt = System.currentTimeMillis()
                    tareaDao.deleteTarea(entity.id, userId)
                    syncOperationDao.upsertOperation(
                        syncOperation(
                            userId = userId,
                            entityId = entity.id,
                            operationType = SyncOperationType.DELETE,
                            payloadJson = deletePayloadJson(deletedAt)
                        )
                    )
                }
                tareas.size
            }
            if (deletedCount > 0) {
                photoPaths.forEach { photoPath ->
                    photoStorage.deletePhoto(photoPath)
                }
                syncScheduler.schedulePendingSync()
            }
            deletedCount
        }
    }

    override suspend fun crearTarea(
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String,
        photoPath: String?
    ): TareaResult {
        val userId = sessionProvider.currentUserId()
            ?: return TareaResult.Error("Inicia sesion para crear tareas.")

        val tarea = Tarea(
            id = UUID.randomUUID().toString(),
            titulo = titulo,
            categoria = categoria,
            rutinaId = rutinaId,
            rutinaNombre = rutinaNombre,
            dia = dia,
            horario = horario,
            notas = notas,
            photoPath = photoPath
        )

        withContext(Dispatchers.IO) {
            database.withTransaction {
                val entity = tarea.toEntity(userId, SyncStatus.PENDING_CREATE)
                tareaDao.upsertTarea(entity)
                syncOperationDao.upsertOperation(
                    syncOperation(
                        userId = userId,
                        entityId = tarea.id,
                        operationType = SyncOperationType.CREATE,
                        payloadJson = entity.toPayloadJson()
                    )
                )
            }
            syncScheduler.schedulePendingSync()
        }

        return TareaResult.Success(tarea)
    }

    override suspend fun editarTarea(
        taskId: String,
        titulo: String,
        categoria: CategoriaTarea,
        rutinaId: String?,
        rutinaNombre: String?,
        dia: DiaSemana?,
        horario: String?,
        notas: String,
        photoPath: String?
    ): TareaResult {
        val userId = sessionProvider.currentUserId()
            ?: return TareaResult.Error("Inicia sesion para editar tareas.")

        return withContext(Dispatchers.IO) {
            val current = tareaDao.getTareaById(taskId, userId)
                ?: return@withContext TareaResult.Error("No se encontro la tarea.")

            val updated = current.toDomain().copy(
                titulo = titulo,
                categoria = categoria,
                rutinaId = rutinaId,
                rutinaNombre = rutinaNombre,
                dia = dia,
                horario = horario,
                notas = notas,
                photoPath = photoPath
            )

            database.withTransaction {
                val entity = updated.toEntity(userId, SyncStatus.PENDING_UPDATE)
                tareaDao.upsertTarea(entity)
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

            if (current.photoPath != photoPath) {
                photoStorage.deletePhoto(current.photoPath)
            }

            TareaResult.Success(updated)
        }
    }

    override suspend fun eliminarTarea(taskId: String): TareaResult {
        val userId = sessionProvider.currentUserId()
            ?: return TareaResult.Error("Inicia sesion para eliminar tareas.")

        return withContext(Dispatchers.IO) {
            val current = tareaDao.getTareaById(taskId, userId)
                ?: return@withContext TareaResult.Error("No se encontro la tarea.")
            val removed = current.toDomain()
            val deletedAt = System.currentTimeMillis()

            database.withTransaction {
                tareaDao.deleteTarea(taskId, userId)
                syncOperationDao.upsertOperation(
                    syncOperation(
                        userId = userId,
                        entityId = taskId,
                        operationType = SyncOperationType.DELETE,
                        payloadJson = deletePayloadJson(deletedAt)
                    )
                )
            }

            syncScheduler.schedulePendingSync()
            photoStorage.deletePhoto(removed.photoPath)
            TareaResult.Success(removed)
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
            entityType = SyncEntityType.TAREA,
            entityId = entityId,
            operationType = operationType,
            payloadJson = payloadJson,
            status = SyncOperationStatus.PENDING
        )
    }

    private fun com.example.apk_mock.data.local.entity.TareaEntity.toPayloadJson(): String {
        return JSONObject()
            .put("id", id)
            .put("titulo", titulo)
            .put("categoriaCode", categoriaCode)
            .put("rutinaId", rutinaId)
            .put("rutinaNombre", rutinaNombre)
            .put("dia", dia)
            .put("horario", horario)
            .put("notas", notas)
            .put("photoPath", photoPath)
            .put("completada", completada)
            .put("updatedAt", updatedAt)
            .toString()
    }

    private fun deletePayloadJson(updatedAt: Long): String {
        return JSONObject()
            .put("updatedAt", updatedAt)
            .toString()
    }
}

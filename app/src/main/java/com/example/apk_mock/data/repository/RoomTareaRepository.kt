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
import com.example.apk_mock.domain.model.CategoriaTarea
import com.example.apk_mock.domain.model.DiaSemana
import com.example.apk_mock.domain.model.Tarea
import com.example.apk_mock.domain.repository.TareaRepository
import com.example.apk_mock.domain.repository.TareaResult
import com.example.apk_mock.domain.repository.UserSessionProvider
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class RoomTareaRepository(
    private val database: TaskPointDatabase,
    private val sessionProvider: UserSessionProvider,
    private val photoStorage: TaskPhotoStorage
) : TareaRepository {

    private val tareaDao = database.tareaDao()
    private val syncOperationDao = database.syncOperationDao()

    override fun getTareas(): List<Tarea> {
        val userId = sessionProvider.currentUserId() ?: return emptyList()
        return runBlocking(Dispatchers.IO) {
            tareaDao.getTareas(userId).toTareaDomainList()
        }
    }

    override fun actualizarNombreRutina(rutinaId: String, nuevoNombre: String): Int {
        val userId = sessionProvider.currentUserId() ?: return 0
        return runBlocking(Dispatchers.IO) {
            database.withTransaction {
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
                            payloadJson = entity.toDomain().toPayloadJson()
                        )
                    }
                )
                updated.size
            }
        }
    }

    override fun eliminarTareasDeRutina(rutinaId: String): Int {
        val userId = sessionProvider.currentUserId() ?: return 0
        return runBlocking(Dispatchers.IO) {
            val photoPaths = mutableListOf<String?>()
            database.withTransaction {
                val tareas = tareaDao.getTareasByRutina(rutinaId, userId)
                photoPaths.addAll(tareas.map { it.photoPath })
                tareas.forEach { entity ->
                    tareaDao.deleteTarea(entity.id, userId)
                    syncOperationDao.upsertOperation(
                        syncOperation(
                            userId = userId,
                            entityId = entity.id,
                            operationType = SyncOperationType.DELETE,
                            payloadJson = null
                        )
                    )
                }
                tareas.size
            }.also {
                if (it > 0) {
                    photoPaths.forEach { photoPath ->
                        photoStorage.deletePhoto(photoPath)
                    }
                }
            }
        }
    }

    override fun crearTarea(
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

        runBlocking(Dispatchers.IO) {
            database.withTransaction {
                tareaDao.upsertTarea(tarea.toEntity(userId, SyncStatus.PENDING_CREATE))
                syncOperationDao.upsertOperation(
                    syncOperation(
                        userId = userId,
                        entityId = tarea.id,
                        operationType = SyncOperationType.CREATE,
                        payloadJson = tarea.toPayloadJson()
                    )
                )
            }
        }

        return TareaResult.Success(tarea)
    }

    override fun editarTarea(
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

        return runBlocking(Dispatchers.IO) {
            val current = tareaDao.getTareaById(taskId, userId)
                ?: return@runBlocking TareaResult.Error("No se encontro la tarea.")

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
                tareaDao.upsertTarea(updated.toEntity(userId, SyncStatus.PENDING_UPDATE))
                syncOperationDao.upsertOperation(
                    syncOperation(
                        userId = userId,
                        entityId = updated.id,
                        operationType = SyncOperationType.UPDATE,
                        payloadJson = updated.toPayloadJson()
                    )
                )
            }

            if (current.photoPath != photoPath) {
                photoStorage.deletePhoto(current.photoPath)
            }

            TareaResult.Success(updated)
        }
    }

    override fun eliminarTarea(taskId: String): TareaResult {
        val userId = sessionProvider.currentUserId()
            ?: return TareaResult.Error("Inicia sesion para eliminar tareas.")

        return runBlocking(Dispatchers.IO) {
            val current = tareaDao.getTareaById(taskId, userId)
                ?: return@runBlocking TareaResult.Error("No se encontro la tarea.")
            val removed = current.toDomain()

            database.withTransaction {
                tareaDao.deleteTarea(taskId, userId)
                syncOperationDao.upsertOperation(
                    syncOperation(
                        userId = userId,
                        entityId = taskId,
                        operationType = SyncOperationType.DELETE,
                        payloadJson = null
                    )
                )
            }

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

    private fun Tarea.toPayloadJson(): String {
        return JSONObject()
            .put("id", id)
            .put("titulo", titulo)
            .put("categoriaCode", categoria.code)
            .put("rutinaId", rutinaId)
            .put("rutinaNombre", rutinaNombre)
            .put("dia", dia?.name)
            .put("horario", horario)
            .put("notas", notas)
            .put("photoPath", photoPath)
            .put("completada", completada)
            .toString()
    }
}

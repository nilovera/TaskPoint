package com.example.apk_mock.data.sync

import androidx.room.withTransaction
import com.example.apk_mock.data.local.SyncEntityType
import com.example.apk_mock.data.local.SyncOperationStatus
import com.example.apk_mock.data.local.SyncOperationType
import com.example.apk_mock.data.local.SyncStatus
import com.example.apk_mock.data.local.TaskPointDatabase
import com.example.apk_mock.data.local.entity.RutinaEntity
import com.example.apk_mock.data.local.entity.SyncOperationEntity
import com.example.apk_mock.data.local.entity.TareaEntity
import com.example.apk_mock.data.remote.RoutineApi
import com.example.apk_mock.data.remote.TaskApi
import com.example.apk_mock.data.remote.dto.DeleteRequestDto
import com.example.apk_mock.data.remote.dto.RoutineDto
import com.example.apk_mock.data.remote.dto.RoutineRequestDto
import com.example.apk_mock.data.remote.dto.TaskDto
import com.example.apk_mock.data.remote.dto.TaskRequestDto
import com.example.apk_mock.data.secure.SecureSessionStorage
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

data class SyncResult(
    val processed: Int = 0,
    val completed: Int = 0,
    val retryableFailures: Int = 0,
    val permanentFailures: Int = 0,
    val skipped: Boolean = false
)

/**
 * Sends local-first operations sequentially, preserving their creation order.
 * It never runs from the UI thread and does not decide when synchronization is scheduled.
 */
@Singleton
class SyncProcessor @Inject constructor(
    private val database: TaskPointDatabase,
    private val taskApi: TaskApi,
    private val routineApi: RoutineApi,
    private val sessionStorage: SecureSessionStorage
) {
    private val syncMutex = Mutex()
    private val syncOperationDao = database.syncOperationDao()
    private val tareaDao = database.tareaDao()
    private val rutinaDao = database.rutinaDao()
    private val categoriaDao = database.categoriaDao()

    suspend fun syncPendingOperations(limit: Int = DEFAULT_LIMIT): SyncResult = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            syncPendingOperationsLocked(limit.coerceIn(1, MAX_LIMIT))
        }
    }

    private suspend fun syncPendingOperationsLocked(limit: Int): SyncResult {
        val userId = sessionStorage.currentUserId()
            ?: return SyncResult(skipped = true).also {
                SyncLog.info("push_skipped", "reason=no_session")
            }
        val authorization = sessionStorage.currentAuthorizationHeader()
            ?: return SyncResult(skipped = true).also {
                SyncLog.info("push_skipped", "reason=no_token")
            }
        val operations = syncOperationDao.getOperations(userId, limit = limit)
        SyncLog.info("push_started", "operations=${operations.size}")

        var processed = 0
        var completed = 0
        var retryableFailures = 0
        var permanentFailures = 0

        for (operation in operations) {
            syncOperationDao.updateStatus(operation.id, SyncOperationStatus.IN_PROGRESS)
            processed += 1
            SyncLog.info("operation_started", operation.logDetails())

            try {
                val response = executeOperation(operation, authorization)
                completeOperation(operation, response)
                completed += 1
                SyncLog.info("operation_completed", operation.logDetails())
            } catch (error: CancellationException) {
                throw error
            } catch (error: HttpException) {
                if (error.code() == HTTP_CONFLICT) {
                    try {
                        resolveLastWriteWinsConflict(operation, authorization)
                        completed += 1
                    } catch (resolutionError: CancellationException) {
                        throw resolutionError
                    } catch (resolutionError: IOException) {
                        failOperation(
                            operation,
                            "Sin conexion con el servidor.",
                            SyncOperationStatus.FAILED_RETRYABLE
                        )
                        retryableFailures += 1
                        SyncLog.warn("operation_retryable_failure", operation.logDetails())
                        break
                    } catch (resolutionError: Exception) {
                        failOperation(
                            operation,
                            "No se pudo resolver el conflicto de sincronizacion.",
                            SyncOperationStatus.FAILED_PERMANENT
                        )
                        permanentFailures += 1
                        SyncLog.warn("operation_permanent_failure", operation.logDetails())
                    }
                    continue
                }

                if (operation.operationType == SyncOperationType.DELETE && error.code() == HTTP_NOT_FOUND) {
                    completeOperation(operation, null)
                    completed += 1
                    continue
                }

                if (error.isRetryable()) {
                    failOperation(
                        operation,
                        error.toSyncMessage(),
                        SyncOperationStatus.FAILED_RETRYABLE
                    )
                    retryableFailures += 1
                    SyncLog.warn("operation_retryable_failure", operation.logDetails())
                    break
                }

                failOperation(
                    operation,
                    error.toSyncMessage(),
                    SyncOperationStatus.FAILED_PERMANENT
                )
                permanentFailures += 1
                SyncLog.warn("operation_permanent_failure", operation.logDetails())
                continue
            } catch (error: IOException) {
                failOperation(
                    operation,
                    "Sin conexion con el servidor.",
                    SyncOperationStatus.FAILED_RETRYABLE
                )
                retryableFailures += 1
                SyncLog.warn("operation_retryable_failure", operation.logDetails())
                break
            } catch (error: Exception) {
                failOperation(
                    operation,
                    "No se pudo sincronizar la operacion.",
                    SyncOperationStatus.FAILED_PERMANENT
                )
                permanentFailures += 1
                SyncLog.warn("operation_permanent_failure", operation.logDetails())
                continue
            }
        }

        return SyncResult(processed, completed, retryableFailures, permanentFailures).also { result ->
            SyncLog.info(
                "push_finished",
                "processed=${result.processed} completed=${result.completed} retryable=${result.retryableFailures} permanent=${result.permanentFailures}"
            )
        }
    }

    private suspend fun executeOperation(
        operation: SyncOperationEntity,
        authorization: String
    ): SyncResponse? {
        return when (operation.entityType) {
            SyncEntityType.TAREA -> executeTaskOperation(operation, authorization)
            SyncEntityType.RUTINA -> executeRoutineOperation(operation, authorization)
            else -> throw IllegalArgumentException("Tipo de entidad no sincronizable.")
        }
    }

    private suspend fun executeTaskOperation(
        operation: SyncOperationEntity,
        authorization: String
    ): SyncResponse? {
        return when (operation.operationType) {
            SyncOperationType.CREATE -> SyncResponse.Task(
                taskApi.createTask(authorization, operation.taskRequest())
            )

            SyncOperationType.UPDATE -> SyncResponse.Task(
                taskApi.updateTask(authorization, operation.entityId, operation.taskRequest())
            )

            SyncOperationType.DELETE -> {
                taskApi.deleteTask(
                    authorization,
                    operation.entityId,
                    DeleteRequestDto(operation.payloadUpdatedAt())
                )
                null
            }
        }
    }

    private suspend fun executeRoutineOperation(
        operation: SyncOperationEntity,
        authorization: String
    ): SyncResponse? {
        return when (operation.operationType) {
            SyncOperationType.CREATE -> SyncResponse.Routine(
                routineApi.createRoutine(authorization, operation.routineRequest())
            )

            SyncOperationType.UPDATE -> SyncResponse.Routine(
                routineApi.updateRoutine(authorization, operation.entityId, operation.routineRequest())
            )

            SyncOperationType.DELETE -> {
                routineApi.deleteRoutine(
                    authorization,
                    operation.entityId,
                    DeleteRequestDto(operation.payloadUpdatedAt())
                )
                null
            }
        }
    }

    private suspend fun completeOperation(
        operation: SyncOperationEntity,
        response: SyncResponse?
    ) {
        database.withTransaction {
            when (response) {
                is SyncResponse.Task -> applyTaskResponse(operation, response.value)
                is SyncResponse.Routine -> applyRoutineResponse(operation, response.value)
                null -> Unit
            }
            syncOperationDao.updateStatus(operation.id, SyncOperationStatus.COMPLETED)
        }
    }

    private suspend fun applyTaskResponse(operation: SyncOperationEntity, remote: TaskDto) {
        val current = tareaDao.getTareaById(operation.entityId, operation.userId)
        if (current == null && operation.operationType != SyncOperationType.DELETE) return
        if (current != null && current.updatedAt > operation.payloadUpdatedAt()) return

        val category = if (current?.categoriaCode == remote.categoriaCode) {
            null
        } else {
            categoriaDao.getCategoriaByCode(remote.categoriaCode)
        }

        tareaDao.upsertTarea(
            TareaEntity(
                id = remote.id,
                userId = operation.userId,
                titulo = remote.titulo,
                categoriaId = category?.id ?: current?.categoriaId ?: UNKNOWN_CATEGORY_ID,
                categoriaName = category?.name ?: current?.categoriaName ?: UNKNOWN_CATEGORY_NAME,
                categoriaCode = remote.categoriaCode,
                categoriaDescription = category?.description
                    ?: current?.categoriaDescription
                    ?: UNKNOWN_CATEGORY_DESCRIPTION,
                categoriaActivatesOffers = category?.activatesOffers
                    ?: current?.categoriaActivatesOffers
                    ?: false,
                rutinaId = remote.rutinaId,
                rutinaNombre = remote.rutinaNombre,
                dia = remote.dia,
                horario = remote.horario,
                notas = remote.notas,
                photoPath = remote.photoPath,
                completada = remote.completada,
                syncStatus = SyncStatus.SYNCED,
                updatedAt = remote.updatedAt ?: operation.payloadUpdatedAt()
            )
        )
    }

    private suspend fun applyRoutineResponse(operation: SyncOperationEntity, remote: RoutineDto) {
        val current = rutinaDao.getRutinaById(operation.entityId, operation.userId)
        if (current == null && operation.operationType != SyncOperationType.DELETE) return
        if (current != null && current.updatedAt > operation.payloadUpdatedAt()) return

        rutinaDao.upsertRutina(
            RutinaEntity(
                id = remote.id,
                userId = operation.userId,
                nombre = remote.nombre,
                icono = remote.icono,
                direccion = remote.direccion,
                latitude = remote.latitude,
                longitude = remote.longitude,
                diasSemana = remote.diasSemana.joinToString(separator = ","),
                horarioInicio = remote.horarioInicio,
                horarioFin = remote.horarioFin,
                descripcion = remote.descripcion,
                syncStatus = SyncStatus.SYNCED,
                updatedAt = remote.updatedAt ?: operation.payloadUpdatedAt()
            )
        )
    }

    private suspend fun resolveLastWriteWinsConflict(
        operation: SyncOperationEntity,
        authorization: String
    ) {
        if (operation.operationType == SyncOperationType.DELETE) {
            val remote = when (operation.entityType) {
                SyncEntityType.TAREA -> taskApi.getTasks(authorization)
                    .firstOrNull { task -> task.id == operation.entityId }
                    ?.let(SyncResponse::Task)

                SyncEntityType.RUTINA -> routineApi.getRoutines(authorization)
                    .firstOrNull { routine -> routine.id == operation.entityId }
                    ?.let(SyncResponse::Routine)

                else -> null
            }
            completeOperation(operation, remote)
        } else {
            completeDeletedConflict(operation)
        }
    }

    private suspend fun completeDeletedConflict(operation: SyncOperationEntity) {
        database.withTransaction {
            when (operation.entityType) {
                SyncEntityType.TAREA -> tareaDao.getTareaById(operation.entityId, operation.userId)
                    ?.takeIf { it.updatedAt <= operation.payloadUpdatedAt() }
                    ?.let { tareaDao.deleteTarea(it.id, operation.userId) }

                SyncEntityType.RUTINA -> rutinaDao.getRutinaById(operation.entityId, operation.userId)
                    ?.takeIf { it.updatedAt <= operation.payloadUpdatedAt() }
                    ?.let { rutinaDao.deleteRutina(it.id, operation.userId) }

                else -> Unit
            }
            syncOperationDao.updateStatus(operation.id, SyncOperationStatus.COMPLETED)
        }
    }

    private suspend fun failOperation(
        operation: SyncOperationEntity,
        message: String,
        operationStatus: SyncOperationStatus
    ) {
        database.withTransaction {
            when (operation.entityType) {
                SyncEntityType.TAREA -> tareaDao.getTareaById(operation.entityId, operation.userId)
                    ?.takeIf { it.updatedAt <= operation.payloadUpdatedAt() }
                    ?.let { tareaDao.updateSyncStatus(it.id, operation.userId, SyncStatus.FAILED) }

                SyncEntityType.RUTINA -> rutinaDao.getRutinaById(operation.entityId, operation.userId)
                    ?.takeIf { it.updatedAt <= operation.payloadUpdatedAt() }
                    ?.let { rutinaDao.updateSyncStatus(it.id, operation.userId, SyncStatus.FAILED) }

                else -> Unit
            }
            syncOperationDao.markFailed(operation.id, message, operationStatus)
        }
    }

    private fun SyncOperationEntity.taskRequest(): TaskRequestDto {
        val payload = payload()
        return TaskRequestDto(
            id = payload.requiredString("id"),
            titulo = payload.requiredString("titulo"),
            categoriaCode = payload.requiredString("categoriaCode"),
            rutinaId = payload.stringOrNull("rutinaId"),
            rutinaNombre = payload.stringOrNull("rutinaNombre"),
            dia = payload.stringOrNull("dia"),
            horario = payload.stringOrNull("horario"),
            notas = payload.stringOrNull("notas") ?: "",
            photoPath = payload.stringOrNull("photoPath"),
            completada = payload.optBoolean("completada", false),
            updatedAt = payloadUpdatedAt()
        )
    }

    private fun SyncOperationEntity.routineRequest(): RoutineRequestDto {
        val payload = payload()
        return RoutineRequestDto(
            id = payload.requiredString("id"),
            nombre = payload.requiredString("nombre"),
            icono = payload.requiredString("icono"),
            direccion = payload.requiredString("direccion"),
            latitude = payload.doubleOrNull("latitude"),
            longitude = payload.doubleOrNull("longitude"),
            diasSemana = payload.getJSONArray("diasSemana").let { days ->
                List(days.length()) { index -> days.getString(index) }
            },
            horarioInicio = payload.requiredString("horarioInicio"),
            horarioFin = payload.requiredString("horarioFin"),
            descripcion = payload.requiredString("descripcion"),
            updatedAt = payloadUpdatedAt()
        )
    }

    private fun SyncOperationEntity.payload(): JSONObject {
        return JSONObject(payloadJson ?: throw IllegalArgumentException("Operacion sin datos."))
    }

    private fun SyncOperationEntity.payloadUpdatedAt(): Long {
        val payload = payloadJson
            ?.let { rawPayload -> runCatching { JSONObject(rawPayload) }.getOrNull() }
        return payload?.takeIf { it.has("updatedAt") && !it.isNull("updatedAt") }
            ?.optLong("updatedAt")
            ?.takeIf { it >= 0L }
            ?: createdAt
    }

    private fun SyncOperationEntity.logDetails(): String {
        return "entity=${entityType.name} operation=${operationType.name} id=${entityId.take(8)}"
    }

    private fun JSONObject.requiredString(name: String): String {
        return stringOrNull(name) ?: throw IllegalArgumentException("Campo obligatorio ausente.")
    }

    private fun JSONObject.stringOrNull(name: String): String? {
        if (!has(name) || isNull(name)) return null
        return optString(name).trim().takeIf { it.isNotEmpty() }
    }

    private fun JSONObject.doubleOrNull(name: String): Double? {
        if (!has(name) || isNull(name)) return null
        return optDouble(name, Double.NaN).takeIf { it.isFinite() }
    }

    private fun HttpException.toSyncMessage(): String {
        return "El servidor rechazo la operacion (codigo ${code()})."
    }

    private fun HttpException.isRetryable(): Boolean {
        return code() == HTTP_REQUEST_TIMEOUT || code() == HTTP_TOO_MANY_REQUESTS || code() >= HTTP_SERVER_ERROR
    }

    private sealed interface SyncResponse {
        data class Task(val value: TaskDto) : SyncResponse
        data class Routine(val value: RoutineDto) : SyncResponse
    }

    private companion object {
        const val DEFAULT_LIMIT = 50
        const val MAX_LIMIT = 100
        const val HTTP_NOT_FOUND = 404
        const val HTTP_CONFLICT = 409
        const val HTTP_REQUEST_TIMEOUT = 408
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val HTTP_SERVER_ERROR = 500
        const val UNKNOWN_CATEGORY_ID = -1
        const val UNKNOWN_CATEGORY_NAME = "Categoria no disponible"
        const val UNKNOWN_CATEGORY_DESCRIPTION = "Categoria no disponible en este dispositivo."
    }
}

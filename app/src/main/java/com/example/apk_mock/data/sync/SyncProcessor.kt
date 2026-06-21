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
import com.example.apk_mock.data.remote.dto.RoutineDto
import com.example.apk_mock.data.remote.dto.RoutineRequestDto
import com.example.apk_mock.data.remote.dto.TaskDto
import com.example.apk_mock.data.remote.dto.TaskRequestDto
import com.example.apk_mock.data.secure.SecureSessionStorage
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

data class SyncResult(
    val processed: Int = 0,
    val completed: Int = 0,
    val failed: Int = 0,
    val retryableFailure: Boolean = false,
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
        val userId = sessionStorage.currentUserId() ?: return SyncResult(skipped = true)
        val authorization = sessionStorage.currentAuthorizationHeader() ?: return SyncResult(skipped = true)
        val operations = syncOperationDao.getOperations(userId, limit = limit)

        var processed = 0
        var completed = 0
        var failed = 0
        var retryableFailure = false

        for (operation in operations) {
            syncOperationDao.updateStatus(operation.id, SyncOperationStatus.IN_PROGRESS)
            processed += 1

            try {
                val response = executeOperation(operation, authorization)
                completeOperation(operation, response)
                completed += 1
            } catch (error: HttpException) {
                if (operation.operationType == SyncOperationType.DELETE && error.code() == HTTP_NOT_FOUND) {
                    completeOperation(operation, null)
                    completed += 1
                    continue
                }

                failOperation(operation, error.toSyncMessage())
                failed += 1
                retryableFailure = error.code() >= HTTP_SERVER_ERROR
                break
            } catch (error: IOException) {
                failOperation(operation, "Sin conexion con el servidor.")
                failed += 1
                retryableFailure = true
                break
            } catch (error: Exception) {
                failOperation(operation, "No se pudo sincronizar la operacion.")
                failed += 1
                break
            }
        }

        return SyncResult(processed, completed, failed, retryableFailure)
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
                taskApi.deleteTask(authorization, operation.entityId)
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
                routineApi.deleteRoutine(authorization, operation.entityId)
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
        val current = tareaDao.getTareaById(operation.entityId, operation.userId) ?: return
        if (current.updatedAt > operation.payloadUpdatedAt()) return

        val category = if (current.categoriaCode == remote.categoriaCode) {
            null
        } else {
            categoriaDao.getCategoriaByCode(remote.categoriaCode)
        }

        tareaDao.upsertTarea(
            TareaEntity(
                id = remote.id,
                userId = operation.userId,
                titulo = remote.titulo,
                categoriaId = category?.id ?: current.categoriaId,
                categoriaName = category?.name ?: current.categoriaName,
                categoriaCode = remote.categoriaCode,
                categoriaDescription = category?.description ?: current.categoriaDescription,
                categoriaActivatesOffers = category?.activatesOffers ?: current.categoriaActivatesOffers,
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
        val current = rutinaDao.getRutinaById(operation.entityId, operation.userId) ?: return
        if (current.updatedAt > operation.payloadUpdatedAt()) return

        rutinaDao.upsertRutina(
            RutinaEntity(
                id = remote.id,
                userId = operation.userId,
                nombre = remote.nombre,
                icono = remote.icono,
                direccion = remote.direccion,
                diasSemana = remote.diasSemana.joinToString(separator = ","),
                horarioInicio = remote.horarioInicio,
                horarioFin = remote.horarioFin,
                descripcion = remote.descripcion,
                syncStatus = SyncStatus.SYNCED,
                updatedAt = remote.updatedAt ?: operation.payloadUpdatedAt()
            )
        )
    }

    private suspend fun failOperation(operation: SyncOperationEntity, message: String) {
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
            syncOperationDao.markFailed(operation.id, message)
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

    private fun JSONObject.requiredString(name: String): String {
        return stringOrNull(name) ?: throw IllegalArgumentException("Campo obligatorio ausente.")
    }

    private fun JSONObject.stringOrNull(name: String): String? {
        if (!has(name) || isNull(name)) return null
        return optString(name).trim().takeIf { it.isNotEmpty() }
    }

    private fun HttpException.toSyncMessage(): String {
        return "El servidor rechazo la operacion (codigo ${code()})."
    }

    private sealed interface SyncResponse {
        data class Task(val value: TaskDto) : SyncResponse
        data class Routine(val value: RoutineDto) : SyncResponse
    }

    private companion object {
        const val DEFAULT_LIMIT = 50
        const val MAX_LIMIT = 100
        const val HTTP_NOT_FOUND = 404
        const val HTTP_SERVER_ERROR = 500
    }
}

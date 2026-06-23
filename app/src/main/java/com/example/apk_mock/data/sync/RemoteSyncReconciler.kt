package com.example.apk_mock.data.sync

import androidx.room.withTransaction
import com.example.apk_mock.data.local.OfferCatalogImporter
import com.example.apk_mock.data.local.SyncEntityType
import com.example.apk_mock.data.local.SyncStatus
import com.example.apk_mock.data.local.TaskPointDatabase
import com.example.apk_mock.data.local.entity.RutinaEntity
import com.example.apk_mock.data.local.entity.SyncOperationEntity
import com.example.apk_mock.data.local.entity.TareaEntity
import com.example.apk_mock.data.remote.RoutineApi
import com.example.apk_mock.data.remote.TaskApi
import com.example.apk_mock.data.remote.dto.RoutineSyncDto
import com.example.apk_mock.data.remote.dto.TaskSyncDto
import com.example.apk_mock.data.secure.SecureSessionStorage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class RemoteSyncResult(
    val downloaded: Int = 0,
    val applied: Int = 0,
    val skipped: Boolean = false
)

/** Reconciles Firestore's visible state and tombstones with Room using last-write-wins. */
@Singleton
class RemoteSyncReconciler @Inject constructor(
    private val database: TaskPointDatabase,
    private val taskApi: TaskApi,
    private val routineApi: RoutineApi,
    private val sessionStorage: SecureSessionStorage,
    private val offerCatalogImporter: OfferCatalogImporter
) {
    private val reconciliationMutex = Mutex()
    private val tareaDao = database.tareaDao()
    private val rutinaDao = database.rutinaDao()
    private val categoriaDao = database.categoriaDao()
    private val syncOperationDao = database.syncOperationDao()

    suspend fun reconcileRemoteChanges(): RemoteSyncResult = reconciliationMutex.withLock {
        withContext(Dispatchers.IO) {
            val userId = sessionStorage.currentUserId() ?: return@withContext RemoteSyncResult(skipped = true)
            val authorization = sessionStorage.currentAuthorizationHeader()
                ?: return@withContext RemoteSyncResult(skipped = true)

            SyncLog.info("pull_started")
            offerCatalogImporter.importIfNeeded()
            val remoteRoutines = routineApi.getRoutineSyncRecords(authorization)
            val remoteTasks = taskApi.getTaskSyncRecords(authorization)
            var applied = 0

            database.withTransaction {
                remoteRoutines.forEach { remote ->
                    if (reconcileRoutine(userId, remote)) applied += 1
                }
                remoteTasks.forEach { remote ->
                    if (reconcileTask(userId, remote)) applied += 1
                }
            }

            RemoteSyncResult(
                downloaded = remoteRoutines.size + remoteTasks.size,
                applied = applied
            ).also { result ->
                SyncLog.info("pull_finished", "downloaded=${result.downloaded} applied=${result.applied}")
            }
        }
    }

    private suspend fun reconcileTask(userId: String, remote: TaskSyncDto): Boolean {
        val local = tareaDao.getTareaById(remote.id, userId)
        val newestLocalTimestamp = maxOfOrNull(
            local?.updatedAt,
            outstandingTimestamp(userId, SyncEntityType.TAREA, remote.id)
        )
        if (newestLocalTimestamp != null && newestLocalTimestamp > remote.updatedAt) return false

        if (remote.deleted) {
            tareaDao.deleteTarea(remote.id, userId)
            completeOutstandingOperations(userId, SyncEntityType.TAREA, remote.id)
            return local != null || newestLocalTimestamp != null
        }

        val title = remote.titulo ?: return false
        val categoryCode = remote.categoriaCode ?: return false
        val category = if (local?.categoriaCode == categoryCode) null else categoriaDao.getCategoriaByCode(categoryCode)

        tareaDao.upsertTarea(
            TareaEntity(
                id = remote.id,
                userId = userId,
                titulo = title,
                categoriaId = category?.id ?: local?.categoriaId ?: UNKNOWN_CATEGORY_ID,
                categoriaName = category?.name ?: local?.categoriaName ?: UNKNOWN_CATEGORY_NAME,
                categoriaCode = categoryCode,
                categoriaDescription = category?.description
                    ?: local?.categoriaDescription
                    ?: UNKNOWN_CATEGORY_DESCRIPTION,
                categoriaActivatesOffers = category?.activatesOffers
                    ?: local?.categoriaActivatesOffers
                    ?: false,
                rutinaId = remote.rutinaId,
                rutinaNombre = remote.rutinaNombre,
                dia = remote.dia,
                horario = remote.horario,
                notas = remote.notas.orEmpty(),
                photoPath = remote.photoPath,
                completada = remote.completada ?: false,
                syncStatus = SyncStatus.SYNCED,
                updatedAt = remote.updatedAt
            )
        )
        completeOutstandingOperations(userId, SyncEntityType.TAREA, remote.id)
        return true
    }

    private suspend fun reconcileRoutine(userId: String, remote: RoutineSyncDto): Boolean {
        val local = rutinaDao.getRutinaById(remote.id, userId)
        val newestLocalTimestamp = maxOfOrNull(
            local?.updatedAt,
            outstandingTimestamp(userId, SyncEntityType.RUTINA, remote.id)
        )
        if (newestLocalTimestamp != null && newestLocalTimestamp > remote.updatedAt) return false

        if (remote.deleted) {
            rutinaDao.deleteRutina(remote.id, userId)
            completeOutstandingOperations(userId, SyncEntityType.RUTINA, remote.id)
            return local != null || newestLocalTimestamp != null
        }

        val nombre = remote.nombre ?: return false
        val icono = remote.icono ?: return false
        val direccion = remote.direccion ?: return false
        val dias = remote.diasSemana ?: return false
        val horarioInicio = remote.horarioInicio ?: return false
        val horarioFin = remote.horarioFin ?: return false
        val descripcion = remote.descripcion ?: return false

        rutinaDao.upsertRutina(
            RutinaEntity(
                id = remote.id,
                userId = userId,
                nombre = nombre,
                icono = icono,
                direccion = direccion,
                latitude = remote.latitude,
                longitude = remote.longitude,
                diasSemana = dias.joinToString(separator = ","),
                horarioInicio = horarioInicio,
                horarioFin = horarioFin,
                descripcion = descripcion,
                syncStatus = SyncStatus.SYNCED,
                updatedAt = remote.updatedAt
            )
        )
        completeOutstandingOperations(userId, SyncEntityType.RUTINA, remote.id)
        return true
    }

    private suspend fun outstandingTimestamp(
        userId: String,
        entityType: SyncEntityType,
        entityId: String
    ): Long? {
        return syncOperationDao.getOutstandingOperationsForEntity(userId, entityType, entityId)
            .maxOfOrNull { operation -> operation.payloadTimestamp() }
    }

    private suspend fun completeOutstandingOperations(
        userId: String,
        entityType: SyncEntityType,
        entityId: String
    ) {
        syncOperationDao.completeOutstandingOperationsForEntity(userId, entityType, entityId)
    }

    private fun SyncOperationEntity.payloadTimestamp(): Long {
        val payload = payloadJson
            ?.let { rawPayload -> runCatching { JSONObject(rawPayload) }.getOrNull() }
        return payload?.takeIf { it.has("updatedAt") && !it.isNull("updatedAt") }
            ?.optLong("updatedAt")
            ?.takeIf { it >= 0L }
            ?: createdAt
    }

    private fun maxOfOrNull(first: Long?, second: Long?): Long? {
        return listOfNotNull(first, second).maxOrNull()
    }

    private companion object {
        const val UNKNOWN_CATEGORY_ID = -1
        const val UNKNOWN_CATEGORY_NAME = "Categoria no disponible"
        const val UNKNOWN_CATEGORY_DESCRIPTION = "Categoria no disponible en este dispositivo."
    }
}

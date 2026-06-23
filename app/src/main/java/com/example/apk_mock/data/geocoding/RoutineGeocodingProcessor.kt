package com.example.apk_mock.data.geocoding

import android.content.Context
import android.location.Geocoder
import androidx.room.withTransaction
import com.example.apk_mock.data.local.SyncEntityType
import com.example.apk_mock.data.local.SyncOperationType
import com.example.apk_mock.data.local.SyncStatus
import com.example.apk_mock.data.local.TaskPointDatabase
import com.example.apk_mock.data.local.entity.RutinaEntity
import com.example.apk_mock.data.local.entity.SyncOperationEntity
import com.example.apk_mock.data.secure.SecureSessionStorage
import com.example.apk_mock.data.sync.SyncLog
import com.example.apk_mock.data.sync.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal sealed interface GeocodingResult {
    data object Success : GeocodingResult
    data object NotFound : GeocodingResult
    data object Retry : GeocodingResult
    data object Skipped : GeocodingResult
}

/** Resolves a typed address through Android's geocoder and persists only its coordinates. */
@Singleton
class RoutineGeocodingProcessor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: TaskPointDatabase,
    private val sessionStorage: SecureSessionStorage,
    private val syncScheduler: SyncScheduler
) {
    private val rutinaDao = database.rutinaDao()
    private val syncOperationDao = database.syncOperationDao()

    @Suppress("DEPRECATION") // The asynchronous Geocoder API starts at API 33; minSdk is 26.
    internal suspend fun resolveRoutineAddress(
        userId: String,
        routineId: String,
        expectedAddress: String
    ): GeocodingResult = withContext(Dispatchers.IO) {
        if (sessionStorage.currentUserId() != userId) return@withContext GeocodingResult.Skipped

        val routine = rutinaDao.getRutinaById(routineId, userId)
            ?: return@withContext GeocodingResult.Skipped
        if (routine.direccion != expectedAddress) return@withContext GeocodingResult.Skipped
        if (routine.latitude != null && routine.longitude != null) return@withContext GeocodingResult.Success
        if (!Geocoder.isPresent()) {
            SyncLog.warn("geocoding_unavailable", "routine=${routineId.take(8)}")
            return@withContext GeocodingResult.NotFound
        }

        val location = try {
            Geocoder(context, Locale.getDefault())
                .getFromLocationName(expectedAddress, 1)
                ?.firstOrNull()
        } catch (error: IOException) {
            SyncLog.warn("geocoding_retry", "routine=${routineId.take(8)}")
            return@withContext GeocodingResult.Retry
        } catch (_: IllegalArgumentException) {
            null
        }

        if (location == null || location.latitude !in -90.0..90.0 || location.longitude !in -180.0..180.0) {
            SyncLog.warn("geocoding_not_found", "routine=${routineId.take(8)}")
            return@withContext GeocodingResult.NotFound
        }
        if (sessionStorage.currentUserId() != userId) return@withContext GeocodingResult.Skipped

        val timestamp = System.currentTimeMillis()
        val updated = routine.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            syncStatus = SyncStatus.PENDING_UPDATE,
            updatedAt = timestamp
        )

        database.withTransaction {
            val latest = rutinaDao.getRutinaById(routineId, userId)
                ?: return@withTransaction
            if (latest.direccion != expectedAddress || (latest.latitude != null && latest.longitude != null)) {
                return@withTransaction
            }
            rutinaDao.upsertRutina(updated)
            syncOperationDao.upsertOperation(
                SyncOperationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    entityType = SyncEntityType.RUTINA,
                    entityId = routineId,
                    operationType = SyncOperationType.UPDATE,
                    payloadJson = updated.toPayloadJson()
                )
            )
        }
        syncScheduler.schedulePendingSync()
        SyncLog.info("geocoding_completed", "routine=${routineId.take(8)}")
        GeocodingResult.Success
    }

    private fun RutinaEntity.toPayloadJson(): String {
        return JSONObject()
            .put("id", id)
            .put("nombre", nombre)
            .put("icono", icono)
            .put("direccion", direccion)
            .put("latitude", latitude)
            .put("longitude", longitude)
            .put("diasSemana", JSONArray(diasSemana.split(",").filter { it.isNotBlank() }))
            .put("horarioInicio", horarioInicio)
            .put("horarioFin", horarioFin)
            .put("descripcion", descripcion)
            .put("updatedAt", updatedAt)
            .toString()
    }
}

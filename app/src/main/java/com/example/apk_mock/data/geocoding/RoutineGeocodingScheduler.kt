package com.example.apk_mock.data.geocoding

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.apk_mock.data.local.TaskPointDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Schedules address resolution without coupling routine CRUD to network availability. */
@Singleton
class RoutineGeocodingScheduler @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context,
    private val database: TaskPointDatabase
) {
    suspend fun scheduleRoutine(userId: String, routineId: String, address: String) {
        if (userId.isBlank() || routineId.isBlank() || address.isBlank()) return

        withContext(Dispatchers.IO) {
            val request = OneTimeWorkRequestBuilder<RoutineGeocodingWorker>()
                .setInputData(
                    Data.Builder()
                        .putString(KEY_USER_ID, userId)
                        .putString(KEY_ROUTINE_ID, routineId)
                        .putString(KEY_ADDRESS, address)
                        .build()
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, INITIAL_BACKOFF_SECONDS, TimeUnit.SECONDS)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                uniqueWorkName(userId, routineId),
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    suspend fun schedulePendingRoutines(userId: String) {
        val routines = withContext(Dispatchers.IO) {
            database.rutinaDao().getRutinasWithoutCoordinates(userId)
        }
        routines.forEach { routine -> scheduleRoutine(userId, routine.id, routine.direccion) }
    }

    suspend fun cancelPendingGeocoding() {
        withContext(Dispatchers.IO) {
            WorkManager.getInstance(applicationContext).cancelAllWorkByTag(TAG)
        }
    }

    private fun uniqueWorkName(userId: String, routineId: String): String {
        return "$UNIQUE_WORK_PREFIX-$userId-$routineId"
    }

    internal companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_ROUTINE_ID = "routine_id"
        const val KEY_ADDRESS = "address"
        const val TAG = "taskpoint-routine-geocoding"
        private const val UNIQUE_WORK_PREFIX = "taskpoint-routine-geocoding"
        private const val INITIAL_BACKOFF_SECONDS = 10L
    }
}

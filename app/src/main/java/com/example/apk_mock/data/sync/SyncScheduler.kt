package com.example.apk_mock.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class SyncScheduler @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context
) {
    suspend fun schedulePendingSync() {
        withContext(Dispatchers.IO) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<PendingSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    INITIAL_BACKOFF_SECONDS,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
            SyncLog.info("worker_scheduled")
        }
    }

    suspend fun cancelPendingSync() {
        withContext(Dispatchers.IO) {
            WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_WORK_NAME)
            SyncLog.info("worker_cancelled")
        }
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "taskpoint-pending-sync"
        const val INITIAL_BACKOFF_SECONDS = 10L
    }
}

@HiltWorker
class PendingSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val syncProcessor: SyncProcessor
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val result = syncProcessor.syncPendingOperations()
        SyncLog.info(
            "worker_finished",
            "completed=${result.completed} retryable=${result.retryableFailures} permanent=${result.permanentFailures}"
        )
        return when {
            result.skipped || result.retryableFailures == 0 -> Result.success()
            result.retryableFailures > 0 -> Result.retry()
            else -> Result.success()
        }
    }
}

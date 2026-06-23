package com.example.apk_mock.data.geocoding

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RoutineGeocodingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val processor: RoutineGeocodingProcessor
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(RoutineGeocodingScheduler.KEY_USER_ID)
            ?: return Result.failure()
        val routineId = inputData.getString(RoutineGeocodingScheduler.KEY_ROUTINE_ID)
            ?: return Result.failure()
        val address = inputData.getString(RoutineGeocodingScheduler.KEY_ADDRESS)
            ?: return Result.failure()

        return when (processor.resolveRoutineAddress(userId, routineId, address)) {
            GeocodingResult.Success,
            GeocodingResult.NotFound,
            GeocodingResult.Skipped -> Result.success()
            GeocodingResult.Retry -> Result.retry()
        }
    }
}

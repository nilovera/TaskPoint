package com.example.apk_mock.data.sync

import com.example.apk_mock.data.local.TaskPointDatabase
import com.example.apk_mock.data.local.entity.SyncOperationEntity
import com.example.apk_mock.domain.repository.UserSessionProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Entry point for a future sync-status UI to retry an operation after the user fixes its cause. */
@Singleton
class SyncFailureManager @Inject constructor(
    private val database: TaskPointDatabase,
    private val sessionProvider: UserSessionProvider,
    private val syncScheduler: SyncScheduler
) {
    suspend fun getPermanentOperations(): List<SyncOperationEntity> {
        val userId = sessionProvider.currentUserId() ?: return emptyList()
        return withContext(Dispatchers.IO) {
            database.syncOperationDao().getPermanentOperations(userId)
        }
    }

    suspend fun retryPermanentOperation(operationId: String): Boolean {
        val userId = sessionProvider.currentUserId() ?: return false
        val retried = withContext(Dispatchers.IO) {
            database.syncOperationDao().retryPermanentOperation(operationId, userId) > 0
        }
        if (retried) syncScheduler.schedulePendingSync()
        return retried
    }
}

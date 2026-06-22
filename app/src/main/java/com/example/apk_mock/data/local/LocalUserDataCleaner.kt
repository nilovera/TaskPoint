package com.example.apk_mock.data.local

import androidx.room.withTransaction
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Removes all private local data only when the corresponding account is deleted. */
@Singleton
class LocalUserDataCleaner @Inject constructor(
    private val database: TaskPointDatabase
) {
    suspend fun clearUserData(userId: String) {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                database.tareaDao().deleteTareasByUser(userId)
                database.rutinaDao().deleteRutinasByUser(userId)
                database.syncOperationDao().deleteOperationsByUser(userId)
            }
        }
    }
}

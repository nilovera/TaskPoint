package com.example.apk_mock.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.apk_mock.data.local.SyncOperationStatus
import com.example.apk_mock.data.local.entity.SyncOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOperationDao {
    @Query("SELECT * FROM sync_operations WHERE userId = :userId AND status IN (:statuses) ORDER BY createdAt ASC")
    fun observeOperations(
        userId: String,
        statuses: List<SyncOperationStatus> = listOf(SyncOperationStatus.PENDING, SyncOperationStatus.FAILED)
    ): Flow<List<SyncOperationEntity>>

    @Query("SELECT * FROM sync_operations WHERE userId = :userId AND status IN (:statuses) ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getOperations(
        userId: String,
        statuses: List<SyncOperationStatus> = listOf(SyncOperationStatus.PENDING, SyncOperationStatus.FAILED),
        limit: Int = 50
    ): List<SyncOperationEntity>

    @Upsert
    suspend fun upsertOperation(operation: SyncOperationEntity)

    @Upsert
    suspend fun upsertOperations(operations: List<SyncOperationEntity>)

    @Query("UPDATE sync_operations SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(
        id: String,
        status: SyncOperationStatus,
        updatedAt: Long = System.currentTimeMillis()
    ): Int

    @Query("UPDATE sync_operations SET status = :status, attempts = attempts + 1, lastError = :lastError, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markFailed(
        id: String,
        lastError: String?,
        status: SyncOperationStatus = SyncOperationStatus.FAILED,
        updatedAt: Long = System.currentTimeMillis()
    ): Int

    @Query("DELETE FROM sync_operations WHERE id = :id")
    suspend fun deleteOperation(id: String): Int

    @Query("DELETE FROM sync_operations WHERE userId = :userId AND status = :status")
    suspend fun deleteOperationsByStatus(
        userId: String,
        status: SyncOperationStatus = SyncOperationStatus.COMPLETED
    ): Int

    @Query("DELETE FROM sync_operations WHERE userId = :userId")
    suspend fun deleteOperationsByUser(userId: String): Int
}

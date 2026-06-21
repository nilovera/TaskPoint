package com.example.apk_mock.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.apk_mock.data.local.SyncEntityType
import com.example.apk_mock.data.local.SyncOperationStatus
import com.example.apk_mock.data.local.entity.SyncOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOperationDao {
    @Query("SELECT * FROM sync_operations WHERE userId = :userId AND status IN (:statuses) ORDER BY createdAt ASC")
    fun observeOperations(
        userId: String,
        statuses: List<SyncOperationStatus> = listOf(
            SyncOperationStatus.PENDING,
            SyncOperationStatus.FAILED_RETRYABLE,
            SyncOperationStatus.IN_PROGRESS
        )
    ): Flow<List<SyncOperationEntity>>

    @Query("SELECT * FROM sync_operations WHERE userId = :userId AND status IN (:statuses) ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getOperations(
        userId: String,
        statuses: List<SyncOperationStatus> = listOf(
            SyncOperationStatus.PENDING,
            SyncOperationStatus.FAILED_RETRYABLE,
            SyncOperationStatus.IN_PROGRESS
        ),
        limit: Int = 50
    ): List<SyncOperationEntity>

    @Query("SELECT * FROM sync_operations WHERE userId = :userId AND status = :status ORDER BY updatedAt DESC")
    fun observePermanentOperations(
        userId: String,
        status: SyncOperationStatus = SyncOperationStatus.FAILED_PERMANENT
    ): Flow<List<SyncOperationEntity>>

    @Query("SELECT * FROM sync_operations WHERE userId = :userId AND status = :status ORDER BY updatedAt DESC")
    suspend fun getPermanentOperations(
        userId: String,
        status: SyncOperationStatus = SyncOperationStatus.FAILED_PERMANENT
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
        status: SyncOperationStatus = SyncOperationStatus.FAILED_RETRYABLE,
        updatedAt: Long = System.currentTimeMillis()
    ): Int

    @Query("UPDATE sync_operations SET status = :pendingStatus, lastError = NULL, updatedAt = :updatedAt WHERE id = :id AND userId = :userId AND status = :permanentStatus")
    suspend fun retryPermanentOperation(
        id: String,
        userId: String,
        pendingStatus: SyncOperationStatus = SyncOperationStatus.PENDING,
        permanentStatus: SyncOperationStatus = SyncOperationStatus.FAILED_PERMANENT,
        updatedAt: Long = System.currentTimeMillis()
    ): Int

    @Query("UPDATE sync_operations SET status = :completedStatus, lastError = NULL, updatedAt = :updatedAt WHERE userId = :userId AND entityType = :entityType AND entityId = :entityId AND status != :completedStatus")
    suspend fun completeOutstandingOperationsForEntity(
        userId: String,
        entityType: SyncEntityType,
        entityId: String,
        completedStatus: SyncOperationStatus = SyncOperationStatus.COMPLETED,
        updatedAt: Long = System.currentTimeMillis()
    ): Int

    @Query("SELECT * FROM sync_operations WHERE userId = :userId AND entityType = :entityType AND entityId = :entityId AND status != :completedStatus ORDER BY createdAt ASC")
    suspend fun getOutstandingOperationsForEntity(
        userId: String,
        entityType: SyncEntityType,
        entityId: String,
        completedStatus: SyncOperationStatus = SyncOperationStatus.COMPLETED
    ): List<SyncOperationEntity>

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

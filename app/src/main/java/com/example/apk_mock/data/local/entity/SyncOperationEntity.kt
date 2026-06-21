package com.example.apk_mock.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.apk_mock.data.local.SyncEntityType
import com.example.apk_mock.data.local.SyncOperationStatus
import com.example.apk_mock.data.local.SyncOperationType

@Entity(
    tableName = "sync_operations",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["entityType", "entityId"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"])
    ]
)
data class SyncOperationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val entityType: SyncEntityType,
    val entityId: String,
    val operationType: SyncOperationType,
    val payloadJson: String?,
    val status: SyncOperationStatus = SyncOperationStatus.PENDING,
    val attempts: Int = 0,
    val lastError: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

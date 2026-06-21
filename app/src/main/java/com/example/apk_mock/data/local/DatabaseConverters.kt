package com.example.apk_mock.data.local

import androidx.room.TypeConverter

class DatabaseConverters {
    @TypeConverter
    fun syncStatusToString(value: SyncStatus): String {
        return value.name
    }

    @TypeConverter
    fun stringToSyncStatus(value: String): SyncStatus {
        return SyncStatus.valueOf(value)
    }

    @TypeConverter
    fun syncOperationStatusToString(value: SyncOperationStatus): String {
        return value.name
    }

    @TypeConverter
    fun stringToSyncOperationStatus(value: String): SyncOperationStatus {
        return SyncOperationStatus.valueOf(value)
    }

    @TypeConverter
    fun syncOperationTypeToString(value: SyncOperationType): String {
        return value.name
    }

    @TypeConverter
    fun stringToSyncOperationType(value: String): SyncOperationType {
        return SyncOperationType.valueOf(value)
    }

    @TypeConverter
    fun syncEntityTypeToString(value: SyncEntityType): String {
        return value.name
    }

    @TypeConverter
    fun stringToSyncEntityType(value: String): SyncEntityType {
        return SyncEntityType.valueOf(value)
    }
}

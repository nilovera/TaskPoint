package com.example.apk_mock.data.local

enum class SyncOperationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED_RETRYABLE,
    FAILED_PERMANENT
}

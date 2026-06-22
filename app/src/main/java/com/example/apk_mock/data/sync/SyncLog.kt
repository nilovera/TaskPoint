package com.example.apk_mock.data.sync

import android.util.Log

internal object SyncLog {
    private const val TAG = "TaskPointSync"

    fun info(event: String, details: String = "") {
        Log.i(TAG, format(event, details))
    }

    fun warn(event: String, details: String = "") {
        Log.w(TAG, format(event, details))
    }

    private fun format(event: String, details: String): String {
        return if (details.isBlank()) "event=$event" else "event=$event $details"
    }
}

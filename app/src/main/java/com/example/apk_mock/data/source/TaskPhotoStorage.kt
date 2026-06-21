package com.example.apk_mock.data.source

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskPhotoStorage(private val context: Context) {

    suspend fun createPhotoFile(): File = withContext(Dispatchers.IO) {
        val directory = File(context.filesDir, PHOTO_DIRECTORY).apply {
            if (!exists()) mkdirs()
        }
        val timestamp = SimpleDateFormat(FILE_TIMESTAMP_PATTERN, Locale.US).format(Date())
        File(directory, "task_photo_$timestamp.jpg")
    }

    suspend fun deletePhoto(photoPath: String?) {
        if (photoPath.isNullOrBlank()) return
        withContext(Dispatchers.IO) {
            runCatching {
                val photoFile = File(photoPath)
                val photoDirectory = File(context.filesDir, PHOTO_DIRECTORY).canonicalFile
                if (photoFile.exists() && photoFile.canonicalFile.startsWith(photoDirectory)) {
                    photoFile.delete()
                }
            }
        }
    }

    private companion object {
        const val PHOTO_DIRECTORY = "task_photos"
        const val FILE_TIMESTAMP_PATTERN = "yyyyMMdd_HHmmss_SSS"
    }
}

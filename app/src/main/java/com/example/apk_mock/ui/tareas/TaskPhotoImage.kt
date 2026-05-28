package com.example.apk_mock.ui.tareas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.apk_mock.ui.theme.TaskPointTheme
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TaskPhotoImage(
    photoPath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val colors = TaskPointTheme.colors
    val bitmap by produceState<Bitmap?>(initialValue = null, photoPath) {
        value = withContext(Dispatchers.IO) {
            decodeTaskPhoto(photoPath)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.success.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = contentDescription,
                tint = colors.success
            )
        }
    }
}

private fun decodeTaskPhoto(photoPath: String?): Bitmap? {
    if (photoPath.isNullOrBlank()) return null
    val photoFile = File(photoPath)
    if (!photoFile.exists()) return null

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(photoFile.absolutePath, bounds)

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(bounds, maxDimension = 1200)
    }
    return BitmapFactory.decodeFile(photoFile.absolutePath, options)
}

private fun calculateInSampleSize(options: BitmapFactory.Options, maxDimension: Int): Int {
    var sampleSize = 1
    var height = options.outHeight
    var width = options.outWidth

    while (height / sampleSize > maxDimension || width / sampleSize > maxDimension) {
        sampleSize *= 2
    }

    return sampleSize
}

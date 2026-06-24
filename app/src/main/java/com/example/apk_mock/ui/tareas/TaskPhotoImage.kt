package com.example.apk_mock.ui.tareas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.apk_mock.ui.theme.TaskPointTheme
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TaskPhotoImage(
    photoPath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: (() -> Unit)? = null,
    containerColor: Color? = null
) {
    val colors = TaskPointTheme.colors
    val bitmap by produceState<Bitmap?>(initialValue = null, photoPath) {
        value = withContext(Dispatchers.IO) {
            decodeTaskPhoto(photoPath)
        }
    }

    val imageModifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = "Ampliar foto",
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .background(containerColor ?: colors.success.copy(alpha = 0.18f))

    Box(
        modifier = imageModifier,
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
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

@Composable
fun ExpandedTaskPhotoDialog(
    photoPath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss)
                .background(Color.Black)
        ) {
            TaskPhotoImage(
                photoPath = photoPath,
                contentDescription = "Foto de la tarea ampliada",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = ContentScale.Fit,
                containerColor = Color.Black
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.56f), RoundedCornerShape(24.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar foto ampliada",
                    tint = Color.White
                )
            }
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
    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath, options) ?: return null
    return bitmap.withExifOrientation(photoFile)
}

private fun Bitmap.withExifOrientation(photoFile: File): Bitmap {
    val orientation = runCatching {
        ExifInterface(photoFile.absolutePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    val matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                setRotate(90f)
                postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                setRotate(-90f)
                postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
        }
    }

    return if (matrix.isIdentity) this else Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
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

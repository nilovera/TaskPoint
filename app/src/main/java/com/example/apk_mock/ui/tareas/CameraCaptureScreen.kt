package com.example.apk_mock.ui.tareas

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.Surface as AndroidSurface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.apk_mock.data.source.TaskPhotoStorage
import com.example.apk_mock.ui.theme.TaskPointTheme

@Composable
fun CameraCaptureScreen(
    onBack: () -> Unit,
    onPhotoCaptured: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = TaskPointTheme.colors
    val hasCamera = remember(context) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
    var hasPermission by remember(context) { mutableStateOf(context.hasCameraPermission()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(hasCamera) {
        if (hasCamera && !hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        when {
            !hasCamera -> CameraMessage(
                title = "Camara no disponible",
                message = "Este dispositivo no informa un sensor de camara usable.",
                onBack = onBack
            )

            !hasPermission -> CameraMessage(
                title = "Permiso de camara",
                message = "Necesitamos permiso para abrir la camara y adjuntar una foto.",
                actionLabel = "Permitir",
                onAction = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onBack = onBack
            )

            else -> CameraPreviewCapture(
                onBack = onBack,
                onPhotoCaptured = onPhotoCaptured
            )
        }
    }
}

@Composable
private fun CameraPreviewCapture(
    onBack: () -> Unit,
    onPhotoCaptured: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = TaskPointTheme.colors
    val photoStorage = remember(context) { TaskPhotoStorage(context) }
    val coroutineScope = rememberCoroutineScope()
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            onImageCaptureReady = { imageCapture = it },
            onCameraError = { cameraError = it }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = CircleShape
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }
        }

        cameraError?.let { error ->
            Surface(
                color = colors.surface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 68.dp, start = 20.dp, end = 20.dp)
            ) {
                Text(
                    text = error,
                    color = colors.destructive,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Surface(
            color = Color.Black.copy(alpha = 0.58f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        val capture = imageCapture ?: return@Button
                        isCapturing = true
                        cameraError = null
                        coroutineScope.launch {
                            runCatching { photoStorage.createPhotoFile() }
                                .onSuccess { photoFile ->
                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                    capture.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                isCapturing = false
                                                onPhotoCaptured(photoFile.absolutePath)
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                isCapturing = false
                                                coroutineScope.launch { photoStorage.deletePhoto(photoFile.absolutePath) }
                                                cameraError = "No pudimos guardar la foto. Proba de nuevo."
                                            }
                                        }
                                    )
                                }
                                .onFailure {
                                    isCapturing = false
                                    cameraError = "No pudimos preparar el archivo de la foto."
                                }
                        }
                    },
                    enabled = imageCapture != null && !isCapturing,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.success),
                    modifier = Modifier.size(76.dp)
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Sacar foto",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (isCapturing) "Guardando foto..." else "Sacar foto",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCameraError: (String) -> Unit
) {
    if (LocalInspectionMode.current) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(context, lifecycleOwner, previewView) {
        var cameraProvider: ProcessCameraProvider? = null
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener(
            {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder()
                    .setTargetRotation(previewView.display?.rotation ?: AndroidSurface.ROTATION_0)
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(previewView.display?.rotation ?: AndroidSurface.ROTATION_0)
                    .build()

                val cameraSelector = provider.selectAvailableCamera()
                if (cameraSelector == null) {
                    onCameraError("No encontramos una camara disponible.")
                    return@addListener
                }

                runCatching {
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                    onImageCaptureReady(imageCapture)
                }.onFailure {
                    onCameraError("No pudimos iniciar la camara.")
                }
            },
            mainExecutor
        )

        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun CameraMessage(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val colors = TaskPointTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(message, color = colors.textSecondary, fontSize = 15.sp)
        Spacer(Modifier.height(22.dp))
        if (actionLabel != null && onAction != null) {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = colors.success)
            ) {
                Text(actionLabel, color = Color.White)
            }
            Spacer(Modifier.height(10.dp))
        }
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
        ) {
            Text("Volver", color = Color.White)
        }
    }
}

private fun Context.hasCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
}

private fun ProcessCameraProvider.selectAvailableCamera(): CameraSelector? {
    return when {
        runCatching { hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) }.getOrDefault(false) ->
            CameraSelector.DEFAULT_BACK_CAMERA

        runCatching { hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) }.getOrDefault(false) ->
            CameraSelector.DEFAULT_FRONT_CAMERA

        else -> null
    }
}

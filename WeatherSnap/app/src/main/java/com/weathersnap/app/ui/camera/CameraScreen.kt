package com.weathersnap.app.ui.camera

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.io.FileOutputStream

private const val TAG = "CameraScreen"

/**
 * Full-screen camera composable using CameraX.
 *
 * Handles runtime camera permission, shows a live preview,
 * and captures + compresses a JPEG photo on demand.
 *
 * @param onClose            Called when the user taps the close button.
 * @param onPhotoCaptured    Called after a photo is captured and compressed,
 *                           providing the compressed file path and sizes in KB.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onClose: () -> Unit,
    onPhotoCaptured: (compressedFilePath: String, originalSizeKb: Long, compressedSizeKb: Long) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ── Permission state ────────────────────────────────────────
    var hasCameraPermission by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        permissionRequested = true
    }

    // Request permission on first composition
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (granted) {
            hasCameraPermission = true
            permissionRequested = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ── CameraX setup ───────────────────────────────────────────
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Bind camera when permission is granted
    DisposableEffect(hasCameraPermission) {
        if (!hasCameraPermission) return@DisposableEffect onDispose {}

        val provider = cameraProviderFuture.get()
        cameraProvider = provider

        onDispose {
            provider.unbindAll()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Custom Camera",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close camera"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (hasCameraPermission) {
            // ── Camera preview + capture ────────────────────────
            CameraContent(
                cameraProvider = cameraProvider,
                imageCapture = imageCapture,
                context = context,
                onPhotoCaptured = onPhotoCaptured,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else if (permissionRequested) {
            // ── Permission denied ───────────────────────────────
            PermissionDeniedContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Sub-composables
// ─────────────────────────────────────────────────────────────────────

/**
 * Camera preview with a capture button at the bottom.
 */
@Composable
private fun CameraContent(
    cameraProvider: ProcessCameraProvider?,
    imageCapture: ImageCapture,
    context: Context,
    onPhotoCaptured: (String, Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Column(modifier = modifier) {
        // ── Live preview ────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                update = { previewView ->
                    cameraProvider?.let { provider ->
                        provider.unbindAll()

                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()

                        try {
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "CameraX bind failed", e)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Capture button ──────────────────────────────────────
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                captureAndCompress(
                    context = context,
                    imageCapture = imageCapture,
                    onPhotoCaptured = onPhotoCaptured
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Capture",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Shown when the user denies camera permission.
 */
@Composable
private fun PermissionDeniedContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.NoPhotography,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Camera permission is required",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please grant camera access in your device settings to capture weather report photos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Capture + compression logic
// ─────────────────────────────────────────────────────────────────────

/**
 * Captures a photo via CameraX [ImageCapture], compresses the result
 * to 60 % JPEG quality, and invokes [onPhotoCaptured] with paths and
 * size metrics.
 */
private fun captureAndCompress(
    context: Context,
    imageCapture: ImageCapture,
    onPhotoCaptured: (compressedFilePath: String, originalSizeKb: Long, compressedSizeKb: Long) -> Unit
) {
    val timestamp = System.currentTimeMillis()
    val originalFile = File(context.filesDir, "photo_${timestamp}.jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(originalFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val originalSizeKb = originalFile.length() / 1024

                // Compress to 60 % JPEG quality
                val compressedFile = File(context.filesDir, "photo_${timestamp}_compressed.jpg")
                try {
                    val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
                    FileOutputStream(compressedFile).use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos)
                    }
                    bitmap.recycle()

                    val compressedSizeKb = compressedFile.length() / 1024

                    // Clean up the original uncompressed file
                    originalFile.delete()

                    onPhotoCaptured(
                        compressedFile.absolutePath,
                        originalSizeKb,
                        compressedSizeKb
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Compression failed", e)
                    // Fall back to the original file if compression fails
                    onPhotoCaptured(
                        originalFile.absolutePath,
                        originalSizeKb,
                        originalSizeKb
                    )
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed", exception)
                Toast.makeText(
                    context,
                    "Capture failed: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )
}

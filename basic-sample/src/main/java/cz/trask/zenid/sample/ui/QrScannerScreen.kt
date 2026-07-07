package cz.trask.zenid.sample.ui

import android.Manifest
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    onQrScanned: (url: String, apiKey: String) -> Unit,
    onBack: () -> Unit
) {
    // Skip permission handling in Preview mode - accompanist permissions don't work in Preview
    val isInPreview = LocalInspectionMode.current
    val cameraPermission = if (!isInPreview) {
        rememberPermissionState(Manifest.permission.CAMERA)
    } else {
        null
    }
    
    DisposableEffect(Unit) {
        if (cameraPermission != null && !cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
        onDispose {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        // In Preview mode, show a placeholder; otherwise check camera permission
        if (isInPreview || (cameraPermission != null && cameraPermission.status.isGranted)) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Skip camera preview in Preview mode - ML Kit can't be initialized
                if (!isInPreview) {
                    QrCameraPreview(onQrScanned = onQrScanned)
                }
                QrOverlay(modifier = Modifier.fillMaxSize())
                Text(
                    text = "Point at a ZenID QR code",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Camera permission required")
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun QrCameraPreview(onQrScanned: (url: String, apiKey: String) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanned = remember { AtomicBoolean(false) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = CameraPreview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(
                        ResolutionSelector.Builder()
                            .setResolutionStrategy(ResolutionStrategy(Size(1280, 720), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
                            .build()
                    )
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    if (scanned.get()) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    val mediaImage = imageProxy.image ?: run { imageProxy.close(); return@setAnalyzer }
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                                ?.rawValue
                                ?.let { value ->
                                    val parts = value.trim().split(" ")
                                    if (parts.size >= 2 && scanned.compareAndSet(false, true)) {
                                        onQrScanned(parts[0], parts[1])
                                    }
                                }
                        }
                        .addOnFailureListener { Timber.e(it) }
                        .addOnCompleteListener { imageProxy.close() }
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        UseCaseGroup.Builder().addUseCase(preview).addUseCase(imageAnalysis).build()
                    )
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
}

@Composable
private fun QrOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val boxSize = size.minDimension * 0.65f
        val left = (size.width - boxSize) / 2
        val top = (size.height - boxSize) / 2
        val cornerLen = boxSize * 0.12f
        val stroke = Stroke(
            width = 4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(cornerLen, boxSize - cornerLen))
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = ComposeSize(boxSize, boxSize),
            cornerRadius = CornerRadius(8.dp.toPx()),
            style = stroke
        )
    }
}

@Preview(showBackground = true, name = "QR Scanner Screen")
@Composable
private fun QrScannerScreenPreview() {
    MaterialTheme {
        QrScannerScreen(onQrScanned = { _, _ -> }, onBack = {})
    }
}

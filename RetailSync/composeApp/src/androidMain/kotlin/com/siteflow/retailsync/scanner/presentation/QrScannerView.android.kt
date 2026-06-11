package com.siteflow.retailsync.scanner.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "QrScanner"

@Composable
actual fun QrScannerView(
    modifier: Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        Log.d(TAG, "Camera permission granted: $granted")
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            Log.d(TAG, "Requesting camera permission...")
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            Log.d(TAG, "Camera permission already granted")
        }
    }

    if (!hasCameraPermission) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission required. Please grant permission.", color = Color.White)
        }
        return
    }

    val hasScanned = remember { AtomicBoolean(false) }
    val frameCount = remember { AtomicInteger(0) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            Log.d(TAG, "=== Creating camera preview ===")
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            val executor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    Log.d(TAG, "CameraProvider obtained successfully")

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    Log.d(TAG, "Preview built, surface provider set")

                    // Use ALL formats
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                    val barcodeScanner = BarcodeScanning.getClient(options)
                    Log.d(TAG, "ML Kit barcode scanner created with QR_CODE format")

                    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                    val resolutionSelector = androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            androidx.camera.core.resolutionselector.ResolutionStrategy(
                                Size(1280, 720),
                                androidx.camera.core.resolutionselector.ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                            )
                        )
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(executor) { imageProxy ->
                                val count = frameCount.incrementAndGet()
                                val mediaImage = imageProxy.image

                                if (mediaImage == null) {
                                    if (count % 100 == 0) Log.w(TAG, "Frame #$count: mediaImage is NULL")
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                if (hasScanned.get()) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                if (count % 30 == 0) {
                                    Log.d(TAG, "Frame #$count: analyzing ${mediaImage.width}x${mediaImage.height}, rotation=${imageProxy.imageInfo.rotationDegrees}")
                                }

                                val inputImage = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                                barcodeScanner.process(inputImage)
                                    .addOnSuccessListener { barcodes ->
                                        if (count % 30 == 0) {
                                            Log.d(TAG, "Frame #$count: ML Kit result = ${barcodes.size} barcodes")
                                        }
                                        if (barcodes.isNotEmpty()) {
                                            Log.d(TAG, ">>> BARCODES FOUND: ${barcodes.size}")
                                            for (barcode in barcodes) {
                                                Log.d(TAG, "  format=${barcode.format}, valueType=${barcode.valueType}")
                                                Log.d(TAG, "  rawValue=${barcode.rawValue}")
                                                Log.d(TAG, "  displayValue=${barcode.displayValue}")

                                                val rawValue = barcode.rawValue
                                                if (rawValue != null && hasScanned.compareAndSet(false, true)) {
                                                    Log.d(TAG, ">>> QR SCANNED SUCCESSFULLY: $rawValue")
                                                    onQrCodeScanned(rawValue)
                                                    return@addOnSuccessListener
                                                }
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        if (count % 30 == 0) {
                                            Log.e(TAG, "Frame #$count: ML Kit processing FAILED: ${e.message}")
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            }
                        }

                    Log.d(TAG, "ImageAnalysis built, binding to lifecycle...")

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    Log.d(TAG, "=== Camera bound to lifecycle SUCCESSFULLY ===")
                } catch (e: Exception) {
                    Log.e(TAG, "Camera setup FAILED: ${e.message}", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

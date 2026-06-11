// androidMain
package com.siteflow.cdo.core.domain

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_BASE
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File

actual class ImagePicker {

    private lateinit var activity: ComponentActivity

    // ── Camera ────────────────────────────────────────────────────────────────

    private var onImagePicked: ((String) -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null
    private lateinit var photoFile: File

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>

    // ── ML Kit Document Scanner ───────────────────────────────────────────────

    private var onScannerImagePicked: ((String) -> Unit)? = null
    private var onScannerPermissionDenied: (() -> Unit)? = null
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>

    fun bind(activity: ComponentActivity) {
        this.activity = activity

        cameraPermissionLauncher =
            activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) launchCamera()
                else onPermissionDenied?.invoke()
            }

        cameraLauncher =
            activity.registerForActivityResult(
                ActivityResultContracts.TakePicture()
            ) { success ->
                if (success) onImagePicked?.invoke(photoFile.absolutePath)
            }

        galleryLauncher =
            activity.registerForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri ->
                uri?.let { onImagePicked?.invoke(copyUriToCache(it)) }
            }

        scannerLauncher =
            activity.registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                    val imageUri = scanResult?.pages?.firstOrNull()?.imageUri
                    if (imageUri != null) {
                        onScannerImagePicked?.invoke(copyUriToCache(imageUri))
                    }
                }
                // RESULT_CANCELED = user dismissed scanner — do nothing
            }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    actual fun openCamera(
        onImagePicked: (String) -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        this.onImagePicked = onImagePicked
        this.onPermissionDenied = onPermissionDenied

        val hasPermission =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) launchCamera()
        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    actual fun openGallery(onImagePicked: (String) -> Unit) {
        this.onImagePicked = onImagePicked
        galleryLauncher.launch("image/*")
    }

    /**
     * Launches the ML Kit Document Scanner UI.
     *
     * The scanner provides an Adobe Scan-style overlay with live edge detection,
     * auto-crop, and perspective correction — returning a processed JPEG path.
     * Falls back to [openCamera] if Play Services / ML Kit is unavailable.
     */
    actual fun openDocumentScanner(
        onImagePicked: (String) -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        this.onScannerImagePicked = onImagePicked
        this.onScannerPermissionDenied = onPermissionDenied

        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG)
            .setScannerMode(SCANNER_MODE_BASE)
            .build()

        GmsDocumentScanning.getClient(options)
            .getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                // Play Services unavailable or ML Kit failed — fall back to standard camera
                openCamera(onImagePicked, onPermissionDenied)
            }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun launchCamera() {
        photoFile = createTempImageFile()
        val uri = FileProviderUtil.getUri(activity, photoFile)
        cameraLauncher.launch(uri)
    }

    private fun createTempImageFile(): File {
        val dir = File(activity.cacheDir, "images").apply { mkdirs() }
        return File.createTempFile("camera_", ".jpg", dir)
    }

    private fun copyUriToCache(uri: Uri): String {
        val file = createTempImageFile()
        activity.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }
}

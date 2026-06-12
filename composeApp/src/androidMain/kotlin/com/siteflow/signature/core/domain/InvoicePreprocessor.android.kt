package com.siteflow.signature.core.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.siteflow.signature.core.domain.model.ProcessedInvoiceImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Android implementation of the invoice preprocessing pipeline.
 *
 * Edge detection and perspective correction are intentionally NOT performed here.
 * The ML Kit Document Scanner (launched via [ImagePicker.openDocumentScanner])
 * already returns a cropped, perspective-corrected JPEG. Running a second
 * warp pass on that output would re-distort the image and cause tilt.
 *
 * For gallery-picked images the input is used as-is — no edge detection fallback.
 *
 * Pipeline (runs on [Dispatchers.Default]):
 *  1. Validate & load JPEG from [imagePath]
 *  2. Quality analysis on a ~600px-wide downscale (before any modifications)
 *  3. Convert to grayscale
 *  4. Enhance contrast (1.35×, centred on mid-grey)
 *  5. Resize to max 1200px width (proportional)
 *  6. JPEG compress to target ≤ 250 KB
 */
actual class InvoicePreprocessor(private val context: Context) {

    actual suspend fun preprocessForUpload(
        imagePath: String,
        filter: InvoiceFilter
    ): Result<ProcessedInvoiceImage> =
        withContext(Dispatchers.Default) {
            runCatching {
                val t0 = System.currentTimeMillis()

                // ── 1. Validate & Load ─────────────────────────────────────────
                if (imagePath.isBlank() || !File(imagePath).exists())
                    throw PreprocessingError.ImageNull

                val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                val original = BitmapFactory.decodeFile(imagePath, options)
                    ?: throw PreprocessingError.ProcessingFailed("BitmapFactory returned null")
                log("1. Loaded ${original.width}×${original.height} in ${elapsed(t0)}ms")

                // ── 2. Quality analysis on a downscaled copy ───────────────────
                //    Run BEFORE grayscale/contrast so scores reflect the true
                //    luminance and sharpness of the captured image.
                val analysisScale = if (original.width > ANALYSIS_MAX_WIDTH)
                    ANALYSIS_MAX_WIDTH.toFloat() / original.width else 1f
                val analysisW = (original.width  * analysisScale).toInt().coerceAtLeast(1)
                val analysisH = (original.height * analysisScale).toInt().coerceAtLeast(1)
                val small = Bitmap.createScaledBitmap(original, analysisW, analysisH, true)
                val analysisPixels = IntArray(analysisW * analysisH)
                small.getPixels(analysisPixels, 0, analysisW, 0, 0, analysisW, analysisH)
                small.recycle()

                val qualityReport = ImageQualityAnalyzer.analyse(analysisPixels, analysisW, analysisH)
                log("2. Quality: blur=${qualityReport.blurScore} brightness=${qualityReport.brightnessScore} " +
                    "edge=${qualityReport.edgeScore} coverage=${qualityReport.documentCoverage} " +
                    "issues=${qualityReport.issues} in ${elapsed(t0)}ms")

                // ── 3. Grayscale (skipped for ORIGINAL filter) ─────────────────
                val afterGray: Bitmap = if (filter == InvoiceFilter.ORIGINAL) {
                    log("3. Grayscale skipped (ORIGINAL filter)")
                    original
                } else {
                    val gray = toGrayscale(original)
                    original.recycle()
                    log("3. Grayscale in ${elapsed(t0)}ms")
                    gray
                }

                // ── 4. Contrast enhancement ────────────────────────────────────
                val contrastFactor = when (filter) {
                    InvoiceFilter.ORIGINAL      -> 1.0f   // no change
                    InvoiceFilter.GRAYSCALE     -> 1.35f
                    InvoiceFilter.HIGH_CONTRAST -> 1.6f
                }
                val contrasted = if (contrastFactor == 1.0f) {
                    log("4. Contrast skipped (ORIGINAL filter)")
                    afterGray
                } else {
                    val c = enhanceContrast(afterGray, factor = contrastFactor)
                    afterGray.recycle()
                    log("4. Contrast (${contrastFactor}×) in ${elapsed(t0)}ms")
                    c
                }

                // ── 5. Scan copy: resize ≤ SCAN_MAX_WIDTH, compress at high quality ─
                //    This is what GPT-4o reads — keep it colour & sharp. GPT downscales
                //    the short edge to 768px internally, so SCAN_MAX_WIDTH only needs
                //    enough margin to land there cleanly; bigger just wastes RAM/upload.
                val scanBitmap = resizeToMaxWidth(contrasted, SCAN_MAX_WIDTH)
                if (scanBitmap !== contrasted) contrasted.recycle()
                val scanBytes = compressToTarget(
                    scanBitmap,
                    targetMaxBytes = SCAN_TARGET_MAX_BYTES,
                    startQuality = SCAN_START_QUALITY,
                    minQuality = SCAN_MIN_QUALITY,
                )
                val finalWidth  = scanBitmap.width
                val finalHeight = scanBitmap.height
                log("5. Scan copy → ${finalWidth}×${finalHeight} ${scanBytes.size / 1024}KB in ${elapsed(t0)}ms")

                // ── 6. Storage copy: derive from the (already shrunk) scan bitmap ──
                //    Grayscale + smaller + lower quality. This is only the persisted
                //    audit artifact, so colour cues don't matter and we squeeze hard
                //    to save server disk. Derived from scanBitmap (NOT the source) so
                //    we never hold two full-resolution bitmaps in memory.
                val grayForStorage = toGrayscale(scanBitmap)
                scanBitmap.recycle()
                val storageBitmap = resizeToMaxWidth(grayForStorage, STORAGE_MAX_WIDTH)
                if (storageBitmap !== grayForStorage) grayForStorage.recycle()
                val storageBytes = compressToTarget(
                    storageBitmap,
                    targetMaxBytes = STORAGE_TARGET_MAX_BYTES,
                    startQuality = STORAGE_START_QUALITY,
                    minQuality = STORAGE_MIN_QUALITY,
                )
                storageBitmap.recycle()
                log("6. Storage copy → ${storageBytes.size / 1024}KB in ${elapsed(t0)}ms | issues=${qualityReport.issues}")

                ProcessedInvoiceImage(
                    imageData        = scanBytes,
                    storageImageData = storageBytes,
                    width            = finalWidth,
                    height           = finalHeight,
                    fileSize         = scanBytes.size,
                    storageFileSize  = storageBytes.size,
                    blurScore        = qualityReport.blurScore,
                    brightnessScore  = qualityReport.brightnessScore,
                    edgeScore        = qualityReport.edgeScore,
                    documentCoverage = qualityReport.documentCoverage,
                    qualityIssues    = qualityReport.issues
                )
            }
        }

    // ── Post-Processing ───────────────────────────────────────────────────────

    private fun toGrayscale(src: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }.also { Canvas(result).drawBitmap(src, 0f, 0f, it) }
        return result
    }

    private fun enhanceContrast(src: Bitmap, factor: Float): Bitmap {
        val shift = 128f * (1f - factor)
        val cm = ColorMatrix(floatArrayOf(
            factor, 0f, 0f, 0f, shift,
            0f, factor, 0f, 0f, shift,
            0f, 0f, factor, 0f, shift,
            0f, 0f, 0f,     1f, 0f
        ))
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(src, 0f, 0f, Paint().apply {
            colorFilter = ColorMatrixColorFilter(cm)
        })
        return result
    }

    private fun resizeToMaxWidth(src: Bitmap, maxWidth: Int): Bitmap {
        if (src.width <= maxWidth) return src
        val scale = maxWidth.toFloat() / src.width
        return Bitmap.createScaledBitmap(src, maxWidth, (src.height * scale).toInt(), true)
    }

    /**
     * Compresses [bitmap] to JPEG, starting at [startQuality] and stepping down by 5
     * only while the result exceeds [targetMaxBytes], never dropping below [minQuality].
     * The [minQuality] floor protects small text from JPEG block artifacts.
     */
    private fun compressToTarget(
        bitmap: Bitmap,
        targetMaxBytes: Int,
        startQuality: Int,
        minQuality: Int,
    ): ByteArray {
        val out = ByteArrayOutputStream()
        var quality = startQuality
        do {
            out.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            quality -= 5
        } while (out.size() > targetMaxBytes && quality >= minQuality)
        return out.toByteArray()
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private fun elapsed(t0: Long) = System.currentTimeMillis() - t0

    private fun log(msg: String) = println("[InvoicePreprocessor/Android] $msg")

    companion object {
        private const val ANALYSIS_MAX_WIDTH = 600

        // Scan copy (→ GPT-4o). High quality; GPT caps the short edge at 768px so
        // ~1500px gives clean detail with margin without wasting RAM/upload.
        private const val SCAN_MAX_WIDTH       = 1500
        private const val SCAN_START_QUALITY   = 82
        private const val SCAN_MIN_QUALITY     = 70
        private const val SCAN_TARGET_MAX_BYTES = 1_200_000   // soft guard; Q82@1500px usually lands well under

        // Storage copy (→ persisted archive). Grayscale + small to save server disk.
        private const val STORAGE_MAX_WIDTH       = 1100
        private const val STORAGE_START_QUALITY   = 65
        private const val STORAGE_MIN_QUALITY     = 35
        private const val STORAGE_TARGET_MAX_BYTES = 200_000
    }
}

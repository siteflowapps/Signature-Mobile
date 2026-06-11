package com.siteflow.cdo.core.domain

import com.siteflow.cdo.core.domain.model.ProcessedInvoiceImage
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreGraphics.*
import platform.CoreImage.*
import platform.Foundation.*
import platform.UIKit.*
import platform.Vision.*
import platform.posix.memcpy
import kotlin.coroutines.resume

/**
 * iOS implementation of the invoice preprocessing pipeline.
 *
 * Pipeline (runs on [Dispatchers.Default]):
 *  1. Load image from [imagePath]
 *  2. Detect document rectangle via VNDetectRectanglesRequest (Vision framework)
 *  3. Perspective correction via CIPerspectiveCorrection (if rectangle found)
 *  4. Grayscale + contrast via CIColorControls
 *  5. Render to UIImage via CIContext
 *  6. Resize to max 1200px width
 *  7. Quality analysis on the resized CGImage (blur, brightness, edges, coverage)
 *  8. JPEG compress to target ≤ 250 KB
 *
 * Quality analysis runs on the resized grayscale image. The analysis pixels are
 * small (≤ 1200px wide) so extraction is fast. Shared analysis logic lives in
 * [ImageQualityAnalyzer] — identical to the Android implementation.
 */
@OptIn(ExperimentalForeignApi::class)
actual class InvoicePreprocessor {

    actual suspend fun preprocessForUpload(
        imagePath: String,
        filter: InvoiceFilter
    ): Result<ProcessedInvoiceImage> =
        withContext(Dispatchers.Default) {
            runCatching {
                val t0 = NSDate.timeIntervalSinceReferenceDate()

                // ── 1. Load image ──────────────────────────────────────────────
                val uiImage = UIImage.imageWithContentsOfFile(imagePath)
                    ?: throw PreprocessingError.ImageNull

                val cgImage = uiImage.CGImage
                    ?: throw PreprocessingError.ImageNull

                val ciImage = CIImage.imageWithCGImage(cgImage)

                val sizeStr = uiImage.size.useContents { "${width.toInt()}x${height.toInt()}" }
                log("1. Loaded $sizeStr in ${elapsed(t0)}ms")

                // ── 2. Detect document rectangle (Vision) ──────────────────────
                val rectObservation = suspendCancellableCoroutine<VNRectangleObservation?> { cont ->
                    val request = VNDetectRectanglesRequest { req, _ ->
                        val obs = req?.results?.firstOrNull() as? VNRectangleObservation
                        cont.resume(obs)
                    }.apply {
                        // High confidence threshold — only correct when Vision is sure.
                        // Low confidence = shaky, wrong-region results.
                        minimumConfidence = 0.85f
                        // Accept portrait (tall) and landscape (wide) invoices.
                        minimumAspectRatio = 0.2f
                        maximumAspectRatio = 1.0f
                        // Document must cover at least 40% of the frame's shorter dimension.
                        minimumSize = 0.4f
                        maximumObservations = 1u
                    }
                    val handler = VNImageRequestHandler(cgImage, options = mapOf<Any?, Any?>())
                    try {
                        handler.performRequests(listOf(request), error = null)
                    } catch (e: Exception) {
                        cont.resume(null)
                    }
                }
                log("2. Rectangle detection in ${elapsed(t0)}ms → found=${rectObservation != null}")

                // ── 3. Perspective correction via CIPerspectiveCorrection ──────
                // NOTE: pass ciImage so the function derives pixel dimensions from
                // the CIImage extent — NOT from uiImage.size (which is in points and
                // would give wrong coordinates on retina devices, causing tilt).
                val correctedCI: CIImage = if (rectObservation != null) {
                    applyCIPerspectiveCorrection(ciImage, rectObservation)
                } else {
                    ciImage
                }
                log("3. Perspective correction in ${elapsed(t0)}ms")

                // ── 4. Grayscale + Contrast via CIColorControls ────────────────
                val saturation = if (filter == InvoiceFilter.ORIGINAL) 1.0 else 0.0
                val contrast   = when (filter) {
                    InvoiceFilter.ORIGINAL      -> 1.0
                    InvoiceFilter.GRAYSCALE     -> 1.35
                    InvoiceFilter.HIGH_CONTRAST -> 1.6
                }
                val brightness = if (filter == InvoiceFilter.ORIGINAL) 0.0 else 0.05

                val colorFilter = CIFilter.filterWithName("CIColorControls")!!
                colorFilter.setValue(correctedCI,  forKey = "inputImage")
                colorFilter.setValue(saturation,   forKey = "inputSaturation")
                colorFilter.setValue(contrast,     forKey = "inputContrast")
                colorFilter.setValue(brightness,   forKey = "inputBrightness")
                val enhancedCI = colorFilter.outputImage
                    ?: throw PreprocessingError.ProcessingFailed("CIColorControls returned nil")
                log("4. filter=$filter sat=$saturation contrast=$contrast in ${elapsed(t0)}ms")

                // ── 5. Render to UIImage via CIContext ─────────────────────────
                val ciContext = CIContext.contextWithOptions(null)
                val renderedCG = ciContext.createCGImage(enhancedCI, fromRect = enhancedCI.extent)
                    ?: throw PreprocessingError.ProcessingFailed("CIContext render failed")
                val renderedImage = UIImage.imageWithCGImage(renderedCG)
                log("5. CI render in ${elapsed(t0)}ms")

                // ── 6. Scan copy: resize ≤ SCAN_MAX_WIDTH ──────────────────────
                //    What GPT-4o reads — kept colour & sharp. GPT downscales the
                //    short edge to 768px internally, so SCAN_MAX_WIDTH only needs
                //    enough margin to land there cleanly.
                val scanImage = resizeToMaxWidth(renderedImage, maxWidth = SCAN_MAX_WIDTH)
                log("6. Scan resize in ${elapsed(t0)}ms")

                // ── 7. Quality analysis on the scan CGImage ────────────────────
                //    Using the resized (small) image keeps pixel extraction fast.
                //    Shared logic in [ImageQualityAnalyzer] — same as Android.
                val scanCG = scanImage.CGImage
                    ?: throw PreprocessingError.ProcessingFailed("resized image has no CGImage")
                val finalW = CGImageGetWidth(scanCG).toInt()
                val finalH = CGImageGetHeight(scanCG).toInt()
                val pixels = extractPixelsFromCGImage(scanCG, finalW, finalH)
                val qualityReport = ImageQualityAnalyzer.analyse(pixels, finalW, finalH)
                log("7. Quality: blur=${qualityReport.blurScore} brightness=${qualityReport.brightnessScore} " +
                    "edge=${qualityReport.edgeScore} coverage=${qualityReport.documentCoverage} " +
                    "issues=${qualityReport.issues} in ${elapsed(t0)}ms")

                // ── 8. Scan JPEG compress (colour, high quality) ───────────────
                val scanBytes = compressToTarget(
                    scanImage,
                    targetMaxBytes = SCAN_TARGET_MAX_BYTES,
                    startQuality = SCAN_START_QUALITY,
                    minQuality = SCAN_MIN_QUALITY,
                )
                log("8. Scan JPEG ${scanBytes.size / 1024}KB in ${elapsed(t0)}ms")

                // ── 9. Storage copy: grayscale + smaller, for the persisted archive ─
                //    Derived from the scan image (not the source). Colour cues aren't
                //    needed for an audit copy, so we squeeze hard to save server disk.
                val grayForStorage = toGrayscale(scanImage)
                val storageImage = resizeToMaxWidth(grayForStorage, maxWidth = STORAGE_MAX_WIDTH)
                val storageBytes = compressToTarget(
                    storageImage,
                    targetMaxBytes = STORAGE_TARGET_MAX_BYTES,
                    startQuality = STORAGE_START_QUALITY,
                    minQuality = STORAGE_MIN_QUALITY,
                )
                log("9. Storage JPEG ${storageBytes.size / 1024}KB in ${elapsed(t0)}ms | issues=${qualityReport.issues}")

                ProcessedInvoiceImage(
                    imageData        = scanBytes,
                    storageImageData = storageBytes,
                    width            = finalW,
                    height           = finalH,
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

    // ── Perspective Correction ────────────────────────────────────────────────

    /**
     * Applies perspective correction using Vision's detected rectangle corners.
     *
     * Coordinate systems:
     * - Vision normalized: (0,0) = bottom-left, (1,1) = top-right of the RAW CGImage.
     * - Core Image pixel: (0,0) = bottom-left, extent = raw pixel dimensions.
     *
     * We derive pixel dimensions from [image].extent (NOT from UIImage.size which is
     * in points and would give wrong coordinates on retina devices, causing tilt).
     */
    private fun applyCIPerspectiveCorrection(
        image: CIImage,
        obs: VNRectangleObservation
    ): CIImage {
        // Use the CIImage pixel extent — matches what VNImageRequestHandler processed
        val imgW = image.extent.useContents { size.width }
        val imgH = image.extent.useContents { size.height }

        // Vision (0,0) = bottom-left, same as Core Image → direct multiply is correct
        fun vnToCI(pt: CValue<CGPoint>): CIVector =
            pt.useContents { CIVector.vectorWithX(x * imgW, Y = y * imgH) }

        val filter = CIFilter.filterWithName("CIPerspectiveCorrection")!!
        filter.setValue(image,               forKey = "inputImage")
        filter.setValue(vnToCI(obs.topLeft),     forKey = "inputTopLeft")
        filter.setValue(vnToCI(obs.topRight),    forKey = "inputTopRight")
        filter.setValue(vnToCI(obs.bottomRight), forKey = "inputBottomRight")
        filter.setValue(vnToCI(obs.bottomLeft),  forKey = "inputBottomLeft")
        return filter.outputImage ?: image
    }

    // ── Resize ────────────────────────────────────────────────────────────────

    /**
     * Resizes [image] so its pixel width does not exceed [maxWidth].
     *
     * UIImage.size is in **points**. On a 3× retina device a 4032-pixel-wide
     * photo has size.width = 1344 pt. UIGraphicsImageRenderer also works in
     * points, so "resize to 1200" would produce a 3600-pixel image on 3×.
     *
     * Fix: multiply size by image.scale to get pixel dimensions, then render
     * with format.scale = 1.0 so the renderer treats its size as pixels.
     */
    private fun resizeToMaxWidth(image: UIImage, maxWidth: Int): UIImage {
        val pixelW = image.size.useContents { width }  * image.scale
        val pixelH = image.size.useContents { height } * image.scale
        if (pixelW <= maxWidth.toDouble()) return image
        val ratio    = maxWidth.toDouble() / pixelW
        val newPixelW = maxWidth.toDouble()
        val newPixelH = pixelH * ratio
        // scale = 1.0 → renderer size == pixel size (not points)
        val format = UIGraphicsImageRendererFormat()
        format.scale = 1.0
        return UIGraphicsImageRenderer(CGSizeMake(newPixelW, newPixelH), format)
            .imageWithActions { _ ->
                image.drawInRect(CGRectMake(0.0, 0.0, newPixelW, newPixelH))
            }
    }

    // ── Pixel Extraction ──────────────────────────────────────────────────────

    /** Renders a CGImage into a CGBitmapContext and returns raw ARGB pixel data. */
    private fun extractPixelsFromCGImage(cgImage: CGImageRef, width: Int, height: Int): IntArray {
        val pixels = IntArray(width * height)
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val ctx = CGBitmapContextCreate(
            null, width.toULong(), height.toULong(),
            8u, (width * 4).toULong(),
            colorSpace,
            CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst.value
        ) ?: return pixels

        CGContextDrawImage(ctx, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), cgImage)

        val byteCount = width * height * 4
        val bytes = ByteArray(byteCount)
        val data = CGBitmapContextGetData(ctx)
        if (data != null) {
            bytes.usePinned { pinned -> memcpy(pinned.addressOf(0), data, byteCount.toULong()) }
            for (i in pixels.indices) {
                val offset = i * 4
                val a = bytes[offset].toInt()     and 0xFF
                val r = bytes[offset + 1].toInt() and 0xFF
                val g = bytes[offset + 2].toInt() and 0xFF
                val b = bytes[offset + 3].toInt() and 0xFF
                pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        CGContextRelease(ctx)
        return pixels
    }

    // ── JPEG Compression ──────────────────────────────────────────────────────

    /**
     * Compresses [image] to JPEG, starting at [startQuality] and stepping down by
     * 0.05 only while the result exceeds [targetMaxBytes], never below [minQuality].
     * The [minQuality] floor protects small text from JPEG block artifacts.
     * Throws [PreprocessingError.ProcessingFailed] if compression produces no data.
     */
    private fun compressToTarget(
        image: UIImage,
        targetMaxBytes: Int,
        startQuality: Double,
        minQuality: Double,
    ): ByteArray {
        var quality = startQuality
        var data: NSData? = null
        while (quality >= minQuality) {
            val candidate = UIImageJPEGRepresentation(image, quality)
            if (candidate != null) {
                data = candidate
                if (candidate.length.toLong() <= targetMaxBytes.toLong()) break
            }
            quality -= 0.05
        }
        return data?.toByteArray()
            ?: throw PreprocessingError.ProcessingFailed("JPEG compression returned no data")
    }

    /**
     * Desaturates [image] to grayscale via CIColorControls. Used only for the
     * persisted storage copy — never for the scan copy GPT-4o reads.
     */
    private fun toGrayscale(image: UIImage): UIImage {
        val cg = image.CGImage ?: return image
        val ci = CIImage.imageWithCGImage(cg)
        val filter = CIFilter.filterWithName("CIColorControls") ?: return image
        filter.setValue(ci, forKey = "inputImage")
        filter.setValue(0.0, forKey = "inputSaturation")
        val out = filter.outputImage ?: return image
        val ctx = CIContext.contextWithOptions(null)
        val outCG = ctx.createCGImage(out, fromRect = out.extent) ?: return image
        return UIImage.imageWithCGImage(outCG)
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private fun elapsed(t0: Double): Int =
        ((NSDate.timeIntervalSinceReferenceDate() - t0) * 1000).toInt()

    private fun log(msg: String) = println("[InvoicePreprocessor/iOS] $msg")

    private fun NSData.toByteArray(): ByteArray =
        ByteArray(length.toInt()).apply {
            usePinned { memcpy(it.addressOf(0), bytes, length) }
        }

    companion object {
        // Scan copy (→ GPT-4o). High quality; GPT caps the short edge at 768px so
        // ~1500px gives clean detail with margin without wasting RAM/upload.
        private const val SCAN_MAX_WIDTH        = 1500
        private const val SCAN_START_QUALITY    = 0.82
        private const val SCAN_MIN_QUALITY      = 0.70
        private const val SCAN_TARGET_MAX_BYTES = 1_200_000   // soft guard; Q0.82@1500px usually lands well under

        // Storage copy (→ persisted archive). Grayscale + small to save server disk.
        private const val STORAGE_MAX_WIDTH        = 1100
        private const val STORAGE_START_QUALITY    = 0.65
        private const val STORAGE_MIN_QUALITY      = 0.35
        private const val STORAGE_TARGET_MAX_BYTES = 200_000
    }
}

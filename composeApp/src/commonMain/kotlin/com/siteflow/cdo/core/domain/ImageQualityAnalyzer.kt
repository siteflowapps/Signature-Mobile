package com.siteflow.cdo.core.domain

import com.siteflow.cdo.core.domain.model.ImageQualityIssue
import kotlin.math.sqrt

/**
 * Platform-independent image quality analysis.
 *
 * Operates on raw ARGB pixel data (IntArray) — compatible with both
 * Android [android.graphics.Bitmap.getPixels] and iOS CGBitmapContext extraction.
 *
 * Used by both [InvoicePreprocessor] actuals to eliminate code duplication.
 */
internal object ImageQualityAnalyzer {

    private const val BLUR_THRESHOLD      = 100f
    private const val DARK_THRESHOLD      = 40f
    private const val BRIGHT_THRESHOLD    = 220f
    private const val EDGE_THRESHOLD      = 0.05f
    private const val COVERAGE_THRESHOLD  = 0.50f
    const val EDGE_GRADIENT_THRESH        = 30.0

    data class QualityReport(
        val blurScore: Float,
        val brightnessScore: Float,
        val edgeScore: Float,
        val documentCoverage: Float,
        val issues: List<ImageQualityIssue>
    )

    /**
     * Runs all quality checks on [pixels] and returns a [QualityReport].
     *
     * Prefer calling this on the original or lightly-downscaled image
     * *before* contrast enhancement so that brightnessScore reflects
     * the true scene luminance and edgeScore is not inflated.
     */
    fun analyse(pixels: IntArray, width: Int, height: Int): QualityReport {
        val blur       = computeBlurScore(pixels, width, height)
        val brightness = computeBrightness(pixels)
        val edge       = computeEdgeScore(pixels, width, height)
        val coverage   = computeDocumentCoverage(pixels, width, height)

        val issues = buildList {
            if (blur < BLUR_THRESHOLD)          add(ImageQualityIssue.TooBlurry)
            if (brightness < DARK_THRESHOLD)    add(ImageQualityIssue.TooDark)
            if (brightness > BRIGHT_THRESHOLD)  add(ImageQualityIssue.TooBright)
            if (edge < EDGE_THRESHOLD)          add(ImageQualityIssue.NoDocumentEdges)
            if (coverage < COVERAGE_THRESHOLD)  add(ImageQualityIssue.DocumentTooSmall)
        }

        return QualityReport(blur, brightness, edge, coverage, issues)
    }

    /** Laplacian variance — higher = sharper. */
    fun computeBlurScore(pixels: IntArray, width: Int, height: Int): Float {
        var sum = 0.0; var sumSq = 0.0; var count = 0
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val c = lum(pixels[y * width + x])
                val l = lum(pixels[y * width + x - 1])
                val r = lum(pixels[y * width + x + 1])
                val t = lum(pixels[(y - 1) * width + x])
                val b = lum(pixels[(y + 1) * width + x])
                val lap = (4 * c - l - r - t - b).toDouble()
                sum += lap; sumSq += lap * lap; count++
            }
        }
        if (count == 0) return 0f
        val mean = sum / count
        return ((sumSq / count) - (mean * mean)).toFloat().coerceAtLeast(0f)
    }

    /** Average perceptual luminance (0–255). */
    fun computeBrightness(pixels: IntArray): Float {
        var total = 0L
        for (p in pixels) total += lum(p)
        return total.toFloat() / pixels.size
    }

    /** Fraction of pixels with Sobel gradient magnitude above [EDGE_GRADIENT_THRESH]. */
    fun computeEdgeScore(pixels: IntArray, width: Int, height: Int): Float {
        var edgeCount = 0
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val gx = lum(pixels[y * width + x + 1]) - lum(pixels[y * width + x - 1])
                val gy = lum(pixels[(y + 1) * width + x]) - lum(pixels[(y - 1) * width + x])
                if (sqrt((gx * gx + gy * gy).toDouble()) > EDGE_GRADIENT_THRESH) edgeCount++
            }
        }
        val total = (width - 2) * (height - 2)
        return if (total > 0) edgeCount.toFloat() / total else 0f
    }

    /** Document area fraction estimated from bounding box of edge pixels. */
    fun computeDocumentCoverage(pixels: IntArray, width: Int, height: Int): Float {
        var minX = width; var maxX = 0; var minY = height; var maxY = 0
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val gx = lum(pixels[y * width + x + 1]) - lum(pixels[y * width + x - 1])
                val gy = lum(pixels[(y + 1) * width + x]) - lum(pixels[(y - 1) * width + x])
                if (sqrt((gx * gx + gy * gy).toDouble()) > EDGE_GRADIENT_THRESH) {
                    if (x < minX) minX = x; if (x > maxX) maxX = x
                    if (y < minY) minY = y; if (y > maxY) maxY = y
                }
            }
        }
        if (maxX <= minX || maxY <= minY) return 0f
        val boxArea = (maxX - minX).toLong() * (maxY - minY)
        return (boxArea.toFloat() / (width.toLong() * height)).coerceIn(0f, 1f)
    }

    /** ITU-R BT.601 perceptual luminance from ARGB int. */
    fun lum(pixel: Int): Int {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8)  and 0xFF
        val b = pixel          and 0xFF
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }
}

package com.siteflow.cdo.core.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.siteflow.cdo.outlet.invoices.domain.InvoiceFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

/**
 * Android actual for [PdfCompressor].
 *
 * Pipeline (runs on [Dispatchers.Default]):
 *  1. Size / page-count gates (fail fast).
 *  2. Try [PdfRenderer] — encrypted PDFs throw `SecurityException`, corrupt ones `IOException`.
 *  3. Scan copy = the untouched original bytes (passthrough). The backend rasterizes
 *     PDFs to 200-DPI PNG itself, so the pristine original gives GPT-4o the best render.
 *  4. Storage copy = walk [PdfCompressorConstants.ATTEMPTS] and rasterize to the first
 *     attempt within `targetBytesFor(pageCount)` (or the smallest attempt if none fit),
 *     purely to save server disk. The upload never fails on compression.
 */
actual class PdfCompressor(private val context: Context) {

    actual suspend fun compress(bytes: ByteArray): PdfCompressionResult =
        withContext(Dispatchers.Default) {
            val originalKb = bytes.size / 1024

            if (bytes.size > PdfCompressorConstants.MAX_INPUT_BYTES) {
                log("Rejected — input ${originalKb}KB > ${PdfCompressorConstants.MAX_INPUT_BYTES / 1024}KB")
                return@withContext PdfCompressionResult.Failure.FileTooLarge
            }

            val tempFile = writeTemp(bytes)
            var renderer: PdfRenderer? = null
            var pfd: ParcelFileDescriptor? = null
            try {
                pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = try {
                    PdfRenderer(pfd)
                } catch (_: SecurityException) {
                    return@withContext PdfCompressionResult.Failure.Encrypted
                } catch (_: IOException) {
                    return@withContext PdfCompressionResult.Failure.Corrupt
                }

                val pageCount = renderer.pageCount
                if (pageCount <= 0) return@withContext PdfCompressionResult.Failure.Corrupt
                if (pageCount > PdfCompressorConstants.MAX_PAGES) {
                    log("Rejected — $pageCount pages > ${PdfCompressorConstants.MAX_PAGES}")
                    return@withContext PdfCompressionResult.Failure.TooManyPages(pageCount)
                }

                val thumbnail = renderFirstPageThumbnail(renderer)
                val budget = PdfCompressorConstants.targetBytesFor(pageCount)
                log("Storage budget for $pageCount-page PDF: ${budget / 1024}KB")

                // Storage copy: walk the ladder and take the first attempt within budget;
                // if none fit, keep the smallest attempt. Never reject — the scan copy is
                // the untouched original, so the upload always succeeds.
                var bestStorage: ByteArray? = null
                for (attempt in PdfCompressorConstants.ATTEMPTS) {
                    val rebuilt = rebuildPdf(renderer, dpi = attempt.dpi, jpegQuality = attempt.quality)
                    log("Attempt dpi=${attempt.dpi} q=${attempt.quality} → ${rebuilt.size / 1024}KB")
                    if (bestStorage == null || rebuilt.size < bestStorage!!.size) bestStorage = rebuilt
                    if (rebuilt.size <= budget) break
                }

                // If compression couldn't beat the original (already-lean digital PDFs),
                // just store the original too.
                val storageBytes = bestStorage
                    ?.takeIf { it.size < bytes.size }
                    ?: bytes
                log("Scan copy: original ${originalKb}KB (passthrough) | storage copy: ${storageBytes.size / 1024}KB")

                PdfCompressionResult.Success(
                    scanFile = InvoiceFile.pdf(bytes = bytes, pageCount = pageCount),
                    storageFile = InvoiceFile.pdf(bytes = storageBytes, pageCount = pageCount),
                    originalKb = originalKb,
                    finalKb = storageBytes.size / 1024,
                    pageCount = pageCount,
                    firstPageThumbnail = thumbnail,
                )
            } finally {
                runCatching { renderer?.close() }
                runCatching { pfd?.close() }
                runCatching { tempFile.delete() }
            }
        }

    private fun writeTemp(bytes: ByteArray): File {
        val dir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        return File.createTempFile("pdf_in_", ".pdf", dir).apply { writeBytes(bytes) }
    }

    private fun renderFirstPageThumbnail(renderer: PdfRenderer): ByteArray {
        val page = renderer.openPage(0)
        try {
            // Aim for ~200px wide thumbnail; PdfRenderer pages are sized in PDF points (72 dpi).
            val scale = 200f / page.width
            val w = (page.width * scale).toInt().coerceAtLeast(1)
            val h = (page.height * scale).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
            bitmap.recycle()
            return out.toByteArray()
        } finally {
            page.close()
        }
    }

    /**
     * Rasterizes every page of [renderer] at [dpi], JPEG-encodes at [jpegQuality],
     * then writes a single-image-per-page PDF and returns its bytes.
     *
     * The [PdfDocument] page is sized to match the rasterized pixel dimensions so
     * the JPEG is drawn 1:1 — this is what gives us a predictable size.
     */
    private fun rebuildPdf(renderer: PdfRenderer, dpi: Int, jpegQuality: Int): ByteArray {
        val document = PdfDocument()
        try {
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                try {
                    val scale = dpi / 72f
                    val pixelW = (page.width * scale).toInt().coerceAtLeast(1)
                    val pixelH = (page.height * scale).toInt().coerceAtLeast(1)

                    val bitmap = Bitmap.createBitmap(pixelW, pixelH, Bitmap.Config.ARGB_8888).apply {
                        eraseColor(Color.WHITE)
                    }
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                    val jpeg = ByteArrayOutputStream().use { os ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, os)
                        os.toByteArray()
                    }
                    bitmap.recycle()

                    val pageInfo = PdfDocument.PageInfo.Builder(pixelW, pixelH, i + 1).create()
                    val outPage = document.startPage(pageInfo)
                    val decoded = android.graphics.BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)
                    outPage.canvas.drawBitmap(decoded, 0f, 0f, null)
                    decoded.recycle()
                    document.finishPage(outPage)
                } finally {
                    page.close()
                }
            }
            val out = ByteArrayOutputStream()
            document.writeTo(out)
            return out.toByteArray()
        } finally {
            document.close()
        }
    }

    private fun log(msg: String) = println("[PdfCompressor/Android] $msg")
}

package com.siteflow.signature.core.domain

import com.siteflow.signature.outlet.invoices.domain.InvoiceFile

/**
 * Compresses an uploaded PDF down to a server-friendly size by rasterizing each
 * page to JPEG (at a controlled DPI/quality) and rebuilding a new PDF from the
 * rasterized pages. Works on both text and scanned PDFs.
 *
 * Android actual: `android.graphics.pdf.PdfRenderer` (read) + `PdfDocument` (write).
 * iOS actual:     `PDFKit.PDFDocument` + `UIGraphicsBeginPDFContextToData`.
 *
 * The picked PDF is validated against [PdfCompressorConstants] before compression:
 * oversized inputs / encrypted / corrupt PDFs short-circuit with a typed failure.
 */
expect class PdfCompressor {
    suspend fun compress(bytes: ByteArray): PdfCompressionResult
}

sealed interface PdfCompressionResult {
    data class Success(
        /**
         * The ORIGINAL picked PDF, untouched — sent to POST /ocr/extract-invoice.
         * The backend rasterizes PDFs to 200-DPI PNG itself before GPT-4o, so handing
         * it the pristine source (not a pre-rasterized one) gives the cleanest render.
         */
        val scanFile: InvoiceFile,
        /**
         * Compressed (rasterized) PDF — persisted via POST /invoices as the archive
         * copy, to keep server disk usage low.
         */
        val storageFile: InvoiceFile,
        val originalKb: Int,
        /** Size in KB of the [storageFile]. */
        val finalKb: Int,
        val pageCount: Int,
        val firstPageThumbnail: ByteArray,
    ) : PdfCompressionResult

    sealed interface Failure : PdfCompressionResult {
        data object FileTooLarge : Failure
        data class TooManyPages(val actual: Int) : Failure
        data object Encrypted : Failure
        data object Corrupt : Failure
    }
}

/**
 * A single compression attempt: render every page at [dpi] and JPEG-encode at [quality].
 * Ladder is ordered "good → readability-floor". The compressor stops at the first attempt
 * that fits the per-file budget; if none do, it keeps the smallest attempt for the storage
 * copy. The scan copy is always the untouched original, so we never block the upload.
 */
data class PdfCompressionAttempt(val dpi: Int, val quality: Int)

object PdfCompressorConstants {
    const val MAX_INPUT_BYTES = 2 * 1024 * 1024          // 2 MB input cap — covers ~85-90% of real invoice PDFs (Tally / phone scans / B&W MFP / light colour MFP)
    const val MAX_PAGES = 3                              // retailer invoices ≤ 3 pages
    const val PER_PAGE_TARGET_BYTES = 350_000            // aim for ~350 KB/page
    const val HARD_MAX_BYTES = 1_000_000                 // 1 MB total — never ship more

    /**
     * Ladder of (DPI, JPEG quality) attempts, from "good readable" to "readability floor".
     * Used only for the persisted storage copy. The scan copy GPT-4o reads is the
     * untouched original, so storage compression is purely a disk-saving optimisation.
     */
    val ATTEMPTS = listOf(
        PdfCompressionAttempt(dpi = 150, quality = 75),  // good default
        PdfCompressionAttempt(dpi = 150, quality = 65),
        PdfCompressionAttempt(dpi = 130, quality = 70),
        PdfCompressionAttempt(dpi = 130, quality = 60),
        PdfCompressionAttempt(dpi = 120, quality = 55),  // readability floor
    )

    /** Per-file target bytes = min(pages × 350 KB, 1 MB hard cap). */
    fun targetBytesFor(pageCount: Int): Int =
        minOf(pageCount * PER_PAGE_TARGET_BYTES, HARD_MAX_BYTES)
}

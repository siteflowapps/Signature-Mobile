package com.siteflow.cdo.outlet.invoices.domain

/**
 * Final binary payload uploaded to the invoice endpoints.
 *
 * Abstracts away whether the original source was a captured image or a PDF —
 * the network layer reads [mimeType] and [filename] off this object so the
 * same upload path handles both.
 */
data class InvoiceFile(
    val bytes: ByteArray,
    val mimeType: String,
    val filename: String,
    val pageCount: Int = 1,
) {
    val sizeKb: Int get() = bytes.size / 1024

    val isPdf: Boolean get() = mimeType == MIME_PDF

    companion object {
        const val MIME_JPEG = "image/jpeg"
        const val MIME_PDF = "application/pdf"

        fun jpeg(bytes: ByteArray, filename: String = "invoice.jpg") =
            InvoiceFile(bytes = bytes, mimeType = MIME_JPEG, filename = filename, pageCount = 1)

        fun pdf(bytes: ByteArray, pageCount: Int, filename: String = "invoice.pdf") =
            InvoiceFile(bytes = bytes, mimeType = MIME_PDF, filename = filename, pageCount = pageCount)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InvoiceFile) return false
        return mimeType == other.mimeType &&
            filename == other.filename &&
            pageCount == other.pageCount &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + pageCount
        return result
    }
}

/**
 * UI-side snapshot of a successfully-compressed PDF — drives the PDF preview
 * screen (thumbnail + page count + size readout).
 */
data class PdfPreviewData(
    val filename: String,
    val pageCount: Int,
    val originalKb: Int,
    val finalKb: Int,
    val thumbnailJpeg: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PdfPreviewData) return false
        return filename == other.filename &&
            pageCount == other.pageCount &&
            originalKb == other.originalKb &&
            finalKb == other.finalKb &&
            thumbnailJpeg.contentEquals(other.thumbnailJpeg)
    }

    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + pageCount
        result = 31 * result + originalKb
        result = 31 * result + finalKb
        result = 31 * result + thumbnailJpeg.contentHashCode()
        return result
    }
}

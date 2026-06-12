package com.siteflow.signature.core.domain.model

/**
 * Output of the on-device invoice preprocessing pipeline.
 *
 * Contains the compressed, enhanced JPEG bytes ready for upload,
 * plus quality scores computed during processing.
 *
 * Pipeline:
 *   Auto-detect edges → Crop → Perspective correction →
 *   Grayscale → Contrast enhance → Resize (≤1200px) → JPEG compress (~200KB)
 */
data class ProcessedInvoiceImage(
    /**
     * Scan-quality JPEG bytes — COLOR, higher resolution/quality, sent to
     * POST /ocr/extract-invoice so GPT-4o can read every digit. Not persisted.
     */
    val imageData: ByteArray,
    /**
     * Storage-quality JPEG bytes — GRAYSCALE, smaller, persisted via
     * POST /invoices as the audit/reference copy. Color cues aren't needed
     * for an archive, so this is aggressively compressed to save server disk.
     */
    val storageImageData: ByteArray,
    val width: Int,
    val height: Int,
    /** Size in bytes of the scan copy ([imageData]). */
    val fileSize: Int,
    /** Size in bytes of the storage copy ([storageImageData]). */
    val storageFileSize: Int,
    /**
     * Laplacian variance — higher = sharper.
     * Threshold: variance < 100 → blurry image.
     */
    val blurScore: Float,
    /**
     * Average pixel luminance (0–255).
     * < 40 → too dark | > 220 → too bright
     */
    val brightnessScore: Float,
    /**
     * Density of detected edge pixels (0.0–1.0).
     * < 0.05 → virtually no document edges found.
     */
    val edgeScore: Float,
    /**
     * Ratio of document bounding area to total image area (0.0–1.0).
     * < 0.50 → invoice too small / not filling the frame.
     */
    val documentCoverage: Float,
    /** List of quality problems detected. Empty = all checks passed. */
    val qualityIssues: List<ImageQualityIssue>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProcessedInvoiceImage) return false
        return imageData.contentEquals(other.imageData) &&
                storageImageData.contentEquals(other.storageImageData) &&
                width == other.width &&
                height == other.height &&
                fileSize == other.fileSize &&
                storageFileSize == other.storageFileSize &&
                blurScore == other.blurScore &&
                brightnessScore == other.brightnessScore &&
                edgeScore == other.edgeScore &&
                documentCoverage == other.documentCoverage &&
                qualityIssues == other.qualityIssues
    }

    override fun hashCode(): Int {
        var result = imageData.contentHashCode()
        result = 31 * result + storageImageData.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + fileSize
        result = 31 * result + storageFileSize
        result = 31 * result + blurScore.hashCode()
        result = 31 * result + brightnessScore.hashCode()
        result = 31 * result + edgeScore.hashCode()
        result = 31 * result + documentCoverage.hashCode()
        result = 31 * result + qualityIssues.hashCode()
        return result
    }
}

/**
 * Quality issues detected during preprocessing.
 *
 * Severity:
 *  - HARD BLOCK → [NoDocumentEdges], [DocumentTooSmall]: must retake
 *  - SOFT WARNING → [TooBlurry], [TooDark], [TooBright]: user can proceed
 */
sealed class ImageQualityIssue {
    /** Laplacian variance < 100. Motion blur or out-of-focus. */
    data object TooBlurry : ImageQualityIssue()

    /** Average luminance < 40. Too dark to read. */
    data object TooDark : ImageQualityIssue()

    /** Average luminance > 220. Overexposed / washed out. */
    data object TooBright : ImageQualityIssue()

    /** Edge density < 0.05. No recognisable document boundary found. */
    data object NoDocumentEdges : ImageQualityIssue()

    /** Document coverage < 50%. Invoice too far away / frame too wide. */
    data object DocumentTooSmall : ImageQualityIssue()
}

/** Whether this issue is a hard block (must retake) or soft warning (can proceed). */
val ImageQualityIssue.isHardBlock: Boolean
    get() = this is ImageQualityIssue.NoDocumentEdges || this is ImageQualityIssue.DocumentTooSmall

/** Human-readable hint shown to the user for each issue. */
val ImageQualityIssue.userMessage: String
    get() = when (this) {
        is ImageQualityIssue.TooBlurry -> "Image is blurry — hold the phone steady and retake"
        is ImageQualityIssue.TooDark -> "Too dark — move to a better lit area"
        is ImageQualityIssue.TooBright -> "Overexposed — avoid direct light on the invoice"
        is ImageQualityIssue.NoDocumentEdges -> "No document detected — place invoice on a flat surface"
        is ImageQualityIssue.DocumentTooSmall -> "Invoice too small — move closer and fill the frame"
    }

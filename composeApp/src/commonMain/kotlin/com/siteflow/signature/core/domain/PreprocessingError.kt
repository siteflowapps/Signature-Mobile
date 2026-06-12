package com.siteflow.signature.core.domain

/**
 * Typed errors returned by [InvoicePreprocessor.preprocessForUpload].
 * Each error has a short machine-readable [code] for logging/analytics.
 */
sealed class PreprocessingError(val code: String) : Exception(code) {

    /** Input image path was null, empty, or file not found. */
    data object ImageNull : PreprocessingError("IMG_NULL")

    /** Edge detection step failed to run (internal error). */
    data object EdgeDetectionFailed : PreprocessingError("EDGE_FAIL")

    /** No document / rectangle could be detected in the image. */
    data object DocumentNotDetected : PreprocessingError("DOC_NOT_FOUND")

    /** Image variance too low — considered too blurry to process. */
    data object ImageTooBlurry : PreprocessingError("BLUR")

    /** Average luminance too low — image unusably dark. */
    data object ImageTooDark : PreprocessingError("DARK")

    /** Bitmap decoding or compression failed. */
    data class ProcessingFailed(val reason: String) : PreprocessingError("PROC_FAIL")

    /** Unexpected error. */
    data class Unknown(val reason: String) : PreprocessingError("UNKNOWN")

    /** User-friendly message for each error. */
    val userMessage: String
        get() = when (this) {
            ImageNull -> "Could not read the image. Please try again."
            EdgeDetectionFailed -> "Could not detect document edges. Try again."
            DocumentNotDetected -> "No invoice detected. Place it on a flat surface."
            ImageTooBlurry -> "Image is too blurry. Hold steady and retake."
            ImageTooDark -> "Image is too dark. Move to a better lit area."
            is ProcessingFailed -> "Image processing failed: $reason"
            is Unknown -> "An unexpected error occurred: $reason"
        }
}

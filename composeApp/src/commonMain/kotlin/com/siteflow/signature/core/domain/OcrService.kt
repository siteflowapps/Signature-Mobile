package com.siteflow.signature.core.domain

/**
 * Cross-platform OCR service.
 * Android: Google ML Kit Text Recognition
 * iOS: Apple Vision VNRecognizeTextRequest
 */
expect class OcrService {
    /**
     * Extract text from an image at the given file path.
     * @return OcrResult with raw text and individual lines.
     */
    suspend fun extractText(imagePath: String): OcrResult
}

/**
 * Result of OCR text extraction.
 */
data class OcrResult(
    val rawText: String,
    val lines: List<String>,
    val success: Boolean = true,
    val errorMessage: String? = null
) {
    companion object {
        fun failure(message: String) = OcrResult(
            rawText = "",
            lines = emptyList(),
            success = false,
            errorMessage = message
        )
    }
}

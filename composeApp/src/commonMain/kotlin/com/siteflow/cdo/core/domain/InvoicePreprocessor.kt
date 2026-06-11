package com.siteflow.cdo.core.domain

import com.siteflow.cdo.core.domain.model.ProcessedInvoiceImage

/**
 * On-device invoice image preprocessing pipeline.
 *
 * Runs the full pipeline on the captured image:
 *   1. Auto-detect document edges
 *   2. Crop document area
 *   3. Perspective correction
 *   4. Convert to grayscale
 *   5. Enhance contrast
 *   6. Resize to max width 1200px
 *   7. JPEG compress to ~150–250 KB
 *   8. Quality analysis (blur / brightness / coverage)
 *
 * Android actual: ML Kit Document Scanner + BitmapFactory post-processing
 * iOS actual:     VNDetectRectanglesRequest + CIPerspectiveCorrection + CIColorControls
 */
expect class InvoicePreprocessor {

    /**
     * Runs the full preprocessing pipeline on [imagePath].
     *
     * @param imagePath Absolute file-system path to the raw captured image.
     * @param filter    Visual filter applied during grayscale / contrast step.
     *                  Defaults to [InvoiceFilter.GRAYSCALE] — best for OCR.
     * @return [Result] wrapping [ProcessedInvoiceImage] on success,
     *         or [PreprocessingError] on failure.
     */
    suspend fun preprocessForUpload(
        imagePath: String,
        filter: InvoiceFilter = InvoiceFilter.GRAYSCALE
    ): Result<ProcessedInvoiceImage>
}

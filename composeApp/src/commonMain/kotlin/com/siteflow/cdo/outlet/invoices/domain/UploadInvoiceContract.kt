package com.siteflow.cdo.outlet.invoices.domain

import com.siteflow.cdo.core.domain.InvoiceFilter
import com.siteflow.cdo.core.domain.model.ImageQualityIssue
import com.siteflow.cdo.core.domain.model.ProcessedInvoiceImage
import com.siteflow.cdo.outlet.invoices.data.ExtractionPhase
import com.siteflow.cdo.outlet.invoices.data.InvoiceMetaData
import com.siteflow.cdo.outlet.invoices.data.UploadInvoiceFormData
import com.siteflow.cdo.outlet.invoices.data.UploadStep

/**
 * MVI Contract for the Upload Invoice flow.
 *
 * Flow:
 *   Capture → [InvoicePreprocessor] → Quality Gate → Upload
 *   → (future) Gemini 1.5 server response → Review form
 */

// ── Actions ──────────────────────────────────────────────────────────────────

sealed interface UploadInvoiceAction {
    data object ScanInvoice : UploadInvoiceAction
    data object UploadFromGallery : UploadInvoiceAction
    data object SaveDraft : UploadInvoiceAction
    data object VerifyAndProceed : UploadInvoiceAction

    /** Submit preprocessed image bytes to POST /invoices */
    data object SubmitToApi : UploadInvoiceAction

    /** User dismisses the retake guide and wants to try again */
    data object DismissRetakeGuide : UploadInvoiceAction

    /** User accepts soft quality warnings and proceeds anyway */
    data object ProceedDespiteWarnings : UploadInvoiceAction

    /** User selects a visual filter — triggers re-processing of the captured image */
    data class SelectFilter(val filter: InvoiceFilter) : UploadInvoiceAction

    /** Triggered after camera / gallery returns an image path */
    data class ImageCaptured(val imagePath: String) : UploadInvoiceAction

    // ── PDF flow ─────────────────────────────────────────────────────────────
    /** User tapped the "Upload PDF" card — analytics-only marker. */
    data object PickPdf : UploadInvoiceAction
    /** PdfPicker delivered raw PDF bytes — kicks off compression. */
    data class PdfPicked(val bytes: ByteArray, val filename: String) : UploadInvoiceAction
    /** User cancelled the system PDF picker. */
    data object PdfPickCancelled : UploadInvoiceAction
    /** User pressed "Remove" on the PDF preview — clears the picked PDF. */
    data object RemovePdf : UploadInvoiceAction

    // ── Form field updates ──
    data class UpdateInvoiceNumber(val value: String) : UploadInvoiceAction
    data class UpdateDate(val value: String) : UploadInvoiceAction
    data class UpdateDistributor(val value: String) : UploadInvoiceAction
    data class UpdateBillTo(val value: String) : UploadInvoiceAction

    // ── Line item editing ──
    data class UpdateLineItem(val index: Int, val field: LineItemField, val value: String) : UploadInvoiceAction
    data class RemoveLineItem(val index: Int) : UploadInvoiceAction
    data object AddLineItem : UploadInvoiceAction

    // ── Summary editing ──
    data class UpdateSummaryField(val field: SummaryField, val value: String) : UploadInvoiceAction

    /** Resets entire upload form to blank — used when starting a new upload. */
    data object ResetState : UploadInvoiceAction

    // ── AI extraction flow ──
    /** Upload image to backend and start polling Gemini AI extraction. */
    data object StartAiExtraction : UploadInvoiceAction
    /** User cancels the in-progress AI extraction. */
    data object CancelExtraction : UploadInvoiceAction
    /** User confirms extracted data and submits the final invoice. */
    data object ConfirmAndSubmit : UploadInvoiceAction
}

// ── Enums for field updates ───────────────────────────────────────────────────

enum class LineItemField {
    PRODUCT_NAME, HSN, QUANTITY, UNIT, MRP, PRICE_PER_UNIT, TOTAL
}

enum class SummaryField {
    GRAND_TOTAL
}

// ── State ─────────────────────────────────────────────────────────────────────

data class UploadInvoiceState(
    val currentStep: UploadStep = UploadStep.CAPTURE,
    val formData: UploadInvoiceFormData = UploadInvoiceFormData(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val hasScannedImage: Boolean = false,
    val capturedImagePath: String? = null,

    // ── Legacy OCR (paused — InvoiceOcrParser is preserved but not called) ──
    @Deprecated("OCR parsing paused. Will be replaced by server-side Gemini 1.5 response.")
    val isProcessingOcr: Boolean = false,
    @Deprecated("OCR parsing paused. Will be replaced by server-side Gemini 1.5 response.")
    val ocrMetaData: InvoiceMetaData? = null,
    @Deprecated("OCR parsing paused. Will be replaced by server-side Gemini 1.5 response.")
    val confidenceScore: Int = 0,

    // ── Preprocessing pipeline ──
    /** True while InvoicePreprocessor is running (edge detect → crop → compress). */
    val isPreprocessing: Boolean = false,
    /** Result of the preprocessing pipeline. imageData used for upload. */
    val preprocessingResult: ProcessedInvoiceImage? = null,
    /** Quality issues found during preprocessing. Empty = all checks passed. */
    val qualityIssues: List<ImageQualityIssue> = emptyList(),
    /**
     * True when a hard-block quality issue was detected.
     * Triggers InvoiceCaptureGuideDialog.
     */
    val showRetakeGuide: Boolean = false,

    /**
     * Currently active visual filter for the SCAN copy. Changing this re-runs the
     * preprocessor. Defaults to ORIGINAL (colour) — GPT-4o reads colour invoices
     * more accurately than grayscale/high-contrast (which clip faint thermal print).
     * The persisted storage copy is always grayscaled regardless of this setting.
     */
    val selectedFilter: InvoiceFilter = InvoiceFilter.ORIGINAL,

    // ── PDF pipeline ──
    /** True while [PdfCompressor] is rasterizing + rebuilding the picked PDF. */
    val isCompressingPdf: Boolean = false,
    /** PDF UI snapshot when the user picked a PDF instead of an image. */
    val pdfPreview: PdfPreviewData? = null,
    /** Original PDF (passthrough) sent to extraction — populated alongside [pdfPreview]. */
    val pdfScanFile: InvoiceFile? = null,
    /** Compressed PDF persisted via submit — populated alongside [pdfPreview]. */
    val pdfStorageFile: InvoiceFile? = null,

    // ── AI extraction ──
    /** True while the AI extraction coroutine is running. */
    val isExtractingWithAi: Boolean = false,
    /** Current stage of the extraction pipeline — drives animation text. */
    val extractionPhase: ExtractionPhase = ExtractionPhase.UPLOADING
) {
    /** True when a PDF has been picked (used by submit/extract paths). */
    val hasPdf: Boolean get() = pdfScanFile != null
}

// ── Events ────────────────────────────────────────────────────────────────────

sealed interface UploadInvoiceEvent {
    data object NavigateBack : UploadInvoiceEvent
    data class ShowToast(val message: String) : UploadInvoiceEvent

    /**
     * Soft quality warning (blurry / dark / bright).
     * UI shows a warning banner but user can still proceed.
     */
    data class ShowQualityWarning(val issues: List<ImageQualityIssue>) : UploadInvoiceEvent

    /**
     * Hard block — document not detected or coverage < 50%.
     * UI must show InvoiceCaptureGuideDialog and user must retake.
     */
    data object PromptRetakeWithGuide : UploadInvoiceEvent

    /** AI extraction started — navigate to InvoiceProcessingScreen. */
    data object NavigateToProcessing : UploadInvoiceEvent

    /** Extraction completed and form pre-filled — navigate to InvoiceReviewScreen. */
    data object NavigateToReview : UploadInvoiceEvent

    /** Extraction failed after all retries — show error with reason. */
    data class ExtractionFailed(val reason: String) : UploadInvoiceEvent

    // ── PDF flow ─────────────────────────────────────────────────────────────
    /** PDF compressed successfully — navigate to PdfPreviewScreen. */
    data object NavigateToPdfPreview : UploadInvoiceEvent
    /** Picked PDF exceeded the 10 MB hard limit. */
    data object PdfFileTooLarge : UploadInvoiceEvent
    /** Picked PDF had more pages than allowed. */
    data class PdfTooManyPages(val actual: Int, val max: Int) : UploadInvoiceEvent
    /** Picked PDF is password-protected. */
    data object PdfEncrypted : UploadInvoiceEvent
    /** Picked PDF is corrupt or could not be opened. */
    data object PdfCorrupt : UploadInvoiceEvent
}

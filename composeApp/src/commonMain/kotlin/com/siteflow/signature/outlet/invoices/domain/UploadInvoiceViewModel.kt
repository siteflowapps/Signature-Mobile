package com.siteflow.signature.outlet.invoices.domain

import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.domain.InvoiceFilter
import com.siteflow.signature.core.domain.InvoicePreprocessor
import com.siteflow.signature.core.domain.PdfCompressionResult
import com.siteflow.signature.core.domain.PdfCompressor
import com.siteflow.signature.core.domain.model.isHardBlock
import com.siteflow.signature.core.domain.AuthRepository
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.signature.core.util.ImageEncoder
import com.siteflow.signature.outlet.invoices.data.ExtractionPhase
import com.siteflow.signature.outlet.invoices.data.InvoiceApi
import com.siteflow.signature.outlet.invoices.data.InvoiceItemDto
import com.siteflow.signature.outlet.invoices.data.InvoiceSummary
import com.siteflow.signature.outlet.invoices.data.InvoiceUploadRequestDto
import com.siteflow.signature.outlet.invoices.data.OcrInvoiceData
import com.siteflow.signature.outlet.invoices.data.SkuLineItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.round
import com.siteflow.signature.core.data.networking.util.JwtUtils

class UploadInvoiceViewModel(
    private val invoicePreprocessor: InvoicePreprocessor,
    private val pdfCompressor: PdfCompressor,
    private val invoiceApi: InvoiceApi,
    private val authRepository: AuthRepository,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<UploadInvoiceState, UploadInvoiceAction, UploadInvoiceEvent>(
    initialState = UploadInvoiceState()
) {
    companion object {
        private const val DEFAULT_INVOICE_DATE = "2026-03-11"
    }

    private var extractionJob: Job? = null

    override fun onAction(action: UploadInvoiceAction) {
        when (action) {
            UploadInvoiceAction.ScanInvoice -> {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceCaptureMethodSelected(method = "camera"))
            }
            UploadInvoiceAction.UploadFromGallery -> {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceCaptureMethodSelected(method = "gallery"))
            }
            is UploadInvoiceAction.ImageCaptured -> onImageCaptured(action.imagePath)
            UploadInvoiceAction.SaveDraft -> onSaveDraft()
            UploadInvoiceAction.VerifyAndProceed -> onVerifyAndProceed()
            UploadInvoiceAction.SubmitToApi -> onSubmitToApi()
            UploadInvoiceAction.DismissRetakeGuide -> {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceRetakeGuideDismissed)
                updateState { it.copy(showRetakeGuide = false) }
            }
            UploadInvoiceAction.ProceedDespiteWarnings -> {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceQualityWarningDismissed)
                updateState { it.copy(qualityIssues = emptyList()) }
            }
            is UploadInvoiceAction.SelectFilter -> onFilterSelected(action.filter)

            // ── Form field updates ──
            is UploadInvoiceAction.UpdateInvoiceNumber -> {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceReviewFieldEdited(fieldName = "invoice_number", wasAutoFilled = state.value.formData.invoiceNumber.isNotBlank()))
                updateField { it.copy(formData = it.formData.copy(invoiceNumber = action.value)) }
            }
            is UploadInvoiceAction.UpdateDate -> {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceReviewFieldEdited(fieldName = "date", wasAutoFilled = state.value.formData.date.isNotBlank()))
                updateField { it.copy(formData = it.formData.copy(date = action.value)) }
            }
            is UploadInvoiceAction.UpdateDistributor -> {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceReviewFieldEdited(fieldName = "distributor_name", wasAutoFilled = state.value.formData.distributorName.isNotBlank()))
                updateField { it.copy(formData = it.formData.copy(distributorName = action.value)) }
            }
            is UploadInvoiceAction.UpdateBillTo -> {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceReviewFieldEdited(fieldName = "bill_to_name", wasAutoFilled = state.value.formData.billToName.isNotBlank()))
                updateField { it.copy(formData = it.formData.copy(billToName = action.value)) }
            }

            // ── Line item editing ──
            is UploadInvoiceAction.UpdateLineItem -> {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceReviewLineItemEdited(
                    itemIndex = action.index,
                    fieldName = action.field.name.lowercase()
                ))
                updateLineItem(action.index, action.field, action.value)
            }
            is UploadInvoiceAction.RemoveLineItem -> {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceReviewLineItemRemoved(itemIndex = action.index))
                removeLineItem(action.index)
            }
            UploadInvoiceAction.AddLineItem -> {
                addLineItem()
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceReviewLineItemAdded(
                    currentItemCount = state.value.formData.skuItems.size + 1
                ))
            }

            // ── Summary editing ──
            is UploadInvoiceAction.UpdateSummaryField ->
                updateSummaryField(action.field, action.value)

            UploadInvoiceAction.ResetState -> {
                updateState { UploadInvoiceState() }
            }

            // ── AI extraction flow ──
            UploadInvoiceAction.StartAiExtraction -> startAiExtraction()
            UploadInvoiceAction.CancelExtraction -> cancelExtraction()
            UploadInvoiceAction.ConfirmAndSubmit -> onSubmitToApi()

            // ── PDF flow ──
            UploadInvoiceAction.PickPdf -> {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceCaptureMethodSelected(method = "pdf"))
            }
            is UploadInvoiceAction.PdfPicked -> onPdfPicked(action.bytes, action.filename)
            UploadInvoiceAction.PdfPickCancelled -> { /* no-op; UI dismisses */ }
            UploadInvoiceAction.RemovePdf -> {
                updateState { it.copy(pdfPreview = null, pdfScanFile = null, pdfStorageFile = null) }
            }
        }
    }

    // ── Image Capture & Preprocessing ────────────────────────────────────────

    private fun onImageCaptured(imagePath: String) {
        analytics.track(AnalyticsEvent.OutletEvent.InvoiceImageCaptured(method = "camera"))
        updateState {
            it.copy(
                isPreprocessing = true,
                capturedImagePath = imagePath,
                qualityIssues = emptyList(),
                showRetakeGuide = false,
                // Switching to image flow clears any prior PDF selection.
                pdfPreview = null,
                pdfScanFile = null,
                pdfStorageFile = null,
            )
        }

        viewModelScope.launch {
            println("[Preprocess] Starting pipeline for: $imagePath")
            analytics.track(AnalyticsEvent.OutletEvent.InvoicePreprocessingStarted)
            val result = invoicePreprocessor.preprocessForUpload(imagePath, state.value.selectedFilter)

            result.onSuccess { processed ->
                println(
                    "[Preprocess] Done | size=${processed.fileSize}B | " +
                    "${processed.width}×${processed.height} | blur=${processed.blurScore} | " +
                    "brightness=${processed.brightnessScore} | edge=${processed.edgeScore} | " +
                    "coverage=${processed.documentCoverage} | issues=${processed.qualityIssues}"
                )

                val hardBlock = processed.qualityIssues.any { it.isHardBlock }

                if (hardBlock) {
                    updateState {
                        it.copy(
                            isPreprocessing = false,
                            showRetakeGuide = true,
                            qualityIssues = processed.qualityIssues,
                            preprocessingResult = processed,
                            hasScannedImage = true
                        )
                    }
                    analytics.track(AnalyticsEvent.OutletEvent.InvoicePreprocessingCompleted(
                        qualityIssuesCount = processed.qualityIssues.size,
                        hasHardBlock = true
                    ))
                    analytics.track(AnalyticsEvent.OutletEvent.InvoiceRetakeGuideShown)
                    emitEvent(UploadInvoiceEvent.PromptRetakeWithGuide)
                    return@onSuccess
                }

                analytics.track(AnalyticsEvent.OutletEvent.InvoicePreprocessingCompleted(
                    qualityIssuesCount = processed.qualityIssues.size,
                    hasHardBlock = false
                ))
                updateState {
                    it.copy(
                        isPreprocessing = false,
                        hasScannedImage = true,
                        preprocessingResult = processed,
                        qualityIssues = processed.qualityIssues
                    )
                }

                if (processed.qualityIssues.isNotEmpty()) {
                    analytics.track(AnalyticsEvent.OutletEvent.InvoiceQualityWarningShown(
                        issues = processed.qualityIssues.joinToString(",") { it.toString() }
                    ))
                    emitEvent(UploadInvoiceEvent.ShowQualityWarning(processed.qualityIssues))
                }

                emitEvent(
                    UploadInvoiceEvent.ShowToast(
                        buildString {
                            append("Invoice image ready")
                            if (processed.fileSize > 0) append(" (${processed.fileSize / 1024}KB)")
                            append(" — fill in details to submit")
                        }
                    )
                )
            }

            result.onFailure { error ->
                println("[Preprocess] Failed: ${error.message}")
                updateState { it.copy(isPreprocessing = false) }
                emitEvent(
                    UploadInvoiceEvent.ShowToast(
                        "Image processing failed — ${error.message ?: "please try again"}"
                    )
                )
            }
        }
    }

    // ── PDF capture ───────────────────────────────────────────────────────────

    private fun onPdfPicked(bytes: ByteArray, filename: String) {
        analytics.track(AnalyticsEvent.OutletEvent.InvoiceImageCaptured(method = "pdf"))
        updateState {
            it.copy(
                isCompressingPdf = true,
                // Clear any prior image state — PDF takes over the upload payload.
                preprocessingResult = null,
                capturedImagePath = null,
                hasScannedImage = false,
                qualityIssues = emptyList(),
                showRetakeGuide = false,
                pdfScanFile = null,
                pdfStorageFile = null,
            )
        }

        viewModelScope.launch {
            println("[PdfPicked] ${bytes.size / 1024}KB \"$filename\"")
            when (val result = pdfCompressor.compress(bytes)) {
                is PdfCompressionResult.Success -> {
                    val preview = com.siteflow.signature.outlet.invoices.domain.PdfPreviewData(
                        filename = filename,
                        pageCount = result.pageCount,
                        originalKb = result.originalKb,
                        finalKb = result.finalKb,
                        thumbnailJpeg = result.firstPageThumbnail,
                    )
                    updateState {
                        it.copy(
                            isCompressingPdf = false,
                            pdfPreview = preview,
                            pdfScanFile = result.scanFile,
                            pdfStorageFile = result.storageFile,
                            hasScannedImage = true,
                        )
                    }
                    emitEvent(UploadInvoiceEvent.NavigateToPdfPreview)
                }
                PdfCompressionResult.Failure.FileTooLarge -> {
                    updateState { it.copy(isCompressingPdf = false) }
                    emitEvent(UploadInvoiceEvent.PdfFileTooLarge)
                }
                is PdfCompressionResult.Failure.TooManyPages -> {
                    updateState { it.copy(isCompressingPdf = false) }
                    emitEvent(
                        UploadInvoiceEvent.PdfTooManyPages(
                            actual = result.actual,
                            max = com.siteflow.signature.core.domain.PdfCompressorConstants.MAX_PAGES,
                        )
                    )
                }
                PdfCompressionResult.Failure.Encrypted -> {
                    updateState { it.copy(isCompressingPdf = false) }
                    emitEvent(UploadInvoiceEvent.PdfEncrypted)
                }
                PdfCompressionResult.Failure.Corrupt -> {
                    updateState { it.copy(isCompressingPdf = false) }
                    emitEvent(UploadInvoiceEvent.PdfCorrupt)
                }
            }
        }
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    private fun onFilterSelected(filter: InvoiceFilter) {
        analytics.track(AnalyticsEvent.OutletEvent.InvoiceFilterSelected(filter = filter.name))
        val imagePath = state.value.capturedImagePath ?: run {
            updateState { it.copy(selectedFilter = filter) }
            return
        }

        updateState { it.copy(selectedFilter = filter, isPreprocessing = true) }

        viewModelScope.launch {
            val result = invoicePreprocessor.preprocessForUpload(imagePath, filter)

            result.onSuccess { processed ->
                updateState {
                    it.copy(
                        isPreprocessing     = false,
                        preprocessingResult = processed,
                        qualityIssues       = processed.qualityIssues
                    )
                }
            }

            result.onFailure { error ->
                updateState { it.copy(isPreprocessing = false) }
                emitEvent(UploadInvoiceEvent.ShowToast("Filter apply failed — ${error.message}"))
            }
        }
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    private fun onSaveDraft() {
        analytics.track(AnalyticsEvent.OutletEvent.InvoiceDraftSaved(itemsCount = state.value.formData.skuItems.size))
        emitEvent(UploadInvoiceEvent.ShowToast("Draft saved successfully"))
        emitEvent(UploadInvoiceEvent.NavigateBack)
    }

    private fun onVerifyAndProceed() {
        emitEvent(UploadInvoiceEvent.ShowToast("Invoice submitted for verification"))
        emitEvent(UploadInvoiceEvent.NavigateBack)
    }

    /**
     * Upload preprocessed image or compressed PDF + extracted form data to POST /invoices.
     */
    private fun onSubmitToApi() {
        // Persist the compressed PDF (storage copy) to save server disk.
        val pdfFile = state.value.pdfStorageFile
        val imagePath = state.value.capturedImagePath
        if (pdfFile == null && imagePath == null) {
            GlobalToastHandler.showError("Please capture an invoice image or upload a PDF first")
            return
        }

        updateState { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            try {
                val file = pdfFile ?: run {
                    // Persist the smaller GRAYSCALE storage copy — this is the archived
                    // copy, not what GPT reads, so we save server disk here.
                    val photoBytes = state.value.preprocessingResult?.storageImageData
                        ?: imagePath?.let { ImageEncoder.encodeToBytes(it) }
                    if (photoBytes == null) {
                        updateState { it.copy(isSubmitting = false) }
                        GlobalToastHandler.showError("Failed to read invoice image")
                        return@launch
                    }
                    com.siteflow.signature.outlet.invoices.domain.InvoiceFile.jpeg(photoBytes)
                }

                val formData = state.value.formData
                // Send only matched items, passing through raw API data
                val items = formData.skuItems.map { sku ->
                    InvoiceItemDto(
                        skuId = sku.skuId,
                        skuName = sku.productName,
                        invoicedSkuName = sku.invoicedSkuName,
                        invoicedQuantity = sku.quantity,
                        invoicedUnit = sku.unit,
                        invoicedUnitPrice = sku.pricePerUnit,
                        invoicedTotalPrice = sku.pricePerUnit * sku.quantity,
                        matchedSkuName = sku.matchedSkuName,
                        caseConfiguration = sku.caseConfiguration,
                        mrpPerCase = sku.mrpPerCase,
                        mrpPerBottle = sku.mrpPerBottle,
                        finalQuantity = sku.finalQuantity,
                        finalUnit = sku.finalUnit,
                        isNonCatalogItem = sku.isNonCatalogItem,
                        confidence = sku.confidence
                    )
                }

                // Use outletId from /users/me (stored in preferences), fallback to JWT sub
                val token = authRepository.getAccessToken()
                val outletId = authRepository.getOutletId()
                    ?: token?.let { JwtUtils.extractUserId(it) }
                    ?: "unknown"
                val distributorId = authRepository.getDistributorId() ?: "unknown"
                println("[OCR_NETWORK] │ Using outletId: $outletId, distributorId: $distributorId")

                // Normalize date to yyyy-MM-dd (backend format)
                val rawDate = formData.date.ifBlank { DEFAULT_INVOICE_DATE }
                val normalizedDate = normalizeDate(rawDate)

                val requestDto = InvoiceUploadRequestDto(
                    outletId = outletId,
                    distributorId = distributorId,
                    invoiceNumber = formData.invoiceNumber.ifBlank { "INV-${(1000000..9999999).random()}" },
                    invoiceDate = normalizedDate,
                    items = items,
                    digitalSignature = false,
                    distributorName = formData.distributorName,
                    retailerName = formData.billToName,
                    totalInvoiceAmount = formData.invoiceSummary.grandTotal.toString()
                )

                // Detailed logging for debugging
                val jsonPayload = kotlinx.serialization.json.Json { prettyPrint = true }
                    .encodeToString(InvoiceUploadRequestDto.serializer(), requestDto)
                println("[OCR_NETWORK] ┌── INVOICE SUBMIT ──────────────────────")
                println("[OCR_NETWORK] │ POST /invoices")
                println("[OCR_NETWORK] │ File: ${file.filename} ${file.mimeType} ${file.sizeKb}KB")
                println("[OCR_NETWORK] │ Items: ${items.size}")
                println("[OCR_NETWORK] │ JSON Payload:")
                jsonPayload.lines().forEach { line ->
                    println("[OCR_NETWORK] │   $line")
                }
                println("[OCR_NETWORK] └─────────────────────────────────────")

                val invoiceIdForAnalytics = requestDto.invoiceNumber
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceSubmitted(
                    itemsCount = items.size,
                    grandTotal = requestDto.totalInvoiceAmount.toDoubleOrNull() ?: 0.0,
                    wasAutoFilled = state.value.formData.isAutoFilled
                ))
                invoiceApi.uploadInvoice(file = file, data = requestDto)
                    .onSuccess { response ->
                        println("[OCR_NETWORK] ✅ INVOICE SUBMIT SUCCESS: ${response.data?.invoiceNumber ?: "no number"} | id=${response.data?.id}")
                        val returnedId = response.data?.invoiceNumber ?: invoiceIdForAnalytics
                        analytics.track(AnalyticsEvent.OutletEvent.InvoiceSubmitSuccess(invoiceId = returnedId, totalUploadDurationMs = 0L))
                        updateState { it.copy(isSubmitting = false) }
                        GlobalToastHandler.showSuccess(
                            "Invoice uploaded! ${response.data?.invoiceNumber ?: requestDto.invoiceNumber}"
                        )
                        emitEvent(UploadInvoiceEvent.NavigateBack)
                    }
                    .onError { error ->
                        println("[OCR_NETWORK] ❌ INVOICE SUBMIT FAILED: code=${error.code} msg=${error.message}")
                        analytics.track(AnalyticsEvent.OutletEvent.InvoiceSubmitFailed(
                            errorMessage = error.message
                        ))
                        updateState { it.copy(isSubmitting = false) }
                        GlobalToastHandler.showError("Upload failed: ${error.message}")
                    }

            } catch (e: Exception) {
                println("[OCR_NETWORK] ❌ INVOICE SUBMIT EXCEPTION: ${e::class.simpleName}: ${e.message}")
                updateState { it.copy(isSubmitting = false) }
                GlobalToastHandler.showError("Error: ${e.message}")
            }
        }
    }

    // ── AI Extraction ─────────────────────────────────────────────────────────

    /**
     * Upload image or PDF to POST /ocr/extract-invoice and wait for the response.
     * The animation keeps running until [OcrExtractResponse] is received.
     */
    private fun startAiExtraction() {
        // Send the untouched original PDF (scan copy) for best GPT-4o extraction.
        val pdfFile = state.value.pdfScanFile
        val imagePath = state.value.capturedImagePath
        if (pdfFile == null && imagePath == null) {
            GlobalToastHandler.showError("Please capture an invoice image or upload a PDF first")
            return
        }

        extractionJob?.cancel()
        analytics.track(AnalyticsEvent.OutletEvent.InvoiceAiExtractionStarted)
        updateState {
            it.copy(
                isExtractingWithAi = true,
                extractionPhase = ExtractionPhase.UPLOADING
            )
        }
        emitEvent(UploadInvoiceEvent.NavigateToProcessing)

        extractionJob = viewModelScope.launch {
            try {
                val file = pdfFile ?: run {
                    // Send the higher-quality COLOUR scan copy for extraction — this is
                    // what GPT-4o reads, so accuracy matters more than size here.
                    val photoBytes = state.value.preprocessingResult?.imageData
                        ?: imagePath?.let { ImageEncoder.encodeToBytes(it) }
                    if (photoBytes == null) {
                        updateState { it.copy(isExtractingWithAi = false, extractionPhase = ExtractionPhase.ERROR) }
                        emitEvent(UploadInvoiceEvent.ExtractionFailed("Failed to read invoice image"))
                        return@launch
                    }
                    com.siteflow.signature.outlet.invoices.domain.InvoiceFile.jpeg(photoBytes)
                }

                println("[OcrExtract] Uploading ${file.sizeKb}KB ${file.mimeType} to /ocr/extract-invoice")

                invoiceApi.extractInvoice(file)
                    .onSuccess { response ->
                        if (response.success && response.data != null) {
                            applyOcrResult(response.data)
                        } else {
                            updateState { it.copy(isExtractingWithAi = false, extractionPhase = ExtractionPhase.ERROR) }
                            emitEvent(UploadInvoiceEvent.ExtractionFailed("Extraction failed — please try again"))
                        }
                    }
                    .onError { error ->
                        analytics.track(AnalyticsEvent.OutletEvent.InvoiceAiExtractionFailed(reason = error.message ?: "Unknown error"))
                        updateState { it.copy(isExtractingWithAi = false, extractionPhase = ExtractionPhase.ERROR) }
                        emitEvent(UploadInvoiceEvent.ExtractionFailed(error.message ?: "Upload failed — please try again"))
                    }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                analytics.track(AnalyticsEvent.OutletEvent.InvoiceAiExtractionFailed(reason = e.message ?: "Unknown error"))
                updateState { it.copy(isExtractingWithAi = false, extractionPhase = ExtractionPhase.ERROR) }
                emitEvent(UploadInvoiceEvent.ExtractionFailed(e.message ?: "Unknown error"))
            }
        }
    }

    private fun cancelExtraction() {
        extractionJob?.cancel()
        analytics.track(AnalyticsEvent.OutletEvent.InvoiceAiExtractionCancelled(
            phaseOnCancel = state.value.extractionPhase.name
        ))
        updateState {
            it.copy(
                isExtractingWithAi = false,
                extractionPhase = ExtractionPhase.UPLOADING
            )
        }
    }

    /**
     * Maps an [OcrInvoiceData] into the form state and emits NavigateToReview.
     * Splits items by confidence: ≥90 → matched items, <90 → rejected items.
     * Grand total = sum of (mrpPerCase × finalQuantity) for matched items.
     */
    private fun applyOcrResult(data: OcrInvoiceData) {
        val allItems = data.items.mapIndexed { i, item ->
            // `finalQuantity`/MRP aren't always returned yet; fall back to the
            // invoiced values the OCR does send so qty/totals are never zero.
            val effectiveQty = if (item.finalQuantity > 0) item.finalQuantity else item.invoicedQuantity
            // Prefer MRP-based revenue (Signature basis); fall back to the invoiced
            // line total until the backend populates MRP for the matched SKU.
            val mrpTotal = (item.mrpPerCase ?: 0.0) * effectiveQty
            val lineTotal = if (mrpTotal > 0.0) mrpTotal else item.invoicedTotalPrice
            SkuLineItem(
                id = "ai-item-$i",
                productName = item.matchedSkuName ?: item.invoicedSkuName,
                invoicedSkuName = item.invoicedSkuName,
                matchedSkuName = item.matchedSkuName,
                skuId = item.skuId,
                isMatched = item.matchedSkuName != null,
                quantity = item.invoicedQuantity,
                unit = item.invoicedUnit,
                pricePerUnit = item.invoicedUnitPrice,
                totalPrice = lineTotal,
                caseConfiguration = item.caseConfiguration,
                mrpPerCase = item.mrpPerCase,
                mrpPerBottle = item.mrpPerBottle,
                finalQuantity = effectiveQty,
                finalUnit = item.finalUnit.ifBlank { item.invoicedUnit },
                confidence = item.confidence,
                isNonCatalogItem = item.isNonCatalogItem,
                lowConfidenceReason = item.lowConfidenceReason
            )
        }

        val matchedItems = allItems.filter { it.confidence >= 90 }
        val rejectedItems = allItems.filter { it.confidence < 90 }
        // Headline = invoice total from the OCR response; fall back to the matched line sum.
        val grandTotal = data.totalInvoiceAmount.toDoubleOrNull()?.takeIf { it > 0.0 }
            ?: matchedItems.sumOf { it.totalPrice }

        val current = state.value.formData
        val updatedForm = current.copy(
            invoiceNumber = data.invoiceNumber.takeIf { it.isNotBlank() } ?: current.invoiceNumber,
            date = data.invoiceDate.takeIf { it.isNotBlank() } ?: current.date,
            distributorName = data.distributorName.takeIf { it.isNotBlank() } ?: current.distributorName,
            billToName = data.retailerName.takeIf { it.isNotBlank() } ?: current.billToName,
            skuItems = matchedItems,
            rejectedItems = rejectedItems,
            invoiceSummary = current.invoiceSummary.copy(grandTotal = grandTotal),
            isAutoFilled = true
        )

        println("[OcrExtract] Applied result: ${matchedItems.size} matched, ${rejectedItems.size} rejected, total=$grandTotal")
        analytics.track(AnalyticsEvent.OutletEvent.InvoiceAiExtractionCompleted(itemsExtracted = matchedItems.size))
        analytics.track(AnalyticsEvent.OutletEvent.InvoiceReviewViewed(
            invoiceId = updatedForm.invoiceNumber.takeIf { it.isNotBlank() } ?: "",
            itemsCount = matchedItems.size,
            grandTotal = grandTotal,
            isAutoFilled = true
        ))
        updateState {
            it.copy(
                isExtractingWithAi = false,
                formData = updatedForm
            )
        }
        emitEvent(UploadInvoiceEvent.NavigateToReview)
    }

    // ── Field Update Helpers ──────────────────────────────────────────────────

    private fun updateField(reducer: (UploadInvoiceState) -> UploadInvoiceState) = updateState(reducer)

    private fun updateLineItem(index: Int, field: LineItemField, value: String) {
        updateField { state ->
            val items = state.formData.skuItems.toMutableList()
            if (index in items.indices) {
                val item = items[index]
                items[index] = when (field) {
                    LineItemField.PRODUCT_NAME -> item.copy(productName = value)
                    LineItemField.HSN          -> item.copy(hsnCode = value)
                    LineItemField.QUANTITY -> {
                        val qty = value.toIntOrNull() ?: 0
                        val total = round(item.pricePerUnit * qty).toDouble()
                        item.copy(quantity = qty, totalPrice = if (total > 0) total else item.totalPrice)
                    }
                    LineItemField.UNIT          -> item.copy(unit = value)
                    LineItemField.MRP           -> item.copy(mrp = value.toDoubleOrNull() ?: 0.0)
                    LineItemField.PRICE_PER_UNIT -> {
                        val price = value.toDoubleOrNull() ?: 0.0
                        val total = round(price * item.quantity).toDouble()
                        item.copy(pricePerUnit = price, totalPrice = if (total > 0) total else item.totalPrice)
                    }
                    LineItemField.TOTAL -> item.copy(totalPrice = value.toDoubleOrNull() ?: 0.0)
                }
            }
            state.copy(formData = state.formData.copy(skuItems = items))
        }
    }

    private fun removeLineItem(index: Int) {
        updateField { state ->
            val items = state.formData.skuItems.toMutableList()
            if (index in items.indices) items.removeAt(index)
            state.copy(formData = state.formData.copy(skuItems = items))
        }
    }

    private fun addLineItem() {
        updateField { state ->
            val newItem = SkuLineItem(
                id = "item-${(100000..999999).random()}",
                productName = "",
                quantity = 1,
                unit = "CS"
            )
            state.copy(formData = state.formData.copy(skuItems = state.formData.skuItems + newItem))
        }
    }

    private fun updateSummaryField(field: SummaryField, value: String) {
        updateField { state ->
            val summary = state.formData.invoiceSummary
            val updated = when (field) {
                SummaryField.GRAND_TOTAL -> summary.copy(grandTotal = value.toDoubleOrNull() ?: 0.0)
            }
            state.copy(formData = state.formData.copy(invoiceSummary = updated))
        }
    }

    /**
     * Normalize various OCR date formats to yyyy-MM-dd for the backend.
     * Handles: dd-MMM-yy, dd-MMM-yyyy, dd/MM/yyyy, dd-MM-yyyy, MM/dd/yyyy, yyyy-MM-dd (passthrough)
     */
    private fun normalizeDate(raw: String): String {
        val trimmed = raw.trim()

        // Already yyyy-MM-dd
        if (Regex("""^\d{4}-\d{2}-\d{2}$""").matches(trimmed)) return trimmed

        val monthMap = mapOf(
            "jan" to "01", "feb" to "02", "mar" to "03", "apr" to "04",
            "may" to "05", "jun" to "06", "jul" to "07", "aug" to "08",
            "sep" to "09", "oct" to "10", "nov" to "11", "dec" to "12"
        )

        // dd-MMM-yy or dd-MMM-yyyy  (e.g. 21-Aug-25, 21-Aug-2025)
        val monthNameRegex = Regex("""^(\d{1,2})[/\-]([A-Za-z]{3,})[/\-](\d{2,4})$""")
        monthNameRegex.matchEntire(trimmed)?.let { m ->
            val dd = m.groupValues[1].padStart(2, '0')
            val mm = monthMap[m.groupValues[2].take(3).lowercase()] ?: return trimmed
            val rawYear = m.groupValues[3]
            val yyyy = if (rawYear.length == 2) "20$rawYear" else rawYear
            return "$yyyy-$mm-$dd"
        }

        // dd-MM-yyyy or dd/MM/yyyy (e.g. 21-08-2025, 21/08/2025)
        val ddMmYyyy = Regex("""^(\d{2})[/\-](\d{2})[/\-](\d{4})$""")
        ddMmYyyy.matchEntire(trimmed)?.let { m ->
            val (dd, mm, yyyy) = m.destructured
            return "$yyyy-$mm-$dd"
        }

        // dd-MM-yy (e.g. 21-08-25)
        val ddMmYy = Regex("""^(\d{2})[/\-](\d{2})[/\-](\d{2})$""")
        ddMmYy.matchEntire(trimmed)?.let { m ->
            val (dd, mm, yy) = m.destructured
            return "20$yy-$mm-$dd"
        }

        println("[OCR_NETWORK] ⚠️ Could not normalize date: '$trimmed', using as-is")
        return trimmed
    }
}

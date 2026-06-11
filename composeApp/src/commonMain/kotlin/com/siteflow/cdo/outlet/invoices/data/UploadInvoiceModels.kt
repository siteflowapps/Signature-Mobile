package com.siteflow.cdo.outlet.invoices.data

import androidx.compose.ui.graphics.Color
import com.siteflow.cdo.core.presentation.design.AppColors

/**
 * Upload flow step indicator.
 */
enum class UploadStep(val stepNumber: Int, val label: String) {
    CAPTURE(1, "Capture"),
    VERIFY(2, "Verify"),
    SUBMIT(3, "Submit")
}

/**
 * Stages of the AI extraction pipeline.
 * Drives the PremiumProcessingAnimation phase text.
 */
enum class ExtractionPhase {
    UPLOADING,  // Uploading image and waiting for extraction result
    ERROR
}

/**
 * A single SKU line item on the invoice.
 * Supports both OCR-extracted and manually-entered items.
 */
data class SkuLineItem(
    val id: String = "",
    val productName: String,              // Display name (matched or invoiced)
    val invoicedSkuName: String = "",     // Original SKU name from invoice
    val matchedSkuName: String? = null,   // Matched catalog SKU name
    val skuId: String? = null,            // Matched catalog SKU ID
    val rawOcrName: String = "",
    val matchedProductId: String? = null,
    val isMatched: Boolean = false,
    val hsnCode: String = "",
    val quantity: Int = 0,
    val rawQuantity: String = "",
    val unit: String = "",
    val mrp: Double = 0.0,
    val pricePerUnit: Double = 0.0,
    val gstRate: Double = 0.0,
    val gstAmount: Double = 0.0,
    val totalPrice: Double = 0.0,

    // ── New API fields ──
    val caseConfiguration: Int? = null,   // e.g. 24 bottles per case
    val mrpPerCase: Double? = null,
    val mrpPerBottle: Double? = null,
    val finalQuantity: Int = 0,
    val finalUnit: String = "",
    val confidence: Int = 0,              // 0-100
    val isNonCatalogItem: Boolean = false,
    val lowConfidenceReason: String? = null
)

/**
 * Tax breakdown row (CGST or SGST).
 */
data class TaxBreakdown(
    val taxType: String = "",            // "CGST" or "SGST"
    val hsnCode: String = "",            // Some invoices show HSN per tax row
    val taxableAmount: Double = 0.0,
    val rate: Double = 0.0,             // e.g. 20.0 for 20%
    val taxAmount: Double = 0.0
)

/**
 * Invoice summary with all monetary totals.
 */
data class InvoiceSummary(
    val subTotal: Double = 0.0,
    val totalTax: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0,
    val receivedAmount: Double = 0.0,
    val balanceDue: Double = 0.0
)

/**
 * Invoice upload form state.
 */
data class UploadInvoiceFormData(
    val invoiceNumber: String = "",
    val date: String = "",
    val distributorName: String = "",
    val distributorGSTIN: String = "",
    val distributorEmail: String = "",
    val distributorPhone: String = "",
    val distributorAddress: String = "",
    val billToName: String = "",
    val skuItems: List<SkuLineItem> = emptyList(),
    val rejectedItems: List<SkuLineItem> = emptyList(),  // Items with confidence < 90
    val taxBreakdown: List<TaxBreakdown> = emptyList(),
    val invoiceSummary: InvoiceSummary = InvoiceSummary(),
    val totalItems: Int = 0,
    val totalQuantity: Int = 0,
    val invoiceImagePath: String? = null,
    val isAutoFilled: Boolean = false
)

/**
 * Mock pre-filled form data matching the design.
 */
val mockUploadFormData = UploadInvoiceFormData(
    invoiceNumber = "INV-2026-001",
    date = "02/15/2026",
    distributorName = "Global Supplies Pvt Ltd",
    skuItems = listOf(
        SkuLineItem(
            id = "item-1",
            productName = "Campa Cola 1L",
            rawOcrName = "CAMPA COLA FLVRD DRINK 1LTR PET",
            matchedProductId = "campa_cola_1l",
            isMatched = true,
            hsnCode = "22021010",
            quantity = 10,
            unit = "CS",
            mrp = 0.0,
            pricePerUnit = 100.0,
            gstRate = 40.0,
            gstAmount = 400.0,
            totalPrice = 1000.0
        ),
        SkuLineItem(
            id = "item-2",
            productName = "Campa Cola 500ml",
            rawOcrName = "CAMPA COLA FLVRD DRINK 500ML PET",
            matchedProductId = "campa_cola_500ml",
            isMatched = true,
            hsnCode = "22021010",
            quantity = 24,
            unit = "CS",
            mrp = 0.0,
            pricePerUnit = 287.5,
            gstRate = 40.0,
            gstAmount = 2760.0,
            totalPrice = 6900.0
        )
    ),
    taxBreakdown = listOf(
        TaxBreakdown(
            taxType = "CGST",
            hsnCode = "22021010",
            taxableAmount = 3950.0,
            rate = 20.0,
            taxAmount = 790.0
        ),
        TaxBreakdown(
            taxType = "SGST",
            hsnCode = "22021010",
            taxableAmount = 3950.0,
            rate = 20.0,
            taxAmount = 790.0
        )
    ),
    invoiceSummary = InvoiceSummary(
        subTotal = 7900.0,
        totalTax = 1580.0,
        roundOff = 0.0,
        grandTotal = 8480.0,
        receivedAmount = 0.0,
        balanceDue = 8480.0
    ),
    totalItems = 2,
    totalQuantity = 34,
    isAutoFilled = true
)

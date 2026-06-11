package com.siteflow.cdo.outlet.invoices.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level response from POST /ocr/extract-invoice.
 */
@Serializable
data class OcrExtractResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("data") val data: OcrInvoiceData? = null,
    @SerialName("timestamp") val timestamp: String? = null
)

/**
 * Extracted invoice data returned by the OCR API.
 */
@Serializable
data class OcrInvoiceData(
    @SerialName("distributorName") val distributorName: String = "",
    @SerialName("retailerName") val retailerName: String = "",
    @SerialName("invoiceNumber") val invoiceNumber: String = "",
    @SerialName("invoiceDate") val invoiceDate: String = "",
    @SerialName("items") val items: List<OcrLineItem> = emptyList(),
    @SerialName("totalInvoiceAmount") val totalInvoiceAmount: String = "0"
)

/**
 * A single line item extracted from the invoice image.
 * Contains both invoiced (raw OCR) and matched (catalog) data.
 */
@Serializable
data class OcrLineItem(
    // ── Invoiced (raw OCR) fields ──
    @SerialName("invoicedSkuName") val invoicedSkuName: String = "",
    @SerialName("invoicedQuantity") val invoicedQuantity: Int = 0,
    @SerialName("invoicedUnit") val invoicedUnit: String = "",
    @SerialName("invoicedUnitPrice") val invoicedUnitPrice: Double = 0.0,
    @SerialName("invoicedTotalPrice") val invoicedTotalPrice: Double = 0.0,

    // ── Matched (catalog) fields — null when no match ──
    @SerialName("skuId") val skuId: String? = null,
    @SerialName("matchedSkuName") val matchedSkuName: String? = null,
    @SerialName("caseConfiguration") val caseConfiguration: Int? = null,
    @SerialName("mrpPerCase") val mrpPerCase: Double? = null,
    @SerialName("mrpPerBottle") val mrpPerBottle: Double? = null,

    // ── Final resolved values ──
    @SerialName("finalQuantity") val finalQuantity: Int = 0,
    @SerialName("finalUnit") val finalUnit: String = "",

    // ── Confidence & rejection ──
    @SerialName("confidence") val confidence: Int = 0,
    @SerialName("isNonCatalogItem") val isNonCatalogItem: Boolean = false,
    @SerialName("lowConfidenceReason") val lowConfidenceReason: String? = null
)

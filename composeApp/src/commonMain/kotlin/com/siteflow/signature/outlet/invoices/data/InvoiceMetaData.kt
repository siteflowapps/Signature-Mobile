package com.siteflow.signature.outlet.invoices.data

/**
 * Structured invoice metadata extracted from OCR text.
 * Block 1 — metadata only (no SKU, no tax breakdown).
 */
data class InvoiceMetaData(
    val distributorName: String?,
    val distributorGSTIN: String?,
    val invoiceNumber: String?,
    val invoiceDate: String?,
    val distributorEmail: String?,
    val distributorPhone: String?,
    val distributorAddress: String?,
    val billToName: String?,
    val rawText: String,
    val confidenceScore: Int  // 0–100
) {
    companion object {
        fun empty(rawText: String = "") = InvoiceMetaData(
            distributorName = null,
            distributorGSTIN = null,
            invoiceNumber = null,
            invoiceDate = null,
            distributorEmail = null,
            distributorPhone = null,
            distributorAddress = null,
            billToName = null,
            rawText = rawText,
            confidenceScore = 0
        )
    }
}

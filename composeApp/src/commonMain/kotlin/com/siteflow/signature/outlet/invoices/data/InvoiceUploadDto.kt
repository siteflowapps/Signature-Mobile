package com.siteflow.signature.outlet.invoices.data

import kotlinx.serialization.Serializable

/**
 * A single invoice line item for the submit payload.
 * Passes through all raw OCR + catalog-matched data from extraction.
 */
@Serializable
data class InvoiceItemDto(
    val skuId: String? = null,
    val skuName: String = "",
    val invoicedSkuName: String = "",
    val invoicedQuantity: Int = 0,
    val invoicedUnit: String = "",
    val invoicedUnitPrice: Double = 0.0,
    val invoicedTotalPrice: Double = 0.0,
    val matchedSkuName: String? = null,
    val caseConfiguration: Int? = null,
    val mrpPerCase: Double? = null,
    val mrpPerBottle: Double? = null,
    val finalQuantity: Int = 0,
    val finalUnit: String = "",
    val category: String? = null,
    val isSchemeItem: Boolean = false,
    val isNonCatalogItem: Boolean = false,
    val confidence: Int = 0
)

/**
 * JSON payload for the `data` part of the multipart invoice upload.
 */
@Serializable
data class InvoiceUploadRequestDto(
    val outletId: String,
    val distributorId: String,
    val invoiceNumber: String,
    val invoiceDate: String,
    val items: List<InvoiceItemDto>,
    val digitalSignature: Boolean,
    val distributorName: String,
    val retailerName: String,
    val totalInvoiceAmount: String = "0"
)

/**
 * Generic API response wrapper for invoice upload.
 */
@Serializable
data class InvoiceUploadResponseDto(
    val success: Boolean,
    val data: InvoiceResponseData? = null,
    val error: String? = null,
    val errorCode: String? = null,
    val timestamp: String? = null
)

@Serializable
data class InvoiceResponseData(
    val id: String? = null,
    val invoiceNumber: String? = null,
    val status: String? = null
)

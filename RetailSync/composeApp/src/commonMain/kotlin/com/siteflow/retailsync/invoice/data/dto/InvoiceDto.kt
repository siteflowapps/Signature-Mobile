package com.siteflow.retailsync.invoice.data.dto

import kotlinx.serialization.Serializable

/**
 * A single invoice line item for the submit payload.
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
 * JSON payload for creating an invoice (without photo).
 */
@Serializable
data class InvoiceUploadRequestDto(
    val outletId: String,
    val distributorId: String = "",
    val invoiceNumber: String = "",
    val invoiceDate: String = "",
    val items: List<InvoiceItemDto> = emptyList(),
    val digitalSignature: Boolean = false,
    val distributorName: String = "",
    val retailerName: String = "",
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

/**
 * Response wrapper for GET /invoices.
 */
@Serializable
data class InvoiceListResponseDto(
    val success: Boolean,
    val data: InvoicePageDto? = null,
    val timestamp: String? = null
)

@Serializable
data class InvoicePageDto(
    val content: List<InvoiceDto> = emptyList(),
    val page: Int = 0,
    val size: Int = 20,
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

@Serializable
data class InvoiceDto(
    val id: String,
    val invoiceNumber: String? = null,
    val invoiceDate: String? = null,
    val outletName: String? = null,
    val distributorName: String? = null,
    val items: List<InvoiceItemDto> = emptyList(),
    val quantity: Double? = null,
    val totalAmount: Double? = null,
    val status: String? = null,
    val photoUrl: String? = null,
    val uploadDate: String? = null
)

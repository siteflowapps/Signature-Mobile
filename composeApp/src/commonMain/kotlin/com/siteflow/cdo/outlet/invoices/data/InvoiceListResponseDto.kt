package com.siteflow.cdo.outlet.invoices.data

import kotlinx.serialization.Serializable

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

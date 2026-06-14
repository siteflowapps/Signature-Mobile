package com.siteflow.signature.outlet.invoices.data

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

/**
 * Response wrapper for GET /invoices/{id} (single invoice). The detail
 * response is a superset of [InvoiceDto]; extra fields are ignored.
 */
@Serializable
data class InvoiceDetailResponseDto(
    val success: Boolean = false,
    val data: InvoiceDto? = null,
    val error: String? = null
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
    val uploadDate: String? = null,
    // ── Detail-only fields (present on GET /invoices/{id}/details) ──
    val createdByName: String? = null,
    val createdByRole: String? = null,
    val eventLog: List<InvoiceEventLogDto> = emptyList()
)

/** One status-transition entry from the invoice detail's eventLog. */
@Serializable
data class InvoiceEventLogDto(
    val fromStatus: String? = null,
    val toStatus: String? = null,
    val remarks: String? = null,
    val eventTime: String? = null,
    val performedByName: String? = null,
    val performedByRole: String? = null
)

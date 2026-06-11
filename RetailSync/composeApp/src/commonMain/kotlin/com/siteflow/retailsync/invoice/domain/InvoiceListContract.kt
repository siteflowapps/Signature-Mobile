package com.siteflow.retailsync.invoice.domain

import com.siteflow.retailsync.invoice.data.dto.InvoiceDto

data class InvoiceListState(
    val invoices: List<InvoiceDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val userName: String? = null
)

sealed interface InvoiceListAction {
    data object Load : InvoiceListAction
    data object ScanQr : InvoiceListAction
    data class ViewDetail(val invoiceId: String) : InvoiceListAction
    data object Logout : InvoiceListAction
}

sealed interface InvoiceListEvent {
    data object NavigateToScanner : InvoiceListEvent
    data class NavigateToDetail(val invoiceId: String) : InvoiceListEvent
    data object NavigateToLogin : InvoiceListEvent
}

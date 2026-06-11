package com.siteflow.cdo.ase.invoices.domain

import com.siteflow.cdo.ase.invoices.data.InvoiceItem

/**
 * MVI Contract for the Invoice feature.
 */

sealed interface InvoiceAction {
    data object LoadInvoices : InvoiceAction
    data object LoadMore : InvoiceAction
    data class SearchQueryChanged(val query: String) : InvoiceAction
    data class FilterSelected(val filter: String) : InvoiceAction
    data class LoadInvoiceDetail(val invoiceId: String) : InvoiceAction
    data class ApproveInvoice(val invoiceId: String, val note: String) : InvoiceAction
    data class RejectInvoice(val invoiceId: String, val note: String) : InvoiceAction
}

data class InvoiceState(
    val invoices: List<InvoiceItem> = emptyList(),
    val filteredInvoices: List<InvoiceItem> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "All",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Int = 0,
    val isLastPage: Boolean = true,
    // Detail
    val selectedInvoice: InvoiceItem? = null,
    val periodInvoices: List<InvoiceItem> = emptyList()
)

sealed interface InvoiceEvent {
    data object NavigateBack : InvoiceEvent
    data class ShowToast(val message: String) : InvoiceEvent
}

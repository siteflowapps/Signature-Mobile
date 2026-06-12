package com.siteflow.signature.outlet.invoices.domain

import com.siteflow.signature.cso.invoices.data.InvoiceItem

/**
 * MVI Contract for Outlet Invoice feature.
 * Read-only — no approve/reject actions.
 */

sealed interface OutletInvoiceAction {
    data object LoadInvoices : OutletInvoiceAction
    data object LoadMore : OutletInvoiceAction
    data class SearchQueryChanged(val query: String) : OutletInvoiceAction
    data class FilterSelected(val filter: String) : OutletInvoiceAction
    data class LoadInvoiceDetail(val invoiceId: String) : OutletInvoiceAction
}

data class OutletInvoiceState(
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
    val selectedInvoice: InvoiceItem? = null
)

sealed interface OutletInvoiceEvent {
    data object NavigateBack : OutletInvoiceEvent
}

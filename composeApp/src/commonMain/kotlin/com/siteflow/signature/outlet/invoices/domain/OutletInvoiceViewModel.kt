package com.siteflow.signature.outlet.invoices.domain

import com.siteflow.signature.cso.invoices.data.InvoiceItem
import com.siteflow.signature.cso.invoices.data.InvoiceStatus
import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.outlet.invoices.data.InvoiceApi
import com.siteflow.signature.outlet.invoices.data.toInvoiceItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for Outlet Invoice list & detail.
 * Read-only view — no approve/reject actions.
 */
class OutletInvoiceViewModel(
    private val invoiceApi: InvoiceApi,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<OutletInvoiceState, OutletInvoiceAction, OutletInvoiceEvent>(
    initialState = OutletInvoiceState()
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun onAction(action: OutletInvoiceAction) {
        when (action) {
            OutletInvoiceAction.LoadInvoices -> loadInvoices()
            OutletInvoiceAction.LoadMore -> loadNextPage()
            is OutletInvoiceAction.SearchQueryChanged -> onSearchChanged(action.query)
            is OutletInvoiceAction.FilterSelected -> onFilterSelected(action.filter)
            is OutletInvoiceAction.LoadInvoiceDetail -> loadInvoiceDetail(action.invoiceId)
        }
    }

    private fun loadInvoices() {
        updateState { it.copy(isLoading = true, error = null) }
        scope.launch {
            invoiceApi.getInvoices(page = 0, size = PAGE_SIZE)
                .onSuccess { response ->
                    val invoices = response.data?.content?.map { it.toInvoiceItem() } ?: emptyList()
                    val pageData = response.data
                    updateState {
                        it.copy(
                            invoices = invoices,
                            filteredInvoices = applyFilters(invoices, it.searchQuery, it.selectedFilter),
                            isLoading = false,
                            error = null,
                            currentPage = pageData?.page ?: 0,
                            totalPages = pageData?.totalPages ?: 0,
                            totalElements = pageData?.totalElements ?: 0,
                            isLastPage = pageData?.last ?: true
                        )
                    }
                    analytics.track(AnalyticsEvent.OutletEvent.InvoiceListViewed(
                        invoiceCount = invoices.size,
                        initialFilter = state.value.selectedFilter
                    ))
                }
                .onError { error ->
                    println("[OutletInvoiceVM] API error: ${error.message}")
                    updateState { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    private fun loadNextPage() {
        val current = state.value
        if (current.isLastPage || current.isLoadingMore || current.isLoading) return

        updateState { it.copy(isLoadingMore = true) }
        scope.launch {
            val nextPage = current.currentPage + 1
            invoiceApi.getInvoices(page = nextPage, size = PAGE_SIZE)
                .onSuccess { response ->
                    val newInvoices = response.data?.content?.map { it.toInvoiceItem() } ?: emptyList()
                    val pageData = response.data
                    // Ensure loading indicator is visible for at least 300ms
                    kotlinx.coroutines.delay(300)
                    updateState {
                        val allInvoices = (it.invoices + newInvoices).distinctBy { inv -> inv.id }
                        it.copy(
                            invoices = allInvoices,
                            filteredInvoices = applyFilters(allInvoices, it.searchQuery, it.selectedFilter),
                            isLoadingMore = false,
                            currentPage = pageData?.page ?: nextPage,
                            totalPages = pageData?.totalPages ?: 0,
                            totalElements = pageData?.totalElements ?: 0,
                            isLastPage = pageData?.last ?: true
                        )
                    }
                }
                .onError { error ->
                    updateState { it.copy(isLoadingMore = false) }
                }
        }
    }

    private fun loadInvoiceDetail(invoiceId: String) {
        // Instant paint from the list, then refresh with GET /invoices/{id}.
        val cached = state.value.invoices.firstOrNull { it.id == invoiceId }
        updateState { it.copy(selectedInvoice = cached) }
        scope.launch {
            invoiceApi.getInvoiceDetail(invoiceId)
                .onSuccess { resp ->
                    resp.data?.toInvoiceItem()?.let { fresh ->
                        updateState { it.copy(selectedInvoice = fresh) }
                    }
                }
                .onError { /* keep cached item on failure */ }
        }
        if (cached != null) {
            analytics.track(AnalyticsEvent.OutletEvent.InvoiceDetailViewed(
                invoiceId = invoiceId,
                invoiceStatus = cached.status.name
            ))
        }
    }

    private fun onSearchChanged(query: String) {
        val filtered = applyFilters(state.value.invoices, query, state.value.selectedFilter)
        if (query.isNotBlank()) {
            analytics.track(AnalyticsEvent.OutletEvent.InvoiceSearchUsed(
                queryLength = query.length,
                resultsCount = filtered.size
            ))
        }
        updateState { current ->
            current.copy(
                searchQuery = query,
                filteredInvoices = filtered
            )
        }
    }

    private fun onFilterSelected(filter: String) {
        analytics.track(AnalyticsEvent.OutletEvent.InvoiceListFilterSelected(filter = filter))
        updateState { current ->
            current.copy(
                selectedFilter = filter,
                filteredInvoices = applyFilters(current.invoices, current.searchQuery, filter)
            )
        }
    }

    fun trackListScrolled() {
        analytics.track(AnalyticsEvent.OutletEvent.InvoiceListScrolled)
    }

    private fun applyFilters(
        invoices: List<InvoiceItem>,
        query: String,
        filter: String
    ): List<InvoiceItem> {
        return invoices.filter { invoice ->
            val matchesSearch = query.isBlank() ||
                    invoice.outletName.contains(query, ignoreCase = true) ||
                    invoice.id.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "Pending" -> invoice.status == InvoiceStatus.SUBMITTED || invoice.status == InvoiceStatus.ASE_APPROVED || invoice.status == InvoiceStatus.ASM_APPROVED || invoice.status == InvoiceStatus.PENDING
                "Approved" -> invoice.status == InvoiceStatus.FINANCE_APPROVED || invoice.status == InvoiceStatus.PAID || invoice.status == InvoiceStatus.APPROVED
                "Rejected" -> invoice.status == InvoiceStatus.REJECTED
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }
}

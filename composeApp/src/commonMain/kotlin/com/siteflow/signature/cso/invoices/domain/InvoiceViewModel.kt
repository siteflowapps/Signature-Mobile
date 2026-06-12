package com.siteflow.signature.cso.invoices.domain

import com.siteflow.signature.cso.invoices.data.InvoiceItem
import com.siteflow.signature.cso.invoices.data.InvoiceStatus
import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.signature.outlet.invoices.data.InvoiceApi
import com.siteflow.signature.outlet.invoices.data.toInvoiceItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InvoiceViewModel(
    private val invoiceApi: InvoiceApi,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<InvoiceState, InvoiceAction, InvoiceEvent>(InvoiceState()) {

    private val scope = CoroutineScope(Dispatchers.Default)

    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun onAction(action: InvoiceAction) {
        when (action) {
            is InvoiceAction.LoadInvoices -> loadInvoices()
            is InvoiceAction.LoadMore -> loadNextPage()
            is InvoiceAction.SearchQueryChanged -> onSearchChanged(action.query)
            is InvoiceAction.FilterSelected -> onFilterSelected(action.filter)
            is InvoiceAction.LoadInvoiceDetail -> loadInvoiceDetail(action.invoiceId)
            is InvoiceAction.ApproveInvoice -> approveInvoice(action.invoiceId, action.note)
            is InvoiceAction.RejectInvoice -> rejectInvoice(action.invoiceId, action.note)
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
                    analytics.track(AnalyticsEvent.ASEEvent.InvoiceListViewed(invoiceCount = invoices.size))
                }
                .onError { error ->
                    println("[InvoiceVM] API error: ${error.message}")
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
        val invoice = state.value.invoices.firstOrNull { it.id == invoiceId }

        val allInvoices = state.value.invoices
        val periodInvoices = if (invoice != null) {
            allInvoices.filter {
                it.outletName == invoice.outletName && it.invoicePeriod == invoice.invoicePeriod
            }
        } else emptyList()

        updateState { it.copy(selectedInvoice = invoice, periodInvoices = periodInvoices) }
        if (invoice != null) {
            analytics.track(AnalyticsEvent.ASEEvent.InvoiceDetailViewed(
                invoiceId = invoiceId,
                invoiceStatus = invoice.status.name,
                outletName = invoice.outletName
            ))
        }
    }

    private fun approveInvoice(invoiceId: String, note: String) {
        val reviewNote = note.ifBlank { "Verified and approved by ASE." }
        analytics.track(AnalyticsEvent.ASEEvent.InvoiceApproved(invoiceId = invoiceId, hasNote = note.isNotBlank()))
        updateState { it.copy(isLoading = true) }
        scope.launch {
            invoiceApi.approveInvoice(invoiceId, reviewNote)
                .onSuccess {
                    GlobalToastHandler.showSuccess("Invoice approved successfully")
                    analytics.track(AnalyticsEvent.ASEEvent.InvoiceActionSuccess(invoiceId = invoiceId, action = "approve"))
                    loadInvoices() // Refresh list
                    emitEvent(InvoiceEvent.NavigateBack)
                }
                .onError { error ->
                    println("[InvoiceVM] Approve error: ${error.message}")
                    GlobalToastHandler.showError("Failed to approve: ${error.message}")
                    analytics.track(AnalyticsEvent.ASEEvent.InvoiceActionFailed(invoiceId = invoiceId, errorMessage = error.message, action = "approve"))
                    updateState { it.copy(isLoading = false) }
                }
        }
    }

    private fun rejectInvoice(invoiceId: String, note: String) {
        val reviewNote = note.ifBlank { "Rejected by ASE." }
        analytics.track(AnalyticsEvent.ASEEvent.InvoiceRejected(invoiceId = invoiceId, hasNote = note.isNotBlank(), rejectionReasonLength = note.length))
        updateState { it.copy(isLoading = true) }
        scope.launch {
            invoiceApi.rejectInvoice(invoiceId, reviewNote)
                .onSuccess {
                    GlobalToastHandler.showSuccess("Invoice rejected")
                    analytics.track(AnalyticsEvent.ASEEvent.InvoiceActionSuccess(invoiceId = invoiceId, action = "reject"))
                    loadInvoices() // Refresh list
                    emitEvent(InvoiceEvent.NavigateBack)
                }
                .onError { error ->
                    println("[InvoiceVM] Reject error: ${error.message}")
                    GlobalToastHandler.showError("Failed to reject: ${error.message}")
                    analytics.track(AnalyticsEvent.ASEEvent.InvoiceActionFailed(invoiceId = invoiceId, errorMessage = error.message, action = "reject"))
                    updateState { it.copy(isLoading = false) }
                }
        }
    }

    private fun onSearchChanged(query: String) {
        val filtered = applyFilters(state.value.invoices, query, state.value.selectedFilter)
        updateState { current ->
            current.copy(
                searchQuery = query,
                filteredInvoices = filtered
            )
        }
        if (query.isNotBlank()) {
            analytics.track(AnalyticsEvent.ASEEvent.InvoiceSearchUsed(
                queryLength = query.length,
                resultsCount = filtered.size
            ))
        }
    }

    private fun onFilterSelected(filter: String) {
        updateState { current ->
            current.copy(
                selectedFilter = filter,
                filteredInvoices = applyFilters(current.invoices, current.searchQuery, filter)
            )
        }
        analytics.track(AnalyticsEvent.ASEEvent.InvoiceFilterSelected(filter = filter))
    }

    private fun applyFilters(
        invoices: List<InvoiceItem>,
        query: String,
        filter: String
    ): List<InvoiceItem> {
        return invoices.filter { invoice ->
            val matchesSearch = query.isBlank() ||
                    invoice.outletName.contains(query, ignoreCase = true) ||
                    invoice.location.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "Pending" -> invoice.status == InvoiceStatus.SUBMITTED || invoice.status == InvoiceStatus.PENDING
                "Approved" -> invoice.status == InvoiceStatus.APPROVED || invoice.status == InvoiceStatus.ASE_APPROVED || invoice.status == InvoiceStatus.ASM_APPROVED || invoice.status == InvoiceStatus.FINANCE_APPROVED
                "Rejected" -> invoice.status == InvoiceStatus.REJECTED
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }
}

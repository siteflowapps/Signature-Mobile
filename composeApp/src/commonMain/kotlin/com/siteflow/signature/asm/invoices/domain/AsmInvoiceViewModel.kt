package com.siteflow.signature.asm.invoices.domain

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
import com.siteflow.signature.shared.data.ApprovalLevel
import com.siteflow.signature.shared.data.ApprovalStepStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * MVI Contract for ASM Invoice module.
 */

sealed interface AsmInvoiceAction {
    data object LoadInvoices : AsmInvoiceAction
    data object LoadMore : AsmInvoiceAction
    data class ApproveInvoice(val invoiceId: String, val remarks: String = "") : AsmInvoiceAction
    data class RejectInvoice(val invoiceId: String, val remarks: String) : AsmInvoiceAction
    data class LoadInvoiceDetail(val invoiceId: String) : AsmInvoiceAction
}

data class AsmInvoiceState(
    val invoices: List<InvoiceItem> = emptyList(),
    val filteredInvoices: List<InvoiceItem> = emptyList(),
    val selectedInvoice: InvoiceItem? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Int = 0,
    val isLastPage: Boolean = true
)

sealed interface AsmInvoiceEvent {
    data class ShowMessage(val message: String) : AsmInvoiceEvent
    data object NavigateBack : AsmInvoiceEvent
}

/**
 * ASM Invoice ViewModel.
 *
 * Shows invoices fetched from API.
 */
class AsmInvoiceViewModel(
    private val invoiceApi: InvoiceApi,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<AsmInvoiceState, AsmInvoiceAction, AsmInvoiceEvent>(
    AsmInvoiceState()
) {

    private val scope = CoroutineScope(Dispatchers.Default)

    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun onAction(action: AsmInvoiceAction) {
        when (action) {
            AsmInvoiceAction.LoadInvoices -> loadInvoices()
            AsmInvoiceAction.LoadMore -> loadNextPage()
            is AsmInvoiceAction.ApproveInvoice -> approveInvoice(action.invoiceId, action.remarks)
            is AsmInvoiceAction.RejectInvoice -> rejectInvoice(action.invoiceId, action.remarks)
            is AsmInvoiceAction.LoadInvoiceDetail -> loadInvoiceDetail(action.invoiceId)
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
                            filteredInvoices = invoices,
                            isLoading = false,
                            error = null,
                            currentPage = pageData?.page ?: 0,
                            totalPages = pageData?.totalPages ?: 0,
                            totalElements = pageData?.totalElements ?: 0,
                            isLastPage = pageData?.last ?: true
                        )
                    }
                    analytics.track(AnalyticsEvent.ASMEvent.InvoiceListViewed(invoiceCount = invoices.size))
                }
                .onError { error ->
                    println("[AsmInvoiceVM] API error: ${error.message}")
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
                            filteredInvoices = allInvoices,
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
        updateState { it.copy(selectedInvoice = invoice) }
        if (invoice != null) {
            analytics.track(AnalyticsEvent.ASMEvent.InvoiceDetailViewed(
                invoiceId = invoiceId,
                invoiceStatus = invoice.status.name,
                submittedByAse = invoice.outletName
            ))
        }
    }

    private fun approveInvoice(invoiceId: String, remarks: String) {
        val reviewNote = remarks.ifBlank { "Approved by area manager" }
        analytics.track(AnalyticsEvent.ASMEvent.InvoiceApproved(invoiceId = invoiceId, hasNote = remarks.isNotBlank()))
        updateState { it.copy(isLoading = true) }
        scope.launch {
            invoiceApi.approveInvoice(invoiceId, reviewNote)
                .onSuccess {
                    GlobalToastHandler.showSuccess("Invoice approved successfully")
                    loadInvoices()
                    emitEvent(AsmInvoiceEvent.NavigateBack)
                }
                .onError { error ->
                    println("[AsmInvoiceVM] Approve error: ${error.message}")
                    GlobalToastHandler.showError("Failed to approve: ${error.message}")
                    updateState { it.copy(isLoading = false) }
                }
        }
    }

    private fun rejectInvoice(invoiceId: String, remarks: String) {
        analytics.track(AnalyticsEvent.ASMEvent.InvoiceRejected(invoiceId = invoiceId, hasNote = remarks.isNotBlank()))
        updateState { it.copy(isLoading = true) }
        scope.launch {
            invoiceApi.rejectInvoice(invoiceId, remarks)
                .onSuccess {
                    GlobalToastHandler.showSuccess("Invoice rejected")
                    loadInvoices()
                    emitEvent(AsmInvoiceEvent.NavigateBack)
                }
                .onError { error ->
                    println("[AsmInvoiceVM] Reject error: ${error.message}")
                    GlobalToastHandler.showError("Failed to reject: ${error.message}")
                    updateState { it.copy(isLoading = false) }
                }
        }
    }
}


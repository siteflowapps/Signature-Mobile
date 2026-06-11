package com.siteflow.retailsync.invoice.domain

import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.core.domain.AuthRepository
import com.siteflow.retailsync.core.presentation.BaseViewModel
import com.siteflow.retailsync.invoice.data.InvoiceRepository
import com.siteflow.retailsync.outlet.data.OutletRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class InvoiceListViewModel(
    private val scope: CoroutineScope,
    private val invoiceRepository: InvoiceRepository,
    private val authRepository: AuthRepository,
    private val outletRepository: OutletRepository
) : BaseViewModel<InvoiceListState, InvoiceListAction, InvoiceListEvent>(InvoiceListState()) {

    init {
        onAction(InvoiceListAction.Load)
    }

    override fun onAction(action: InvoiceListAction) {
        when (action) {
            InvoiceListAction.Load -> loadInvoices()
            InvoiceListAction.ScanQr -> emitEvent(InvoiceListEvent.NavigateToScanner)
            is InvoiceListAction.ViewDetail -> emitEvent(InvoiceListEvent.NavigateToDetail(action.invoiceId))
            InvoiceListAction.Logout -> logout()
        }
    }

    private fun loadInvoices() {
        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            try {
                // Fetch outlets in background (cache for QR validation)
                launch { outletRepository.fetchOutlets() }

                val userName = authRepository.getUserName()

                when (val result = invoiceRepository.getInvoices()) {
                    is NetworkResult.Success -> {
                        updateState {
                            it.copy(
                                invoices = result.data,
                                isLoading = false,
                                userName = userName
                            )
                        }
                    }
                    is NetworkResult.Error -> {
                        updateState {
                            it.copy(
                                isLoading = false,
                                error = result.error.message,
                                userName = userName
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                updateState {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load invoices"
                    )
                }
            }
        }
    }

    private fun logout() {
        scope.launch {
            authRepository.logout()
            emitEvent(InvoiceListEvent.NavigateToLogin)
        }
    }
}

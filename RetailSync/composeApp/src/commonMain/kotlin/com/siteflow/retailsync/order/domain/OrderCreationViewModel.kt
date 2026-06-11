package com.siteflow.retailsync.order.domain

import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.core.domain.AuthRepository
import com.siteflow.retailsync.core.presentation.BaseViewModel
import com.siteflow.retailsync.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.retailsync.invoice.data.InvoiceRepository
import com.siteflow.retailsync.invoice.data.dto.InvoiceItemDto
import com.siteflow.retailsync.invoice.data.dto.InvoiceUploadRequestDto
import com.siteflow.retailsync.order.data.SkuRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class OrderCreationViewModel(
    private val scope: CoroutineScope,
    private val skuRepository: SkuRepository,
    private val invoiceRepository: InvoiceRepository,
    private val authRepository: AuthRepository
) : BaseViewModel<OrderCreationState, OrderCreationAction, OrderCreationEvent>(OrderCreationState()) {

    fun setOutletInfo(outletId: String, outletName: String) {
        updateState { it.copy(outletId = outletId, outletName = outletName) }
        onAction(OrderCreationAction.LoadSkus)
    }

    override fun onAction(action: OrderCreationAction) {
        when (action) {
            OrderCreationAction.LoadSkus -> loadSkus()
            is OrderCreationAction.AddItem -> addItem(action.skuId)
            is OrderCreationAction.RemoveItem -> removeItem(action.skuId)
            is OrderCreationAction.SearchChanged -> updateState { it.copy(searchQuery = action.query) }
            OrderCreationAction.Submit -> submitOrder()
            OrderCreationAction.Cancel -> emitEvent(OrderCreationEvent.NavigateBack)
        }
    }

    private fun loadSkus() {
        scope.launch {
            updateState { it.copy(isLoading = true) }
            try {
                val skus = skuRepository.getMasterSkus()
                updateState { it.copy(allSkus = skus, isLoading = false) }
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun addItem(skuId: String) {
        val sku = state.value.allSkus.find { it.id == skuId } ?: return
        val currentItems = state.value.selectedItems.toMutableMap()
        val existing = currentItems[skuId]
        currentItems[skuId] = OrderItem(sku, (existing?.quantity ?: 0) + 1)
        updateState { it.copy(selectedItems = currentItems) }
    }

    private fun removeItem(skuId: String) {
        val currentItems = state.value.selectedItems.toMutableMap()
        val existing = currentItems[skuId] ?: return
        if (existing.quantity <= 1) {
            currentItems.remove(skuId)
        } else {
            currentItems[skuId] = existing.copy(quantity = existing.quantity - 1)
        }
        updateState { it.copy(selectedItems = currentItems) }
    }

    private fun submitOrder() {
        if (state.value.selectedItems.isEmpty()) {
            GlobalToastHandler.showError("Please add at least one item")
            return
        }

        scope.launch {
            updateState { it.copy(isSubmitting = true) }

            val currentState = state.value

            // Get distributorId from saved user profile
            val distributorId = authRepository.getDistributorId() ?: ""
            val userName = authRepository.getUserName() ?: ""

            // Generate dynamic invoice number & date
            val timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
            val invoiceNumber = "INV-${timestamp}-${(1000..9999).random()}"

            // Use ISO date format (YYYY-MM-DD) from epoch
            val epochSeconds = timestamp / 1000
            val invoiceDate = formatEpochToDate(epochSeconds)

            val items = currentState.selectedItems.values.map { orderItem ->
                InvoiceItemDto(
                    skuId = orderItem.sku.id,
                    skuName = orderItem.sku.articleDescription,
                    invoicedSkuName = orderItem.sku.articleDescription,
                    invoicedQuantity = orderItem.quantity,
                    invoicedUnit = "case",
                    invoicedUnitPrice = orderItem.sku.mrpPerCase,
                    invoicedTotalPrice = orderItem.sku.mrpPerCase * orderItem.quantity,
                    matchedSkuName = orderItem.sku.articleDescription,
                    caseConfiguration = orderItem.sku.caseConfiguration,
                    mrpPerCase = orderItem.sku.mrpPerCase,
                    mrpPerBottle = orderItem.sku.mrpPerBottle,
                    finalQuantity = orderItem.quantity,
                    finalUnit = "case",
                    category = orderItem.sku.category
                )
            }

            val requestDto = InvoiceUploadRequestDto(
                outletId = currentState.outletId,
                distributorId = distributorId,
                invoiceNumber = invoiceNumber,
                invoiceDate = invoiceDate,
                items = items,
                distributorName = userName,
                retailerName = currentState.outletName,
                totalInvoiceAmount = currentState.totalAmount.toString()
            )

            when (val result = invoiceRepository.createInvoice(requestDto)) {
                is NetworkResult.Success -> {
                    val invoiceId = result.data.data?.id ?: result.data.data?.invoiceNumber ?: invoiceNumber
                    updateState { it.copy(isSubmitting = false) }
                    GlobalToastHandler.showSuccess("Invoice created successfully!")
                    emitEvent(OrderCreationEvent.NavigateToSuccess(invoiceId))
                }
                is NetworkResult.Error -> {
                    updateState { it.copy(isSubmitting = false) }
                    GlobalToastHandler.showError(result.error.message)
                }
            }
        }
    }

    /**
     * Simple epoch-to-date formatter without kotlinx-datetime LocalDateTime.
     * Produces YYYY-MM-DD format.
     */
    private fun formatEpochToDate(epochSeconds: Long): String {
        // Days since epoch
        var days = epochSeconds / 86400
        var year = 1970
        while (true) {
            val daysInYear = if (isLeapYear(year)) 366 else 365
            if (days < daysInYear) break
            days -= daysInYear
            year++
        }
        val monthDays = if (isLeapYear(year))
            intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        else
            intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        var month = 0
        while (month < 12 && days >= monthDays[month]) {
            days -= monthDays[month]
            month++
        }
        val day = days + 1
        month += 1
        return "${year}-${month.toString().padStart(2, '0')}-${day.toInt().toString().padStart(2, '0')}"
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}

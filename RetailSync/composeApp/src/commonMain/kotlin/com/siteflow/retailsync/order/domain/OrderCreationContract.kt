package com.siteflow.retailsync.order.domain

import com.siteflow.retailsync.order.data.dto.SkuDto

data class OrderItem(
    val sku: SkuDto,
    val quantity: Int = 0
)

data class OrderCreationState(
    val outletId: String = "",
    val outletName: String = "",
    val allSkus: List<SkuDto> = emptyList(),
    val selectedItems: Map<String, OrderItem> = emptyMap(), // skuId → OrderItem
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
) {
    val filteredSkus: List<SkuDto>
        get() = if (searchQuery.isBlank()) allSkus
        else allSkus.filter {
            it.articleDescription.contains(searchQuery, ignoreCase = true) ||
                    it.brand.contains(searchQuery, ignoreCase = true) ||
                    it.flavor.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }

    val totalItems: Int get() = selectedItems.values.sumOf { it.quantity }
    val totalAmount: Double get() = selectedItems.values.sumOf { it.sku.mrpPerCase * it.quantity }
}

sealed interface OrderCreationAction {
    data object LoadSkus : OrderCreationAction
    data class AddItem(val skuId: String) : OrderCreationAction
    data class RemoveItem(val skuId: String) : OrderCreationAction
    data class SearchChanged(val query: String) : OrderCreationAction
    data object Submit : OrderCreationAction
    data object Cancel : OrderCreationAction
}

sealed interface OrderCreationEvent {
    data class NavigateToSuccess(val orderId: String) : OrderCreationEvent
    data object NavigateBack : OrderCreationEvent
}

package com.siteflow.signature.cso.dashboard.domain

import com.siteflow.signature.cso.dashboard.data.OutletItem

sealed interface OutletDetailAction {
    data class LoadOutlet(val name: String) : OutletDetailAction
    data class PhotoCaptured(val slotId: String, val imagePath: String) : OutletDetailAction
    data class PhotoRemoved(val slotId: String) : OutletDetailAction
    data class RequestAsset(
        val outletId: String,
        val coolerType: String,
        val capacity: String,
        val signageType: String,
        val dmsId: String
    ) : OutletDetailAction
}

data class OutletDetailState(
    val outlet: OutletItem? = null,
    val isLoading: Boolean = false,
    val isRequestingAsset: Boolean = false
)

sealed interface OutletDetailEvent {
    data object NavigateBack : OutletDetailEvent
    data object AssetRequestSuccess : OutletDetailEvent
}

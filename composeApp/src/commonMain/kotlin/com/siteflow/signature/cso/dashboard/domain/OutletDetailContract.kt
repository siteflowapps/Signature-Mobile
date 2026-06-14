package com.siteflow.signature.cso.dashboard.domain

import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.onboarding.data.dto.MarketingItemDto

sealed interface OutletDetailAction {
    data class LoadById(val outletId: String) : OutletDetailAction
    data class PhotoCaptured(val slotId: String, val imagePath: String) : OutletDetailAction
    data class PhotoRemoved(val slotId: String) : OutletDetailAction
    data class RequestCooler(
        val outletId: String,
        val coolerSize: String,   // backend CoolerSize enum, e.g. "SIZE_300L"
        val quantity: Int,
        val details: String
    ) : OutletDetailAction

    /** CSO raise a marketing/branding request with one or more asset items. */
    data class RequestMarketing(
        val outletId: String,
        val items: List<MarketingItemDto>,
        val details: String
    ) : OutletDetailAction

    /** CSO/ASE upload a compliance photo for an installed asset (kind = COOLER|MARKETING). */
    data class UploadCompliance(
        val outletId: String,
        val kind: String,
        val imagePath: String
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

package com.siteflow.cdo.ase.dashboard.domain

import com.siteflow.cdo.ase.dashboard.data.AssetStatus
import com.siteflow.cdo.ase.dashboard.data.OutletItem
import com.siteflow.cdo.ase.dashboard.data.mockOutlets
import com.siteflow.cdo.ase.onboarding.data.OutletRepository
import com.siteflow.cdo.core.analytics.AnalyticsEvent
import com.siteflow.cdo.core.analytics.AnalyticsTracker
import com.siteflow.cdo.core.data.networking.result.onError
import com.siteflow.cdo.core.data.networking.result.onSuccess
import com.siteflow.cdo.core.presentation.BaseViewModel
import com.siteflow.cdo.core.presentation.components.toast.GlobalToastHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class OutletDetailViewModel(
    private val scope: CoroutineScope,
    private val outletRepository: OutletRepository,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<OutletDetailState, OutletDetailAction, OutletDetailEvent>(OutletDetailState()) {

    override fun onAction(action: OutletDetailAction) {
        when (action) {
            is OutletDetailAction.LoadOutlet -> loadOutlet(action.name)
            is OutletDetailAction.PhotoCaptured -> handlePhotoCaptured(action.slotId, action.imagePath)
            is OutletDetailAction.PhotoRemoved -> handlePhotoRemoved(action.slotId)
            is OutletDetailAction.RequestAsset -> requestAsset(action)
        }
    }

    /** Sync an outlet into this ViewModel from an external source (e.g. the dashboard). */
    fun updateOutlet(outlet: OutletItem) {
        updateState { it.copy(outlet = outlet, isLoading = false) }
        analytics.track(AnalyticsEvent.ASEEvent.OutletDetailViewed(outletId = outlet.id))
    }

    private fun loadOutlet(name: String) {
        updateState { it.copy(isLoading = true) }
        // TODO: Replace with real API call when backend is ready
        val outlet = mockOutlets.firstOrNull { it.name == name }
        updateState { it.copy(outlet = outlet, isLoading = false) }
    }

    private fun requestAsset(action: OutletDetailAction.RequestAsset) {
        analytics.track(AnalyticsEvent.ASEEvent.AssetRequestTapped(
            outletId = action.outletId,
            coolerType = action.coolerType,
            capacity = action.capacity,
            signageType = action.signageType
        ))
        scope.launch {
            updateState { it.copy(isRequestingAsset = true) }

            outletRepository.requestAsset(
                outletId = action.outletId,
                coolerType = action.coolerType,
                capacity = action.capacity,
                signageType = action.signageType,
                dmsId = action.dmsId
            )
                .onSuccess {
                    updateState { it.copy(isRequestingAsset = false) }
                    GlobalToastHandler.showSuccess("Asset request submitted")
                    val currentOutlet = state.value.outlet
                    if (currentOutlet != null) {
                        updateState {
                            it.copy(outlet = currentOutlet.copy(assetStatus = AssetStatus.REQUESTED))
                        }
                    }
                    analytics.track(AnalyticsEvent.ASEEvent.AssetRequestSuccess(outletId = action.outletId))
                    emitEvent(OutletDetailEvent.AssetRequestSuccess)
                }
                .onError { error ->
                    updateState { it.copy(isRequestingAsset = false) }
                    GlobalToastHandler.showError(error.message)
                    analytics.track(
                        AnalyticsEvent.ASEEvent.AssetRequestFailed(
                            outletId = action.outletId,
                            errorMessage = error.message
                        )
                    )
                }
        }
    }

    private fun handlePhotoCaptured(slotId: String, imagePath: String) {
        val currentOutlet = state.value.outlet ?: return
        
        val updatedPhotos = currentOutlet.cdoVerificationPhotos.toMutableList()
        val index = updatedPhotos.indexOfFirst { it.id == slotId }
        
        if (index != -1) {
            updatedPhotos[index] = updatedPhotos[index].copy(imagePath = imagePath)
        } else if (slotId == "add_new") {
            val newId = "cdo_photo_${updatedPhotos.size + 1}"
            updatedPhotos.add(
                com.siteflow.cdo.ase.onboarding.data.PhotoSlot(
                    id = newId,
                    label = "Verification Photo ${updatedPhotos.size + 1}",
                    required = false,
                    imagePath = imagePath
                )
            )
        }
        
        updateState { it.copy(outlet = currentOutlet.copy(cdoVerificationPhotos = updatedPhotos)) }
    }

    private fun handlePhotoRemoved(slotId: String) {
        val currentOutlet = state.value.outlet ?: return
        val updatedPhotos = currentOutlet.cdoVerificationPhotos.filter { it.id != slotId }
        updateState { it.copy(outlet = currentOutlet.copy(cdoVerificationPhotos = updatedPhotos)) }
    }
}

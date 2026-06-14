package com.siteflow.signature.cso.dashboard.domain

import com.siteflow.signature.cso.dashboard.data.AssetStatus
import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.onboarding.data.OutletRepository
import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.core.presentation.components.toast.GlobalToastHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class OutletDetailViewModel(
    private val scope: CoroutineScope,
    private val outletRepository: OutletRepository,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<OutletDetailState, OutletDetailAction, OutletDetailEvent>(OutletDetailState()) {

    override fun onAction(action: OutletDetailAction) {
        when (action) {
            is OutletDetailAction.LoadById -> loadById(action.outletId)
            is OutletDetailAction.PhotoCaptured -> handlePhotoCaptured(action.slotId, action.imagePath)
            is OutletDetailAction.PhotoRemoved -> handlePhotoRemoved(action.slotId)
            is OutletDetailAction.RequestCooler -> requestCooler(action)
            is OutletDetailAction.RequestMarketing -> requestMarketing(action)
            is OutletDetailAction.UploadCompliance ->
                uploadCompliance(action.outletId, action.kind, action.imagePath)
        }
    }

    private fun uploadCompliance(outletId: String, kind: String, imagePath: String) {
        scope.launch {
            updateState { it.copy(isRequestingAsset = true) }
            val bytes = com.siteflow.signature.core.util.ImageEncoder.encodeToBytes(imagePath)
            if (bytes == null) {
                updateState { it.copy(isRequestingAsset = false) }
                GlobalToastHandler.showError("Couldn't read the selected image")
                return@launch
            }
            outletRepository.uploadAssetCompliance(outletId, kind, bytes)
                .onSuccess {
                    updateState { it.copy(isRequestingAsset = false) }
                    val label = if (kind == "COOLER") "Cooler" else "Branding"
                    GlobalToastHandler.showSuccess("$label compliance submitted")
                    loadById(outletId)
                }
                .onError { error ->
                    updateState { it.copy(isRequestingAsset = false) }
                    GlobalToastHandler.showError(error.message ?: "Compliance upload failed")
                }
        }
    }

    private fun requestMarketing(action: OutletDetailAction.RequestMarketing) {
        scope.launch {
            updateState { it.copy(isRequestingAsset = true) }
            outletRepository.requestMarketing(
                outletId = action.outletId,
                items = action.items,
                details = action.details
            )
                .onSuccess {
                    updateState { it.copy(isRequestingAsset = false) }
                    GlobalToastHandler.showSuccess("Branding request submitted")
                    val currentOutlet = state.value.outlet
                    if (currentOutlet != null) {
                        updateState {
                            it.copy(outlet = currentOutlet.copy(marketingComplianceStatus = "REQUESTED"))
                        }
                    }
                    emitEvent(OutletDetailEvent.AssetRequestSuccess)
                }
                .onError { error ->
                    updateState { it.copy(isRequestingAsset = false) }
                    GlobalToastHandler.showError(error.message)
                }
        }
    }

    /** Sync an outlet into this ViewModel from an external source (e.g. the dashboard). */
    fun updateOutlet(outlet: OutletItem) {
        updateState { it.copy(outlet = outlet, isLoading = false) }
        analytics.track(AnalyticsEvent.ASEEvent.OutletDetailViewed(outletId = outlet.id))
    }

    /** Fetch the full outlet detail from GET /outlets/{id} (fresh, not the list item). */
    private fun loadById(outletId: String) {
        // Guard against an empty id (e.g. a stale recomposition during back-nav),
        // which would hit GET /outlets/ and 404.
        if (outletId.isBlank()) return
        scope.launch {
            updateState { it.copy(isLoading = true) }
            outletRepository.getOutletById(outletId)
                .onSuccess { data ->
                    updateState { it.copy(outlet = OutletItem.fromDto(data), isLoading = false) }
                    analytics.track(AnalyticsEvent.ASEEvent.OutletDetailViewed(outletId = outletId))
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false) }
                    GlobalToastHandler.showError(error.message)
                }
        }
    }

    private fun requestCooler(action: OutletDetailAction.RequestCooler) {
        analytics.track(AnalyticsEvent.ASEEvent.AssetRequestTapped(
            outletId = action.outletId,
            coolerType = "COOLER",
            capacity = action.coolerSize,
            signageType = ""
        ))
        scope.launch {
            updateState { it.copy(isRequestingAsset = true) }

            outletRepository.requestCooler(
                outletId = action.outletId,
                coolerSize = action.coolerSize,
                quantity = action.quantity,
                details = action.details
            )
                .onSuccess {
                    updateState { it.copy(isRequestingAsset = false) }
                    GlobalToastHandler.showSuccess("Cooler request submitted")
                    val currentOutlet = state.value.outlet
                    if (currentOutlet != null) {
                        updateState {
                            it.copy(outlet = currentOutlet.copy(coolerRequested = true))
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
        
        val updatedPhotos = currentOutlet.signatureVerificationPhotos.toMutableList()
        val index = updatedPhotos.indexOfFirst { it.id == slotId }
        
        if (index != -1) {
            updatedPhotos[index] = updatedPhotos[index].copy(imagePath = imagePath)
        } else if (slotId == "add_new") {
            val newId = "signature_photo_${updatedPhotos.size + 1}"
            updatedPhotos.add(
                com.siteflow.signature.cso.onboarding.data.PhotoSlot(
                    id = newId,
                    label = "Verification Photo ${updatedPhotos.size + 1}",
                    required = false,
                    imagePath = imagePath
                )
            )
        }
        
        updateState { it.copy(outlet = currentOutlet.copy(signatureVerificationPhotos = updatedPhotos)) }
    }

    private fun handlePhotoRemoved(slotId: String) {
        val currentOutlet = state.value.outlet ?: return
        val updatedPhotos = currentOutlet.signatureVerificationPhotos.filter { it.id != slotId }
        updateState { it.copy(outlet = currentOutlet.copy(signatureVerificationPhotos = updatedPhotos)) }
    }
}

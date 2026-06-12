package com.siteflow.signature.cso.compliance.domain

import com.siteflow.signature.cso.onboarding.data.OutletRepository
import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.domain.ImageProcessor
import com.siteflow.signature.core.domain.LocationService
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.signature.core.domain.nowLabel
import com.siteflow.signature.core.domain.truncate4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ComplianceViewModel(
    private val scope: CoroutineScope,
    private val outletRepository: OutletRepository,
    private val locationService: LocationService,
    private val imageProcessor: ImageProcessor,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<ComplianceState, ComplianceAction, ComplianceEvent>(ComplianceState()) {

    fun setOutletId(outletId: String) {
        updateState { it.copy(outletId = outletId) }
        analytics.track(AnalyticsEvent.ASEEvent.ComplianceScreenViewed(outletId = outletId))
    }

    override fun onAction(action: ComplianceAction) {
        when (action) {
            is ComplianceAction.CoolerInstalledChanged -> updateState { it.copy(coolerInstalled = action.value) }
            is ComplianceAction.SerialNoChanged -> updateState { it.copy(serialNo = action.value) }
            is ComplianceAction.CoolerTypeChanged -> updateState { it.copy(coolerType = action.value) }
            is ComplianceAction.CapacityChanged -> updateState { it.copy(capacity = action.value) }
            is ComplianceAction.SignageInstalledChanged -> updateState { it.copy(signageInstalled = action.value) }
            is ComplianceAction.PhotoCaptured -> handlePhotoCaptured(action.slotId, action.imagePath)
            is ComplianceAction.PhotoRemoved -> handlePhotoRemoved(action.slotId)
            ComplianceAction.Submit -> submit()
        }
    }

    private fun handlePhotoCaptured(slotId: String, imagePath: String) {
        scope.launch {
            val (lat, lng, locError) = fetchCurrentGps()
            if (locError != null) {
                GlobalToastHandler.showError(locError)
                return@launch
            }
            val processedPath = imageProcessor.processImage(
                originalPath = imagePath,
                latitude = lat,
                longitude = lng
            )
            val now = nowLabel()
            val gps = if (lat != null && lng != null) "${truncate4(lat)}, ${truncate4(lng)}" else null
            val updated = state.value.photoSlots.map { slot ->
                if (slot.id == slotId)
                    slot.copy(imagePath = processedPath, capturedAt = now, gpsLabel = gps)
                else slot
            }
            updateState { it.copy(photoSlots = updated) }
            val capturedCount = updated.count { it.imagePath != null }
            analytics.track(AnalyticsEvent.ASEEvent.CompliancePhotoCaptured(slotId = slotId, photosCapturedCount = capturedCount))
        }
    }

    private fun handlePhotoRemoved(slotId: String) {
        val updated = state.value.photoSlots.map { slot ->
            if (slot.id == slotId) slot.copy(imagePath = null) else slot
        }
        updateState { it.copy(photoSlots = updated) }
        analytics.track(AnalyticsEvent.ASEEvent.CompliancePhotoRemoved(slotId = slotId))
    }

    /**
     * Fetches current GPS coordinates at the moment of photo capture.
     * Returns Triple(lat, lng, errorMessage) — errorMessage is non-null when location is unavailable.
     */
    private suspend fun fetchCurrentGps(): Triple<Double?, Double?, String?> =
        suspendCancellableCoroutine { cont ->
            locationService.getCurrentLocation(
                onSuccess = { lat, lng -> cont.resume(Triple(lat, lng, null)) },
                onFailure = { error ->
                    val msg = when {
                        error.contains("DISABLED", ignoreCase = true) ->
                            "Location Services are turned off. Please enable them in Settings to capture photos."
                        error.contains("DENIED", ignoreCase = true) ->
                            "Location permission denied. Please enable Location in Settings to capture photos."
                        else ->
                            "Unable to fetch location. Please enable GPS and try again."
                    }
                    cont.resume(Triple(null, null, msg))
                }
            )
        }

    private fun submit() {
        val s = state.value
        if (!s.isFormValid) return

        fun photoPath(id: String) = s.photoSlots.first { it.id == id }.imagePath!!

        analytics.track(AnalyticsEvent.ASEEvent.ComplianceSubmitted(
            outletId = s.outletId,
            coolerInstalled = s.coolerInstalled,
            signageInstalled = s.signageInstalled,
            photosCount = s.photoSlots.count { it.imagePath != null }
        ))
        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            outletRepository.submitCompliance(
                outletId = s.outletId,
                coolerInstalled = s.coolerInstalled,
                serialNo = s.serialNo,
                coolerType = s.coolerType,
                capacity = s.capacity,
                signageInstalled = s.signageInstalled,
                coolerImagePath = photoPath("coolerImage"),
                assetLabelImagePath = photoPath("assetLabelImage"),
                signageImagePath = photoPath("signageImage"),
                outerOutletImagePath = photoPath("outerOutletImage"),
                innerOutletImagePath = photoPath("innerOutletImage")
            )
                .onSuccess {
                    updateState { it.copy(isLoading = false) }
                    GlobalToastHandler.showSuccess("Compliance submitted successfully")
                    analytics.track(AnalyticsEvent.ASEEvent.ComplianceSubmitSuccess(outletId = s.outletId))
                    emitEvent(ComplianceEvent.SubmitSuccess)
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, error = error.message) }
                    GlobalToastHandler.showError(error.message)
                    analytics.track(AnalyticsEvent.ASEEvent.ComplianceSubmitFailed(outletId = s.outletId, errorMessage = error.message))
                }
        }
    }
}

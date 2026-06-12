package com.siteflow.signature.outlet.walkthrough.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siteflow.signature.cso.onboarding.data.OutletRepository
import com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData
import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.data.auth.TokenStorage
import com.siteflow.signature.core.data.networking.result.NetworkResult
import com.siteflow.signature.outlet.walkthrough.data.OutletKycData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Contract ──────────────────────────────────────────────────────────────────

data class WalkthroughState(
    val giftRevealed: Boolean = false,
    val consentConfirmInfo: Boolean = false,
    val consentAcceptTerms: Boolean = false,
    val outlet: OutletResponseData? = null,
    val isLoadingOutlet: Boolean = false,
    val outletError: String? = null,
    val kycData: OutletKycData? = null,
    val isLoadingKyc: Boolean = false,
    val kycError: String? = null
) {
    val canProceedFromAgreement: Boolean
        get() = consentConfirmInfo && consentAcceptTerms
}

sealed interface WalkthroughAction {
    data object RevealGift : WalkthroughAction
    data object LoadOutlet : WalkthroughAction
    data object RetryLoadOutlet : WalkthroughAction
    data object LoadKyc : WalkthroughAction
    data object RetryLoadKyc : WalkthroughAction
    data object GoBack : WalkthroughAction
    data object ContinueFromWelcome : WalkthroughAction
    data object ContinueFromOutletDetails : WalkthroughAction
    data object ContinueFromAgreement : WalkthroughAction
    data object FinishWalkthrough : WalkthroughAction
    data object ToggleConsentConfirmInfo : WalkthroughAction
    data object ToggleConsentAcceptTerms : WalkthroughAction
}

sealed interface WalkthroughEvent {
    data object NavigateToOutletDetails : WalkthroughEvent
    data object NavigateToAgreement : WalkthroughEvent
    data object NavigateToPayment : WalkthroughEvent
    data object NavigateToDashboard : WalkthroughEvent
    data object NavigateBack : WalkthroughEvent
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class WalkthroughViewModel(
    private val walkthroughRepository: WalkthroughRepository,
    private val outletRepository: OutletRepository,
    private val tokenStorage: TokenStorage,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(WalkthroughState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<WalkthroughEvent>()
    val events = _events.asSharedFlow()

    private var currentStepName = "welcome"

    fun onAction(action: WalkthroughAction) {
        when (action) {
            WalkthroughAction.RevealGift ->
                _state.update { it.copy(giftRevealed = true) }

            WalkthroughAction.LoadOutlet -> loadOutlet(forceRefresh = false)
            WalkthroughAction.RetryLoadOutlet -> loadOutlet(forceRefresh = true)

            WalkthroughAction.LoadKyc -> loadKyc(forceRefresh = false)
            WalkthroughAction.RetryLoadKyc -> loadKyc(forceRefresh = true)

            WalkthroughAction.GoBack -> {
                analytics.track(AnalyticsEvent.OutletEvent.WalkthroughAbandoned(
                    stepOnExit = currentStepName,
                    timeSpentMs = 0L
                ))
                emit(WalkthroughEvent.NavigateBack)
            }

            WalkthroughAction.ToggleConsentConfirmInfo ->
                _state.update { it.copy(consentConfirmInfo = !it.consentConfirmInfo) }

            WalkthroughAction.ToggleConsentAcceptTerms ->
                _state.update { it.copy(consentAcceptTerms = !it.consentAcceptTerms) }

            WalkthroughAction.ContinueFromWelcome -> {
                currentStepName = "outlet_details"
                analytics.track(AnalyticsEvent.OutletEvent.WalkthroughWelcomeCompleted(timeOnStepMs = 0L))
                emit(WalkthroughEvent.NavigateToOutletDetails)
            }

            WalkthroughAction.ContinueFromOutletDetails -> {
                currentStepName = "agreement"
                analytics.track(AnalyticsEvent.OutletEvent.WalkthroughOutletDetailsCompleted(timeOnStepMs = 0L))
                emit(WalkthroughEvent.NavigateToAgreement)
            }

            WalkthroughAction.ContinueFromAgreement -> {
                if (_state.value.canProceedFromAgreement) {
                    currentStepName = "payment"
                    analytics.track(AnalyticsEvent.OutletEvent.WalkthroughAgreementCompleted(timeOnStepMs = 0L))
                    analytics.track(AnalyticsEvent.OutletEvent.WalkthroughPaymentViewed)
                    emit(WalkthroughEvent.NavigateToPayment)
                }
            }

            WalkthroughAction.FinishWalkthrough -> finishWalkthrough()
        }
    }

    private fun loadOutlet(forceRefresh: Boolean) {
        if (!forceRefresh && _state.value.outlet != null) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingOutlet = true, outletError = null) }
            val outletId = tokenStorage.getOutletId()
            if (outletId == null) {
                _state.update {
                    it.copy(isLoadingOutlet = false, outletError = "Outlet not found. Please contact support.")
                }
                return@launch
            }
            when (val result = outletRepository.getOutletById(outletId)) {
                is NetworkResult.Success -> {
                    if (!forceRefresh) analytics.track(AnalyticsEvent.OutletEvent.WalkthroughOutletDetailsViewed)
                    _state.update { it.copy(isLoadingOutlet = false, outlet = result.data) }
                }
                is NetworkResult.Error ->
                    _state.update {
                        it.copy(isLoadingOutlet = false, outletError = "Failed to load outlet details. Please try again.")
                    }
            }
        }
    }

    private fun loadKyc(forceRefresh: Boolean) {
        if (!forceRefresh && _state.value.kycData != null) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingKyc = true, kycError = null) }
            val outletId = tokenStorage.getOutletId()
            if (outletId == null) {
                _state.update {
                    it.copy(isLoadingKyc = false, kycError = "Outlet not found. Please contact support.")
                }
                return@launch
            }
            when (val result = outletRepository.getOutletKyc(outletId)) {
                is NetworkResult.Success -> {
                    if (!forceRefresh) analytics.track(AnalyticsEvent.OutletEvent.WalkthroughAgreementViewed)
                    _state.update { it.copy(isLoadingKyc = false, kycData = result.data) }
                }
                is NetworkResult.Error ->
                    _state.update {
                        it.copy(isLoadingKyc = false, kycError = "Failed to load payment details. Please try again.")
                    }
            }
        }
    }

    private fun finishWalkthrough() {
        viewModelScope.launch {
            walkthroughRepository.markCompleted()
            analytics.track(AnalyticsEvent.OutletEvent.WalkthroughCompleted(totalDurationMs = 0L, stepsCompleted = 4))
            _events.emit(WalkthroughEvent.NavigateToDashboard)
        }
    }

    private fun emit(event: WalkthroughEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}

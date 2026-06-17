package com.siteflow.signature.cso.onboarding.domain

import com.siteflow.signature.cso.onboarding.data.Classification
import com.siteflow.signature.cso.onboarding.data.GpsLocation
import com.siteflow.signature.cso.onboarding.data.OutletRepository
import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.domain.ImageProcessor
import com.siteflow.signature.core.domain.LocationService
import com.siteflow.signature.core.domain.nowLabel
import com.siteflow.signature.core.domain.truncate4
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.core.presentation.components.toast.GlobalToastHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class OnboardingViewModel(
    private val scope: CoroutineScope,
    private val locationService: LocationService,
    private val outletRepository: OutletRepository,
    private val imageProcessor: ImageProcessor,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<OnboardingState, OnboardingAction, OnboardingEvent>(OnboardingState()) {

    /** Guards automatic re-requests (same pattern as SiteFlow) */
    private var locationRequested = false
    private var pincodeLookupJob: Job? = null
    private var agreementOtpCountdownJob: Job? = null

    override fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.OutletNameChanged ->
                updateState { it.copy(outletName = action.value, error = null) }

            is OnboardingAction.OwnerNameChanged ->
                updateState { it.copy(ownerName = action.value, error = null) }

            is OnboardingAction.ContactNumberChanged -> {
                val filtered = action.value.filter { it.isDigit() }.take(10)
                updateState {
                    it.copy(
                        contactNumber = filtered,
                        whatsAppNumber = if (it.sameAsContact) filtered else it.whatsAppNumber,
                        error = null
                    )
                }
            }

            is OnboardingAction.WhatsAppNumberChanged -> {
                val filtered = action.value.filter { it.isDigit() }.take(10)
                updateState { it.copy(whatsAppNumber = filtered, error = null) }
            }

            is OnboardingAction.SameAsContactToggled -> {
                updateState {
                    it.copy(
                        sameAsContact = action.checked,
                        whatsAppNumber = if (action.checked) it.contactNumber else it.whatsAppNumber
                    )
                }
            }

            is OnboardingAction.EmailChanged ->
                updateState { it.copy(email = action.value, error = null) }

            is OnboardingAction.OutletTypeSelected -> {
                analytics.track(AnalyticsEvent.ASEEvent.OnboardingOutletTypeSelected(outletType = action.value))
                updateState { it.copy(outletType = action.value, error = null) }
            }

            is OnboardingAction.AddressChanged ->
                updateState { it.copy(address = action.value, error = null) }

            is OnboardingAction.LandmarkChanged ->
                updateState { it.copy(landmark = action.value, error = null) }

            is OnboardingAction.LocalityChanged ->
                updateState { it.copy(locality = action.value, error = null) }

            is OnboardingAction.PincodeChanged -> {
                val filtered = action.value.filter { it.isDigit() }.take(6)
                updateState {
                    it.copy(
                        pincode = filtered,
                        error = null,
                        // Clear location when pincode changes
                        city = if (filtered.length < 6) "" else it.city,
                        state = if (filtered.length < 6) "" else it.state,
                        locationId = if (filtered.length < 6) "" else it.locationId,
                        pincodeError = null
                    )
                }
                // Auto-lookup when 6 digits entered
                if (filtered.length == 6) {
                    pincodeLookupJob?.cancel()
                    pincodeLookupJob = scope.launch {
                        delay(300) // debounce rapid typing
                        lookupPincode(filtered)
                    }
                }
            }

            OnboardingAction.LookupPincode -> {
                val pincode = state.value.pincode
                if (pincode.length == 6) {
                    lookupPincode(pincode)
                } else {
                    updateState { it.copy(pincodeError = "Please enter a 6-digit pincode") }
                }
            }

            is OnboardingAction.CityChanged ->
                updateState { it.copy(city = action.value, error = null) }

            is OnboardingAction.StateChanged ->
                updateState { it.copy(state = action.value, error = null) }

            OnboardingAction.RequestLocation -> requestLocationOnce()

            OnboardingAction.RetryLocation -> {
                locationRequested = false
                requestLocationOnce()
            }

            OnboardingAction.FetchSlabs -> fetchSlabs()

            is OnboardingAction.SlabSelected -> {
                val slab = state.value.slabs.firstOrNull { it.id == action.slabId }
                if (slab != null) {
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingSlabSelected(slabId = slab.id, slabLabel = slab.classification))
                    updateState {
                        it.copy(
                            selectedClassification = slab.classification,
                            selectedSlabId = slab.id,
                            classification = mapSlabToUiClassification(slab),
                            error = null
                        )
                    }
                }
            }

            is OnboardingAction.PhotoCaptured -> {
                analytics.track(AnalyticsEvent.ASEEvent.OnboardingPhotoCaptured(
                    slotId = action.slotId,
                    photoCount = state.value.photoSlots.count { it.imagePath != null } + 1,
                    totalRequired = state.value.photoSlots.count { it.required }
                ))
                scope.launch {
                    val (lat, lng, locError) = fetchCurrentGps()
                    if (locError != null) {
                        GlobalToastHandler.showError(locError)
                        return@launch
                    }
                    val processedPath = imageProcessor.processImage(
                        originalPath = action.path,
                        latitude = lat,
                        longitude = lng
                    )
                    val now = nowLabel()
                    val gps = if (lat != null && lng != null) "${truncate4(lat)}, ${truncate4(lng)}" else null
                    updateState { state ->
                        state.copy(
                            photoSlots = state.photoSlots.map { slot ->
                                if (slot.id == action.slotId)
                                    slot.copy(imagePath = processedPath, capturedAt = now, gpsLabel = gps)
                                else slot
                            },
                            error = null
                        )
                    }
                }
            }

            is OnboardingAction.PhotoRemoved -> {
                analytics.track(AnalyticsEvent.ASEEvent.OnboardingPhotoRemoved(slotId = action.slotId))
                updateState { state ->
                    state.copy(
                        photoSlots = state.photoSlots.map { slot ->
                            if (slot.id == action.slotId) slot.copy(imagePath = null)
                            else slot
                        }
                    )
                }
            }

            is OnboardingAction.DistributorSelected -> {
                analytics.track(AnalyticsEvent.ASEEvent.OnboardingDistributorSelected(distributorId = action.id, distributorName = action.name))
                updateState { it.copy(distributorName = action.name, selectedDistributorId = action.id, error = null) }
            }

            OnboardingAction.FetchDistributors -> fetchDistributors()

            is OnboardingAction.AccountHolderChanged ->
                updateState { it.copy(accountHolderName = action.value, error = null) }

            is OnboardingAction.BankNameChanged ->
                updateState { it.copy(bankName = action.value, error = null) }

            is OnboardingAction.AccountNumberChanged -> {
                val filtered = action.value.filter { it.isDigit() }.take(20)
                updateState { it.copy(accountNumber = filtered, error = null) }
            }

            is OnboardingAction.IfscCodeChanged -> {
                val filtered = action.value.filter { it.isLetterOrDigit() }.uppercase().take(11)
                updateState { it.copy(ifscCode = filtered, error = null) }
            }

            is OnboardingAction.UpiIdChanged ->
                updateState { it.copy(upiId = action.value, error = null) }

            is OnboardingAction.BranchNameChanged -> {
                val filtered = action.value.filter { it.isLetterOrDigit() || it == ' ' }
                updateState { it.copy(branchName = filtered, error = null) }
            }

            is OnboardingAction.CancelledChequeCaptured -> {
                scope.launch {
                    val (lat, lng, locError) = fetchCurrentGps()
                    if (locError != null) {
                        GlobalToastHandler.showError(locError)
                        return@launch
                    }
                    val processedPath = imageProcessor.processImage(
                        originalPath = action.path,
                        latitude = lat,
                        longitude = lng
                    )
                    val now = nowLabel()
                    val gps = if (lat != null && lng != null) "${truncate4(lat)}, ${truncate4(lng)}" else null
                    updateState { it.copy(
                        cancelledChequePath = processedPath,
                        cancelledChequeCapturedAt = now,
                        cancelledChequeGpsLabel = gps,
                        error = null
                    ) }
                }
            }

            OnboardingAction.CancelledChequeRemoved ->
                updateState { it.copy(cancelledChequePath = null, cancelledChequeCapturedAt = null, cancelledChequeGpsLabel = null) }

            is OnboardingAction.PaymentModeSelected -> {
                updateState {
                    it.copy(
                        bankPaymentMode = action.mode,
                        // Clear irrelevant fields when switching modes
                        upiId = if (action.mode == BankPaymentMode.BANK) it.upiId else it.upiId,
                        cancelledChequePath = if (action.mode == BankPaymentMode.UPI) null else it.cancelledChequePath,
                        cancelledChequeCapturedAt = if (action.mode == BankPaymentMode.UPI) null else it.cancelledChequeCapturedAt,
                        cancelledChequeGpsLabel = if (action.mode == BankPaymentMode.UPI) null else it.cancelledChequeGpsLabel,
                        error = null
                    )
                }
            }

            is OnboardingAction.BankAccountTypeSelected ->
                updateState { it.copy(bankAccountType = action.type, error = null) }

            is OnboardingAction.KycIdTypeSelected -> {
                analytics.track(AnalyticsEvent.ASEEvent.OnboardingKycTypeSelected(kycType = action.value))
                updateState { it.copy(kycIdType = action.value, error = null) }
            }

            is OnboardingAction.KycIdNumberChanged ->
                updateState { it.copy(kycIdNumber = action.value, error = null) }

            is OnboardingAction.KycGstNumberChanged ->
                updateState { it.copy(kycGstNumber = action.value.uppercase(), error = null) }

            is OnboardingAction.KycLocationTypeSelected ->
                updateState { it.copy(kycLocationType = action.value, error = null) }

            is OnboardingAction.KycPhotoCaptured -> {
                analytics.track(AnalyticsEvent.ASEEvent.OnboardingKycPhotoCaptured(photoType = action.type))
                scope.launch {
                    val (lat, lng, locError) = fetchCurrentGps()
                    if (locError != null) {
                        GlobalToastHandler.showError(locError)
                        return@launch
                    }
                    val processedPath = imageProcessor.processImage(
                        originalPath = action.path,
                        latitude = lat,
                        longitude = lng
                    )
                    val now = nowLabel()
                    val gps = if (lat != null && lng != null) "${truncate4(lat)}, ${truncate4(lng)}" else null
                    updateState { state ->
                        when (action.type) {
                            "ID_PROOF" -> state.copy(
                                kycIdProofPath = processedPath,
                                kycIdProofCapturedAt = now, kycIdProofGpsLabel = gps, error = null
                            )
                            "GST_CERT" -> state.copy(
                                kycGstCertificatePath = processedPath,
                                kycGstCertificateCapturedAt = now, kycGstCertificateGpsLabel = gps, error = null
                            )
                            "LOCATION_PROOF" -> state.copy(
                                kycLocationProofPath = processedPath,
                                kycLocationProofCapturedAt = now, kycLocationProofGpsLabel = gps, error = null
                            )
                            else -> state
                        }
                    }
                }
            }

            is OnboardingAction.KycPhotoRemoved -> {
                updateState { state ->
                    when (action.type) {
                        "ID_PROOF" -> state.copy(kycIdProofPath = null)
                        "GST_CERT" -> state.copy(kycGstCertificatePath = null)
                        "LOCATION_PROOF" -> state.copy(kycLocationProofPath = null)
                        else -> state
                    }
                }
            }

            is OnboardingAction.StockingItemToggled -> {
                val isAdding = action.item !in state.value.stockingCommitment
                if (isAdding) analytics.track(AnalyticsEvent.ASEEvent.OnboardingStockingItemsSelected)
                updateState { state ->
                    val current = state.stockingCommitment.toMutableList()
                    if (action.item in current) {
                        current.remove(action.item)
                    } else {
                        current.add(action.item)
                    }
                    state.copy(stockingCommitment = current, error = null)
                }
            }

            is OnboardingAction.MonthlyRentalAmountChanged -> {
                val filtered = action.value.filter { it.isDigit() }.take(8)
                updateState { it.copy(monthlyRentalAmount = filtered, error = null) }
            }

            is OnboardingAction.ExpectedSalesPotentialChanged -> {
                val filtered = action.value.filter { it.isDigit() }.take(8)
                updateState { it.copy(expectedSalesPotential = filtered, error = null) }
            }

            is OnboardingAction.AgreementAccepted -> {
                if (action.accepted) analytics.track(AnalyticsEvent.ASEEvent.OnboardingAgreementAccepted)
                updateState { it.copy(agreementAccepted = action.accepted, error = null) }
            }

            OnboardingAction.RequestAgreementOtp,
            OnboardingAction.SubmitOnboarding -> {
                if (!state.value.agreementAccepted) {
                    updateState { it.copy(error = "Please accept the agreement to submit") }
                    return
                }
                if (state.value.isOtpSent && !state.value.canResendAgreementOtp) return
                requestAgreementOtp()
            }

            is OnboardingAction.AgreementOtpChanged -> {
                val filtered = action.value.filter { it.isDigit() }.take(6)
                updateState { it.copy(agreementOtp = filtered, error = null) }
            }

            OnboardingAction.VerifyAgreementOtp -> {
                val otp = state.value.agreementOtp
                if (otp.length != 6) {
                    updateState { it.copy(error = "Please enter a valid 6-digit OTP") }
                    return
                }
                verifyAgreementOtp()
            }

            OnboardingAction.ContinueToNextStep -> continueToNext()

            OnboardingAction.GoBack -> {
                analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepAbandoned(
                    stepNumber = state.value.currentStep,
                    stepLabel = stepLabel(state.value.currentStep)
                ))
                if (state.value.currentStep > 1) {
                    updateState { it.copy(currentStep = it.currentStep - 1) }
                }
                emitEvent(OnboardingEvent.NavigateBack)
            }

            is OnboardingAction.SetCurrentStep -> {
                analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepViewed(
                    stepNumber = action.step,
                    stepLabel = stepLabel(action.step),
                    outletId = state.value.createdOutletId ?: ""
                ))
                updateState { it.copy(currentStep = action.step, error = null) }
            }

            is OnboardingAction.SetOutletId -> {
                updateState { state ->
                    // If we are switching to a completely different outlet,
                    // we must reset the in-memory state so data doesn't leak.
                    if (state.createdOutletId != null && state.createdOutletId != action.outletId) {
                        OnboardingState(createdOutletId = action.outletId)
                    } else {
                        state.copy(createdOutletId = action.outletId)
                    }
                }
                // Fetch outlet details if contactNumber is not already populated
                if (state.value.contactNumber.isBlank()) {
                    scope.launch {
                        outletRepository.getOutletById(action.outletId)
                            .onSuccess { outlet ->
                                updateState { it.copy(contactNumber = outlet.phone ?: outlet.ownerMobile) }
                            }
                    }
                }
            }

            OnboardingAction.ResetState -> {
                locationRequested = false
                pincodeLookupJob?.cancel()
                updateState { OnboardingState() }
            }
        }
    }

    // ── Pincode Location Lookup ──

    private fun lookupPincode(pincode: String) {
        pincodeLookupJob?.cancel()
        pincodeLookupJob = scope.launch {
            updateState { it.copy(isPincodeLoading = true, pincodeError = null) }

            outletRepository.searchLocation(pincode)
                .onSuccess { locations ->
                    if (locations.isNotEmpty()) {
                        val loc = locations.first()
                        analytics.track(AnalyticsEvent.ASEEvent.OnboardingPincodeLookedUp(
                            pincode = pincode,
                            success = true,
                            autoFilledCity = loc.city,
                            autoFilledState = loc.state
                        ))
                        updateState {
                            it.copy(
                                city = loc.city,
                                state = loc.state,
                                locationId = loc.id,
                                isPincodeLoading = false,
                                pincodeError = null
                            )
                        }
                    } else {
                        updateState {
                            it.copy(
                                city = "",
                                state = "",
                                locationId = "",
                                isPincodeLoading = false,
                                pincodeError = "No location found for this pincode"
                            )
                        }
                    }
                }
                .onError { error ->
                    updateState {
                        it.copy(
                            isPincodeLoading = false,
                            pincodeError = error.message
                        )
                    }
                }
        }
    }

    // ── GPS Location ──

    private fun requestLocationOnce() {
        if (locationRequested) return
        locationRequested = true

        updateState { it.copy(isCapturingGps = true, error = null) }

        locationService.getCurrentLocation(
            onSuccess = { lat, lng ->
                analytics.track(AnalyticsEvent.ASEEvent.OnboardingGpsCaptured(lat = lat, lng = lng))
                updateState {
                    it.copy(
                        isCapturingGps = false,
                        gpsLocation = GpsLocation(lat, lng, "")
                    )
                }
            },
            onFailure = { errorMsg ->
                analytics.track(AnalyticsEvent.ASEEvent.OnboardingGpsFailed(errorMessage = errorMsg))
                locationRequested = errorMsg == "LOCATION_PERMISSION_PERMANENTLY_DENIED"
                updateState {
                    it.copy(
                        isCapturingGps = false,
                        error = when (errorMsg) {
                            "LOCATION_PERMISSION_PERMANENTLY_DENIED" ->
                                "Location permission permanently denied. Please enable it in Settings."
                            "LOCATION_PERMISSION_DENIED" ->
                                "Location permission is required. Tap retry."
                            else -> errorMsg
                        }
                    )
                }
            }
        )
    }

    // ── Step Navigation ──

    private fun continueToNext() {
        val currentState = state.value
        when (currentState.currentStep) {
            1 -> {
                if (!OnboardingValidator.isStep1Valid(currentState)) {
                    val errorMsg = when {
                        currentState.locationId.isBlank() && currentState.pincode.length == 6 ->
                            "Please wait for pincode lookup to complete"
                        currentState.locationId.isBlank() ->
                            "Please enter a valid 6-digit pincode"
                        else -> "Please fill all required fields"
                    }
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepError(stepNumber = 1, errorMessage = errorMsg))
                    updateState { it.copy(error = errorMsg) }
                    return
                }
                createOutletAndContinue()
            }
            2 -> {
                if (!OnboardingValidator.isStep2Valid(currentState)) {
                    val errorMsg = "Please wait for classification slabs to load"
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepError(stepNumber = 2, errorMessage = errorMsg))
                    updateState { it.copy(error = errorMsg) }
                    return
                }
                analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepCompleted(stepNumber = 2, stepLabel = stepLabel(2), timeOnStepMs = 0L, outletId = currentState.createdOutletId ?: ""))
                updateState { it.copy(currentStep = 3, error = null) }
                emitEvent(OnboardingEvent.NavigateToStep3)
            }
            3 -> {
                if (!OnboardingValidator.isStep3Valid(currentState)) {
                    val errorMsg = "Please fill all required distributor fields"
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepError(stepNumber = 3, errorMessage = errorMsg))
                    updateState { it.copy(error = errorMsg) }
                    return
                }
                submitBusinessDetailsAndContinue()
            }
            4 -> {
                if (!OnboardingValidator.isStep4Valid(currentState)) {
                    val errorMsg = "Please fill all required KYC fields and capture photos"
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepError(stepNumber = 4, errorMessage = errorMsg))
                    updateState { it.copy(error = errorMsg) }
                    return
                }
                submitKycDetailsAndContinue()
            }
            5 -> {
                if (!OnboardingValidator.isStep5Valid(currentState)) {
                    val errorMsg = "Please capture all 2 required photos"
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepError(stepNumber = 5, errorMessage = errorMsg))
                    updateState { it.copy(error = errorMsg) }
                    return
                }
                submitPhotosAndContinue()
            }
        }
    }

    private fun stepLabel(step: Int) = when (step) {
        1 -> "Basic Info"
        2 -> "Slab Selection"
        3 -> "Business Details"
        4 -> "KYC"
        5 -> "Photos"
        6 -> "Agreement"
        else -> "Step $step"
    }

    private fun createOutletAndContinue() {
        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            outletRepository.createOutlet(
                state = state.value,
                locationId = state.value.locationId
            )
                .onSuccess { outletData ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            createdOutletId = outletData.id,
                            currentStep = 2,
                            error = null
                        )
                    }
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingStarted(outletId = outletData.id))
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepCompleted(stepNumber = 1, stepLabel = stepLabel(1), timeOnStepMs = 0L, outletId = outletData.id))
                    GlobalToastHandler.showSuccess("Outlet registered successfully")
                    emitEvent(OnboardingEvent.NavigateToStep2)
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, error = error.message) }
                    GlobalToastHandler.showError(error.message)
                }
        }
    }

    private fun submitBusinessDetailsAndContinue() {
        val outletId = state.value.createdOutletId
        if (outletId.isNullOrBlank()) {
            updateState { it.copy(error = "Outlet not found. Please go back and retry.") }
            return
        }

        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            outletRepository.submitBusinessDetails(
                state = state.value,
                outletId = outletId
            )
                .onSuccess {
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepCompleted(stepNumber = 3, stepLabel = stepLabel(3), timeOnStepMs = 0L, outletId = outletId))
                    updateState {
                        it.copy(
                            isLoading = false,
                            currentStep = 4,
                            error = null
                        )
                    }
                    GlobalToastHandler.showSuccess("Business details submitted")
                    emitEvent(OnboardingEvent.NavigateToStep4)
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, error = error.message) }
                    GlobalToastHandler.showError(error.message)
                }
        }
    }

    private fun submitKycDetailsAndContinue() {
        val outletId = state.value.createdOutletId
        if (outletId.isNullOrBlank()) {
            updateState { it.copy(error = "Outlet not found. Please go back and retry.") }
            return
        }

        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            analytics.track(AnalyticsEvent.ASEEvent.OnboardingBankDetailsEntered(
                hasUpi = state.value.upiId.isNotBlank(),
                hasChequePhoto = state.value.cancelledChequePath != null
            ))
            outletRepository.submitKycDetails(
                state = state.value,
                outletId = outletId
            )
                .onSuccess {
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepCompleted(stepNumber = 4, stepLabel = stepLabel(4), timeOnStepMs = 0L, outletId = outletId))
                    updateState {
                        it.copy(
                            isLoading = false,
                            currentStep = 5,
                            error = null
                        )
                    }
                    GlobalToastHandler.showSuccess("KYC details submitted")
                    emitEvent(OnboardingEvent.NavigateToStep5)
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, error = error.message) }
                    GlobalToastHandler.showError(error.message)
                }
        }
    }

    private fun submitPhotosAndContinue() {
        val outletId = state.value.createdOutletId
        if (outletId.isNullOrBlank()) {
            updateState { it.copy(error = "Outlet not found. Please go back and retry.") }
            return
        }

        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            outletRepository.submitPhotos(
                state = state.value,
                outletId = outletId
            )
                .onSuccess {
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingStepCompleted(stepNumber = 5, stepLabel = stepLabel(5), timeOnStepMs = 0L, outletId = outletId))
                    updateState {
                        it.copy(
                            isLoading = false,
                            currentStep = 6,
                            error = null
                        )
                    }
                    GlobalToastHandler.showSuccess("Photos submitted")
                    emitEvent(OnboardingEvent.NavigateToStep6)
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, error = error.message) }
                    GlobalToastHandler.showError(error.message)
                }
        }
    }

    // ── Agreement OTP ──

    private fun requestAgreementOtp() {
        if (state.value.isLoading) return
        val outletId = state.value.createdOutletId
        if (outletId.isNullOrBlank()) {
            updateState { it.copy(error = "Outlet not found. Please go back and retry.") }
            return
        }

        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            outletRepository.requestAgreementOtp(outletId)
                .onSuccess {
                    updateState { it.copy(isLoading = false, isOtpSent = true, error = null) }
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingAgreementOtpRequested)
                    GlobalToastHandler.showSuccess("OTP sent to outlet owner's phone")
                    emitEvent(OnboardingEvent.AgreementOtpSent)
                    startAgreementOtpCountdown()
                }
                .onError { error ->
                    // If OTP was already sent (409 STATE_CONFLICT), show OTP input
                    if (error.message.contains("AGREEMENT_PENDING", ignoreCase = true) ||
                        error.message.contains("STATE_CONFLICT", ignoreCase = true)) {
                        updateState { it.copy(isLoading = false, isOtpSent = true, error = null) }
                        GlobalToastHandler.showSuccess("OTP was already sent. Please enter the OTP.")
                        startAgreementOtpCountdown()
                    } else {
                        updateState { it.copy(isLoading = false, error = error.message) }
                        GlobalToastHandler.showError(error.message)
                    }
                }
        }
    }

    private fun startAgreementOtpCountdown() {
        agreementOtpCountdownJob?.cancel()
        agreementOtpCountdownJob = scope.launch {
            updateState { it.copy(agreementOtpCountdown = 60, canResendAgreementOtp = false) }
            for (i in 60 downTo 0) {
                updateState { it.copy(agreementOtpCountdown = i, canResendAgreementOtp = i == 0) }
                if (i > 0) delay(1000)
            }
        }
    }

    private fun verifyAgreementOtp() {
        val outletId = state.value.createdOutletId
        if (outletId.isNullOrBlank()) {
            updateState { it.copy(error = "Outlet not found. Please go back and retry.") }
            return
        }

        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            outletRepository.verifyAgreementOtp(outletId, state.value.agreementOtp)
                .onSuccess {
                    updateState { it.copy(isLoading = false, error = null) }
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingAgreementOtpVerified(outletId = outletId))
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingSubmitted(outletId = outletId))
                    analytics.track(AnalyticsEvent.ASEEvent.OnboardingCompleted(outletId = outletId))
                    GlobalToastHandler.showSuccess("Agreement verified! Outlet submitted for ASM review.")
                    emitEvent(OnboardingEvent.OnboardingSubmitted)
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, error = error.message) }
                    GlobalToastHandler.showError(error.message)
                }
        }
    }

    // ── Distributors ──

    private fun fetchDistributors() {
        if (state.value.distributors.isNotEmpty()) return // already loaded
        scope.launch {
            updateState { it.copy(isDistributorsLoading = true) }
            outletRepository.getDistributors()
                .onSuccess { distributors ->
                    updateState {
                        it.copy(
                            distributors = distributors,
                            isDistributorsLoading = false
                        )
                    }
                }
                .onError {
                    updateState { it.copy(isDistributorsLoading = false) }
                }
        }
    }

    // ── Slabs ──

    private fun fetchSlabs() {
        if (state.value.slabs.isNotEmpty()) return // already loaded
        scope.launch {
            updateState { it.copy(isSlabsLoading = true) }
            outletRepository.getSlabs()
                .onSuccess { slabs ->
                    // Auto-select the default (lowest-tier) slab
                    val defaultSlab = slabs.minByOrNull { it.minQuantity }
                    updateState {
                        it.copy(
                            slabs = slabs,
                            isSlabsLoading = false,
                            selectedClassification = defaultSlab?.classification ?: "",
                            selectedSlabId = defaultSlab?.id ?: "",
                            classification = mapSlabToUiClassification(defaultSlab)
                        )
                    }
                }
                .onError {
                    updateState { it.copy(isSlabsLoading = false) }
                }
        }
    }

    /**
     * Maps a backend slab classification string to the UI Classification enum (for theming).
     */
    private fun mapSlabToUiClassification(
        slab: com.siteflow.signature.cso.onboarding.data.dto.SlabDto?
    ): Classification {
        return when (slab?.classification) {
            "PLATINUM" -> Classification.PLATINUM
            "DIAMOND" -> Classification.DIAMOND
            "GOLD" -> Classification.GOLD
            "SILVER" -> Classification.SILVER
            else -> Classification.SILVER
        }
    }

    /**
     * Fetches the current GPS coordinates. Prefers the already-fetched cached value in state
     * for speed. If unavailable, fires a live LocationService request and awaits the result.
     * Returns Triple(lat, lng, errorMessage) — errorMessage is non-null when location is unavailable.
     */
    private suspend fun fetchCurrentGps(): Triple<Double?, Double?, String?> {
        // Fast path: if we already have location in state, use it
        val cached = state.value.gpsLocation
        if (cached != null) return Triple(cached.latitude, cached.longitude, null)

        // Slow path: ask LocationService directly and await the callback
        return suspendCancellableCoroutine { cont ->
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
    }
}

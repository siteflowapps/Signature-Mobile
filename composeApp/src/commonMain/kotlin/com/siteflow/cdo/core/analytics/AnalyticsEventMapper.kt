package com.siteflow.cdo.core.analytics

fun AnalyticsEvent.toEventName(): String = when (this) {
    is AnalyticsEvent.GlobalEvent -> this.toGlobalEventName()
    is AnalyticsEvent.ASEEvent -> this.toAseEventName()
    is AnalyticsEvent.ASMEvent -> this.toAsmEventName()
    is AnalyticsEvent.OutletEvent -> this.toOutletEventName()
    is AnalyticsEvent.ProfileEvent -> this.toProfileEventName()
}

fun AnalyticsEvent.toEventParams(): Map<String, Any> = when (this) {
    is AnalyticsEvent.GlobalEvent -> this.toGlobalEventParams()
    is AnalyticsEvent.ASEEvent -> this.toAseEventParams()
    is AnalyticsEvent.ASMEvent -> this.toAsmEventParams()
    is AnalyticsEvent.OutletEvent -> this.toOutletEventParams()
    is AnalyticsEvent.ProfileEvent -> this.toProfileEventParams()
}

// ─────────────────────────────────────────────────────────────────────────────
// GLOBAL
// ─────────────────────────────────────────────────────────────────────────────

private fun AnalyticsEvent.GlobalEvent.toGlobalEventName(): String = when (this) {
    is AnalyticsEvent.GlobalEvent.AppOpened -> "app_opened"
    is AnalyticsEvent.GlobalEvent.AppBackgrounded -> "app_backgrounded"
    is AnalyticsEvent.GlobalEvent.AppCrashed -> "app_crashed"
    is AnalyticsEvent.GlobalEvent.SessionStarted -> "session_started"
    is AnalyticsEvent.GlobalEvent.SessionEnded -> "session_ended"
    is AnalyticsEvent.GlobalEvent.DeepLinkOpened -> "deep_link_opened"
    is AnalyticsEvent.GlobalEvent.LoginScreenViewed -> "login_screen_viewed"
    is AnalyticsEvent.GlobalEvent.LoginSubmitted -> "login_submitted"
    is AnalyticsEvent.GlobalEvent.LoginFailed -> "login_failed"
    is AnalyticsEvent.GlobalEvent.OtpScreenViewed -> "otp_screen_viewed"
    is AnalyticsEvent.GlobalEvent.OtpEntered -> "otp_entered"
    is AnalyticsEvent.GlobalEvent.OtpResendTapped -> "otp_resend_tapped"
    is AnalyticsEvent.GlobalEvent.OtpEditNumberTapped -> "otp_edit_number_tapped"
    is AnalyticsEvent.GlobalEvent.OtpVerified -> "otp_verified"
    is AnalyticsEvent.GlobalEvent.OtpVerificationFailed -> "otp_verification_failed"
    is AnalyticsEvent.GlobalEvent.SessionExpired -> "session_expired"
    is AnalyticsEvent.GlobalEvent.LogoutTapped -> "logout_tapped"
    is AnalyticsEvent.GlobalEvent.LogoutConfirmed -> "logout_confirmed"
    is AnalyticsEvent.GlobalEvent.ScreenViewed -> "screen_viewed"
    is AnalyticsEvent.GlobalEvent.BottomTabTapped -> "bottom_tab_tapped"
    is AnalyticsEvent.GlobalEvent.BackButtonTapped -> "back_button_tapped"
    is AnalyticsEvent.GlobalEvent.NetworkStatusChanged -> "network_status_changed"
    is AnalyticsEvent.GlobalEvent.ApiCallCompleted -> "api_call_completed"
    is AnalyticsEvent.GlobalEvent.ApiCallFailed -> "api_call_failed"
    is AnalyticsEvent.GlobalEvent.TokenRefreshed -> "token_refreshed"
    is AnalyticsEvent.GlobalEvent.TokenRefreshFailed -> "token_refresh_failed"
}

private fun AnalyticsEvent.GlobalEvent.toGlobalEventParams(): Map<String, Any> = when (this) {
    is AnalyticsEvent.GlobalEvent.AppOpened -> mapOf(
        AnalyticsParams.PLATFORM to platform,
        AnalyticsParams.OS_VERSION to osVersion,
        AnalyticsParams.APP_VERSION to appVersion,
        AnalyticsParams.BUILD_NUMBER to buildNumber,
        AnalyticsParams.IS_FRESH_INSTALL to isFreshInstall
    )
    is AnalyticsEvent.GlobalEvent.AppBackgrounded -> emptyMap()
    is AnalyticsEvent.GlobalEvent.AppCrashed -> emptyMap()
    is AnalyticsEvent.GlobalEvent.SessionStarted -> mapOf(
        AnalyticsParams.USER_ROLE to userRole,
        AnalyticsParams.USER_ID to userId,
        AnalyticsParams.BUSINESS_ID to businessId,
        AnalyticsParams.IS_RETURNING_USER to isReturningUser
    )
    is AnalyticsEvent.GlobalEvent.SessionEnded -> emptyMap()
    is AnalyticsEvent.GlobalEvent.DeepLinkOpened -> mapOf(AnalyticsParams.URL to url)
    is AnalyticsEvent.GlobalEvent.LoginScreenViewed -> mapOf(AnalyticsParams.IS_AUTO_REDIRECT to isAutoRedirect)
    is AnalyticsEvent.GlobalEvent.LoginSubmitted -> mapOf(AnalyticsParams.MOBILE_NUMBER_LENGTH to mobileNumberLength)
    is AnalyticsEvent.GlobalEvent.LoginFailed -> mapOf(
        AnalyticsParams.ERROR_MESSAGE to errorMessage,
        AnalyticsParams.ERROR_CODE to errorCode
    )
    is AnalyticsEvent.GlobalEvent.OtpScreenViewed -> mapOf(AnalyticsParams.MOBILE_NUMBER_MASKED to mobileNumberMasked)
    is AnalyticsEvent.GlobalEvent.OtpEntered -> mapOf(AnalyticsParams.TIME_TO_ENTER_MS to timeToEnterMs)
    is AnalyticsEvent.GlobalEvent.OtpResendTapped -> mapOf(
        AnalyticsParams.RESEND_ATTEMPT_NUMBER to resendAttemptNumber,
        AnalyticsParams.COUNTDOWN_REMAINING to countdownRemaining
    )
    is AnalyticsEvent.GlobalEvent.OtpEditNumberTapped -> emptyMap()
    is AnalyticsEvent.GlobalEvent.OtpVerified -> mapOf(
        AnalyticsParams.USER_ROLE to userRole,
        AnalyticsParams.TIME_TO_VERIFY_MS to timeToVerifyMs,
        AnalyticsParams.IS_FIRST_LOGIN to isFirstLogin
    )
    is AnalyticsEvent.GlobalEvent.OtpVerificationFailed -> mapOf(
        AnalyticsParams.ERROR_MESSAGE to errorMessage,
        AnalyticsParams.ATTEMPT_NUMBER to attemptNumber
    )
    is AnalyticsEvent.GlobalEvent.SessionExpired -> mapOf(
        AnalyticsParams.SCREEN_ON_EXPIRY to screenOnExpiry,
        AnalyticsParams.SESSION_DURATION_MS to sessionDurationMs
    )
    is AnalyticsEvent.GlobalEvent.LogoutTapped -> mapOf(
        AnalyticsParams.USER_ROLE to userRole,
        AnalyticsParams.SCREEN to screen
    )
    is AnalyticsEvent.GlobalEvent.LogoutConfirmed -> mapOf(AnalyticsParams.SESSION_DURATION_MS to sessionDurationMs)
    is AnalyticsEvent.GlobalEvent.ScreenViewed -> mapOf(
        AnalyticsParams.SCREEN_NAME to screenName,
        AnalyticsParams.PREVIOUS_SCREEN to previousScreen,
        AnalyticsParams.USER_ROLE to userRole
    )
    is AnalyticsEvent.GlobalEvent.BottomTabTapped -> mapOf(
        AnalyticsParams.TAB to tab,
        AnalyticsParams.PREVIOUS_TAB to previousTab,
        AnalyticsParams.USER_ROLE to userRole
    )
    is AnalyticsEvent.GlobalEvent.BackButtonTapped -> mapOf(AnalyticsParams.SCREEN_NAME to screenName)
    is AnalyticsEvent.GlobalEvent.NetworkStatusChanged -> mapOf(
        AnalyticsParams.NEW_STATUS to newStatus,
        AnalyticsParams.SCREEN to screen
    )
    is AnalyticsEvent.GlobalEvent.ApiCallCompleted -> mapOf(
        AnalyticsParams.ENDPOINT to endpoint,
        AnalyticsParams.METHOD to method,
        AnalyticsParams.STATUS_CODE to statusCode,
        AnalyticsParams.LATENCY_MS to latencyMs,
        AnalyticsParams.IS_RETRY to isRetry
    )
    is AnalyticsEvent.GlobalEvent.ApiCallFailed -> mapOf(
        AnalyticsParams.ENDPOINT to endpoint,
        AnalyticsParams.ERROR_TYPE to errorType,
        AnalyticsParams.ERROR_MESSAGE to errorMessage,
        AnalyticsParams.RETRY_COUNT to retryCount
    )
    is AnalyticsEvent.GlobalEvent.TokenRefreshed -> mapOf(AnalyticsParams.LATENCY_MS to latencyMs)
    is AnalyticsEvent.GlobalEvent.TokenRefreshFailed -> mapOf(AnalyticsParams.ERROR_CODE to errorCode)
}

// ─────────────────────────────────────────────────────────────────────────────
// ASE
// ─────────────────────────────────────────────────────────────────────────────

private fun AnalyticsEvent.ASEEvent.toAseEventName(): String = when (this) {
    is AnalyticsEvent.ASEEvent.DashboardLoaded -> "ase_dashboard_loaded"
    is AnalyticsEvent.ASEEvent.DashboardCardTapped -> "ase_dashboard_card_tapped"
    is AnalyticsEvent.ASEEvent.SlabChartViewed -> "ase_slab_chart_viewed"
    is AnalyticsEvent.ASEEvent.OutletListViewed -> "ase_outlet_list_viewed"
    is AnalyticsEvent.ASEEvent.OutletFilterSelected -> "ase_outlet_filter_selected"
    is AnalyticsEvent.ASEEvent.OutletSearchUsed -> "ase_outlet_search_used"
    is AnalyticsEvent.ASEEvent.OutletCardTapped -> "ase_outlet_card_tapped"
    is AnalyticsEvent.ASEEvent.OutletListScrolled -> "ase_outlet_list_scrolled"
    is AnalyticsEvent.ASEEvent.OutletDetailViewed -> "ase_outlet_detail_viewed"
    is AnalyticsEvent.ASEEvent.AssetRequestTapped -> "ase_asset_request_tapped"
    is AnalyticsEvent.ASEEvent.AssetRequestSuccess -> "ase_asset_request_success"
    is AnalyticsEvent.ASEEvent.AssetRequestFailed -> "ase_asset_request_failed"
    is AnalyticsEvent.ASEEvent.ComplianceTapped -> "ase_compliance_tapped"
    is AnalyticsEvent.ASEEvent.OnboardingStarted -> "onboarding_started"
    is AnalyticsEvent.ASEEvent.OnboardingStepViewed -> "onboarding_step_viewed"
    is AnalyticsEvent.ASEEvent.OnboardingStepCompleted -> "onboarding_step_completed"
    is AnalyticsEvent.ASEEvent.OnboardingStepAbandoned -> "onboarding_step_abandoned"
    is AnalyticsEvent.ASEEvent.OnboardingStepError -> "onboarding_step_error"
    is AnalyticsEvent.ASEEvent.OnboardingCompleted -> "onboarding_completed"
    is AnalyticsEvent.ASEEvent.OnboardingGpsCaptured -> "onboarding_gps_captured"
    is AnalyticsEvent.ASEEvent.OnboardingGpsFailed -> "onboarding_gps_failed"
    is AnalyticsEvent.ASEEvent.OnboardingPincodeLookedUp -> "onboarding_pincode_looked_up"
    is AnalyticsEvent.ASEEvent.OnboardingOutletTypeSelected -> "onboarding_outlet_type_selected"
    is AnalyticsEvent.ASEEvent.OnboardingSlabSelected -> "onboarding_slab_selected"
    is AnalyticsEvent.ASEEvent.OnboardingStockingItemsSelected -> "onboarding_stocking_items_selected"
    is AnalyticsEvent.ASEEvent.OnboardingDistributorSelected -> "onboarding_distributor_selected"
    is AnalyticsEvent.ASEEvent.OnboardingBankDetailsEntered -> "onboarding_bank_details_entered"
    is AnalyticsEvent.ASEEvent.OnboardingKycTypeSelected -> "onboarding_kyc_type_selected"
    is AnalyticsEvent.ASEEvent.OnboardingKycPhotoCaptured -> "onboarding_kyc_photo_captured"
    is AnalyticsEvent.ASEEvent.OnboardingPhotoCaptured -> "onboarding_photo_captured"
    is AnalyticsEvent.ASEEvent.OnboardingPhotoRemoved -> "onboarding_photo_removed"
    is AnalyticsEvent.ASEEvent.OnboardingAgreementAccepted -> "onboarding_agreement_accepted"
    is AnalyticsEvent.ASEEvent.OnboardingAgreementOtpRequested -> "onboarding_agreement_otp_requested"
    is AnalyticsEvent.ASEEvent.OnboardingAgreementOtpVerified -> "onboarding_agreement_otp_verified"
    is AnalyticsEvent.ASEEvent.OnboardingSubmitted -> "onboarding_submitted"
    is AnalyticsEvent.ASEEvent.ComplianceScreenViewed -> "compliance_screen_viewed"
    is AnalyticsEvent.ASEEvent.CompliancePhotoCaptured -> "compliance_photo_captured"
    is AnalyticsEvent.ASEEvent.CompliancePhotoRemoved -> "compliance_photo_removed"
    is AnalyticsEvent.ASEEvent.ComplianceSubmitted -> "compliance_submitted"
    is AnalyticsEvent.ASEEvent.ComplianceSubmitSuccess -> "compliance_submit_success"
    is AnalyticsEvent.ASEEvent.ComplianceSubmitFailed -> "compliance_submit_failed"
    is AnalyticsEvent.ASEEvent.InvoiceListViewed -> "ase_invoice_list_viewed"
    is AnalyticsEvent.ASEEvent.InvoiceFilterSelected -> "ase_invoice_filter_selected"
    is AnalyticsEvent.ASEEvent.InvoiceSearchUsed -> "ase_invoice_search_used"
    is AnalyticsEvent.ASEEvent.InvoiceDetailViewed -> "ase_invoice_detail_viewed"
    is AnalyticsEvent.ASEEvent.InvoiceApproved -> "ase_invoice_approved"
    is AnalyticsEvent.ASEEvent.InvoiceRejected -> "ase_invoice_rejected"
    is AnalyticsEvent.ASEEvent.InvoiceActionSuccess -> "ase_invoice_action_success"
    is AnalyticsEvent.ASEEvent.InvoiceActionFailed -> "ase_invoice_action_failed"
}

private fun AnalyticsEvent.ASEEvent.toAseEventParams(): Map<String, Any> = when (this) {
    is AnalyticsEvent.ASEEvent.DashboardLoaded -> mapOf(
        AnalyticsParams.TOTAL_OUTLETS to totalOutlets,
        AnalyticsParams.IN_PROGRESS to inProgress,
        AnalyticsParams.ASM_PENDING to asmPending,
        AnalyticsParams.PENDING_INVOICES to pendingInvoices,
        AnalyticsParams.LOAD_TIME_MS to loadTimeMs
    )
    is AnalyticsEvent.ASEEvent.DashboardCardTapped -> mapOf(AnalyticsParams.CARD_NAME to cardName)
    is AnalyticsEvent.ASEEvent.SlabChartViewed -> emptyMap()
    is AnalyticsEvent.ASEEvent.OutletListViewed -> mapOf(
        AnalyticsParams.OUTLET_COUNT to outletCount,
        AnalyticsParams.INITIAL_FILTER to initialFilter
    )
    is AnalyticsEvent.ASEEvent.OutletFilterSelected -> mapOf(AnalyticsParams.FILTER to filter)
    is AnalyticsEvent.ASEEvent.OutletSearchUsed -> mapOf(
        AnalyticsParams.QUERY_LENGTH to queryLength,
        AnalyticsParams.RESULTS_COUNT to resultsCount
    )
    is AnalyticsEvent.ASEEvent.OutletCardTapped -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.OUTLET_STATUS to outletStatus,
        AnalyticsParams.ACTION to action
    )
    is AnalyticsEvent.ASEEvent.OutletListScrolled -> emptyMap()
    is AnalyticsEvent.ASEEvent.OutletDetailViewed -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.OUTLET_STATUS to outletStatus,
        AnalyticsParams.COMPLETION_PERCENT to completionPercent,
        AnalyticsParams.CDO_STEPS_COMPLETED to cdoStepsCompleted
    )
    is AnalyticsEvent.ASEEvent.AssetRequestTapped -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.COOLER_TYPE to coolerType,
        AnalyticsParams.CAPACITY to capacity,
        AnalyticsParams.SIGNAGE_TYPE to signageType
    )
    is AnalyticsEvent.ASEEvent.AssetRequestSuccess -> mapOf(AnalyticsParams.OUTLET_ID to outletId)
    is AnalyticsEvent.ASEEvent.AssetRequestFailed -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.ERROR_MESSAGE to errorMessage
    )
    is AnalyticsEvent.ASEEvent.ComplianceTapped -> mapOf(AnalyticsParams.OUTLET_ID to outletId)
    is AnalyticsEvent.ASEEvent.OnboardingStarted -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.IS_NEW to isNew,
        AnalyticsParams.RESUME_STEP to resumeStep
    )
    is AnalyticsEvent.ASEEvent.OnboardingStepViewed -> mapOf(
        AnalyticsParams.STEP_NUMBER to stepNumber,
        AnalyticsParams.STEP_LABEL to stepLabel,
        AnalyticsParams.OUTLET_ID to outletId
    )
    is AnalyticsEvent.ASEEvent.OnboardingStepCompleted -> mapOf(
        AnalyticsParams.STEP_NUMBER to stepNumber,
        AnalyticsParams.STEP_LABEL to stepLabel,
        AnalyticsParams.TIME_ON_STEP_MS to timeOnStepMs,
        AnalyticsParams.OUTLET_ID to outletId
    )
    is AnalyticsEvent.ASEEvent.OnboardingStepAbandoned -> mapOf(
        AnalyticsParams.STEP_NUMBER to stepNumber,
        AnalyticsParams.STEP_LABEL to stepLabel,
        AnalyticsParams.TIME_ON_STEP_MS to timeOnStepMs,
        AnalyticsParams.FIELDS_FILLED_COUNT to fieldsFilledCount
    )
    is AnalyticsEvent.ASEEvent.OnboardingStepError -> mapOf(
        AnalyticsParams.STEP_NUMBER to stepNumber,
        AnalyticsParams.ERROR_MESSAGE to errorMessage,
        AnalyticsParams.OUTLET_ID to outletId
    )
    is AnalyticsEvent.ASEEvent.OnboardingCompleted -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.TOTAL_DURATION_MS to totalDurationMs,
        AnalyticsParams.STEPS_WITH_ERRORS_COUNT to stepsWithErrorsCount
    )
    is AnalyticsEvent.ASEEvent.OnboardingGpsCaptured -> mapOf(
        AnalyticsParams.LAT to lat,
        AnalyticsParams.LNG to lng,
        AnalyticsParams.ACCURACY_METERS to accuracyMeters,
        AnalyticsParams.TIME_TO_CAPTURE_MS to timeToCaptureMs
    )
    is AnalyticsEvent.ASEEvent.OnboardingGpsFailed -> mapOf(AnalyticsParams.ERROR_MESSAGE to errorMessage)
    is AnalyticsEvent.ASEEvent.OnboardingPincodeLookedUp -> mapOf(
        AnalyticsParams.PINCODE to pincode,
        AnalyticsParams.SUCCESS to success,
        AnalyticsParams.AUTO_FILLED_CITY to autoFilledCity,
        AnalyticsParams.AUTO_FILLED_STATE to autoFilledState
    )
    is AnalyticsEvent.ASEEvent.OnboardingOutletTypeSelected -> mapOf(AnalyticsParams.OUTLET_TYPE to outletType)
    is AnalyticsEvent.ASEEvent.OnboardingSlabSelected -> mapOf(
        AnalyticsParams.SLAB_ID to slabId,
        AnalyticsParams.SLAB_LABEL to slabLabel
    )
    is AnalyticsEvent.ASEEvent.OnboardingStockingItemsSelected -> emptyMap()
    is AnalyticsEvent.ASEEvent.OnboardingDistributorSelected -> mapOf(
        AnalyticsParams.DISTRIBUTOR_ID to distributorId,
        AnalyticsParams.DISTRIBUTOR_NAME to distributorName
    )
    is AnalyticsEvent.ASEEvent.OnboardingBankDetailsEntered -> mapOf(
        AnalyticsParams.HAS_UPI to hasUpi,
        AnalyticsParams.HAS_CHEQUE_PHOTO to hasChequePhoto
    )
    is AnalyticsEvent.ASEEvent.OnboardingKycTypeSelected -> mapOf(
        AnalyticsParams.KYC_ID_TYPE to kycType,
        AnalyticsParams.HAS_GST to hasGst
    )
    is AnalyticsEvent.ASEEvent.OnboardingKycPhotoCaptured -> mapOf(AnalyticsParams.PHOTO_TYPE to photoType)
    is AnalyticsEvent.ASEEvent.OnboardingPhotoCaptured -> mapOf(
        AnalyticsParams.SLOT_ID to slotId,
        AnalyticsParams.PHOTO_COUNT to photoCount,
        AnalyticsParams.TOTAL_REQUIRED to totalRequired
    )
    is AnalyticsEvent.ASEEvent.OnboardingPhotoRemoved -> mapOf(AnalyticsParams.SLOT_ID to slotId)
    is AnalyticsEvent.ASEEvent.OnboardingAgreementAccepted -> emptyMap()
    is AnalyticsEvent.ASEEvent.OnboardingAgreementOtpRequested -> emptyMap()
    is AnalyticsEvent.ASEEvent.OnboardingAgreementOtpVerified -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.TIME_TO_VERIFY_MS to timeToVerifyMs
    )
    is AnalyticsEvent.ASEEvent.OnboardingSubmitted -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.TOTAL_DURATION_MS to totalDurationMs
    )
    is AnalyticsEvent.ASEEvent.ComplianceScreenViewed -> mapOf(AnalyticsParams.OUTLET_ID to outletId)
    is AnalyticsEvent.ASEEvent.CompliancePhotoCaptured -> mapOf(
        AnalyticsParams.SLOT_ID to slotId,
        AnalyticsParams.PHOTOS_COUNT to photosCapturedCount
    )
    is AnalyticsEvent.ASEEvent.CompliancePhotoRemoved -> mapOf(AnalyticsParams.SLOT_ID to slotId)
    is AnalyticsEvent.ASEEvent.ComplianceSubmitted -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.COOLER_INSTALLED to coolerInstalled,
        AnalyticsParams.SIGNAGE_INSTALLED to signageInstalled,
        AnalyticsParams.PHOTOS_COUNT to photosCount,
        AnalyticsParams.TIME_ON_SCREEN_MS to timeOnScreenMs
    )
    is AnalyticsEvent.ASEEvent.ComplianceSubmitSuccess -> mapOf(AnalyticsParams.OUTLET_ID to outletId)
    is AnalyticsEvent.ASEEvent.ComplianceSubmitFailed -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.ERROR_MESSAGE to errorMessage
    )
    is AnalyticsEvent.ASEEvent.InvoiceListViewed -> mapOf(
        AnalyticsParams.INVOICE_COUNT to invoiceCount,
        AnalyticsParams.INITIAL_FILTER to initialFilter
    )
    is AnalyticsEvent.ASEEvent.InvoiceFilterSelected -> mapOf(AnalyticsParams.FILTER to filter)
    is AnalyticsEvent.ASEEvent.InvoiceSearchUsed -> mapOf(
        AnalyticsParams.QUERY_LENGTH to queryLength,
        AnalyticsParams.RESULTS_COUNT to resultsCount
    )
    is AnalyticsEvent.ASEEvent.InvoiceDetailViewed -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.INVOICE_STATUS to invoiceStatus,
        AnalyticsParams.OUTLET_NAME to outletName
    )
    is AnalyticsEvent.ASEEvent.InvoiceApproved -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.HAS_NOTE to hasNote,
        AnalyticsParams.TIME_ON_REVIEW_MS to timeOnDetailMs
    )
    is AnalyticsEvent.ASEEvent.InvoiceRejected -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.HAS_NOTE to hasNote,
        AnalyticsParams.REJECTION_REASON_LENGTH to rejectionReasonLength
    )
    is AnalyticsEvent.ASEEvent.InvoiceActionSuccess -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.ACTION to action
    )
    is AnalyticsEvent.ASEEvent.InvoiceActionFailed -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.ERROR_MESSAGE to errorMessage,
        AnalyticsParams.ACTION to action
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// ASM
// ─────────────────────────────────────────────────────────────────────────────

private fun AnalyticsEvent.ASMEvent.toAsmEventName(): String = when (this) {
    is AnalyticsEvent.ASMEvent.DashboardLoaded -> "asm_dashboard_loaded"
    is AnalyticsEvent.ASMEvent.DashboardCardTapped -> "asm_dashboard_card_tapped"
    is AnalyticsEvent.ASMEvent.MyTeamTapped -> "asm_my_team_tapped"
    is AnalyticsEvent.ASMEvent.OutletListViewed -> "asm_outlet_list_viewed"
    is AnalyticsEvent.ASMEvent.OutletFilterSelected -> "asm_outlet_filter_selected"
    is AnalyticsEvent.ASMEvent.OutletReviewViewed -> "asm_outlet_review_viewed"
    is AnalyticsEvent.ASMEvent.OutletApproved -> "asm_outlet_approved"
    is AnalyticsEvent.ASMEvent.OutletRejected -> "asm_outlet_rejected"
    is AnalyticsEvent.ASMEvent.InvoiceListViewed -> "asm_invoice_list_viewed"
    is AnalyticsEvent.ASMEvent.InvoiceFilterSelected -> "asm_invoice_filter_selected"
    is AnalyticsEvent.ASMEvent.InvoiceDetailViewed -> "asm_invoice_detail_viewed"
    is AnalyticsEvent.ASMEvent.InvoiceApproved -> "asm_invoice_approved"
    is AnalyticsEvent.ASMEvent.InvoiceRejected -> "asm_invoice_rejected"
    is AnalyticsEvent.ASMEvent.MyTeamViewed -> "asm_my_team_viewed"
    is AnalyticsEvent.ASMEvent.MyTeamSearched -> "asm_my_team_searched"
    is AnalyticsEvent.ASMEvent.MyTeamRefreshed -> "asm_my_team_refreshed"
}

private fun AnalyticsEvent.ASMEvent.toAsmEventParams(): Map<String, Any> = when (this) {
    is AnalyticsEvent.ASMEvent.DashboardLoaded -> mapOf(
        AnalyticsParams.TOTAL_ASES to totalAses,
        AnalyticsParams.TOTAL_OUTLETS to totalOutlets,
        AnalyticsParams.ACTIVE_OUTLETS to activeOutlets,
        AnalyticsParams.IN_PROGRESS to inProgress,
        AnalyticsParams.SUSPENDED to suspended,
        AnalyticsParams.ASM_PENDING to asmPending,
        AnalyticsParams.PENDING_INVOICES to pendingInvoices,
        AnalyticsParams.LOAD_TIME_MS to loadTimeMs
    )
    is AnalyticsEvent.ASMEvent.DashboardCardTapped -> mapOf(AnalyticsParams.CARD_NAME to cardName)
    is AnalyticsEvent.ASMEvent.MyTeamTapped -> emptyMap()
    is AnalyticsEvent.ASMEvent.OutletListViewed -> mapOf(
        AnalyticsParams.OUTLET_COUNT to outletCount,
        AnalyticsParams.INITIAL_FILTER to initialFilter
    )
    is AnalyticsEvent.ASMEvent.OutletFilterSelected -> mapOf(AnalyticsParams.FILTER to filter)
    is AnalyticsEvent.ASMEvent.OutletReviewViewed -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.OUTLET_STATUS to outletStatus,
        AnalyticsParams.OUTLET_NAME to onboardedByAse
    )
    is AnalyticsEvent.ASMEvent.OutletApproved -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.TIME_ON_REVIEW_MS to timeOnReviewMs
    )
    is AnalyticsEvent.ASMEvent.OutletRejected -> mapOf(
        AnalyticsParams.OUTLET_ID to outletId,
        AnalyticsParams.REJECTION_REASON to rejectionReason,
        AnalyticsParams.TIME_ON_REVIEW_MS to timeOnReviewMs
    )
    is AnalyticsEvent.ASMEvent.InvoiceListViewed -> mapOf(
        AnalyticsParams.INVOICE_COUNT to invoiceCount,
        AnalyticsParams.INITIAL_FILTER to initialFilter
    )
    is AnalyticsEvent.ASMEvent.InvoiceFilterSelected -> mapOf(AnalyticsParams.FILTER to filter)
    is AnalyticsEvent.ASMEvent.InvoiceDetailViewed -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.INVOICE_STATUS to invoiceStatus,
        AnalyticsParams.OUTLET_NAME to submittedByAse
    )
    is AnalyticsEvent.ASMEvent.InvoiceApproved -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.HAS_NOTE to hasNote
    )
    is AnalyticsEvent.ASMEvent.InvoiceRejected -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.HAS_NOTE to hasNote
    )
    is AnalyticsEvent.ASMEvent.MyTeamViewed -> mapOf(AnalyticsParams.ASE_COUNT to aseCount)
    is AnalyticsEvent.ASMEvent.MyTeamSearched -> mapOf(
        AnalyticsParams.QUERY_LENGTH to queryLength,
        AnalyticsParams.RESULTS_COUNT to resultsCount
    )
    is AnalyticsEvent.ASMEvent.MyTeamRefreshed -> mapOf(AnalyticsParams.ASE_COUNT to aseCountAfterRefresh)
}

// ─────────────────────────────────────────────────────────────────────────────
// OUTLET
// ─────────────────────────────────────────────────────────────────────────────

private fun AnalyticsEvent.OutletEvent.toOutletEventName(): String = when (this) {
    is AnalyticsEvent.OutletEvent.WalkthroughStarted -> "walkthrough_started"
    is AnalyticsEvent.OutletEvent.WalkthroughWelcomeCompleted -> "walkthrough_welcome_completed"
    is AnalyticsEvent.OutletEvent.WalkthroughOutletDetailsViewed -> "walkthrough_outlet_details_viewed"
    is AnalyticsEvent.OutletEvent.WalkthroughOutletDetailsCompleted -> "walkthrough_outlet_details_completed"
    is AnalyticsEvent.OutletEvent.WalkthroughAgreementViewed -> "walkthrough_agreement_viewed"
    is AnalyticsEvent.OutletEvent.WalkthroughAgreementCompleted -> "walkthrough_agreement_completed"
    is AnalyticsEvent.OutletEvent.WalkthroughPaymentViewed -> "walkthrough_payment_viewed"
    is AnalyticsEvent.OutletEvent.WalkthroughCompleted -> "walkthrough_completed"
    is AnalyticsEvent.OutletEvent.WalkthroughAbandoned -> "walkthrough_abandoned"
    is AnalyticsEvent.OutletEvent.DashboardLoaded -> "outlet_dashboard_loaded"
    is AnalyticsEvent.OutletEvent.DashboardCardTapped -> "outlet_dashboard_card_tapped"
    is AnalyticsEvent.OutletEvent.NotificationViewed -> "outlet_notification_viewed"
    is AnalyticsEvent.OutletEvent.NotificationDismissed -> "outlet_notification_dismissed"
    is AnalyticsEvent.OutletEvent.UploadInvoiceTapped -> "outlet_upload_invoice_tapped"
    is AnalyticsEvent.OutletEvent.RecentInvoiceTapped -> "outlet_recent_invoice_tapped"
    is AnalyticsEvent.OutletEvent.InvoiceUploadStarted -> "invoice_upload_started"
    is AnalyticsEvent.OutletEvent.InvoiceCaptureMethodSelected -> "invoice_capture_method_selected"
    is AnalyticsEvent.OutletEvent.InvoiceImageCaptured -> "invoice_image_captured"
    is AnalyticsEvent.OutletEvent.InvoiceCropReviewViewed -> "invoice_crop_review_viewed"
    is AnalyticsEvent.OutletEvent.InvoiceCropConfirmed -> "invoice_crop_confirmed"
    is AnalyticsEvent.OutletEvent.InvoiceCropRetake -> "invoice_crop_retake"
    is AnalyticsEvent.OutletEvent.InvoicePreprocessingStarted -> "invoice_preprocessing_started"
    is AnalyticsEvent.OutletEvent.InvoicePreprocessingCompleted -> "invoice_preprocessing_completed"
    is AnalyticsEvent.OutletEvent.InvoiceQualityWarningShown -> "invoice_quality_warning_shown"
    is AnalyticsEvent.OutletEvent.InvoiceQualityWarningDismissed -> "invoice_quality_warning_dismissed"
    is AnalyticsEvent.OutletEvent.InvoiceRetakeGuideShown -> "invoice_retake_guide_shown"
    is AnalyticsEvent.OutletEvent.InvoiceRetakeGuideDismissed -> "invoice_retake_guide_dismissed"
    is AnalyticsEvent.OutletEvent.InvoiceFilterSelected -> "invoice_filter_selected"
    is AnalyticsEvent.OutletEvent.InvoiceAiExtractionStarted -> "invoice_ai_extraction_started"
    is AnalyticsEvent.OutletEvent.InvoiceAiExtractionPhaseChanged -> "invoice_ai_extraction_phase_changed"
    is AnalyticsEvent.OutletEvent.InvoiceAiExtractionCompleted -> "invoice_ai_extraction_completed"
    is AnalyticsEvent.OutletEvent.InvoiceAiExtractionFailed -> "invoice_ai_extraction_failed"
    is AnalyticsEvent.OutletEvent.InvoiceAiExtractionCancelled -> "invoice_ai_extraction_cancelled"
    is AnalyticsEvent.OutletEvent.InvoiceReviewViewed -> "invoice_review_viewed"
    is AnalyticsEvent.OutletEvent.InvoiceReviewFieldEdited -> "invoice_review_field_edited"
    is AnalyticsEvent.OutletEvent.InvoiceReviewLineItemEdited -> "invoice_review_line_item_edited"
    is AnalyticsEvent.OutletEvent.InvoiceReviewLineItemAdded -> "invoice_review_line_item_added"
    is AnalyticsEvent.OutletEvent.InvoiceReviewLineItemRemoved -> "invoice_review_line_item_removed"
    is AnalyticsEvent.OutletEvent.InvoiceRegionScanUsed -> "invoice_region_scan_used"
    is AnalyticsEvent.OutletEvent.InvoiceSubmitted -> "invoice_submitted"
    is AnalyticsEvent.OutletEvent.InvoiceSubmitSuccess -> "invoice_submit_success"
    is AnalyticsEvent.OutletEvent.InvoiceSubmitFailed -> "invoice_submit_failed"
    is AnalyticsEvent.OutletEvent.InvoiceDraftSaved -> "invoice_draft_saved"
    is AnalyticsEvent.OutletEvent.InvoiceListViewed -> "outlet_invoice_list_viewed"
    is AnalyticsEvent.OutletEvent.InvoiceListFilterSelected -> "outlet_invoice_filter_selected"
    is AnalyticsEvent.OutletEvent.InvoiceSearchUsed -> "outlet_invoice_search_used"
    is AnalyticsEvent.OutletEvent.InvoiceDetailViewed -> "outlet_invoice_detail_viewed"
    is AnalyticsEvent.OutletEvent.InvoiceListScrolled -> "outlet_invoice_list_scrolled"
}

private fun AnalyticsEvent.OutletEvent.toOutletEventParams(): Map<String, Any> = when (this) {
    is AnalyticsEvent.OutletEvent.WalkthroughStarted -> mapOf(AnalyticsParams.IS_FIRST_TIME to isFirstTime)
    is AnalyticsEvent.OutletEvent.WalkthroughWelcomeCompleted -> mapOf(AnalyticsParams.TIME_ON_STEP_MS to timeOnStepMs)
    is AnalyticsEvent.OutletEvent.WalkthroughOutletDetailsViewed -> emptyMap()
    is AnalyticsEvent.OutletEvent.WalkthroughOutletDetailsCompleted -> mapOf(AnalyticsParams.TIME_ON_STEP_MS to timeOnStepMs)
    is AnalyticsEvent.OutletEvent.WalkthroughAgreementViewed -> emptyMap()
    is AnalyticsEvent.OutletEvent.WalkthroughAgreementCompleted -> mapOf(AnalyticsParams.TIME_ON_STEP_MS to timeOnStepMs)
    is AnalyticsEvent.OutletEvent.WalkthroughPaymentViewed -> emptyMap()
    is AnalyticsEvent.OutletEvent.WalkthroughCompleted -> mapOf(
        AnalyticsParams.TOTAL_DURATION_MS to totalDurationMs,
        AnalyticsParams.STEPS_COMPLETED to stepsCompleted
    )
    is AnalyticsEvent.OutletEvent.WalkthroughAbandoned -> mapOf(
        AnalyticsParams.STEP_ON_EXIT to stepOnExit,
        AnalyticsParams.TIME_SPENT_MS to timeSpentMs
    )
    is AnalyticsEvent.OutletEvent.DashboardLoaded -> mapOf(
        AnalyticsParams.TIER to tier,
        AnalyticsParams.TOTAL_INVOICES to totalInvoices,
        AnalyticsParams.PENDING_INVOICES to pendingInvoices,
        AnalyticsParams.LAST_PAYOUT_AMOUNT to lastPayoutAmount,
        AnalyticsParams.MONTHLY_PERFORMANCE to monthlyPerformance,
        AnalyticsParams.LOAD_TIME_MS to loadTimeMs
    )
    is AnalyticsEvent.OutletEvent.DashboardCardTapped -> mapOf(AnalyticsParams.CARD_NAME to cardName)
    is AnalyticsEvent.OutletEvent.NotificationViewed -> mapOf(
        AnalyticsParams.NOTIFICATION_ID to notificationId,
        AnalyticsParams.NOTIFICATION_TYPE to notificationType
    )
    is AnalyticsEvent.OutletEvent.NotificationDismissed -> mapOf(
        AnalyticsParams.NOTIFICATION_ID to notificationId,
        AnalyticsParams.NOTIFICATION_TYPE to notificationType
    )
    is AnalyticsEvent.OutletEvent.UploadInvoiceTapped -> mapOf(AnalyticsParams.SOURCE to source)
    is AnalyticsEvent.OutletEvent.RecentInvoiceTapped -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.INVOICE_STATUS to invoiceStatus
    )
    is AnalyticsEvent.OutletEvent.InvoiceUploadStarted -> mapOf(AnalyticsParams.SOURCE to source)
    is AnalyticsEvent.OutletEvent.InvoiceCaptureMethodSelected -> mapOf(AnalyticsParams.METHOD to method)
    is AnalyticsEvent.OutletEvent.InvoiceImageCaptured -> mapOf(
        AnalyticsParams.METHOD to method,
        AnalyticsParams.TIME_TO_CAPTURE_MS to captureTimeMs
    )
    is AnalyticsEvent.OutletEvent.InvoiceCropReviewViewed -> emptyMap()
    is AnalyticsEvent.OutletEvent.InvoiceCropConfirmed -> emptyMap()
    is AnalyticsEvent.OutletEvent.InvoiceCropRetake -> emptyMap()
    is AnalyticsEvent.OutletEvent.InvoicePreprocessingStarted -> emptyMap()
    is AnalyticsEvent.OutletEvent.InvoicePreprocessingCompleted -> mapOf(
        AnalyticsParams.DURATION_MS to durationMs,
        AnalyticsParams.QUALITY_ISSUES_COUNT to qualityIssuesCount,
        AnalyticsParams.HAS_HARD_BLOCK to hasHardBlock
    )
    is AnalyticsEvent.OutletEvent.InvoiceQualityWarningShown -> mapOf(AnalyticsParams.ISSUES to issues)
    is AnalyticsEvent.OutletEvent.InvoiceQualityWarningDismissed -> emptyMap()
    is AnalyticsEvent.OutletEvent.InvoiceRetakeGuideShown -> emptyMap()
    is AnalyticsEvent.OutletEvent.InvoiceRetakeGuideDismissed -> emptyMap()
    is AnalyticsEvent.OutletEvent.InvoiceFilterSelected -> mapOf(AnalyticsParams.FILTER to filter)
    is AnalyticsEvent.OutletEvent.InvoiceAiExtractionStarted -> emptyMap()
    is AnalyticsEvent.OutletEvent.InvoiceAiExtractionPhaseChanged -> mapOf(AnalyticsParams.PHASE to phase)
    is AnalyticsEvent.OutletEvent.InvoiceAiExtractionCompleted -> mapOf(
        AnalyticsParams.ITEMS_EXTRACTED to itemsExtracted,
        AnalyticsParams.DURATION_MS to durationMs,
        AnalyticsParams.CONFIDENCE_AVG to confidenceAvg
    )
    is AnalyticsEvent.OutletEvent.InvoiceAiExtractionFailed -> mapOf(
        AnalyticsParams.REASON to reason,
        AnalyticsParams.RETRY_COUNT to retryCount,
        AnalyticsParams.DURATION_MS to durationMs
    )
    is AnalyticsEvent.OutletEvent.InvoiceAiExtractionCancelled -> mapOf(
        AnalyticsParams.PHASE_ON_CANCEL to phaseOnCancel,
        AnalyticsParams.TIME_ELAPSED_MS to timeElapsedMs
    )
    is AnalyticsEvent.OutletEvent.InvoiceReviewViewed -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.ITEMS_COUNT to itemsCount,
        AnalyticsParams.GRAND_TOTAL to grandTotal,
        AnalyticsParams.WAS_AUTO_FILLED to isAutoFilled
    )
    is AnalyticsEvent.OutletEvent.InvoiceReviewFieldEdited -> mapOf(
        AnalyticsParams.FIELD_NAME to fieldName,
        AnalyticsParams.WAS_AUTO_FILLED to wasAutoFilled
    )
    is AnalyticsEvent.OutletEvent.InvoiceReviewLineItemEdited -> mapOf(
        AnalyticsParams.ITEMS_COUNT to itemIndex,
        AnalyticsParams.FIELD_NAME to fieldName
    )
    is AnalyticsEvent.OutletEvent.InvoiceReviewLineItemAdded -> mapOf(AnalyticsParams.ITEMS_COUNT to currentItemCount)
    is AnalyticsEvent.OutletEvent.InvoiceReviewLineItemRemoved -> mapOf(AnalyticsParams.ITEMS_COUNT to itemIndex)
    is AnalyticsEvent.OutletEvent.InvoiceRegionScanUsed -> mapOf(
        AnalyticsParams.ITEMS_COUNT to itemIndex,
        AnalyticsParams.FIELD_NAME to fieldName
    )
    is AnalyticsEvent.OutletEvent.InvoiceSubmitted -> mapOf(
        AnalyticsParams.ITEMS_COUNT to itemsCount,
        AnalyticsParams.GRAND_TOTAL to grandTotal,
        AnalyticsParams.WAS_AUTO_FILLED to wasAutoFilled,
        AnalyticsParams.EDITS_MADE_COUNT to editsMadeCount,
        AnalyticsParams.TIME_ON_REVIEW_MS to timeOnReviewMs
    )
    is AnalyticsEvent.OutletEvent.InvoiceSubmitSuccess -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.TOTAL_UPLOAD_DURATION_MS to totalUploadDurationMs
    )
    is AnalyticsEvent.OutletEvent.InvoiceSubmitFailed -> mapOf(
        AnalyticsParams.ERROR_MESSAGE to errorMessage,
        AnalyticsParams.RETRY_COUNT to retryCount
    )
    is AnalyticsEvent.OutletEvent.InvoiceDraftSaved -> mapOf(AnalyticsParams.ITEMS_COUNT to itemsCount)
    is AnalyticsEvent.OutletEvent.InvoiceListViewed -> mapOf(
        AnalyticsParams.INVOICE_COUNT to invoiceCount,
        AnalyticsParams.INITIAL_FILTER to initialFilter
    )
    is AnalyticsEvent.OutletEvent.InvoiceListFilterSelected -> mapOf(AnalyticsParams.FILTER to filter)
    is AnalyticsEvent.OutletEvent.InvoiceSearchUsed -> mapOf(
        AnalyticsParams.QUERY_LENGTH to queryLength,
        AnalyticsParams.RESULTS_COUNT to resultsCount
    )
    is AnalyticsEvent.OutletEvent.InvoiceDetailViewed -> mapOf(
        AnalyticsParams.INVOICE_ID to invoiceId,
        AnalyticsParams.INVOICE_STATUS to invoiceStatus,
        AnalyticsParams.GRAND_TOTAL to invoiceAmount
    )
    is AnalyticsEvent.OutletEvent.InvoiceListScrolled -> emptyMap()
}

// ─────────────────────────────────────────────────────────────────────────────
// PROFILE
// ─────────────────────────────────────────────────────────────────────────────

private fun AnalyticsEvent.ProfileEvent.toProfileEventName(): String = when (this) {
    is AnalyticsEvent.ProfileEvent.ProfileViewed -> "profile_viewed"
    is AnalyticsEvent.ProfileEvent.ProfileMyTeamTapped -> "profile_my_team_tapped"
}

private fun AnalyticsEvent.ProfileEvent.toProfileEventParams(): Map<String, Any> = when (this) {
    is AnalyticsEvent.ProfileEvent.ProfileViewed -> mapOf(
        AnalyticsParams.USER_ROLE to userRole,
        AnalyticsParams.HAS_KYC_DATA to hasKycData
    )
    is AnalyticsEvent.ProfileEvent.ProfileMyTeamTapped -> emptyMap()
}

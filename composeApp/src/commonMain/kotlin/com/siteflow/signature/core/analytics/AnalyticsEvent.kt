package com.siteflow.signature.core.analytics

sealed class AnalyticsEvent {

    // ─────────────────────────────────────────────────────────────────────────
    // GLOBAL
    // ─────────────────────────────────────────────────────────────────────────
    sealed class GlobalEvent : AnalyticsEvent() {

        // App Lifecycle
        data class AppOpened(
            val platform: String,
            val osVersion: String,
            val appVersion: String,
            val buildNumber: String,
            val isFreshInstall: Boolean = false
        ) : GlobalEvent()
        data object AppBackgrounded : GlobalEvent()
        data object AppCrashed : GlobalEvent()
        data class SessionStarted(
            val userRole: String,
            val userId: String,
            val businessId: String,
            val isReturningUser: Boolean
        ) : GlobalEvent()
        data object SessionEnded : GlobalEvent()
        data class DeepLinkOpened(val url: String) : GlobalEvent()

        // Authentication
        data class LoginScreenViewed(val isAutoRedirect: Boolean = false) : GlobalEvent()
        data class LoginSubmitted(val mobileNumberLength: Int) : GlobalEvent()
        data class LoginFailed(
            val errorMessage: String,
            val errorCode: String
        ) : GlobalEvent()
        data class OtpScreenViewed(val mobileNumberMasked: String = "") : GlobalEvent()
        data class OtpEntered(val timeToEnterMs: Long = 0) : GlobalEvent()
        data class OtpResendTapped(
            val resendAttemptNumber: Int,
            val countdownRemaining: Int
        ) : GlobalEvent()
        data object OtpEditNumberTapped : GlobalEvent()
        data class OtpVerified(
            val userRole: String,
            val timeToVerifyMs: Long = 0,
            val isFirstLogin: Boolean = false
        ) : GlobalEvent()
        data class OtpVerificationFailed(
            val errorMessage: String,
            val attemptNumber: Int = 1
        ) : GlobalEvent()
        data class SessionExpired(
            val screenOnExpiry: String = "",
            val sessionDurationMs: Long = 0
        ) : GlobalEvent()
        data class LogoutTapped(
            val userRole: String = "",
            val screen: String = ""
        ) : GlobalEvent()
        data class LogoutConfirmed(val sessionDurationMs: Long = 0) : GlobalEvent()

        // Navigation
        data class ScreenViewed(
            val screenName: String,
            val previousScreen: String,
            val userRole: String
        ) : GlobalEvent()
        data class BottomTabTapped(
            val tab: String,
            val previousTab: String = "",
            val userRole: String = ""
        ) : GlobalEvent()
        data class BackButtonTapped(val screenName: String = "") : GlobalEvent()

        // Network & Performance
        data class NetworkStatusChanged(
            val newStatus: String,
            val screen: String = ""
        ) : GlobalEvent()
        data class ApiCallCompleted(
            val endpoint: String,
            val method: String = "",
            val statusCode: Int = 0,
            val latencyMs: Long = 0,
            val isRetry: Boolean = false
        ) : GlobalEvent()
        data class ApiCallFailed(
            val endpoint: String,
            val errorType: String = "",
            val errorMessage: String = "",
            val retryCount: Int = 0
        ) : GlobalEvent()
        data class TokenRefreshed(val latencyMs: Long = 0) : GlobalEvent()
        data class TokenRefreshFailed(val errorCode: String = "") : GlobalEvent()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ASE
    // ─────────────────────────────────────────────────────────────────────────
    sealed class ASEEvent : AnalyticsEvent() {

        // Dashboard
        data class DashboardLoaded(
            val totalOutlets: Int = 0,
            val inProgress: Int = 0,
            val asmPending: Int = 0,
            val pendingInvoices: Int = 0,
            val loadTimeMs: Long = 0
        ) : ASEEvent()
        data class DashboardCardTapped(val cardName: String) : ASEEvent()
        data object SlabChartViewed : ASEEvent()

        // Outlet List
        data class OutletListViewed(
            val outletCount: Int = 0,
            val initialFilter: String = "All"
        ) : ASEEvent()
        data class OutletFilterSelected(val filter: String) : ASEEvent()
        data class OutletSearchUsed(
            val queryLength: Int = 0,
            val resultsCount: Int = 0
        ) : ASEEvent()
        data class OutletCardTapped(
            val outletId: String,
            val outletStatus: String = "",
            val action: String = ""
        ) : ASEEvent()
        data object OutletListScrolled : ASEEvent()

        // Outlet Details
        data class OutletDetailViewed(
            val outletId: String,
            val outletStatus: String = "",
            val completionPercent: Int = 0,
            val signatureStepsCompleted: Int = 0
        ) : ASEEvent()
        data class AssetRequestTapped(
            val outletId: String,
            val coolerType: String = "",
            val capacity: String = "",
            val signageType: String = ""
        ) : ASEEvent()
        data class AssetRequestSuccess(val outletId: String) : ASEEvent()
        data class AssetRequestFailed(
            val outletId: String,
            val errorMessage: String
        ) : ASEEvent()
        data class ComplianceTapped(val outletId: String) : ASEEvent()

        // Onboarding Funnel
        data class OnboardingStarted(
            val outletId: String,
            val isNew: Boolean = true,
            val resumeStep: Int = 0
        ) : ASEEvent()
        data class OnboardingStepViewed(
            val stepNumber: Int,
            val stepLabel: String,
            val outletId: String = ""
        ) : ASEEvent()
        data class OnboardingStepCompleted(
            val stepNumber: Int,
            val stepLabel: String,
            val timeOnStepMs: Long,
            val outletId: String = ""
        ) : ASEEvent()
        data class OnboardingStepAbandoned(
            val stepNumber: Int,
            val stepLabel: String,
            val timeOnStepMs: Long = 0,
            val fieldsFilledCount: Int = 0
        ) : ASEEvent()
        data class OnboardingStepError(
            val stepNumber: Int,
            val errorMessage: String,
            val outletId: String = ""
        ) : ASEEvent()
        data class OnboardingCompleted(
            val outletId: String,
            val totalDurationMs: Long = 0,
            val stepsWithErrorsCount: Int = 0
        ) : ASEEvent()

        // Onboarding Step Events
        data class OnboardingGpsCaptured(
            val lat: Double = 0.0,
            val lng: Double = 0.0,
            val accuracyMeters: Float = 0f,
            val timeToCaptureMs: Long = 0
        ) : ASEEvent()
        data class OnboardingGpsFailed(val errorMessage: String) : ASEEvent()
        data class OnboardingPincodeLookedUp(
            val pincode: String = "",
            val success: Boolean = true,
            val autoFilledCity: String = "",
            val autoFilledState: String = ""
        ) : ASEEvent()
        data class OnboardingOutletTypeSelected(val outletType: String) : ASEEvent()
        data class OnboardingSlabSelected(
            val slabId: String,
            val slabLabel: String
        ) : ASEEvent()
        data object OnboardingStockingItemsSelected : ASEEvent()
        data class OnboardingDistributorSelected(
            val distributorId: String,
            val distributorName: String = ""
        ) : ASEEvent()
        data class OnboardingBankDetailsEntered(
            val hasUpi: Boolean = false,
            val hasChequePhoto: Boolean = false
        ) : ASEEvent()
        data class OnboardingKycTypeSelected(
            val kycType: String,
            val hasGst: Boolean = false
        ) : ASEEvent()
        data class OnboardingKycPhotoCaptured(val photoType: String = "") : ASEEvent()
        data class OnboardingPhotoCaptured(
            val slotId: String = "",
            val photoCount: Int = 0,
            val totalRequired: Int = 0
        ) : ASEEvent()
        data class OnboardingPhotoRemoved(val slotId: String = "") : ASEEvent()
        data object OnboardingAgreementAccepted : ASEEvent()
        data object OnboardingAgreementOtpRequested : ASEEvent()
        data class OnboardingAgreementOtpVerified(
            val outletId: String = "",
            val timeToVerifyMs: Long = 0
        ) : ASEEvent()
        data class OnboardingSubmitted(
            val outletId: String = "",
            val totalDurationMs: Long = 0
        ) : ASEEvent()

        // Compliance
        data class ComplianceScreenViewed(val outletId: String = "") : ASEEvent()
        data class CompliancePhotoCaptured(
            val slotId: String = "",
            val photosCapturedCount: Int = 0
        ) : ASEEvent()
        data class CompliancePhotoRemoved(val slotId: String = "") : ASEEvent()
        data class ComplianceSubmitted(
            val outletId: String = "",
            val coolerInstalled: Boolean = false,
            val signageInstalled: Boolean = false,
            val photosCount: Int = 0,
            val timeOnScreenMs: Long = 0
        ) : ASEEvent()
        data class ComplianceSubmitSuccess(val outletId: String = "") : ASEEvent()
        data class ComplianceSubmitFailed(
            val outletId: String = "",
            val errorMessage: String
        ) : ASEEvent()

        // Invoices
        data class InvoiceListViewed(
            val invoiceCount: Int = 0,
            val initialFilter: String = "All"
        ) : ASEEvent()
        data class InvoiceFilterSelected(val filter: String) : ASEEvent()
        data class InvoiceSearchUsed(
            val queryLength: Int = 0,
            val resultsCount: Int = 0
        ) : ASEEvent()
        data class InvoiceDetailViewed(
            val invoiceId: String,
            val invoiceStatus: String = "",
            val outletName: String = ""
        ) : ASEEvent()
        data class InvoiceApproved(
            val invoiceId: String,
            val hasNote: Boolean = false,
            val timeOnDetailMs: Long = 0
        ) : ASEEvent()
        data class InvoiceRejected(
            val invoiceId: String,
            val hasNote: Boolean = false,
            val rejectionReasonLength: Int = 0
        ) : ASEEvent()
        data class InvoiceActionSuccess(
            val invoiceId: String,
            val action: String = ""
        ) : ASEEvent()
        data class InvoiceActionFailed(
            val invoiceId: String,
            val errorMessage: String,
            val action: String = ""
        ) : ASEEvent()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ASM
    // ─────────────────────────────────────────────────────────────────────────
    sealed class ASMEvent : AnalyticsEvent() {

        // Dashboard
        data class DashboardLoaded(
            val totalAses: Int = 0,
            val totalOutlets: Int = 0,
            val activeOutlets: Int = 0,
            val inProgress: Int = 0,
            val suspended: Int = 0,
            val asmPending: Int = 0,
            val pendingInvoices: Int = 0,
            val loadTimeMs: Long = 0
        ) : ASMEvent()
        data class DashboardCardTapped(val cardName: String) : ASMEvent()
        data object MyTeamTapped : ASMEvent()

        // Outlet Review
        data class OutletListViewed(
            val outletCount: Int = 0,
            val initialFilter: String = "All"
        ) : ASMEvent()
        data class OutletFilterSelected(val filter: String) : ASMEvent()
        data class OutletReviewViewed(
            val outletId: String,
            val outletStatus: String = "",
            val onboardedByAse: String = ""
        ) : ASMEvent()
        data class OutletApproved(
            val outletId: String,
            val timeOnReviewMs: Long = 0
        ) : ASMEvent()
        data class OutletRejected(
            val outletId: String,
            val rejectionReason: String = "",
            val timeOnReviewMs: Long = 0
        ) : ASMEvent()

        // Invoices
        data class InvoiceListViewed(
            val invoiceCount: Int = 0,
            val initialFilter: String = "All"
        ) : ASMEvent()
        data class InvoiceFilterSelected(val filter: String) : ASMEvent()
        data class InvoiceDetailViewed(
            val invoiceId: String,
            val invoiceStatus: String = "",
            val submittedByAse: String = ""
        ) : ASMEvent()
        data class InvoiceApproved(
            val invoiceId: String,
            val hasNote: Boolean = false
        ) : ASMEvent()
        data class InvoiceRejected(
            val invoiceId: String,
            val hasNote: Boolean = false
        ) : ASMEvent()

        // My Team
        data class MyTeamViewed(val aseCount: Int = 0) : ASMEvent()
        data class MyTeamSearched(
            val queryLength: Int = 0,
            val resultsCount: Int = 0
        ) : ASMEvent()
        data class MyTeamRefreshed(val aseCountAfterRefresh: Int = 0) : ASMEvent()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OUTLET
    // ─────────────────────────────────────────────────────────────────────────
    sealed class OutletEvent : AnalyticsEvent() {

        // Walkthrough
        data class WalkthroughStarted(val isFirstTime: Boolean = true) : OutletEvent()
        data class WalkthroughWelcomeCompleted(val timeOnStepMs: Long = 0) : OutletEvent()
        data object WalkthroughOutletDetailsViewed : OutletEvent()
        data class WalkthroughOutletDetailsCompleted(val timeOnStepMs: Long = 0) : OutletEvent()
        data object WalkthroughAgreementViewed : OutletEvent()
        data class WalkthroughAgreementCompleted(val timeOnStepMs: Long = 0) : OutletEvent()
        data object WalkthroughPaymentViewed : OutletEvent()
        data class WalkthroughCompleted(
            val totalDurationMs: Long = 0,
            val stepsCompleted: Int = 0
        ) : OutletEvent()
        data class WalkthroughAbandoned(
            val stepOnExit: String = "",
            val timeSpentMs: Long = 0
        ) : OutletEvent()

        // Dashboard
        data class DashboardLoaded(
            val tier: String = "",
            val totalInvoices: Int = 0,
            val pendingInvoices: Int = 0,
            val lastPayoutAmount: String = "",
            val monthlyPerformance: String = "",
            val loadTimeMs: Long = 0
        ) : OutletEvent()
        data class DashboardCardTapped(val cardName: String) : OutletEvent()
        data class NotificationViewed(
            val notificationId: String = "",
            val notificationType: String = ""
        ) : OutletEvent()
        data class NotificationDismissed(
            val notificationId: String = "",
            val notificationType: String = ""
        ) : OutletEvent()
        data class UploadInvoiceTapped(val source: String = "") : OutletEvent()
        data class RecentInvoiceTapped(
            val invoiceId: String,
            val invoiceStatus: String = ""
        ) : OutletEvent()

        // Invoice Upload Funnel
        data class InvoiceUploadStarted(val source: String = "") : OutletEvent()
        data class InvoiceCaptureMethodSelected(val method: String) : OutletEvent()
        data class InvoiceImageCaptured(
            val method: String = "",
            val captureTimeMs: Long = 0
        ) : OutletEvent()
        data object InvoiceCropReviewViewed : OutletEvent()
        data object InvoiceCropConfirmed : OutletEvent()
        data object InvoiceCropRetake : OutletEvent()
        data object InvoicePreprocessingStarted : OutletEvent()
        data class InvoicePreprocessingCompleted(
            val durationMs: Long = 0,
            val qualityIssuesCount: Int = 0,
            val hasHardBlock: Boolean = false
        ) : OutletEvent()
        data class InvoiceQualityWarningShown(val issues: String = "") : OutletEvent()
        data object InvoiceQualityWarningDismissed : OutletEvent()
        data object InvoiceRetakeGuideShown : OutletEvent()
        data object InvoiceRetakeGuideDismissed : OutletEvent()
        data class InvoiceFilterSelected(val filter: String) : OutletEvent()
        data object InvoiceAiExtractionStarted : OutletEvent()
        data class InvoiceAiExtractionPhaseChanged(val phase: String) : OutletEvent()
        data class InvoiceAiExtractionCompleted(
            val itemsExtracted: Int,
            val durationMs: Long = 0,
            val confidenceAvg: Double = 0.0
        ) : OutletEvent()
        data class InvoiceAiExtractionFailed(
            val reason: String,
            val retryCount: Int = 0,
            val durationMs: Long = 0
        ) : OutletEvent()
        data class InvoiceAiExtractionCancelled(
            val phaseOnCancel: String = "",
            val timeElapsedMs: Long = 0
        ) : OutletEvent()
        data class InvoiceReviewViewed(
            val invoiceId: String,
            val itemsCount: Int,
            val grandTotal: Double = 0.0,
            val isAutoFilled: Boolean = false
        ) : OutletEvent()
        data class InvoiceReviewFieldEdited(
            val fieldName: String,
            val wasAutoFilled: Boolean = false
        ) : OutletEvent()
        data class InvoiceReviewLineItemEdited(
            val itemIndex: Int = 0,
            val fieldName: String = ""
        ) : OutletEvent()
        data class InvoiceReviewLineItemAdded(val currentItemCount: Int = 0) : OutletEvent()
        data class InvoiceReviewLineItemRemoved(val itemIndex: Int = 0) : OutletEvent()
        data class InvoiceRegionScanUsed(
            val itemIndex: Int = 0,
            val fieldName: String = ""
        ) : OutletEvent()
        data class InvoiceSubmitted(
            val itemsCount: Int,
            val grandTotal: Double,
            val wasAutoFilled: Boolean = false,
            val editsMadeCount: Int = 0,
            val timeOnReviewMs: Long = 0
        ) : OutletEvent()
        data class InvoiceSubmitSuccess(
            val invoiceId: String,
            val totalUploadDurationMs: Long = 0
        ) : OutletEvent()
        data class InvoiceSubmitFailed(
            val errorMessage: String,
            val retryCount: Int = 0
        ) : OutletEvent()
        data class InvoiceDraftSaved(val itemsCount: Int = 0) : OutletEvent()

        // Invoice List
        data class InvoiceListViewed(
            val invoiceCount: Int = 0,
            val initialFilter: String = "All"
        ) : OutletEvent()
        data class InvoiceListFilterSelected(val filter: String) : OutletEvent()
        data class InvoiceSearchUsed(
            val queryLength: Int = 0,
            val resultsCount: Int = 0
        ) : OutletEvent()
        data class InvoiceDetailViewed(
            val invoiceId: String,
            val invoiceStatus: String = "",
            val invoiceAmount: Double = 0.0
        ) : OutletEvent()
        data object InvoiceListScrolled : OutletEvent()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PROFILE
    // ─────────────────────────────────────────────────────────────────────────
    sealed class ProfileEvent : AnalyticsEvent() {
        data class ProfileViewed(
            val userRole: String = "",
            val hasKycData: Boolean = false
        ) : ProfileEvent()
        data object ProfileMyTeamTapped : ProfileEvent()
    }
}

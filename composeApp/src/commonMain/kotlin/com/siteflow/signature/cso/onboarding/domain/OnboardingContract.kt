package com.siteflow.signature.cso.onboarding.domain

import com.siteflow.signature.cso.onboarding.data.Classification
import com.siteflow.signature.cso.onboarding.data.GpsLocation
import com.siteflow.signature.cso.onboarding.data.PhotoSlot
import com.siteflow.signature.cso.onboarding.data.defaultPhotoSlots
import com.siteflow.signature.cso.onboarding.data.dto.DistributorDto
import com.siteflow.signature.cso.onboarding.data.dto.SlabDto

/**
 * Payout model for an outlet.
 * DYNAMIC = volume-based slab payout (no extra input needed).
 * FIXED   = fixed monthly payout (requires volume commitment + monthly amount).
 */
enum class PayoutType(val label: String, val description: String) {
    DYNAMIC("Volume-based Payout Slab", "Volume-based slab payout calculated monthly"),
    FIXED("Fixed Payout Slab", "Fixed monthly payout with committed volume")
}

/**
 * Payment method mode selected by the user in the Bank Details step.
 * BANK = traditional bank account details + cancelled cheque required.
 * UPI  = UPI ID only; cancelled cheque is not required.
 */
enum class BankPaymentMode { BANK, UPI }

/**
 * Account type for bank accounts.
 */
enum class BankAccountType(val label: String) {
    SAVINGS("Savings Account"),
    CURRENT("Current Account")
}

// ── Actions ──
sealed interface OnboardingAction {
    // Step 1 - Basic Details
    data class OutletNameChanged(val value: String) : OnboardingAction
    data class OwnerNameChanged(val value: String) : OnboardingAction
    data class ContactNumberChanged(val value: String) : OnboardingAction
    data class WhatsAppNumberChanged(val value: String) : OnboardingAction
    data class SameAsContactToggled(val checked: Boolean) : OnboardingAction
    data class EmailChanged(val value: String) : OnboardingAction
    data class OutletTypeSelected(val value: String) : OnboardingAction
    data class AddressChanged(val value: String) : OnboardingAction
    data class LandmarkChanged(val value: String) : OnboardingAction
    data class LocalityChanged(val value: String) : OnboardingAction
    data class PincodeChanged(val value: String) : OnboardingAction
    data class CityChanged(val value: String) : OnboardingAction
    data class StateChanged(val value: String) : OnboardingAction
    data object LookupPincode : OnboardingAction
    data object RequestLocation : OnboardingAction
    data object RetryLocation : OnboardingAction
    
    // Step 2 - Classification & Stocking
    data object FetchSlabs : OnboardingAction
    data class SlabSelected(val slabId: String) : OnboardingAction
    data class StockingItemToggled(val item: String) : OnboardingAction
    data class PayoutTypeSelected(val type: PayoutType) : OnboardingAction
    data class FixedMonthlyVolumeChanged(val value: String) : OnboardingAction
    data class FixedMonthlyAmountChanged(val value: String) : OnboardingAction

    // Step 3 - Photos
    data class PhotoCaptured(val slotId: String, val path: String) : OnboardingAction
    data class PhotoRemoved(val slotId: String) : OnboardingAction

    // Step 4 - Distributor & Bank Details
    data object FetchDistributors : OnboardingAction
    data class DistributorSelected(val id: String, val name: String) : OnboardingAction
    data class AccountHolderChanged(val value: String) : OnboardingAction
    data class BankNameChanged(val value: String) : OnboardingAction
    data class AccountNumberChanged(val value: String) : OnboardingAction
    data class IfscCodeChanged(val value: String) : OnboardingAction
    data class UpiIdChanged(val value: String) : OnboardingAction
    data class BranchNameChanged(val value: String) : OnboardingAction
    data class CancelledChequeCaptured(val path: String) : OnboardingAction
    data object CancelledChequeRemoved : OnboardingAction
    data class PaymentModeSelected(val mode: BankPaymentMode) : OnboardingAction
    data class BankAccountTypeSelected(val type: BankAccountType) : OnboardingAction

    // Step 4 - KYC Details
    data class KycIdTypeSelected(val value: String) : OnboardingAction
    data class KycIdNumberChanged(val value: String) : OnboardingAction
    data class KycGstNumberChanged(val value: String) : OnboardingAction
    data class KycLocationTypeSelected(val value: String) : OnboardingAction
    data class KycPhotoCaptured(val type: String, val path: String) : OnboardingAction
    data class KycPhotoRemoved(val type: String) : OnboardingAction

    // Step 6 - Agreement
    data class AgreementAccepted(val accepted: Boolean) : OnboardingAction
    data object RequestAgreementOtp : OnboardingAction
    data class AgreementOtpChanged(val value: String) : OnboardingAction
    data object VerifyAgreementOtp : OnboardingAction
    data object SubmitOnboarding : OnboardingAction
    
    data object ContinueToNextStep : OnboardingAction
    data object GoBack : OnboardingAction
    data class SetCurrentStep(val step: Int) : OnboardingAction
    data class SetOutletId(val outletId: String) : OnboardingAction

    /** Resets entire onboarding form to blank — used when starting a new onboarding. */
    data object ResetState : OnboardingAction
}

data class OnboardingState(
    val currentStep: Int = 1,
    val totalSteps: Int = 6,

    // Step 1 — Basic Details
    val outletName: String = "",
    val ownerName: String = "",
    val contactNumber: String = "",
    val whatsAppNumber: String = "",
    val sameAsContact: Boolean = false,
    val email: String = "",
    val outletType: String = "",
    val address: String = "",
    val landmark: String = "",
    val locality: String = "",
    val pincode: String = "",
    val city: String = "",
    val state: String = "",
    val gpsLocation: GpsLocation? = null,
    val isCapturingGps: Boolean = false,

    // Pincode lookup
    val locationId: String = "",
    val isPincodeLoading: Boolean = false,
    val pincodeError: String? = null,

    // Created outlet tracking
    val createdOutletId: String? = null,

    // Step 2 — Classification & Stocking
    val classification: Classification = Classification.SILVER,
    val selectedClassification: String = "",  // backend value e.g. "A_CLASS"
    val selectedSlabId: String = "",  // slab UUID from API
    val slabs: List<SlabDto> = emptyList(),
    val isSlabsLoading: Boolean = false,
    val stockingCommitment: List<String> = emptyList(),
    val payoutType: PayoutType = PayoutType.DYNAMIC,
    val fixedMonthlyVolume: String = "",   // cases/month (numeric)
    val fixedMonthlyAmount: String = "",   // ₹/month   (numeric)

    // Step 3 — Photos
    val photoSlots: List<PhotoSlot> = defaultPhotoSlots(),

    // Step 4 — Distributor & Bank Details
    val distributorName: String = "",
    val selectedDistributorId: String = "",
    val distributors: List<DistributorDto> = emptyList(),
    val isDistributorsLoading: Boolean = false,
    val accountHolderName: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val upiId: String = "",
    val branchName: String = "",
    val cancelledChequePath: String? = null,
    val cancelledChequeCapturedAt: String? = null,
    val cancelledChequeGpsLabel: String? = null,
    val bankPaymentMode: BankPaymentMode = BankPaymentMode.BANK,
    val bankAccountType: BankAccountType = BankAccountType.SAVINGS,

    // Step 4 — KYC Details
    val kycIdType: String = "",
    val kycIdNumber: String = "",
    val kycGstNumber: String = "",
    val kycLocationType: String = "",
    val kycIdProofPath: String? = null,
    val kycIdProofCapturedAt: String? = null,
    val kycIdProofGpsLabel: String? = null,
    val kycGstCertificatePath: String? = null,
    val kycGstCertificateCapturedAt: String? = null,
    val kycGstCertificateGpsLabel: String? = null,
    val kycLocationProofPath: String? = null,
    val kycLocationProofCapturedAt: String? = null,
    val kycLocationProofGpsLabel: String? = null,

    // Step 6 — Agreement
    val agreementAccepted: Boolean = false,
    val agreementOtp: String = "",
    val isOtpSent: Boolean = false,
    val agreementOtpCountdown: Int = 0,
    val canResendAgreementOtp: Boolean = false,

    val isLoading: Boolean = false,
    val error: String? = null
) {
    val outletTypes = listOf(
        "Grocery",
        "E&D",
        "Highway Dhaba",
        "Paan Shop",
        "OAGS (MT)",
        "Bakery",
        "Convenience",
        "Bus Stand",
        "Others"
    )

    val capturedPhotoCount: Int
        get() = photoSlots.count { it.imagePath != null }
}

sealed interface OnboardingEvent {
    data object NavigateBack : OnboardingEvent
    data object NavigateToStep2 : OnboardingEvent
    data object NavigateToStep3 : OnboardingEvent
    data object NavigateToStep4 : OnboardingEvent
    data object NavigateToStep5 : OnboardingEvent
    data object NavigateToStep6 : OnboardingEvent
    data object AgreementOtpSent : OnboardingEvent
    data object OnboardingSubmitted : OnboardingEvent
}

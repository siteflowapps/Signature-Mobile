package com.siteflow.signature.cso.onboarding.domain

/**
 * Single Responsibility: Validation logic for the Onboarding flow.
 * Keeps the State and ViewModel clean.
 */
object OnboardingValidator {

    fun isStep1Valid(state: OnboardingState): Boolean {
        return state.outletName.isNotBlank() &&
                state.ownerName.isNotBlank() &&
                state.contactNumber.length >= 10 &&
                state.outletType.isNotBlank() &&
                state.address.isNotBlank() &&
                state.pincode.length == 6 &&
                state.city.isNotBlank() &&
                state.locationId.isNotBlank() &&
                state.gpsLocation != null
    }

    fun isStep2Valid(state: OnboardingState): Boolean {
        // Dynamic payout: always valid (slab auto-selected from API)
        // Fixed payout: require both volume and amount fields
        return when (state.payoutType) {
            PayoutType.DYNAMIC -> true
            PayoutType.FIXED ->
                state.fixedMonthlyVolume.isNotBlank() &&
                state.fixedMonthlyAmount.isNotBlank()
        }
    }

    /** Step 3 - Distributor & Bank */
    fun isStep3Valid(state: OnboardingState): Boolean {
        val distributorFilled = state.distributorName.isNotBlank()
        return when (state.bankPaymentMode) {
            BankPaymentMode.UPI ->
                distributorFilled && isValidUpiId(state.upiId)
            BankPaymentMode.BANK ->
                distributorFilled &&
                state.accountHolderName.isNotBlank() &&
                state.bankName.isNotBlank() &&
                state.accountNumber.isNotBlank() &&
                state.ifscCode.length == 11
        }
    }

    /**
     * Validates a UPI VPA (Virtual Payment Address).
     * Format: username@bankhandle
     *   - username: 2–256 chars, allowed: a-z A-Z 0-9 . - _
     *   - handle  : 2–64  chars, alphanumeric (e.g. okicici, ybl, paytm, kotak811, ibl)
     * Disallows: spaces, multiple @, special chars like # $ % & *
     */
    fun isValidUpiId(upiId: String): Boolean {
        if (upiId.isBlank()) return false
        val regex = Regex("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z0-9]{2,64}$")
        return regex.matches(upiId.trim())
    }

    /** Step 4 - KYC */
    fun isStep4Valid(state: OnboardingState): Boolean {
        return state.kycIdType.isNotBlank() &&
                state.kycIdNumber.isNotBlank() &&
                state.kycLocationType.isNotBlank() &&
                state.kycIdProofPath != null &&
                state.kycLocationProofPath != null
    }

    /** Step 5 - Photos (first 3 are required) */
    fun isStep5Valid(state: OnboardingState): Boolean {
        return state.photoSlots.filter { it.required }.all { it.imagePath != null }
    }

    /** Step 6 - Agreement */
    fun isStep6Valid(state: OnboardingState): Boolean {
        return state.agreementAccepted
    }
}

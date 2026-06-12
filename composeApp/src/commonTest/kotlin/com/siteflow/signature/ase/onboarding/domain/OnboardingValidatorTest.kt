package com.siteflow.signature.cso.onboarding.domain

import com.siteflow.signature.cso.onboarding.data.GpsLocation
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingValidatorTest {

    private val validStep1State = OnboardingState(
        outletName = "Test Store",
        ownerName = "Test Owner",
        contactNumber = "9999999999",
        outletType = "Kirana / General Store",
        address = "Test Address",
        pincode = "400001",
        city = "Mumbai",
        state = "Maharashtra",
        locationId = "loc-id-123",
        gpsLocation = GpsLocation(19.0, 72.0, "")
    )

    // ── Step 1: Basic Details ──

    @Test
    fun `isStep1Valid - all required fields present`() {
        assertTrue(OnboardingValidator.isStep1Valid(validStep1State))
    }

    @Test
    fun `isStep1Valid - fails when outletName is blank`() {
        assertFalse(OnboardingValidator.isStep1Valid(validStep1State.copy(outletName = "")))
    }

    @Test
    fun `isStep1Valid - fails when ownerName is blank`() {
        assertFalse(OnboardingValidator.isStep1Valid(validStep1State.copy(ownerName = "")))
    }

    @Test
    fun `isStep1Valid - fails when contactNumber is short`() {
        assertFalse(OnboardingValidator.isStep1Valid(validStep1State.copy(contactNumber = "12345")))
    }

    @Test
    fun `isStep1Valid - fails when outletType is blank`() {
        assertFalse(OnboardingValidator.isStep1Valid(validStep1State.copy(outletType = "")))
    }

    @Test
    fun `isStep1Valid - fails when address is blank`() {
        assertFalse(OnboardingValidator.isStep1Valid(validStep1State.copy(address = "")))
    }

    @Test
    fun `isStep1Valid - fails when pincode is not 6 digits`() {
        assertFalse(OnboardingValidator.isStep1Valid(validStep1State.copy(pincode = "4000")))
    }

    @Test
    fun `isStep1Valid - fails when city is blank`() {
        assertFalse(OnboardingValidator.isStep1Valid(validStep1State.copy(city = "")))
    }

    @Test
    fun `isStep1Valid - fails when locationId is blank`() {
        assertFalse(OnboardingValidator.isStep1Valid(validStep1State.copy(locationId = "")))
    }

    @Test
    fun `isStep1Valid - fails when gpsLocation is null`() {
        assertFalse(OnboardingValidator.isStep1Valid(validStep1State.copy(gpsLocation = null)))
    }

    // ── Step 2: Classification (always valid – default slab auto-selected) ──

    @Test
    fun `isStep2Valid - always valid`() {
        val state = OnboardingState()
        assertTrue(OnboardingValidator.isStep2Valid(state))
    }

    // ── Step 3: Distributor & Bank ──

    @Test
    fun `isStep3Valid - valid with all required fields`() {
        val state = OnboardingState(
            distributorName = "Test Dist",
            selectedDistributorId = "DIS-001",
            accountHolderName = "Test",
            bankName = "SBI",
            accountNumber = "123456789",
            ifscCode = "SBIN0001234"
        )
        assertTrue(OnboardingValidator.isStep3Valid(state))
    }

    @Test
    fun `isStep3Valid - fails when IFSC is not 11 chars`() {
        val state = OnboardingState(
            distributorName = "Test Dist",
            selectedDistributorId = "DIS-001",
            accountHolderName = "Test",
            bankName = "SBI",
            accountNumber = "123456789",
            ifscCode = "SBIN00"
        )
        assertFalse(OnboardingValidator.isStep3Valid(state))
    }

    // ── Step 4: KYC ──

    @Test
    fun `isStep4Valid - all required fields and photos`() {
        val state = OnboardingState(
            kycIdType = "AADHAAR",
            kycIdNumber = "1234-5678-9012",
            kycLocationType = "ELECTRICITY_BILL",
            kycIdProofPath = "/path/id.jpg",
            kycLocationProofPath = "/path/loc.jpg"
        )
        assertTrue(OnboardingValidator.isStep4Valid(state))
    }

    @Test
    fun `isStep4Valid - fails without idProofPath`() {
        val state = OnboardingState(
            kycIdType = "AADHAAR",
            kycIdNumber = "1234-5678-9012",
            kycLocationType = "ELECTRICITY_BILL",
            kycIdProofPath = null,
            kycLocationProofPath = "/path/loc.jpg"
        )
        assertFalse(OnboardingValidator.isStep4Valid(state))
    }

    @Test
    fun `isStep4Valid - fails without kycIdType`() {
        val state = OnboardingState(
            kycIdType = "",
            kycIdNumber = "1234-5678-9012",
            kycLocationType = "ELECTRICITY_BILL",
            kycIdProofPath = "/path/id.jpg",
            kycLocationProofPath = "/path/loc.jpg"
        )
        assertFalse(OnboardingValidator.isStep4Valid(state))
    }

    // ── Step 5: Photos ──

    @Test
    fun `isStep5Valid - valid with at least one photo`() {
        val state = OnboardingState(
            photoSlots = listOf(
                com.siteflow.signature.cso.onboarding.data.PhotoSlot("1", "Test", true, "/path.jpg")
            )
        )
        assertTrue(OnboardingValidator.isStep5Valid(state))
    }

    @Test
    fun `isStep5Valid - fails with no photos`() {
        val state = OnboardingState(
            photoSlots = listOf(
                com.siteflow.signature.cso.onboarding.data.PhotoSlot("1", "Test", true, null)
            )
        )
        assertFalse(OnboardingValidator.isStep5Valid(state))
    }

    // ── Step 6: Agreement ──

    @Test
    fun `isStep6Valid - accepted`() {
        assertTrue(OnboardingValidator.isStep6Valid(OnboardingState(agreementAccepted = true)))
    }

    @Test
    fun `isStep6Valid - not accepted`() {
        assertFalse(OnboardingValidator.isStep6Valid(OnboardingState(agreementAccepted = false)))
    }
}

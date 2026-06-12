package com.siteflow.signature.cso.dashboard.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OutletStatusTest {

    // ── fromBackend mapping ──

    @Test
    fun `fromBackend - DRAFT_BASIC maps correctly`() {
        assertEquals(OutletStatus.DRAFT_BASIC, OutletStatus.fromBackend("DRAFT_BASIC"))
    }

    @Test
    fun `fromBackend - DRAFT_BUSINESS_DETAILS maps correctly`() {
        assertEquals(OutletStatus.DRAFT_BUSINESS_DETAILS, OutletStatus.fromBackend("DRAFT_BUSINESS_DETAILS"))
    }

    @Test
    fun `fromBackend - DRAFT_KYC maps correctly`() {
        assertEquals(OutletStatus.DRAFT_KYC, OutletStatus.fromBackend("DRAFT_KYC"))
    }

    @Test
    fun `fromBackend - DRAFT_PHOTOS maps correctly`() {
        assertEquals(OutletStatus.DRAFT_PHOTOS, OutletStatus.fromBackend("DRAFT_PHOTOS"))
    }

    @Test
    fun `fromBackend - AGREEMENT_PENDING maps correctly`() {
        assertEquals(OutletStatus.AGREEMENT_PENDING, OutletStatus.fromBackend("AGREEMENT_PENDING"))
    }

    @Test
    fun `fromBackend - ASM_PENDING maps correctly`() {
        assertEquals(OutletStatus.ASM_PENDING, OutletStatus.fromBackend("ASM_PENDING"))
    }

    @Test
    fun `fromBackend - PENDING_APPROVAL maps to ASM_PENDING`() {
        assertEquals(OutletStatus.ASM_PENDING, OutletStatus.fromBackend("PENDING_APPROVAL"))
    }

    @Test
    fun `fromBackend - ASM_APPROVED maps correctly`() {
        assertEquals(OutletStatus.ASM_APPROVED, OutletStatus.fromBackend("ASM_APPROVED"))
    }

    @Test
    fun `fromBackend - SUBMITTED maps to ASM_APPROVED`() {
        assertEquals(OutletStatus.ASM_APPROVED, OutletStatus.fromBackend("SUBMITTED"))
    }

    @Test
    fun `fromBackend - ACTIVE maps to ASM_APPROVED`() {
        assertEquals(OutletStatus.ASM_APPROVED, OutletStatus.fromBackend("ACTIVE"))
    }

    @Test
    fun `fromBackend - ASM_REJECTED maps correctly`() {
        assertEquals(OutletStatus.ASM_REJECTED, OutletStatus.fromBackend("ASM_REJECTED"))
    }

    @Test
    fun `fromBackend - REJECTED maps to ASM_REJECTED`() {
        assertEquals(OutletStatus.ASM_REJECTED, OutletStatus.fromBackend("REJECTED"))
    }

    @Test
    fun `fromBackend - ONBOARDED maps correctly`() {
        assertEquals(OutletStatus.ONBOARDED, OutletStatus.fromBackend("ONBOARDED"))
    }

    @Test
    fun `fromBackend - VERIFIED maps to ONBOARDED`() {
        assertEquals(OutletStatus.ONBOARDED, OutletStatus.fromBackend("VERIFIED"))
    }

    @Test
    fun `fromBackend - unknown value defaults to DRAFT_BASIC`() {
        assertEquals(OutletStatus.DRAFT_BASIC, OutletStatus.fromBackend("UNKNOWN_VALUE"))
    }

    @Test
    fun `fromBackend - null defaults to DRAFT_BASIC`() {
        assertEquals(OutletStatus.DRAFT_BASIC, OutletStatus.fromBackend(null))
    }

    // ── Labels ──

    @Test
    fun `OutletStatus labels are user-friendly`() {
        assertEquals("Draft Basic", OutletStatus.DRAFT_BASIC.label)
        assertEquals("Draft Business", OutletStatus.DRAFT_BUSINESS_DETAILS.label)
        assertEquals("KYC Pending", OutletStatus.DRAFT_KYC.label)
        assertEquals("Photos Pending", OutletStatus.DRAFT_PHOTOS.label)
        assertEquals("Agreement Pending", OutletStatus.AGREEMENT_PENDING.label)
        assertEquals("Pending Review", OutletStatus.ASM_PENDING.label)
        assertEquals("ASM Approved", OutletStatus.ASM_APPROVED.label)
        assertEquals("ASM Rejected", OutletStatus.ASM_REJECTED.label)
        assertEquals("Onboarded", OutletStatus.ONBOARDED.label)
    }
}

class OutletItemTest {

    private fun makeItem(status: OutletStatus) = OutletItem(
        name = "Test",
        initials = "T",
        slab = OutletSlab.SILVER,
        location = "Test",
        status = status,
        updatedTime = "now"
    )

    // ── isContinuingOnboarding ──

    @Test
    fun `isContinuingOnboarding - true for DRAFT_BASIC`() {
        assertTrue(makeItem(OutletStatus.DRAFT_BASIC).isContinuingOnboarding)
    }

    @Test
    fun `isContinuingOnboarding - true for DRAFT_BUSINESS_DETAILS`() {
        assertTrue(makeItem(OutletStatus.DRAFT_BUSINESS_DETAILS).isContinuingOnboarding)
    }

    @Test
    fun `isContinuingOnboarding - true for DRAFT_KYC`() {
        assertTrue(makeItem(OutletStatus.DRAFT_KYC).isContinuingOnboarding)
    }

    @Test
    fun `isContinuingOnboarding - true for DRAFT_PHOTOS`() {
        assertTrue(makeItem(OutletStatus.DRAFT_PHOTOS).isContinuingOnboarding)
    }

    @Test
    fun `isContinuingOnboarding - true for AGREEMENT_PENDING`() {
        assertTrue(makeItem(OutletStatus.AGREEMENT_PENDING).isContinuingOnboarding)
    }

    @Test
    fun `isContinuingOnboarding - false for ASM_PENDING`() {
        assertFalse(makeItem(OutletStatus.ASM_PENDING).isContinuingOnboarding)
    }

    @Test
    fun `isContinuingOnboarding - false for ONBOARDED`() {
        assertFalse(makeItem(OutletStatus.ONBOARDED).isContinuingOnboarding)
    }

    @Test
    fun `isContinuingOnboarding - false for ASM_REJECTED`() {
        assertFalse(makeItem(OutletStatus.ASM_REJECTED).isContinuingOnboarding)
    }

    // ── onboardingStep ──

    @Test
    fun `onboardingStep - DRAFT_BASIC resumes at step 2`() {
        assertEquals(2, makeItem(OutletStatus.DRAFT_BASIC).onboardingStep)
    }

    @Test
    fun `onboardingStep - DRAFT_BUSINESS_DETAILS resumes at step 4 KYC`() {
        assertEquals(4, makeItem(OutletStatus.DRAFT_BUSINESS_DETAILS).onboardingStep)
    }

    @Test
    fun `onboardingStep - DRAFT_KYC resumes at step 5 Photos`() {
        assertEquals(5, makeItem(OutletStatus.DRAFT_KYC).onboardingStep)
    }

    @Test
    fun `onboardingStep - DRAFT_PHOTOS resumes at step 6 Agreement`() {
        assertEquals(6, makeItem(OutletStatus.DRAFT_PHOTOS).onboardingStep)
    }

    @Test
    fun `onboardingStep - AGREEMENT_PENDING resumes at step 6`() {
        assertEquals(6, makeItem(OutletStatus.AGREEMENT_PENDING).onboardingStep)
    }

    // ── fromDto ──

    @Test
    fun `fromDto creates correct OutletItem`() {
        val dto = com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData(
            id = "test-id",
            name = "Sharma General Point",
            phone = "9123456789",
            ownerName = "Rajesh Sharma",
            ownerMobile = "9123456789",
            ownerWhatsapp = "9123456789",
            email = "test@test.com",
            outletType = "GROCERY",
            address = "Shop No. 5",
            landmark = "Near Station",
            locality = "Vashi",
            city = "Mumbai",
            state = "Maharashtra",
            pincode = "400001",
            latitude = 19.0771,
            longitude = 72.9986,
            outletStatus = "DRAFT_BASIC",
            businessId = "biz-id",
            createdByAseName = "Rakesh Verma",
            createdAt = "2026-03-03T12:43:13.376922Z"
        )

        val item = OutletItem.fromDto(dto)

        assertEquals("test-id", item.id)
        assertEquals("Sharma General Point", item.name)
        assertEquals("SG", item.initials)
        assertEquals("Vashi, Mumbai", item.location)
        assertEquals(OutletStatus.DRAFT_BASIC, item.status)
        assertEquals("Rajesh Sharma", item.ownerName)
        assertEquals("9123456789", item.contactNumber)
        assertEquals("GROCERY", item.outletType)
        assertEquals("400001", item.pincode)
        assertEquals("Rakesh Verma", item.onboardedByAse)
        assertTrue(item.isContinuingOnboarding)
    }

    @Test
    fun `fromDto handles null locality by falling back to address`() {
        val dto = com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData(
            id = "test-id",
            name = "Test Store",
            phone = "9999999999",
            ownerName = "Test",
            ownerMobile = "9999999999",
            outletType = "GROCERY",
            address = "Test Address",
            locality = null,
            city = null,
            outletStatus = "DRAFT_BASIC"
        )

        val item = OutletItem.fromDto(dto)
        assertEquals("Test Address", item.location)
    }

    @Test
    fun `fromDto generates initials from single-word name`() {
        val dto = com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData(
            id = "test-id",
            name = "Sharma",
            phone = "9999999999",
            ownerName = "Test",
            ownerMobile = "9999999999",
            outletType = "GROCERY",
            address = "Test",
            outletStatus = "DRAFT_BASIC"
        )

        val item = OutletItem.fromDto(dto)
        assertEquals("S", item.initials)
    }

    @Test
    fun `fromDto with DRAFT_KYC status maps correctly`() {
        val dto = com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData(
            id = "test-id",
            name = "Test Store",
            phone = "9999999999",
            ownerName = "Test",
            ownerMobile = "9999999999",
            outletType = "GROCERY",
            address = "Test",
            outletStatus = "DRAFT_KYC"
        )

        val item = OutletItem.fromDto(dto)
        assertEquals(OutletStatus.DRAFT_KYC, item.status)
        assertTrue(item.isContinuingOnboarding)
        assertEquals(5, item.onboardingStep)
    }

    @Test
    fun `fromDto with ONBOARDED status maps correctly`() {
        val dto = com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData(
            id = "test-id",
            name = "Test Store",
            phone = "9999999999",
            ownerName = "Test",
            ownerMobile = "9999999999",
            outletType = "GROCERY",
            address = "Test",
            outletStatus = "ONBOARDED"
        )

        val item = OutletItem.fromDto(dto)
        assertEquals(OutletStatus.ONBOARDED, item.status)
        assertFalse(item.isContinuingOnboarding)
    }

    @Test
    fun `fromDto maps assetStatus correctly`() {
        val dto = com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData(
            id = "test-id",
            name = "Test Store",
            phone = "9999999999",
            ownerName = "Test",
            ownerMobile = "9999999999",
            outletType = "GROCERY",
            address = "Test",
            outletStatus = "ASM_APPROVED",
            assetStatus = "REQUESTED"
        )

        val item = OutletItem.fromDto(dto)
        assertEquals(AssetStatus.REQUESTED, item.assetStatus)
    }

    @Test
    fun `fromDto maps complianceId and complianceState`() {
        val dto = com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData(
            id = "test-id",
            name = "Test Store",
            phone = "9999999999",
            ownerName = "Test",
            ownerMobile = "9999999999",
            outletType = "GROCERY",
            address = "Test",
            outletStatus = "ASM_APPROVED",
            complianceId = "comp-123",
            complianceState = "SUBMITTED"
        )

        val item = OutletItem.fromDto(dto)
        assertEquals("comp-123", item.complianceId)
        assertEquals(ComplianceState.SUBMITTED, item.complianceState)
    }

    @Test
    fun `fromDto defaults complianceId to empty when null`() {
        val dto = com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData(
            id = "test-id",
            name = "Test Store",
            phone = "9999999999",
            ownerName = "Test",
            ownerMobile = "9999999999",
            outletType = "GROCERY",
            address = "Test",
            outletStatus = "DRAFT_BASIC"
        )

        val item = OutletItem.fromDto(dto)
        assertEquals("", item.complianceId)
        assertEquals(ComplianceState.NONE, item.complianceState)
    }
}

class AssetStatusTest {

    @Test
    fun `fromBackend - REQUESTED maps correctly`() {
        assertEquals(AssetStatus.REQUESTED, AssetStatus.fromBackend("REQUESTED"))
    }

    @Test
    fun `fromBackend - VERIFIED maps correctly`() {
        assertEquals(AssetStatus.VERIFIED, AssetStatus.fromBackend("VERIFIED"))
    }

    @Test
    fun `fromBackend - null defaults to NOT_REQUESTED`() {
        assertEquals(AssetStatus.NOT_REQUESTED, AssetStatus.fromBackend(null))
    }

    @Test
    fun `fromBackend - unknown value defaults to NOT_REQUESTED`() {
        assertEquals(AssetStatus.NOT_REQUESTED, AssetStatus.fromBackend("UNKNOWN"))
    }
}

class ComplianceStateTest {

    @Test
    fun `fromBackend - SUBMITTED maps correctly`() {
        assertEquals(ComplianceState.SUBMITTED, ComplianceState.fromBackend("SUBMITTED"))
    }

    @Test
    fun `fromBackend - VERIFIED maps correctly`() {
        assertEquals(ComplianceState.VERIFIED, ComplianceState.fromBackend("VERIFIED"))
    }

    @Test
    fun `fromBackend - null defaults to NONE`() {
        assertEquals(ComplianceState.NONE, ComplianceState.fromBackend(null))
    }

    @Test
    fun `fromBackend - unknown value defaults to NONE`() {
        assertEquals(ComplianceState.NONE, ComplianceState.fromBackend("SOMETHING_ELSE"))
    }

    @Test
    fun `labels are correct`() {
        assertEquals("None", ComplianceState.NONE.label)
        assertEquals("Submitted", ComplianceState.SUBMITTED.label)
        assertEquals("Verified", ComplianceState.VERIFIED.label)
    }
}

package com.siteflow.signature.cso.dashboard.data

import androidx.compose.ui.graphics.Color
import com.siteflow.signature.cso.onboarding.data.GpsLocation
import com.siteflow.signature.cso.onboarding.data.PhotoSlot
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.util.toTitleCase

/**
 * The 5 steps of Signature (Complete Definition of an Outlet).
 *
 * 1. Enrollment      — Outlet Onboarding + PFP Enrollment (ASE in field)
 * 2. ASM Approval    — Manager approves the enrollment (ASM)
 * 3. Asset Request   — ASE requests cooler/branding asset for the outlet
 * 4. DMS Linking     — ASE gets DMS number from business, links to outlet
 * 5. Signature Verify      — Branding + Cooler installed & verified with photos (ASE)
 */
enum class SignatureStep(val label: String) {
    ENROLLMENT("Enrollment"),
    ASM_APPROVAL("ASM Approval"),
    ASSET_REQUEST("Asset Request"),
    SIGNATURE_VERIFICATION("Signature Verification")
}

/**
 * Classification slab for an outlet based on case volume.
 */
enum class OutletSlab(
    val label: String,
    val emoji: String,
    val color: Color,
    val bgColor: Color
) {
    PLATINUM("Platinum", "💎", Color(0xFF7C3AED), Color(0xFFF3E8FF)),
    DIAMOND("Diamond",  "🔷", Color(0xFF2563EB), Color(0xFFDBEAFE)),
    GOLD("Gold",        "👑", Color(0xFFF59E0B), Color(0xFFFEF3C7)),
    SILVER("Silver",    "🥈", Color(0xFF6B7280), Color(0xFFF3F4F6))
}

/**
 * Status of an outlet in the onboarding pipeline.
 * Matches backend outletStatus values.
 */
enum class OutletStatus(
    val label: String,
    val color: Color,
    val bgColor: Color,
    val barColor: Color
) {
    DRAFT_BASIC("Classification Next", Color(0xFF9CA3AF), Color(0xFFF3F4F6), Color(0xFF9CA3AF)),
    DRAFT_BUSINESS_DETAILS("KYC Next", Color(0xFF9CA3AF), Color(0xFFF3F4F6), Color(0xFF9CA3AF)),
    DRAFT_KYC("Photos Next", Color(0xFFF59E0B), Color(0xFFFEF3C7), Color(0xFFF59E0B)),
    DRAFT_PHOTOS("Agreement Next", Color(0xFFF59E0B), Color(0xFFFEF3C7), Color(0xFFF59E0B)),
    AGREEMENT_PENDING("Awaiting Approval", Color(0xFFF59E0B), Color(0xFFFEF3C7), Color(0xFFF59E0B)),
    ASE_PENDING("Pending L1 Review", Color(0xFFF59E0B), Color(0xFFFEF3C7), Color(0xFFF59E0B)),
    ASE_APPROVED("L1 Approved", AppColors.BlueGradientStart, Color(0xFFDBEAFE), AppColors.BlueGradientStart),
    ASE_REJECTED("L1 Rejected", AppColors.Danger, Color(0xFFFEE2E2), AppColors.Danger),
    ASM_PENDING("Pending Review", Color(0xFFF59E0B), Color(0xFFFEF3C7), Color(0xFFF59E0B)),
    ASM_APPROVED("ASM Approved", AppColors.BlueGradientStart, Color(0xFFDBEAFE), AppColors.BlueGradientStart),
    ASM_REJECTED("ASM Rejected", AppColors.Danger, Color(0xFFFEE2E2), AppColors.Danger),
    ONBOARDED("Onboarded", AppColors.Success, Color(0xFFD1FAE5), AppColors.Success);

    companion object {
        fun fromBackend(value: String?): OutletStatus = when (value) {
            "DRAFT_BASIC" -> DRAFT_BASIC
            "DRAFT_BUSINESS_DETAILS" -> DRAFT_BUSINESS_DETAILS
            "DRAFT_KYC" -> DRAFT_KYC
            "DRAFT_PHOTOS" -> DRAFT_PHOTOS
            "AGREEMENT_PENDING" -> AGREEMENT_PENDING
            "ASE_PENDING" -> ASE_PENDING
            "ASE_APPROVED" -> ASE_APPROVED
            "ASE_REJECTED" -> ASE_REJECTED
            "ASM_PENDING", "PENDING_APPROVAL" -> ASM_PENDING
            "ASM_APPROVED", "SUBMITTED", "ACTIVE" -> ASM_APPROVED
            "ASM_REJECTED", "REJECTED" -> ASM_REJECTED
            "ONBOARDED", "VERIFIED" -> ONBOARDED
            else -> DRAFT_BASIC
        }
    }
}

/**
 * Tracks asset lifecycle (cooler/branding) for an outlet.
 */
enum class AssetStatus(val label: String) {
    NOT_REQUESTED("Not Requested"),
    REQUESTED("Requested"),
    VERIFICATION_PENDING("Verification Pending"),
    VERIFIED("Verified");

    companion object {
        fun fromBackend(value: String?): AssetStatus = when (value) {
            "REQUESTED" -> REQUESTED
            "VERIFICATION_PENDING" -> VERIFICATION_PENDING
            "VERIFIED" -> VERIFIED
            else -> NOT_REQUESTED
        }
    }
}

/**
 * Tracks compliance submission lifecycle.
 */
enum class ComplianceState(val label: String) {
    NONE("None"),
    SUBMITTED("Submitted"),
    VERIFIED("Verified");

    companion object {
        fun fromBackend(value: String?): ComplianceState = when (value) {
            "SUBMITTED" -> SUBMITTED
            "VERIFIED" -> VERIFIED
            else -> NONE
        }
    }
}

/**
 * A single entry in the outlet's status timeline.
 */
data class TimelineEntry(
    val title: String,
    val timestamp: String
)

/**
 * Parsed compliance record for display.
 */
data class ComplianceRecord(
    val id: String,
    val coolerInstalled: Boolean,
    val serialNo: String,
    val coolerType: String,
    val capacity: String,
    val signageInstalled: Boolean,
    val verified: Boolean,
    val uploadedAt: String,
    val uploadedByName: String,
    val verifiedAt: String?,
    val verifiedByName: String?,
    val images: List<ComplianceImage>
)

data class ComplianceImage(
    val id: String,
    val imageUrl: String,
    val imageType: String
)

/**
 * Outlet photo from the API (onboarding photos).
 */
data class OutletPhoto(
    val id: String,
    val photoUrl: String,
    val photoType: String,
    val uploadedByName: String
)

/**
 * Outlet data for the ASE dashboard and detail screen.
 */
data class OutletItem(
    // ── Identity ──
    val id: String = "",

    // ── Dashboard fields ──
    val name: String,
    val initials: String,
    val slab: OutletSlab,
    val location: String,
    val status: OutletStatus,
    val updatedTime: String,
    val createdAtRaw: String = "",  // Original creation time
    val updatedAtRaw: String = "",  // Latest activity time for sorting
    val assetStatus: AssetStatus = AssetStatus.NOT_REQUESTED,
    val complianceId: String = "",
    val complianceState: ComplianceState = ComplianceState.NONE,
    val complianceRecords: List<ComplianceRecord> = emptyList(),
    val completedSteps: Set<SignatureStep> = emptySet(),

    // ── Basic Details (Step 1) ──
    val ownerName: String = "",
    val contactNumber: String = "",
    val whatsAppNumber: String = "",
    val outletType: String = "",
    val address: String = "",
    val pincode: String = "",
    val city: String = "",
    val gpsLocation: GpsLocation? = null,

    // ── Classification & Volume (Step 2) ──
    val estimatedVolume: String = "",

    // ── Photos (Step 3 & 4) ──
    val photoSlots: List<PhotoSlot> = emptyList(),
    val signatureVerificationPhotos: List<PhotoSlot> = emptyList(),
    val onboardingPhotos: List<OutletPhoto> = emptyList(),

    // ── Distributor Details (Step 4) ──
    val distributorName: String = "",
    val dmsId: String = "",
    val distributorContact: String = "",

    // ── Bank Details (Step 4) ──
    val accountHolderName: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val upiId: String = "",

    // ── Agreement (Step 5) ──
    val agreementAccepted: Boolean = false,

    // ── Timeline ──
    val timeline: List<TimelineEntry> = emptyList(),

    // ── ASE Attribution (for ASM view) ──
    val onboardedByAse: String = ""
) {
    val completionPercent: Int
        get() = ((completedSteps.size.toFloat() / SignatureStep.entries.size) * 100).toInt()

    val nextPendingStep: SignatureStep?
        get() = SignatureStep.entries.firstOrNull { it !in completedSteps }

    val capturedPhotoCount: Int
        get() = photoSlots.count { it.imagePath != null }

    val isContinuingOnboarding: Boolean
        get() = status in listOf(
            OutletStatus.DRAFT_BASIC,
            OutletStatus.DRAFT_BUSINESS_DETAILS,
            OutletStatus.DRAFT_KYC,
            OutletStatus.DRAFT_PHOTOS,
            OutletStatus.AGREEMENT_PENDING
        )

    val onboardingStep: Int
        get() = when (status) {
            OutletStatus.DRAFT_BASIC -> 2
            OutletStatus.DRAFT_BUSINESS_DETAILS -> 4
            OutletStatus.DRAFT_KYC -> 5
            OutletStatus.DRAFT_PHOTOS, OutletStatus.AGREEMENT_PENDING -> 6
            else -> 1
        }

    companion object {
        /**
         * Factory: create from API DTO
         */
        fun fromDto(dto: com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData): OutletItem {
            val initials = dto.name.orEmpty().split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")

            val location = listOfNotNull(dto.locality, dto.city)
                .filter { it.isNotBlank() }
                .joinToString(", ")
                .ifBlank { dto.address ?: "" }

            val sortTime = dto.updatedAt ?: dto.createdAt ?: ""
            val timeAgo = if (sortTime.isNotBlank()) formatTimeAgo(sortTime) else ""
            val status = OutletStatus.fromBackend(dto.outletStatus)

            return OutletItem(
                id = dto.id,
                name = dto.name.orEmpty().toTitleCase(),
                initials = initials.ifBlank { "?" },
                slab = OutletSlab.SILVER,
                location = location,
                status = status,
                updatedTime = timeAgo,
                completedSteps = completedStepsFromStatus(status, AssetStatus.fromBackend(dto.assetStatus)),
                ownerName = dto.ownerName ?: "",
                contactNumber = dto.phone ?: dto.ownerMobile ?: "",
                whatsAppNumber = dto.ownerWhatsapp ?: "",
                outletType = dto.outletType ?: "",
                address = dto.address ?: "",
                pincode = dto.pincode ?: "",
                city = dto.city ?: "",
                gpsLocation = if (dto.latitude != null && dto.longitude != null)
                    GpsLocation(dto.latitude, dto.longitude, "") else null,
                onboardedByAse = dto.createdByCsoName ?: dto.createdByAseName ?: "",
                createdAtRaw = dto.createdAt ?: "",
                updatedAtRaw = dto.updatedAt ?: dto.createdAt ?: "",
                assetStatus = AssetStatus.fromBackend(dto.assetStatus),
                complianceId = dto.complianceId ?: "",
                complianceState = ComplianceState.fromBackend(dto.complianceState),
                dmsId = dto.dmsId ?: "",
                onboardingPhotos = dto.photos.mapNotNull { photo ->
                    val url = photo.photoUrl ?: return@mapNotNull null
                    OutletPhoto(
                        id = photo.id ?: "",
                        photoUrl = url,
                        photoType = photo.photoType ?: "UNKNOWN",
                        uploadedByName = photo.uploadedByName ?: ""
                    )
                },
                complianceRecords = dto.complianceRecords.map { rec ->
                    ComplianceRecord(
                        id = rec.id,
                        coolerInstalled = rec.coolerInstalled,
                        serialNo = rec.serialNo ?: "",
                        coolerType = rec.coolerType ?: "",
                        capacity = rec.capacity ?: "",
                        signageInstalled = rec.signageInstalled,
                        verified = rec.verified,
                        uploadedAt = rec.uploadedAt ?: "",
                        uploadedByName = rec.uploadedByName ?: "",
                        verifiedAt = rec.verifiedAt,
                        verifiedByName = rec.verifiedByName,
                        images = rec.images.map { img ->
                            ComplianceImage(
                                id = img.id ?: "",
                                imageUrl = img.imageUrl ?: "",
                                imageType = img.imageType ?: ""
                            )
                        }
                    )
                }.also { records ->
                    // Logic to set complianceId if missing in root but present in records
                }
            ).let { item ->
                if (item.complianceId.isBlank() && item.complianceRecords.isNotEmpty()) {
                    item.copy(complianceId = item.complianceRecords.first().id)
                } else {
                    item
                }
            }
        }

        /**
         * Derives completed Signature steps from the outlet's status and asset status.
         * Once ASM_PENDING, enrollment is complete.
         * ASSET_REQUEST is complete when assetStatus != NOT_REQUESTED.
         */
        private fun completedStepsFromStatus(
            status: OutletStatus,
            assetStatus: AssetStatus = AssetStatus.NOT_REQUESTED
        ): Set<SignatureStep> {
            val steps = mutableSetOf<SignatureStep>()
            when (status) {
                OutletStatus.DRAFT_BASIC,
                OutletStatus.DRAFT_BUSINESS_DETAILS,
                OutletStatus.DRAFT_KYC,
                OutletStatus.DRAFT_PHOTOS,
                OutletStatus.AGREEMENT_PENDING -> { /* no steps */ }
                // CSO has finished enrollment; awaiting/cleared L1 (ASE) review.
                OutletStatus.ASE_PENDING,
                OutletStatus.ASE_APPROVED,
                OutletStatus.ASE_REJECTED -> steps.add(SignatureStep.ENROLLMENT)
                OutletStatus.ASM_PENDING -> steps.add(SignatureStep.ENROLLMENT)
                OutletStatus.ASM_REJECTED -> steps.add(SignatureStep.ENROLLMENT)
                OutletStatus.ASM_APPROVED -> {
                    steps.add(SignatureStep.ENROLLMENT)
                    steps.add(SignatureStep.ASM_APPROVAL)
                }
                OutletStatus.ONBOARDED -> return SignatureStep.entries.toSet()
            }
            // Asset request step
            if (assetStatus != AssetStatus.NOT_REQUESTED) {
                steps.add(SignatureStep.ASSET_REQUEST)
            }
            // Signature Verification step — complete only when ASM has verified (VERIFIED status)
            if (assetStatus == AssetStatus.VERIFIED) {
                steps.add(SignatureStep.SIGNATURE_VERIFICATION)
            }
            return steps
        }

        private fun formatTimeAgo(isoTimestamp: String): String {
            // Simple display — just show the date portion
            return isoTimestamp.substringBefore("T")
        }
    }
}

val mockOutlets = listOf(
    OutletItem(
        name = "Krishna General Store",
        initials = "KG",
        slab = OutletSlab.GOLD,
        location = "Whitefield, Bangalore",
        status = OutletStatus.ASM_PENDING,
        updatedTime = "10 mins ago",
        completedSteps = setOf(SignatureStep.ENROLLMENT),
        ownerName = "Ramesh Krishna",
        contactNumber = "9876543210",
        whatsAppNumber = "9876543210",
        outletType = "Kirana/General Store",
        address = "Shop No. 12, MG Road, Whitefield",
        pincode = "560066",
        city = "Bangalore",
        gpsLocation = GpsLocation(12.9716, 77.5946, "Whitefield"),
        estimatedVolume = "120",
        photoSlots = listOf(
            PhotoSlot("shop_front", "Shop Front", true, "/mock/shop_front.jpg"),
            PhotoSlot("inside_shop", "Inside Shop", false, "/mock/inside.jpg"),
            PhotoSlot("cooler_space", "Cooler Space", false),
            PhotoSlot("shelf_visibility", "Shelf Visibility", false),
            PhotoSlot("branding", "Branding Opportunity", false)
        ),
        distributorName = "Karnataka Beverages Pvt Ltd",
        dmsId = "KRN-2024-001",
        distributorContact = "9988776655",
        accountHolderName = "Ramesh Krishna",
        bankName = "State Bank of India",
        accountNumber = "XXXXXXXXXX3456",
        ifscCode = "SBIN0001234",
        upiId = "ramesh@sbi",
        agreementAccepted = true,
        timeline = listOf(
            TimelineEntry("Pending Review", "Today, 10:30 AM"),
            TimelineEntry("Enrollment Submitted", "Today, 09:15 AM"),
            TimelineEntry("Enrollment Started", "Yesterday, 04:00 PM")
        ),
        onboardedByAse = "Rahul Sharma"
    ),
    OutletItem(
        name = "Pooja Provisions",
        initials = "PP",
        slab = OutletSlab.SILVER,
        location = "Koramangala, Bangalore",
        status = OutletStatus.ASM_APPROVED,
        updatedTime = "Yesterday",
        completedSteps = setOf(SignatureStep.ENROLLMENT, SignatureStep.ASM_APPROVAL, SignatureStep.ASSET_REQUEST),
        ownerName = "Pooja Sharma",
        contactNumber = "9123456789",
        whatsAppNumber = "9123456789",
        outletType = "Kirana/General Store",
        address = "1st Cross, 4th Block, Koramangala",
        pincode = "560034",
        city = "Bangalore",
        gpsLocation = GpsLocation(12.9352, 77.6245, "Koramangala"),
        estimatedVolume = "75",
        photoSlots = listOf(
            PhotoSlot("shop_front", "Shop Front", true, "/mock/shop_front.jpg"),
            PhotoSlot("inside_shop", "Inside Shop", false, "/mock/inside.jpg"),
            PhotoSlot("cooler_space", "Cooler Space", false, "/mock/cooler.jpg"),
            PhotoSlot("shelf_visibility", "Shelf Visibility", false),
            PhotoSlot("branding", "Branding Opportunity", false)
        ),
        distributorName = "South India Distributors",
        dmsId = "SID-2024-045",
        distributorContact = "9876512340",
        accountHolderName = "Pooja Sharma",
        bankName = "HDFC Bank",
        accountNumber = "XXXXXXXXXX7890",
        ifscCode = "HDFC0001234",
        upiId = "pooja@hdfc",
        agreementAccepted = true,
        timeline = listOf(
            TimelineEntry("Submitted for Verification", "Today, 09:30 AM"),
            TimelineEntry("DMS Linked", "Yesterday, 02:15 PM"),
            TimelineEntry("ASM Approved", "Feb 20, 03:45 PM"),
            TimelineEntry("Enrollment Submitted", "Feb 19, 11:00 AM"),
            TimelineEntry("Enrollment Started", "Feb 19, 10:30 AM")
        ),
        onboardedByAse = "Rahul Sharma"
    ),
    OutletItem(
        name = "Metro Supermarket",
        initials = "MS",
        slab = OutletSlab.PLATINUM,
        location = "MG Road, Bangalore",
        status = OutletStatus.ONBOARDED,
        updatedTime = "2 days ago",
        completedSteps = SignatureStep.entries.toSet(),
        ownerName = "Vikram Mehta",
        contactNumber = "9871234567",
        whatsAppNumber = "9871234567",
        outletType = "Kirana/General Store",
        address = "No. 5, Brigade Road, MG Road",
        pincode = "560001",
        city = "Bangalore",
        gpsLocation = GpsLocation(12.9758, 77.6068, "MG Road"),
        estimatedVolume = "250",
        photoSlots = listOf(
            PhotoSlot("shop_front", "Shop Front", true, "/mock/shop_front.jpg"),
            PhotoSlot("inside_shop", "Inside Shop", false, "/mock/inside.jpg"),
            PhotoSlot("cooler_space", "Cooler Space", false, "/mock/cooler.jpg"),
            PhotoSlot("shelf_visibility", "Shelf Visibility", false, "/mock/shelf.jpg"),
            PhotoSlot("branding", "Branding Opportunity", false, "/mock/branding.jpg")
        ),
        signatureVerificationPhotos = listOf(
            PhotoSlot("signature_front", "Signature Front", true, "/mock/signature_front.jpg"),
            PhotoSlot("signature_inside1", "Inside View 1", false, "/mock/signature_in1.jpg"),
            PhotoSlot("signature_inside2", "Inside View 2", false, "/mock/signature_in2.jpg"),
            PhotoSlot("signature_cooler", "Cooler Installed", false, "/mock/signature_cool.jpg"),
            PhotoSlot("signature_branding", "Branding Completed", false, "/mock/signature_brand.jpg")
        ),
        distributorName = "Metro Beverages Ltd",
        dmsId = "MBL-2024-012",
        distributorContact = "9900112233",
        accountHolderName = "Vikram Mehta",
        bankName = "ICICI Bank",
        accountNumber = "XXXXXXXXXX5678",
        ifscCode = "ICIC0001234",
        upiId = "vikram@icici",
        agreementAccepted = true,
        timeline = listOf(
            TimelineEntry("Signature Verified", "Feb 22, 10:00 AM"),
            TimelineEntry("DMS Linked", "Feb 21, 01:30 PM"),
            TimelineEntry("ASM Approved", "Feb 20, 11:15 AM"),
            TimelineEntry("Enrollment Submitted", "Feb 19, 09:00 AM"),
            TimelineEntry("Enrollment Started", "Feb 18, 05:00 PM")
        ),
        onboardedByAse = "Amit Patel"
    ),
    OutletItem(
        name = "Sharma Traders",
        initials = "ST",
        slab = OutletSlab.DIAMOND,
        location = "Indiranagar, Bangalore",
        status = OutletStatus.ASM_REJECTED,
        updatedTime = "1 day ago",
        completedSteps = setOf(SignatureStep.ENROLLMENT, SignatureStep.ASM_APPROVAL),
        ownerName = "Anil Sharma",
        contactNumber = "9988001122",
        whatsAppNumber = "9988001122",
        outletType = "Electronics Store",
        address = "12th Main, Indiranagar",
        pincode = "560038",
        city = "Bangalore",
        gpsLocation = GpsLocation(12.9784, 77.6408, "Indiranagar"),
        estimatedVolume = "175",
        photoSlots = listOf(
            PhotoSlot("shop_front", "Shop Front", true, "/mock/shop_front.jpg"),
            PhotoSlot("inside_shop", "Inside Shop", false),
            PhotoSlot("cooler_space", "Cooler Space", false),
            PhotoSlot("shelf_visibility", "Shelf Visibility", false),
            PhotoSlot("branding", "Branding Opportunity", false)
        ),
        distributorName = "Eastern Distributors",
        dmsId = "EDT-2024-033",
        distributorContact = "9876001122",
        accountHolderName = "Anil Sharma",
        bankName = "Axis Bank",
        accountNumber = "XXXXXXXXXX9012",
        ifscCode = "UTIB0001234",
        agreementAccepted = true,
        timeline = listOf(
            TimelineEntry("Rejected — Incorrect documents", "Today, 11:00 AM"),
            TimelineEntry("ASM Approved", "Feb 22, 09:30 AM"),
            TimelineEntry("Enrollment Submitted", "Feb 21, 03:00 PM"),
            TimelineEntry("Enrollment Started", "Feb 21, 02:00 PM")
        ),
        onboardedByAse = "Priya Nair"
    ),
    OutletItem(
        name = "Kiran Mart",
        initials = "KM",
        slab = OutletSlab.GOLD,
        location = "Pune",
        status = OutletStatus.ASM_PENDING,
        updatedTime = "3 hours ago",
        completedSteps = setOf(SignatureStep.ENROLLMENT, SignatureStep.ASM_APPROVAL),
        ownerName = "Kiran Patil",
        contactNumber = "9765432100",
        outletType = "Kirana/General Store",
        address = "FC Road, Shivajinagar",
        pincode = "411005",
        city = "Pune",
        distributorName = "Shree Distributors",
        dmsId = "", // Intentionally blank for testing DMS linking
        distributorContact = "9876500000",
        timeline = listOf(
            TimelineEntry("ASM Approved", "Today, 10:00 AM"),
            TimelineEntry("Enrollment Started", "Today, 01:00 PM")
        ),
        onboardedByAse = "Rahul Sharma"
    ),
    OutletItem(
        name = "Rahul Enterprises",
        initials = "RE",
        slab = OutletSlab.DIAMOND,
        location = "Viman Nagar, Pune",
        status = OutletStatus.ASM_APPROVED,
        updatedTime = "1 hour ago",
        completedSteps = setOf(SignatureStep.ENROLLMENT, SignatureStep.ASM_APPROVAL, SignatureStep.ASSET_REQUEST),
        ownerName = "Rahul Desai",
        contactNumber = "9988776655",
        outletType = "Supermarket",
        address = "Viman Nagar Main Road",
        pincode = "411014",
        city = "Pune",
        distributorName = "Pune Central Agency",
        dmsId = "PCA-2024-998",
        signatureVerificationPhotos = emptyList(), // Intentionally empty to show "Upload Signature photos"
        timeline = listOf(
            TimelineEntry("DMS Linked", "Today, 09:00 AM"),
            TimelineEntry("ASM Approved", "Yesterday, 04:00 PM"),
            TimelineEntry("Enrollment Started", "Yesterday, 10:00 AM")
        ),
        onboardedByAse = "Amit Patel"
    )
)

package com.siteflow.signature.cso.onboarding.data

import com.siteflow.signature.cso.onboarding.data.dto.BusinessDetailsRequestDto
import com.siteflow.signature.cso.onboarding.data.dto.CreateOutletRequestDto
import com.siteflow.signature.cso.onboarding.data.dto.DistributorDto
import com.siteflow.signature.cso.onboarding.data.dto.LocationDto
import com.siteflow.signature.cso.onboarding.data.dto.OutletListPageDto
import com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData
import com.siteflow.signature.cso.onboarding.data.dto.SlabDto
import com.siteflow.signature.cso.onboarding.data.dto.KycDetailsRequestDto
import com.siteflow.signature.cso.onboarding.domain.OnboardingState
import com.siteflow.signature.core.data.networking.error.ApiError
import com.siteflow.signature.core.data.networking.result.NetworkResult
import com.siteflow.signature.core.data.networking.result.map
import com.siteflow.signature.core.domain.AuthRepository
import com.siteflow.signature.core.util.SignatureLog
import com.siteflow.signature.core.util.ImageEncoder
import com.siteflow.signature.outlet.walkthrough.data.OutletKycData

class OutletRepository(
    private val outletApi: OutletApi,
    private val locationApi: LocationApi,
    private val authRepository: AuthRepository
) {

    suspend fun getSlabs(): NetworkResult<List<SlabDto>, ApiError> {
        return outletApi.getSlabs().map { response ->
            if (response.success && response.data != null) {
                response.data.content
            } else {
                emptyList()
            }
        }
    }

    suspend fun getDistributors(): NetworkResult<List<DistributorDto>, ApiError> {
        return outletApi.getDistributors().map { response ->
            if (response.success && response.data != null) {
                response.data.content
            } else {
                emptyList()
            }
        }
    }

    suspend fun searchLocation(pincode: String): NetworkResult<List<LocationDto>, ApiError> {
        return locationApi.searchByPincode(pincode).map { response ->
            if (response.success) {
                response.data
            } else {
                emptyList()
            }
        }
    }

    suspend fun createOutlet(
        state: OnboardingState,
        locationId: String
    ): NetworkResult<OutletResponseData, ApiError> {
        val businessId = authRepository.getBusinessId()
            ?: return NetworkResult.Error(ApiError(-1, "Business ID not found"))

        val request = CreateOutletRequestDto(
            name = state.outletName,
            phone = state.contactNumber,
            ownerName = state.ownerName,
            ownerMobile = state.contactNumber,
            ownerWhatsapp = state.whatsAppNumber.ifBlank { state.contactNumber },
            email = state.email.ifBlank { null },
            outletType = mapOutletType(state.outletType),
            address = state.address,
            landmark = state.landmark.ifBlank { null },
            locality = state.locality.ifBlank { null },
            latitude = state.gpsLocation?.latitude ?: 0.0,
            longitude = state.gpsLocation?.longitude ?: 0.0,
            locationId = locationId,
            businessId = businessId
        )

        return outletApi.createOutlet(request).map { response ->
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error ?: "Failed to create outlet")
            }
        }
    }

    suspend fun getOutletById(outletId: String): NetworkResult<OutletResponseData, ApiError> {
        return outletApi.getOutletById(outletId).map { response ->
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error ?: "Failed to fetch outlet")
            }
        }
    }

    suspend fun getOutletKyc(outletId: String): NetworkResult<OutletKycData, ApiError> {
        return outletApi.getOutletKyc(outletId).map { response ->
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception("Failed to fetch KYC details")
            }
        }
    }

    suspend fun getOutlets(
        page: Int = 0,
        size: Int = 20,
        showLoader: Boolean = true,
        coolerComplianceStatus: String? = null,
        marketingComplianceStatus: String? = null
    ): NetworkResult<OutletListPageDto, ApiError> {
        return outletApi.getOutlets(page, size, showLoader, coolerComplianceStatus, marketingComplianceStatus)
            .map { response ->
                if (response.success && response.data != null) {
                    response.data
                } else {
                    throw Exception(response.error ?: "Failed to fetch outlets")
                }
            }
    }

    /**
     * Count of (team-scoped) outlets whose [kind] asset is at [complianceStatus]
     * — e.g. NOT_REQUESTED, for the CSO "to request" pills. Uses size=1 and reads
     * the page's totalElements. Requires the backend compliance-status filter.
     */
    suspend fun countOutletsByCompliance(
        kind: String,
        complianceStatus: String
    ): NetworkResult<Int, ApiError> {
        val cooler = if (kind == "COOLER") complianceStatus else null
        val marketing = if (kind == "MARKETING") complianceStatus else null
        return outletApi.getOutlets(
            page = 0, size = 1, showLoader = false,
            coolerComplianceStatus = cooler, marketingComplianceStatus = marketing
        ).map { it.data?.totalElements ?: 0 }
    }

    /**
     * Submits business details (Step 2 + Step 4 data) with optional cancelled cheque.
     */
    suspend fun submitBusinessDetails(
        state: OnboardingState,
        outletId: String
    ): NetworkResult<OutletResponseData, ApiError> {
        val annualVolume = 0  // Volume is no longer user-entered; slab decided by sales

        val distributorId = state.selectedDistributorId
        if (distributorId.isBlank()) {
            return NetworkResult.Error(ApiError(-1, "Please select a distributor"))
        }

        val isFixed = state.payoutType == com.siteflow.signature.cso.onboarding.domain.PayoutType.FIXED
        val isBankMode = state.bankPaymentMode == com.siteflow.signature.cso.onboarding.domain.BankPaymentMode.BANK

        val dto = BusinessDetailsRequestDto(
            classification = state.selectedClassification.ifBlank { "D_CLASS" },
            slabId = state.selectedSlabId.ifBlank { null },
            distributorId = distributorId,
            plannedAnnualVolume = annualVolume,
            monthlyRentalAmount = state.monthlyRentalAmount.toIntOrNull() ?: 0,
            expectedSalesPotential = state.expectedSalesPotential.toIntOrNull() ?: 0,
            stockingCommitment = emptyList(),
            upiId = state.upiId.ifBlank { null },
            bankAccountNumber = state.accountNumber,
            bankAccountType = if (isBankMode) state.bankAccountType.name else null,
            ifscCode = state.ifscCode,
            accountHolderName = state.accountHolderName,
            bankName = state.bankName,
            branch = state.branchName.ifBlank { null },
            payoutType = if (isFixed) "FIXED" else "DYNAMIC",
            // Backend requires slabClassification (@NotNull). For FIXED the
            // classification UI is hidden, so default to SILVER when unset.
            slabClassification = state.selectedClassification.ifBlank { "SILVER" },
            monthlyVolumeCommitment = if (isFixed) state.fixedMonthlyVolume.toIntOrNull() else null,
            monthlyPayoutAmount = if (isFixed) state.fixedMonthlyAmount.toIntOrNull() else null
        )

        // Read cancelled cheque image bytes (if captured)
        val chequeBytes = state.cancelledChequePath?.let { path ->
            ImageEncoder.encodeToBytes(path)
        }

        println("┌── Business Details Payload ─────────")
        println("│ outletId              : $outletId")
        println("│ classification        : ${dto.classification}")
        println("│ payoutType            : ${dto.payoutType}")
        println("│ slabClassification    : ${dto.slabClassification}")
        println("│ monthlyVolumeCommit.  : ${dto.monthlyVolumeCommitment}")
        println("│ monthlyPayoutAmount   : ${dto.monthlyPayoutAmount}")
        println("│ distributorId         : ${dto.distributorId}")
        println("│ annualVolume          : ${dto.plannedAnnualVolume}")
        println("│ accountHolder         : ${dto.accountHolderName}")
        println("│ bankName              : ${dto.bankName}")
        println("│ branch                : ${dto.branch}")
        println("│ accountNumber         : ${dto.bankAccountNumber}")
        println("│ bankAccountType       : ${dto.bankAccountType}")
        println("│ ifscCode              : ${dto.ifscCode}")
        println("│ upiId                 : ${dto.upiId}")
        println("│ chequeImage           : ${if (chequeBytes != null) "${chequeBytes.size} bytes" else "none"}")
        println("└──────────────────────────────────────")


        return outletApi.submitBusinessDetails(outletId, dto, chequeBytes).map { response ->
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error ?: "Failed to submit business details")
            }
        }
    }

    /**
     * Submits KYC details (Step 4 data).
     */
    suspend fun submitKycDetails(
        state: OnboardingState,
        outletId: String
    ): NetworkResult<OutletResponseData, ApiError> {
        val dto = KycDetailsRequestDto(
            idProofType = state.kycIdType,
            idNumber = state.kycIdNumber,
            gstNumber = state.kycGstNumber.ifBlank { null },
            locationProofType = state.kycLocationType
        )

        val idProofBytes = state.kycIdProofPath?.let { ImageEncoder.encodeToBytes(it) }
            ?: return NetworkResult.Error(ApiError(-1, "ID Proof not captured"))

        val gstBytes = state.kycGstCertificatePath?.let { ImageEncoder.encodeToBytes(it) }

        val locationProofBytes = state.kycLocationProofPath?.let { ImageEncoder.encodeToBytes(it) }
            ?: return NetworkResult.Error(ApiError(-1, "Location Proof not captured"))

        println("┌── KYC Details Payload ──────────────")
        println("│ outletId     : $outletId")
        println("│ idType       : ${dto.idProofType}")
        println("│ idNumber     : ${dto.idNumber.take(2)}****${dto.idNumber.takeLast(2)}")
        println("│ gstNumber    : ${dto.gstNumber?.take(2)}****${dto.gstNumber?.takeLast(2)}")
        println("│ locationType : ${dto.locationProofType}")
        println("│ idProof      : ${idProofBytes.size} bytes")
        println("│ gstCert      : ${gstBytes?.size ?: 0} bytes")
        println("│ locationProof: ${locationProofBytes.size} bytes")
        println("└──────────────────────────────────────")

        return outletApi.submitKycDetails(
            outletId = outletId,
            data = dto,
            idProofBytes = idProofBytes,
            gstCertificateBytes = gstBytes,
            locationProofBytes = locationProofBytes
        ).map { response ->
            val data: OutletResponseData? = response.data
            if (response.success && data != null) {
                data
            } else {
                val errorMsg = response.error ?: "Failed to submit KYC details"
                throw Exception(errorMsg)
            }
        }
    }

    /**
     * Maps UI-friendly outlet type labels to backend enum values.
     */
    private fun mapOutletType(uiType: String): String = when (uiType) {
        "Grocery" -> "GROCERY"
        "E&D" -> "E_AND_D"
        "Highway Dhaba" -> "HIGHWAY_DHABA"
        "Paan Shop" -> "PAAN_SHOP"
        "OAGS (MT)" -> "OAGS_MT"
        "Bakery" -> "BAKERY"
        "Convenience" -> "CONVENIENCE"
        "Bus Stand" -> "BUS_STAND"
        "Others" -> "OTHERS"
        else -> uiType.uppercase().replace(" ", "_")
    }

    /**
     * Maps Classification enum to backend string.
     */
    private fun mapClassification(classification: Classification): String = when (classification) {
        Classification.PLATINUM -> "PLATINUM"
        Classification.DIAMOND -> "DIAMOND"
        Classification.GOLD -> "GOLD"
        Classification.SILVER -> "SILVER"
    }

    /**
     * Submits photos (Step 5) as multipart form data.
     * Each captured photo slot is encoded to bytes and uploaded
     * with the slot ID as the form field name.
     */
    suspend fun submitPhotos(
        state: OnboardingState,
        outletId: String
    ): NetworkResult<OutletResponseData, ApiError> {
        val photoEntries = state.photoSlots
            .filter { it.imagePath != null }
            .mapNotNull { slot ->
                val bytes = ImageEncoder.encodeToBytes(slot.imagePath!!, maxSize = 512_000)
                if (bytes != null) slot.id to bytes else null
            }

        if (photoEntries.isEmpty()) {
            return NetworkResult.Error(ApiError(-1, "No photos captured"))
        }

        println("┌── Photos Payload ───────────────────")
        println("│ outletId   : $outletId")
        println("│ photoCount : ${photoEntries.size}")
        photoEntries.forEach { (type, bytes) ->
            println("│ $type : ${bytes.size} bytes")
        }
        println("└──────────────────────────────────────")

        return outletApi.submitPhotos(outletId, photoEntries).map { response ->
            val data: OutletResponseData? = response.data
            if (response.success && data != null) {
                data
            } else {
                throw Exception(response.error ?: "Failed to submit photos")
            }
        }
    }

    /**
     * Requests agreement OTP to be sent to the outlet owner.
     */
    suspend fun requestAgreementOtp(
        outletId: String
    ): NetworkResult<OutletResponseData, ApiError> {
        println("┌── Request Agreement OTP ────────────")
        println("│ outletId : $outletId")
        println("└──────────────────────────────────────")

        return outletApi.requestAgreementOtp(outletId).map { response ->
            val data = response.data
            if (response.success && data != null) {
                data
            } else {
                throw Exception(response.error ?: "Failed to request agreement OTP")
            }
        }
    }

    /**
     * Verifies the agreement OTP. After success, outlet status becomes ASM_PENDING.
     */
    suspend fun verifyAgreementOtp(
        outletId: String,
        otp: String
    ): NetworkResult<OutletResponseData, ApiError> {
        println("┌── Verify Agreement OTP ─────────────")
        println("│ outletId : $outletId")
        println("│ otp      : ${otp.take(2)}****")
        println("└──────────────────────────────────────")

        return outletApi.verifyAgreementOtp(outletId, otp).map { response ->
            val data = response.data
            if (response.success && data != null) {
                data
            } else {
                throw Exception(response.error ?: "Failed to verify agreement OTP")
            }
        }
    }

    /**
     * Approve/reject an outlet via the unified decision endpoint. The backend
     * resolves L1 (ASE on ASE_PENDING) vs L2 (ASM on ASM_PENDING) from the
     * caller's role + the outlet's status.
     *
     * @param action "APPROVE" | "REJECT" | "RESUBMIT"
     */
    suspend fun decide(
        outletId: String,
        action: String,
        reason: String? = null
    ): NetworkResult<OutletResponseData, ApiError> {
        println("┌── Outlet Decision ──────────────────")
        println("│ outletId : $outletId")
        println("│ action   : $action")
        println("│ reason   : $reason")
        println("└──────────────────────────────────────")

        return outletApi.decide(outletId, action, reason).map { response ->
            val data = response.data
            if (response.success && data != null) {
                data
            } else {
                throw Exception(response.error ?: "Failed to $action outlet")
            }
        }
    }

    /** ASM L2 approve — delegates to the unified [decide] endpoint. */
    suspend fun asmApprove(outletId: String): NetworkResult<OutletResponseData, ApiError> =
        decide(outletId, "APPROVE")

    /** ASM L2 reject — delegates to the unified [decide] endpoint. */
    suspend fun asmReject(outletId: String, remarks: String): NetworkResult<OutletResponseData, ApiError> =
        decide(outletId, "REJECT", remarks)

    /**
     * CSO raises a COOLER asset request for an ACTIVE outlet
     * (POST /asset-requests, status -> REQUESTED).
     */
    suspend fun requestCooler(
        outletId: String,
        coolerSize: String,
        quantity: Int,
        details: String?
    ): NetworkResult<com.siteflow.signature.cso.onboarding.data.dto.AssetRequestData, ApiError> {
        println("┌── Request Cooler ───────────────────")
        println("│ outletId   : $outletId")
        println("│ coolerSize : $coolerSize")
        println("│ quantity   : $quantity")
        println("│ details    : $details")
        println("└──────────────────────────────────────")

        val request = com.siteflow.signature.cso.onboarding.data.dto.CreateAssetRequestDto(
            outletId = outletId,
            kind = "COOLER",
            coolerSize = coolerSize,
            quantity = quantity,
            details = details?.ifBlank { null }
        )

        return outletApi.createAssetRequest(request).map { response ->
            val data = response.data
            if (response.success && data != null) {
                data
            } else {
                throw Exception(response.error ?: "Failed to raise cooler request")
            }
        }
    }

    /**
     * CSO raises a MARKETING/branding request for an ACTIVE outlet. Each item is
     * {assetType, quantity, brands[]}; at least one required.
     */
    suspend fun requestMarketing(
        outletId: String,
        items: List<com.siteflow.signature.cso.onboarding.data.dto.MarketingItemDto>,
        details: String?
    ): NetworkResult<com.siteflow.signature.cso.onboarding.data.dto.AssetRequestData, ApiError> {
        val request = com.siteflow.signature.cso.onboarding.data.dto.CreateAssetRequestDto(
            outletId = outletId,
            kind = "MARKETING",
            items = items,
            details = details?.ifBlank { null }
        )
        return outletApi.createAssetRequest(request).map { response ->
            val data = response.data
            if (response.success && data != null) data
            else throw Exception(response.error ?: "Failed to raise branding request")
        }
    }

    /**
     * Team-scoped asset requests of [kind] at [status] (for the ASE/ASM approval
     * finder). ASE sees REQUESTED, ASM sees ASE_APPROVED.
     */
    suspend fun getAssetRequestsForApproval(
        status: String?,
        kind: String
    ): NetworkResult<List<com.siteflow.signature.cso.onboarding.data.dto.AssetRequestItemDto>, ApiError> {
        return outletApi.getAssetRequests(status = status, kind = kind)
            .map { it.data?.content ?: emptyList() }
    }

    /**
     * Upload an asset compliance photo for an outlet: finds the outlet's request
     * of [kind] awaiting compliance (EXECUTED / OVERDUE / NON_COMPLIANT) and posts it.
     */
    suspend fun uploadAssetCompliance(
        outletId: String,
        kind: String,
        photoBytes: ByteArray
    ): NetworkResult<Boolean, ApiError> {
        val requestId: String? = when (
            val res = outletApi.getAssetRequests(kind = kind, outletId = outletId)
        ) {
            is NetworkResult.Success -> res.data.data?.content?.firstOrNull {
                it.status == "EXECUTED" || it.status == "COMPLIANCE_OVERDUE" || it.status == "NON_COMPLIANT"
            }?.id
            is NetworkResult.Error -> return NetworkResult.Error(res.error)
        }
        if (requestId == null) {
            return NetworkResult.Error(ApiError(-1, "No $kind asset awaiting compliance for this outlet"))
        }
        return outletApi.uploadCompliancePhoto(requestId, photoBytes).map { resp ->
            if (resp.success) true else throw Exception(resp.error ?: "Compliance upload failed")
        }
    }

    /**
     * The outlet's current (non-terminal) request of [kind], for showing the
     * approval/action on the outlet detail. Null if none / only rejected.
     */
    suspend fun getOutletAssetRequest(
        outletId: String,
        kind: String
    ): NetworkResult<com.siteflow.signature.cso.onboarding.data.dto.AssetRequestItemDto?, ApiError> {
        return outletApi.getAssetRequests(kind = kind, outletId = outletId).map { resp ->
            resp.data?.content?.firstOrNull { it.status != null && it.status != "REJECTED" }
        }
    }

    /** Approve/reject an asset request via POST /asset-requests/{id}/decision. */
    suspend fun decideAssetRequest(
        requestId: String,
        action: String,
        reason: String?
    ): NetworkResult<Boolean, ApiError> {
        return outletApi.decideAssetRequest(requestId, action, reason).map { response ->
            if (response.success) true
            else throw Exception(response.error ?: "Failed to $action cooler request")
        }
    }

    /**
     * ASE submits compliance data with asset photos.
     */
    suspend fun submitCompliance(
        outletId: String,
        coolerInstalled: Boolean,
        serialNo: String,
        coolerType: String,
        capacity: String,
        signageInstalled: Boolean,
        coolerImagePath: String,
        assetLabelImagePath: String,
        signageImagePath: String,
        outerOutletImagePath: String,
        innerOutletImagePath: String
    ): NetworkResult<OutletResponseData, ApiError> {
        SignatureLog.d("Signature-Repo", "┌── Submit Compliance ─────────────────")
        SignatureLog.d("Signature-Repo", "│ outletId        : $outletId")
        SignatureLog.d("Signature-Repo", "│ coolerInstalled : $coolerInstalled")
        SignatureLog.d("Signature-Repo", "│ serialNo        : $serialNo")
        SignatureLog.d("Signature-Repo", "│ coolerType      : $coolerType")
        SignatureLog.d("Signature-Repo", "│ capacity        : $capacity")
        SignatureLog.d("Signature-Repo", "│ signageInstalled: $signageInstalled")
        SignatureLog.d("Signature-Repo", "└──────────────────────────────────────")

        val dto = com.siteflow.signature.cso.onboarding.data.dto.ComplianceRequestDto(
            coolerInstalled = coolerInstalled,
            serialNo = serialNo,
            coolerType = coolerType,
            capacity = capacity,
            signageInstalled = signageInstalled
        )

        val coolerBytes = ImageEncoder.encodeToBytes(coolerImagePath)
            ?: return NetworkResult.Error(ApiError(-1, "Failed to encode cooler image"))
        val assetLabelBytes = ImageEncoder.encodeToBytes(assetLabelImagePath)
            ?: return NetworkResult.Error(ApiError(-1, "Failed to encode asset label image"))
        val signageBytes = ImageEncoder.encodeToBytes(signageImagePath)
            ?: return NetworkResult.Error(ApiError(-1, "Failed to encode signage image"))
        val outerBytes = ImageEncoder.encodeToBytes(outerOutletImagePath)
            ?: return NetworkResult.Error(ApiError(-1, "Failed to encode outer outlet image"))
        val innerBytes = ImageEncoder.encodeToBytes(innerOutletImagePath)
            ?: return NetworkResult.Error(ApiError(-1, "Failed to encode inner outlet image"))

        val totalBytes = coolerBytes.size + assetLabelBytes.size + signageBytes.size +
                outerBytes.size + innerBytes.size
        SignatureLog.d("Signature-Repo", "┌── Compliance Photo Sizes ────────────")
        SignatureLog.d("Signature-Repo", "│ coolerImage      : ${coolerBytes.size} B  (~${coolerBytes.size / 1024} KB)")
        SignatureLog.d("Signature-Repo", "│ assetLabelImage  : ${assetLabelBytes.size} B  (~${assetLabelBytes.size / 1024} KB)")
        SignatureLog.d("Signature-Repo", "│ signageImage     : ${signageBytes.size} B  (~${signageBytes.size / 1024} KB)")
        SignatureLog.d("Signature-Repo", "│ outerOutletImage : ${outerBytes.size} B  (~${outerBytes.size / 1024} KB)")
        SignatureLog.d("Signature-Repo", "│ innerOutletImage : ${innerBytes.size} B  (~${innerBytes.size / 1024} KB)")
        SignatureLog.d("Signature-Repo", "│ ─────────────────────────────────────")
        SignatureLog.d("Signature-Repo", "│ TOTAL PAYLOAD    : ${totalBytes} B  (~${totalBytes / 1024} KB)")
        SignatureLog.d("Signature-Repo", "└──────────────────────────────────────")

        return outletApi.submitCompliance(
            outletId = outletId,
            body = dto,
            coolerImageBytes = coolerBytes,
            assetLabelImageBytes = assetLabelBytes,
            signageImageBytes = signageBytes,
            outerOutletImageBytes = outerBytes,
            innerOutletImageBytes = innerBytes
        ).map { response ->
            val data = response.data
            if (response.success && data != null) {
                data
            } else {
                throw Exception(response.error ?: "Failed to submit compliance")
            }
        }
    }


    /**
     * ASM verifies/approves ASE's compliance submission.
     */
    suspend fun verifyCompliance(
        complianceId: String
    ): NetworkResult<OutletResponseData, ApiError> {
        println("┌── Verify Compliance ─────────────────")
        println("│ complianceId : $complianceId")
        println("└──────────────────────────────────────")

        return outletApi.verifyCompliance(complianceId).map { response ->
            val data = response.data
            if (response.success && data != null) {
                data
            } else {
                throw Exception(response.error ?: "Failed to verify compliance")
            }
        }
    }
}

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

    suspend fun getOutlets(page: Int = 0, size: Int = 20, showLoader: Boolean = true): NetworkResult<OutletListPageDto, ApiError> {
        return outletApi.getOutlets(page, size, showLoader).map { response ->
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error ?: "Failed to fetch outlets")
            }
        }
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
            stockingCommitment = emptyList(),
            upiId = state.upiId.ifBlank { null },
            bankAccountNumber = state.accountNumber,
            bankAccountType = if (isBankMode) state.bankAccountType.name else null,
            ifscCode = state.ifscCode,
            accountHolderName = state.accountHolderName,
            bankName = state.bankName,
            branch = state.branchName.ifBlank { null },
            payoutType = if (isFixed) "FIXED" else "DYNAMIC",
            slabClassification = state.selectedClassification.ifBlank { null },
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
     * ASM approves the outlet. Status becomes ASM_APPROVED.
     */
    suspend fun asmApprove(
        outletId: String
    ): NetworkResult<OutletResponseData, ApiError> {
        println("┌── ASM Approve ──────────────────────")
        println("│ outletId : $outletId")
        println("└──────────────────────────────────────")

        return outletApi.asmApprove(outletId).map { response ->
            val data = response.data
            if (response.success && data != null) {
                data
            } else {
                throw Exception(response.error ?: "Failed to approve outlet")
            }
        }
    }

    /**
     * ASM rejects the outlet.
     */
    suspend fun asmReject(
        outletId: String,
        remarks: String
    ): NetworkResult<OutletResponseData, ApiError> {
        println("┌── ASM Reject ───────────────────────")
        println("│ outletId : $outletId")
        println("│ remarks  : $remarks")
        println("└──────────────────────────────────────")

        return outletApi.asmReject(outletId, remarks).map { response ->
            val data = response.data
            if (response.success && data != null) {
                data
            } else {
                throw Exception(response.error ?: "Failed to reject outlet")
            }
        }
    }

    /**
     * ASE requests an asset (cooler/branding) for an approved outlet.
     */
    suspend fun requestAsset(
        outletId: String,
        coolerType: String,
        capacity: String,
        signageType: String,
        dmsId: String
    ): NetworkResult<OutletResponseData, ApiError> {
        println("┌── Request Asset ────────────────────")
        println("│ outletId    : $outletId")
        println("│ coolerType  : $coolerType")
        println("│ capacity    : $capacity")
        println("│ signageType : $signageType")
        println("│ dmsId       : $dmsId")
        println("└──────────────────────────────────────")

        val request = com.siteflow.signature.cso.onboarding.data.dto.AssetRequestDto(
            coolerType = coolerType,
            capacity = capacity,
            signageType = signageType,
            dmsId = dmsId
        )

        return outletApi.requestAsset(outletId, request).map { response ->
            val data = response.data
            if (response.success && data != null) {
                data
            } else {
                throw Exception(response.error ?: "Failed to request asset")
            }
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

package com.siteflow.cdo.outlet.walkthrough.data

import kotlinx.serialization.Serializable

@Serializable
data class OutletKycResponseDto(
    val success: Boolean,
    val data: OutletKycData? = null,
    val timestamp: String? = null
)

@Serializable
data class OutletKycData(
    val outletId: String,
    val aadhaarDocUrl: String? = null,
    val outletDocUrl: String? = null,
    val bankAccountNumber: String? = null,
    val bankAccountType: String? = null,
    val ifscCode: String? = null,
    val upiId: String? = null,
    val aadhaarNumber: String? = null,
    val gstNumber: String? = null,
    val gstCertificateUrl: String? = null,
    val cancelledChequeUrl: String? = null,
    val accountHolderName: String? = null,
    val bankName: String? = null,
    val branch: String? = null,
    val swiftCode: String? = null,
    val ownerImageUrl: String? = null,
    val idProofType: String? = null,
    val idNumber: String? = null,
    val idProofUrl: String? = null,
    val locationProofType: String? = null,
    val locationProofUrl: String? = null,
    val verifiedBy: String? = null,
    val verifiedAt: String? = null,
    val createdAt: String? = null
)

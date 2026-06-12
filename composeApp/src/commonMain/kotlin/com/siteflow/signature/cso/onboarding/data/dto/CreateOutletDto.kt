package com.siteflow.signature.cso.onboarding.data.dto

import kotlinx.serialization.Serializable

// ── Request ──

@Serializable
data class CreateOutletRequestDto(
    val name: String,
    val phone: String,
    val ownerName: String,
    val ownerMobile: String,
    val ownerWhatsapp: String,
    val email: String? = null,
    val outletType: String,
    val address: String,
    val landmark: String? = null,
    val locality: String? = null,
    val latitude: Double,
    val longitude: Double,
    val locationId: String,
    val businessId: String
)

// ── Response ──

@Serializable
data class CreateOutletResponseDto(
    val success: Boolean,
    val data: OutletResponseData? = null,
    val errorCode: String? = null,
    val error: String? = null,
    val timestamp: String? = null
)

@Serializable
data class OutletResponseData(
    val id: String,
    val name: String,
    val phone: String,
    val ownerName: String,
    val ownerMobile: String,
    val ownerWhatsapp: String? = null,
    val email: String? = null,
    val outletType: String,
    val address: String,
    val landmark: String? = null,
    val locality: String? = null,
    val locationId: String? = null,
    val pincode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val classification: String? = null,
    val plannedAnnualVolume: Double? = null,
    val stockingCommitment: String? = null,
    val outletStatus: String? = null,
    val operationalStatus: String? = null,
    val assetStatus: String? = null,
    val complianceState: String? = null,
    val complianceId: String? = null,
    val relaxationEndDate: String? = null,
    val daysRemaining: Int? = null,
    val businessId: String? = null,
    val distributorId: String? = null,
    val dmsId: String? = null,
    val createdByAseId: String? = null,
    val createdByAseName: String? = null,
    val onboardedAt: String? = null,
    val activatedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val photos: List<OutletPhotoDto> = emptyList(),
    val complianceRecords: List<ComplianceRecordDto> = emptyList()
)

@Serializable
data class OutletPhotoDto(
    val id: String? = null,
    val photoUrl: String? = null,
    val photoType: String? = null,
    val uploadedAt: String? = null,
    val uploadedByName: String? = null
)

@Serializable
data class ComplianceRecordDto(
    val id: String,
    val outletId: String? = null,
    val coolerInstalled: Boolean = false,
    val serialNo: String? = null,
    val coolerType: String? = null,
    val capacity: String? = null,
    val signageInstalled: Boolean = false,
    val verified: Boolean = false,
    val uploadedAt: String? = null,
    val uploadedByName: String? = null,
    val verifiedAt: String? = null,


    val verifiedByName: String? = null,
    val images: List<ComplianceImageDto> = emptyList()
)

@Serializable
data class ComplianceImageDto(
    val id: String? = null,
    val imageUrl: String? = null,
    val imageType: String? = null
)

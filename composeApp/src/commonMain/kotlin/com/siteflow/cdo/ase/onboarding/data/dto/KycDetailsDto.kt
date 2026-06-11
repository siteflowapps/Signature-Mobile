package com.siteflow.cdo.ase.onboarding.data.dto

import kotlinx.serialization.Serializable

/**
 * JSON payload for POST /outlets/{outletId}/kyc-details
 * Sent as the "data" part of a multipart form.
 */
@Serializable
data class KycDetailsRequestDto(
    val idProofType: String,
    val idNumber: String,
    val gstNumber: String? = null,
    val locationProofType: String
)

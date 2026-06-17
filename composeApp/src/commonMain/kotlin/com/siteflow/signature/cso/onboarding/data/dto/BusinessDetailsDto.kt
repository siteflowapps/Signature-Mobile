package com.siteflow.signature.cso.onboarding.data.dto

import kotlinx.serialization.Serializable

/**
 * JSON payload for POST /outlets/{outletId}/business-details
 * Sent as the "data" part of a multipart form.
 */
@Serializable
data class BusinessDetailsRequestDto(
    val classification: String,
    val slabId: String? = null,
    val distributorId: String,
    val plannedAnnualVolume: Int,
    /** ₹/month outlet rent — required by backend (@NotNull). */
    val monthlyRentalAmount: Int,
    /** ₹/month expected sales — required by backend (@NotNull). */
    val expectedSalesPotential: Int,
    val stockingCommitment: List<String>,
    val upiId: String? = null,
    val bankAccountNumber: String,
    val bankAccountType: String? = null,
    val ifscCode: String,
    val accountHolderName: String,
    val bankName: String,
    val branch: String? = null,

    /** Always "DYNAMIC" — volume-based slab payout. Kept because the backend requires it (@NotNull). */
    val payoutType: String,

    /** Backend classification label e.g. "GOLD", "SILVER" — mirrors [classification] for payout endpoint. */
    val slabClassification: String? = null,

    /** No longer collected in the UI — hardcoded on submit. */
    val monthlyVolumeCommitment: Int? = null,

    /** No longer collected in the UI — hardcoded on submit. */
    val monthlyPayoutAmount: Int? = null
)

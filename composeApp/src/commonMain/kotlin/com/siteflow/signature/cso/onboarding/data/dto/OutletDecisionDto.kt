package com.siteflow.signature.cso.onboarding.data.dto

import kotlinx.serialization.Serializable

/**
 * Body for POST /outlets/{id}/decision.
 * @param action "APPROVE" | "REJECT" | "RESUBMIT"
 * @param reason optional rejection reason (omitted when null)
 */
@Serializable
data class OutletDecisionRequestDto(
    val action: String,
    val reason: String? = null
)

package com.siteflow.signature.cso.onboarding.data.dto

import kotlinx.serialization.Serializable

/**
 * Response from GET /slabs
 */
@Serializable
data class SlabsResponseDto(
    val success: Boolean,
    val data: SlabsPageDto? = null
)

@Serializable
data class SlabsPageDto(
    val content: List<SlabDto>,
    val page: Int = 0,
    val size: Int = 20,
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

@Serializable
data class SlabDto(
    val id: String,
    val businessId: String,
    val classification: String,
    val minQuantity: Double,
    val maxQuantity: Double? = null,
    val percentage: Double? = null,
    val percentageBelowTlp: Double? = null,
    val ratePerCase: Double? = null,
    val locationId: String? = null
)


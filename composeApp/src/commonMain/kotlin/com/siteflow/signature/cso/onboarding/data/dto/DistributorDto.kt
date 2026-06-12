package com.siteflow.signature.cso.onboarding.data.dto

import kotlinx.serialization.Serializable

/**
 * Response wrapper for GET /api/v1/distributors
 */
@Serializable
data class DistributorListResponseDto(
    val success: Boolean,
    val data: DistributorPageDto? = null,
    val error: String? = null
)

@Serializable
data class DistributorPageDto(
    val content: List<DistributorDto> = emptyList(),
    val page: Int = 0,
    val size: Int = 20,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

@Serializable
data class DistributorDto(
    val id: String,
    val name: String,
    val address: String? = null,
    val gstNumber: String? = null
)

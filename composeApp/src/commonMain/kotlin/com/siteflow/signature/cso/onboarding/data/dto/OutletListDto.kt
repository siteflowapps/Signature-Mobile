package com.siteflow.signature.cso.onboarding.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OutletListResponseDto(
    val success: Boolean,
    val data: OutletListPageDto? = null,
    val errorCode: String? = null,
    val error: String? = null,
    val timestamp: String? = null
)

@Serializable
data class OutletListPageDto(
    val content: List<OutletResponseData> = emptyList(),
    val page: Int = 0,
    val size: Int = 20,
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

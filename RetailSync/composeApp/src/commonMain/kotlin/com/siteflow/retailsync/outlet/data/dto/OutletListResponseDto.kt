package com.siteflow.retailsync.outlet.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OutletListResponseDto(
    val success: Boolean,
    val data: OutletPageDto? = null,
    val timestamp: String? = null
)

@Serializable
data class OutletPageDto(
    val content: List<OutletDto> = emptyList(),
    val page: Int = 0,
    val size: Int = 50,
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

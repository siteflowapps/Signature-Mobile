package com.siteflow.retailsync.order.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SkuListResponseDto(
    val success: Boolean,
    val data: List<SkuDto> = emptyList(),
    val timestamp: String? = null
)

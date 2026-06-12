package com.siteflow.signature.cso.onboarding.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationSearchResponseDto(
    val success: Boolean,
    val data: List<LocationDto> = emptyList(),
    val timestamp: String? = null
)

@Serializable
data class LocationDto(
    val id: String,
    val pincode: String,
    val city: String,
    val state: String
)

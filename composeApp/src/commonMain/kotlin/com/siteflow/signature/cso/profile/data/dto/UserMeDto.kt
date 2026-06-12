package com.siteflow.signature.cso.profile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserMeResponseDto(
    val success: Boolean,
    val data: UserMeDetailsDto? = null,
    val timestamp: String? = null
)

@Serializable
data class UserMeDetailsDto(
    val id: String,
    val name: String,
    val role: String,
    val authType: String,
    val email: String? = null,
    val phone: String,
    val status: String,
    val businessId: String? = null,
    val locationId: String? = null,
    val outletId: String? = null,
    val createdAt: String? = null
)

package com.siteflow.signature.login.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserMeResponseDto(
    val success: Boolean,
    val data: UserMeDataDto? = null,
    val timestamp: String? = null
)

@Serializable
data class UserMeDataDto(
    val id: String,
    val name: String,
    val role: String,
    val authType: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val status: String? = null,
    val businessId: String? = null,
    val locationId: String? = null,
    val outletId: String? = null,
    val distributorId: String? = null,
    val distributorName: String? = null,
    val createdAt: String? = null
)

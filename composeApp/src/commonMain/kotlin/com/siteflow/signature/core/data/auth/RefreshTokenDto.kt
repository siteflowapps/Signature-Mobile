package com.siteflow.signature.core.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponseDto(
    val success: Boolean,
    val data: RefreshTokenDataDto? = null,
    val timestamp: String? = null
)

@Serializable
data class RefreshTokenDataDto(
    val accessToken: String,
    val refreshToken: String,
    val role: String,
    val userId: String
)

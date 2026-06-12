package com.siteflow.signature.login.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpVerifyResponseDto(
    val success: Boolean,
    val data: TokenDataDto? = null,
    val errorCode: String? = null,
    val error: String? = null,
    val timestamp: String? = null
)

@Serializable
data class TokenDataDto(
    val accessToken: String,
    val refreshToken: String,
    val role: String,
    val userId: String
)

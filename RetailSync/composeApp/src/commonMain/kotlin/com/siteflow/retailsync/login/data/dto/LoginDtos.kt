package com.siteflow.retailsync.login.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpRequestDto(val phone: String)

@Serializable
data class OtpResponseDto(
    val success: Boolean,
    val data: String? = null,
    val errorCode: String? = null,
    val error: String? = null,
    val timestamp: String? = null
)

@Serializable
data class OtpVerifyRequestDto(
    val phone: String,
    val otp: String
)

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

@Serializable
data class UserMeResponseDto(
    val success: Boolean,
    val data: UserMeDataDto? = null
)

@Serializable
data class UserMeDataDto(
    val id: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val outletId: String? = null,
    val distributorId: String? = null
)

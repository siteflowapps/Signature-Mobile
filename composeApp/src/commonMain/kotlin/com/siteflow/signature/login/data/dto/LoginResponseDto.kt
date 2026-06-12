package com.siteflow.signature.login.data.dto

import com.siteflow.signature.core.data.networking.error.ErrorDto


import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val success: Boolean,
    val data: TokenDto? = null,
    val error: ErrorDto? = null,
    val timestamp: String
)


@Serializable
data class TokenDto(
    val token: String
    // future-ready:
    // val refreshToken: String? = null
)


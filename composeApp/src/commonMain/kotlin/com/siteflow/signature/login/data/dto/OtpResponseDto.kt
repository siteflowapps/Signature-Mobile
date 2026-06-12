package com.siteflow.signature.login.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpResponseDto(
    val success: Boolean,
    val data: String? = null,
    val errorCode: String? = null,
    val error: String? = null,
    val timestamp: String? = null
)

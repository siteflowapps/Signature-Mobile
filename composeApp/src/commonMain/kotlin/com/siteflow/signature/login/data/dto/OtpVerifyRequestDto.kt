package com.siteflow.signature.login.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpVerifyRequestDto(
    val phone: String,
    val otp: String
)

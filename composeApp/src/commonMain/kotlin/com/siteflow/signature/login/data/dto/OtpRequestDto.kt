package com.siteflow.signature.login.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpRequestDto(val phone: String)

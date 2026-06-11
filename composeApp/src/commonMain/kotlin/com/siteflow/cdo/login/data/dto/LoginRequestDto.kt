package com.siteflow.cdo.login.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val mobileNumber: String
)

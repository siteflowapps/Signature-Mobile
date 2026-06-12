package com.siteflow.signature.cso.onboarding.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AssetRequestDto(
    val coolerType: String,
    val capacity: String,
    val signageType: String,
    val dmsId: String
)

package com.siteflow.cdo.ase.onboarding.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ComplianceRequestDto(
    val coolerInstalled: Boolean,
    val serialNo: String,
    val coolerType: String,
    val capacity: String,
    val signageInstalled: Boolean
)

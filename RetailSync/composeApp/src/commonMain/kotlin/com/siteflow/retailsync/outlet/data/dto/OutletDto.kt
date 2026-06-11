package com.siteflow.retailsync.outlet.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OutletDto(
    val id: String,
    val name: String,
    val phone: String? = null,
    val ownerName: String? = null
)

package com.siteflow.retailsync.order.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SkuDto(
    val id: String,
    val articleDescription: String,
    val caseConfiguration: Int,
    val ml: String,
    val mrpPerBottle: Double,
    val mrpPerCase: Double,
    val articleType: String,
    val category: String,
    val packForm: String,
    val brand: String,
    val flavor: String
)

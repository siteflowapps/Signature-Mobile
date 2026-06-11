package com.siteflow.cdo.support.data

import kotlinx.serialization.Serializable

@Serializable
data class SupportTicketData(
    val category: String,
    val description: String,
    val appVersion: String,
    val platform: String,
    val osVersion: String,
    val deviceModel: String
)

@Serializable
data class SupportTicketResponse(
    val success: Boolean = false,
    val data: SupportTicketResponseData? = null
)

@Serializable
data class SupportTicketResponseData(
    val id: String,
    val ticketNumber: String,
    val status: String = "OPEN"
)

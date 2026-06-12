package com.siteflow.signature.support.data

import kotlinx.serialization.Serializable

@Serializable
data class TicketListResponse(
    val success: Boolean = false,
    val data: TicketListData? = null
)

@Serializable
data class TicketListData(
    val content: List<TicketItem> = emptyList(),
    val page: Int = 0,
    val size: Int = 20,
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

@Serializable
data class TicketItem(
    val id: String,
    val ticketNumber: String,
    val raisedBy: String = "",
    val raisedByName: String = "",
    val raisedByPhone: String = "",
    val category: String,
    val description: String,
    val status: String = "OPEN",
    val appVersion: String? = null,
    val platform: String? = null,
    val osVersion: String? = null,
    val deviceModel: String? = null,
    val screenshotUrls: List<String> = emptyList(),
    val resolutionNote: String? = null,
    val resolvedBy: String? = null,
    val resolvedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

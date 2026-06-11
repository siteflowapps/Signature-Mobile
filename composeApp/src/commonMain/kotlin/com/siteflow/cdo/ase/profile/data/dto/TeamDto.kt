package com.siteflow.cdo.ase.profile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TeamResponseDto(
    val success: Boolean,
    val data: TeamPageDto? = null,
    val errorCode: String? = null,
    val error: String? = null,
    val timestamp: String? = null
)

@Serializable
data class TeamPageDto(
    val content: List<AseUserDto> = emptyList(),
    val page: Int = 0,
    val size: Int = 10,
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

@Serializable
data class AseUserDto(
    val id: String,
    val name: String,
    val role: String,
    val authType: String? = null,
    val email: String? = null,
    val phone: String,
    val status: String,
    val businessId: String? = null,
    val locationId: String? = null,
    val createdAt: String? = null
)

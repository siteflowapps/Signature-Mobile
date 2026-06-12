package com.siteflow.signature.core.data.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Full envelope returned by GET /api/v1/config */
@Serializable
data class ConfigResponseDto(
    val success: Boolean = false,
    val data: ConfigData? = null,
    val timestamp: String? = null
)

@Serializable
data class ConfigData(
    val updateRequired: Boolean = false,
    val forceUpdate: Boolean = false,
    val latestVersion: String = "",
    val apiVersion: String = "",
    val features: Map<String, JsonElement> = emptyMap(),
    val userFlags: Map<String, String> = emptyMap()
)

/** Request body for PUT /api/v1/config/user-flags */
@Serializable
data class UpdateUserFlagRequest(
    val flagKey: String,
    val flagValue: String
)

/** Full envelope returned by PUT /api/v1/config/user-flags */
@Serializable
data class UpdateUserFlagResponseDto(
    val success: Boolean = false,
    val data: Map<String, String> = emptyMap(),
    val timestamp: String? = null
)

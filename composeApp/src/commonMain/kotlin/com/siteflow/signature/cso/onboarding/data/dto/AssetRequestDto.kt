package com.siteflow.signature.cso.onboarding.data.dto

import kotlinx.serialization.Serializable

/**
 * Body for POST /asset-requests (CreateAssetRequestPayload).
 * - kind = "COOLER": coolerSize (+ optional quantity) required
 * - kind = "MARKETING": items required
 */
@Serializable
data class CreateAssetRequestDto(
    val outletId: String,
    val kind: String,
    val details: String? = null,
    val coolerSize: String? = null,
    val quantity: Int? = null,
    val items: List<MarketingItemDto>? = null
)

@Serializable
data class MarketingItemDto(
    val assetType: String,
    val quantity: Int? = null,
    val brands: List<String>? = null
)

/** Minimal response wrapper for /asset-requests (unknown fields ignored). */
@Serializable
data class AssetRequestResponseDto(
    val success: Boolean = false,
    val data: AssetRequestData? = null,
    val errorCode: String? = null,
    val error: String? = null
)

@Serializable
data class AssetRequestData(
    val id: String? = null,
    val status: String? = null
)

/** Body for POST /asset-requests/{id}/decision. */
@Serializable
data class AssetDecisionDto(
    val action: String,   // "APPROVE" | "REJECT"
    val reason: String? = null
)

/** GET /asset-requests (paged) — fields the approval queue needs. */
@Serializable
data class AssetRequestListResponseDto(
    val success: Boolean = false,
    val data: AssetRequestPageDto? = null
)

@Serializable
data class AssetRequestPageDto(
    val content: List<AssetRequestItemDto> = emptyList()
)

@Serializable
data class AssetRequestItemDto(
    val id: String,
    val outletId: String? = null,
    val outletName: String? = null,
    val kind: String? = null,
    val status: String? = null,
    val coolerSize: String? = null,
    val quantity: Int? = null,
    val details: String? = null,
    val raisedByName: String? = null,
    val createdAt: String? = null,
    val items: List<MarketingItemDto> = emptyList()
)

/** Backend CoolerSize enum values + display labels for the request form. */
enum class CoolerSizeOption(val backendValue: String, val label: String) {
    SIZE_300L("SIZE_300L", "300 L"),
    SIZE_450L("SIZE_450L", "450 L"),
    SIZE_550L("SIZE_550L", "550 L"),
    SIZE_800L("SIZE_800L", "800 L"),
    SIZE_950L("SIZE_950L", "950 L")
}

/** Backend MarketingAssetType enum values + display labels for the branding form. */
enum class MarketingAssetType(val backendValue: String, val label: String) {
    NON_LIT_BOARD("NON_LIT_BOARD", "Non-lit board"),
    GLOW_SIGN_BOARD("GLOW_SIGN_BOARD", "Glow sign board"),
    ACP_BOARD("ACP_BOARD", "ACP board"),
    BRANDED_TRAYS("BRANDED_TRAYS", "Branded trays"),
    WALL_BRANDING("WALL_BRANDING", "Wall branding"),
    END_CAP("END_CAP", "End cap")
}

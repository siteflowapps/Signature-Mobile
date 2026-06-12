package com.siteflow.signature.cso.onboarding.data.dto

import kotlinx.serialization.Serializable

/**
 * Request body for POST /outlets/{outletId}/photos
 */
@Serializable
data class PhotosRequestDto(
    val outletPhotoUrls: List<String>
)

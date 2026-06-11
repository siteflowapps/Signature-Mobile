package com.siteflow.cdo.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

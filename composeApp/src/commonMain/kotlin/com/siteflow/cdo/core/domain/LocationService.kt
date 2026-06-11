package com.siteflow.cdo.core.domain

expect class LocationService {
    fun getCurrentLocation(
        onSuccess: (Double, Double) -> Unit,
        onFailure: (String) -> Unit
    )
}
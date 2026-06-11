package com.siteflow.cdo.ase.onboarding.data

import com.siteflow.cdo.ase.onboarding.data.dto.LocationSearchResponseDto
import com.siteflow.cdo.core.data.networking.client.NetworkConfig
import com.siteflow.cdo.core.data.networking.error.ApiError
import com.siteflow.cdo.core.data.networking.request.safeRequest
import com.siteflow.cdo.core.data.networking.result.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

class LocationApi(
    private val client: HttpClient
) {

    suspend fun searchByPincode(
        pincode: String
    ): NetworkResult<LocationSearchResponseDto, ApiError> {
        return safeRequest<LocationSearchResponseDto>(
            block = {
                client.get(NetworkConfig.v1("locations")) {
                    parameter("pincode", pincode)
                }
            }
        )
    }
}

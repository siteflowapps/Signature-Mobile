package com.siteflow.retailsync.outlet.data

import com.siteflow.retailsync.core.data.networking.client.NetworkConfig
import com.siteflow.retailsync.core.data.networking.error.ApiError
import com.siteflow.retailsync.core.data.networking.request.safeRequest
import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.outlet.data.dto.OutletListResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class OutletApi(
    private val client: HttpClient
) {

    suspend fun getOutlets(
        page: Int = 0,
        size: Int = 50
    ): NetworkResult<OutletListResponseDto, ApiError> {
        return safeRequest<OutletListResponseDto>(
            showLoader = false,
            block = {
                client.get("${NetworkConfig.BASE_URL}/outlets") {
                    parameter("page", page)
                    parameter("size", size)
                }
            }
        )
    }
}

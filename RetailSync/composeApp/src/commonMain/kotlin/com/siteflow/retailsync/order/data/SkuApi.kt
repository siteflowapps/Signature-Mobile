package com.siteflow.retailsync.order.data

import com.siteflow.retailsync.core.data.networking.client.NetworkConfig
import com.siteflow.retailsync.core.data.networking.error.ApiError
import com.siteflow.retailsync.core.data.networking.request.safeRequest
import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.order.data.dto.SkuListResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class SkuApi(
    private val client: HttpClient
) {

    suspend fun getMasterSkus(): NetworkResult<SkuListResponseDto, ApiError> {
        return safeRequest<SkuListResponseDto>(
            showLoader = true,
            block = {
                client.get("${NetworkConfig.BASE_URL}/skus")
            }
        )
    }
}

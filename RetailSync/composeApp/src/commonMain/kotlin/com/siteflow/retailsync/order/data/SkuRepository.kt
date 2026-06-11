package com.siteflow.retailsync.order.data

import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.order.data.dto.SkuDto

class SkuRepository(private val api: SkuApi) {

    suspend fun getMasterSkus(): List<SkuDto> {
        return when (val result = api.getMasterSkus()) {
            is NetworkResult.Success -> {
                if (result.data.success) {
                    result.data.data
                } else {
                    throw Exception("API returned success=false")
                }
            }
            is NetworkResult.Error -> {
                throw Exception(result.error.message)
            }
        }
    }
}

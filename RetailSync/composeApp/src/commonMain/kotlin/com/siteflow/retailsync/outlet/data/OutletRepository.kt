package com.siteflow.retailsync.outlet.data

import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.outlet.data.dto.OutletDto

class OutletRepository(private val api: OutletApi) {

    private var cachedOutlets: List<OutletDto> = emptyList()

    suspend fun fetchOutlets() {
        when (val result = api.getOutlets()) {
            is NetworkResult.Success -> {
                if (result.data.success) {
                    cachedOutlets = result.data.data?.content ?: emptyList()
                    println("[OutletRepository] Fetched ${cachedOutlets.size} outlets")
                }
            }
            is NetworkResult.Error -> {
                println("[OutletRepository] Failed to fetch outlets: ${result.error.message}")
            }
        }
    }

    fun getOutletById(id: String): OutletDto? {
        return cachedOutlets.find { it.id == id }
    }

    fun getAllOutlets(): List<OutletDto> = cachedOutlets
}

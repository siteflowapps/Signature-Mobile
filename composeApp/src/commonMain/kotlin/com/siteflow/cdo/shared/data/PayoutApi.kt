package com.siteflow.cdo.shared.data

import com.siteflow.cdo.core.data.networking.client.NetworkConfig
import com.siteflow.cdo.core.data.networking.error.ApiError
import com.siteflow.cdo.core.data.networking.request.safeRequest
import com.siteflow.cdo.core.data.networking.result.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get

/**
 * API client for payout calculations.
 * Shared across all roles (ASE, ASM, Retailer).
 */
class PayoutApi(
    private val client: HttpClient
) {
    /**
     * GET /payouts/calculate/{invoiceId}
     */
    suspend fun calculatePayout(
        invoiceId: String
    ): NetworkResult<PayoutCalculationResponseDto, ApiError> {
        return safeRequest<PayoutCalculationResponseDto>(
            block = {
                client.get(NetworkConfig.v1("payouts/calculate/$invoiceId"))
            }
        )
    }
}

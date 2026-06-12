package com.siteflow.signature.outlet.dashboard.data

import com.siteflow.signature.core.data.networking.client.NetworkConfig
import com.siteflow.signature.core.data.networking.error.ApiError
import com.siteflow.signature.core.data.networking.request.safeRequest
import com.siteflow.signature.core.data.networking.result.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

/**
 * API client for GET /api/v1/dashboard.
 */
class DashboardApi(
    private val client: HttpClient
) {
    suspend fun getDashboard(): NetworkResult<DashboardResponseDto, ApiError> {
        return safeRequest<DashboardResponseDto>(
            block = {
                client.get(NetworkConfig.v1("dashboard"))
            }
        )
    }
}

// ── DTOs ──

@Serializable
data class DashboardResponseDto(
    val success: Boolean,
    val data: DashboardData? = null,
    val timestamp: String? = null
)

@Serializable
data class DashboardData(
    val totalOutlets: Int = 0,
    val inProgressOutlets: Int = 0,
    val activeOutlets: Int = 0,
    val suspendedOutlets: Int = 0,
    val asmPendingOutlets: Int = 0,
    val totalInvoices: Int = 0,
    val submittedInvoices: Int = 0,
    val aseApprovedInvoices: Int = 0,
    val asmApprovedInvoices: Int = 0,
    val financeApprovedInvoices: Int = 0,
    val rejectedInvoices: Int = 0,
    val paidInvoices: Int = 0,
    val totalPayouts: Int = 0,
    val pendingPayouts: Int = 0,
    val paidPayouts: Int = 0,
    val totalPayoutAmount: Double = 0.0,
    val paidPayoutAmount: Double = 0.0,
    val totalUsers: Int = 0,
    val totalAse: Int = 0,
    val totalDistributors: Int = 0,
    val totalBusinesses: Int = 0,
    val pendingInvoices: Int = 0
)

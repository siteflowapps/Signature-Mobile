package com.siteflow.signature.support.data

import com.siteflow.signature.core.data.networking.error.ApiError
import com.siteflow.signature.core.data.networking.result.NetworkResult
import com.siteflow.signature.core.util.ImageEncoder

class SupportRepository(private val api: SupportApi) {

    suspend fun getMyTickets(
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<TicketListResponse, ApiError> {
        return api.getMyTickets(page, size)
    }

    suspend fun createTicket(
        data: SupportTicketData,
        screenshotPaths: List<String> = emptyList()
    ): NetworkResult<SupportTicketResponse, ApiError> {
        val screenshots = screenshotPaths.mapNotNull { ImageEncoder.encodeToBytes(it, maxSize = 512_000) }
        return api.createTicket(data, screenshots)
    }
}

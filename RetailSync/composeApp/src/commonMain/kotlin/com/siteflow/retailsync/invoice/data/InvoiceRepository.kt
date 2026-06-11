package com.siteflow.retailsync.invoice.data

import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.core.data.networking.result.map
import com.siteflow.retailsync.invoice.data.dto.InvoiceDto
import com.siteflow.retailsync.invoice.data.dto.InvoiceUploadRequestDto
import com.siteflow.retailsync.invoice.data.dto.InvoiceUploadResponseDto
import com.siteflow.retailsync.core.data.networking.error.ApiError

class InvoiceRepository(
    private val api: InvoiceApi
) {
    suspend fun getInvoices(page: Int = 0, size: Int = 20): NetworkResult<List<InvoiceDto>, ApiError> {
        return api.getInvoices(page, size).map { response ->
            response.data?.content ?: emptyList()
        }
    }

    suspend fun createInvoice(data: InvoiceUploadRequestDto): NetworkResult<InvoiceUploadResponseDto, ApiError> {
        return api.createInvoice(data)
    }
}

package com.siteflow.retailsync.invoice.data

import com.siteflow.retailsync.core.data.networking.client.NetworkConfig
import com.siteflow.retailsync.core.data.networking.error.ApiError
import com.siteflow.retailsync.core.data.networking.request.safeRequest
import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.invoice.data.dto.InvoiceListResponseDto
import com.siteflow.retailsync.invoice.data.dto.InvoiceUploadRequestDto
import com.siteflow.retailsync.invoice.data.dto.InvoiceUploadResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * API client for invoice operations.
 */
class InvoiceApi(
    private val client: HttpClient
) {

    /**
     * POST /invoices — multipart form upload without photo.
     * Sends only the `data` part as JSON (same endpoint as CDO's uploadInvoice but without photo).
     */
    suspend fun createInvoice(
        data: InvoiceUploadRequestDto
    ): NetworkResult<InvoiceUploadResponseDto, ApiError> {
        val jsonBody = Json.encodeToString(data)
        println("┌── Invoice Create Request Body ──────")
        println("│ $jsonBody")
        println("└─────────────────────────────────────")

        return safeRequest<InvoiceUploadResponseDto>(
            block = {
                client.submitFormWithBinaryData(
                    url = "${NetworkConfig.BASE_URL}/invoices",
                    formData = formData {
                        // JSON data part (same key as CDO's uploadInvoice)
                        append(
                            "data",
                            jsonBody,
                            Headers.build {
                                append(HttpHeaders.ContentType, "application/json")
                            }
                        )
                    }
                )
            }
        )
    }

    /**
     * GET /invoices — paginated invoice list.
     */
    suspend fun getInvoices(
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<InvoiceListResponseDto, ApiError> {
        return safeRequest<InvoiceListResponseDto>(
            block = {
                client.get("${NetworkConfig.BASE_URL}/invoices") {
                    parameter("page", page)
                    parameter("size", size)
                }
            }
        )
    }
}

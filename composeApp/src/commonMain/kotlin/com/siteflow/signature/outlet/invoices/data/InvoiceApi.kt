package com.siteflow.signature.outlet.invoices.data

import com.siteflow.signature.core.data.networking.client.NetworkConfig
import com.siteflow.signature.core.data.networking.error.ApiError
import com.siteflow.signature.core.data.networking.request.safeRequest
import com.siteflow.signature.core.data.networking.result.NetworkResult
import com.siteflow.signature.outlet.invoices.domain.InvoiceFile
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * API client for invoice operations.
 */
class InvoiceApi(
    private val client: HttpClient
) {

    /**
     * POST /invoices — multipart form upload.
     * - `photo`: invoice file bytes (JPEG or PDF; MIME + filename read from [file])
     * - `data`:  JSON-encoded [InvoiceUploadRequestDto]
     */
    suspend fun uploadInvoice(
        file: InvoiceFile,
        data: InvoiceUploadRequestDto
    ): NetworkResult<InvoiceUploadResponseDto, ApiError> {
        return safeRequest<InvoiceUploadResponseDto>(
            block = {
                client.submitFormWithBinaryData(
                    url = NetworkConfig.v1("invoices"),
                    formData = formData {
                        append(
                            "photo",
                            file.bytes,
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=${file.filename}")
                                append(HttpHeaders.ContentType, file.mimeType)
                            }
                        )
                        append(
                            "data",
                            Json.encodeToString(data),
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
                client.get(NetworkConfig.v1("invoices")) {
                    parameter("page", page)
                    parameter("size", size)
                }
            }
        )
    }

    /**
     * GET /invoices/{id}/details — full single-invoice detail (incl. eventLog
     * timeline + createdBy), fetched fresh. Used by every role's detail screen.
     */
    suspend fun getInvoiceDetail(
        invoiceId: String
    ): NetworkResult<InvoiceDetailResponseDto, ApiError> {
        return safeRequest<InvoiceDetailResponseDto>(
            block = {
                client.get(NetworkConfig.v1("invoices/$invoiceId/details"))
            }
        )
    }

    /**
     * POST /invoices/{id}/approve
     */
    suspend fun approveInvoice(
        invoiceId: String,
        remarks: String
    ): NetworkResult<InvoiceUploadResponseDto, ApiError> {
        return safeRequest<InvoiceUploadResponseDto>(
            block = {
                client.post(NetworkConfig.v1("invoices/$invoiceId/approve")) {
                    contentType(ContentType.Application.Json)
                    setBody(InvoiceActionRequestDto(remarks = remarks))
                }
            }
        )
    }

    /**
     * POST /invoices/{id}/reject
     */
    suspend fun rejectInvoice(
        invoiceId: String,
        remarks: String
    ): NetworkResult<InvoiceUploadResponseDto, ApiError> {
        return safeRequest<InvoiceUploadResponseDto>(
            block = {
                client.post(NetworkConfig.v1("invoices/$invoiceId/reject")) {
                    contentType(ContentType.Application.Json)
                    setBody(InvoiceActionRequestDto(remarks = remarks))
                }
            }
        )
    }

    /**
     * POST /ocr/extract-invoice — multipart upload; blocks until extraction is complete.
     * Returns [OcrExtractResponse] with full extracted data. The form field stays
     * named `image` to preserve the existing backend contract; MIME + filename
     * come from [file] so both JPEG and PDF inputs go through the same endpoint.
     */
    suspend fun extractInvoice(
        file: InvoiceFile
    ): NetworkResult<OcrExtractResponse, ApiError> {
        return safeRequest<OcrExtractResponse>(
            block = {
                client.submitFormWithBinaryData(
                    url = NetworkConfig.v1("ocr/extract-invoice"),
                    formData = formData {
                        append(
                            "image",
                            file.bytes,
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=${file.filename}")
                                append(HttpHeaders.ContentType, file.mimeType)
                            }
                        )
                    }
                )
            }
        )
    }
}

@Serializable
data class InvoiceActionRequestDto(
    val remarks: String
)

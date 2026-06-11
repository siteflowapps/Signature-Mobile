package com.siteflow.cdo.support.data

import com.siteflow.cdo.core.data.networking.client.NetworkConfig
import com.siteflow.cdo.core.data.networking.error.ApiError
import com.siteflow.cdo.core.data.networking.request.safeRequest
import com.siteflow.cdo.core.data.networking.result.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val supportJson = Json { ignoreUnknownKeys = true; explicitNulls = false }

class SupportApi(private val client: HttpClient) {

    suspend fun getMyTickets(
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<TicketListResponse, ApiError> {
        return safeRequest<TicketListResponse>(
            showLoader = false,
            block = {
                client.get(NetworkConfig.v1("support-tickets/me")) {
                    parameter("page", page)
                    parameter("size", size)
                }
            }
        )
    }

    suspend fun createTicket(
        data: SupportTicketData,
        screenshotBytes: List<ByteArray> = emptyList()
    ): NetworkResult<SupportTicketResponse, ApiError> {
        return safeRequest<SupportTicketResponse>(
            showLoader = true,
            block = {
                client.submitFormWithBinaryData(
                    url = NetworkConfig.v1("support-tickets"),
                    formData = formData {
                        append(
                            "data",
                            supportJson.encodeToString(data),
                            Headers.build {
                                append(HttpHeaders.ContentType, "application/json")
                            }
                        )
                        screenshotBytes.forEachIndexed { index, bytes ->
                            append(
                                "screenshots",
                                bytes,
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=screenshot_$index.jpg")
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                }
                            )
                        }
                    }
                )
            }
        )
    }
}

package com.siteflow.signature.cso.profile.data

import com.siteflow.signature.core.data.networking.client.NetworkConfig
import com.siteflow.signature.core.data.networking.error.ApiError
import com.siteflow.signature.core.data.networking.request.safeRequest
import com.siteflow.signature.core.data.networking.result.NetworkResult
import com.siteflow.signature.cso.profile.data.dto.TeamResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import com.siteflow.signature.cso.profile.data.dto.UserMeResponseDto

class UserApi(
    private val client: HttpClient
) {
    suspend fun getMyAses(
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<TeamResponseDto, ApiError> {
        return safeRequest<TeamResponseDto>(
            block = {
                client.get(NetworkConfig.v1("users/my-ases")) {
                    contentType(ContentType.Application.Json)
                    parameter("page", page)
                    parameter("size", size)
                }
            }
        )
    }

    suspend fun getMe(): NetworkResult<UserMeResponseDto, ApiError> {
        return safeRequest<UserMeResponseDto>(
            block = {
                client.get(NetworkConfig.v1("users/me")) {
                    contentType(ContentType.Application.Json)
                }
            }
        )
    }

    /**
     * POST /outlets/{outletId}/deactivate
     * Soft-deactivates the retailer's own outlet (used as "Delete Account" on Profile screen).
     */
    suspend fun deactivateOutlet(outletId: String): NetworkResult<Unit, ApiError> {
        return safeRequest<Unit>(
            block = {
                client.post(NetworkConfig.v1("outlets/$outletId/deactivate")) {
                    contentType(ContentType.Application.Json)
                }
            }
        )
    }
}

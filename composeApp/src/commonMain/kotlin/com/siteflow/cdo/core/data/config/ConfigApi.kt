package com.siteflow.cdo.core.data.config

import com.siteflow.cdo.core.data.networking.client.NetworkConfig
import com.siteflow.cdo.core.data.networking.error.ApiError
import com.siteflow.cdo.core.data.networking.request.safeRequest
import com.siteflow.cdo.core.data.networking.result.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ConfigApi(
    private val client: HttpClient
) {

    /**
     * GET /config
     * Headers: app-version, platform
     */
    suspend fun getConfig(
        appVersion: String = "1.0.0",
        platform: String = "android"
    ): NetworkResult<ConfigResponseDto, ApiError> {
        return safeRequest<ConfigResponseDto>(
            showLoader = false,
            block = {
                client.get(NetworkConfig.v1("config")) {
                    header("app-version", appVersion)
                    header("platform", platform)
                }
            }
        )
    }

    /**
     * PUT /config/user-flags
     * Body: { "flagKey": "...", "flagValue": "..." }
     */
    suspend fun updateUserFlag(
        flagKey: String,
        flagValue: String
    ): NetworkResult<UpdateUserFlagResponseDto, ApiError> {
        return safeRequest<UpdateUserFlagResponseDto>(
            showLoader = false,
            block = {
                client.put(NetworkConfig.v1("config/user-flags")) {
                    contentType(ContentType.Application.Json)
                    setBody(UpdateUserFlagRequest(flagKey = flagKey, flagValue = flagValue))
                }
            }
        )
    }
}

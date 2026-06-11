package com.siteflow.retailsync.login.data

import com.siteflow.retailsync.core.data.networking.client.NetworkConfig
import com.siteflow.retailsync.core.data.networking.error.ApiError
import com.siteflow.retailsync.core.data.networking.request.safeRequest
import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.login.data.dto.OtpRequestDto
import com.siteflow.retailsync.login.data.dto.OtpResponseDto
import com.siteflow.retailsync.login.data.dto.OtpVerifyRequestDto
import com.siteflow.retailsync.login.data.dto.OtpVerifyResponseDto
import com.siteflow.retailsync.login.data.dto.UserMeResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class LoginApi(
    private val client: HttpClient
) {

    suspend fun requestOtp(
        request: OtpRequestDto,
        showLoader: Boolean = true
    ): NetworkResult<OtpResponseDto, ApiError> {
        return safeRequest<OtpResponseDto>(
            showLoader = showLoader,
            block = {
                client.post("${NetworkConfig.BASE_URL}/auth/login/otp/request") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }
        )
    }

    suspend fun verifyOtp(
        request: OtpVerifyRequestDto,
        showLoader: Boolean = true
    ): NetworkResult<OtpVerifyResponseDto, ApiError> {
        return safeRequest<OtpVerifyResponseDto>(
            showLoader = showLoader,
            block = {
                client.post("${NetworkConfig.BASE_URL}/auth/login/otp/verify") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }
        )
    }

    suspend fun fetchMe(
        showLoader: Boolean = false
    ): NetworkResult<UserMeResponseDto, ApiError> {
        return safeRequest<UserMeResponseDto>(
            showLoader = showLoader,
            block = {
                client.get("${NetworkConfig.BASE_URL}/users/me")
            }
        )
    }
}

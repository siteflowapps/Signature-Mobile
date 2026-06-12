package com.siteflow.signature.login.data

import com.siteflow.signature.core.data.networking.client.NetworkConfig
import com.siteflow.signature.core.data.networking.error.ApiError
import com.siteflow.signature.core.data.networking.request.safeRequest
import com.siteflow.signature.core.data.networking.result.NetworkResult
import com.siteflow.signature.login.data.dto.OtpRequestDto
import com.siteflow.signature.login.data.dto.OtpResponseDto
import com.siteflow.signature.login.data.dto.OtpVerifyRequestDto
import com.siteflow.signature.login.data.dto.OtpVerifyResponseDto
import com.siteflow.signature.login.data.dto.UserMeResponseDto
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
                client.post(NetworkConfig.v1("auth/login/otp/request")) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }
        )
    }

    suspend fun retryOtp(
        request: OtpRequestDto,
        showLoader: Boolean = false
    ): NetworkResult<OtpResponseDto, ApiError> {
        return safeRequest<OtpResponseDto>(
            showLoader = showLoader,
            block = {
                client.post(NetworkConfig.v1("auth/login/otp/retry")) {
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
                client.post(NetworkConfig.v1("auth/login/otp/verify")) {
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
                client.get(NetworkConfig.v1("users/me"))
            }
        )
    }
}

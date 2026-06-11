package com.siteflow.retailsync.login.data

import com.siteflow.retailsync.core.data.networking.error.ApiError
import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.core.data.networking.result.map
import com.siteflow.retailsync.login.data.dto.OtpRequestDto
import com.siteflow.retailsync.login.data.dto.OtpVerifyRequestDto
import com.siteflow.retailsync.login.data.dto.TokenDataDto
import com.siteflow.retailsync.login.data.dto.UserMeDataDto

class LoginRepository(
    private val api: LoginApi
) {

    suspend fun requestOtp(
        phone: String,
        showLoader: Boolean = true
    ): NetworkResult<String, ApiError> {
        return api.requestOtp(
            OtpRequestDto(phone = phone),
            showLoader = showLoader
        ).map { response ->
            response.data ?: "OTP sent"
        }
    }

    suspend fun verifyOtp(
        phone: String,
        otp: String,
        showLoader: Boolean = true
    ): NetworkResult<TokenDataDto, ApiError> {
        return api.verifyOtp(
            OtpVerifyRequestDto(phone = phone, otp = otp),
            showLoader = showLoader
        ).map { response ->
            response.data
                ?: throw IllegalStateException("OTP verified but token data is missing")
        }
    }

    suspend fun fetchMe(): NetworkResult<UserMeDataDto, ApiError> {
        return api.fetchMe().map { response ->
            response.data
                ?: throw IllegalStateException("User profile data is missing")
        }
    }
}

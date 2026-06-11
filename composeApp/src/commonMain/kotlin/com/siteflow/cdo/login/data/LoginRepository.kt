package com.siteflow.cdo.login.data

import com.siteflow.cdo.core.data.networking.error.ApiError
import com.siteflow.cdo.core.data.networking.result.NetworkResult
import com.siteflow.cdo.core.data.networking.result.map
import com.siteflow.cdo.login.data.dto.OtpRequestDto
import com.siteflow.cdo.login.data.dto.OtpVerifyRequestDto
import com.siteflow.cdo.login.data.dto.TokenDataDto
import com.siteflow.cdo.login.data.dto.UserMeDataDto

class LoginRepository(
    private val api: LoginApi
) {

    /**
     * Request OTP for the given phone number.
     * Returns the success message string (e.g., "OTP sent successfully").
     */
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

    suspend fun retryOtp(phone: String): NetworkResult<String, ApiError> {
        return api.retryOtp(OtpRequestDto(phone = phone)).map { response ->
            response.data ?: "OTP resent"
        }
    }

    /**
     * Verify OTP and get tokens + role.
     * Returns [TokenDataDto] on success.
     */
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

    /**
     * Fetch current user profile from GET /users/me.
     * Returns [UserMeDataDto] on success.
     */
    suspend fun fetchMe(): NetworkResult<UserMeDataDto, ApiError> {
        return api.fetchMe().map { response ->
            response.data
                ?: throw IllegalStateException("User profile data is missing")
        }
    }
}

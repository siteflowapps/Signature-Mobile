package com.siteflow.signature.core.data.networking.response


import com.siteflow.signature.core.data.auth.TokenRefreshManager
import com.siteflow.signature.core.data.networking.error.ApiError
import com.siteflow.signature.core.data.networking.result.NetworkResult
import com.siteflow.signature.core.data.networking.error.parseApiError
import com.siteflow.signature.core.domain.SessionManager
import com.siteflow.signature.core.logger.ReceeLogger
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

const val TAG = "NetworkResponse"

val json = Json { ignoreUnknownKeys = true }

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): NetworkResult<T, ApiError> {

    val status = response.status.value

    val rawBody = try {
        response.bodyAsText()
    } catch (e: Exception) {
        ReceeLogger.e(TAG, "Failed to read body", e)
        ""
    }

    ReceeLogger.d(TAG, "status=$status body=$rawBody")

    return if (status in 200..299) {
        try {
            // Unit responses (e.g. DELETE 204 No Content) don't have a body to decode
            @Suppress("UNCHECKED_CAST")
            if (T::class == Unit::class) {
                NetworkResult.Success(Unit as T)
            } else {
                val success = json.decodeFromString<T>(rawBody)
                NetworkResult.Success(success)
            }
        } catch (e: Exception) {
            ReceeLogger.e(TAG, "Failed to decode success body", e)
            NetworkResult.Error(
                ApiError(code = -2, message = "Decoding error")
            )
        }
    } else {
        // 🔐 Global 401 handler — attempt silent token refresh first
        if (status == 401) {
            val refreshManager = TokenRefreshManager.instance
            if (refreshManager != null) {
                val refreshed = refreshManager.tryRefresh()
                if (refreshed) {
                    // Token refreshed — tell the caller to retry
                    ReceeLogger.d(TAG, "Token refreshed, returning retryable error")
                    return NetworkResult.Error(
                        ApiError(code = 401, message = "__TOKEN_REFRESHED__")
                    )
                }
            }
            // Refresh failed or not available — force logout
            SessionManager.notifySessionExpired()
        }
        
        NetworkResult.Error(parseApiError(rawBody, status))
    }
}

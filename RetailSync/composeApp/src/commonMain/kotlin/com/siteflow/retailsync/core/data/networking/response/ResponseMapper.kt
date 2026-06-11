package com.siteflow.retailsync.core.data.networking.response

import com.siteflow.retailsync.core.data.networking.error.ApiError
import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.core.data.networking.error.parseApiError
import com.siteflow.retailsync.core.domain.SessionManager
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

suspend inline fun <reified T> responseToResult(
    response: HttpResponse
): NetworkResult<T, ApiError> {

    val status = response.status.value

    val rawBody = try {
        response.bodyAsText()
    } catch (e: Exception) {
        ""
    }

    return if (status in 200..299) {
        try {
            val success = json.decodeFromString<T>(rawBody)
            NetworkResult.Success(success)
        } catch (e: Exception) {
            NetworkResult.Error(
                ApiError(code = -2, message = "Decoding error")
            )
        }
    } else {
        // 🔐 Global 401 handler - Trigger session expiry
        if (status == 401) {
            SessionManager.notifySessionExpired()
        }

        NetworkResult.Error(parseApiError(rawBody, status))
    }
}

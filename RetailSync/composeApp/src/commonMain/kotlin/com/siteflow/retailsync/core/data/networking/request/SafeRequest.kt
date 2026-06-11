package com.siteflow.retailsync.core.data.networking.request

import com.siteflow.retailsync.core.data.networking.error.ApiError
import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.delay

suspend inline fun <reified T> safeRequest(
    crossinline block: suspend () -> HttpResponse,
    retryCount: Int = 0,
    showLoader: Boolean = true
): NetworkResult<T, ApiError> {
    var attempt = 0
    var lastError: NetworkResult.Error<ApiError>? = null
    while (attempt <= retryCount) {
        val result = safeCall<T>(showLoader = showLoader) { block() }
        when (result) {
            is NetworkResult.Success -> return result
            is NetworkResult.Error -> {
                lastError = result
                val shouldRetry = when (result.error.code) {
                    -100 -> true // network issues
                    429 -> true  // too many requests
                    else -> false
                }
                if (!shouldRetry) return result
            }
        }
        attempt++
        if (attempt <= retryCount) {
            val backoff = 500L * (1 shl (attempt - 1))
            delay(backoff)
        }
    }
    return lastError ?: NetworkResult.Error(ApiError(-1, "Unknown error"))
}

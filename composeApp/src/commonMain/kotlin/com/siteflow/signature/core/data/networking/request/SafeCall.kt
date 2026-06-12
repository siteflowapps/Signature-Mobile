package com.siteflow.signature.core.data.networking.request

import com.siteflow.signature.core.data.networking.error.ApiError
import com.siteflow.signature.core.data.networking.response.responseToResult
import com.siteflow.signature.core.data.networking.result.NetworkResult
import com.siteflow.signature.core.logger.ReceeLogger
import com.siteflow.signature.core.presentation.components.loader.GlobalLoading
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlin.coroutines.coroutineContext

const val TAG = "NetworkDebug"

@Suppress("DEPRECATION")
suspend inline fun <reified T> safeCall(
    showLoader: Boolean = true,
    crossinline execute: suspend () -> HttpResponse
): NetworkResult<T, ApiError> {

    if (showLoader) GlobalLoading.show()
    ReceeLogger.i(TAG, "safeCall: starting execute()")

    return try {

        val response: HttpResponse = try {
            val r = execute()
            ReceeLogger.i(TAG, "safeCall: execute() returned status=${r.status.value}")
            r

        } catch (e: UnresolvedAddressException) {
            ReceeLogger.e(TAG, "No internet (unresolved address)", e)
            return NetworkResult.Error(ApiError(code = -100, message = "No internet"))

        } catch (e: IOException) {
            ReceeLogger.e(TAG, "Network IO error", e)
            return NetworkResult.Error(ApiError(code = -100, message = "Network error"))

        } catch (e: SerializationException) {
            ReceeLogger.e(TAG, "Serialization error", e)
            return NetworkResult.Error(ApiError(code = -1, message = "Serialization error"))

        } catch (e: CancellationException) {
            ReceeLogger.d(TAG, "Request cancelled")
            throw e // MUST rethrow

        } catch (e: Exception) {
            ReceeLogger.e(TAG, "Unexpected exception executing request", e)
            coroutineContext.ensureActive()
            return NetworkResult.Error(
                ApiError(code = -1, message = e.message ?: "Unknown error")
            )
        }

        responseToResult<T>(response)

    } catch (e: Exception) {
        ReceeLogger.e(TAG, "Exception in responseToResult", e)
        NetworkResult.Error(ApiError(code = -1, message = "Mapping error"))

    } finally {
        if (showLoader) GlobalLoading.hide()
    }
}


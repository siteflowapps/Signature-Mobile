package com.siteflow.retailsync.core.data.networking.request

import com.siteflow.retailsync.core.data.networking.error.ApiError
import com.siteflow.retailsync.core.data.networking.response.responseToResult
import com.siteflow.retailsync.core.data.networking.result.NetworkResult
import com.siteflow.retailsync.core.presentation.components.loader.GlobalLoading
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlin.coroutines.coroutineContext

@Suppress("DEPRECATION")
suspend inline fun <reified T> safeCall(
    showLoader: Boolean = true,
    crossinline execute: suspend () -> HttpResponse
): NetworkResult<T, ApiError> {

    if (showLoader) GlobalLoading.show()

    return try {

        val response: HttpResponse = try {
            execute()

        } catch (e: UnresolvedAddressException) {
            return NetworkResult.Error(ApiError(code = -100, message = "No internet"))

        } catch (e: IOException) {
            return NetworkResult.Error(ApiError(code = -100, message = "Network error"))

        } catch (e: SerializationException) {
            return NetworkResult.Error(ApiError(code = -1, message = "Serialization error"))

        } catch (e: CancellationException) {
            throw e // MUST rethrow

        } catch (e: Exception) {
            coroutineContext.ensureActive()
            return NetworkResult.Error(
                ApiError(code = -1, message = e.message ?: "Unknown error")
            )
        }

        responseToResult<T>(response)

    } catch (e: Exception) {
        NetworkResult.Error(ApiError(code = -1, message = "Mapping error"))

    } finally {
        if (showLoader) GlobalLoading.hide()
    }
}

package com.siteflow.signature.core.data.networking.result

import com.siteflow.signature.core.data.networking.error.ApiError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NetworkResultTest {

    // ─── map ───────────────────────────────────────────────────────

    @Test
    fun `map transforms Success data`() {
        val result: NetworkResult<Int, ApiError> = NetworkResult.Success(42)
        val mapped = result.map { it.toString() }
        assertIs<NetworkResult.Success<String>>(mapped)
        assertEquals("42", mapped.data)
    }

    @Test
    fun `map preserves Error`() {
        val error = ApiError(400, "Bad request")
        val result: NetworkResult<Int, ApiError> = NetworkResult.Error(error)
        val mapped = result.map { it.toString() }
        assertIs<NetworkResult.Error<ApiError>>(mapped)
        assertEquals(error, mapped.error)
    }

    // ─── onSuccess ─────────────────────────────────────────────────

    @Test
    fun `onSuccess executes action for Success`() {
        var captured = ""
        val result: NetworkResult<String, ApiError> = NetworkResult.Success("hello")
        result.onSuccess { captured = it }
        assertEquals("hello", captured)
    }

    @Test
    fun `onSuccess does not execute action for Error`() {
        var executed = false
        val result: NetworkResult<String, ApiError> = NetworkResult.Error(ApiError(400, "err"))
        result.onSuccess { executed = true }
        assertTrue(!executed)
    }

    // ─── onError ───────────────────────────────────────────────────

    @Test
    fun `onError executes action for Error`() {
        var captured: ApiError? = null
        val error = ApiError(500, "server error")
        val result: NetworkResult<String, ApiError> = NetworkResult.Error(error)
        result.onError { captured = it }
        assertEquals(error, captured)
    }

    @Test
    fun `onError does not execute action for Success`() {
        var executed = false
        val result: NetworkResult<String, ApiError> = NetworkResult.Success("ok")
        result.onError { executed = true }
        assertTrue(!executed)
    }

    // ─── chaining ──────────────────────────────────────────────────

    @Test
    fun `chaining onSuccess and onError on Success`() {
        var successCaptured = ""
        var errorExecuted = false

        val result: NetworkResult<String, ApiError> = NetworkResult.Success("data")
        result
            .onSuccess { successCaptured = it }
            .onError { errorExecuted = true }

        assertEquals("data", successCaptured)
        assertTrue(!errorExecuted)
    }

    @Test
    fun `chaining onSuccess and onError on Error`() {
        var successExecuted = false
        var errorCaptured: ApiError? = null
        val error = ApiError(404, "not found")

        val result: NetworkResult<String, ApiError> = NetworkResult.Error(error)
        result
            .onSuccess { successExecuted = true }
            .onError { errorCaptured = it }

        assertTrue(!successExecuted)
        assertEquals(error, errorCaptured)
    }
}

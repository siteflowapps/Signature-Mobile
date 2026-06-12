package com.siteflow.signature.core.data.networking.error

import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorParserTest {

    // ─── Signature Backend Format ────────────────────────────────────────

    @Test
    fun `parse Signature backend error - NOT_FOUND`() {
        val body = """{"success":false,"errorCode":"NOT_FOUND","error":"User not found with id: 999999992","timestamp":"2026-03-03T11:06:18.546Z"}"""
        val error = parseApiError(body, 404)
        assertEquals(404, error.code)
        assertEquals("User not found with id: 999999992", error.message)
    }

    @Test
    fun `parse Signature backend error - AUTH_INVALID_CREDENTIALS`() {
        val body = """{"success":false,"errorCode":"AUTH_INVALID_CREDENTIALS","error":"Invalid username or password","timestamp":"2026-03-03T11:21:33.507Z"}"""
        val error = parseApiError(body, 401)
        assertEquals(401, error.code)
        assertEquals("Invalid username or password", error.message)
    }

    @Test
    fun `parse Signature backend error - missing error message uses errorCode`() {
        val body = """{"success":false,"errorCode":"INTERNAL_ERROR","timestamp":"2026-03-03T11:06:18.546Z"}"""
        val error = parseApiError(body, 500)
        assertEquals(500, error.code)
        assertEquals("INTERNAL_ERROR", error.message)
    }

    @Test
    fun `parse Signature backend error - missing both error and errorCode`() {
        val body = """{"success":false,"timestamp":"2026-03-03T11:06:18.546Z"}"""
        val error = parseApiError(body, 400)
        assertEquals(400, error.code)
        assertEquals("Request failed (400)", error.message)
    }

    @Test
    fun `parse Signature backend success should NOT be treated as error`() {
        // success=true should fall through to normal parsing
        val body = """{"success":true,"data":"OTP sent successfully","timestamp":"2026-03-03T09:08:28.912Z"}"""
        val error = parseApiError(body, 400)
        // Since success=true, it skips Signature handler and falls through
        assertEquals(400, error.code)
    }

    // ─── Standard Error Formats ────────────────────────────────────

    @Test
    fun `parse plain string error`() {
        val body = "\"Something went wrong\""
        val error = parseApiError(body, 500)
        assertEquals(500, error.code)
        assertEquals("Something went wrong", error.message)
    }

    @Test
    fun `parse error object with message field`() {
        val body = """{"message":"Invalid request"}"""
        val error = parseApiError(body, 400)
        assertEquals(400, error.code)
        assertEquals("Invalid request", error.message)
    }

    @Test
    fun `parse error object with error string`() {
        val body = """{"error":"Unauthorized"}"""
        val error = parseApiError(body, 401)
        assertEquals(401, error.code)
        assertEquals("Unauthorized", error.message)
    }

    @Test
    fun `parse nested error object`() {
        val body = """{"error":{"code":422,"message":"Validation failed"}}"""
        val error = parseApiError(body, 422)
        assertEquals(422, error.code)
        assertEquals("Validation failed", error.message)
    }

    @Test
    fun `parse RFC 7807 problem+json`() {
        val body = """{"title":"Not Found","detail":"Resource not found","status":404}"""
        val error = parseApiError(body, 404)
        assertEquals(404, error.code)
        assertEquals("Resource not found", error.message)
    }

    @Test
    fun `parse empty body returns fallback`() {
        val error = parseApiError("", 500)
        assertEquals(500, error.code)
        assertEquals("Request failed (500)", error.message)
    }

    @Test
    fun `parse blank body returns fallback`() {
        val error = parseApiError("   ", 503)
        assertEquals(503, error.code)
        assertEquals("Request failed (503)", error.message)
    }

    @Test
    fun `parse malformed JSON returns fallback`() {
        val error = parseApiError("not json at all", 500)
        assertEquals(500, error.code)
        assertEquals("Request failed (500)", error.message)
    }

    // ─── ErrorDto Extensions ───────────────────────────────────────

    @Test
    fun `ErrorDto toApiError with all fields`() {
        val dto = ErrorDto(code = 400, message = "Bad request")
        val error = dto.toApiError()
        assertEquals(400, error.code)
        assertEquals("Bad request", error.message)
    }

    @Test
    fun `ErrorDto toApiError with null code uses fallback`() {
        val dto = ErrorDto(code = null, message = "Error")
        val error = dto.toApiError(fallbackCode = 500)
        assertEquals(500, error.code)
        assertEquals("Error", error.message)
    }

    @Test
    fun `ErrorDto toApiError with null message uses Unknown error`() {
        val dto = ErrorDto(code = 400, message = null)
        val error = dto.toApiError()
        assertEquals(400, error.code)
        assertEquals("Unknown error", error.message)
    }

    @Test
    fun `ErrorDto toApiError with blank message uses Unknown error`() {
        val dto = ErrorDto(code = 400, message = "   ")
        val error = dto.toApiError()
        assertEquals(400, error.code)
        assertEquals("Unknown error", error.message)
    }
}

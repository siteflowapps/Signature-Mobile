package com.siteflow.signature.login.data.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class OtpDtoSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ─── OtpRequestDto ──────────────────────────────────────────

    @Test
    fun `OtpRequestDto serializes correctly`() {
        val dto = OtpRequestDto(phone = "9999999992")
        val serialized = json.encodeToString(OtpRequestDto.serializer(), dto)
        assertTrue(serialized.contains("\"phone\":\"9999999992\""))
    }

    // ─── OtpResponseDto ─────────────────────────────────────────

    @Test
    fun `OtpResponseDto deserializes success response`() {
        val body = """{"success":true,"data":"OTP sent successfully","timestamp":"2026-03-03T09:08:28.912Z"}"""
        val dto = json.decodeFromString(OtpResponseDto.serializer(), body)
        assertTrue(dto.success)
        assertEquals("OTP sent successfully", dto.data)
        assertNull(dto.errorCode)
        assertNull(dto.error)
        assertNotNull(dto.timestamp)
    }

    @Test
    fun `OtpResponseDto deserializes error response`() {
        val body = """{"success":false,"errorCode":"NOT_FOUND","error":"User not found with id: 999999992","timestamp":"2026-03-03T11:06:18.546Z"}"""
        val dto = json.decodeFromString(OtpResponseDto.serializer(), body)
        assertFalse(dto.success)
        assertNull(dto.data)
        assertEquals("NOT_FOUND", dto.errorCode)
        assertEquals("User not found with id: 999999992", dto.error)
    }

    // ─── OtpVerifyRequestDto ────────────────────────────────────

    @Test
    fun `OtpVerifyRequestDto serializes correctly`() {
        val dto = OtpVerifyRequestDto(phone = "9999999994", otp = "123456")
        val serialized = json.encodeToString(OtpVerifyRequestDto.serializer(), dto)
        assertTrue(serialized.contains("\"phone\":\"9999999994\""))
        assertTrue(serialized.contains("\"otp\":\"123456\""))
    }

    // ─── OtpVerifyResponseDto ───────────────────────────────────

    @Test
    fun `OtpVerifyResponseDto deserializes success with tokens`() {
        val body = """{
            "success": true,
            "data": {
                "accessToken": "eyJhbGciOiJIUzI1NiJ9.test.sig",
                "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh.sig",
                "role": "ASM",
                "userId": "28f18b2f-099e-4a7a-90af-3a943b417efd"
            },
            "timestamp": "2026-03-03T11:19:12.494Z"
        }"""
        val dto = json.decodeFromString(OtpVerifyResponseDto.serializer(), body)
        assertTrue(dto.success)
        assertNotNull(dto.data)
        assertEquals("eyJhbGciOiJIUzI1NiJ9.test.sig", dto.data!!.accessToken)
        assertEquals("eyJhbGciOiJIUzI1NiJ9.refresh.sig", dto.data!!.refreshToken)
        assertEquals("ASM", dto.data!!.role)
        assertEquals("28f18b2f-099e-4a7a-90af-3a943b417efd", dto.data!!.userId)
    }

    @Test
    fun `OtpVerifyResponseDto deserializes error response`() {
        val body = """{
            "success": false,
            "errorCode": "AUTH_INVALID_CREDENTIALS",
            "error": "Invalid username or password",
            "timestamp": "2026-03-03T11:21:33.507Z"
        }"""
        val dto = json.decodeFromString(OtpVerifyResponseDto.serializer(), body)
        assertFalse(dto.success)
        assertNull(dto.data)
        assertEquals("AUTH_INVALID_CREDENTIALS", dto.errorCode)
        assertEquals("Invalid username or password", dto.error)
    }

    @Test
    fun `OtpVerifyResponseDto handles unknown fields gracefully`() {
        val body = """{
            "success": true,
            "data": {
                "accessToken": "tok",
                "refreshToken": "ref",
                "role": "ASE",
                "userId": "uid",
                "extraField": "ignored"
            },
            "timestamp": "2026-03-03T11:19:12.494Z",
            "anotherExtra": 123
        }"""
        val dto = json.decodeFromString(OtpVerifyResponseDto.serializer(), body)
        assertTrue(dto.success)
        assertEquals("ASE", dto.data!!.role)
    }

    // ─── TokenDataDto ───────────────────────────────────────────

    @Test
    fun `TokenDataDto deserializes all fields`() {
        val body = """{
            "accessToken": "access123",
            "refreshToken": "refresh456",
            "role": "OUTLET",
            "userId": "user-abc"
        }"""
        val dto = json.decodeFromString(TokenDataDto.serializer(), body)
        assertEquals("access123", dto.accessToken)
        assertEquals("refresh456", dto.refreshToken)
        assertEquals("OUTLET", dto.role)
        assertEquals("user-abc", dto.userId)
    }
}

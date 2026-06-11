package com.siteflow.cdo.core.data.networking.util

import com.siteflow.cdo.core.domain.UserRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JwtUtilsTest {

    // Real JWT from the backend sample (ASM role)
    private val sampleToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyOGYxOGIyZi0wOTllLTRhN2EtOTBhZi0zYTk0M2I0MTdlZmQiLCJwaG9uZSI6Ijk5OTk5OTk5OTQiLCJyb2xlIjoiQVNNIiwiYnVzaW5lc3NJZCI6ImMwMDAwMDAwLTAwMDAtMDAwMC0wMDAwLTAwMDAwMDAwMDAwMSIsImxvY2F0aW9uSWQiOiJiMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDEiLCJpYXQiOjE3NzI1MzY3NTIsImV4cCI6MTc3MjU3Mjc1Mn0.tdeCwO0MveqA0HJDJOaMFMCnOAEJEalXgDOl41i_6SY"

    // ─── extractRole ───────────────────────────────────────────────

    @Test
    fun `extractRole returns ASM for ASM token`() {
        val role = JwtUtils.extractRole(sampleToken)
        assertNotNull(role)
        assertEquals(UserRole.ASM, role)
    }

    @Test
    fun `extractRole returns null for invalid token`() {
        val role = JwtUtils.extractRole("not.a.jwt")
        assertNull(role)
    }

    @Test
    fun `extractRole returns null for empty token`() {
        val role = JwtUtils.extractRole("")
        assertNull(role)
    }

    @Test
    fun `extractRole returns null for token without role claim`() {
        // JWT with payload: {"sub":"test","iat":1234}
        // Base64url encoded: eyJzdWIiOiJ0ZXN0IiwiaWF0IjoxMjM0fQ
        val noRoleToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0IiwiaWF0IjoxMjM0fQ.signature"
        val role = JwtUtils.extractRole(noRoleToken)
        assertNull(role)
    }

    @Test
    fun `extractRole returns null for unknown role value`() {
        // JWT with payload: {"role":"SUPER_ADMIN"}
        // Base64url encoded payload: eyJyb2xlIjoiU1VQRVJfQURNSU4ifQ
        val unknownRoleToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiU1VQRVJfQURNSU4ifQ.signature"
        val role = JwtUtils.extractRole(unknownRoleToken)
        assertNull(role)
    }

    @Test
    fun `extractRole returns OUTLET for RETAILER alias`() {
        // JWT with payload: {"role":"RETAILER"}
        // Base64url encoded payload: eyJyb2xlIjoiUkVUQUlMRVIifQ
        val retailerToken = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUkVUQUlMRVIifQ.signature"
        val role = JwtUtils.extractRole(retailerToken)
        assertNotNull(role)
        assertEquals(UserRole.OUTLET, role)
    }

    // ─── extractUserId ─────────────────────────────────────────────

    @Test
    fun `extractUserId returns correct UUID`() {
        val userId = JwtUtils.extractUserId(sampleToken)
        assertEquals("28f18b2f-099e-4a7a-90af-3a943b417efd", userId)
    }

    @Test
    fun `extractUserId returns null for invalid token`() {
        assertNull(JwtUtils.extractUserId("invalid"))
    }

    // ─── extractBusinessId ─────────────────────────────────────────

    @Test
    fun `extractBusinessId returns correct UUID`() {
        val businessId = JwtUtils.extractBusinessId(sampleToken)
        assertEquals("c0000000-0000-0000-0000-000000000001", businessId)
    }

    @Test
    fun `extractBusinessId returns null for invalid token`() {
        assertNull(JwtUtils.extractBusinessId("invalid"))
    }

    // ─── extractLocationId ─────────────────────────────────────────

    @Test
    fun `extractLocationId returns correct UUID`() {
        val locationId = JwtUtils.extractLocationId(sampleToken)
        assertEquals("b0000000-0000-0000-0000-000000000001", locationId)
    }

    @Test
    fun `extractLocationId returns null for invalid token`() {
        assertNull(JwtUtils.extractLocationId("invalid"))
    }
}

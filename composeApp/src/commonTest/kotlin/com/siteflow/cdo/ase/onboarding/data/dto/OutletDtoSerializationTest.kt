package com.siteflow.cdo.ase.onboarding.data.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OutletDtoSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ── Location DTOs ──

    @Test
    fun `LocationSearchResponseDto - success with results`() {
        val raw = """
        {
            "success": true,
            "data": [
                {
                    "id": "b0000000-0000-0000-0000-000000000002",
                    "pincode": "400001",
                    "city": "Mumbai",
                    "state": "Maharashtra"
                }
            ],
            "timestamp": "2026-03-03T15:07:14.620272905Z"
        }
        """.trimIndent()

        val result = json.decodeFromString<LocationSearchResponseDto>(raw)
        assertTrue(result.success)
        assertEquals(1, result.data.size)
        assertEquals("b0000000-0000-0000-0000-000000000002", result.data[0].id)
        assertEquals("400001", result.data[0].pincode)
        assertEquals("Mumbai", result.data[0].city)
        assertEquals("Maharashtra", result.data[0].state)
    }

    @Test
    fun `LocationSearchResponseDto - success with empty data for invalid pincode`() {
        val raw = """
        {
            "success": true,
            "data": [],
            "timestamp": "2026-03-03T15:08:30.002997177Z"
        }
        """.trimIndent()

        val result = json.decodeFromString<LocationSearchResponseDto>(raw)
        assertTrue(result.success)
        assertTrue(result.data.isEmpty())
    }

    // ── Create Outlet DTOs ──

    @Test
    fun `CreateOutletRequestDto - serialization`() {
        val request = CreateOutletRequestDto(
            name = "Sharma General Point",
            phone = "9123456789",
            ownerName = "Rajesh Sharma",
            ownerMobile = "9123456789",
            ownerWhatsapp = "9123456789",
            email = "rajesh.sharma@gmail.com",
            outletType = "GROCERY",
            address = "Shop No. 5, Sector 17, Vashi",
            landmark = "Near Vashi Railway Station",
            locality = "Vashi",
            latitude = 19.0771,
            longitude = 72.9986,
            locationId = "b0000000-0000-0000-0000-000000000001",
            businessId = "c0000000-0000-0000-0000-000000000001"
        )

        val serialized = json.encodeToString(CreateOutletRequestDto.serializer(), request)
        val decoded = json.decodeFromString<CreateOutletRequestDto>(serialized)

        assertEquals(request.name, decoded.name)
        assertEquals(request.phone, decoded.phone)
        assertEquals(request.email, decoded.email)
        assertEquals(request.outletType, decoded.outletType)
        assertEquals(request.latitude, decoded.latitude)
        assertEquals(request.longitude, decoded.longitude)
        assertEquals(request.locationId, decoded.locationId)
        assertEquals(request.businessId, decoded.businessId)
    }

    @Test
    fun `CreateOutletRequestDto - optional fields can be null`() {
        val request = CreateOutletRequestDto(
            name = "Test Store",
            phone = "9999999999",
            ownerName = "Test",
            ownerMobile = "9999999999",
            ownerWhatsapp = "9999999999",
            email = null,
            outletType = "GROCERY",
            address = "Test Address",
            landmark = null,
            locality = null,
            latitude = 0.0,
            longitude = 0.0,
            locationId = "loc-id",
            businessId = "biz-id"
        )

        val serialized = json.encodeToString(CreateOutletRequestDto.serializer(), request)
        val decoded = json.decodeFromString<CreateOutletRequestDto>(serialized)

        assertNull(decoded.email)
        assertNull(decoded.landmark)
        assertNull(decoded.locality)
    }

    @Test
    fun `CreateOutletResponseDto - success response`() {
        val raw = """
        {
            "success": true,
            "data": {
                "id": "a757535f-d461-4858-ab66-a252e89a6e2b",
                "name": "Sharma General Point",
                "phone": "9123456789",
                "ownerName": "Rajesh Sharma",
                "ownerMobile": "9123456789",
                "ownerWhatsapp": "9123456789",
                "email": "rajesh.sharma@gmail.com",
                "outletType": "GROCERY",
                "address": "Shop No. 5, Sector 17, Vashi",
                "landmark": "Near Vashi Railway Station",
                "locality": "Vashi",
                "locationId": "b0000000-0000-0000-0000-000000000001",
                "pincode": "110001",
                "city": "New Delhi",
                "state": "Delhi",
                "latitude": 19.0771,
                "longitude": 72.9986,
                "classification": null,
                "outletStatus": "DRAFT_BASIC",
                "operationalStatus": "IN_PROGRESS",
                "assetStatus": "NOT_REQUESTED",
                "complianceState": "NON_COMPLIANT",
                "businessId": "c0000000-0000-0000-0000-000000000001",
                "createdByAseId": "99cb351a-9cf2-49bd-b176-6af413aeb9f9",
                "createdByAseName": "Rakesh Verma",
                "createdAt": "2026-03-03T12:43:13.376921935Z",
                "photos": []
            },
            "timestamp": "2026-03-03T12:43:13.408289709Z"
        }
        """.trimIndent()

        val result = json.decodeFromString<CreateOutletResponseDto>(raw)
        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals("a757535f-d461-4858-ab66-a252e89a6e2b", result.data!!.id)
        assertEquals("Sharma General Point", result.data!!.name)
        assertEquals("GROCERY", result.data!!.outletType)
        assertEquals("DRAFT_BASIC", result.data!!.outletStatus)
        assertEquals("Rakesh Verma", result.data!!.createdByAseName)
        assertEquals(19.0771, result.data!!.latitude)
        assertTrue(result.data!!.photos.isEmpty())
    }

    @Test
    fun `CreateOutletResponseDto - error response`() {
        val raw = """
        {
            "success": false,
            "errorCode": "VALIDATION_ERROR",
            "error": "Phone number is required",
            "timestamp": "2026-03-03T12:43:13.408289709Z"
        }
        """.trimIndent()

        val result = json.decodeFromString<CreateOutletResponseDto>(raw)
        assertEquals(false, result.success)
        assertNull(result.data)
        assertEquals("VALIDATION_ERROR", result.errorCode)
        assertEquals("Phone number is required", result.error)
    }

    // ── Outlet List DTOs ──

    @Test
    fun `OutletListResponseDto - paginated response`() {
        val raw = """
        {
            "success": true,
            "data": {
                "content": [
                    {
                        "id": "a757535f-d461-4858-ab66-a252e89a6e2b",
                        "name": "Sharma General Point",
                        "phone": "9123456789",
                        "ownerName": "Rajesh Sharma",
                        "ownerMobile": "9123456789",
                        "ownerWhatsapp": "9123456789",
                        "email": "rajesh.sharma@gmail.com",
                        "outletType": "GROCERY",
                        "address": "Shop No. 5, Sector 17, Vashi",
                        "outletStatus": "DRAFT_BASIC",
                        "businessId": "c0000000-0000-0000-0000-000000000001",
                        "createdByAseId": "99cb351a-9cf2-49bd-b176-6af413aeb9f9",
                        "createdByAseName": "Rakesh Verma",
                        "createdAt": "2026-03-03T12:43:13.376922Z",
                        "photos": []
                    }
                ],
                "page": 0,
                "size": 20,
                "totalElements": 1,
                "totalPages": 1,
                "last": true
            },
            "timestamp": "2026-03-03T12:43:56.809127084Z"
        }
        """.trimIndent()

        val result = json.decodeFromString<OutletListResponseDto>(raw)
        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(1, result.data!!.content.size)
        assertEquals(0, result.data!!.page)
        assertEquals(20, result.data!!.size)
        assertEquals(1, result.data!!.totalElements)
        assertEquals(1, result.data!!.totalPages)
        assertTrue(result.data!!.last)

        val outlet = result.data!!.content[0]
        assertEquals("a757535f-d461-4858-ab66-a252e89a6e2b", outlet.id)
        assertEquals("Sharma General Point", outlet.name)
        assertEquals("DRAFT_BASIC", outlet.outletStatus)
    }

    @Test
    fun `OutletListResponseDto - auth error`() {
        val raw = """
        {
            "success": false,
            "errorCode": "AUTH_UNAUTHORIZED",
            "error": "Invalid or missing JWT token",
            "timestamp": "2026-03-03T15:15:37.805420026Z"
        }
        """.trimIndent()

        val result = json.decodeFromString<OutletListResponseDto>(raw)
        assertEquals(false, result.success)
        assertNull(result.data)
        assertEquals("AUTH_UNAUTHORIZED", result.errorCode)
    }

    @Test
    fun `OutletListResponseDto - empty page`() {
        val raw = """
        {
            "success": true,
            "data": {
                "content": [],
                "page": 0,
                "size": 20,
                "totalElements": 0,
                "totalPages": 0,
                "last": true
            },
            "timestamp": "2026-03-03T12:43:56.809127084Z"
        }
        """.trimIndent()

        val result = json.decodeFromString<OutletListResponseDto>(raw)
        assertTrue(result.success)
        assertTrue(result.data!!.content.isEmpty())
        assertEquals(0, result.data!!.totalElements)
        assertTrue(result.data!!.last)
    }
}

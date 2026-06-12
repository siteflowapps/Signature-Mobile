package com.siteflow.signature.core.data.networking.util

import com.siteflow.signature.core.domain.UserRole
import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object JwtUtils {

    /** Backend role strings that don't match enum names exactly. */
    private val ROLE_ALIASES = mapOf("RETAILER" to UserRole.OUTLET)

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodePayload(token: String): JsonObject? {
        return try {
            val payload = token.split(".")[1]
            val padded = payload.padEnd(payload.length + (4 - payload.length % 4) % 4, '=')
            val decodedBytes = Base64.UrlSafe.decode(padded)
            val jsonString = decodedBytes.decodeToString()
            Json.parseToJsonElement(jsonString).jsonObject
        } catch (e: Exception) {
            null
        }
    }

    fun extractRole(token: String): UserRole? {
        return try {
            val json = decodePayload(token) ?: return null
            val role = json["role"]?.jsonPrimitive?.content ?: return null
            ROLE_ALIASES[role] ?: UserRole.entries.firstOrNull { it.name == role }
        } catch (e: Exception) {
            null
        }
    }

    fun extractUserId(token: String): String? {
        return try {
            val json = decodePayload(token) ?: return null
            json["sub"]?.jsonPrimitive?.contentOrNull
        } catch (e: Exception) {
            null
        }
    }

    fun extractBusinessId(token: String): String? {
        return try {
            val json = decodePayload(token) ?: return null
            json["businessId"]?.jsonPrimitive?.contentOrNull
        } catch (e: Exception) {
            null
        }
    }

    fun extractLocationId(token: String): String? {
        return try {
            val json = decodePayload(token) ?: return null
            json["locationId"]?.jsonPrimitive?.contentOrNull
        } catch (e: Exception) {
            null
        }
    }
}

package com.siteflow.signature.core.data.networking.error

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.text.get

fun ErrorDto.toApiError(fallbackCode: Int = -1): ApiError {
    val resolvedCode = this.code ?: fallbackCode
    val resolvedMessage = this.message?.takeIf { it.isNotBlank() } ?: "Unknown error"
    return ApiError(resolvedCode, resolvedMessage)
}


@Serializable
data class ErrorDto(
    val code: Int? = null,
    val message: String? = null,
)




private val json = Json {
    ignoreUnknownKeys = true
}

/**
 * Tolerant API error parser.
 *
 * Supports:
 * - "Plain string error"
 * - { "message": "error" }
 * - { "error": "error" }
 * - { "error": { "code": 400, "message": "error" } }
 * - RFC7807: { "title": "...", "detail": "...", "status": 400 }
 * - Empty / malformed responses
 */
fun parseApiError(
    rawBody: String,
    httpStatus: Int
): ApiError {

    if (rawBody.isBlank()) {
        return ApiError(
            code = httpStatus,
            message = "Request failed ($httpStatus)"
        )
    }

    return runCatching {

        val element = json.parseToJsonElement(rawBody)

        // Case 1: plain string response
        if (element is JsonPrimitive && element.isString) {
            return ApiError(httpStatus, element.content)
        }

        val root = element.jsonObject

        // Case: Signature backend: { "success": false, "errorCode": "...", "error": "..." }
        root["success"]?.jsonPrimitive?.contentOrNull?.let { successStr ->
            if (successStr == "false") {
                val errorCode = root["errorCode"]?.jsonPrimitive?.contentOrNull
                val rawMessage = root["error"]?.jsonPrimitive?.contentOrNull
                    ?: errorCode
                    ?: "Request failed ($httpStatus)"

                // Map raw backend errors to user-friendly messages
                val friendlyMessage = toFriendlyMessage(errorCode, rawMessage, httpStatus)
                return ApiError(httpStatus, friendlyMessage)
            }
        }

        // Case 2: { "error": "message" }
        root["error"]?.let { error ->
            if (error is JsonPrimitive && error.isString) {
                return ApiError(httpStatus, error.content)
            }

            // Case 3: { "error": { "code":..., "message":... } }
            if (error is JsonObject) {
                val code = error["code"]?.jsonPrimitive?.intOrNull ?: httpStatus
                val message =
                    error["message"]?.jsonPrimitive?.contentOrNull
                        ?: "Request failed ($code)"
                return ApiError(code, message)
            }
        }

        // Case 4: { "message": "error" }
        root["message"]?.jsonPrimitive?.contentOrNull?.let { message ->
            return ApiError(httpStatus, message)
        }

        // Case 5: RFC 7807 (problem+json)
        if ("title" in root || "detail" in root) {
            val status = root["status"]?.jsonPrimitive?.intOrNull ?: httpStatus
            val message =
                root["detail"]?.jsonPrimitive?.contentOrNull
                    ?: root["title"]?.jsonPrimitive?.contentOrNull
                    ?: "Request failed ($status)"
            return ApiError(status, message)
        }

        ApiError(
            code = httpStatus,
            message = "Request failed ($httpStatus)"
        )

    }.getOrElse {
        ApiError(
            code = httpStatus,
            message = "Request failed ($httpStatus)"
        )
    }
}

/**
 * Maps raw backend error codes / messages to polished, user-facing messages.
 */
private fun toFriendlyMessage(
    errorCode: String?,
    rawMessage: String,
    httpStatus: Int
): String {
    // Map by errorCode first (most reliable)
    when (errorCode) {
        "NOT_FOUND" -> {
            if (rawMessage.contains("User not found", ignoreCase = true)) {
                return "This phone number is not registered. Please contact your administrator."
            }
        }
        "UNAUTHORIZED" -> return "Your session has expired. Please log in again."
        "FORBIDDEN" -> return "You don't have permission to perform this action."
        "TOO_MANY_REQUESTS" -> return "Too many attempts. Please wait a moment and try again."
    }

    // Map by HTTP status as fallback
    return when (httpStatus) {
        401 -> "Your session has expired. Please log in again."
        403 -> "You don't have permission to perform this action."
        404 -> "The requested resource was not found."
        429 -> "Too many attempts. Please wait a moment and try again."
        in 500..599 -> "Something went wrong on our end. Please try again later."
        else -> rawMessage
    }
}

package com.siteflow.signature.core.data.auth

import com.siteflow.signature.getPlatform
import io.ktor.client.plugins.api.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlin.random.Random

class AuthInterceptor(
    private val tokenStorage: TokenStorage
) {

    val plugin = createClientPlugin("AuthInterceptor") {

        onRequest { request, _ ->
            tokenStorage.getAccessToken()?.let { token ->
                request.headers.append(
                    HttpHeaders.Authorization,
                    "Bearer $token"
                )
            }
            request.headers.append("X-Request-Id", generateRequestId())
            request.headers.append("X-Client-Type", getPlatform().clientType)

            // ── Request Logging ──
            val method = request.method.value
            val url = request.url.buildString()
            val contentType = request.contentType()?.toString() ?: "N/A"
            println("┌── API Request ──────────────────────")
            println("│ $method $url")
            println("│ Content-Type: $contentType")
            request.headers.entries().forEach { (key, values) ->
                if (key != HttpHeaders.Authorization) {
                    println("│ $key: ${values.joinToString()}")
                } else {
                    println("│ Authorization: ${values.joinToString()}")
                }
            }
            println("└─────────────────────────────────────")
        }

        onResponse { response ->
            val status = response.status.value
            val url = response.request.url.toString()
            val method = response.request.method.value
            println("┌── API Response ─────────────────────")
            println("│ $status ← $method $url")
            println("└─────────────────────────────────────")
        }
    }

    private fun generateRequestId(): String {
        val bytes = ByteArray(16).also { Random.nextBytes(it) }
        bytes[6] = (bytes[6].toInt() and 0x0F or 0x40).toByte() // version 4
        bytes[8] = (bytes[8].toInt() and 0x3F or 0x80).toByte() // variant
        return buildString {
            bytes.forEachIndexed { i, byte ->
                if (i == 4 || i == 6 || i == 8 || i == 10) append('-')
                append(byte.toInt().and(0xFF).toString(16).padStart(2, '0'))
            }
        }
    }
}

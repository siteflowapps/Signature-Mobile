package com.siteflow.retailsync.core.data.auth

import io.ktor.client.plugins.api.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*

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
                    println("│ Authorization: Bearer ***")
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

            // Log response body for error responses
            if (status >= 400) {
                try {
                    val bodyText = response.bodyAsText()
                    println("│ BODY: $bodyText")
                } catch (_: Exception) {
                    println("│ BODY: (unable to read)")
                }
            }

            println("└─────────────────────────────────────")
        }
    }
}

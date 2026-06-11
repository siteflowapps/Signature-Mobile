package com.siteflow.retailsync.core.data.networking

import com.siteflow.retailsync.core.data.networking.client.HttpClientProvider
import com.siteflow.retailsync.core.data.networking.client.NetworkConfig
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class AndroidHttpClient : HttpClientProvider {

    override fun create(): HttpClient =
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[HTTP] $message")
                    }
                }
                level = LogLevel.HEADERS
                sanitizeHeader { header -> header == "Authorization" }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = NetworkConfig.TIMEOUT_MS
                socketTimeoutMillis = NetworkConfig.TIMEOUT_MS
                connectTimeoutMillis = 15_000L
            }
        }
}

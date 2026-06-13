package com.siteflow.signature.core.data.networking


import com.siteflow.signature.core.data.networking.client.HttpClientProvider
import com.siteflow.signature.core.data.networking.client.NetworkConfig
import com.siteflow.signature.core.logger.ReceeLogger
import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class IosHttpClient : HttpClientProvider {

    override fun create(): HttpClient =
        HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    coerceInputValues = true
                })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        ReceeLogger.d("HTTP", message)
                    }
                }
                level = LogLevel.ALL
                sanitizeHeader { header -> header == "Authorization" }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = NetworkConfig.TIMEOUT_MS
                socketTimeoutMillis = NetworkConfig.TIMEOUT_MS
                connectTimeoutMillis = 30_000L
            }
        }
}

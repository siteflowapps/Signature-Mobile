package com.siteflow.signature.core.data.auth

import com.siteflow.signature.core.data.networking.client.HttpClientProvider
import com.siteflow.signature.core.data.networking.client.NetworkConfig
import com.siteflow.signature.core.logger.ReceeLogger
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * Handles silent token refresh when the access token expires (401).
 *
 * Uses a **raw HttpClient** (no AuthInterceptor) to avoid attaching the
 * expired Bearer token or triggering recursive refresh loops.
 *
 * A Mutex ensures that concurrent 401 responses only trigger a single
 * refresh call — subsequent callers wait and reuse the new token.
 */
class TokenRefreshManager(
    private val tokenStorage: TokenStorage,
    private val httpClientProvider: HttpClientProvider
) {
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val TAG = "TokenRefresh"
        
        /**
         * Global instance set during DI initialization.
         * Accessed by ResponseMapper (which is an inline function
         * and cannot use constructor injection).
         */
        var instance: TokenRefreshManager? = null
            private set
        
        fun initialize(manager: TokenRefreshManager) {
            instance = manager
        }
    }

    /**
     * Attempt to refresh the access token using the stored refresh token.
     *
     * The Mutex ensures that if multiple 401 responses arrive concurrently,
     * only a single refresh call is made — the rest wait and reuse the result.
     *
     * @return `true` if the token was refreshed successfully,
     *         `false` if the refresh failed and the user should be logged out.
     */
    suspend fun tryRefresh(): Boolean {
        return mutex.withLock {
            val refreshToken = tokenStorage.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                ReceeLogger.e(TAG, "No refresh token available")
                return@withLock false
            }

            val rawClient = httpClientProvider.create()
            try {
                val response = rawClient.post(NetworkConfig.v1("auth/refresh")) {
                    contentType(ContentType.Application.Json)
                    setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
                }

                val status = response.status.value
                val body = response.bodyAsText()

                ReceeLogger.d(TAG, "Refresh response: status=$status body=$body")

                if (status in 200..299) {
                    val parsed = json.decodeFromString<RefreshTokenResponseDto>(body)
                    if (parsed.success && parsed.data != null) {
                        tokenStorage.saveAccessToken(parsed.data.accessToken)
                        tokenStorage.saveRefreshToken(parsed.data.refreshToken)
                        ReceeLogger.d(TAG, "✅ Token refreshed successfully")
                        return@withLock true
                    }
                }

                ReceeLogger.e(TAG, "❌ Refresh failed: status=$status body=$body")
                return@withLock false

            } catch (e: Exception) {
                ReceeLogger.e(TAG, "❌ Refresh exception", e)
                return@withLock false
            } finally {
                rawClient.close()
            }
        }
    }
}

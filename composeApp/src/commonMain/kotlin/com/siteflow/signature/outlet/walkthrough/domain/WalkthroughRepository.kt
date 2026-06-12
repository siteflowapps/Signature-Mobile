package com.siteflow.signature.outlet.walkthrough.domain

import com.siteflow.signature.core.data.auth.TokenStorage
import com.siteflow.signature.core.data.config.ConfigApi
import com.siteflow.signature.core.data.networking.result.NetworkResult

/**
 * Manages walkthrough completion state.
 * Backed by the config API (server) + local storage (cache).
 *
 * Flow:
 * 1. On login / cold-start → call [syncFromConfig] with the `userFlags` map
 *    from the config response.
 * 2. [isCompleted] reads the local cache (fast, offline-safe).
 * 3. On walkthrough finish → [markCompleted] persists to server (PUT) then caches locally.
 */
class WalkthroughRepository(
    private val storage: TokenStorage,
    private val configApi: ConfigApi
) {

    /** Fast local check — always reads from persisted prefs. */
    suspend fun isCompleted(): Boolean = storage.getWalkthroughCompleted()

    /**
     * Accept userFlags from a GET /config response and cache the
     * `welcome_shown` flag locally.
     */
    suspend fun syncFromConfig(userFlags: Map<String, String>) {
        val welcomeShown = userFlags["welcome_shown"] == "true"
        storage.saveWalkthroughCompleted(welcomeShown)
    }

    /**
     * Mark the walkthrough as completed:
     * 1. PUT the flag to the server.
     * 2. Save to local storage regardless of server result
     *    (so the user is never stuck in a loop).
     */
    suspend fun markCompleted() {
        // Fire-and-forget — save locally even if API fails
        try {
            configApi.updateUserFlag(
                flagKey = "welcome_shown",
                flagValue = "true"
            )
        } catch (e: Exception) {
            println("[WalkthroughRepo] Failed to PUT welcome_shown flag: ${e.message}")
        }
        storage.saveWalkthroughCompleted(true)
    }
}

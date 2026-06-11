package com.siteflow.cdo.core.data.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton manager for the active [AppEnvironment].
 *
 * - Loaded from persistent storage once at app start via [initialize].
 * - Exposes [currentEnv] as a [StateFlow] so the UI can react to changes.
 * - [NetworkConfig.BASE_URL] reads from this at request time, so no Ktor
 *   client recreation is needed when the environment switches.
 */
object EnvironmentManager {

    private val _currentEnv = MutableStateFlow(AppEnvironment.PROD)
    val currentEnv: StateFlow<AppEnvironment> = _currentEnv.asStateFlow()

    /** Called once at startup with the persisted value from platform storage. */
    fun initialize(saved: AppEnvironment) {
        _currentEnv.value = saved
    }

    /** Switch environment and persist via the provided save lambda. */
    fun switch(env: AppEnvironment, persist: (String) -> Unit) {
        _currentEnv.value = env
        persist(env.name)
    }

    val baseUrl: String
        get() = _currentEnv.value.baseUrl

    val apiHost: String
        get() = _currentEnv.value.apiHost
}

package com.siteflow.signature.core.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory role storage for dev. Will be replaced with JWT-based role
 * extraction when the real API is integrated.
 */
object RoleManager {
    private val _currentRole = MutableStateFlow<UserRole?>(null)
    val currentRole = _currentRole.asStateFlow()

    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    fun clear() {
        _currentRole.value = null
    }
}

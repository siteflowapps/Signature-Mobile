package com.siteflow.cdo.core.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class SessionEvent {
    object SessionExpired : SessionEvent()
}

object SessionManager {
    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()
    
    private var authRepository: AuthRepository? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    fun initialize(repository: AuthRepository) {
        authRepository = repository
    }

    fun notifySessionExpired() {
        scope.launch {
            // Clear token first
            authRepository?.logout()
            // Then notify UI to navigate
            _events.emit(SessionEvent.SessionExpired)
        }
    }
}

package com.siteflow.signature.core.presentation.components.connectivity

import com.siteflow.signature.core.domain.Status
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global reactive connectivity state — similar pattern to GlobalLoading / GlobalToastHandler.
 * Observed by NoInternetBanner in App.kt.
 */
object ConnectivityState {

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun update(status: Status) {
        _isConnected.value = (status == Status.Available)
    }
}

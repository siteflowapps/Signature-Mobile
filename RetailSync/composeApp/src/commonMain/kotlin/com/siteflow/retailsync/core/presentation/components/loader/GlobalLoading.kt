package com.siteflow.retailsync.core.presentation.components.loader

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object GlobalLoading {

    private val _counter = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun show() {
        _counter.value += 1
        _isLoading.value = true
    }

    fun hide() {
        _counter.value = (_counter.value - 1).coerceAtLeast(0)
        _isLoading.value = _counter.value > 0
    }

    fun reset() {
        _counter.value = 0
        _isLoading.value = false
    }
}

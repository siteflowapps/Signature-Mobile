package com.siteflow.signature.core.presentation.components.toast

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class ToastType { SUCCESS, ERROR, INFO }

data class ToastMessage(
    val message: String,
    val type: ToastType = ToastType.INFO
)

object GlobalToastHandler {

    private val _toasts = MutableSharedFlow<ToastMessage>(extraBufferCapacity = 1)
    val toasts = _toasts.asSharedFlow()

    fun showSuccess(message: String) {
        _toasts.tryEmit(ToastMessage(message, ToastType.SUCCESS))
    }

    fun showError(message: String) {
        _toasts.tryEmit(ToastMessage(message, ToastType.ERROR))
    }

    fun showInfo(message: String) {
        _toasts.tryEmit(ToastMessage(message, ToastType.INFO))
    }
}

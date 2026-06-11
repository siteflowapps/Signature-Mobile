package com.siteflow.retailsync.core.presentation

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Platform-agnostic Base ViewModel for MVI architecture.
 *
 * @param S State: The UI state
 * @param A Action: User intents
 * @param E Event: One-time side effects (Navigation, SnackBar)
 */
abstract class BaseViewModel<S, A, E>(initialState: S) {

    protected val viewModelScope = MainScope()

    open fun clear() {
        viewModelScope.cancel()
    }

    protected val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    protected val _events = MutableSharedFlow<E>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    abstract fun onAction(action: A)

    protected fun updateState(reducer: (S) -> S) {
        _state.update(reducer)
    }

    protected fun emitEvent(event: E) {
        _events.tryEmit(event)
    }
}

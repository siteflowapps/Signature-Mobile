package com.siteflow.cdo.core.presentation

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
 * @param S State: The UI state (e.g., OnboardingState)
 * @param A Action: User intents (e.g., OnboardingAction)
 * @param E Event: One-time side effects (e.g., OnboardingEvent / Navigation)
 */
abstract class BaseViewModel<S, A, E>(initialState: S) {

    /**
     * Lifecycle-scoped coroutine scope for ViewModel work.
     * Uses [MainScope] (SupervisorJob + Dispatchers.Main).
     * Call [clear] when the ViewModel is no longer needed.
     */
    protected val viewModelScope = MainScope()

    /** Cancel all coroutines launched in [viewModelScope]. Call on ViewModel destruction. */
    open fun clear() {
        viewModelScope.cancel()
    }


    // 1. State: Represented as a StateFlow for reactive UI updates
    protected val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    // 2. Events: Represented as a SharedFlow for one-time side effects (Navigation, SnackBar)
    protected val _events = MutableSharedFlow<E>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * Entry point for UI actions. 
     * Every subclass must implement this to handle screen-specific logic.
     */
    abstract fun onAction(action: A)

    /**
     * Helper to update the state in a thread-safe manner.
     */
    protected fun updateState(reducer: (S) -> S) {
        _state.update(reducer)
    }

    /**
     * Helper to emit one-time events.
     */
    protected fun emitEvent(event: E) {
        _events.tryEmit(event)
    }
}

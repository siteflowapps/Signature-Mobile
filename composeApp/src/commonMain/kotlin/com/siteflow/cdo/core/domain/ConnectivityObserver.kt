package com.siteflow.cdo.core.domain

import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific connectivity observer.
 * Emits real-time changes to network connectivity status.
 */
expect class ConnectivityObserver() {
    fun observe(): Flow<Status>
}

enum class Status {
    Available,
    Unavailable
}

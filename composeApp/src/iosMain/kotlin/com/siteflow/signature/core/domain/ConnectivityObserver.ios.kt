package com.siteflow.signature.core.domain

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import platform.Network.*
import platform.darwin.*

/**
 * iOS connectivity observer using NWPathMonitor.
 * Emits real-time network status via callbackFlow.
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class ConnectivityObserver {

    actual fun observe(): Flow<Status> = callbackFlow {
        val monitor = nw_path_monitor_create()
        val queue = dispatch_queue_create("com.siteflow.connectivity", null)

        nw_path_monitor_set_update_handler(monitor) { path ->
            val pathStatus = nw_path_get_status(path)
            val isConnected = pathStatus == nw_path_status_satisfied
            trySend(if (isConnected) Status.Available else Status.Unavailable)
        }

        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)

        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }.distinctUntilChanged()
}

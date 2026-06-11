package com.siteflow.retailsync.scanner.domain

data class ScannerState(
    val scannedData: String? = null,
    val outletId: String? = null,
    val outletName: String? = null,
    val isProcessing: Boolean = false,
    val error: String? = null
)

sealed interface ScannerAction {
    data class QrScanned(val data: String) : ScannerAction
    data object Retry : ScannerAction
    data object Cancel : ScannerAction
    data object ConfirmOutlet : ScannerAction
}

sealed interface ScannerEvent {
    data class NavigateToOrderCreation(val outletId: String, val outletName: String) : ScannerEvent
    data object NavigateBack : ScannerEvent
}

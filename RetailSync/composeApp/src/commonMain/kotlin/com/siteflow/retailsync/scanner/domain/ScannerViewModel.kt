package com.siteflow.retailsync.scanner.domain

import com.siteflow.retailsync.core.presentation.BaseViewModel
import com.siteflow.retailsync.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.retailsync.outlet.data.OutletRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ScannerViewModel(
    private val outletRepository: OutletRepository
) : BaseViewModel<ScannerState, ScannerAction, ScannerEvent>(ScannerState()) {

    private val json = Json { ignoreUnknownKeys = true }

    override fun onAction(action: ScannerAction) {
        when (action) {
            is ScannerAction.QrScanned -> processQrData(action.data)
            ScannerAction.Retry -> reset()
            ScannerAction.Cancel -> emitEvent(ScannerEvent.NavigateBack)
            ScannerAction.ConfirmOutlet -> confirmOutlet()
        }
    }

    private fun processQrData(data: String) {
        updateState { it.copy(isProcessing = true, error = null, scannedData = data) }

        try {
            val element = json.parseToJsonElement(data)
            val obj = element.jsonObject
            val scannedOutletId = obj["outletId"]?.jsonPrimitive?.content

            if (scannedOutletId == null) {
                updateState {
                    it.copy(
                        isProcessing = false,
                        error = "Invalid QR code: missing outletId"
                    )
                }
                GlobalToastHandler.showError("Invalid QR code format")
                return
            }

            // Validate scanned outlet ID against cached outlets
            val matchedOutlet = outletRepository.getOutletById(scannedOutletId)

            if (matchedOutlet != null) {
                updateState {
                    it.copy(
                        outletId = matchedOutlet.id,
                        outletName = matchedOutlet.name,
                        isProcessing = false
                    )
                }
                GlobalToastHandler.showSuccess("Outlet found: ${matchedOutlet.name}")
            } else {
                updateState {
                    it.copy(
                        isProcessing = false,
                        error = "Outlet not found. This outlet is not assigned to you."
                    )
                }
                GlobalToastHandler.showError("Outlet not recognized")
            }
        } catch (e: Exception) {
            updateState {
                it.copy(
                    isProcessing = false,
                    error = "Invalid QR code format. Expected JSON with outletId."
                )
            }
            GlobalToastHandler.showError("Could not parse QR code")
        }
    }

    private fun confirmOutlet() {
        val outletId = state.value.outletId ?: return
        val outletName = state.value.outletName ?: return
        emitEvent(ScannerEvent.NavigateToOrderCreation(outletId, outletName))
    }

    private fun reset() {
        updateState { ScannerState() }
    }
}

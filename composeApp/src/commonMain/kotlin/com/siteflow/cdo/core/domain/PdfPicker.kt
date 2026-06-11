package com.siteflow.cdo.core.domain

/**
 * Picks a PDF file from the device's storage.
 *
 * Android: Storage Access Framework via `ActivityResultContracts.OpenDocument`,
 *          restricted to `application/pdf`. No runtime permission needed.
 * iOS:     `UIDocumentPickerViewController` with `UTType.pdf`.
 */
expect class PdfPicker {
    fun pickPdf(
        onPicked: (PdfPickResult) -> Unit
    )
}

sealed interface PdfPickResult {
    data class Success(val bytes: ByteArray, val filename: String) : PdfPickResult
    data object Cancelled : PdfPickResult
    data class Error(val message: String) : PdfPickResult
}

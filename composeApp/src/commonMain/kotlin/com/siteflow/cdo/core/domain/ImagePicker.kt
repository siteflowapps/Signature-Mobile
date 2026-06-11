package com.siteflow.cdo.core.domain

// commonMain
expect class ImagePicker {
    fun openCamera(
        onImagePicked: (String) -> Unit,
        onPermissionDenied: () -> Unit
    )

    fun openGallery(
        onImagePicked: (String) -> Unit
    )

    /**
     * Opens the platform document scanner for invoice capture.
     *
     * Android: Launches ML Kit Document Scanner (edge overlay, auto-crop,
     *          perspective correction). Falls back to [openCamera] if Play Services
     *          unavailable.
     * iOS:     Delegates to [openCamera] — VNDetectRectanglesRequest perspective
     *          correction is handled inside InvoicePreprocessor.
     */
    fun openDocumentScanner(
        onImagePicked: (String) -> Unit,
        onPermissionDenied: () -> Unit
    )
}

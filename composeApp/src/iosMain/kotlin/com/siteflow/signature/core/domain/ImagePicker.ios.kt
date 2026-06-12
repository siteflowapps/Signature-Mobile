package com.siteflow.signature.core.domain

import platform.AVFoundation.*
import platform.Foundation.*
import platform.UIKit.*
import platform.VisionKit.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlinx.cinterop.ExperimentalForeignApi

actual class ImagePicker {

    private var onImagePicked: ((String) -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null

    // Strong references — UIImagePickerController.delegate and
    // VNDocumentCameraViewController.delegate are both weak in ObjC/Swift.
    // Without a strong ref on the Kotlin side, ARC deallocates the delegate
    // before capture completes and the callback never fires.
    private var pickerDelegate: PickerDelegate? = null
    private var scannerDelegate: DocumentScannerDelegate? = null

    actual fun openCamera(
        onImagePicked: (String) -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        this.onImagePicked = onImagePicked
        this.onPermissionDenied = onPermissionDenied

        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)

        when (status) {
            AVAuthorizationStatusAuthorized -> {
                presentPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)
            }
            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    if (granted) {
                        dispatch_async(dispatch_get_main_queue()) {
                            presentPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)
                        }
                    } else {
                        onPermissionDenied()
                    }
                }
            }
            AVAuthorizationStatusDenied,
            AVAuthorizationStatusRestricted -> {
                onPermissionDenied()
            }
            else -> {
                onPermissionDenied()
            }
        }
    }

    actual fun openGallery(onImagePicked: (String) -> Unit) {
        this.onImagePicked = onImagePicked
        presentPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)
    }

    /**
     * Opens VNDocumentCameraViewController — provides live document edge overlay,
     * auto-capture, and manual shutter (same experience as iOS Notes scanning).
     *
     * The scanned page is saved as JPEG and forwarded to [InvoicePreprocessor]
     * for quality analysis + enhancement (grayscale, contrast, resize, compress).
     * VNDetectRectanglesRequest inside the preprocessor is skipped when the image
     * already has clean edges from VisionKit — the confidence threshold (0.85)
     * ensures this naturally.
     *
     * Falls back to [openCamera] if VisionKit document scanning is unsupported.
     */
    actual fun openDocumentScanner(
        onImagePicked: (String) -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (!VNDocumentCameraViewController.isSupported()) {
            openCamera(onImagePicked, onPermissionDenied)
            return
        }

        val delegate = DocumentScannerDelegate(
            onScanned = onImagePicked,
            onFailed  = { openCamera(onImagePicked, onPermissionDenied) }
        )
        scannerDelegate = delegate // hold strong ref

        val scanner = VNDocumentCameraViewController()
        scanner.delegate = delegate
        getTopViewController()?.presentViewController(scanner, true, null)
    }

    // ── UIImagePickerController (camera + gallery) ────────────────────────────

    private fun presentPicker(sourceType: UIImagePickerControllerSourceType) {
        val delegate = PickerDelegate()
        pickerDelegate = delegate // hold strong ref

        val picker = UIImagePickerController().apply {
            this.sourceType = sourceType
            this.delegate = delegate
        }
        getTopViewController()?.presentViewController(picker, true, null)
    }

    private inner class PickerDelegate :
        NSObject(),
        UIImagePickerControllerDelegateProtocol,
        UINavigationControllerDelegateProtocol {

        override fun imagePickerController(
            picker: UIImagePickerController,
            didFinishPickingMediaWithInfo: Map<Any?, *>
        ) {
            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            if (image != null) {
                val path = saveImage(image)
                onImagePicked?.invoke(path)
            }
            picker.dismissViewControllerAnimated(true, null)
            pickerDelegate = null
        }

        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            picker.dismissViewControllerAnimated(true, null)
            pickerDelegate = null
        }
    }

    // ── VNDocumentCameraViewController ───────────────────────────────────────

    private inner class DocumentScannerDelegate(
        private val onScanned: (String) -> Unit,
        private val onFailed:  () -> Unit
    ) : NSObject(), VNDocumentCameraViewControllerDelegateProtocol {

        override fun documentCameraViewController(
            controller: VNDocumentCameraViewController,
            didFinishWithScan: VNDocumentCameraScan
        ) {
            // Page 0 = first (and typically only) scanned page
            val image = didFinishWithScan.imageOfPageAtIndex(0u)
            val path  = saveImage(image)
            controller.dismissViewControllerAnimated(true) { onScanned(path) }
            scannerDelegate = null
        }

        override fun documentCameraViewControllerDidCancel(
            controller: VNDocumentCameraViewController
        ) {
            controller.dismissViewControllerAnimated(true, null)
            scannerDelegate = null
        }

        override fun documentCameraViewController(
            controller: VNDocumentCameraViewController,
            didFailWithError: NSError
        ) {
            controller.dismissViewControllerAnimated(true) { onFailed() }
            scannerDelegate = null
        }
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private fun saveImage(image: UIImage): String {
        val data = UIImageJPEGRepresentation(image, 0.9)
        val fileName = "recce_${NSDate().timeIntervalSince1970}.jpg"
        val path = NSTemporaryDirectory() + fileName
        data?.writeToFile(path, true)
        return path
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getTopViewController(): UIViewController? {
        val window = UIApplication.sharedApplication.keyWindow ?: return null
        var topController = window.rootViewController
        while (topController?.presentedViewController != null) {
            topController = topController.presentedViewController
        }
        return topController
    }
}

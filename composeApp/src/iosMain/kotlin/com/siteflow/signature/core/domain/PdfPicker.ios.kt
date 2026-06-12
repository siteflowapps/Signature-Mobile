package com.siteflow.signature.core.domain

import com.siteflow.signature.core.util.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.darwin.NSObject

/**
 * iOS actual for [PdfPicker]. Wraps [UIDocumentPickerViewController] restricted
 * to `UTType.pdf`. Reads the picked file inside the security-scoped access
 * block, returning bytes to the caller.
 */
@OptIn(ExperimentalForeignApi::class)
actual class PdfPicker {

    private var delegate: PickerDelegate? = null

    actual fun pickPdf(onPicked: (PdfPickResult) -> Unit) {
        val delegate = PickerDelegate(onPicked = { result ->
            onPicked(result)
            this.delegate = null
        })
        this.delegate = delegate

        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypePDF))
        picker.delegate = delegate
        picker.allowsMultipleSelection = false

        val top = topViewController()
        if (top == null) {
            this.delegate = null
            onPicked(PdfPickResult.Error("No active view controller to present from"))
            return
        }
        top.presentViewController(picker, animated = true, completion = null)
    }

    private inner class PickerDelegate(
        private val onPicked: (PdfPickResult) -> Unit,
    ) : NSObject(), UIDocumentPickerDelegateProtocol {

        override fun documentPicker(
            controller: UIDocumentPickerViewController,
            didPickDocumentsAtURLs: List<*>,
        ) {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            controller.dismissViewControllerAnimated(true, completion = null)
            if (url == null) {
                onPicked(PdfPickResult.Cancelled)
                return
            }
            val scoped = url.startAccessingSecurityScopedResource()
            try {
                val data = NSData.create(contentsOfURL = url)
                if (data == null) {
                    onPicked(PdfPickResult.Error("Could not read PDF data"))
                    return
                }
                val bytes = data.toByteArray()
                val filename = url.lastPathComponent ?: "invoice.pdf"
                onPicked(PdfPickResult.Success(bytes = bytes, filename = filename))
            } catch (t: Throwable) {
                onPicked(PdfPickResult.Error(t.message ?: "Could not read PDF"))
            } finally {
                if (scoped) url.stopAccessingSecurityScopedResource()
            }
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            controller.dismissViewControllerAnimated(true, completion = null)
            onPicked(PdfPickResult.Cancelled)
        }
    }

    private fun topViewController(): UIViewController? {
        val window = UIApplication.sharedApplication.keyWindow ?: return null
        var top = window.rootViewController
        while (top?.presentedViewController != null) {
            top = top.presentedViewController
        }
        return top
    }
}

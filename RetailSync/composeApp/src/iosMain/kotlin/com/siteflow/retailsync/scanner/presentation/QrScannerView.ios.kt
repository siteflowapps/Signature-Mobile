package com.siteflow.retailsync.scanner.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSLog
import platform.UIKit.*
import platform.darwin.dispatch_get_main_queue
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QrScannerView(
    modifier: Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    var hasScanned by remember { mutableStateOf(false) }

    UIKitViewController(
        modifier = modifier,
        factory = {
            QrScannerViewController(
                onScanned = { value ->
                    if (!hasScanned) {
                        hasScanned = true
                        onQrCodeScanned(value)
                    }
                }
            )
        }
    )
}

@Suppress("CONFLICTING_OVERLOADS")
@OptIn(ExperimentalForeignApi::class)
private class QrScannerViewController(
    private val onScanned: (String) -> Unit
) : UIViewController(nibName = null, bundle = null),
    AVCaptureMetadataOutputObjectsDelegateProtocol {

    private val captureSession = AVCaptureSession()
    private var previewLayer: AVCaptureVideoPreviewLayer? = null

    override fun viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = UIColor.blackColor

        captureSession.sessionPreset = AVCaptureSessionPresetHigh

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (device == null) {
            NSLog("QrScanner: No camera device found")
            return
        }

        val input = try {
            AVCaptureDeviceInput.deviceInputWithDevice(device, null)
        } catch (e: Exception) {
            NSLog("QrScanner: Failed to create camera input: ${e.message}")
            null
        }

        if (input != null && captureSession.canAddInput(input)) {
            captureSession.addInput(input)
        } else {
            NSLog("QrScanner: Cannot add camera input")
            return
        }

        val output = AVCaptureMetadataOutput()
        if (captureSession.canAddOutput(output)) {
            captureSession.addOutput(output)
            output.setMetadataObjectsDelegate(this, dispatch_get_main_queue())
            output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        } else {
            NSLog("QrScanner: Cannot add metadata output")
            return
        }

        val layer = AVCaptureVideoPreviewLayer(session = captureSession)
        layer.videoGravity = AVLayerVideoGravityResizeAspectFill
        previewLayer = layer
        view.layer.insertSublayer(layer, atIndex = 0u)

        // Start capture session on background queue
        platform.darwin.dispatch_async(
            platform.darwin.dispatch_get_global_queue(
                platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u
            )
        ) {
            captureSession.startRunning()
        }
    }

    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        // Resize preview layer to match view bounds
        previewLayer?.frame = view.bounds
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        if (captureSession.isRunning()) {
            captureSession.stopRunning()
        }
    }

    // AVCaptureMetadataOutputObjectsDelegateProtocol
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        for (metadataObject in didOutputMetadataObjects) {
            val readableObject = metadataObject as? AVMetadataMachineReadableCodeObject
            readableObject?.stringValue?.let { value ->
                NSLog("QrScanner: Scanned QR code: $value")
                // Vibrate on scan
                platform.AudioToolbox.AudioServicesPlaySystemSound(
                    platform.AudioToolbox.kSystemSoundID_Vibrate
                )
                captureSession.stopRunning()
                onScanned(value)
            }
        }
    }
}

package com.siteflow.cdo.outlet.invoices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.core.domain.ImagePicker
import com.siteflow.cdo.core.domain.PdfPicker
import com.siteflow.cdo.core.domain.PdfPickResult
import com.siteflow.cdo.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceAction
import com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceEvent
import com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceViewModel
import com.siteflow.cdo.outlet.invoices.presentation.components.*
import com.siteflow.cdo.core.presentation.components.dismissKeyboardOnTap
import org.koin.compose.koinInject

/**
 * Upload Invoice Screen — Capture step.
 * Shows: step indicator, source-select cards (gallery + PDF recommended, camera secondary),
 * captured image preview, and "Upload for AI Extraction" CTA for the image flow.
 * The PDF flow navigates to [PdfPreviewScreen] before reaching processing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadInvoiceScreen(
    onBack: () -> Unit = {},
    onNavigateToProcessing: () -> Unit = {},
    onNavigateToCropReview: ((String) -> Unit)? = null,
    onNavigateToPdfPreview: () -> Unit = {},
    onScanRegion: ((Int, com.siteflow.cdo.outlet.invoices.domain.LineItemField) -> Unit)? = null,
    viewModel: UploadInvoiceViewModel = koinInject(),
    imagePicker: ImagePicker = koinInject(),
    pdfPicker: PdfPicker = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var showPreScanGuide by remember { mutableStateOf(false) }

    // Collect one-time events (state is reset by dashboard before navigating here)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                UploadInvoiceEvent.NavigateBack -> onBack()
                UploadInvoiceEvent.NavigateToProcessing -> onNavigateToProcessing()
                UploadInvoiceEvent.NavigateToPdfPreview -> onNavigateToPdfPreview()
                is UploadInvoiceEvent.ShowToast -> { /* handled elsewhere */ }
                is UploadInvoiceEvent.ShowQualityWarning -> { /* state-driven banner */ }
                UploadInvoiceEvent.PromptRetakeWithGuide -> { /* state-driven dialog */ }
                UploadInvoiceEvent.NavigateToReview -> { /* handled by InvoiceProcessingScreen */ }
                is UploadInvoiceEvent.ExtractionFailed -> { /* handled by InvoiceProcessingScreen */ }

                // ── PDF flow events ──
                UploadInvoiceEvent.PdfFileTooLarge ->
                    GlobalToastHandler.showError("PDF is too large. Please use a file under 2 MB — try setting your scanner to B&W or 200 DPI.")
                is UploadInvoiceEvent.PdfTooManyPages ->
                    GlobalToastHandler.showError("PDF has ${event.actual} pages. Please upload a PDF with ${event.max} or fewer pages.")
                UploadInvoiceEvent.PdfEncrypted ->
                    GlobalToastHandler.showError("This PDF is password-protected. Please remove the password and try again.")
                UploadInvoiceEvent.PdfCorrupt ->
                    GlobalToastHandler.showError("Couldn't read this PDF. Please try a different file.")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.ScreenBackground)
                .dismissKeyboardOnTap()
        ) {
            // ── Scrollable content ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                UploadStepIndicator(currentStep = state.currentStep)

                Spacer(Modifier.height(24.dp))

                // ── Recommended sources ──
                CaptureSectionHeader(label = "RECOMMENDED")
                Spacer(Modifier.height(10.dp))

                InvoiceSourceCard(
                    icon = Icons.Default.Image,
                    title = "Upload from Gallery",
                    subtitle = "Pick a saved invoice image",
                    recommended = true,
                    enabled = !state.isPreprocessing && !state.isCompressingPdf,
                    onClick = {
                        viewModel.onAction(UploadInvoiceAction.UploadFromGallery)
                        imagePicker.openGallery { path ->
                            if (onNavigateToCropReview != null) {
                                onNavigateToCropReview(path)
                            } else {
                                viewModel.onAction(UploadInvoiceAction.ImageCaptured(path))
                            }
                        }
                    },
                )

                Spacer(Modifier.height(10.dp))

                InvoiceSourceCard(
                    icon = Icons.Default.PictureAsPdf,
                    title = "Upload PDF",
                    subtitle = "Up to 2 MB · 3 pages max",
                    recommended = true,
                    enabled = !state.isPreprocessing && !state.isCompressingPdf,
                    onClick = {
                        viewModel.onAction(UploadInvoiceAction.PickPdf)
                        pdfPicker.pickPdf { result ->
                            when (result) {
                                is PdfPickResult.Success ->
                                    viewModel.onAction(
                                        UploadInvoiceAction.PdfPicked(result.bytes, result.filename)
                                    )
                                PdfPickResult.Cancelled ->
                                    viewModel.onAction(UploadInvoiceAction.PdfPickCancelled)
                                is PdfPickResult.Error ->
                                    GlobalToastHandler.showError(result.message)
                            }
                        }
                    },
                )

                Spacer(Modifier.height(20.dp))

                // ── Other options ──
                CaptureSectionHeader(label = "OTHER OPTIONS")
                Spacer(Modifier.height(10.dp))

                InvoiceSourceCard(
                    icon = Icons.Default.CameraAlt,
                    title = "Take a Photo",
                    subtitle = "Use only if you don't have a saved file",
                    recommended = false,
                    enabled = !state.isPreprocessing && !state.isCompressingPdf,
                    onClick = {
                        if (!state.isPreprocessing && !state.isCompressingPdf) {
                            showPreScanGuide = true
                        }
                    },
                )

                // ── Captured Image Preview (image flow only) ──
                if (state.capturedImagePath != null) {
                    Spacer(Modifier.height(16.dp))
                    CapturedImagePreview(
                        imagePath = state.capturedImagePath!!,
                        isProcessing = state.isPreprocessing,
                    )
                }

                Spacer(Modifier.height(28.dp))
            }

            // ── Bottom CTA Bar (image flow only — PDF flow uses PdfPreviewScreen) ──
            if (state.hasScannedImage && !state.hasPdf) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                    color = Color.White,
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { viewModel.onAction(UploadInvoiceAction.StartAiExtraction) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.BlueGradientStart
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            enabled = !state.isPreprocessing && !state.isExtractingWithAi,
                        ) {
                            if (state.isExtractingWithAi) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    text = "Upload for AI Extraction",
                                    style = AppTypography.BodyPrimary.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Color.White,
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── PDF Compression Overlay ──
        if (state.isCompressingPdf) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.BlueGradientStart,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "Optimising your PDF…",
                            style = AppTypography.BodyPrimary.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = AppColors.TextPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "This usually takes a couple of seconds.",
                            style = AppTypography.Caption.copy(fontSize = 12.sp),
                            color = AppColors.TextTertiary,
                        )
                    }
                }
            }
        }
    }

    // ── Camera Permission Dialog ──
    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = { Text("Camera Access Needed") },
            text = { Text("Please allow camera access to scan invoices.") },
            confirmButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) { Text("OK") }
            },
        )
    }

    // ── Pre-Scan Tips Dialog ──
    if (showPreScanGuide) {
        InvoiceCaptureGuideDialog(
            isPreScan = true,
            onRetake = {
                showPreScanGuide = false
                viewModel.onAction(UploadInvoiceAction.ScanInvoice)
                imagePicker.openDocumentScanner(
                    onImagePicked = { path ->
                        if (onNavigateToCropReview != null) {
                            onNavigateToCropReview(path)
                        } else {
                            viewModel.onAction(UploadInvoiceAction.ImageCaptured(path))
                        }
                    },
                    onPermissionDenied = { showCameraPermissionDialog = true },
                )
            },
            onDismiss = { showPreScanGuide = false },
        )
    }

    // ── Capture Guide Dialog (hard block) ──
    if (state.showRetakeGuide) {
        InvoiceCaptureGuideDialog(
            onRetake = {
                viewModel.onAction(UploadInvoiceAction.DismissRetakeGuide)
                imagePicker.openDocumentScanner(
                    onImagePicked = { path ->
                        if (onNavigateToCropReview != null) {
                            onNavigateToCropReview(path)
                        } else {
                            viewModel.onAction(UploadInvoiceAction.ImageCaptured(path))
                        }
                    },
                    onPermissionDenied = { showCameraPermissionDialog = true },
                )
            },
            onDismiss = {
                viewModel.onAction(UploadInvoiceAction.DismissRetakeGuide)
                viewModel.onAction(UploadInvoiceAction.ProceedDespiteWarnings)
            },
        )
    }
}

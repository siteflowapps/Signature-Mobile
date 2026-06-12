package com.siteflow.signature.outlet.invoices.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.core.util.decodeToImageBitmap
import com.siteflow.signature.outlet.invoices.domain.UploadInvoiceAction
import com.siteflow.signature.outlet.invoices.domain.UploadInvoiceEvent
import com.siteflow.signature.outlet.invoices.domain.UploadInvoiceViewModel
import org.koin.compose.koinInject

/**
 * Post-pick preview for the PDF flow. Shows page-1 thumbnail + filename + page
 * count + final size, and offers Continue (→ AI extraction) or Remove.
 *
 * This screen replaces [InvoiceCropReviewScreen] for PDF inputs — crop/rotate
 * doesn't apply because PDFs come pre-paginated and we're rebuilding the file
 * from the original page geometry.
 */
@Composable
fun PdfPreviewScreen(
    onBack: () -> Unit,
    onNavigateToProcessing: () -> Unit,
    viewModel: UploadInvoiceViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val preview = state.pdfPreview

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                UploadInvoiceEvent.NavigateToProcessing -> onNavigateToProcessing()
                UploadInvoiceEvent.NavigateBack -> onBack()
                else -> Unit
            }
        }
    }

    // If we end up here without a PDF in state, pop back.
    LaunchedEffect(preview) {
        if (preview == null) onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Review your PDF",
                style = AppTypography.BodyPrimary.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = AppColors.TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "We'll send this to the AI to extract the invoice details.",
                style = AppTypography.Caption.copy(fontSize = 13.sp),
                color = AppColors.TextTertiary,
            )

            Spacer(Modifier.height(24.dp))

            if (preview != null) {
                // ── Thumbnail card ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (preview.thumbnailJpeg.isNotEmpty()) {
                                val bitmap = remember(preview.thumbnailJpeg) {
                                    runCatching { decodeToImageBitmap(preview.thumbnailJpeg) }.getOrNull()
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "PDF first page preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit,
                                    )
                                } else {
                                    PdfPlaceholderIcon()
                                }
                            } else {
                                PdfPlaceholderIcon()
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = AppColors.Danger,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preview.filename,
                                    style = AppTypography.BodyPrimary.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = AppColors.TextPrimary,
                                    maxLines = 1,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${preview.pageCount} page${if (preview.pageCount == 1) "" else "s"} · ${preview.finalKb} KB",
                                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                                    color = AppColors.TextTertiary,
                                )
                            }
                        }

                    }
                }
            }
        }

        // ── Bottom CTAs ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
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
                    enabled = preview != null && !state.isExtractingWithAi,
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

                OutlinedButton(
                    onClick = {
                        viewModel.onAction(UploadInvoiceAction.RemovePdf)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isExtractingWithAi,
                ) {
                    Text(
                        text = "Choose a different file",
                        style = AppTypography.BodyPrimary.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = AppColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PdfPlaceholderIcon() {
    Icon(
        imageVector = Icons.Default.PictureAsPdf,
        contentDescription = null,
        tint = AppColors.Danger.copy(alpha = 0.6f),
        modifier = Modifier.size(72.dp),
    )
}

package com.siteflow.cdo.outlet.invoices.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceAction
import com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceEvent
import com.siteflow.cdo.outlet.invoices.domain.UploadInvoiceViewModel
import com.siteflow.cdo.outlet.invoices.presentation.components.CapturedImagePreview
import com.siteflow.cdo.outlet.invoices.presentation.components.InvoiceHeaderCard
import com.siteflow.cdo.outlet.invoices.presentation.components.MatchedItemCard
import com.siteflow.cdo.outlet.invoices.presentation.components.MatchedSectionHeader
import com.siteflow.cdo.outlet.invoices.presentation.components.RejectedItemCard
import com.siteflow.cdo.outlet.invoices.presentation.components.RejectedSectionHeader
import com.siteflow.cdo.outlet.invoices.presentation.components.ZoomableImageViewer
import com.siteflow.cdo.outlet.invoices.presentation.components.formatPrice
import org.koin.compose.koinInject

/**
 * Invoice review screen — shown after AI extraction completes.
 *
 * Displays (all read-only):
 *  - Tappable image thumbnail (opens [ZoomableImageViewer])
 *  - Invoice header: number, date, distributor, retailer
 *  - Matched items (confidence ≥ 90%) — card per item with config chips
 *  - Rejected items (confidence < 90%) — red accent cards with rejection reason
 *  - Grand total in sticky bottom bar + "Confirm & Submit" CTA
 */
@Composable
fun InvoiceReviewScreen(
    onSubmitSuccess: () -> Unit,
    viewModel: UploadInvoiceViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var showZoomViewer by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                UploadInvoiceEvent.NavigateBack -> onSubmitSuccess()
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 10.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Cases + Amount stacked
                    val total = state.formData.invoiceSummary.grandTotal
                    val totalCases = state.formData.skuItems.sumOf { it.finalQuantity }

                    Column {
                        // Total Cases — highlighted green
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$totalCases",
                                style = AppTypography.TitleLarge.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF059669)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "cases",
                                style = AppTypography.Caption.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFF059669).copy(alpha = 0.7f)
                            )
                        }
                        // Invoice amount — secondary
                        if (total > 0) {
                            Text(
                                text = "₹${formatPrice(total)}",
                                style = AppTypography.Caption.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = AppColors.TextTertiary
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Button(
                        onClick = { viewModel.onAction(UploadInvoiceAction.ConfirmAndSubmit) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.BlueGradientStart
                        ),
                        enabled = !state.isSubmitting
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Confirm & Submit",
                                style = AppTypography.BodyPrimary.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Invoice image thumbnail ──
            state.capturedImagePath?.let { imagePath ->
                item(key = "image_preview") {
                    CapturedImagePreview(
                        imagePath = imagePath,
                        isProcessing = false,
                        modifier = Modifier.clickable { showZoomViewer = true }
                    )
                    if (state.formData.isAutoFilled) {
                        Spacer(Modifier.height(6.dp))
                        val matched = state.formData.skuItems.size
                        val rejected = state.formData.rejectedItems.size
                        Text(
                            text = "AI extracted ${matched + rejected} item(s) · $matched matched · Tap image to zoom",
                            style = AppTypography.Caption.copy(fontSize = 12.sp),
                            color = AppColors.TextTertiary
                        )
                    }
                }
            }

            // ── Invoice Header Card (read-only) ──
            item(key = "invoice_header") {
                InvoiceHeaderCard(
                    invoiceNumber = state.formData.invoiceNumber,
                    invoiceDate = state.formData.date,
                    distributorName = state.formData.distributorName,
                    retailerName = state.formData.billToName
                )
            }

            // ── Matched Items Section ──
            val matchedItems = state.formData.skuItems
            if (matchedItems.isNotEmpty()) {
                item(key = "matched_header") {
                    MatchedSectionHeader(count = matchedItems.size)
                }

                itemsIndexed(
                    items = matchedItems,
                    key = { _, item -> "matched_${item.id}" }
                ) { index, item ->
                    MatchedItemCard(
                        item = item,
                        index = index
                    )
                }
            }

            // ── Rejected Items Section ──
            val rejectedItems = state.formData.rejectedItems
            if (rejectedItems.isNotEmpty()) {
                item(key = "rejected_header") {
                    RejectedSectionHeader(count = rejectedItems.size)
                }

                itemsIndexed(
                    items = rejectedItems,
                    key = { _, item -> "rejected_${item.id}" }
                ) { _, item ->
                    RejectedItemCard(item = item)
                }
            }
        }
    }

    // Full-screen zoomable image viewer
    if (showZoomViewer) {
        state.capturedImagePath?.let { imagePath ->
            ZoomableImageViewer(
                imagePath = imagePath,
                onDismiss = { showZoomViewer = false }
            )
        }
    }
}

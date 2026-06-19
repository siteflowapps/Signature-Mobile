package com.siteflow.signature.outlet.invoices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.siteflow.signature.cso.invoices.data.InvoiceItem
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.outlet.invoices.data.InvoiceTimelineStep
import com.siteflow.signature.outlet.invoices.data.InvoiceTimelineSummary
import com.siteflow.signature.outlet.invoices.data.TimelineStepStatus
import com.siteflow.signature.outlet.invoices.data.buildTimelineFromStatus
import com.siteflow.signature.outlet.invoices.data.buildSummaryFromInvoice
import com.siteflow.signature.outlet.invoices.data.toSkuLineItem
import com.siteflow.signature.outlet.invoices.domain.OutletInvoiceAction
import com.siteflow.signature.outlet.invoices.domain.OutletInvoiceViewModel
import com.siteflow.signature.outlet.invoices.presentation.components.MatchedItemCard
import com.siteflow.signature.outlet.invoices.presentation.components.MatchedSectionHeader
import com.siteflow.signature.shared.components.PayoutEstimateCard
import com.siteflow.signature.shared.data.PayoutApi
import com.siteflow.signature.shared.data.PayoutCalculationDto
import org.koin.compose.koinInject

/**
 * Outlet Invoice Detail — Status Timeline screen with invoice data.
 * Shows: summary header, invoice stats, SKU breakdown, invoice photo,
 * status timeline, and help CTA.
 */
@Composable
fun OutletInvoiceDetailScreen(
    invoiceId: String,
    onBack: () -> Unit,
    viewModel: OutletInvoiceViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(invoiceId) {
        viewModel.onAction(OutletInvoiceAction.LoadInvoiceDetail(invoiceId))
    }

    val invoice = state.selectedInvoice

    // Full-screen image viewer state
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    if (invoice == null) {
        // Loading or not found
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Build timeline from real invoice status
    val summary = remember(invoice) { buildSummaryFromInvoice(invoice) }
    val timelineSteps = remember(invoice) { buildTimelineFromStatus(invoice) }

    // ── Fetch payout calculation ──
    val payoutApi: PayoutApi = koinInject()
    var payoutData by remember { mutableStateOf<PayoutCalculationDto?>(null) }
    var isPayoutLoading by remember { mutableStateOf(true) }

    LaunchedEffect(invoiceId) {
        isPayoutLoading = true
        val result = payoutApi.calculatePayout(invoiceId)
        when (result) {
            is com.siteflow.signature.core.data.networking.result.NetworkResult.Success -> {
                payoutData = result.data.data
            }
            is com.siteflow.signature.core.data.networking.result.NetworkResult.Error -> { /* silently ignore */ }
        }
        isPayoutLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Payout Eligible Badge ──
            if (summary.isPayoutEligible) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Payout Eligible",
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.Success,
                        modifier = Modifier
                            .background(Color(0xFFD1FAE5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Invoice Summary Header Card ──
            InvoiceSummaryCard(summary = summary)

            // ── Invoice Summary Stats ──
            InvoiceSummaryStats(invoice = invoice)
            Spacer(Modifier.height(14.dp))

            // ── Payout Estimate Card ──
            PayoutEstimateCard(
                payout = payoutData,
                isLoading = isPayoutLoading
            )
            Spacer(Modifier.height(14.dp))

            // ── SKU Line Items (Reusing Matched Item Card) ──
            if (invoice.lineItems.isNotEmpty()) {
                // Reuse the MatchedSectionHeader from InvoiceReviewComponents
                MatchedSectionHeader(count = invoice.lineItems.size)
                Spacer(Modifier.height(8.dp))

                // Reuse MatchedItemCard by converting InvoiceLineItem → SkuLineItem
                invoice.lineItems.forEachIndexed { index, item ->
                    MatchedItemCard(
                        item = item.toSkuLineItem(),
                        index = index
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Grand Total Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Cases",
                                style = AppTypography.Caption.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "Invoice Amt: ${invoice.invoiceAmount}",
                                style = AppTypography.Caption.copy(fontSize = 12.sp),
                                color = Color(0xFF059669).copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = "${invoice.totalCases} cases",
                            style = AppTypography.TitleMedium.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF059669)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
            }

            // ── Invoice Photo ──
            if (!invoice.invoiceImageUrl.isNullOrBlank()) {
                InvoicePhotoCard(
                    imageUrl = invoice.invoiceImageUrl!!,
                    onViewFullScreen = { selectedImageUrl = it }
                )
                Spacer(Modifier.height(14.dp))
            }

            // ── Title ──
            Text(
                text = "Status Timeline",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = summary.cyclePeriod,
                style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                color = AppColors.TextTertiary
            )

            Spacer(Modifier.height(24.dp))

            // ── Timeline Steps ──
            timelineSteps.forEachIndexed { index, step ->
                TimelineStepRow(
                    step = step,
                    isLast = index == timelineSteps.lastIndex
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Need Help Card ──
            NeedHelpCard()

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── Full-screen image viewer ──
    if (selectedImageUrl != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedImageUrl = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { selectedImageUrl = null }
            ) {
                AsyncImage(
                    model = selectedImageUrl,
                    contentDescription = "Full Screen Invoice",
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════
// Invoice Summary Stats (3-column: SKUs | Cases | Amount)
// ═══════════════════════════════════════════════════════════

@Composable
private fun InvoiceSummaryStats(invoice: InvoiceItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // SKU count
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${invoice.lineItemCount}",
                    style = AppTypography.TitleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = AppColors.BlueGradientStart
                )
                Text("SKUs", style = AppTypography.Caption.copy(fontSize = 11.sp), color = AppColors.TextTertiary)
            }

            Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFFE5E7EB)))

            // Total Cases — hero metric driving payout
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${invoice.totalCases}",
                    style = AppTypography.TitleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF059669)
                )
                Text("Cases", style = AppTypography.Caption.copy(fontSize = 11.sp), color = AppColors.TextTertiary)
            }

            Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFFE5E7EB)))

            // Invoice Amount — secondary reference
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = invoice.invoiceAmount,
                    style = AppTypography.TitleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = AppColors.TextSecondary
                )
                Text("Inv. Amount", style = AppTypography.Caption.copy(fontSize = 11.sp), color = AppColors.TextTertiary)
            }
        }
    }
}





// ═══════════════════════════════════════════════════════════
// Invoice Photo Card
// ═══════════════════════════════════════════════════════════

@Composable
private fun InvoicePhotoCard(
    imageUrl: String,
    onViewFullScreen: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Invoice Photo",
                style = AppTypography.TitleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onViewFullScreen(imageUrl) }
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Invoice Photo",
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp),
                    contentScale = ContentScale.Fit
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(30.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Tap to view full screen",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tap to view full size",
                style = AppTypography.Caption.copy(fontSize = 11.sp),
                color = AppColors.TextTertiary
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════
// Invoice Summary Header Card
// ═══════════════════════════════════════════════════════════

@Composable
private fun InvoiceSummaryCard(summary: InvoiceTimelineSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Invoice # + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                AppColors.BlueGradientStart.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = null,
                            tint = AppColors.BlueGradientStart,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = summary.invoiceNumber,
                            style = AppTypography.TitleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF111827)
                        )
                    }
                }

                Text(
                    text = summary.amount,
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF111827)
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            // Distributor + Date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Distributor — takes all available space, ellipsize if too long
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Business,
                        contentDescription = null,
                        tint = AppColors.TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = summary.distributorName,
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = AppColors.TextTertiary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Date — always on the right, never wraps
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = AppColors.TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = summary.submissionDate,
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = AppColors.TextTertiary,
                        softWrap = false
                    )
                }
            }

        }
    }
    Spacer(Modifier.height(14.dp))
}


// ═══════════════════════════════════════════════════════════
// Timeline Step Row
// ═══════════════════════════════════════════════════════════

@Composable
private fun TimelineStepRow(
    step: InvoiceTimelineStep,
    isLast: Boolean
) {
    val lineColor = when (step.status) {
        TimelineStepStatus.COMPLETED -> AppColors.Success.copy(alpha = 0.3f)
        TimelineStepStatus.PROCESSING -> AppColors.BlueGradientStart.copy(alpha = 0.3f)
        TimelineStepStatus.PENDING -> Color(0xFFE5E7EB)
    }
    val textAlpha = if (step.status == TimelineStepStatus.PENDING) 0.45f else 1f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(32.dp)
                .then(
                    if (!isLast) Modifier.drawBehind {
                        val dashEffect = if (step.status == TimelineStepStatus.PENDING) {
                            PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        } else null

                        drawLine(
                            color = lineColor,
                            start = Offset(size.width / 2, 28.dp.toPx()),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = dashEffect
                        )
                    } else Modifier
                )
        ) {
            when (step.status) {
                TimelineStepStatus.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = AppColors.Success,
                        modifier = Modifier.size(28.dp)
                    )
                }
                TimelineStepStatus.PROCESSING -> {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AppColors.BlueGradientStart.copy(alpha = 0.12f))
                            .border(2.dp, AppColors.BlueGradientStart, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            tint = AppColors.BlueGradientStart,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                TimelineStepStatus.PENDING -> {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6))
                            .border(1.5.dp, Color(0xFFD1D5DB), CircleShape)
                    )
                }
            }
        }

        val contentModifier = Modifier
            .weight(1f)
            .padding(bottom = if (isLast) 0.dp else 24.dp)

        if (step.isHighlighted) {
            Card(
                modifier = contentModifier,
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                TimelineStepContent(step, textAlpha, Modifier.padding(14.dp))
            }
        } else {
            TimelineStepContent(step, textAlpha, contentModifier)
        }
    }
}


// ═══════════════════════════════════════════════════════════
// Timeline Step Content
// ═══════════════════════════════════════════════════════════

@Composable
private fun TimelineStepContent(
    step: InvoiceTimelineStep,
    textAlpha: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = step.title,
                    style = AppTypography.TitleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF111827).copy(alpha = textAlpha)
                )
                if (step.statusLabel.isNotBlank() && step.status == TimelineStepStatus.PROCESSING) {
                    StatusBadge(label = step.statusLabel, status = step.status)
                }
            }
            if (step.date.isNotBlank()) {
                Text(
                    text = step.date,
                    style = AppTypography.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = AppColors.TextTertiary.copy(alpha = textAlpha)
                )
            }
        }

        if (step.subtitle.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = step.subtitle,
                style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                color = AppColors.TextTertiary.copy(alpha = textAlpha)
            )
        }

        if (step.statusLabel.isNotBlank() && step.status == TimelineStepStatus.COMPLETED) {
            Spacer(Modifier.height(6.dp))
            StatusBadge(label = step.statusLabel, status = step.status)
        }

        if (!step.description.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = step.description,
                style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                color = AppColors.TextTertiary.copy(alpha = textAlpha)
            )
        }

        if (step.details.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                step.details.forEach { (key, value) ->
                    Column {
                        Text(
                            text = key.uppercase(),
                            style = AppTypography.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = AppColors.TextTertiary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = value,
                            style = AppTypography.BodyPrimary.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = if (value.startsWith("+")) AppColors.Success
                            else Color(0xFF111827).copy(alpha = textAlpha)
                        )
                    }
                }
            }
        }

        if (step.progressValue != null) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { step.progressValue },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = AppColors.BlueGradientStart,
                trackColor = Color(0xFFE5E7EB)
            )
        }

        if (!step.badge.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.Success,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = step.badge,
                    style = AppTypography.BodyPrimary.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                    color = AppColors.Success
                )
            }
        }

        if (!step.rejectionReason.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = step.rejectionReason,
                    style = AppTypography.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = Color(0xFFDC2626)
                )
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════
// Status Badge
// ═══════════════════════════════════════════════════════════

@Composable
private fun StatusBadge(label: String, status: TimelineStepStatus) {
    val bgColor = when (status) {
        TimelineStepStatus.COMPLETED -> Color(0xFFD1FAE5)
        TimelineStepStatus.PROCESSING -> Color(0xFFCCFBF1)
        TimelineStepStatus.PENDING -> Color(0xFFF3F4F6)
    }
    val textColor = when (status) {
        TimelineStepStatus.COMPLETED -> AppColors.Success
        TimelineStepStatus.PROCESSING -> AppColors.BlueGradientStart
        TimelineStepStatus.PENDING -> AppColors.TextTertiary
    }

    Text(
        text = if (status == TimelineStepStatus.COMPLETED) label.uppercase() else label,
        style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
        color = textColor,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}


// ═══════════════════════════════════════════════════════════
// Payout Scheme Info Card
// ═══════════════════════════════════════════════════════════

@Composable
private fun PayoutSchemeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Text(
                text = "How Your Payout Works",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                ),
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Based on your monthly volume across all invoices",
                style = AppTypography.Caption.copy(fontSize = 11.sp),
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(16.dp))

            PayoutInfoRow(
                icon = Icons.Default.TrendingUp,
                iconBg = Color(0xFFF0FDFA),
                iconTint = Color(0xFF0D9488),
                title = "Volume-Based Slab",
                subtitle = "Your payout tier is determined by your cumulative monthly case volume."
            )

            Spacer(Modifier.height(14.dp))

            PayoutInfoRow(
                icon = Icons.Default.LocalAtm,
                iconBg = Color(0xFFF0FDF4),
                iconTint = Color(0xFF16A34A),
                title = "Fixed ₹ per Case",
                subtitle = "You earn a fixed rupee amount for every qualifying case delivered."
            )

            Spacer(Modifier.height(14.dp))

            PayoutInfoRow(
                icon = Icons.Default.Inventory2,
                iconBg = Color(0xFFFFF7ED),
                iconTint = Color(0xFFEA580C),
                title = "CSD, Juices & Energy Included",
                subtitle = "Payout is applicable on CSD, Juices and Energy drink categories."
            )
        }
    }
}

@Composable
private fun PayoutInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(iconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.BodyPrimary.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = AppTypography.Caption.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                ),
                color = Color(0xFF6B7280)
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════
// Need Help Card
// ═══════════════════════════════════════════════════════════

@Composable
private fun NeedHelpCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Need Help?",
                        style = AppTypography.TitleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF92400E)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "If you have questions about your eligibility status, please contact your Sales Executive.",
                        style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                        color = Color(0xFFB45309)
                    )
                }
            }
        }
    }
}

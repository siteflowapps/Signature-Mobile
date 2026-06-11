package com.siteflow.cdo.ase.invoices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.siteflow.cdo.ase.invoices.data.InvoiceItem
import com.siteflow.cdo.ase.invoices.data.InvoiceLineItem
import com.siteflow.cdo.ase.invoices.data.InvoiceStatus
import com.siteflow.cdo.ase.invoices.data.SlabQualification
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import com.siteflow.cdo.outlet.invoices.data.SkuLineItem
import com.siteflow.cdo.outlet.invoices.data.toSkuLineItem
import com.siteflow.cdo.outlet.invoices.presentation.components.MatchedItemCard
import com.siteflow.cdo.outlet.invoices.presentation.components.MatchedSectionHeader
import com.siteflow.cdo.shared.components.ApprovalTimelineView
import com.siteflow.cdo.shared.components.PayoutEstimateCard
import com.siteflow.cdo.shared.data.PayoutCalculationDto

/**
 * Shared invoice detail content.
 * Extracted so it can be used by both ASE Invoice Detail (pager and single view)
 * and ASM Invoice Detail.
 */
@Composable
    fun InvoiceDetailContent(
    invoice: InvoiceItem,
    showPeriodSection: Boolean,
    periodInvoices: List<InvoiceItem> = emptyList(),
    payoutData: PayoutCalculationDto? = null,
    isPayoutLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Full-screen image viewer state
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Invoice Header Card ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Row 1: Outlet name + status badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = invoice.outletName,
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF111827),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(invoice.status.bgColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = invoice.status.label,
                            style = AppTypography.Caption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = invoice.status.color
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Row 2a: Invoice # (left) + Date (right) — fixed, no wrapping
                if (invoice.invoiceNumber.isNotBlank() || invoice.invoicePeriod.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (invoice.invoiceNumber.isNotBlank()) {
                            MetadataChip(
                                text = "#${invoice.invoiceNumber}",
                                color = AppColors.BlueGradientStart
                            )
                        }
                        if (invoice.invoicePeriod.isNotBlank()) {
                            MetadataChip(
                                text = invoice.invoicePeriod,
                                color = AppColors.TextTertiary
                            )
                        }
                    }
                }

                // Row 2b: Distributor on its own row, full width, truncated
                if (invoice.distributorName.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    MetadataChip(
                        text = invoice.distributorName,
                        color = AppColors.TextSecondary,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFF3F4F6))
                Spacer(Modifier.height(14.dp))

                // Row 3: Total Cases (left) + Inv. Amount (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TOTAL CASES",
                            style = AppTypography.Caption.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = AppColors.TextTertiary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${invoice.totalCases} cases",
                            style = AppTypography.TitleMedium.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF059669)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "INV. AMOUNT",
                            style = AppTypography.Caption.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = AppColors.TextTertiary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = invoice.invoiceAmount,
                            style = AppTypography.TitleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF374151)
                        )
                    }
                }

            }
        }


        // ── Payout Estimate Card ──
        PayoutEstimateCard(
            payout = payoutData,
            isLoading = isPayoutLoading
        )

        // ── Invoice Summary Stats ──
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
                // SKUs count
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${invoice.lineItemCount}",
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.BlueGradientStart
                    )
                    Text(
                        text = "SKUs",
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color(0xFFE5E7EB))
                )

                // Total Cases — hero metric driving payout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${invoice.totalCases}",
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF059669)
                    )
                    Text(
                        text = "Cases",
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color(0xFFE5E7EB))
                )

                // Invoice Amount (secondary reference)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = invoice.invoiceAmount,
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = "Inv. Amount",
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary
                    )
                }
            }
        }

        // ── Period Invoices Section (non-pager mode only, if > 1) ──
        if (showPeriodSection && periodInvoices.size > 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Invoices for ${invoice.invoicePeriod}",
                            style = AppTypography.TitleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "${periodInvoices.size} invoices",
                            style = AppTypography.Caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = AppColors.TextTertiary
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    periodInvoices.forEachIndexed { index, periodInv ->
                        val isCurrent = periodInv.id == invoice.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isCurrent) Modifier.border(
                                        1.dp,
                                        AppColors.BlueGradientStart.copy(alpha = 0.3f),
                                        RoundedCornerShape(10.dp)
                                    ) else Modifier
                                )
                                .background(
                                    if (isCurrent) Color(0xFFEFF6FF) else Color(0xFFF9FAFB),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (periodInv.invoiceNumber.isNotBlank()) "#${periodInv.invoiceNumber}" else "#${periodInv.id}",
                                        style = AppTypography.BodyPrimary.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color(0xFF111827)
                                    )
                                    if (isCurrent) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    AppColors.BlueGradientStart,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "CURRENT",
                                                style = AppTypography.Caption.copy(
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(periodInv.status.color, CircleShape)
                                    )
                                    Text(
                                        text = "${periodInv.status.label}  •  ${periodInv.submittedTime}",
                                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                                        color = AppColors.TextTertiary
                                    )
                                }
                            }
                            Text(
                                text = periodInv.invoiceAmount,
                                style = AppTypography.BodyPrimary.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF111827)
                            )
                        }
                        if (index < periodInvoices.size - 1) {
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

//        // ── Slab Qualification Row ──
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(
//                    if (invoice.slabQualification == SlabQualification.MEETS)
//                        Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
//                    RoundedCornerShape(10.dp)
//                )
//                .padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Icon(
//                imageVector = if (invoice.slabQualification == SlabQualification.MEETS)
//                    Icons.Default.CheckCircle else Icons.Default.Warning,
//                contentDescription = null,
//                tint = invoice.slabQualification.color,
//                modifier = Modifier.size(18.dp)
//            )
//            Column {
//                Text(
//                    text = "${invoice.slabQualification.label} for ${invoice.slab.label}",
//                    style = AppTypography.BodyPrimary.copy(
//                        fontSize = 13.sp,
//                        fontWeight = FontWeight.SemiBold
//                    ),
//                    color = invoice.slabQualification.color
//                )
//                val casesSuffix = if (invoice.totalCases > 0) " · ${invoice.totalCases} cases" else ""
//                Text(
//                    text = "Based on total cases in this invoice$casesSuffix",
//                    style = AppTypography.Caption.copy(fontSize = 11.sp),
//                    color = AppColors.TextTertiary
//                )
//            }
//        }

        // ── SKU Line Items (Reusing Matched Item Card) ──
        if (invoice.lineItems.isNotEmpty()) {
            // Reuse the MatchedSectionHeader from InvoiceReviewComponents
            MatchedSectionHeader(count = invoice.lineItems.size)

            // Reuse MatchedItemCard by converting InvoiceLineItem → SkuLineItem
            invoice.lineItems.forEachIndexed { index, item ->
                MatchedItemCard(
                    item = item.toSkuLineItem(),
                    index = index
                )
            }

            // ── Grand Total Card ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Invoice Amount",
                        style = AppTypography.BodyPrimary.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF1E40AF)
                    )
                    Text(
                        text = invoice.invoiceAmount,
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1E40AF)
                    )
                }
            }
        }

        // ── Invoice Image ──
        if (!invoice.invoiceImageUrl.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Invoice Photo",
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF111827)
                    )

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedImageUrl = invoice.invoiceImageUrl }
                    ) {
                        AsyncImage(
                            model = invoice.invoiceImageUrl,
                            contentDescription = "Invoice Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 400.dp),
                            contentScale = ContentScale.Fit
                        )

                        // Zoom affordance
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

        // ── Review Note (if already reviewed) ──
        if (invoice.reviewNote != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        invoice.status.bgColor.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(y = 5.dp)
                        .background(invoice.status.color, CircleShape)
                )
                Column {
                    Text(
                        text = "Review Note",
                        style = AppTypography.Caption.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF374151)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = invoice.reviewNote,
                        style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                        color = Color(0xFF374151)
                    )
                }
            }
        }

        // ── Submitted By ──
        if (invoice.submittedByAse.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Submitted By",
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.4.sp
                        ),
                        color = AppColors.TextTertiary
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(AppColors.BlueGradientStart, AppColors.BlueGradientEnd)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = invoice.submittedByAse,
                                style = AppTypography.BodyPrimary.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                color = AppColors.black27
                            )
                            Text(
                                text = "Area Sales Executive",
                                style = AppTypography.Caption.copy(fontSize = 12.sp),
                                color = AppColors.TextTertiary
                            )
                        }
                    }
                }
            }
        }

        // ── Approval Timeline ──
        if (invoice.approvalTimeline.isNotEmpty()) {
            ApprovalTimelineView(steps = invoice.approvalTimeline)
        }

        // Bottom spacer for CTA clearance
        if (invoice.status == InvoiceStatus.SUBMITTED ||
            invoice.status == InvoiceStatus.ASE_APPROVED ||
            invoice.status == InvoiceStatus.PENDING) {
            Spacer(Modifier.height(8.dp))
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
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

/**
 * Small pill-shaped metadata chip for header info.
 */
@Composable
private fun MetadataChip(
    text: String,
    color: Color,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        style = AppTypography.Caption.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        ),
        color = color,
        maxLines = maxLines,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        modifier = Modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}


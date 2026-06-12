package com.siteflow.signature.shared.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.shared.data.PayoutCalculationDto

/**
 * Premium Monthly Payout Estimate Card.
 *
 * Shows the cumulative month-to-date payout estimate:
 *  • Monthly payout = totalMonthlyVolumePc × ratePerCase  (NOT per-invoice amount)
 *  • Slab tier achieved + animated progress toward next tier
 *  • Monthly cases accumulated across all invoices
 *
 * Since retailers upload multiple invoices per month and the slab is based
 * on cumulative volume, single-invoice figures would be misleading.
 */
@Composable
fun PayoutEstimateCard(
    payout: PayoutCalculationDto?,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (payout == null && !isLoading) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF059669)
                    )
                    Text(
                        text = "Calculating monthly payout…",
                        style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                        color = AppColors.TextTertiary
                    )
                }
            }
            return@Card
        }

        val data = payout ?: return@Card

        // ── Gradient header banner ─────────────────────────────────────────
        val gradientColors = if (data.isEligible)
            listOf(Color(0xFF064E3B), Color(0xFF065F46), Color(0xFF047857))
        else
            listOf(Color(0xFF374151), Color(0xFF4B5563))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(gradientColors),
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column {
                // Label row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Monthly Payout Estimate",
                            style = AppTypography.Caption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                    // Estimated badge
                    if (data.isEstimated == true) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "~ Estimated",
                                style = AppTypography.Caption.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Payout amount + slab tier badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        if (data.isEligible) {
                            // ── Hero: monthly payout = totalMonthlyVolumePc × rate ──
                            val monthlyPayout = data.monthlyEstimatedPayout
                                ?: data.calculatedPayoutAmount
                                ?: 0.0
                            Text(
                                text = "₹${formatAmount(monthlyPayout)}",
                                style = AppTypography.TitleMedium.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "${formatCases(data.totalMonthlyVolumePc ?: data.totalCases?.toDouble())} cases × ₹${formatRate(data.resolvedRatePerCase ?: 0.0)}/case",
                                style = AppTypography.Caption.copy(fontSize = 11.sp),
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        } else {
                            Text(
                                text = "Not Eligible",
                                style = AppTypography.TitleMedium.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Minimum 40 cases required",
                                style = AppTypography.Caption.copy(fontSize = 11.sp),
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                    // Tier badge
                    SlabTierBadgeLarge(data)
                }
            }
        }

        // ── Body ──────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {

            // Monthly volume stat pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MonthlyStatPill(
                    label = "Monthly Cases",
                    value = formatCases(data.totalMonthlyVolumePc ?: data.totalCases?.toDouble()),
                    accent = Color(0xFF059669),
                    modifier = Modifier.weight(1f)
                )
                MonthlyStatPill(
                    label = "Rate / Case",
                    value = data.resolvedRatePerCase?.let { "₹${formatRate(it)}" } ?: "—",
                    accent = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )
                if ((data.waterVolumePc ?: 0.0) > 0.0) {
                    MonthlyStatPill(
                        label = "Water Cases",
                        value = formatCases(data.waterVolumePc),
                        accent = Color(0xFF0EA5E9),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Slab progress bar (only when eligible)
            if (data.isEligible) {
                Spacer(Modifier.height(14.dp))
                SlabProgressSection(data)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(10.dp))

            // Context note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF7ED), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(13.dp).padding(top = 1.dp)
                )
                Text(
                    text = "Payout is based on your total monthly cases across all invoices — not just this one.",
                    style = AppTypography.Caption.copy(fontSize = 10.sp),
                    color = Color(0xFF92400E)
                )
            }
        }
    }
}

// ── Slab Progress ─────────────────────────────────────────────────────────────

@Composable
private fun SlabProgressSection(data: PayoutCalculationDto) {
    val monthlyVol = data.totalMonthlyVolumePc ?: data.totalCases?.toDouble() ?: 0.0
    val min = data.minQuantity ?: 40.0
    val max = data.maxQuantity ?: 300.0
    val progress = ((monthlyVol - min) / (max - min)).coerceIn(0.0, 1.0).toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "slabProgress"
    )

    val (barColor, nextLabel) = when (data.classification?.uppercase()) {
        "SILVER"   -> Color(0xFF94A3B8) to "Gold at ${data.maxQuantity?.toInt() ?: 80}+ cases"
        "GOLD"     -> Color(0xFFF59E0B) to "Diamond at ${data.maxQuantity?.toInt() ?: 149}+ cases"
        "DIAMOND"  -> Color(0xFF8B5CF6) to "Platinum at ${data.maxQuantity?.toInt() ?: 299}+ cases"
        "PLATINUM" -> Color(0xFF0EA5E9) to "Max tier achieved 🏆"
        else       -> AppColors.BlueGradientStart to ""
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Slab Progress",
                style = AppTypography.Caption.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                ),
                color = Color(0xFF6B7280)
            )
            Text(
                text = "${formatCases(monthlyVol)} / ${formatCases(max)} cases",
                style = AppTypography.Caption.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = barColor
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(listOf(barColor.copy(alpha = 0.7f), barColor))
                    )
            )
        }
        if (nextLabel.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = nextLabel,
                style = AppTypography.Caption.copy(fontSize = 9.sp),
                color = Color(0xFF9CA3AF)
            )
        }
    }
}

// ── Monthly Stat Pill ─────────────────────────────────────────────────────────

@Composable
private fun MonthlyStatPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(accent.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = AppTypography.TitleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            ),
            color = accent
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = AppTypography.Caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
            color = Color(0xFF6B7280),
            maxLines = 1
        )
    }
}

// ── Tier Badge ────────────────────────────────────────────────────────────────

@Composable
private fun SlabTierBadgeLarge(data: PayoutCalculationDto) {
    if (!data.isEligible) return
    val (bg, textColor) = when (data.classification?.uppercase()) {
        "SILVER"   -> Color(0xFFE2E8F0) to Color(0xFF334155)
        "GOLD"     -> Color(0xFFFEF3C7) to Color(0xFF78350F)
        "DIAMOND"  -> Color(0xFFEDE9FE) to Color(0xFF4C1D95)
        "PLATINUM" -> Color(0xFFE0F2FE) to Color(0xFF0C4A6E)
        else       -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
    }
    Column(
        modifier = Modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = data.tierEmoji, fontSize = 20.sp)
        Text(
            text = data.displayClassification,
            style = AppTypography.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatCases(v: Double?): String {
    if (v == null) return "—"
    return if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
}

private fun formatRate(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString()
    else {
        val whole = value.toLong()
        val frac = ((value - whole) * 100).toLong()
        "$whole.${frac.toString().padStart(2, '0')}"
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) addCommas(amount.toLong().toString())
    else {
        val whole = amount.toLong()
        val frac = ((amount - whole) * 100).toLong()
        "${addCommas(whole.toString())}.${frac.toString().padStart(2, '0')}"
    }
}

private fun addCommas(s: String): String {
    val reversed = s.reversed()
    return reversed.chunked(3).joinToString(",").reversed()
}

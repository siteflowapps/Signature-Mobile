package com.siteflow.signature.outlet.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.outlet.dashboard.data.OutletMonthlyPerformance

@Composable
fun OutletPerformanceCard(
    performance: OutletMonthlyPerformance,
    onUploadInvoice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // ── Title row with trend badge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "MONTHLY",
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "PERFORMANCE",
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = performance.month,
                        style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                        color = AppColors.TextTertiary
                    )
                }

                // Month-over-month trend badge
                if (performance.vsLastMonth != null) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (performance.vsLastMonthPositive) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (performance.vsLastMonthPositive) "↑" else "↓",
                                style = AppTypography.Caption.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (performance.vsLastMonthPositive) AppColors.Success else Color(0xFFDC2626)
                            )
                            Text(
                                text = performance.vsLastMonth,
                                style = AppTypography.Caption.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (performance.vsLastMonthPositive) AppColors.Success else Color(0xFFDC2626)
                            )
                            Text(
                                text = "vs last month",
                                style = AppTypography.Caption.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (performance.vsLastMonthPositive) Color(0xFF065F46) else Color(0xFF991B1B)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Forecast Target + Approved Amount ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Forecast Target",
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = AppColors.TextTertiary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = performance.forecastTarget,
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF111827)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Approved",
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = AppColors.TextTertiary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = performance.approvedAmount,
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.Success
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Invoice Breakdown Chips ──
            if (performance.totalInvoices > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InvoiceBreakdownChip(
                        emoji = "✅",
                        count = performance.approvedCount,
                        label = "Approved",
                        color = AppColors.Success
                    )
                    InvoiceBreakdownChip(
                        emoji = "⏳",
                        count = performance.pendingCount,
                        label = "Pending",
                        color = Color(0xFFF59E0B)
                    )
                    InvoiceBreakdownChip(
                        emoji = "❌",
                        count = performance.rejectedCount,
                        label = "Rejected",
                        color = Color(0xFFDC2626)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Upload Invoice Button ──
            Button(
                onClick = onUploadInvoice,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Upload Invoice",
                    style = AppTypography.BodyPrimary.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

/**
 * Small chip displaying an invoice status count.
 * e.g. "✅ 3 Approved"
 */
@Composable
private fun InvoiceBreakdownChip(
    emoji: String,
    count: Int,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Text(
            text = "$count",
            style = AppTypography.BodyPrimary.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = AppTypography.Caption.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = color.copy(alpha = 0.8f)
        )
    }
}

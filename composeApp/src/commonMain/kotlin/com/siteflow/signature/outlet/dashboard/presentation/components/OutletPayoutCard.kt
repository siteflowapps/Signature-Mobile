package com.siteflow.signature.outlet.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.outlet.dashboard.data.OutletPayoutSummary

@Composable
fun OutletPayoutCard(
    payout: OutletPayoutSummary,
    onTotalInvoicesClick: () -> Unit = {},
    onPendingInvoicesClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E40AF),
                            Color(0xFF3B82F6)
                        )
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // ── Last Payout Row ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Last Payout",
                            style = AppTypography.Caption.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = payout.lastPayoutAmount,
                            style = AppTypography.TitleMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        if (payout.lastPayoutDate != null) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Paid on ${payout.lastPayoutDate}",
                                style = AppTypography.Caption.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Wallet icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                Spacer(Modifier.height(16.dp))

                // ── Invoice Stat Tiles ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InvoiceStatTile(
                        icon = Icons.Default.Description,
                        count = payout.totalInvoices,
                        label = "Total\nInvoices",
                        tileColor = Color(0xFF3B82F6),
                        onClick = onTotalInvoicesClick,
                        modifier = Modifier.weight(1f)
                    )
                    InvoiceStatTile(
                        icon = Icons.Default.HourglassBottom,
                        count = payout.pendingInvoices,
                        label = "Pending\nInvoices",
                        tileColor = Color(0xFFF59E0B),
                        onClick = onPendingInvoicesClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceStatTile(
    icon: ImageVector,
    count: Int,
    label: String,
    tileColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tileColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tileColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = "$count",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = label,
                    style = AppTypography.Caption.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    ),
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

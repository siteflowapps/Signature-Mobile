package com.siteflow.cdo.outlet.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.ase.invoices.data.InvoiceStatus
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import com.siteflow.cdo.outlet.dashboard.data.OutletRecentInvoice

@Composable
fun OutletRecentInvoiceRow(invoice: OutletRecentInvoice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        invoice.accentColor.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = invoice.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Title + subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.title,
                    style = AppTypography.BodyPrimary.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = invoice.subtitle,
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = AppColors.TextTertiary
                )
            }

            // Amount + Status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = invoice.amount,
                    style = AppTypography.BodyPrimary.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(4.dp))
                InvoiceStatusBadge(status = invoice.status)
            }
        }
    }
}

@Composable
private fun InvoiceStatusBadge(status: InvoiceStatus) {
    val (bgColor, textColor, label) = when (status) {
        InvoiceStatus.SUBMITTED, InvoiceStatus.PENDING -> Triple(Color(0xFFFEF3C7), Color(0xFFF59E0B), "Pending")
        InvoiceStatus.ASE_APPROVED -> Triple(Color(0xFFDBEAFE), AppColors.BlueGradientStart, "ASE Approved")
        InvoiceStatus.ASM_APPROVED -> Triple(Color(0xFFDBEAFE), AppColors.BlueGradientStart, "ASM Approved")
        InvoiceStatus.FINANCE_APPROVED -> Triple(Color(0xFFD1FAE5), AppColors.Success, "Approved")
        InvoiceStatus.PAID -> Triple(Color(0xFFD1FAE5), AppColors.Success, "Paid")
        InvoiceStatus.APPROVED -> Triple(Color(0xFFD1FAE5), AppColors.Success, "Approved")
        InvoiceStatus.REJECTED -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "Rejected")
        InvoiceStatus.CALCULATED -> Triple(Color(0xFFF3F4F6), AppColors.TextTertiary, "Calculated")
    }

    Text(
        text = label,
        style = AppTypography.Caption.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        ),
        color = textColor,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

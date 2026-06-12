package com.siteflow.signature.outlet.invoices.presentation.components

import androidx.compose.foundation.layout.*
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
import com.siteflow.signature.outlet.invoices.data.SkuLineItem

@Composable
fun SkuItemRow(item: SkuLineItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                style = AppTypography.BodyPrimary.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Qty: ${item.quantity}",
                style = AppTypography.Caption.copy(fontSize = 12.sp),
                color = AppColors.TextTertiary
            )
        }

        Text(
            text = "₹${formatWithCommas(item.totalPrice.toInt())}",
            style = AppTypography.BodyPrimary.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF111827)
        )
    }
}

/**
 * KMP-compatible comma-separated number formatting (e.g. 5000 → "5,000").
 */
private fun formatWithCommas(number: Int): String {
    val str = number.toString()
    val result = StringBuilder()
    var count = 0
    for (i in str.length - 1 downTo 0) {
        if (count > 0 && count % 3 == 0) result.insert(0, ',')
        result.insert(0, str[i])
        count++
    }
    return result.toString()
}

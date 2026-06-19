package com.siteflow.signature.outlet.invoices.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.outlet.invoices.data.SkuLineItem

// ═══════════════════════════════════════════════════════════════
// Invoice Header Card — read-only metadata
// ═══════════════════════════════════════════════════════════════

@Composable
fun InvoiceHeaderCard(
    invoiceNumber: String,
    invoiceDate: String,
    distributorName: String,
    retailerName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Invoice Details",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF111827)
                )
            }

            Spacer(Modifier.height(18.dp))

            // Row 1: Invoice # and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetadataLabel(
                    icon = Icons.Default.Receipt,
                    label = "Invoice #",
                    value = invoiceNumber,
                    modifier = Modifier.weight(1f)
                )
                MetadataLabel(
                    icon = Icons.Default.CalendarMonth,
                    label = "Date",
                    value = formatDisplayDate(invoiceDate),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Row 2: Distributor and Retailer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetadataLabel(
                    icon = Icons.Default.LocalShipping,
                    label = "Distributor",
                    value = distributorName,
                    modifier = Modifier.weight(1f)
                )
                MetadataLabel(
                    icon = Icons.Default.Store,
                    label = "Retailer",
                    value = retailerName,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetadataLabel(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = AppTypography.Caption.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextTertiary
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = value.ifBlank { "—" },
            style = AppTypography.BodyPrimary.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color(0xFF111827),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Section Headers — Matched / Rejected
// ═══════════════════════════════════════════════════════════════

@Composable
fun MatchedSectionHeader(count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = AppColors.Success,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Matched Items",
            style = AppTypography.TitleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF111827)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "($count)",
            style = AppTypography.Caption.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = AppColors.TextTertiary
        )
    }
}

@Composable
fun RejectedSectionHeader(count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = null,
            tint = Color(0xFFDC2626),
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Rejected Items",
            style = AppTypography.TitleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF111827)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "($count)",
            style = AppTypography.Caption.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color(0xFFDC2626)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Matched Item Card
// ═══════════════════════════════════════════════════════════════

@Composable
fun MatchedItemCard(
    item: SkuLineItem,
    index: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ── Header: matched SKU name + match-% badge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.matchedSkuName ?: item.productName,
                        style = AppTypography.BodyPrimary.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF111827)
                    )
                    if (item.invoicedSkuName.isNotBlank() &&
                        item.invoicedSkuName != item.matchedSkuName
                    ) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Invoiced: ${item.invoicedSkuName}",
                            style = AppTypography.Caption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = AppColors.TextTertiary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                ConfidenceBadge(item.confidence)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(Modifier.height(12.dp))

            // ── Price-first footer: qty / rate (secondary) + amount (hero) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${item.finalQuantity} ${item.finalUnit}",
                        style = AppTypography.BodyPrimary.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF111827)
                    )
                    if (item.pricePerUnit > 0) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "₹${formatPrice(item.pricePerUnit)} / ${item.finalUnit.lowercase()}",
                            style = AppTypography.Caption.copy(fontSize = 11.sp),
                            color = AppColors.TextTertiary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Amount",
                        style = AppTypography.Caption.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.3.sp
                        ),
                        color = AppColors.TextTertiary
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = "₹${formatPrice(item.totalPrice)}",
                        style = AppTypography.TitleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.Primary
                    )
                }
            }

            // ── MRP reference chips (shown only once the backend sends MRP) ──
            if (item.mrpPerCase != null || item.mrpPerBottle != null || item.caseConfiguration != null) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.mrpPerCase?.let { ConfigChip(text = "MRP/case ₹${formatPrice(it)}") }
                    item.mrpPerBottle?.let { ConfigChip(text = "MRP/pc ₹${formatPrice(it)}") }
                    item.caseConfiguration?.let { ConfigChip(text = "$it/case") }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Rejected Item Card — red accent
// ═══════════════════════════════════════════════════════════════

@Composable
fun RejectedItemCard(
    item: SkuLineItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Red accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFDC2626), RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // SKU name
                Text(
                    text = item.invoicedSkuName,
                    style = AppTypography.BodyPrimary.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF991B1B)
                )

                Spacer(Modifier.height(8.dp))

                // Rejection reason
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = item.lowConfidenceReason ?: "Low confidence match",
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFFDC2626)
                    )
                }

                // Show invoiced quantity info
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ConfigChip(
                        text = "Qty: ${item.quantity} ${item.unit}",
                        chipColor = Color(0xFFFEE2E2),
                        textColor = Color(0xFF991B1B)
                    )
                    if (item.totalPrice > 0) {
                        ConfigChip(
                            text = "₹${formatPrice(item.totalPrice)}",
                            chipColor = Color(0xFFFEE2E2),
                            textColor = Color(0xFF991B1B)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Confidence Badge — green for ≥90%, red for <90%
// ═══════════════════════════════════════════════════════════════

@Composable
fun ConfidenceBadge(confidence: Int, modifier: Modifier = Modifier) {
    val isHigh = confidence >= 90
    val bgColor = if (isHigh) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
    val textColor = if (isHigh) Color(0xFF065F46) else Color(0xFF991B1B)

    Text(
        text = "$confidence%",
        style = AppTypography.Caption.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        ),
        color = textColor,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

// ═══════════════════════════════════════════════════════════════
// Config Chip — "24/case", "₹20/pc", "2 CASE"
// ═══════════════════════════════════════════════════════════════

@Composable
fun ConfigChip(
    text: String,
    isPrimary: Boolean = false,
    chipColor: Color = if (isPrimary) Color(0xFFCCFBF1) else Color(0xFFF3F4F6),
    textColor: Color = if (isPrimary) Color(0xFF0F766E) else Color(0xFF4B5563),
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = AppTypography.Caption.copy(
            fontSize = 11.sp,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.SemiBold
        ),
        color = textColor,
        modifier = modifier
            .background(chipColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

// ═══════════════════════════════════════════════════════════════
// Helpers
// ═══════════════════════════════════════════════════════════════

/** Format price: show as integer if whole number, else 2 decimal places */
fun formatPrice(price: Double): String {
    return if (price == kotlin.math.floor(price) && price < 1_000_000) {
        price.toLong().toString()
    } else {
        ((price * 100).toLong() / 100.0).toString()
    }
}

/** Format date: "2026-02-02" → "02 Feb 2026" */
private fun formatDisplayDate(dateStr: String): String {
    if (dateStr.isBlank()) return "—"
    val monthNames = listOf(
        "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    return try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val day = parts[2]
            val month = monthNames.getOrElse(parts[1].toInt()) { parts[1] }
            val year = parts[0]
            "$day $month $year"
        } else dateStr
    } catch (_: Exception) { dateStr }
}

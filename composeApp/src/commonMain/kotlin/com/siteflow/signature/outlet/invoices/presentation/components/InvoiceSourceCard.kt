package com.siteflow.signature.outlet.invoices.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography

/**
 * Full-width horizontal capture-source card.
 *
 * Two visual variants:
 *  - [recommended] = true  → filled brand-blue tinted card, used for Gallery + PDF.
 *  - [recommended] = false → outlined white card, used for Camera.
 *
 * Camera capture is intentionally de-emphasized because in-app photos of paper
 * invoices often come out pixelated or misaligned vs. a pre-saved file.
 */
@Composable
fun InvoiceSourceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    recommended: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val accent = AppColors.BlueGradientStart
    val containerColor = if (recommended) Color(0xFFF0FDFA) else Color.White
    val borderColor = if (recommended) accent.copy(alpha = 0.25f) else Color(0xFFE5E7EB)
    val iconBg = if (recommended) accent.copy(alpha = 0.15f) else Color(0xFFF3F4F6)
    val iconTint = if (recommended) accent else Color(0xFF6B7280)
    val titleColor = if (recommended) accent else Color(0xFF111827)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.6f)
            .clickable(enabled = enabled, onClick = onClick)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (recommended) 0.dp else 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTypography.BodyPrimary.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = titleColor,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = AppColors.TextTertiary,
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (recommended) accent else Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Small uppercase section header — "Recommended" / "Other options".
 */
@Composable
fun CaptureSectionHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = AppTypography.Caption.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
        ),
        color = AppColors.TextTertiary,
        modifier = modifier,
    )
}

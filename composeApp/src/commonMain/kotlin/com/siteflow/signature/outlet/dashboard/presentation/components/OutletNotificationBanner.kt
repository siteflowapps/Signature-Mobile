package com.siteflow.signature.outlet.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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
import com.siteflow.signature.outlet.dashboard.data.NotificationType
import com.siteflow.signature.outlet.dashboard.data.OutletNotification

@Composable
fun OutletNotificationBanner(
    notification: OutletNotification,
    onDismiss: () -> Unit
) {
    val (bgColor, iconTint, textColor) = when (notification.type) {
        NotificationType.ERROR -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), Color(0xFF991B1B))
        NotificationType.WARNING -> Triple(Color(0xFFFEF3C7), Color(0xFFF59E0B), Color(0xFF92400E))
        NotificationType.SUCCESS -> Triple(Color(0xFFD1FAE5), AppColors.Success, Color(0xFF065F46))
        NotificationType.INFO -> Triple(Color(0xFFDBEAFE), AppColors.BlueGradientStart, Color(0xFF1E40AF))
    }
    val icon = when (notification.type) {
        NotificationType.ERROR -> Icons.Filled.Error
        NotificationType.WARNING -> Icons.Filled.Warning
        NotificationType.SUCCESS -> Icons.Filled.Info
        NotificationType.INFO -> Icons.Filled.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = notification.message,
                style = AppTypography.BodyPrimary.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

package com.siteflow.cdo.core.presentation.components.state

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography

/**
 * Standard empty state used across all list screens.
 *
 * @param title         Primary message, e.g. "No invoices yet"
 * @param subtitle      Supporting detail, e.g. "Invoices will appear once uploaded"
 * @param icon          Icon to display above the text. Required.
 * @param iconTint      Tint for the icon. Defaults to a neutral grey.
 * @param horizontalPadding  Side padding around the subtitle for narrow text columns.
 */
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = Color(0xFFD1D5DB),
    horizontalPadding: Dp = 32.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = AppTypography.TitleMedium.copy(fontSize = 16.sp),
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = AppTypography.Caption.copy(fontSize = 13.sp),
            color = AppColors.TextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = horizontalPadding)
        )
    }
}

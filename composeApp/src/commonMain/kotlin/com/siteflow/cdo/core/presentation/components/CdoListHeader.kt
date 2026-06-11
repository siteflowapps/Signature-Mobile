package com.siteflow.cdo.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography

/**
 * Standardised list section header row.
 *
 * Example output:
 *   OUTLETS (12)                       Latest first
 *
 * @param label    Primary label, typically uppercased with count — e.g. "OUTLETS (12)".
 * @param trailing Optional trailing label — e.g. "Latest first". Pass null to hide.
 */
@Composable
fun CdoListHeader(
    label: String,
    trailing: String? = "Latest first",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AppTypography.Caption.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = AppColors.TextTertiary
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = AppTypography.Caption.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextTertiary
            )
        }
    }
}

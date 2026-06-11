package com.siteflow.cdo.core.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography

/**
 * Horizontally scrollable filter chip row used across all list screens.
 *
 * @param filters     Ordered list of filter label strings.
 * @param selectedFilter  Currently selected filter.
 * @param countForFilter  Lambda that returns the count for a given filter label.
 * @param onFilterSelected  Callback when a chip is tapped.
 * @param showCount   Whether to show the "(n)" count suffix. Default true.
 */
@Composable
fun CdoFilterChipRow(
    filters: List<String>,
    selectedFilter: String,
    countForFilter: (String) -> Int,
    onFilterSelected: (String) -> Unit,
    showCount: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            val label = if (showCount) "$filter (${countForFilter(filter)})" else filter

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = label,
                        style = AppTypography.Caption.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = Color(0xFF1F2937),
                    labelColor = AppColors.TextSecondary,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color(0xFFE5E7EB),
                    selectedBorderColor = Color(0xFF1F2937),
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

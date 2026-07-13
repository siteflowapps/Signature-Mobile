package com.siteflow.signature.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography

/**
 * Horizontally scrollable filter chip row used across all list screens.
 * Automatically scrolls to reveal the selected chip when selection changes
 * (including on back-navigation when the selected filter is restored).
 */
@Composable
fun SignatureFilterChipRow(
    filters: List<String>,
    selectedFilter: String,
    countForFilter: (String) -> Int,
    onFilterSelected: (String) -> Unit,
    showCount: Boolean = true,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Smooth-scroll to reveal the selected chip whenever selection changes
    LaunchedEffect(selectedFilter) {
        val index = filters.indexOf(selectedFilter)
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(filters) { _, filter ->
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

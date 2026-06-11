package com.siteflow.cdo.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reusable dropdown selector for the CDO app.
 * Supports label, required indicator, and custom placeholder.
 *
 * NOTE: Uses deferred dismissal (delay) to work around a Compose Multiplatform
 * iOS crash in DropdownMenu popup scene layer disposal (NoSuchElementException
 * in SortedSet.remove during layout node detachment).
 */
@Composable
fun CdoDropdown(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    placeholder: String = "Select an option"
) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        // Label
        if (label != null) {
            Row {
                Text(
                    text = label,
                    style = AppTypography.TitleMedium.copy(fontSize = 14.sp),
                    color = Color(0xFF111827)
                )
                if (required) {
                    Text(
                        text = " *",
                        style = AppTypography.TitleMedium.copy(fontSize = 14.sp),
                        color = AppColors.Danger
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected.ifEmpty { placeholder },
                    style = AppTypography.BodyPrimary.copy(fontSize = 15.sp),
                    color = if (selected.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF111827),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(22.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    scope.launch {
                        delay(50)
                        expanded = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                                color = if (option == selected) AppColors.BlueGradientStart
                                else Color(0xFF374151)
                            )
                        },
                        onClick = {
                            onSelected(option)
                            scope.launch {
                                delay(50)
                                expanded = false
                            }
                        }
                    )
                }
            }
        }
    }
}


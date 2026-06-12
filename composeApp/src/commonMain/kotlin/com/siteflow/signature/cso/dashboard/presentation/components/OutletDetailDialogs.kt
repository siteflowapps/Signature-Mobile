package com.siteflow.signature.cso.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.siteflow.signature.cso.onboarding.data.PhotoSlot
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PhotoViewerDialog(photo: PhotoSlot, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.CardBackground,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = photo.label,
                style = AppTypography.TitleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (photo.imagePath != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = photo.imagePath,
                            contentDescription = photo.label,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(AppColors.greyF6, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = AppColors.PlaceholderTextColor,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Photo preview",
                                style = AppTypography.Caption.copy(fontSize = 12.sp),
                                color = AppColors.TextTertiary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (photo.required) "Required photo" else "Optional photo",
                    style = AppTypography.Caption.copy(fontSize = 11.sp),
                    color = AppColors.TextTertiary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    color = AppColors.BlueGradientStart,
                    style = AppTypography.Button
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (dmsOutletId: String, coolerSerialNumber: String, isPrimary: Boolean) -> Unit
) {
    var dmsOutletId by remember { mutableStateOf("") }
    var coolerSerialNumber by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isValid = dmsOutletId.isNotBlank() && coolerSerialNumber.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.CardBackground,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Submit Compliance",
                style = AppTypography.TitleLarge,
                color = AppColors.TextPrimary
            )

            Text(
                text = "Enter the DMS outlet ID and cooler details to complete compliance.",
                style = AppTypography.BodySecondary,
                color = AppColors.TextSecondary
            )

            // ── DMS Outlet ID ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DMS Outlet ID",
                        style = AppTypography.Caption.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = " *",
                        style = AppTypography.Caption.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = AppColors.Danger
                    )
                }
                OutlinedTextField(
                    value = dmsOutletId,
                    onValueChange = { dmsOutletId = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., DMS-MH-10042", color = AppColors.PlaceholderTextColor) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.BlueGradientStart,
                        unfocusedBorderColor = AppColors.Divider
                    ),
                    textStyle = AppTypography.BodyPrimary
                )
            }

            // ── Cooler Serial Number ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Cooler Serial Number",
                        style = AppTypography.Caption.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = " *",
                        style = AppTypography.Caption.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = AppColors.Danger
                    )
                }
                OutlinedTextField(
                    value = coolerSerialNumber,
                    onValueChange = { coolerSerialNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., CLR-MH-2026-00042", color = AppColors.PlaceholderTextColor) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.BlueGradientStart,
                        unfocusedBorderColor = AppColors.Divider
                    ),
                    textStyle = AppTypography.BodyPrimary
                )
            }

            // ── Primary Outlet Toggle ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Primary Outlet",
                        style = AppTypography.BodyPrimary.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "Mark as primary distribution point",
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary
                    )
                }
                Switch(
                    checked = isPrimary,
                    onCheckedChange = { isPrimary = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = AppColors.BlueGradientStart,
                        checkedThumbColor = Color.White
                    )
                )
            }

            // ── Submit Button ──
            Button(
                onClick = { if (isValid) onSubmit(dmsOutletId, coolerSerialNumber, isPrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.BlueGradientStart,
                    disabledContainerColor = AppColors.Divider
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Submit Compliance",
                    style = AppTypography.BodyPrimary.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetRequestBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (coolerType: String, capacity: String, signageType: String, dmsId: String) -> Unit
) {
    val coolerTypes = listOf("Campa", "Campa Energy", "Suncrush")
    val capacityMap = mapOf(
        "Campa" to listOf("14 Caser", "20 Caser", "30 Caser"),
        "Campa Energy" to listOf("6 Caser"),
        "Suncrush" to listOf("6 Caser")
    )
    val signageTypes = listOf("Glow Sign", "Flex", "LED Board", "Painted", "Other")

    var coolerType by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var signageType by remember { mutableStateOf("") }
    var dmsId by remember { mutableStateOf("") }

    var coolerTypeExpanded by remember { mutableStateOf(false) }
    var capacityExpanded by remember { mutableStateOf(false) }
    var signageTypeExpanded by remember { mutableStateOf(false) }
    val ddScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isValid = coolerType.isNotBlank() && capacity.isNotBlank() && signageType.isNotBlank() && dmsId.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.CardBackground,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Request Asset",
                style = AppTypography.TitleLarge,
                color = AppColors.TextPrimary
            )

            Text(
                text = "Provide cooler and signage details to raise an asset request for this outlet.",
                style = AppTypography.BodySecondary,
                color = AppColors.TextSecondary
            )

            // ── DMS ID (placed first so keyboard doesn't overlap) ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DMS ID",
                        style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = " *",
                        style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = AppColors.Danger
                    )
                }
                OutlinedTextField(
                    value = dmsId,
                    onValueChange = { dmsId = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., DMS-12345", color = AppColors.PlaceholderTextColor) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.BlueGradientStart,
                        unfocusedBorderColor = AppColors.Divider
                    ),
                    textStyle = AppTypography.BodyPrimary
                )
            }

            // ── Cooler Type Dropdown ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Cooler Type",
                        style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = " *",
                        style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = AppColors.Danger
                    )
                }
                ExposedDropdownMenuBox(
                    expanded = coolerTypeExpanded,
                    onExpandedChange = { coolerTypeExpanded = !coolerTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = coolerType,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        placeholder = { Text("Select cooler type", color = AppColors.PlaceholderTextColor) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = coolerTypeExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.BlueGradientStart,
                            unfocusedBorderColor = AppColors.Divider
                        ),
                        textStyle = AppTypography.BodyPrimary
                    )
                    ExposedDropdownMenu(
                        expanded = coolerTypeExpanded,
                        onDismissRequest = { ddScope.launch { delay(50); coolerTypeExpanded = false } }
                    ) {
                        coolerTypes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, style = AppTypography.BodyPrimary) },
                                onClick = {
                                    coolerType = option
                                    capacity = "" // reset capacity when cooler type changes
                                    ddScope.launch { delay(50); coolerTypeExpanded = false }
                                }
                            )
                        }
                    }
                }
            }

            // ── Capacity Dropdown ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Capacity",
                        style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = " *",
                        style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = AppColors.Danger
                    )
                }
                val capacities = capacityMap[coolerType] ?: emptyList()
                ExposedDropdownMenuBox(
                    expanded = capacityExpanded,
                    onExpandedChange = { if (capacities.isNotEmpty()) capacityExpanded = !capacityExpanded }
                ) {
                    OutlinedTextField(
                        value = capacity,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        placeholder = {
                            Text(
                                if (coolerType.isBlank()) "Select cooler type first"
                                else "Select capacity",
                                color = AppColors.PlaceholderTextColor
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = capacityExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.BlueGradientStart,
                            unfocusedBorderColor = AppColors.Divider
                        ),
                        textStyle = AppTypography.BodyPrimary,
                        enabled = coolerType.isNotBlank()
                    )
                    ExposedDropdownMenu(
                        expanded = capacityExpanded,
                        onDismissRequest = { ddScope.launch { delay(50); capacityExpanded = false } }
                    ) {
                        capacities.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, style = AppTypography.BodyPrimary) },
                                onClick = { capacity = option; ddScope.launch { delay(50); capacityExpanded = false } }
                            )
                        }
                    }
                }
            }

            // ── Signage Type Dropdown ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Signage Type",
                        style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = " *",
                        style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = AppColors.Danger
                    )
                }
                ExposedDropdownMenuBox(
                    expanded = signageTypeExpanded,
                    onExpandedChange = { signageTypeExpanded = !signageTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = signageType,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        placeholder = { Text("Select signage type", color = AppColors.PlaceholderTextColor) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = signageTypeExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.BlueGradientStart,
                            unfocusedBorderColor = AppColors.Divider
                        ),
                        textStyle = AppTypography.BodyPrimary
                    )
                    ExposedDropdownMenu(
                        expanded = signageTypeExpanded,
                        onDismissRequest = { ddScope.launch { delay(50); signageTypeExpanded = false } }
                    ) {
                        signageTypes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, style = AppTypography.BodyPrimary) },
                                onClick = { signageType = option; ddScope.launch { delay(50); signageTypeExpanded = false } }
                            )
                        }
                    }
                }
            }

            // ── Submit Button ──
            Button(
                onClick = { if (isValid) onSubmit(coolerType, capacity, signageType, dmsId) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.BlueGradientStart,
                    disabledContainerColor = AppColors.Divider
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Submit Request",
                    style = AppTypography.BodyPrimary.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

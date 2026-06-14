package com.siteflow.signature.cso.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.siteflow.signature.cso.onboarding.data.dto.CoolerSizeOption
import com.siteflow.signature.cso.onboarding.data.dto.MarketingAssetType
import com.siteflow.signature.cso.onboarding.data.dto.MarketingItemDto
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
    onSubmit: (coolerSize: String, quantity: Int, details: String) -> Unit
) {
    val sizeOptions = CoolerSizeOption.entries

    var selectedSize by remember { mutableStateOf<CoolerSizeOption?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var details by remember { mutableStateOf("") }

    var sizeExpanded by remember { mutableStateOf(false) }
    val ddScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val qty = quantity.toIntOrNull() ?: 0
    val isValid = selectedSize != null && qty > 0

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
                text = "Request Cooler",
                style = AppTypography.TitleLarge,
                color = AppColors.TextPrimary
            )

            Text(
                text = "Select the cooler size and quantity to raise a request for this outlet.",
                style = AppTypography.BodySecondary,
                color = AppColors.TextSecondary
            )

            // ── Cooler Size Dropdown ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Cooler Size",
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
                    expanded = sizeExpanded,
                    onExpandedChange = { sizeExpanded = !sizeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSize?.label ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        placeholder = { Text("Select cooler size", color = AppColors.PlaceholderTextColor) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sizeExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.BlueGradientStart,
                            unfocusedBorderColor = AppColors.Divider
                        ),
                        textStyle = AppTypography.BodyPrimary
                    )
                    ExposedDropdownMenu(
                        expanded = sizeExpanded,
                        onDismissRequest = { ddScope.launch { delay(50); sizeExpanded = false } }
                    ) {
                        sizeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label, style = AppTypography.BodyPrimary) },
                                onClick = {
                                    selectedSize = option
                                    ddScope.launch { delay(50); sizeExpanded = false }
                                }
                            )
                        }
                    }
                }
            }

            // ── Quantity ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Quantity",
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
                    value = quantity,
                    onValueChange = { input -> quantity = input.filter { it.isDigit() }.take(3) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., 1", color = AppColors.PlaceholderTextColor) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.BlueGradientStart,
                        unfocusedBorderColor = AppColors.Divider
                    ),
                    textStyle = AppTypography.BodyPrimary
                )
            }

            // ── Details (optional) ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Notes (optional)",
                    style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                    color = AppColors.TextSecondary
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Any specific notes for this request", color = AppColors.PlaceholderTextColor) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.BlueGradientStart,
                        unfocusedBorderColor = AppColors.Divider
                    ),
                    textStyle = AppTypography.BodyPrimary
                )
            }

            // ── Submit Button ──
            Button(
                onClick = { selectedSize?.let { if (isValid) onSubmit(it.backendValue, qty, details) } },
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

/** Mutable working row for the branding request form (one asset item). */
private class MarketingItemDraft(
    type: MarketingAssetType? = null,
    quantity: String = "1",
    brands: String = ""
) {
    var type by mutableStateOf(type)
    var quantity by mutableStateOf(quantity)
    var brands by mutableStateOf(brands)

    val qtyInt: Int get() = quantity.toIntOrNull() ?: 0
    val isValid: Boolean get() = type != null && qtyInt > 0

    fun toDto(): MarketingItemDto = MarketingItemDto(
        assetType = type!!.backendValue,
        quantity = qtyInt,
        brands = brands.split(",").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { null }
    )
}

/**
 * Multi-item branding/marketing request form. The CSO can add several asset
 * items (each: type + quantity + optional brands) in one request, mirroring the
 * backend MARKETING create payload (items[]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingRequestBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (items: List<MarketingItemDto>, details: String) -> Unit
) {
    val drafts = remember { mutableStateListOf(MarketingItemDraft()) }
    var details by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isValid = drafts.isNotEmpty() && drafts.all { it.isValid }

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
                text = "Request Branding",
                style = AppTypography.TitleLarge,
                color = AppColors.TextPrimary
            )
            Text(
                text = "Add one or more branding assets to raise a request for this outlet.",
                style = AppTypography.BodySecondary,
                color = AppColors.TextSecondary
            )

            drafts.forEachIndexed { index, draft ->
                MarketingItemRow(
                    draft = draft,
                    index = index,
                    canRemove = drafts.size > 1,
                    onRemove = { drafts.removeAt(index) }
                )
            }

            TextButton(
                onClick = { drafts.add(MarketingItemDraft()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.BlueGradientStart)
                Spacer(Modifier.width(6.dp))
                Text("Add another item", style = AppTypography.Button, color = AppColors.BlueGradientStart)
            }

            // ── Details (optional) ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Notes (optional)",
                    style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                    color = AppColors.TextSecondary
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Any specific notes for this request", color = AppColors.PlaceholderTextColor) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.BlueGradientStart,
                        unfocusedBorderColor = AppColors.Divider
                    ),
                    textStyle = AppTypography.BodyPrimary
                )
            }

            Button(
                onClick = { if (isValid) onSubmit(drafts.map { it.toDto() }, details) },
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
                    style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.SemiBold, color = Color.White)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketingItemRow(
    draft: MarketingItemDraft,
    index: Int,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    var typeExpanded by remember { mutableStateOf(false) }
    val ddScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Item ${index + 1}",
                style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                color = AppColors.TextSecondary,
                modifier = Modifier.weight(1f)
            )
            if (canRemove) {
                TextButton(onClick = onRemove, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text("Remove", style = AppTypography.Caption, color = AppColors.Danger)
                }
            }
        }

        // ── Asset type ──
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded }
        ) {
            OutlinedTextField(
                value = draft.type?.label ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                placeholder = { Text("Select asset type", color = AppColors.PlaceholderTextColor) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.BlueGradientStart,
                    unfocusedBorderColor = AppColors.Divider
                ),
                textStyle = AppTypography.BodyPrimary
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { ddScope.launch { delay(50); typeExpanded = false } }
            ) {
                MarketingAssetType.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label, style = AppTypography.BodyPrimary) },
                        onClick = {
                            draft.type = option
                            ddScope.launch { delay(50); typeExpanded = false }
                        }
                    )
                }
            }
        }

        // ── Quantity ──
        OutlinedTextField(
            value = draft.quantity,
            onValueChange = { input -> draft.quantity = input.filter { it.isDigit() }.take(3) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Quantity") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.BlueGradientStart,
                unfocusedBorderColor = AppColors.Divider
            ),
            textStyle = AppTypography.BodyPrimary
        )

        // ── Brands (free text, comma-separated) ──
        OutlinedTextField(
            value = draft.brands,
            onValueChange = { draft.brands = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Brands (optional, comma-separated)") },
            placeholder = { Text("e.g. Coke, Sprite", color = AppColors.PlaceholderTextColor) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.BlueGradientStart,
                unfocusedBorderColor = AppColors.Divider
            ),
            textStyle = AppTypography.BodyPrimary
        )
    }
}

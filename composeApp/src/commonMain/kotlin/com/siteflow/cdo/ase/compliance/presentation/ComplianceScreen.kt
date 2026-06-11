package com.siteflow.cdo.ase.compliance.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.siteflow.cdo.ase.compliance.domain.ComplianceAction
import com.siteflow.cdo.ase.compliance.domain.ComplianceEvent
import com.siteflow.cdo.ase.compliance.domain.ComplianceViewModel
import com.siteflow.cdo.ase.onboarding.data.PhotoSlot
import com.siteflow.cdo.core.domain.ImagePicker
import com.siteflow.cdo.core.presentation.components.CdoButton
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceScreen(
    outletId: String,
    onBack: () -> Unit,
    viewModel: ComplianceViewModel = koinInject(),
    imagePicker: ImagePicker = koinInject()
) {
    val state by viewModel.state.collectAsState()

    var activeSlotId by remember { mutableStateOf<String?>(null) }
    var showImageSourceSheet by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    var coolerTypeExpanded by remember { mutableStateOf(false) }
    var capacityExpanded by remember { mutableStateOf(false) }
    val dropdownScope = rememberCoroutineScope()

    val coolerTypes = listOf("Campa", "Campa Energy", "Suncrush")
    val capacityMap = mapOf(
        "Campa" to listOf("14 Caser", "20 Caser", "30 Caser"),
        "Campa Energy" to listOf("6 Caser"),
        "Suncrush" to listOf("6 Caser")
    )

    LaunchedEffect(outletId) {
        viewModel.setOutletId(outletId)
        viewModel.events.collectLatest { event ->
            when (event) {
                ComplianceEvent.SubmitSuccess -> onBack()
                ComplianceEvent.NavigateBack -> onBack()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Info Banner ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Ensure all photos are clear and in focus before submitting.",
                    style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                    color = Color(0xFF92400E)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ═══════════════════════════════
            // SECTION 1: Asset Details
            // ═══════════════════════════════
            SectionHeader(title = "Asset Details", icon = Icons.Default.Inventory)

            Spacer(Modifier.height(12.dp))

            // Cooler Installed Toggle
            ToggleRow(
                label = "Cooler Installed",
                subLabel = "Is the cooler physically installed?",
                checked = state.coolerInstalled,
                onCheckedChange = { viewModel.onAction(ComplianceAction.CoolerInstalledChanged(it)) }
            )

            Spacer(Modifier.height(12.dp))

            // Signage Installed Toggle
            ToggleRow(
                label = "Signage Installed",
                subLabel = "Is the outlet signage in place?",
                checked = state.signageInstalled,
                onCheckedChange = { viewModel.onAction(ComplianceAction.SignageInstalledChanged(it)) }
            )

            Spacer(Modifier.height(12.dp))

            // Serial Number
            FieldLabel("Cooler Serial Number", required = true)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = state.serialNo,
                onValueChange = { viewModel.onAction(ComplianceAction.SerialNoChanged(it.uppercase())) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., SN-001", color = AppColors.PlaceholderTextColor) },
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

            Spacer(Modifier.height(12.dp))

            // Cooler Type Dropdown
            FieldLabel("Cooler Type", required = true)
            Spacer(Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = coolerTypeExpanded,
                onExpandedChange = { coolerTypeExpanded = !coolerTypeExpanded }
            ) {
                OutlinedTextField(
                    value = state.coolerType,
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
                ExposedDropdownMenu(expanded = coolerTypeExpanded, onDismissRequest = { dropdownScope.launch { delay(50); coolerTypeExpanded = false } }) {
                    coolerTypes.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, style = AppTypography.BodyPrimary) },
                            onClick = {
                                viewModel.onAction(ComplianceAction.CoolerTypeChanged(option))
                                viewModel.onAction(ComplianceAction.CapacityChanged("")) // reset capacity
                                dropdownScope.launch { delay(50); coolerTypeExpanded = false }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Capacity Dropdown
            FieldLabel("Capacity", required = true)
            Spacer(Modifier.height(6.dp))
            val capacities = capacityMap[state.coolerType] ?: emptyList()
            ExposedDropdownMenuBox(
                expanded = capacityExpanded,
                onExpandedChange = { if (capacities.isNotEmpty()) capacityExpanded = !capacityExpanded }
            ) {
                OutlinedTextField(
                    value = state.capacity,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    placeholder = {
                        Text(
                            if (state.coolerType.isBlank()) "Select cooler type first"
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
                    enabled = state.coolerType.isNotBlank()
                )
                ExposedDropdownMenu(expanded = capacityExpanded, onDismissRequest = { dropdownScope.launch { delay(50); capacityExpanded = false } }) {
                    capacities.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, style = AppTypography.BodyPrimary) },
                            onClick = { viewModel.onAction(ComplianceAction.CapacityChanged(option)); dropdownScope.launch { delay(50); capacityExpanded = false } }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══════════════════════════════
            // SECTION 2: Evidence Photos
            // ═══════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Evidence Photos", icon = Icons.Default.PhotoCamera)
                Text(
                    text = "${state.capturedCount} / 5",
                    style = AppTypography.Caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                    color = if (state.allPhotosCaptured) AppColors.Success else AppColors.TextTertiary
                )
            }

            Spacer(Modifier.height(12.dp))

            state.photoSlots.forEachIndexed { index, slot ->
                CompliancePhotoSlotCard(
                    slot = slot,
                    onCapture = {
                        activeSlotId = slot.id
                        showImageSourceSheet = true
                    },
                    onRemove = { viewModel.onAction(ComplianceAction.PhotoRemoved(slot.id)) }
                )
                if (index < state.photoSlots.lastIndex) Spacer(Modifier.height(14.dp))
            }

            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.error ?: "",
                    style = AppTypography.Caption,
                    color = AppColors.Danger
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Fixed Bottom Button ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.CardBackground,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                CdoButton(
                    text = if (state.isLoading) "Submitting…" else "Submit Compliance",
                    onClick = { viewModel.onAction(ComplianceAction.Submit) },
                    enabled = state.isFormValid && !state.isLoading,
                    loading = state.isLoading
                )
            }
        }
    }

    // ── Camera Permission Dialog ──
    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = { Text("Camera Access Needed") },
            text = { Text("Please allow camera access to capture compliance photos.") },
            confirmButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) { Text("OK") }
            }
        )
    }

    // ── Image Source Sheet ──
    if (showImageSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Add Photo",
                    style = AppTypography.TitleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Surface(
                    onClick = {
                        showImageSourceSheet = false
                        imagePicker.openCamera(
                            onImagePicked = { path ->
                                activeSlotId?.let { viewModel.onAction(ComplianceAction.PhotoCaptured(it, path)) }
                            },
                            onPermissionDenied = { showCameraPermissionDialog = true }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3F4F6)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoCamera, null, tint = AppColors.BlueGradientStart, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Take Photo", style = AppTypography.BodyPrimary.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium), color = Color(0xFF111827))
                    }
                }
            }
        }
    }
}

// ── Reusable sub-composables ──

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).background(AppColors.BlueGradientStart.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.BlueGradientStart, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = AppTypography.TitleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
            color = AppColors.TextPrimary
        )
    }
}

@Composable
private fun FieldLabel(label: String, required: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
            color = AppColors.TextSecondary
        )
        if (required) {
            Text(
                text = " *",
                style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                color = AppColors.Danger
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, subLabel: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F4F6))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Medium), color = AppColors.TextPrimary)
            Text(text = subLabel, style = AppTypography.Caption.copy(fontSize = 11.sp), color = AppColors.TextTertiary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = AppColors.BlueGradientStart,
                checkedThumbColor = Color.White
            )
        )
    }
}

@Composable
private fun CompliancePhotoSlotCard(slot: PhotoSlot, onCapture: () -> Unit, onRemove: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(slot.label, style = AppTypography.TitleMedium.copy(fontSize = 14.sp), color = Color(0xFF111827))
                Text(" *", style = AppTypography.TitleMedium.copy(fontSize = 14.sp), color = AppColors.Danger)
            }
            if (slot.imagePath != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = AppColors.Success, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Captured", style = AppTypography.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold), color = AppColors.Success)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (slot.imagePath != null) {
            Box(
                modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(12.dp)).clickable { onCapture() }
            ) {
                AsyncImage(model = slot.imagePath, contentDescription = slot.label, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                // Gradient + watermark info overlay at bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    if (slot.capturedAt != null) {
                        Text(
                            text = "📅 ${slot.capturedAt}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                    if (slot.gpsLabel != null) {
                        Text(
                            text = "📍 ${slot.gpsLabel}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
                IconButton(
                    onClick = { onCapture() },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Edit, "Re-capture", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp).size(32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Close, "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(130.dp)
                    .border(1.5.dp, Color(0xFFD1D5DB), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { onCapture() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(44.dp).background(AppColors.BlueGradientStart.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, "Capture ${slot.label}", tint = AppColors.BlueGradientStart, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Tap to Capture", style = AppTypography.BodyPrimary.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium), color = Color(0xFF6B7280))
                }
            }
        }
    }
}

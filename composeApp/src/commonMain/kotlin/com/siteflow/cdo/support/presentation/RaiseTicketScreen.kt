package com.siteflow.cdo.support.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.siteflow.cdo.core.domain.ImagePicker
import com.siteflow.cdo.core.presentation.components.CdoButton
import com.siteflow.cdo.core.presentation.components.CdoTextField
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import com.siteflow.cdo.support.domain.IssueType
import com.siteflow.cdo.support.domain.RaiseTicketAction
import com.siteflow.cdo.support.domain.RaiseTicketViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseTicketScreen(
    onBack: () -> Unit,
    viewModel: RaiseTicketViewModel = koinInject(),
    imagePicker: ImagePicker = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { onBack() }
    }

    if (state.isSuccess) {
        TicketSuccessScreen(ticketNumber = state.ticketNumber, onBack = onBack)
        return
    }

    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = { Text("Camera Access Needed", style = AppTypography.TitleMedium.copy(fontWeight = FontWeight.SemiBold)) },
            text = { Text("Please allow camera access to attach screenshots.", style = AppTypography.BodyPrimary, color = Color(0xFF64748B)) },
            confirmButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) {
                    Text("OK", color = AppColors.BlueGradientStart)
                }
            }
        )
    }

    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Add Screenshot",
                    style = AppTypography.TitleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                PickerOption(
                    icon = Icons.Default.PhotoCamera,
                    label = "Take Photo",
                    onClick = {
                        showAttachmentSheet = false
                        imagePicker.openCamera(
                            onImagePicked = { viewModel.onAction(RaiseTicketAction.ImageAdded(it)) },
                            onPermissionDenied = { showCameraPermissionDialog = true }
                        )
                    }
                )
                PickerOption(
                    icon = Icons.Default.PhotoLibrary,
                    label = "Choose from Gallery",
                    onClick = {
                        showAttachmentSheet = false
                        imagePicker.openGallery(
                            onImagePicked = { viewModel.onAction(RaiseTicketAction.ImageAdded(it)) }
                        )
                    }
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Spacer(Modifier.height(16.dp))

        // ── Issue Type ──
        FormSectionLabel("What's the issue?")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IssueType.entries.forEach { type ->
                IssueTypeChip(
                    type = type,
                    selected = state.issueType == type,
                    onClick = { viewModel.onAction(RaiseTicketAction.IssueTypeSelected(type)) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Description ──
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            FormSectionLabel("Describe the issue")
            Spacer(Modifier.height(10.dp))
            CdoTextField(
                value = state.description,
                onValueChange = { viewModel.onAction(RaiseTicketAction.DescriptionChanged(it)) },
                placeholder = "Tell us what happened and what you expected…",
                minLines = 5,
                characterLimit = 500,
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(
                visible = state.description.isNotEmpty() && state.description.length < 20,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Minimum 20 characters required",
                    style = AppTypography.Caption.copy(fontSize = 11.sp),
                    color = AppColors.Danger,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Screenshots ──
            FormSectionLabel("Screenshots  ·  Optional (max 2)")
            Spacer(Modifier.height(10.dp))
            AttachmentsRow(
                attachments = state.attachments,
                onAdd = { if (state.attachments.size < 2) showAttachmentSheet = true },
                onRemove = { viewModel.onAction(RaiseTicketAction.ImageRemoved(it)) }
            )

            Spacer(Modifier.height(24.dp))

            // ── Auto-collected info ──
            AutoCollectedCard(
                appVersion = state.appVersion,
                platform = state.platform,
                osVersion = state.osVersion,
                deviceModel = state.deviceModel
            )

            Spacer(Modifier.height(24.dp))

            CdoButton(
                text = "Submit Issue",
                onClick = { viewModel.onAction(RaiseTicketAction.Submit) },
                enabled = state.canSubmit,
                loading = state.isSubmitting,
                trailingIcon = null
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Issue Type Chip ──

@Composable
private fun IssueTypeChip(
    type: IssueType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) AppColors.BlueGradientStart else Color.White
    val borderColor = if (selected) AppColors.BlueGradientStart else Color(0xFFE5E7EB)
    val contentColor = if (selected) Color.White else Color(0xFF374151)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = type.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = type.label,
                style = AppTypography.Caption.copy(
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

// ── Attachments Row ──

@Composable
private fun AttachmentsRow(
    attachments: List<String>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        // Add button — always visible unless 2 attachments filled
        if (attachments.size < 2) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(1.5.dp, Color(0xFFD1D5DB), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add screenshot",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "${attachments.size}/2",
                        style = AppTypography.Caption.copy(fontSize = 10.sp),
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }

        attachments.forEach { path ->
            Box(modifier = Modifier.size(72.dp)) {
                AsyncImage(
                    model = path,
                    contentDescription = "Screenshot",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .background(Color(0xFF374151), CircleShape)
                        .clickable { onRemove(path) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

// ── Auto-collected Info Card ──

@Composable
private fun AutoCollectedCard(
    appVersion: String,
    platform: String,
    osVersion: String,
    deviceModel: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF0F9FF),
        border = BorderStroke(1.dp, Color(0xFFBAE6FD))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(18.dp).padding(top = 1.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "We'll also include automatically",
                    style = AppTypography.Caption.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF0369A1)
                )
                AutoInfoRow(label = "App version", value = appVersion.ifBlank { "—" })
                AutoInfoRow(label = "Platform", value = platform.ifBlank { "—" })
                AutoInfoRow(label = "OS", value = osVersion.ifBlank { "—" })
                AutoInfoRow(label = "Device", value = deviceModel.ifBlank { "—" })
            }
        }
    }
}

@Composable
private fun AutoInfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$label:",
            style = AppTypography.Caption.copy(fontSize = 12.sp),
            color = Color(0xFF0369A1).copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = AppTypography.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            color = Color(0xFF0369A1)
        )
    }
}

// ── Attachment Picker Sheet Option ──

@Composable
private fun PickerOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF3F4F6)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.BlueGradientStart, modifier = Modifier.size(22.dp))
            Text(
                text = label,
                style = AppTypography.BodyPrimary.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                color = Color(0xFF111827)
            )
        }
    }
}

// ── Section Label ──

@Composable
private fun FormSectionLabel(text: String) {
    Text(
        text = text,
        style = AppTypography.TitleMedium.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = Color(0xFF1E293B),
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

// ── Success Screen ──

@Composable
private fun TicketSuccessScreen(ticketNumber: String?, onBack: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn() + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(AppColors.BlueGradientStart, AppColors.BlueGradientEnd)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Ticket Raised!",
                style = AppTypography.TitleLarge.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF111827),
                textAlign = TextAlign.Center
            )

            Text(
                text = "We've received your issue and will get back to you within 24–48 hours.",
                style = AppTypography.BodyPrimary.copy(fontSize = 15.sp),
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            if (ticketNumber != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F9FF),
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                clipboard.setText(AnnotatedString(ticketNumber))
                                copied = true
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Reference Number",
                                style = AppTypography.Caption.copy(fontSize = 11.sp),
                                color = Color(0xFF0369A1).copy(alpha = 0.7f)
                            )
                            Text(
                                text = ticketNumber,
                                style = AppTypography.TitleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFF0369A1)
                            )
                        }
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = if (copied) "Copied" else "Copy",
                            tint = if (copied) Color(0xFF16A34A) else Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (copied) {
                    Text(
                        text = "Copied to clipboard",
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = Color(0xFF16A34A)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            CdoButton(
                text = "Back to Profile",
                onClick = onBack,
                trailingIcon = null
            )
        }
    }
}

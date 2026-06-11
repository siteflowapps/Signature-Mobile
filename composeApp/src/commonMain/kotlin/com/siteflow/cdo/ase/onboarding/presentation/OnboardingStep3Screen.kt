package com.siteflow.cdo.ase.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.siteflow.cdo.ase.onboarding.data.Classification
import com.siteflow.cdo.ase.onboarding.data.GpsLocation
import com.siteflow.cdo.ase.onboarding.data.PhotoSlot
import com.siteflow.cdo.ase.onboarding.domain.OnboardingAction
import com.siteflow.cdo.ase.onboarding.domain.OnboardingEvent
import com.siteflow.cdo.ase.onboarding.domain.OnboardingViewModel
import com.siteflow.cdo.ase.onboarding.domain.OnboardingValidator
import com.siteflow.cdo.core.domain.ImagePicker
import com.siteflow.cdo.core.presentation.components.CdoButton
import com.siteflow.cdo.core.presentation.components.dismissKeyboardOnTap
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingStep3Screen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    outletId: String? = null,
    viewModel: OnboardingViewModel = koinInject(),
    imagePicker: ImagePicker = koinInject()
) {
    val state by viewModel.state.collectAsState()

    // Track which slot is being captured
    var activeSlotId by remember { mutableStateOf<String?>(null) }
    var showImageSourceSheet by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onAction(OnboardingAction.SetCurrentStep(5))
        outletId?.let { viewModel.onAction(OnboardingAction.SetOutletId(it)) }
        viewModel.events.collectLatest { event ->
            when (event) {
                OnboardingEvent.NavigateBack -> onBack()
                OnboardingEvent.NavigateToStep6 -> onContinue()
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .dismissKeyboardOnTap()
    ) {
        // ── Content (scrollable) ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Info Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Ensure shop board is clearly visible. GPS location will be auto-tagged to all photos.",
                    style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                    color = Color(0xFF92400E)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Photo counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Outlet Photos",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF111827)
                )
                Text(
                    text = "${state.capturedPhotoCount} / 5 captured",
                    style = AppTypography.Caption.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (state.capturedPhotoCount > 0) AppColors.Success
                    else AppColors.TextTertiary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Photo slots
            state.photoSlots.forEachIndexed { index, slot ->
                PhotoSlotCard(
                    slot = slot,
                    gpsLocation = state.gpsLocation,
                    onCapture = {
                        activeSlotId = slot.id
                        showImageSourceSheet = true
                    },
                    onRemove = {
                        viewModel.onAction(OnboardingAction.PhotoRemoved(slot.id))
                    }
                )
                if (index < state.photoSlots.lastIndex) {
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Error
            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.error ?: "",
                    style = AppTypography.Caption,
                    color = AppColors.Danger,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Bottom Continue Button ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp)
                .background(Color.White)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            CdoButton(
                text = "Continue",
                onClick = { viewModel.onAction(OnboardingAction.ContinueToNextStep) },
                enabled = OnboardingValidator.isStep5Valid(state) && !state.isLoading,
                loading = state.isLoading
            )
        }
    }

    // ── Camera Permission Dialog ──
    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = { Text("Camera Access Needed") },
            text = { Text("Please allow camera access to capture outlet photos.") },
            confirmButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // ── Direct camera capture (no gallery option) ──
    if (showImageSourceSheet) {
        // Immediately open camera instead of showing a sheet
        LaunchedEffect(showImageSourceSheet) {
            showImageSourceSheet = false
            imagePicker.openCamera(
                onImagePicked = { path ->
                    activeSlotId?.let { slotId ->
                        viewModel.onAction(
                            OnboardingAction.PhotoCaptured(slotId, path)
                        )
                    }
                },
                onPermissionDenied = {
                    showCameraPermissionDialog = true
                }
            )
        }
    }
}

// ── Photo Slot Card ──

@Composable
private fun PhotoSlotCard(
    slot: PhotoSlot,
    gpsLocation: GpsLocation?,
    onCapture: () -> Unit,
    onRemove: () -> Unit
) {
    Column {
        // Label row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = slot.label,
                    style = AppTypography.TitleMedium.copy(fontSize = 14.sp),
                    color = Color(0xFF111827)
                )
                if (slot.required) {
                    Text(
                        text = " *",
                        style = AppTypography.TitleMedium.copy(fontSize = 14.sp),
                        color = AppColors.Danger
                    )
                }
            }

            if (slot.imagePath != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppColors.Success,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Captured",
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AppColors.Success
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (slot.imagePath != null) {
            // ── Filled state: image preview with overlay ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCapture() }
            ) {
                AsyncImage(
                    model = slot.imagePath,
                    contentDescription = slot.label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient + date/GPS info overlay at bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (slot.capturedAt != null) {
                        Text(
                            text = "📅 ${slot.capturedAt}",
                            style = AppTypography.Caption.copy(fontSize = 10.sp),
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    if (slot.gpsLabel != null) {
                        Text(
                            text = "📍 ${slot.gpsLabel}",
                            style = AppTypography.Caption.copy(fontSize = 10.sp),
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                // Re-capture button (top-right)
                IconButton(
                    onClick = { onCapture() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Re-capture",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Remove button (top-left)
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            // ── Empty state: dashed capture card ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFFD1D5DB),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { onCapture() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = AppColors.BlueGradientStart.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture ${slot.label}",
                            tint = AppColors.BlueGradientStart,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Tap to Capture",
                        style = AppTypography.BodyPrimary.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

package com.siteflow.signature.cso.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.siteflow.signature.cso.dashboard.data.AssetStatus
import com.siteflow.signature.cso.dashboard.data.SignatureStep
import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.dashboard.data.OutletStatus
import com.siteflow.signature.cso.dashboard.data.TimelineEntry
import com.siteflow.signature.cso.onboarding.data.PhotoSlot
import com.siteflow.signature.cso.dashboard.domain.OutletDetailAction
import com.siteflow.signature.cso.dashboard.domain.OutletDetailEvent
import com.siteflow.signature.cso.dashboard.domain.OutletDetailViewModel
import com.siteflow.signature.asset.approvals.presentation.AssetCard
import com.siteflow.signature.core.domain.ImagePicker
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import org.koin.compose.koinInject
import androidx.compose.ui.text.style.TextAlign
import com.siteflow.signature.core.presentation.components.SignatureShimmerEffect

import com.siteflow.signature.cso.dashboard.presentation.components.*

@Composable
fun OutletDetailScreen(
    outletId: String,
    onBack: () -> Unit,
    onContinueOnboarding: () -> Unit = {},
    onSubmitCompliance: () -> Unit = {},
    onUploadSignaturePhoto: () -> Unit = {},
    onAssetRequestSuccess: () -> Unit = {},
    viewModel: OutletDetailViewModel = koinInject(),
    imagePicker: ImagePicker = koinInject()
) {
    var showAssetRequestSheet by remember { mutableStateOf(false) }
    var showMarketingSheet by remember { mutableStateOf(false) }

    // Image Picker State
    var activeSlotId by remember { mutableStateOf<String?>(null) }
    var showImageSourceSheet by remember { mutableStateOf(false) }
    // When non-null, the next picked image is a compliance photo for this kind
    // ("COOLER"|"MARKETING") rather than a signature photo.
    var complianceKind by remember { mutableStateOf<String?>(null) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    val detailState by viewModel.state.collectAsState()

    // Navigate back to outlet list on asset request success
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                OutletDetailEvent.AssetRequestSuccess -> onAssetRequestSuccess()
                else -> Unit
            }
        }
    }

    // Fetch the full, fresh outlet from GET /outlets/{id} (not the list item).
    LaunchedEffect(outletId) {
        viewModel.onAction(OutletDetailAction.LoadById(outletId))
    }

    val outlet = detailState.outlet
    if (outlet == null) {
        SkeletonOutletDetail()
        return
    }

    // Photo viewer dialog state
    var selectedPhoto by remember { mutableStateOf<PhotoSlot?>(null) }

    selectedPhoto?.let { photo ->
        PhotoViewerDialog(
            photo = photo,
            onDismiss = { selectedPhoto = null }
        )
    }

    // Cooler/branding raise + compliance now live on the AssetCards above; the
    // bottom bar only covers onboarding resubmit + signature-verification compliance.
    val showCta = outlet.assetStatus != AssetStatus.VERIFIED &&
        outlet.assetStatus != AssetStatus.VERIFICATION_PENDING && (
        outlet.status == OutletStatus.ASM_REJECTED ||
        outlet.nextPendingStep == SignatureStep.SIGNATURE_VERIFICATION
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground)
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutletInfoCard(outlet)
            SignaturePipelineCard(outlet)

            // ── Next Action: context-aware CTA (new) ──
            NextActionCard(
                outlet               = outlet,
                onContinueOnboarding = onContinueOnboarding,
                onRequestCooler      = { showAssetRequestSheet = true },
                onRequestBranding    = { showMarketingSheet    = true },
                onUploadCompliance   = {
                    complianceKind = "COOLER"
                    showImageSourceSheet = true
                }
            )

            // Asset requests (raise / approve-status / compliance) — cooler + branding.
            AssetCard(
                outlet = outlet,
                kind = "COOLER",
                onRequest = { showAssetRequestSheet = true },
                onUploadCompliance = { complianceKind = "COOLER"; showImageSourceSheet = true }
            )
            AssetCard(
                outlet = outlet,
                kind = "MARKETING",
                onRequest = { showMarketingSheet = true },
                onUploadCompliance = { complianceKind = "MARKETING"; showImageSourceSheet = true }
            )

            OutletDetailsCard(outlet)
            OnboardingPhotosCard(outlet)
            ComplianceRecordCard(outlet)
            if (outlet.photoSlots.isNotEmpty()) {
                PhotosCard(outlet, onViewPhoto = { selectedPhoto = it })
            }
            if (outlet.timeline.isNotEmpty()) {
                TimelineCard(outlet.timeline)
            }
            if (outlet.distributorName.isNotBlank()) {
                DistributorDetailsCard(outlet)
            }
            if (outlet.bankName.isNotBlank() || outlet.upiId.isNotBlank()) {
                PaymentModeCard(outlet)
            }

            // Extra space for bottom CTA clearance
            if (showCta) {
                Spacer(Modifier.height(8.dp))
            }

        }

        // Fixed bottom CTA
        if (showCta) {
            BottomCtaBar(
                outlet = outlet,
                onAction = onContinueOnboarding,
                onSubmitCompliance = onSubmitCompliance,
                onUploadSignaturePhoto = {
                    activeSlotId = "add_new"
                    showImageSourceSheet = true
                }
            )
        }
    }

    if (showAssetRequestSheet) {
        AssetRequestBottomSheet(
            onDismiss = { showAssetRequestSheet = false },
            onSubmit = { coolerSize, quantity, details ->
                showAssetRequestSheet = false
                viewModel.onAction(
                    OutletDetailAction.RequestCooler(
                        outletId = outlet.id,
                        coolerSize = coolerSize,
                        quantity = quantity,
                        details = details
                    )
                )
            }
        )
    }

    if (showMarketingSheet) {
        MarketingRequestBottomSheet(
            onDismiss = { showMarketingSheet = false },
            onSubmit = { items, details ->
                showMarketingSheet = false
                viewModel.onAction(
                    OutletDetailAction.RequestMarketing(
                        outletId = outlet.id,
                        items = items,
                        details = details
                    )
                )
            }
        )
    }

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

    // ── Image Source Bottom Sheet ──
    if (showImageSourceSheet) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Add Photo",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Take Photo
                Surface(
                    onClick = {
                        showImageSourceSheet = false
                        imagePicker.openCamera(
                            onImagePicked = { path ->
                                val kind = complianceKind
                                if (kind != null) {
                                    complianceKind = null
                                    viewModel.onAction(OutletDetailAction.UploadCompliance(outlet.id, kind, path))
                                } else activeSlotId?.let { slotId ->
                                    viewModel.onAction(OutletDetailAction.PhotoCaptured(slotId, path))
                                }
                            },
                            onPermissionDenied = {
                                showCameraPermissionDialog = true
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3F4F6)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = AppColors.BlueGradientStart,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Take Photo",
                            style = AppTypography.BodyPrimary.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF111827)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Choose from Gallery
                Surface(
                    onClick = {
                        showImageSourceSheet = false
                        imagePicker.openGallery { path ->
                            val kind = complianceKind
                            if (kind != null) {
                                complianceKind = null
                                viewModel.onAction(OutletDetailAction.UploadCompliance(outlet.id, kind, path))
                            } else activeSlotId?.let { slotId ->
                                viewModel.onAction(OutletDetailAction.PhotoCaptured(slotId, path))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3F4F6)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = AppColors.BlueGradientStart,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Choose from Gallery",
                            style = AppTypography.BodyPrimary.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF111827)
                        )
                    }
                }
            }
        }
    }
}

package com.siteflow.cdo.asm.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.collectLatest
import com.siteflow.cdo.ase.dashboard.data.CdoStep
import com.siteflow.cdo.ase.dashboard.data.ComplianceRecord
import com.siteflow.cdo.ase.dashboard.data.ComplianceState
import com.siteflow.cdo.ase.dashboard.data.OutletItem
import com.siteflow.cdo.ase.dashboard.data.OutletStatus


import com.siteflow.cdo.ase.dashboard.presentation.CdoPipelineCard

import com.siteflow.cdo.ase.dashboard.presentation.DistributorDetailsCard
import com.siteflow.cdo.ase.dashboard.presentation.OutletDetailsCard
import com.siteflow.cdo.ase.dashboard.presentation.OutletInfoCard
import com.siteflow.cdo.ase.dashboard.presentation.PaymentModeCard
import com.siteflow.cdo.ase.dashboard.presentation.OnboardingPhotosCard
import com.siteflow.cdo.ase.dashboard.presentation.PhotosCard
import com.siteflow.cdo.ase.dashboard.presentation.TimelineCard
import com.siteflow.cdo.asm.dashboard.domain.AsmDashboardAction
import com.siteflow.cdo.asm.dashboard.domain.AsmDashboardEvent
import com.siteflow.cdo.asm.dashboard.domain.AsmDashboardViewModel
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import org.koin.compose.koinInject

/**
 * ASM Outlet Review Screen — shows full outlet details with approve/reject actions.
 * Reuses all section cards from OutletDetailComponents to maintain visual consistency with the ASE view.
 */
@Composable
fun AsmOutletReviewScreen(
    outletId: String,
    onBack: () -> Unit,
    viewModel: AsmDashboardViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val outlet = state.outlets.firstOrNull { it.id == outletId }

    if (outlet == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Outlet not found", color = AppColors.TextTertiary)
        }
        return
    }

    val isPendingReview = outlet.status == OutletStatus.ASM_PENDING
    val isCompliancePending = outlet.complianceState == ComplianceState.SUBMITTED || 
                             outlet.assetStatus == com.siteflow.cdo.ase.dashboard.data.AssetStatus.VERIFICATION_PENDING
    val hasActionBar = isPendingReview || isCompliancePending
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    
    // Full-screen image viewer state
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    // Listen for events (success/error messages) and auto-navigate back
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AsmDashboardEvent.ShowMessage -> {
                    // Navigate back after successful action
                    if (event.message.contains("approved") || 
                        event.message.contains("rejected") ||
                        event.message.contains("verified")) {
                        onBack()
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .verticalScroll(rememberScrollState())
                .padding(bottom = if (hasActionBar) 100.dp else 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Header Card ──
                OutletInfoCard(outlet)

                // ── P0: Review Status Banner — shown at top AFTER a decision ──
                // (only visible when NOT pending, i.e. decision already made)
                if (!isPendingReview && !isCompliancePending) {
                    ReviewStatusBanner(outlet)
                }

                // ── P0: Decision Context Banner — shown when action is required ──
                if (isPendingReview || isCompliancePending) {
                    AsmDecisionContextBanner(
                        outlet = outlet,
                        isPendingReview = isPendingReview,
                        isCompliancePending = isCompliancePending
                    )
                }

                // ── P1: ASE Attribution — moved up for immediate context ──
                if (outlet.onboardedByAse.isNotBlank()) {
                    AseAttributionCard(outlet)
                }

                // ── P1: Compliance Submission Review — moved up, reviewed first by ASM ──
                if (outlet.complianceRecords.isNotEmpty()) {
                    outlet.complianceRecords.forEach { record ->
                        ComplianceReviewCard(
                            record = record,
                            onImageClick = { selectedImageUrl = it }
                        )
                    }
                }

                // ── CDO Pipeline ──
                CdoPipelineCard(outlet)

                // ── Submission Timeline ──
                if (outlet.timeline.isNotEmpty()) {
                    TimelineCard(outlet.timeline)
                }

                // ── Outlet Details (read-only for ASM) ──
                OutletDetailsCard(outlet)

                // ── Onboarding Photos (from API) ──
                OnboardingPhotosCard(outlet, onImageClick = { selectedImageUrl = it })

                // ── Captured Photos (read-only) ──
                PhotosCard(outlet)

                // ── Distributor ──
                if (outlet.distributorName.isNotBlank()) {
                    DistributorDetailsCard(outlet)
                }

                // ── Payment ──
                if (outlet.bankName.isNotBlank() || outlet.upiId.isNotBlank()) {
                    PaymentModeCard(outlet)
                }
            }
        }

        // ── Bottom Action Bar (only for pending review) ──
        if (isPendingReview) {
            AsmApproveRejectBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                enabled = !state.isApproving,
                onReject = { showRejectDialog = true },
                onApprove = {
                    viewModel.onAction(AsmDashboardAction.ApproveOutlet(outlet.id))
                }
            )
        } else if (isCompliancePending) {
            // ── P0: Compliance Verify Bar with context ──
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                val complianceId = outlet.complianceId.ifBlank { outlet.complianceRecords.firstOrNull()?.id ?: "" }
                val submittedBy = outlet.complianceRecords.firstOrNull()?.uploadedByName ?: outlet.onboardedByAse
                val submittedOn = outlet.complianceRecords.firstOrNull()?.uploadedAt?.substringBefore("T") ?: ""

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Context hint
                    if (submittedBy.isNotBlank()) {
                        Text(
                            text = buildString {
                                append("Submitted by $submittedBy")
                                if (submittedOn.isNotBlank()) append(" · $submittedOn")
                                append(". Review the photos above before verifying.")
                            },
                            style = AppTypography.Caption.copy(fontSize = 12.sp),
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Button(
                        onClick = {
                            if (complianceId.isNotBlank()) {
                                viewModel.onAction(AsmDashboardAction.VerifyCompliance(complianceId))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !state.isApproving && complianceId.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            disabledContainerColor = AppColors.Divider
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Verify Compliance",
                            style = AppTypography.Button,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ── Loading overlay during approve/reject ──
        if (state.isApproving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // ── Full Screen Image Viewer ──
        if (selectedImageUrl != null) {
            FullScreenImageViewer(
                imageUrl = selectedImageUrl!!,
                onDismiss = { selectedImageUrl = null }
            )
        }
    }

    // ── Reject Dialog ──
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = {
                Text(
                    "Why are you rejecting \"${outlet.name}\"?",
                    style = AppTypography.TitleMedium.copy(fontSize = 17.sp)
                )
            },
            text = {
                Column {
                    Text(
                        text = "The ASE will be notified and can re-submit after addressing the issue.",
                        style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                        color = AppColors.TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("Reason for rejection...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onAction(
                            AsmDashboardAction.RejectOutlet(outlet.id, rejectReason)
                        )
                        showRejectDialog = false
                    },
                    enabled = rejectReason.isNotBlank() && !state.isApproving,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Danger)
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════
// ASM-Unique Composables
// ═══════════════════════════════════════════════════════

/**
 * P0: Decision context banner shown when the ASM has a pending action.
 * Clearly tells them what they need to do and who submitted.
 */
@Composable
private fun AsmDecisionContextBanner(
    outlet: OutletItem,
    isPendingReview: Boolean,
    isCompliancePending: Boolean
) {
    val (bgColor, borderColor, iconTint, title, subtitle) = when {
        isPendingReview -> ContextBannerData(
            bgColor = Color(0xFFEFF6FF),
            borderColor = AppColors.BlueGradientStart.copy(alpha = 0.3f),
            iconTint = AppColors.BlueGradientStart,
            title = "Awaiting Your Approval",
            subtitle = if (outlet.onboardedByAse.isNotBlank())
                "${outlet.onboardedByAse} has submitted this outlet for your review."
            else
                "Review all details below and approve or reject."
        )
        isCompliancePending -> ContextBannerData(
            bgColor = Color(0xFFFFFBEB),
            borderColor = Color(0xFFF59E0B).copy(alpha = 0.4f),
            iconTint = Color(0xFFD97706),
            title = "Compliance Review Needed",
            subtitle = if (outlet.onboardedByAse.isNotBlank())
                "${outlet.onboardedByAse} has submitted compliance photos. Review and verify."
            else
                "Compliance photos have been submitted. Review and verify below."
        )
        else -> return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp).padding(top = 1.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = AppTypography.BodyPrimary.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = iconTint
                )
                Text(
                    text = subtitle,
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}

private data class ContextBannerData(
    val bgColor: Color,
    val borderColor: Color,
    val iconTint: Color,
    val title: String,
    val subtitle: String
)

/**
 * Shows which ASE onboarded this outlet — unique to the ASM review flow.
 */
@Composable
private fun AseAttributionCard(outlet: OutletItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SUBMITTED BY",
                style = AppTypography.Caption.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                ),
                color = AppColors.TextTertiary
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AppColors.BlueGradientStart, AppColors.BlueGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = outlet.onboardedByAse,
                        style = AppTypography.BodyPrimary.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = AppColors.black27
                    )
                    Text(
                        text = "Area Sales Executive",
                        style = AppTypography.Caption.copy(fontSize = 12.sp),
                        color = AppColors.TextTertiary
                    )
                }
            }
        }
    }
}

/**
 * Banner shown only when the outlet has been explicitly rejected by the ASM.
 * Not shown for approved outlets — the ASM only sees their own team's outlets,
 * so "you approved this" is always true and adds no information.
 */
@Composable
private fun ReviewStatusBanner(outlet: OutletItem) {
    // Only meaningful when the outlet is in a REJECTED state
    if (outlet.status != OutletStatus.ASM_REJECTED) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = AppColors.Danger,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "This outlet was rejected",
                    style = AppTypography.BodyPrimary.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = AppColors.Danger
                )
                Text(
                    text = "The ASE has been notified and can re-submit.",
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = AppColors.Danger.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Fixed bottom action bar with Reject and Approve buttons.
 */
@Composable
private fun AsmApproveRejectBar(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onReject: () -> Unit,
    onApprove: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReject,
                enabled = enabled,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Danger),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Danger.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reject", style = AppTypography.Button)
            }

            Button(
                onClick = onApprove,
                enabled = enabled,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Success,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Approve", style = AppTypography.Button)
            }
        }
    }
}

/**
 * Compliance Submission card — shows all evidence the ASE submitted.
 * Appears on ASM review when complianceRecords is non-empty.
 */
@Composable
private fun ComplianceReviewCard(
    record: ComplianceRecord,
    onImageClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AppColors.BlueGradientStart,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Compliance Submission",
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AppColors.TextPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (record.verified) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (record.verified) "Verified" else "Pending",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = AppTypography.Caption.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (record.verified) AppColors.Success else Color(0xFFD97706)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(Modifier.height(14.dp))

            // ── Asset Details ──
            ComplianceDetailRow("Cooler Installed", if (record.coolerInstalled) "Yes" else "No")
            ComplianceDetailRow("Serial No", record.serialNo.ifBlank { "—" })
            ComplianceDetailRow("Cooler Type", record.coolerType.ifBlank { "—" })
            ComplianceDetailRow("Capacity", record.capacity.ifBlank { "—" })
            ComplianceDetailRow("Signage Installed", if (record.signageInstalled) "Yes" else "No")

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(Modifier.height(14.dp))

            // ── Evidence Photos ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "EVIDENCE PHOTOS",
                    style = AppTypography.Caption.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = AppColors.TextTertiary
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE0E7FF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${record.images.size} photo${if (record.images.size != 1) "s" else ""}",
                        style = AppTypography.Caption.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AppColors.BlueGradientStart
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            
            // ── Carousel of Photos ──
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(record.images.size) { index ->
                    val img = record.images[index]
                    val typeLabel = when (img.imageType) {
                        "COOLER" -> "Cooler"
                        "ASSET_LABEL" -> "Asset Label"
                        "SIGNAGE" -> "Signage"
                        "OUTER_OUTLET" -> "Outer Outlet"
                        "INNER_OUTLET" -> "Inner Outlet"
                        else -> img.imageType
                    }
                    
                    Column(
                        modifier = Modifier
                            .width(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick(img.imageUrl) }
                    ) {
                        Box {
                            coil3.compose.AsyncImage(
                                model = img.imageUrl,
                                contentDescription = typeLabel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )

                            // Label overlay — bottom-left
                            Surface(
                                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = typeLabel,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = AppTypography.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }

                            // P1: Tap affordance — zoom icon top-right corner
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(26.dp)
                                    .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Tap to view full screen",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(Modifier.height(10.dp))

            // ── Submitted by ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Submitted by ${record.uploadedByName}",
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = AppColors.TextTertiary
                )
                Text(
                    text = record.uploadedAt.substringBefore("T"),
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = AppColors.TextTertiary
                )
            }
        }
    }
}

@Composable
private fun ComplianceDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
            color = AppColors.TextSecondary
        )
        Text(
            text = value,
            style = AppTypography.BodyPrimary.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            color = AppColors.TextPrimary
        )
    }
}
@Composable
private fun FullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            coil3.compose.AsyncImage(
                model = imageUrl,
                contentDescription = "Full Screen View",
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
            
            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

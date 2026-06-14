package com.siteflow.signature.cso.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.siteflow.signature.cso.dashboard.data.AssetStatus
import com.siteflow.signature.cso.dashboard.data.SignatureStep
import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.dashboard.data.OutletStatus
import com.siteflow.signature.cso.dashboard.data.TimelineEntry
import com.siteflow.signature.cso.onboarding.data.PhotoSlot
import com.siteflow.signature.core.presentation.components.HorizontalSignatureStepper
import com.siteflow.signature.core.presentation.components.StepState
import com.siteflow.signature.core.presentation.components.StepperItem
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography

// ═══════════════════════════════════════════════════════
// Shared Outlet Detail Composables
// Used by both OutletDetailScreen (ASE) and AsmOutletReviewScreen (ASM)
// ═══════════════════════════════════════════════════════

// ── Header Card ──

@Composable
fun OutletInfoCard(outlet: OutletItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        outlet.status.barColor,
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(outlet.status.bgColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = outlet.initials,
                                style = AppTypography.TitleMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = outlet.status.color
                            )
                        }

                        Column {
                            Text(
                                text = outlet.name,
                                style = AppTypography.TitleMedium.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AppColors.TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AppColors.TextTertiary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = outlet.location,
                                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                                    color = AppColors.TextTertiary
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(outlet.status.bgColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = outlet.status.label,
                            style = AppTypography.Caption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = outlet.status.color
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = AppColors.Divider)
                Spacer(Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Updated: ${outlet.updatedTime}",
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// Signature Journey Card — 5-step horizontal stepper
// ══════════════════════════════════════════════════════════════════════

/**
 * Redesigned macro-pipeline card showing the full 5-step Signature journey
 * (Enrollment → ASE Review → ASM Approval → Asset Request → Signature Verified).
 * Replaces the old flat list with an interactive horizontal stepper.
 */
@Composable
fun SignaturePipelineCard(outlet: OutletItem) {
    val steps = SignatureStep.entries.map { step ->
        val isDone     = step in outlet.completedSteps
        val isRejected = when (step) {
            SignatureStep.ASE_APPROVAL -> outlet.status == OutletStatus.ASE_REJECTED
            SignatureStep.ASM_APPROVAL -> outlet.status == OutletStatus.ASM_REJECTED
            else                       -> false
        }
        val isActive = !isDone && !isRejected && step == outlet.nextPendingStep
        StepperItem(
            label = step.label,
            state = when {
                isDone     -> StepState.DONE
                isRejected -> StepState.REJECTED
                isActive   -> StepState.ACTIVE
                else       -> StepState.PENDING
            }
        )
    }

    SectionCard(
        title    = "Signature Journey",
        trailing = "${outlet.completedSteps.size}/${SignatureStep.entries.size} done"
    ) {
        HorizontalSignatureStepper(
            steps    = steps,
            dotSize  = 28.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // Completion banner
        if (outlet.assetStatus == AssetStatus.VERIFIED) {
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                color    = AppColors.CtaGreenBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppColors.StepDone,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text  = "Signature Activated! 🎉",
                            style = AppTypography.BodyPrimary.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp
                            ),
                            color = AppColors.green3D
                        )
                        Text(
                            text  = "All steps verified — outlet is fully onboarded.",
                            style = AppTypography.Caption.copy(fontSize = 12.sp),
                            color = AppColors.green4A
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
// Next Action Card — context-aware CTA (NEW)
// ══════════════════════════════════════════════════════════════════════

/**
 * Prominent, context-aware card that tells the CSO exactly what to do next.
 * Appears immediately after the Signature Journey Card on the detail page.
 *
 * @param outlet                 Current outlet data.
 * @param onContinueOnboarding   Navigate back into the onboarding flow.
 * @param onRequestCooler        Open cooler request bottom sheet.
 * @param onRequestBranding      Open branding request bottom sheet.
 * @param onUploadCompliance     Open compliance upload picker.
 */
@Composable
fun NextActionCard(
    outlet: OutletItem,
    onContinueOnboarding: () -> Unit = {},
    onRequestCooler: () -> Unit = {},
    onRequestBranding: () -> Unit = {},
    onUploadCompliance: () -> Unit = {}
) {
    // Fully verified — no CTA needed
    if (outlet.assetStatus == AssetStatus.VERIFIED) return

    data class CtaConfig(
        val icon: ImageVector,
        val iconTint: Color,
        val bgColor: Color,
        val borderColor: Color,
        val title: String,
        val subtitle: String,
        val primaryLabel: String?,
        val secondaryLabel: String?,
        val onPrimary: () -> Unit,
        val onSecondary: () -> Unit
    )

    val config: CtaConfig = when {
        // ── Draft: still enrolling ──
        outlet.isContinuingOnboarding -> CtaConfig(
            icon          = Icons.Default.Info,
            iconTint      = AppColors.StepActive,
            bgColor       = AppColors.CtaBlueBg,
            borderColor   = AppColors.StepActiveBg,
            title         = "Complete Outlet Setup",
            subtitle      = "Step ${outlet.onboardingStep - 1} of 6 done — tap to resume where you left off.",
            primaryLabel  = "Resume Setup →",
            secondaryLabel = null,
            onPrimary     = onContinueOnboarding,
            onSecondary   = {}
        )

        // ── ASE rejected enrollment ──
        outlet.status == OutletStatus.ASE_REJECTED -> CtaConfig(
            icon          = Icons.Default.Warning,
            iconTint      = AppColors.StepRejected,
            bgColor       = AppColors.CtaRedBg,
            borderColor   = AppColors.StepRejectedBg,
            title         = "L1 Review — Rejected",
            subtitle      = "The enrollment was rejected by the Area Sales Executive. Review the feedback and resubmit.",
            primaryLabel  = "Fix & Resubmit →",
            secondaryLabel = null,
            onPrimary     = onContinueOnboarding,
            onSecondary   = {}
        )

        // ── Awaiting ASE review ──
        outlet.status == OutletStatus.ASE_PENDING -> CtaConfig(
            icon          = Icons.Default.Info,
            iconTint      = AppColors.Warning,
            bgColor       = AppColors.CtaAmberBg,
            borderColor   = AppColors.AssetPendingBg,
            title         = "Awaiting L1 Review",
            subtitle      = "Your enrollment is with the Area Sales Executive for approval.",
            primaryLabel  = null,
            secondaryLabel = null,
            onPrimary     = {},
            onSecondary   = {}
        )

        // ── ASM rejected ──
        outlet.status == OutletStatus.ASM_REJECTED -> CtaConfig(
            icon          = Icons.Default.Warning,
            iconTint      = AppColors.StepRejected,
            bgColor       = AppColors.CtaRedBg,
            borderColor   = AppColors.StepRejectedBg,
            title         = "L2 Approval — Rejected",
            subtitle      = "The ASM has rejected this enrollment. Address the issues and resubmit.",
            primaryLabel  = "Fix & Resubmit →",
            secondaryLabel = null,
            onPrimary     = onContinueOnboarding,
            onSecondary   = {}
        )

        // ── Awaiting ASM review ──
        outlet.status == OutletStatus.ASM_PENDING -> CtaConfig(
            icon          = Icons.Default.Info,
            iconTint      = AppColors.Warning,
            bgColor       = AppColors.CtaAmberBg,
            borderColor   = AppColors.AssetPendingBg,
            title         = "Awaiting L2 Approval",
            subtitle      = "L1 approved. The Account Sales Manager is reviewing this outlet.",
            primaryLabel  = null,
            secondaryLabel = null,
            onPrimary     = {},
            onSecondary   = {}
        )

        // ── ASM approved, no assets raised yet — the key action state ──
        outlet.status == OutletStatus.ASM_APPROVED &&
        outlet.assetStatus == AssetStatus.NOT_REQUESTED -> CtaConfig(
            icon          = Icons.Default.CheckCircle,
            iconTint      = AppColors.StepActive,
            bgColor       = AppColors.CtaBlueBg,
            borderColor   = AppColors.StepActiveBg,
            title         = "Outlet Approved — Start Signature Setup",
            subtitle      = "Request a cooler and branding assets to begin the Signature activation.",
            primaryLabel  = "Request Cooler",
            secondaryLabel = "Request Branding",
            onPrimary     = onRequestCooler,
            onSecondary   = onRequestBranding
        )

        // ── Compliance photo needed ──
        outlet.coolerNeedsCompliance -> CtaConfig(
            icon          = Icons.Default.Warning,
            iconTint      = AppColors.Warning,
            bgColor       = AppColors.CtaAmberBg,
            borderColor   = AppColors.AssetPendingBg,
            title         = "Compliance Photo Required",
            subtitle      = "The cooler has been installed. Upload a compliance photo to complete the step.",
            primaryLabel  = "Upload Compliance Photo →",
            secondaryLabel = null,
            onPrimary     = onUploadCompliance,
            onSecondary   = {}
        )

        // ── Verification pending (assets submitted) ──
        outlet.assetStatus == AssetStatus.VERIFICATION_PENDING -> CtaConfig(
            icon          = Icons.Default.Info,
            iconTint      = AppColors.StepActive,
            bgColor       = AppColors.CtaBlueBg,
            borderColor   = AppColors.StepActiveBg,
            title         = "Signature Verification In Progress",
            subtitle      = "Compliance submitted — awaiting final verification by the ASM.",
            primaryLabel  = null,
            secondaryLabel = null,
            onPrimary     = {},
            onSecondary   = {}
        )

        // ── Default: no specific action ──
        else -> return
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = config.bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, config.borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = config.icon,
                    contentDescription = null,
                    tint = config.iconTint,
                    modifier = Modifier.size(22.dp).padding(top = 1.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = config.title,
                        style = AppTypography.TitleMedium.copy(
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AppColors.TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = config.subtitle,
                        style = AppTypography.Caption.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = AppColors.TextSecondary
                    )
                }
            }

            // Action buttons (if any)
            if (config.primaryLabel != null) {
                Spacer(Modifier.height(14.dp))
                if (config.secondaryLabel != null) {
                    // Two side-by-side buttons (e.g. Request Cooler + Request Branding)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = config.onPrimary,
                            shape   = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors  = ButtonDefaults.outlinedButtonColors(
                                contentColor = config.iconTint
                            ),
                            border  = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = Brush.horizontalGradient(
                                    listOf(config.iconTint, config.iconTint)
                                )
                            )
                        ) {
                            Text(
                                text  = config.primaryLabel,
                                style = AppTypography.Button.copy(fontSize = 13.sp)
                            )
                        }
                        Button(
                            onClick = config.onSecondary,
                            shape   = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = config.iconTint
                            )
                        ) {
                            Text(
                                text  = config.secondaryLabel,
                                style = AppTypography.Button.copy(fontSize = 13.sp),
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Single full-width button
                    Button(
                        onClick  = config.onPrimary,
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = config.iconTint
                        )
                    ) {
                        Text(
                            text  = config.primaryLabel,
                            style = AppTypography.Button.copy(fontSize = 13.sp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ── Submission Timeline Card ──

@Composable
fun TimelineCard(timeline: List<TimelineEntry>) {
    SectionCard("Submission Timeline") {
        val lineColor = AppColors.Divider
        val dotSize = 10.dp

        timeline.forEachIndexed { index, entry ->
            val isFirst = index == 0

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        if (index < timeline.size - 1) {
                            drawLine(
                                color = lineColor,
                                start = Offset(dotSize.toPx() / 2, 20.dp.toPx()),
                                end = Offset(dotSize.toPx() / 2, size.height),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    }
                    .padding(bottom = if (index < timeline.size - 1) 16.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .background(
                            if (isFirst) AppColors.BlueGradientStart else AppColors.PlaceholderTextColor,
                            CircleShape
                        )
                )

                Column {
                    Text(
                        text = entry.title,
                        style = AppTypography.BodyPrimary.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isFirst) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isFirst) AppColors.TextPrimary else AppColors.TextSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = entry.timestamp,
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary
                    )
                }
            }
        }
    }
}

// ── Outlet Details Card ──

@Composable
fun OutletDetailsCard(outlet: OutletItem) {
    val uriHandler = LocalUriHandler.current

    SectionCard("Outlet Details") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                DetailField("Owner Name", outlet.ownerName)
            }
            if (outlet.outletType.isNotBlank()) {
                Box(modifier = Modifier.weight(1f)) {
                    DetailField("Outlet Type", outlet.outletType)
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                DetailField("DMS ID", outlet.dmsId.ifBlank { "Not linked" })
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (outlet.contactNumber.isNotBlank()) {
                Box(modifier = Modifier.weight(1f)) {
                    TappablePhoneField(
                        label = "Contact Number",
                        number = outlet.contactNumber,
                        onClick = { uriHandler.openUri("tel:${outlet.contactNumber}") }
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            if (outlet.whatsAppNumber.isNotBlank()) {
                Box(modifier = Modifier.weight(1f)) {
                    TappablePhoneField(
                        label = "WhatsApp",
                        number = outlet.whatsAppNumber,
                        onClick = { uriHandler.openUri("https://wa.me/91${outlet.whatsAppNumber}") }
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        DetailField("Address", buildString {
            append(outlet.address)
            if (outlet.city.isNotBlank()) append(", ${outlet.city}")
            if (outlet.pincode.isNotBlank()) append(" - ${outlet.pincode}")
        })
        if (outlet.gpsLocation != null) {
            DetailField(
                "GPS Location",
                "${outlet.gpsLocation.areaName} (${outlet.gpsLocation.latitude}, ${outlet.gpsLocation.longitude})"
            )
        }
    }
}

// ── Classification & Volume Card ──

@Composable
fun ClassificationVolumeCard(outlet: OutletItem) {
    SectionCard("Classification & Volume") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Classification",
                style = AppTypography.Caption.copy(fontSize = 12.sp),
                color = AppColors.TextTertiary,
                modifier = Modifier.width(110.dp)
            )
            Box(
                modifier = Modifier
                    .background(outlet.slab.bgColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${outlet.slab.emoji} ${outlet.slab.label}",
                    style = AppTypography.Caption.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = outlet.slab.color
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (outlet.estimatedVolume.isNotBlank()) {
            DetailField("Monthly Volume", "${outlet.estimatedVolume} cs")
        }
    }
}

// ── Captured Photos Card (read-only) ──

@Composable
fun PhotosCard(outlet: OutletItem, onViewPhoto: (PhotoSlot) -> Unit = {}) {
    if (outlet.photoSlots.isEmpty()) return

    SectionCard(
        "Captured Photos",
        trailing = "${outlet.capturedPhotoCount}/${outlet.photoSlots.size}"
    ) {
        outlet.photoSlots.chunked(2).forEachIndexed { rowIndex, rowSlots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowSlots.forEach { slot ->
                    val isCaptured = slot.imagePath != null
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (isCaptured) Modifier.clickable { onViewPhoto(slot) }
                                else Modifier
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isCaptured) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = slot.imagePath,
                                    contentDescription = slot.label,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .background(AppColors.greyF6, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = AppColors.PlaceholderTextColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = slot.label,
                            style = AppTypography.BodyPrimary.copy(fontSize = 12.sp),
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (slot.required) "Required" else "Optional",
                            style = AppTypography.Caption.copy(fontSize = 11.sp),
                            color = AppColors.TextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (rowSlots.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
            if (rowIndex < (outlet.photoSlots.size + 1) / 2 - 1) {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Onboarding Photos Card (from API) ──

@Composable
fun OnboardingPhotosCard(
    outlet: OutletItem,
    onImageClick: ((String) -> Unit)? = null
) {
    if (outlet.onboardingPhotos.isEmpty()) return

    // Internal full-screen viewer state (used when no external handler is provided)
    var selectedUrl by remember { mutableStateOf<String?>(null) }

    SectionCard(
        "Onboarding Photos",
        trailing = "${outlet.onboardingPhotos.size} photo${if (outlet.onboardingPhotos.size != 1) "s" else ""}"
    ) {
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(outlet.onboardingPhotos.size) { index ->
                val photo = outlet.onboardingPhotos[index]
                val typeLabel = when (photo.photoType) {
                    "SHOP_FRONT" -> "Shop Front"
                    "INSIDE_SHOP" -> "Inside Shop"
                    "COOLER_AREA" -> "Cooler Area"
                    else -> photo.photoType.replace("_", " ")
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                }

                Column(
                    modifier = Modifier
                        .width(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (onImageClick != null) onImageClick(photo.photoUrl)
                            else selectedUrl = photo.photoUrl
                        }
                ) {
                    Box {
                        AsyncImage(
                            model = photo.photoUrl,
                            contentDescription = typeLabel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentScale = ContentScale.Crop
                        )

                        // Label overlay
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(6.dp),
                            color = Color.Black.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = typeLabel,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = AppTypography.Caption.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Internal full-screen viewer
    if (selectedUrl != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedUrl = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { selectedUrl = null }
            ) {
                AsyncImage(
                    model = selectedUrl,
                    contentDescription = "Full Screen View",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

// ── Compliance Record Card (submitted Signature data) ──

@Composable
fun ComplianceRecordCard(outlet: OutletItem) {
    if (outlet.complianceRecords.isEmpty()) return

    val record = outlet.complianceRecords.first()
    var selectedUrl by remember { mutableStateOf<String?>(null) }

    val statusLabel = when {
        record.verified -> "Verified"
        outlet.assetStatus == AssetStatus.VERIFICATION_PENDING -> "Awaiting Verification"
        else -> "Submitted"
    }
    val statusColor = when {
        record.verified -> AppColors.Success
        else -> AppColors.BlueGradientStart
    }
    val statusBg = when {
        record.verified -> AppColors.greenE7
        else -> Color(0xFFDBEAFE)
    }

    SectionCard(
        "Compliance Details",
        trailing = statusLabel
    ) {
        // Status banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = statusBg
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (record.verified) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (record.verified) "Compliance verified by ${record.verifiedByName ?: "ASM"}"
                    else "Compliance submitted, awaiting ASM verification",
                    style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                    color = statusColor
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Cooler & Signage details
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                DetailField("Cooler Installed", if (record.coolerInstalled) "Yes" else "No")
            }
            Box(modifier = Modifier.weight(1f)) {
                DetailField("Signage Installed", if (record.signageInstalled) "Yes" else "No")
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                DetailField("Cooler Type", record.coolerType.ifBlank { "—" })
            }
            Box(modifier = Modifier.weight(1f)) {
                DetailField("Capacity", record.capacity.ifBlank { "—" })
            }
        }

        if (record.serialNo.isNotBlank()) {
            DetailField("Serial No", record.serialNo)
        }

        // Compliance photos
        if (record.images.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = AppColors.Divider)
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Compliance Photos",
                style = AppTypography.BodyPrimary.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(10.dp))

            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(record.images.size) { index ->
                    val image = record.images[index]
                    val typeLabel = when (image.imageType) {
                        "COOLER" -> "Cooler"
                        "ASSET_LABEL" -> "Asset Label"
                        "SIGNAGE" -> "Signage"
                        "OUTER_OUTLET" -> "Outer Outlet"
                        "INNER_OUTLET" -> "Inner Outlet"
                        else -> image.imageType.replace("_", " ")
                            .lowercase()
                            .replaceFirstChar { it.uppercase() }
                    }

                    Column(
                        modifier = Modifier
                            .width(140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedUrl = image.imageUrl }
                    ) {
                        Box {
                            AsyncImage(
                                model = image.imageUrl,
                                contentDescription = typeLabel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentScale = ContentScale.Crop
                            )

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp),
                                color = Color.Black.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = typeLabel,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = AppTypography.Caption.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full-screen image viewer
    if (selectedUrl != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedUrl = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { selectedUrl = null }
            ) {
                AsyncImage(
                    model = selectedUrl,
                    contentDescription = "Full Screen View",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

// ── Distributor Details Card ──

@Composable
fun DistributorDetailsCard(outlet: OutletItem) {
    val uriHandler = LocalUriHandler.current

    SectionCard("Distributor Details") {
        DetailField("Distributor Name", outlet.distributorName)
        if (outlet.distributorContact.isNotBlank()) {
            TappablePhoneField(
                label = "Contact Number",
                number = outlet.distributorContact,
                onClick = { uriHandler.openUri("tel:${outlet.distributorContact}") }
            )
        }
    }
}

// ── Payment Mode Card ──

@Composable
fun PaymentModeCard(outlet: OutletItem) {
    val hasBank = outlet.bankName.isNotBlank()
    val hasUpi = outlet.upiId.isNotBlank()

    SectionCard("Payment Mode") {
        if (hasBank) {
            PaymentMethodRow(
                icon = Icons.Default.AccountBalance,
                method = "Bank Transfer",
                detail = outlet.bankName,
                isActive = true
            )
        }

        if (hasBank && hasUpi) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AppColors.greyF6)
            Spacer(Modifier.height(8.dp))
        }

        if (hasUpi) {
            PaymentMethodRow(
                icon = Icons.Default.AccountBalance,
                method = "UPI",
                detail = outlet.upiId.let {
                    val atIndex = it.indexOf('@')
                    if (atIndex > 3) "${it.take(3)}***${it.substring(atIndex)}"
                    else it
                },
                isActive = true
            )
        }
    }
}

// ── Agreement Status Card ──

@Composable
fun AgreementStatusCard(outlet: OutletItem) {
    SectionCard("Agreement Status") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (outlet.agreementAccepted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (outlet.agreementAccepted) AppColors.Success else AppColors.PlaceholderTextColor,
                modifier = Modifier.size(22.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (outlet.agreementAccepted) "Signed & Submitted" else "Not yet signed",
                    style = AppTypography.BodyPrimary.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (outlet.agreementAccepted) AppColors.TextPrimary else AppColors.TextSecondary
                )
                Text(
                    text = "PFP Agreement",
                    style = AppTypography.Caption.copy(fontSize = 11.sp),
                    color = AppColors.TextTertiary
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        if (outlet.agreementAccepted) AppColors.greenE7 else AppColors.orangeEB,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (outlet.agreementAccepted) "Accepted" else "Pending",
                    style = AppTypography.Caption.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (outlet.agreementAccepted) AppColors.Success else AppColors.Warning
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Primitive Reusable Helpers (internal)
// ═══════════════════════════════════════════════════════

@Composable
fun SectionCard(
    title: String,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.TextPrimary
                )
                if (trailing != null) {
                    Text(
                        text = trailing,
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = AppColors.TextTertiary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
fun DetailField(label: String, value: String) {
    if (value.isBlank()) return

    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label,
            style = AppTypography.Caption.copy(fontSize = 11.sp),
            color = AppColors.TextTertiary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
            color = AppColors.TextPrimary
        )
    }
}

@Composable
fun TappablePhoneField(
    label: String,
    number: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label,
            style = AppTypography.Caption.copy(fontSize = 11.sp),
            color = AppColors.TextTertiary
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .background(AppColors.lightBlueFF, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call $number",
                tint = AppColors.BlueGradientStart,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = number,
                style = AppTypography.BodyPrimary.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                color = AppColors.BlueGradientStart
            )
        }
    }
}

@Composable
fun PaymentMethodRow(
    icon: ImageVector,
    method: String,
    detail: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isActive) AppColors.blueFE else AppColors.greyF6,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) AppColors.BlueGradientStart else AppColors.PlaceholderTextColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = method,
                style = AppTypography.BodyPrimary.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextPrimary
            )
            Text(
                text = detail,
                style = AppTypography.Caption.copy(fontSize = 11.sp),
                color = AppColors.TextTertiary
            )
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (isActive) AppColors.BlueGradientStart else Color.Transparent,
                    CircleShape
                )
                .then(
                    if (!isActive) Modifier.border(1.dp, AppColors.Divider, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

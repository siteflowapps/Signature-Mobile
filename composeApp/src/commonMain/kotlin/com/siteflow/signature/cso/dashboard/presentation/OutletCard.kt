package com.siteflow.signature.cso.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.components.CompactSignatureStepper
import com.siteflow.signature.core.presentation.components.StepState
import com.siteflow.signature.core.presentation.components.StepperItem
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.cso.dashboard.data.AssetStatus
import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.dashboard.data.OutletStatus
import com.siteflow.signature.cso.dashboard.data.SignatureStep

// ══════════════════════════════════════════════════════════════════════
//  OutletCard
//  Redesigned list card with 3-zone layout:
//    Zone A — Identity: avatar, name, location, status badge
//    Zone B — Progress: draft mini-bar OR macro pipeline stepper
//    Zone C — Assets: cooler + branding chips (when requested)
//  Bottom: timestamp + context-aware CTA
// ══════════════════════════════════════════════════════════════════════

/**
 * Reusable outlet card for the CSO/ASE dashboard list.
 *
 * @param outlet         The outlet data.
 * @param onContinue     Called when the "Continue Setup" CTA is tapped (draft outlets).
 * @param onViewDetails  Called when "View Details" or pipeline-action CTA is tapped.
 * @param modifier       Modifier.
 */
@Composable
fun OutletCard(
    outlet: OutletItem,
    onContinue: () -> Unit = {},
    onViewDetails: () -> Unit = {},
    // legacy params kept for call-site compatibility
    actionLabel: String? = null,
    actionColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // ── Left colour bar (status-driven) ──
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        outlet.status.barColor,
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ════════════════════════════════
                //  Zone A — Identity
                // ════════════════════════════════
                OutletCardIdentityRow(outlet)

                // ════════════════════════════════
                //  Zone B — Progress
                // ════════════════════════════════
                if (outlet.isContinuingOnboarding) {
                    // Still in draft — show 6-step mini progress bar
                    DraftProgressBar(outlet)
                } else {
                    // Enrolled — show macro 5-step Signature Journey
                    OutletCardPipelineRow(outlet)
                }

                // ════════════════════════════════
                //  Zone C — Asset Status Chips
                //  (only when at least one asset requested)
                // ════════════════════════════════
                val coolerChip   = assetChipData("🧊", "Cooler", outlet.coolerComplianceStatus)
                val brandingChip = assetChipData("📣", "Branding", outlet.marketingComplianceStatus)

                if (coolerChip != null || brandingChip != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        coolerChip?.let   { AssetChip(it) }
                        brandingChip?.let { AssetChip(it) }
                    }
                }

                // ════════════════════════════════
                //  Bottom — timestamp + CTA
                // ════════════════════════════════
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = outlet.updatedTime.ifBlank { "" }.let {
                            if (it.isNotBlank()) "Updated $it" else ""
                        },
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary
                    )

                    val (ctaLabel, ctaColor) = resolveCtaLabel(outlet, actionLabel, actionColor)
                    Text(
                        text = ctaLabel,
                        style = AppTypography.BodyPrimary.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = ctaColor,
                        modifier = Modifier.clickable {
                            if (outlet.isContinuingOnboarding) onContinue() else onViewDetails()
                        }
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
//  Zone A — Identity row: avatar + name/location + status badge
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun OutletCardIdentityRow(outlet: OutletItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle with initials
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(outlet.status.bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = outlet.initials,
                style = AppTypography.TitleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = outlet.status.color
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = outlet.name,
                style = AppTypography.TitleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.black27,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (outlet.location.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AppColors.TextTertiary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = outlet.location,
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Status badge
        Box(
            modifier = Modifier
                .background(outlet.status.bgColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 4.dp)
        ) {
            Text(
                text = outlet.status.label,
                style = AppTypography.Caption.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = outlet.status.color,
                maxLines = 1
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
//  Zone B — Draft: thin 6-step progress bar (shown only for DRAFT_* statuses)
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun DraftProgressBar(outlet: OutletItem) {
    val currentStep = outlet.onboardingStep
    val total = 6

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Segmented fill bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            for (i in 1..total) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (i < currentStep) AppColors.StepDone
                                    else if (i == currentStep) AppColors.StepActive.copy(alpha = 0.6f)
                                    else AppColors.StepLine,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
        Text(
            text = "Setup: Step ${currentStep - 1} of $total",
            style = AppTypography.Caption.copy(fontSize = 10.sp),
            color = AppColors.TextTertiary
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────
//  Zone B — Macro pipeline: 5-step Signature Journey (post-enrollment)
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun OutletCardPipelineRow(outlet: OutletItem) {
    val steps = SignatureStep.entries.map { step ->
        val isDone     = step in outlet.completedSteps
        val isRejected = isStepRejected(outlet.status, step)
        val isActive   = !isDone && !isRejected && (step == outlet.nextPendingStep)

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

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CompactSignatureStepper(
            steps = steps,
            dotSize = 16.dp,
            modifier = Modifier.fillMaxWidth()
        )
        // Current step label
        val activeStep = steps.firstOrNull {
            it.state == StepState.ACTIVE || it.state == StepState.REJECTED
        }
        if (activeStep != null) {
            val prefix = if (activeStep.state == StepState.REJECTED) "❌" else "→"
            Text(
                text = "$prefix ${activeStep.label}",
                style = AppTypography.Caption.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = if (activeStep.state == StepState.REJECTED)
                    AppColors.StepRejected else AppColors.StepActive
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
//  Zone C — Asset chip
// ──────────────────────────────────────────────────────────────────────────

private data class AssetChipData(
    val emoji: String,
    val label: String,
    val color: Color,
    val bgColor: Color
)

private fun assetChipData(emoji: String, prefix: String, status: String): AssetChipData? {
    val (label, color, bg) = when (status) {
        "REQUESTED"            -> Triple("Requested",        AppColors.AssetPending,   AppColors.AssetPendingBg)
        "ASE_APPROVED",
        "PENDING_PHOTO"        -> Triple("L1 Approved",      AppColors.AssetApproved,  AppColors.AssetApprovedBg)
        "ASM_APPROVED",
        "MARKETING_APPROVED"   -> Triple("L2 Approved",      AppColors.AssetApproved,  AppColors.AssetApprovedBg)
        "EXECUTED"             -> Triple("Installed",         AppColors.AssetExecuted,  AppColors.AssetExecutedBg)
        "SUBMITTED",
        "COMPLIANCE_SUBMITTED" -> Triple("Compliance Due",   AppColors.AssetExecuted,  AppColors.AssetExecutedBg)
        "COMPLIANT"            -> Triple("Compliant ✓",      AppColors.AssetCompliant, AppColors.AssetCompliantBg)
        "OVERDUE",
        "COMPLIANCE_OVERDUE"   -> Triple("Overdue ⚠",        AppColors.AssetOverdue,   AppColors.AssetOverdueBg)
        "NON_COMPLIANT"        -> Triple("Non-compliant",    AppColors.AssetOverdue,   AppColors.AssetOverdueBg)
        else                   -> return null // NOT_REQUESTED — render nothing
    }
    return AssetChipData("$emoji", "$prefix: $label", color, bg)
}

@Composable
private fun AssetChip(data: AssetChipData) {
    Box(
        modifier = Modifier
            .background(data.bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "${data.emoji} ${data.label}",
            style = AppTypography.Caption.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = data.color
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────
//  CTA label resolver — context-aware action text
// ──────────────────────────────────────────────────────────────────────────

private fun resolveCtaLabel(
    outlet: OutletItem,
    overrideLabel: String?,
    overrideColor: Color?
): Pair<String, Color> {
    if (overrideLabel != null && overrideColor != null) return overrideLabel to overrideColor

    return when {
        outlet.isContinuingOnboarding                          ->
            "Continue Setup →"  to AppColors.StepActive
        outlet.status == OutletStatus.ASE_REJECTED ||
        outlet.status == OutletStatus.ASM_REJECTED             ->
            "Fix & Resubmit →"  to AppColors.StepRejected
        outlet.status == OutletStatus.ASM_APPROVED &&
        outlet.assetStatus == AssetStatus.NOT_REQUESTED        ->
            "Start Signature →" to AppColors.Primary
        outlet.coolerNeedsCompliance                           ->
            "Upload Compliance →" to AppColors.Warning
        outlet.status == OutletStatus.ONBOARDED                ->
            "View Report →"     to AppColors.StepDone
        else                                                   ->
            "View Details →"    to AppColors.BlueGradientStart
    }
}

// ──────────────────────────────────────────────────────────────────────────
//  Helper — which step is in rejected state for a given status
// ──────────────────────────────────────────────────────────────────────────

private fun isStepRejected(status: OutletStatus, step: SignatureStep): Boolean = when (step) {
    SignatureStep.ASE_APPROVAL -> status == OutletStatus.ASE_REJECTED
    SignatureStep.ASM_APPROVAL -> status == OutletStatus.ASM_REJECTED
    else                       -> false
}

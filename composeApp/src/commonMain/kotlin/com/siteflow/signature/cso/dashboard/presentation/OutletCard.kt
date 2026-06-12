package com.siteflow.signature.cso.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import androidx.compose.ui.graphics.StrokeCap
import com.siteflow.signature.cso.dashboard.data.SignatureStep
import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.dashboard.data.OutletStatus
import com.siteflow.signature.cso.dashboard.data.AssetStatus

/**
 * Reusable outlet card with left color bar and Signature pipeline progress.
 */
@Composable
fun OutletCard(
    outlet: OutletItem,
    onContinue: () -> Unit = {},
    onViewDetails: () -> Unit = {},
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
            // Left color bar
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
                    .padding(16.dp)
            ) {
                // Row 1: Name + Slab badge + Status badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = outlet.name,
                            style = AppTypography.TitleMedium.copy(fontSize = 16.sp),
                            color = Color(0xFF111827)
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AppColors.TextTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = outlet.location,
                                style = AppTypography.Caption.copy(fontSize = 11.sp),
                                color = AppColors.TextTertiary
                            )
                        }
                    }

                    // Status badge
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

                // Onboarding Progress
                SignaturePipelineRow(outlet.completedSteps)

                // Next pending action OR status banner
                val isFullyVerified = outlet.assetStatus == AssetStatus.VERIFIED
                val isVerificationPending = outlet.assetStatus == AssetStatus.VERIFICATION_PENDING
                val nextStep = outlet.nextPendingStep
                Spacer(Modifier.height(8.dp))
                if (isFullyVerified) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AppColors.Success,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Fully Onboarded",
                            style = AppTypography.Caption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AppColors.Success
                        )
                    }
                } else if (isVerificationPending) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(AppColors.BlueGradientStart, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Awaiting Verification",
                            style = AppTypography.Caption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = AppColors.BlueGradientStart
                        )
                    }
                } else if (nextStep != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFFF59E0B), CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Next: ${nextStep.label}",
                            style = AppTypography.Caption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFFF59E0B)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Bottom row: Time + Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Updated: ${outlet.updatedTime}",
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary
                    )

                    Text(
                        text = actionLabel
                            ?: if (outlet.isContinuingOnboarding) "Continue Onboarding →" else "View Details →",
                        style = AppTypography.BodyPrimary.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = actionColor
                            ?: if (outlet.isContinuingOnboarding) Color(0xFFF59E0B) else AppColors.BlueGradientStart,
                        modifier = Modifier.clickable {
                            if (outlet.isContinuingOnboarding) onContinue() else onViewDetails()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Mini Signature pipeline progress — 5 step dots with check/empty indicators.
 */
@Composable
private fun SignaturePipelineRow(completedSteps: Set<SignatureStep>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "${completedSteps.size}/${SignatureStep.entries.size}",
            style = AppTypography.Caption.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            ),
            color = AppColors.TextTertiary
        )
        Spacer(Modifier.width(4.dp))

        SignatureStep.entries.forEach { step ->
            val isDone = step in completedSteps
            Icon(
                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = step.label,
                tint = if (isDone) AppColors.Success else Color(0xFFD1D5DB),
                modifier = Modifier.size(16.dp)
            )
        }

        // Progress bar
        Spacer(Modifier.width(4.dp))
        LinearProgressIndicator(
            progress = { completedSteps.size.toFloat() / SignatureStep.entries.size },
            modifier = Modifier
                .weight(1f)
                .height(4.dp),
            color = AppColors.Success,
            trackColor = Color(0xFFE5E7EB),
            strokeCap = StrokeCap.Round
        )
    }
}

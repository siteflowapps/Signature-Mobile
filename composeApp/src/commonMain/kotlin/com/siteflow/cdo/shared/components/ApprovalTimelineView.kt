package com.siteflow.cdo.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import com.siteflow.cdo.shared.data.ApprovalStep
import com.siteflow.cdo.shared.data.ApprovalStepStatus

/**
 * Reusable multi-level approval timeline composable.
 *
 * Shows a vertical stepper with:
 *   ✅ Approved steps (green)
 *   ❌ Rejected steps (red)
 *   🔄 Pending steps (amber, pulsing)
 *   ⏳ Not started (grey)
 *
 * Used by both outlet approval and invoice approval flows.
 */
@Composable
fun ApprovalTimelineView(
    steps: List<ApprovalStep>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Approval Timeline",
                style = AppTypography.Caption.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                ),
                color = AppColors.TextTertiary
            )

            Spacer(Modifier.height(16.dp))

            steps.forEachIndexed { index, step ->
                val isLast = index == steps.lastIndex

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Timeline indicator column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(32.dp)
                    ) {
                        // Status icon
                        TimelineIcon(step.status)

                        // Connecting line (except last)
                        if (!isLast) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(48.dp)
                                    .background(
                                        when {
                                            step.status == ApprovalStepStatus.APPROVED -> AppColors.Success.copy(alpha = 0.3f)
                                            step.status == ApprovalStepStatus.REJECTED -> AppColors.Danger.copy(alpha = 0.3f)
                                            else -> AppColors.greyEB
                                        }
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    // Content
                    Column(modifier = Modifier.weight(1f)) {
                        // Level label + status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = step.level.label,
                                style = AppTypography.BodyPrimary.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = AppColors.black27
                            )

                            // Status pill
                            val (statusText, statusColor, statusBg) = when (step.status) {
                                ApprovalStepStatus.APPROVED -> Triple("Approved", AppColors.Success, Color(0xFFD1FAE5))
                                ApprovalStepStatus.REJECTED -> Triple("Rejected", AppColors.Danger, Color(0xFFFEE2E2))
                                ApprovalStepStatus.PENDING -> Triple("Pending", Color(0xFFF59E0B), Color(0xFFFEF3C7))
                                ApprovalStepStatus.NOT_STARTED -> Triple("Waiting", AppColors.TextTertiary, AppColors.greyF6)
                            }
                            Box(
                                modifier = Modifier
                                    .background(statusBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    style = AppTypography.Caption.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = statusColor
                                )
                            }
                        }

                        // Approver name
                        if (step.approverName != null) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = step.approverName,
                                style = AppTypography.Caption.copy(fontSize = 12.sp),
                                color = AppColors.TextSecondary
                            )
                        }

                        // Timestamp
                        if (step.timestamp != null) {
                            Text(
                                text = step.timestamp,
                                style = AppTypography.Caption.copy(fontSize = 11.sp),
                                color = AppColors.TextTertiary
                            )
                        }

                        // Rejection note
                        if (step.note != null && step.status == ApprovalStepStatus.REJECTED) {
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "\"${step.note}\"",
                                    style = AppTypography.Caption.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = AppColors.Danger
                                )
                            }
                        }

                        // Spacing before next step
                        if (!isLast) {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineIcon(status: ApprovalStepStatus) {
    when (status) {
        ApprovalStepStatus.APPROVED -> Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Approved",
            tint = AppColors.Success,
            modifier = Modifier.size(24.dp)
        )
        ApprovalStepStatus.REJECTED -> Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Rejected",
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .background(AppColors.Danger, CircleShape)
                .padding(4.dp)
        )
        ApprovalStepStatus.PENDING -> Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Pending",
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(24.dp)
        )
        ApprovalStepStatus.NOT_STARTED -> Icon(
            imageVector = Icons.Outlined.Circle,
            contentDescription = "Not started",
            tint = AppColors.greyEB,
            modifier = Modifier.size(24.dp)
        )
    }
}

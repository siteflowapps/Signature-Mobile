package com.siteflow.signature.core.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography

/**
 * The display state of a single step in the Signature pipeline stepper.
 */
enum class StepState {
    DONE,     // Green — completed
    ACTIVE,   // Blue  — current step (animated pulse)
    PENDING,  // Grey  — not yet reached
    REJECTED  // Red   — rejected at this step
}

/**
 * Data for a single step pill in the stepper.
 *
 * @param caption Optional status line shown under the label in the vertical
 *   timeline (e.g. "Awaiting L2 approval"). Falls back to a state-derived
 *   default when null. Ignored by the horizontal/compact steppers.
 */
data class StepperItem(
    val label: String,
    val state: StepState,
    val caption: String? = null
)

// ── Colour helpers ──────────────────────────────────────────────────────────

private fun stepDotColor(state: StepState) = when (state) {
    StepState.DONE     -> AppColors.StepDone
    StepState.ACTIVE   -> AppColors.StepActive
    StepState.PENDING  -> AppColors.StepPending
    StepState.REJECTED -> AppColors.StepRejected
}

private fun stepDotBg(state: StepState) = when (state) {
    StepState.DONE     -> AppColors.StepDoneBg
    StepState.ACTIVE   -> AppColors.StepActiveBg
    StepState.PENDING  -> AppColors.StepPendingBg
    StepState.REJECTED -> AppColors.StepRejectedBg
}

// ════════════════════════════════════════════════════════════════════════════
// HorizontalSignatureStepper
// Full-size stepper used in Outlet Detail page — dots + labels below.
// ════════════════════════════════════════════════════════════════════════════

/**
 * Full-width horizontal stepper with labelled dots and connector lines.
 * Used in the Outlet Detail Signature Journey card.
 *
 * @param steps       Ordered list of steps with their display state.
 * @param dotSize     Diameter of each step circle (default 26 dp).
 * @param lineHeight  Thickness of connector line (default 3 dp).
 * @param modifier    Modifier.
 */
@Composable
fun HorizontalSignatureStepper(
    steps: List<StepperItem>,
    dotSize: Dp = 26.dp,
    lineHeight: Dp = 3.dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // ── Row of dots + connector lines ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                // Dot
                StepDot(state = step.state, size = dotSize)

                // Connector line (after every step except the last)
                if (index < steps.size - 1) {
                    val leftDone  = step.state == StepState.DONE
                    val rightDone = steps[index + 1].state == StepState.DONE
                    val lineFilled = leftDone && rightDone

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(lineHeight)
                            .background(
                                if (lineFilled)
                                    Brush.horizontalGradient(
                                        listOf(AppColors.StepDone, AppColors.StepDone)
                                    )
                                else
                                    Brush.horizontalGradient(
                                        listOf(AppColors.StepLine, AppColors.StepLine)
                                    )
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // ── Row of labels ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, step ->
                // First left-align, last right-align, others center
                val textAlign = when (index) {
                    0                  -> TextAlign.Start
                    steps.size - 1     -> TextAlign.End
                    else               -> TextAlign.Center
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = when (index) {
                        0              -> Alignment.CenterStart
                        steps.size - 1 -> Alignment.CenterEnd
                        else           -> Alignment.Center
                    }
                ) {
                    Text(
                        text = step.label,
                        style = AppTypography.Caption.copy(
                            fontSize = 10.sp,
                            fontWeight = if (step.state == StepState.ACTIVE ||
                                step.state == StepState.DONE) FontWeight.SemiBold
                            else FontWeight.Normal
                        ),
                        color = when (step.state) {
                            StepState.DONE     -> AppColors.StepDone
                            StepState.ACTIVE   -> AppColors.StepActive
                            StepState.PENDING  -> AppColors.StepPending
                            StepState.REJECTED -> AppColors.StepRejected
                        },
                        textAlign = textAlign,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// VerticalSignatureTimeline
// Premium top-to-bottom timeline used in the Outlet Detail Signature Journey card.
// One step per row: status dot + connector on the left, title + caption on the
// right. The active row gets a soft highlighted pill. No perpetual animation.
// ════════════════════════════════════════════════════════════════════════════

/**
 * Vertical journey timeline. Reads top-to-bottom so each step gets room to
 * breathe — no cramped horizontal labels.
 *
 * @param steps Ordered list of steps with their display state (+ optional caption).
 */
@Composable
fun VerticalSignatureTimeline(
    steps: List<StepperItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            val isLast = index == steps.size - 1
            val isActive = step.state == StepState.ACTIVE
            // The rail below a dot only "fills" (green) once that step is done, so
            // the connector reaching the active step reflects real progress.
            val connectorColor =
                if (step.state == StepState.DONE) AppColors.StepDone else AppColors.StepLine

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // ── Left rail: dot + connector line ──
                Column(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimelineDot(step.state)
                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(connectorColor)
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                // ── Right content: title + status caption ──
                val titleColor = when (step.state) {
                    StepState.DONE     -> AppColors.TextPrimary
                    StepState.ACTIVE   -> AppColors.StepActive
                    StepState.PENDING  -> AppColors.TextTertiary
                    StepState.REJECTED -> AppColors.StepRejected
                }
                val captionColor = when (step.state) {
                    StepState.DONE     -> AppColors.StepDone
                    StepState.ACTIVE   -> AppColors.StepActive
                    StepState.PENDING  -> AppColors.TextTertiary
                    StepState.REJECTED -> AppColors.StepRejected
                }
                val caption = step.caption ?: when (step.state) {
                    StepState.DONE     -> "Completed"
                    StepState.ACTIVE   -> "In progress"
                    StepState.PENDING  -> "Pending"
                    StepState.REJECTED -> "Rejected"
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (isLast) 0.dp else 20.dp)
                ) {
                    Column(
                        modifier = if (isActive)
                            Modifier
                                .fillMaxWidth()
                                .background(AppColors.StepActiveBg, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        else
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                    ) {
                        Text(
                            text = step.label,
                            style = AppTypography.BodyPrimary.copy(
                                fontSize = 14.sp,
                                fontWeight = if (step.state == StepState.PENDING)
                                    FontWeight.Normal else FontWeight.SemiBold
                            ),
                            color = titleColor
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = caption,
                            style = AppTypography.Caption.copy(
                                fontSize = 12.sp,
                                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                            ),
                            color = captionColor
                        )
                    }
                }
            }
        }
    }
}

// ── TimelineDot — status marker for the vertical timeline (no animation) ──
@Composable
private fun TimelineDot(state: StepState) {
    val dotColor = stepDotColor(state)
    Box(
        modifier = Modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft, static glow ring behind the active marker — premium, calm, no pulse.
        if (state == StepState.ACTIVE) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(AppColors.StepActive.copy(alpha = 0.16f), CircleShape)
            )
        }
        when (state) {
            StepState.DONE -> Box(
                modifier = Modifier.size(22.dp).background(dotColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
            StepState.REJECTED -> Box(
                modifier = Modifier.size(22.dp).background(dotColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Rejected",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
            StepState.ACTIVE -> Box(
                modifier = Modifier.size(22.dp).background(dotColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(8.dp).background(Color.White, CircleShape)
                )
            }
            StepState.PENDING -> Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(AppColors.CardBackground, CircleShape)
                    .border(2.dp, AppColors.StepLine, CircleShape)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// CompactSignatureStepper
// Compact version used in the Outlet List Card — dots only, no labels.
// ════════════════════════════════════════════════════════════════════════════

/**
 * Compact dot-only stepper with connector lines. No step labels.
 * Used in the Outlet List Card for glanceable pipeline progress.
 *
 * @param steps     Ordered list of steps with their display state.
 * @param dotSize   Diameter of each step circle (default 14 dp).
 */
@Composable
fun CompactSignatureStepper(
    steps: List<StepperItem>,
    dotSize: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            StepDot(state = step.state, size = dotSize, showIcon = false)

            if (index < steps.size - 1) {
                val lineFilled = step.state == StepState.DONE &&
                        steps[index + 1].state == StepState.DONE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (lineFilled) AppColors.StepDone else AppColors.StepLine
                        )
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// StepDot — private building block
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun StepDot(
    state: StepState,
    size: Dp,
    showIcon: Boolean = true
) {
    val dotColor  = stepDotColor(state)
    val dotBg     = stepDotBg(state)

    // Infinite scale pulse animation for the active step only
    val scale = if (state == StepState.ACTIVE) {
        val infiniteTransition = rememberInfiniteTransition(label = "stepPulse")
        val pulse by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue  = 1.18f,
            animationSpec = infiniteRepeatable(
                animation  = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        pulse
    } else 1f

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .background(dotBg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            state == StepState.DONE && showIcon -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Done",
                tint = dotColor,
                modifier = Modifier.size(size * 0.55f)
            )
            state == StepState.REJECTED && showIcon -> Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Rejected",
                tint = dotColor,
                modifier = Modifier.size(size * 0.55f)
            )
            else -> Box(
                modifier = Modifier
                    .size(size * 0.45f)
                    .background(dotColor, CircleShape)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// Onboarding Step Indicator
// Used in OnboardingTopAppBar — 6 fixed steps with number labels.
// ════════════════════════════════════════════════════════════════════════════

/**
 * Onboarding-specific 6-step progress indicator.
 *
 * @param currentStep  1-indexed active step (1..totalSteps).
 * @param totalSteps   Total number of steps (default 6).
 * @param stepLabels   Short label for each step (shown below active dot only).
 */
@Composable
fun OnboardingStepIndicator(
    currentStep: Int,
    totalSteps: Int = 6,
    stepLabels: List<String> = listOf(
        "Basic Info", "Economics", "Distributor", "KYC", "Photos", "Agreement"
    ),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // ── Dots + connectors ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..totalSteps) {
                val state = when {
                    i < currentStep  -> StepState.DONE
                    i == currentStep -> StepState.ACTIVE
                    else             -> StepState.PENDING
                }
                StepDot(state = state, size = 20.dp)

                if (i < totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (i < currentStep) AppColors.StepDone else AppColors.StepLine
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── "Step X of Y · Label" text ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step $currentStep of $totalSteps",
                style = AppTypography.Caption.copy(fontSize = 12.sp),
                color = AppColors.TextTertiary
            )
            val label = stepLabels.getOrElse(currentStep - 1) { "Step $currentStep" }
            Text(
                text = label,
                style = AppTypography.Caption.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.StepActive
            )
        }
    }
}

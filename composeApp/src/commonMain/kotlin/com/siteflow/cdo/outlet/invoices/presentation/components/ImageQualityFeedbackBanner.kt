package com.siteflow.cdo.outlet.invoices.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.core.domain.model.ImageQualityIssue
import com.siteflow.cdo.core.domain.model.isHardBlock
import com.siteflow.cdo.core.domain.model.userMessage

/**
 * Shows image quality issues detected during preprocessing.
 *
 * Two severity levels:
 *  - HARD block (red): [ImageQualityIssue.NoDocumentEdges] / [ImageQualityIssue.DocumentTooSmall]
 *    → User must retake the photo.
 *  - Soft warning (amber): Blur / brightness issues
 *    → User is warned but can still proceed.
 *
 * @param issues Quality issues detected. Empty → banner is hidden.
 * @param onRetake Called when user taps "Retake Photo" button.
 * @param onProceed Called when user taps "Proceed Anyway" (soft warnings only).
 */
@Composable
fun ImageQualityFeedbackBanner(
    issues: List<ImageQualityIssue>,
    onRetake: () -> Unit,
    onProceed: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val visible = issues.isNotEmpty()
    val hasHardBlock = issues.any { it.isHardBlock }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        val bgColor = if (hasHardBlock) Color(0xFFFEE2E2) else Color(0xFFFEF9C3)
        val borderColor = if (hasHardBlock) Color(0xFFEF4444) else Color(0xFFF59E0B)
        val iconColor = if (hasHardBlock) Color(0xFFDC2626) else Color(0xFFD97706)
        val titleColor = if (hasHardBlock) Color(0xFF991B1B) else Color(0xFF92400E)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor, RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (hasHardBlock) Icons.Default.Warning else Icons.Default.Info,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (hasHardBlock) "Photo cannot be used" else "Image quality warning",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = titleColor
                )
            }

            // Issue list
            issues.forEach { issue ->
                Text(
                    text = "• ${issue.userMessage}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = titleColor,
                    modifier = Modifier.padding(start = 28.dp)
                )
            }

            Spacer(Modifier.height(2.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRetake,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasHardBlock) Color(0xFFDC2626) else Color(0xFFD97706)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Retake Photo",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = Color.White
                    )
                }

                // Only show "Proceed" for soft warnings
                if (!hasHardBlock && onProceed != null) {
                    OutlinedButton(
                        onClick = onProceed,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD97706)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Proceed Anyway",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

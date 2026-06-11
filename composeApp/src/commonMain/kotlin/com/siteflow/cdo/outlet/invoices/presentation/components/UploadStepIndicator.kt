package com.siteflow.cdo.outlet.invoices.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import com.siteflow.cdo.outlet.invoices.data.UploadStep

@Composable
fun UploadStepIndicator(currentStep: UploadStep) {
    val steps = UploadStep.entries

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        steps.forEachIndexed { index, step ->
            val isActive = step.stepNumber <= currentStep.stepNumber
            val isCurrent = step == currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Numbered circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCurrent) AppColors.BlueGradientStart
                            else if (isActive) AppColors.BlueGradientStart.copy(alpha = 0.15f)
                            else Color(0xFFF3F4F6)
                        )
                        .then(
                            if (!isCurrent && !isActive) Modifier.border(
                                1.dp, Color(0xFFD1D5DB), CircleShape
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.stepNumber.toString(),
                        style = AppTypography.BodyPrimary.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isCurrent) Color.White
                        else if (isActive) AppColors.BlueGradientStart
                        else Color(0xFF9CA3AF)
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Step label
                Text(
                    text = step.label,
                    style = AppTypography.Caption.copy(
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isCurrent) AppColors.BlueGradientStart
                    else if (isActive) Color(0xFF374151)
                    else Color(0xFF9CA3AF)
                )
            }

            // Connector line between steps
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(top = 18.dp)
                        .height(2.dp)
                        .background(
                            if (step.stepNumber < currentStep.stepNumber) AppColors.BlueGradientStart
                            else Color(0xFFE5E7EB),
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}

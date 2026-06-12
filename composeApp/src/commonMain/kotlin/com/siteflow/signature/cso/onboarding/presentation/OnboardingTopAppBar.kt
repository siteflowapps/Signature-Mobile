package com.siteflow.signature.cso.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography

/**
 * Centralized top app bar for the Onboarding flow.
 * SiteFlow pattern: back arrow + title + step progress bar + divider.
 */
@Composable
fun OnboardingTopAppBar(
    currentStep: Int = 1,
    totalSteps: Int = 3,
    stepLabel: String = "Basic Details",
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Title row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF111827)
                )
            }
            Text(
                text = "New Outlet Onboarding",
                fontSize = 20.sp,
                style = AppTypography.TitleMedium,
                color = Color(0xFF111827)
            )
        }

        // Step progress bar
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                color = if (i <= currentStep) AppColors.BlueGradientStart
                                else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Step $currentStep of $totalSteps",
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = AppColors.TextTertiary
                )
                Text(
                    text = stepLabel,
                    style = AppTypography.Caption.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.BlueGradientStart
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFE5E7EB),
            thickness = 1.dp
        )
    }
}

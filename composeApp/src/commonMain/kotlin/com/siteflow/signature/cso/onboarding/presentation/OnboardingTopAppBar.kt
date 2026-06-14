package com.siteflow.signature.cso.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.siteflow.signature.core.presentation.components.OnboardingStepIndicator
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography

/**
 * Centralized top app bar for the Onboarding flow.
 *
 * Replaces the old hard-coded 3-bar progress with a proper 6-step dot stepper
 * that matches the real onboarding flow:
 *   1 Basic Info · 2 Economics · 3 Distributor & Bank · 4 KYC · 5 Photos · 6 Agreement
 */
@Composable
fun OnboardingTopAppBar(
    currentStep: Int = 1,
    totalSteps: Int = 6,
    stepLabel: String = "Basic Info",   // kept for API compatibility (unused — derived from step)
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ── Title row ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.black27
                )
            }
            Text(
                text = "New Outlet Onboarding",
                fontSize = 18.sp,
                style = AppTypography.TitleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppColors.black27
            )
        }

        // ── 6-step progress indicator ──
        OnboardingStepIndicator(
            currentStep = currentStep,
            totalSteps = totalSteps,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(14.dp))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.Divider,
            thickness = 1.dp
        )
    }
}

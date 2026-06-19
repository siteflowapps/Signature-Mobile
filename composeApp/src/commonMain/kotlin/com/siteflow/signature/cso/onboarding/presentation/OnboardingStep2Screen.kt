package com.siteflow.signature.cso.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.cso.onboarding.domain.OnboardingAction
import com.siteflow.signature.cso.onboarding.domain.OnboardingEvent
import com.siteflow.signature.cso.onboarding.domain.OnboardingViewModel
import com.siteflow.signature.cso.onboarding.domain.OnboardingValidator
import com.siteflow.signature.core.presentation.components.SignatureButton
import com.siteflow.signature.core.presentation.components.SignatureTextField
import com.siteflow.signature.core.presentation.components.dismissKeyboardOnTap
import com.siteflow.signature.core.presentation.components.imeBottomPadding
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun OnboardingStep2Screen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    outletId: String? = null,
    viewModel: OnboardingViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(OnboardingAction.SetCurrentStep(2))
        // When resuming from dashboard, set the outlet ID in state
        outletId?.let { viewModel.onAction(OnboardingAction.SetOutletId(it)) }
        viewModel.events.collectLatest { event ->
            when (event) {
                OnboardingEvent.NavigateBack -> onBack()
                OnboardingEvent.NavigateToStep3 -> onContinue()
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .dismissKeyboardOnTap()
            .imeBottomPadding()
    ) {
        // ── Content (scrollable) ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // How Payout Works Card
            PayoutInfoCard()

            Spacer(Modifier.height(20.dp))

            // ── Outlet Economics ──
            Text(
                text = "Outlet Economics",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF374151)
            )

            Spacer(Modifier.height(12.dp))

            SignatureTextField(
                value = state.monthlyRentalAmount,
                onValueChange = { viewModel.onAction(OnboardingAction.MonthlyRentalAmountChanged(it)) },
                placeholder = "e.g. 15000",
                label = "Monthly Rental Amount",
                required = true,
                prefix = "₹",
                leadingIconVector = Icons.Default.CurrencyRupee,
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(16.dp))

            SignatureTextField(
                value = state.expectedSalesPotential,
                onValueChange = { viewModel.onAction(OnboardingAction.ExpectedSalesPotentialChanged(it)) },
                placeholder = "e.g. 50000",
                label = "Expected Sales Potential",
                required = true,
                prefix = "₹",
                leadingIconVector = Icons.Default.TrendingUp,
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(24.dp))
        }

        // ── Bottom Button ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp)
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            SignatureButton(
                text = "Continue",
                onClick = { viewModel.onAction(OnboardingAction.ContinueToNextStep) },
                enabled = OnboardingValidator.isStep2Valid(state) && !state.isLoading,
                loading = state.isLoading
            )
        }
    }
}

@Composable
private fun PayoutInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0F766E), Color(0xFF14B8A6))
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "💰 How Payout Works",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                PayoutInfoRow(
                    icon = Icons.Default.CurrencyRupee,
                    iconBg = Color(0xFFF0FDFA),
                    iconTint = Color(0xFF0D9488),
                    title = "Fixed Monthly Rental",
                    subtitle = "The outlet earns a fixed rental every month for hosting Signature coolers and branding."
                )

                Spacer(Modifier.height(14.dp))

                PayoutInfoRow(
                    icon = Icons.Default.TrendingUp,
                    iconBg = Color(0xFFECFDF5),
                    iconTint = Color(0xFF059669),
                    title = "Performance Bonus",
                    subtitle = "Beat the monthly sales targets to unlock an extra performance payout on top of the rental."
                )

                Spacer(Modifier.height(14.dp))

                PayoutInfoRow(
                    icon = Icons.Default.Percent,
                    iconBg = Color(0xFFFFF7ED),
                    iconTint = Color(0xFFEA580C),
                    title = "Rental Stays Fair to Sales",
                    subtitle = "Rental is kept in healthy proportion to the outlet's monthly sales — that's why we capture both below."
                )
            }
        }
    }
}

@Composable
private fun PayoutInfoRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.TitleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = AppTypography.Caption.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun InfoBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFEF3C7), // amber-100 (solid, matches Step 1)
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = Color(0xFFD97706), // amber-600
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
            color = Color(0xFF92400E) // amber-800
        )
    }
}


package com.siteflow.signature.outlet.walkthrough.presentation


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.outlet.walkthrough.data.OutletKycData
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughAction
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughEvent
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun WalkthroughPaymentSummaryScreen(
    onNavigateToDashboard: () -> Unit,
    onBack: () -> Unit,
    viewModel: WalkthroughViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(WalkthroughAction.LoadKyc)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                WalkthroughEvent.NavigateToDashboard -> onNavigateToDashboard()
                WalkthroughEvent.NavigateBack -> onBack()
                else -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppColors.BackgroundGradientStart, AppColors.BackgroundGradientEnd)
                )
            )
    ) {
        // ── Sticky header ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.BackgroundGradientStart)
                .padding(start = 8.dp, end = 20.dp, top = 48.dp, bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { viewModel.onAction(WalkthroughAction.GoBack) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.black27
                    )
                }
                WalkthroughStepIndicator(currentStep = 4, totalSteps = 4)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Payment & KYC Summary",
                color = AppColors.black27,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Review your payment and verification details",
                color = AppColors.WalkthroughTextSubtle,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = AppColors.Divider)
        }

        // ── Body ──────────────────────────────────────────────────────────────
        when {
            state.isLoadingKyc -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.BlueGradientStart)
                }
            }

            state.kycError != null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.kycError!!,
                            color = AppColors.TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        TextButton(
                            onClick = { viewModel.onAction(WalkthroughAction.RetryLoadKyc) }
                        ) {
                            Text("Retry", color = AppColors.BlueGradientStart, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val kyc = state.kycData

                    // Bank Details Card
                    PaymentInfoCard(
                        title = "💳  Bank Details",
                        rows = buildList {
                            add("Account Holder" to (kyc?.accountHolderName ?: "—"))
                            add("Bank Name" to (kyc?.bankName ?: "—"))
                            add("Account Number" to (kyc?.bankAccountNumber ?: "—"))
                            add("IFSC Code" to (kyc?.ifscCode ?: "—"))
                            kyc?.branch?.let { add("Branch" to it) }
                            kyc?.upiId?.let { add("UPI ID" to it) }
                            kyc?.swiftCode?.let { add("SWIFT Code" to it) }
                        }
                    )

                    // Identity Verification Card
                    PaymentInfoCard(
                        title = "🪪  Identity Verification",
                        rows = buildList {
                            add("ID Type" to (kyc?.idProofType?.toFriendlyLabel() ?: "—"))
                            add("ID Number" to (kyc?.idNumber ?: "—"))
                            kyc?.aadhaarNumber?.let { add("Aadhaar" to it) }
                            kyc?.gstNumber?.let { add("GST Number" to it) }
                        }
                    )

                    // Location Proof Card
                    PaymentInfoCard(
                        title = "📍  Location Verification",
                        rows = buildList {
                            add("Proof Type" to (kyc?.locationProofType?.toFriendlyLabel() ?: "—"))
                        }
                    )


                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // ── Sticky footer ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.CardBackground)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            HorizontalDivider(color = AppColors.Divider, modifier = Modifier.padding(bottom = 16.dp))
            Button(
                onClick = { viewModel.onAction(WalkthroughAction.FinishWalkthrough) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.green4A)
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun PaymentInfoCard(title: String, rows: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
    ) {
        // Card header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    AppColors.BlueGradientStart.copy(alpha = 0.06f),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = title,
                color = AppColors.WalkthroughTextBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        HorizontalDivider(color = AppColors.Divider)
        rows.forEachIndexed { index, (label, value) ->
            PaymentInfoRow(label = label, value = value)
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    color = AppColors.Divider.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = AppColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            color = AppColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun String.maskMiddle(): String {
    if (length <= 6) return this
    val start = take(3)
    val end = takeLast(3)
    val masked = "*".repeat((length - 6).coerceAtMost(8))
    return "$start$masked$end"
}

private fun String.toFriendlyLabel(): String = when (this.uppercase()) {
    "AADHAAR" -> "Aadhaar Card"
    "PAN" -> "PAN Card"
    "VOTER_ID" -> "Voter ID"
    "DRIVING_LICENSE" -> "Driving Licence"
    "PASSPORT" -> "Passport"
    "RENT_AGREEMENT" -> "Rent Agreement"
    "ELECTRICITY_BILL" -> "Electricity Bill"
    "PROPERTY_TAX" -> "Property Tax Receipt"
    "OWNERSHIP_DEED" -> "Ownership Deed"
    else -> this.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

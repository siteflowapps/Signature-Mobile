package com.siteflow.signature.cso.onboarding.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.cso.onboarding.domain.OnboardingAction
import com.siteflow.signature.cso.onboarding.domain.OnboardingEvent
import com.siteflow.signature.cso.onboarding.domain.OnboardingState
import com.siteflow.signature.cso.onboarding.domain.OnboardingViewModel
import com.siteflow.signature.cso.onboarding.domain.OnboardingValidator
import com.siteflow.signature.core.presentation.components.SignatureButton
import com.siteflow.signature.core.presentation.components.dismissKeyboardOnTap
import com.siteflow.signature.core.presentation.components.imeBottomPadding
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.shared.pfp.AgreementClause
import com.siteflow.signature.shared.pfp.PFP_CLAUSES
import com.siteflow.signature.shared.pfp.VOLUME_SLAB_TABLE
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject


// ── ID Proof display names (same as KYC screen) ──

private val ID_PROOF_NAMES = mapOf(
    "AADHAAR" to "Aadhaar Card Copy",
    "DRIVING_LICENSE" to "Valid Driving License Copy",
    "PASSPORT" to "Passport Copy",
    "VOTER_ID" to "Voter Identity Card Copy",
    "RATION_CARD" to "Ration Card",
    "BANK_PASSBOOK" to "Bank Passbook",
    "GAS_CONNECTION" to "Gas Connection Copy",
    "OTHER_GOVT_PROOF" to "Other Government Approved Proof"
)

private val LOCATION_PROOF_NAMES = mapOf(
    "FSSAI_LICENSE" to "Valid FSSAI License",
    "GST_CERTIFICATE" to "Valid GST Certificate",
    "SHOP_ESTABLISHMENT" to "Shop Establishment Certificate",
    "TRADE_LICENSE" to "Trade License",
    "ELECTRICITY_BILL" to "Recent Electricity Bill",
    "RENT_AGREEMENT" to "Rent Agreement",
    "GAS_CONNECTION" to "Gas Connection",
    "GRAM_PANCHAYAT" to "Gram Panchayat Certificate Copy",
    "INSTITUTION_LETTER" to "Authorized Letter from Institution",
    "NOTARIZED_DECLARATION" to "Notarized Address Declaration",
    "GAS_BILL" to "Gas Connection Bill Copy",
    "MUNICIPALITY_BILL" to "Municipality Bill Copy",
    "TELEPHONE_BILL" to "Shop's Telephone Bill",
    "MOBILE_POSTPAID_BILL" to "Mobile Postpaid Bill Copy"
)

// ═══════════════════════════════════════════════════════════════════════════════
// MAIN SCREEN
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingStep5Screen(
    onBack: () -> Unit,
    onComplete: () -> Unit,
    outletId: String? = null,
    viewModel: OnboardingViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var showAgreementSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onAction(OnboardingAction.SetCurrentStep(6))
        outletId?.let { viewModel.onAction(OnboardingAction.SetOutletId(it)) }
        viewModel.events.collectLatest { event ->
            when (event) {
                OnboardingEvent.NavigateBack -> onBack()
                OnboardingEvent.OnboardingSubmitted -> onComplete()
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
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // ── Performance Rewards Card ──
            PerformanceRewardsCard(
                onViewAgreement = { showAgreementSheet = true }
            )

            Spacer(Modifier.height(16.dp))

            // ── Bank Details Collapsible ──
            BankDetailsSection(
                accountHolder = state.accountHolderName,
                bankName = state.bankName,
                accountNumber = state.accountNumber,
                ifsc = state.ifscCode
            )

            Spacer(Modifier.height(16.dp))

            // ── Declaration Card ──
            DeclarationCard()

            Spacer(Modifier.height(24.dp))

            // ── Acceptance Checkbox ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.onAction(OnboardingAction.AgreementAccepted(!state.agreementAccepted)) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.agreementAccepted,
                    onCheckedChange = { viewModel.onAction(OnboardingAction.AgreementAccepted(it)) },
                    colors = CheckboxDefaults.colors(checkedColor = AppColors.blueEB)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "I have read and agree to the PFP enrollment terms and conditions.",
                    style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                    color = Color(0xFF374151)
                )
            }

            Spacer(Modifier.height(12.dp))
            
            // Error
            if (state.error != null) {
                Text(
                    text = state.error ?: "",
                    style = AppTypography.Caption,
                    color = AppColors.Danger,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))
        }

        // ── Bottom Action ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp)
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            if (!state.isOtpSent) {
                // Phase 1: Send OTP to retailer
                SignatureButton(
                    text = "Send OTP to Retailer",
                    onClick = { viewModel.onAction(OnboardingAction.RequestAgreementOtp) },
                    enabled = state.agreementAccepted && !state.isLoading,
                    loading = state.isLoading
                )
            } else {
                // Phase 2: Retailer gives OTP → ASE enters and verifies
                Column {
                    Text(
                        text = "Enter OTP",
                        style = AppTypography.TitleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF111827),
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp, top = 8.dp)
                    )

                    Text(
                        text = "A one-time password has been sent to the retailer's registered mobile number",
                        style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                        color = Color(0xFF6B7280),
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 6.dp)
                    )

                    Text(
                        text = "+91 ${state.contactNumber}",
                        style = AppTypography.BodyPrimary.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF111827),
                        modifier = Modifier.fillMaxWidth().padding(start = 3.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = state.agreementOtp,
                        onValueChange = { viewModel.onAction(OnboardingAction.AgreementOtpChanged(it)) },
                        label = { Text("6-digit OTP") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.blueEB,
                            unfocusedBorderColor = Color(0xFFD1D5DB)
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.onAction(OnboardingAction.RequestAgreementOtp) },
                            enabled = state.canResendAgreementOtp && !state.isLoading
                        ) {
                            Text(
                                text = if (state.canResendAgreementOtp) "Resend OTP"
                                       else "Resend OTP in ${state.agreementOtpCountdown}s",
                                color = if (state.canResendAgreementOtp) AppColors.blueEB else Color(0xFF9CA3AF)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    SignatureButton(
                        text = "Verify OTP & Submit",
                        onClick = { viewModel.onAction(OnboardingAction.VerifyAgreementOtp) },
                        enabled = state.agreementOtp.length == 6 && !state.isLoading,
                        loading = state.isLoading
                    )
                }
            }
        }
    }

    // ── Full Agreement Bottom Sheet ──
    if (showAgreementSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAgreementSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFFF9FAFB),
            dragHandle = null
        ) {
            AgreementSheetContent(
                state = state,
                onDismiss = { showAgreementSheet = false }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// FULL AGREEMENT BOTTOM SHEET CONTENT
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AgreementSheetContent(
    state: OnboardingState,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp)
    ) {
        // ── Header ──
        Text(
            text = "PFP Agreement Details",
            style = AppTypography.TitleMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF111827)
        )
        Text(
            text = "Campa Destination Outlet – Pay-for-Performance Program",
            style = AppTypography.Caption.copy(fontSize = 13.sp),
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(24.dp))

        // ═══════════════════════════════════════
        // SECTION 1: Outlet Information
        // ═══════════════════════════════════════
        AgreementSectionCard(
            icon = Icons.Default.Storefront,
            iconBg = Color(0xFFF0FDFA),
            iconTint = Color(0xFF0D9488),
            title = "Outlet Information"
        ) {
            DetailRow("Outlet Name", state.outletName)
            DetailRow("Owner Name", state.ownerName)
            DetailRow("Contact", state.contactNumber)
            DetailRow("Outlet Type", state.outletType)
            DetailRow("Address", buildString {
                if (state.address.isNotBlank()) append(state.address)
                if (state.landmark.isNotBlank()) append(", ${state.landmark}")
                if (state.locality.isNotBlank()) append(", ${state.locality}")
            })
            DetailRow("City", state.city)
            DetailRow("Pincode", state.pincode)
        }

        Spacer(Modifier.height(16.dp))

        // ═══════════════════════════════════════
        // SECTION 2: Classification & Volume
        // ═══════════════════════════════════════
        AgreementSectionCard(
            icon = Icons.Default.BarChart,
            iconBg = Color(0xFFF0FDF4),
            iconTint = Color(0xFF16A34A),
            title = "Classification"
        ) {
            DetailRow("Classification", state.selectedClassification.ifBlank { "—" })
        }

        Spacer(Modifier.height(16.dp))

        // ═══════════════════════════════════════
        // SECTION 3: Bank & Payment Details
        // ═══════════════════════════════════════
        AgreementSectionCard(
            icon = Icons.Default.AccountBalance,
            iconBg = Color(0xFFFFF7ED),
            iconTint = Color(0xFFEA580C),
            title = "Bank & Payment Details"
        ) {
            DetailRow("Account Holder", state.accountHolderName)
            DetailRow("Bank Name", state.bankName)
            DetailRow("Account Number", state.accountNumber)
            DetailRow("IFSC Code", state.ifscCode)
            if (state.branchName.isNotBlank()) {
                DetailRow("Branch", state.branchName)
            }
            if (state.upiId.isNotBlank()) {
                DetailRow("UPI ID", state.upiId)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ═══════════════════════════════════════
        // SECTION 4: KYC Summary
        // ═══════════════════════════════════════
        AgreementSectionCard(
            icon = Icons.Default.Badge,
            iconBg = Color(0xFFFDF2F8),
            iconTint = Color(0xFFDB2777),
            title = "KYC Summary"
        ) {
            DetailRow("ID Proof Type", ID_PROOF_NAMES[state.kycIdType] ?: state.kycIdType)
            DetailRow("ID Number", state.kycIdNumber)
            DetailRow("Location Proof", LOCATION_PROOF_NAMES[state.kycLocationType] ?: state.kycLocationType)
            if (state.kycGstNumber.isNotBlank()) {
                DetailRow("GST Number", state.kycGstNumber)
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFE5E7EB))
        Spacer(Modifier.height(24.dp))

        // ═══════════════════════════════════════
        // SECTION 5: Terms & Conditions
        // ═══════════════════════════════════════
        Text(
            text = "📜  Terms & Conditions",
            style = AppTypography.TitleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF111827)
        )
        Text(
            text = "Campa Destination Outlet – PFP Program",
            style = AppTypography.Caption.copy(fontSize = 13.sp),
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(16.dp))

        PFP_CLAUSES.forEach { clause ->
            ClauseItem(clause)
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(16.dp))

        // ── Annexure 1: Classification Slab ──
        Text(
            text = "Annexure 1: Classification Slab",
            style = AppTypography.TitleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF111827)
        )
        Spacer(Modifier.height(8.dp))
        OnboardingVolumeSlabTable()
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Payout applies on Beverage Category — Sparkling, Stills and Energy. Water is excluded.",
            style = AppTypography.BodyPrimary.copy(fontSize = 12.sp),
            color = Color(0xFF6B7280)
        )

        Spacer(Modifier.height(32.dp))

        // ── Close button ──
        SignatureButton(
            text = "Close",
            onClick = onDismiss
        )

        Spacer(Modifier.height(24.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AgreementSectionCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconBg, CircleShape),
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
                Text(
                    text = title,
                    style = AppTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF111827)
                )
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val display = value.ifBlank { "—" }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
            color = Color(0xFF6B7280),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = display,
            style = AppTypography.BodyPrimary.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF111827),
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun OnboardingVolumeSlabTable() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Slab", style = AppTypography.BodyPrimary.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = Color(0xFF6B7280), modifier = Modifier.weight(1f))
                Text("Volume (excl. water)", style = AppTypography.BodyPrimary.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = Color(0xFF6B7280), modifier = Modifier.weight(1.4f))
                Text("Payout/case", style = AppTypography.BodyPrimary.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = Color(0xFF6B7280))
            }
            HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 8.dp))
            VOLUME_SLAB_TABLE.forEach { (tier, volume, payout) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tier, style = AppTypography.BodyPrimary.copy(fontSize = 13.sp), color = Color(0xFF374151), modifier = Modifier.weight(1f))
                    Text(volume, style = AppTypography.BodyPrimary.copy(fontSize = 13.sp), color = Color(0xFF374151), modifier = Modifier.weight(1.4f))
                    Text(payout, style = AppTypography.BodyPrimary.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = Color(0xFF0D9488))
                }
            }
        }
    }
}

@Composable
private fun ClauseItem(clause: AgreementClause) {
    Column {
        Text(
            text = "${clause.id}. ${clause.title}",
            style = AppTypography.BodyPrimary.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color(0xFF111827)
        )
        Spacer(Modifier.height(4.dp))

        if (clause.text != null) {
            Text(
                text = clause.text,
                style = AppTypography.BodyPrimary.copy(
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                ),
                color = Color(0xFF4B5563)
            )
        }

        clause.subClauses.forEach { sub ->
            Row(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                Text(
                    text = "(${sub.first}) ",
                    style = AppTypography.BodyPrimary.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = sub.second,
                    style = AppTypography.BodyPrimary.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    ),
                    color = Color(0xFF4B5563)
                )
            }
        }
    }
}



// ═══════════════════════════════════════════════════════════════════════════════
// EXISTING SUB-COMPONENTS (unchanged)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PerformanceRewardsCard(onViewAgreement: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF0FDFA), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = null,
                        tint = Color(0xFF0D9488),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Performance-Based Rewards\nAcknowledgement",
                    style = AppTypography.TitleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    ),
                    color = Color(0xFF111827)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "By enrolling in this program, the retailer agrees that:",
                style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(12.dp))

            val points = listOf(
                "Incentives are paid based on clearly defined sales targets",
                "Performance will be tracked weekly or monthly",
                "Eligible payouts will be credited digitally or via credit note",
                "Consistent performance may unlock additional benefits"
            )

            points.forEach { point ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AppColors.Success,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = point,
                        style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                        color = Color(0xFF374151)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.clickable { onViewAgreement() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFF0D9488),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "View Full Agreement",
                    style = AppTypography.BodyPrimary.copy(
                        color = Color(0xFF0D9488),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun BankDetailsSection(
    accountHolder: String,
    bankName: String,
    accountNumber: String,
    ifsc: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFFF7ED), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color(0xFFEA580C),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Bank & Payment Details",
                            style = AppTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF111827)
                        )

                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    Spacer(Modifier.height(16.dp))
                    
                    BankDetailRow("Account Holder", accountHolder.ifBlank { "N/A" })
                    BankDetailRow("Bank Name", bankName.ifBlank { "N/A" })
                    BankDetailRow("Account Number", accountNumber.ifBlank { "N/A" })
                    BankDetailRow("IFSC Code", ifsc.ifBlank { "N/A" })
                }
            }
        }
    }
}

@Composable
private fun BankDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            style = AppTypography.BodyPrimary.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF111827)
        )
    }
}

@Composable
private fun DeclarationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color(0xFF0D9488),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "DECLARATION BY OUTLET OWNER",
                    style = AppTypography.TitleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = Color(0xFF0F766E)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "\"I voluntarily enroll my outlet in the RCPL Beverages Pay-for-Performance Program. I agree to promote RCPL beverages actively and understand that performance incentives are subject to defined targets and compliance. All data shared will remain confidential.\"",
                style = AppTypography.BodyPrimary.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontStyle = FontStyle.Italic
                ),
                color = Color(0xFF1E3A8A)
            )
        }
    }
}

package com.siteflow.signature.cso.onboarding.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.siteflow.signature.cso.onboarding.domain.BankAccountType
import com.siteflow.signature.cso.onboarding.domain.BankPaymentMode
import com.siteflow.signature.cso.onboarding.domain.OnboardingAction
import com.siteflow.signature.cso.onboarding.domain.OnboardingEvent
import com.siteflow.signature.cso.onboarding.domain.OnboardingViewModel
import com.siteflow.signature.cso.onboarding.domain.OnboardingValidator
import com.siteflow.signature.core.domain.ImagePicker
import com.siteflow.signature.core.presentation.components.SignatureButton
import com.siteflow.signature.core.presentation.components.dismissKeyboardOnTap
import com.siteflow.signature.core.presentation.components.SignatureDropdown
import com.siteflow.signature.core.presentation.components.SignatureTextField
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun OnboardingStep4Screen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    outletId: String? = null,
    viewModel: OnboardingViewModel = koinInject(),
    imagePicker: ImagePicker = koinInject()
) {
    val state by viewModel.state.collectAsState()

    var showChequeSourceSheet by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onAction(OnboardingAction.SetCurrentStep(3))
        viewModel.onAction(OnboardingAction.FetchDistributors)
        outletId?.let { viewModel.onAction(OnboardingAction.SetOutletId(it)) }
        viewModel.events.collectLatest { event ->
            when (event) {
                OnboardingEvent.NavigateBack -> onBack()
                OnboardingEvent.NavigateToStep4 -> onContinue()
                else -> {}
            }
        }
    }

    // Map distributors to dropdown options
    val distributorOptions = state.distributors.map { it.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .dismissKeyboardOnTap()
    ) {
        // ── Content (scrollable) ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Info Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Provide distributor and banking details for this outlet.",
                    style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                    color = Color(0xFF92400E)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ═══════════════════════════════════════
            // SECTION: Distributor Information
            // ═══════════════════════════════════════
            SectionHeader("Distributor Information")

            Spacer(Modifier.height(16.dp))

            SignatureDropdown(
                label = "Select Distributor",
                required = true,
                selected = state.distributorName,
                options = distributorOptions,
                placeholder = if (state.isDistributorsLoading) "Loading distributors…" else "Choose a distributor",
                onSelected = { name ->
                    val distributor = state.distributors.firstOrNull { it.name == name }
                    if (distributor != null) {
                        viewModel.onAction(OnboardingAction.DistributorSelected(distributor.id, distributor.name))
                    }
                }
            )

            Spacer(Modifier.height(28.dp))

            // Divider
            HorizontalDivider(color = Color(0xFFE5E7EB))

            Spacer(Modifier.height(24.dp))

            // ═══════════════════════════════════════
            // SECTION: Payment Method
            // ═══════════════════════════════════════
            SectionHeader("Payment Method")
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Choose how you'd like to receive payments for this outlet.",
                style = AppTypography.Caption.copy(fontSize = 13.sp),
                color = AppColors.TextTertiary
            )

            Spacer(Modifier.height(16.dp))

            // ── Segmented Tab Toggle ──
            PaymentModeToggle(
                selectedMode = state.bankPaymentMode,
                onModeSelected = { mode ->
                    viewModel.onAction(OnboardingAction.PaymentModeSelected(mode))
                }
            )

            Spacer(Modifier.height(24.dp))

            // ── Animated content switching ──
            AnimatedContent(
                targetState = state.bankPaymentMode,
                transitionSpec = {
                    fadeIn() + slideInHorizontally(
                        initialOffsetX = { if (targetState == BankPaymentMode.BANK) -it else it }
                    ) togetherWith fadeOut() + slideOutHorizontally(
                        targetOffsetX = { if (targetState == BankPaymentMode.BANK) it else -it }
                    )
                },
                label = "payment_section"
            ) { mode ->
                when (mode) {
                    BankPaymentMode.BANK -> BankDetailsSection(
                        state = state,
                        viewModel = viewModel,
                        onRequestChequeCapture = { showChequeSourceSheet = true }
                    )
                    BankPaymentMode.UPI -> UpiDetailsSection(
                        state = state,
                        viewModel = viewModel
                    )
                }
            }

            // Error
            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.error ?: "",
                    style = AppTypography.Caption,
                    color = AppColors.Danger,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Bottom Continue Button ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp)
                .background(Color.White)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            SignatureButton(
                text = "Continue",
                onClick = { viewModel.onAction(OnboardingAction.ContinueToNextStep) },
                enabled = OnboardingValidator.isStep3Valid(state) && !state.isLoading,
                loading = state.isLoading
            )
        }
    }

    // ── Camera Permission Dialog ──
    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = { Text("Camera Access Needed") },
            text = { Text("Please allow camera access to capture the cancelled cheque.") },
            confirmButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // ── Image Source Bottom Sheet ──
    if (showChequeSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChequeSourceSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Upload Cancelled Cheque",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Take Photo
                Surface(
                    onClick = {
                        showChequeSourceSheet = false
                        imagePicker.openCamera(
                            onImagePicked = { path ->
                                viewModel.onAction(OnboardingAction.CancelledChequeCaptured(path))
                            },
                            onPermissionDenied = {
                                showCameraPermissionDialog = true
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3F4F6)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = AppColors.BlueGradientStart,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Take Photo",
                            style = AppTypography.BodyPrimary.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF111827)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Choose from Gallery
                Surface(
                    onClick = {
                        showChequeSourceSheet = false
                        imagePicker.openGallery { path ->
                            viewModel.onAction(OnboardingAction.CancelledChequeCaptured(path))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3F4F6)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = AppColors.BlueGradientStart,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Choose from Gallery",
                            style = AppTypography.BodyPrimary.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF111827)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// Payment Mode Toggle — Premium Card Tiles
// ══════════════════════════════════════════════════════════════

@Composable
private fun PaymentModeToggle(
    selectedMode: BankPaymentMode,
    onModeSelected: (BankPaymentMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PaymentModeCard(
            icon = Icons.Default.AccountBalance,
            label = "Bank Account",
            subtitle = "NEFT / IMPS transfer",
            isSelected = selectedMode == BankPaymentMode.BANK,
            selectedColor = Color(0xFF2563EB),
            iconBubbleColor = Color(0xFFEFF6FF),
            iconTint = Color(0xFF2563EB),
            modifier = Modifier.weight(1f),
            onClick = { onModeSelected(BankPaymentMode.BANK) }
        )
        PaymentModeCard(
            icon = Icons.Default.PhoneAndroid,
            label = "UPI",
            subtitle = "Instant UPI payment",
            isSelected = selectedMode == BankPaymentMode.UPI,
            selectedColor = Color(0xFF7C3AED),
            iconBubbleColor = Color(0xFFF5F3FF),
            iconTint = Color(0xFF7C3AED),
            modifier = Modifier.weight(1f),
            onClick = { onModeSelected(BankPaymentMode.UPI) }
        )
    }
}

@Composable
private fun PaymentModeCard(
    icon: ImageVector,
    label: String,
    subtitle: String,
    isSelected: Boolean,
    selectedColor: Color,
    iconBubbleColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isSelected) selectedColor.copy(alpha = 0.04f) else Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) selectedColor else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Icon bubble
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBubbleColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Label
            Text(
                text = label,
                style = AppTypography.TitleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (isSelected) selectedColor else Color(0xFF111827)
            )

            // Subtitle
            Text(
                text = subtitle,
                style = AppTypography.Caption.copy(fontSize = 11.sp),
                color = Color(0xFF6B7280)
            )
        }

        // Selected checkmark badge
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .background(selectedColor, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// Bank Account Details Section
// ══════════════════════════════════════════════════════════════

@Composable
private fun BankDetailsSection(
    state: com.siteflow.signature.cso.onboarding.domain.OnboardingState,
    viewModel: OnboardingViewModel,
    onRequestChequeCapture: () -> Unit
) {
    Column {
        // Account Holder Name
        SignatureTextField(
            label = "Account Holder Name",
            value = state.accountHolderName,
            placeholder = "e.g. RAJESH KUMAR",
            required = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
            onValueChange = { viewModel.onAction(OnboardingAction.AccountHolderChanged(it.uppercase())) }
        )

        Spacer(Modifier.height(20.dp))

        // Bank Name
        SignatureTextField(
            label = "Bank Name",
            value = state.bankName,
            placeholder = "e.g. STATE BANK OF INDIA",
            required = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
            onValueChange = { viewModel.onAction(OnboardingAction.BankNameChanged(it.uppercase())) }
        )

        Spacer(Modifier.height(20.dp))

        // Account Type Dropdown
        AccountTypeDropdown(
            selectedType = state.bankAccountType,
            onTypeSelected = { viewModel.onAction(OnboardingAction.BankAccountTypeSelected(it)) }
        )

        Spacer(Modifier.height(20.dp))

        // Branch
        SignatureTextField(
            label = "Branch",
            value = state.branchName,
            placeholder = "e.g. Vashi Branch",
            required = false,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            onValueChange = { viewModel.onAction(OnboardingAction.BranchNameChanged(it)) }
        )

        Spacer(Modifier.height(20.dp))

        // Account Number
        SignatureTextField(
            label = "Account Number",
            value = state.accountNumber,
            placeholder = "e.g. 1234567890123456",
            required = true,
            characterLimit = 20,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            onValueChange = { viewModel.onAction(OnboardingAction.AccountNumberChanged(it)) }
        )

        Spacer(Modifier.height(20.dp))

        // IFSC Code
        SignatureTextField(
            label = "IFSC Code",
            value = state.ifscCode,
            placeholder = "e.g. SBIN0001234",
            required = true,
            characterLimit = 11,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            onValueChange = { viewModel.onAction(OnboardingAction.IfscCodeChanged(it.uppercase())) }
        )

        Text(
            text = "11-character alphanumeric code (e.g. SBIN0001234)",
            style = AppTypography.Caption.copy(fontSize = 13.sp),
            color = AppColors.TextTertiary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(28.dp))

        // Divider
        HorizontalDivider(color = Color(0xFFE5E7EB))

        Spacer(Modifier.height(24.dp))

        // ── Cancelled Cheque Section ──
        Row {
            Text(
                text = "Cancelled Cheque",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF111827)
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Upload a photo of a cancelled cheque for bank verification",
            style = AppTypography.Caption.copy(fontSize = 13.sp),
            color = AppColors.TextTertiary
        )

        Spacer(Modifier.height(12.dp))

        if (state.cancelledChequePath != null) {
            // ── Cheque preview ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onRequestChequeCapture() }
            ) {
                AsyncImage(
                    model = state.cancelledChequePath,
                    contentDescription = "Cancelled Cheque",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Watermark info overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (state.cancelledChequeCapturedAt != null) {
                        Text("📅 ${state.cancelledChequeCapturedAt}", color = Color.White, fontSize = 10.sp, maxLines = 1)
                    }
                    if (state.cancelledChequeGpsLabel != null) {
                        Text("📍 ${state.cancelledChequeGpsLabel}", color = Color.White, fontSize = 10.sp, maxLines = 1)
                    }
                }

                // Remove button
                IconButton(
                    onClick = { viewModel.onAction(OnboardingAction.CancelledChequeRemoved) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            // ── Empty capture card ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFFD1D5DB),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { onRequestChequeCapture() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = AppColors.BlueGradientStart.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture Cancelled Cheque",
                            tint = AppColors.BlueGradientStart,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Tap to Upload Cheque",
                        style = AppTypography.BodyPrimary.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// UPI Details Section
// ══════════════════════════════════════════════════════════════

@Composable
private fun UpiDetailsSection(
    state: com.siteflow.signature.cso.onboarding.domain.OnboardingState,
    viewModel: OnboardingViewModel
) {
    // Derive validation state — only show error after user has typed something
    val isUpiTyped = state.upiId.isNotBlank()
    val isUpiValid = OnboardingValidator.isValidUpiId(state.upiId)
    val showUpiError = isUpiTyped && !isUpiValid

    Column {
        // UPI Info card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Payments will be transferred directly to your UPI ID. No cheque upload required.",
                style = AppTypography.Caption.copy(fontSize = 12.sp),
                color = Color(0xFF1E40AF)
            )
        }

        Spacer(Modifier.height(20.dp))

        SignatureTextField(
            label = "UPI ID",
            value = state.upiId,
            placeholder = "e.g. shopname@upi",
            required = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onValueChange = { viewModel.onAction(OnboardingAction.UpiIdChanged(it.trim())) }
        )

        Spacer(Modifier.height(6.dp))

        when {
            showUpiError -> {
                Text(
                    text = "Invalid UPI ID. Use format: username@bankhandle (e.g. rajesh@okicici)",
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = AppColors.Danger
                )
            }
            isUpiTyped && isUpiValid -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✓ Valid UPI ID",
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AppColors.Success
                    )
                }
            }
            else -> {
                Text(
                    text = "Format: username@bankhandle  (e.g. rajesh@okicici, shop@ybl)",
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = AppColors.TextTertiary
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// Account Type Dropdown
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountTypeDropdown(
    selectedType: BankAccountType,
    onTypeSelected: (BankAccountType) -> Unit
) {
    val options = BankAccountType.entries
    val displayNames = options.map { it.label }

    Column {
        Row {
            Text(
                text = "Account Type",
                style = AppTypography.TitleMedium.copy(fontSize = 14.sp),
                color = Color(0xFF111827)
            )
            Text(
                text = " *",
                style = AppTypography.TitleMedium.copy(fontSize = 14.sp),
                color = AppColors.Danger
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEach { type ->
                val isSelected = selectedType == type
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) AppColors.Primary else Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = if (isSelected) AppColors.Primary.copy(alpha = 0.05f) else Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTypeSelected(type) }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onTypeSelected(type) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AppColors.Primary,
                            unselectedColor = Color(0xFFD1D5DB)
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = type.label,
                        style = AppTypography.BodyPrimary.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) AppColors.Primary else Color(0xFF374151)
                    )
                }
            }
        }
    }
}

// ── Section Header ──

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = AppTypography.TitleMedium.copy(
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        ),
        color = Color(0xFF111827)
    )
}

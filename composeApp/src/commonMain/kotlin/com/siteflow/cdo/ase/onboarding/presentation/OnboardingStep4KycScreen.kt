package com.siteflow.cdo.ase.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.siteflow.cdo.ase.onboarding.domain.OnboardingAction
import com.siteflow.cdo.ase.onboarding.domain.OnboardingEvent
import com.siteflow.cdo.ase.onboarding.domain.OnboardingValidator
import com.siteflow.cdo.ase.onboarding.domain.OnboardingViewModel
import com.siteflow.cdo.core.domain.ImagePicker
import com.siteflow.cdo.core.presentation.components.CdoButton
import com.siteflow.cdo.core.presentation.components.dismissKeyboardOnTap
import com.siteflow.cdo.core.presentation.components.CdoTextField
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingStep4KycScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    outletId: String? = null,
    viewModel: OnboardingViewModel = koinInject(),
    imagePicker: ImagePicker = koinInject()
) {
    val state by viewModel.state.collectAsState()

    var activePhotoType by remember { mutableStateOf<String?>(null) }
    var showPhotoSourceSheet by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onAction(OnboardingAction.SetCurrentStep(4))
        outletId?.let { viewModel.onAction(OnboardingAction.SetOutletId(it)) }
        viewModel.events.collectLatest { event ->
            when (event) {
                OnboardingEvent.NavigateBack -> onBack()
                OnboardingEvent.NavigateToStep5 -> onContinue()
                else -> {}
            }
        }
    }

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
                    text = "Upload identity and location proof documents.",
                    style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                    color = Color(0xFF92400E)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Section: ID Proof ──
            Row {
                Text(
                    text = "ID Proof",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF111827)
                )
                Text(
                    text = " *",
                    style = AppTypography.TitleMedium.copy(fontSize = 16.sp),
                    color = AppColors.Danger
                )
            }
            Spacer(Modifier.height(16.dp))

            KycDropdownField(
                label = "ID Proof Type",
                value = state.kycIdType,
                options = listOf(
                    "AADHAAR", "DRIVING_LICENSE", "PASSPORT", "VOTER_ID",
                    "RATION_CARD", "BANK_PASSBOOK", "GAS_CONNECTION", "OTHER_GOVT_PROOF"
                ),
                displayNames = mapOf(
                    "AADHAAR" to "Aadhaar Card Copy",
                    "DRIVING_LICENSE" to "Valid Driving License Copy",
                    "PASSPORT" to "Passport Copy",
                    "VOTER_ID" to "Voter Identity Card Copy",
                    "RATION_CARD" to "Ration Card",
                    "BANK_PASSBOOK" to "Bank Passbook",
                    "GAS_CONNECTION" to "Gas Connection Copy",
                    "OTHER_GOVT_PROOF" to "Other Government Approved Proof"
                ),
                onSelected = { viewModel.onAction(OnboardingAction.KycIdTypeSelected(it)) }
            )

            Spacer(Modifier.height(16.dp))

            CdoTextField(
                value = state.kycIdNumber,
                onValueChange = { viewModel.onAction(OnboardingAction.KycIdNumberChanged(it.uppercase())) },
                label = "ID Number",
                placeholder = "ENTER ID NUMBER",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (state.kycIdType == "AADHAAR") KeyboardType.Number else KeyboardType.Text,
                    capitalization = if (state.kycIdType == "AADHAAR") KeyboardCapitalization.None else KeyboardCapitalization.Characters
                )
            )

            Spacer(Modifier.height(20.dp))

            KYCPhotoCard(
                title = "Capture ID Proof",
                imagePath = state.kycIdProofPath,
                capturedAt = state.kycIdProofCapturedAt,
                gpsLabel = state.kycIdProofGpsLabel,
                onCapture = {
                    activePhotoType = "ID_PROOF"
                    showPhotoSourceSheet = true
                },
                onRemove = { viewModel.onAction(OnboardingAction.KycPhotoRemoved("ID_PROOF")) }
            )

            Spacer(Modifier.height(32.dp))

            // ── Section: GST Details (Optional) ──
            Text(
                text = "GST Details (Optional)",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(16.dp))

            CdoTextField(
                value = state.kycGstNumber,
                onValueChange = { viewModel.onAction(OnboardingAction.KycGstNumberChanged(it.uppercase())) },
                label = "GST Number",
                placeholder = "ENTER GSTIN",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                )
            )

            Spacer(Modifier.height(20.dp))

            KYCPhotoCard(
                title = "Capture GST Certificate",
                imagePath = state.kycGstCertificatePath,
                capturedAt = state.kycGstCertificateCapturedAt,
                gpsLabel = state.kycGstCertificateGpsLabel,
                onCapture = {
                    activePhotoType = "GST_CERT"
                    showPhotoSourceSheet = true
                },
                onRemove = { viewModel.onAction(OnboardingAction.KycPhotoRemoved("GST_CERT")) }
            )

            Spacer(Modifier.height(32.dp))

            // ── Section: Location Proof ──
            Row {
                Text(
                    text = "Location Proof",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF111827)
                )
                Text(
                    text = " *",
                    style = AppTypography.TitleMedium.copy(fontSize = 16.sp),
                    color = AppColors.Danger
                )
            }
            Spacer(Modifier.height(16.dp))

            KycDropdownField(
                label = "Location Proof Type",
                value = state.kycLocationType,
                options = listOf(
                    "FSSAI_LICENSE", "GST_CERTIFICATE", "SHOP_ESTABLISHMENT",
                    "TRADE_LICENSE", "ELECTRICITY_BILL", "RENT_AGREEMENT",
                    "GAS_CONNECTION", "GRAM_PANCHAYAT", "INSTITUTION_LETTER",
                    "NOTARIZED_DECLARATION", "GAS_BILL", "MUNICIPALITY_BILL",
                    "TELEPHONE_BILL", "MOBILE_POSTPAID_BILL"
                ),
                displayNames = mapOf(
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
                ),
                onSelected = { viewModel.onAction(OnboardingAction.KycLocationTypeSelected(it)) }
            )

            Spacer(Modifier.height(20.dp))

            KYCPhotoCard(
                title = "Capture Location Proof",
                imagePath = state.kycLocationProofPath,
                capturedAt = state.kycLocationProofCapturedAt,
                gpsLabel = state.kycLocationProofGpsLabel,
                onCapture = {
                    activePhotoType = "LOCATION_PROOF"
                    showPhotoSourceSheet = true
                },
                onRemove = { viewModel.onAction(OnboardingAction.KycPhotoRemoved("LOCATION_PROOF")) }
            )

            Spacer(Modifier.height(32.dp))
        }

        // ── Sticky Bottom Button ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp)
                .background(Color.White)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            CdoButton(
                text = "Continue",
                onClick = { viewModel.onAction(OnboardingAction.ContinueToNextStep) },
                enabled = !state.isLoading && OnboardingValidator.isStep4Valid(state),
                loading = state.isLoading
            )
        }
    }

    // Capture Dialogs & Sheets
    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = { Text("Camera Permission Required") },
            text = { Text("This app needs camera access to capture KYC documents.") },
            confirmButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "Select Photo Source",
                    style = AppTypography.TitleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoSourceSheet = false
                            imagePicker.openCamera(
                                onImagePicked = { path ->
                                    activePhotoType?.let { type ->
                                        viewModel.onAction(OnboardingAction.KycPhotoCaptured(type, path))
                                    }
                                },
                                onPermissionDenied = {
                                    showCameraPermissionDialog = true
                                }
                            )
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AppColors.Primary)
                    Spacer(Modifier.width(16.dp))
                    Text("Camera", style = AppTypography.BodyPrimary)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoSourceSheet = false
                            imagePicker.openGallery { path ->
                                activePhotoType?.let { type ->
                                    viewModel.onAction(OnboardingAction.KycPhotoCaptured(type, path))
                                }
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = AppColors.Primary)
                    Spacer(Modifier.width(16.dp))
                    Text("Gallery", style = AppTypography.BodyPrimary)
                }
            }
        }
    }

    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = { Text("Camera Access Needed") },
            text = { Text("Please allow camera access to capture KYC documents.") },
            confirmButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KycDropdownField(
    label: String,
    value: String,
    options: List<String>,
    displayNames: Map<String, String> = emptyMap(),
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxWidth()) {
        CdoTextField(
            value = displayNames[value] ?: value,
            onValueChange = {},
            label = label,
            placeholder = "Select Type",
            readOnly = true,
            trailingIconComposable = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )

        // Transparent overlay to capture taps (readOnly TextField absorbs touch on iOS)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = !expanded }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                scope.launch {
                    delay(50)
                    expanded = false
                }
            },
            modifier = Modifier
                .background(Color.White)
                .heightIn(max = 350.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(displayNames[option] ?: option) },
                    onClick = {
                        onSelected(option)
                        scope.launch {
                            delay(50)
                            expanded = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun KYCPhotoCard(
    title: String,
    imagePath: String?,
    capturedAt: String? = null,
    gpsLabel: String? = null,
    onCapture: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = if (imagePath != null) AppColors.Primary.copy(0.3f) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { if (imagePath == null) onCapture() }
    ) {
        if (imagePath != null) {
            AsyncImage(
                model = imagePath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient + date/GPS overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (capturedAt != null) {
                    Text("📅 $capturedAt", color = Color.White, fontSize = 10.sp, maxLines = 1)
                }
                if (gpsLabel != null) {
                    Text("📍 $gpsLabel", color = Color.White, fontSize = 10.sp, maxLines = 1)
                }
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.Red,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = title,
                    style = AppTypography.BodySecondary,
                    color = Color.Gray
                )
            }
        }
    }
}

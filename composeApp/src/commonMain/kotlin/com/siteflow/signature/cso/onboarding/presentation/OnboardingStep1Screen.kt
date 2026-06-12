package com.siteflow.signature.cso.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.cso.onboarding.data.Classification
import com.siteflow.signature.cso.onboarding.data.GpsLocation
import com.siteflow.signature.cso.onboarding.data.PhotoSlot
import com.siteflow.signature.cso.onboarding.domain.OnboardingAction
import com.siteflow.signature.cso.onboarding.domain.OnboardingEvent
import com.siteflow.signature.cso.onboarding.domain.OnboardingViewModel
import com.siteflow.signature.cso.onboarding.domain.OnboardingValidator
import com.siteflow.signature.core.presentation.components.SignatureButton
import com.siteflow.signature.core.presentation.components.dismissKeyboardOnTap
import com.siteflow.signature.core.presentation.components.SignatureDropdown
import com.siteflow.signature.core.presentation.components.SignatureTextField
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun OnboardingStep1Screen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    viewModel: OnboardingViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                OnboardingEvent.NavigateBack -> onBack()
                OnboardingEvent.NavigateToStep2 -> onContinue()
                else -> {} // Other events managed by downstream screens
            }
        }
    }

    // 🔑 Reset form + auto-capture GPS when screen is fully settled
    LaunchedEffect(Unit) {
        viewModel.onAction(OnboardingAction.ResetState)
        viewModel.onAction(OnboardingAction.SetCurrentStep(1))
        kotlinx.coroutines.delay(600)
        viewModel.onAction(OnboardingAction.RequestLocation)
    }

    // Auto-dismiss keyboard & scroll when pincode reaches 6 digits
    LaunchedEffect(state.pincode) {
        if (state.pincode.length == 6) {
            keyboardController?.hide()
        }
    }

    // Auto-scroll down when city/state are populated (success)
    LaunchedEffect(state.city, state.state) {
        if (state.city.isNotBlank() && state.state.isNotBlank()) {
            kotlinx.coroutines.delay(200) // let animation start first
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .dismissKeyboardOnTap()
    ) {

        // ── Form Content (scrollable) ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Info Banner
            InfoBanner("Provide outlet details below. GPS coordinates are captured automatically.")

            Spacer(Modifier.height(24.dp))

            // Outlet Name
            SignatureTextField(
                label = "Outlet Name",
                value = state.outletName,
                placeholder = "e.g., Krishna General Store",
                required = true,
                characterLimit = 50,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                onValueChange = { viewModel.onAction(OnboardingAction.OutletNameChanged(it)) }
            )

            Spacer(Modifier.height(20.dp))

            // Owner Name
            SignatureTextField(
                label = "Owner Name",
                value = state.ownerName,
                placeholder = "Full name of the owner",
                required = true,
                characterLimit = 50,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                onValueChange = { viewModel.onAction(OnboardingAction.OwnerNameChanged(it)) }
            )

            Spacer(Modifier.height(20.dp))

            // Contact Number
            SignatureTextField(
                label = "Contact Number",
                value = state.contactNumber,
                placeholder = "98765 43210",
                required = true,
                prefix = "+91",
                keyboardType = KeyboardType.Phone,
                characterLimit = 10,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                onValueChange = { viewModel.onAction(OnboardingAction.ContactNumberChanged(it)) }
            )

            Spacer(Modifier.height(20.dp))

            // WhatsApp Number
            Column {
                // WhatsApp header with label + same-as-contact toggle on same row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "WhatsApp Number",
                        style = AppTypography.TitleMedium.copy(fontSize = 14.sp),
                        color = Color(0xFF111827)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            viewModel.onAction(OnboardingAction.SameAsContactToggled(!state.sameAsContact))
                        }
                    ) {
                        Checkbox(
                            checked = state.sameAsContact,
                            onCheckedChange = { viewModel.onAction(OnboardingAction.SameAsContactToggled(it)) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppColors.BlueGradientStart,
                                uncheckedColor = Color(0xFFD1D5DB)
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Same as contact",
                            style = AppTypography.BodyPrimary.copy(fontSize = 12.sp),
                            color = AppColors.TextTertiary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                SignatureTextField(
                    value = state.whatsAppNumber,
                    placeholder = "98765 43210",
                    enabled = !state.sameAsContact,
                    prefix = "+91",
                    keyboardType = KeyboardType.Phone,
                    characterLimit = 10,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    onValueChange = { viewModel.onAction(OnboardingAction.WhatsAppNumberChanged(it)) }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Email (optional)
            SignatureTextField(
                label = "Email",
                value = state.email,
                placeholder = "owner@email.com",
                characterLimit = 100,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                onValueChange = { viewModel.onAction(OnboardingAction.EmailChanged(it)) }
            )

            Spacer(Modifier.height(20.dp))

            // Outlet Type
            SignatureDropdown(
                label = "Outlet Type",
                selected = state.outletType,
                options = state.outletTypes,
                required = true,
                placeholder = "Select outlet type",
                onSelected = { viewModel.onAction(OnboardingAction.OutletTypeSelected(it)) }
            )

            Spacer(Modifier.height(20.dp))

            // Address
            SignatureTextField(
                label = "Address",
                value = state.address,
                placeholder = "House / Street / Area",
                required = true,
                minLines = 2,
                characterLimit = 200,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                onValueChange = { viewModel.onAction(OnboardingAction.AddressChanged(it)) }
            )

            Spacer(Modifier.height(20.dp))

            // Landmark
            SignatureTextField(
                label = "Landmark",
                value = state.landmark,
                placeholder = "Near Vashi Railway Station",
                characterLimit = 100,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                onValueChange = { viewModel.onAction(OnboardingAction.LandmarkChanged(it)) }
            )

            Spacer(Modifier.height(20.dp))

            // Locality
            SignatureTextField(
                label = "Locality",
                value = state.locality,
                placeholder = "e.g., Vashi",
                characterLimit = 100,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                onValueChange = { viewModel.onAction(OnboardingAction.LocalityChanged(it)) }
            )

            Spacer(Modifier.height(20.dp))

            // Pincode with inline loading/success feedback
            val pincodeSuccess = state.city.isNotBlank() && state.state.isNotBlank() && !state.isPincodeLoading

            SignatureTextField(
                label = "Pincode",
                value = state.pincode,
                placeholder = "6-digit PIN",
                required = true,
                characterLimit = 6,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                trailingIconComposable = {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            state.isPincodeLoading -> CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = AppColors.BlueGradientStart,
                                strokeWidth = 2.dp
                            )
                            pincodeSuccess -> Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Pincode verified",
                                tint = AppColors.Success,
                                modifier = Modifier.size(22.dp)
                            )
                            else -> IconButton(
                                onClick = { viewModel.onAction(OnboardingAction.LookupPincode) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search pincode",
                                    tint = if (state.pincode.length == 6) AppColors.BlueGradientStart else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                onValueChange = { viewModel.onAction(OnboardingAction.PincodeChanged(it)) }
            )

            // Pincode loading status (subtle text below)
            androidx.compose.animation.AnimatedVisibility(
                visible = state.isPincodeLoading,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(200)
                ) + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(150)
                ) + androidx.compose.animation.shrinkVertically()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = AppColors.BlueGradientStart,
                        strokeWidth = 1.5.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Looking up pincode...",
                        style = AppTypography.Caption.copy(fontSize = 12.sp),
                        color = AppColors.TextTertiary
                    )
                }
            }

            // Pincode error
            androidx.compose.animation.AnimatedVisibility(
                visible = state.pincodeError != null,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Text(
                    text = state.pincodeError ?: "",
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = AppColors.Danger,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // City (auto-filled, read-only)
            SignatureTextField(
                label = "City",
                value = state.city,
                placeholder = if (state.isPincodeLoading) "Loading..." else "Auto-filled from pincode",
                required = true,
                enabled = false,
                onValueChange = { /* read-only */ }
            )

            Spacer(Modifier.height(20.dp))

            // State (auto-filled, read-only)
            SignatureTextField(
                label = "State",
                value = state.state,
                placeholder = if (state.isPincodeLoading) "Loading..." else "Auto-filled from pincode",
                enabled = false,
                onValueChange = { /* read-only */ }
            )

            Spacer(Modifier.height(20.dp))

            // GPS Location (auto-captured)
            GpsLocationSection(
                location = state.gpsLocation,
                isCapturing = state.isCapturingGps,
                error = state.error,
                onRetry = { viewModel.onAction(OnboardingAction.RetryLocation) }
            )

            // Error
            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.error ?: "",
                    style = AppTypography.Caption,
                    color = AppColors.Danger,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
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
                enabled = OnboardingValidator.isStep1Valid(state) && !state.isLoading,
                loading = state.isLoading
            )
        }
    }
}

// ── Re-usable Components ──


@Composable
private fun InfoBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFEF3C7), // amber-100
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
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



@Composable
private fun GpsLocationSection(
    location: GpsLocation?,
    isCapturing: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Column {
        Row {
            Text(
                text = "GPS Location",
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

        // State: Capturing
        if (isCapturing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = AppColors.BlueGradientStart,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Capturing location...",
                    style = AppTypography.BodyPrimary.copy(
                        fontSize = 14.sp,
                        color = AppColors.TextTertiary
                    )
                )
            }
        }

        // State: Error → show retry button
        if (!isCapturing && location == null && error != null) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = Brush.horizontalGradient(
                        listOf(AppColors.Danger, AppColors.Danger)
                    )
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.Danger
                )
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Retry GPS Capture",
                    style = AppTypography.BodyPrimary.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                )
            }
        }

        // State: Success
        if (location != null) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = AppColors.Success.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.Success,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Location Captured",
                        style = AppTypography.BodyPrimary.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = AppColors.Success
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${location.latitude}° N,  ${location.longitude}° E",
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = AppColors.BlueGradientStart
                    )
                    if (location.areaName.isNotEmpty()) {
                        Text(
                            text = location.areaName,
                            style = AppTypography.Caption.copy(fontSize = 12.sp),
                            color = AppColors.TextTertiary
                        )
                    }
                }
            }
        }
    }
}

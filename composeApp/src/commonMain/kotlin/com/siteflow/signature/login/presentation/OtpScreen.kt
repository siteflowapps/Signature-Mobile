package com.siteflow.signature.login.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import coil3.compose.AsyncImage
import signature.composeapp.generated.resources.Res
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.core.presentation.components.SignatureButton
import com.siteflow.signature.core.presentation.components.dismissKeyboardOnTap
import com.siteflow.signature.core.domain.UserRole
import com.siteflow.signature.login.domain.OtpAction
import com.siteflow.signature.login.domain.OtpEvent
import com.siteflow.signature.login.domain.OtpViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun OtpScreen(
    mobileNumber: String,
    onVerified: (UserRole) -> Unit,
    onEditNumber: () -> Unit,
    onNavigateToWalkthrough: () -> Unit = {},
    viewModel: OtpViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Initialize mobile number
    LaunchedEffect(mobileNumber) {
        viewModel.setMobileNumber(mobileNumber)
    }

    // 🎨 Animated Gradient Background
    val infiniteTransition = rememberInfiniteTransition()
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Focus requesters for OTP boxes
    val focusRequesters = remember { List(6) { FocusRequester() } }

    // 🎯 Auto-focus first box on launch
    LaunchedEffect(Unit) {
        delay(500)
        focusRequesters[0].requestFocus()
    }

    // 📳 Haptic on all digits filled
    LaunchedEffect(state.isValid) {
        if (state.isValid) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Navigation events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OtpEvent.NavigateToDashboard -> onVerified(event.role)
                OtpEvent.NavigateBackToLogin -> onEditNumber()
                OtpEvent.NavigateToWalkthrough -> onNavigateToWalkthrough()
            }
        }
    }

    val scrollState = rememberScrollState()

    // Auto-scroll to bottom when keyboard opens so CTA is visible
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(imeBottom) {
        if (imeBottom > 0) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dismissKeyboardOnTap()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.BackgroundGradientStart,
                        AppColors.BackgroundGradientEnd
                    ),
                    startY = -animatedOffset,
                    endY = 1000f + animatedOffset
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Spacer(Modifier.height(8.dp))

            // 🛡️ Shield Icon
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                var svgBytes by remember { mutableStateOf<ByteArray?>(null) }
                LaunchedEffect(Unit) {
                    svgBytes = signature.composeapp.generated.resources.Res.readBytes("drawable/ic_shield.svg")
                }
                svgBytes?.let { bytes ->
                    AsyncImage(
                        model = bytes,
                        contentDescription = "Signature Shield",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // 📝 Header
            Text(
                text = "Verify OTP",
                style = AppTypography.TitleLarge.copy(fontSize = 28.sp),
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "We've sent a 6-digit code to",
                style = AppTypography.BodyPrimary,
                color = AppColors.TextTertiary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = state.maskedNumber,
                style = AppTypography.TitleMedium.copy(fontSize = 18.sp),
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(12.dp))

            // ✏️ Edit Number Link
            Row(
                modifier = Modifier
                    .clickable { viewModel.onAction(OtpAction.EditNumber) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = AppColors.BlueGradientStart,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Edit Number",
                    style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.BlueGradientStart
                )
            }

            Spacer(Modifier.height(40.dp))

            // 🔢 OTP Boxes
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (i in 0 until 6) {
                    OtpBox(
                        value = state.otp[i],
                        isFocused = i == state.otp.indexOfFirst { it.isEmpty() }.let {
                            if (it == -1) 5 else it
                        },
                        focusRequester = focusRequesters[i],
                        onValueChange = { newValue ->
                            if (newValue.isEmpty()) {
                                viewModel.onAction(OtpAction.BackspacePressed(i))
                                if (i > 0) {
                                    focusRequesters[i - 1].requestFocus()
                                }
                            } else {
                                viewModel.onAction(OtpAction.DigitEntered(i, newValue))
                                val nextIndex = if (newValue.length > 1) {
                                    minOf(i + newValue.length, 5)
                                } else {
                                    minOf(i + 1, 5)
                                }
                                if (nextIndex < 6) {
                                    focusRequesters[nextIndex].requestFocus()
                                }
                            }
                        },
                        onBackspace = {
                            if (i > 0) {
                                viewModel.onAction(OtpAction.BackspacePressed(i - 1))
                                focusRequesters[i - 1].requestFocus()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Error Card
            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AppColors.Danger,
                            modifier = Modifier.size(18.dp).padding(top = 1.dp)
                        )
                        Text(
                            text = state.error ?: "",
                            style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                            color = Color(0xFF991B1B),
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ⏱️ Resend Section
            Text(
                text = "Didn't receive the code?",
                style = AppTypography.BodyPrimary,
                color = AppColors.TextTertiary
            )

            Spacer(Modifier.height(8.dp))

            if (state.canResend) {
                Text(
                    text = "Resend Code",
                    style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.BlueGradientStart,
                    modifier = Modifier
                        .clickable { viewModel.onAction(OtpAction.ResendOtp) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resend in ",
                        style = AppTypography.BodyPrimary,
                        color = AppColors.TextTertiary
                    )
                    Text(
                        text = "00:${state.resendCountdown.toString().padStart(2, '0')}",
                        style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Bold),
                        color = AppColors.BlueGradientStart
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // 🚀 Verify Button
            SignatureButton(
                text = "Verify & Login →",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onAction(OtpAction.Submit)
                },
                enabled = state.isValid,
                loading = state.isLoading,
                trailingIcon = null,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OtpBox(
    value: String,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        value.isNotEmpty() -> AppColors.BlueGradientStart
        isFocused -> AppColors.BlueGradientStart
        else -> Color(0xFFD1D5DB)
    }
    val backgroundColor = if (value.isNotEmpty()) {
        AppColors.BlueGradientStart.copy(alpha = 0.04f)
    } else {
        Color(0xFFF9FAFB)
    }

    // Sentinel: a zero-width space keeps the field non-empty so the keyboard
    // always fires onValueChange when backspace is pressed.
    val sentinel = "\u200B"

    // Build a TextFieldValue with the cursor always at the END so that
    // pressing backspace will delete the sentinel (or the digit).
    val textContent = if (value.isEmpty()) sentinel else value
    val tfv = remember(value) {
        androidx.compose.ui.text.input.TextFieldValue(
            text = textContent,
            selection = androidx.compose.ui.text.TextRange(textContent.length)
        )
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = if (isFocused || value.isNotEmpty()) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = tfv,
            onValueChange = { newTfv ->
                val newText = newTfv.text
                // Strip sentinel characters so we only deal with actual digits
                val cleaned = newText.replace(sentinel, "")

                if (cleaned.isEmpty() && newText.length < textContent.length) {
                    // A deletion happened
                    if (value.isEmpty()) {
                        // Was already logically empty → move focus back
                        onBackspace()
                    } else {
                        // Had a digit, now cleared
                        onValueChange("")
                    }
                } else {
                    val digits = cleaned.filter { it.isDigit() }
                    if (digits.isNotEmpty()) {
                        onValueChange(digits.last().toString())
                    }
                    // If no digits and no deletion, do nothing (ignore non-digit input)
                }
            },
            textStyle = AppTypography.TitleLarge.copy(
                fontSize = 24.sp,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                // Belt-and-suspenders: onKeyEvent catches backspace on devices
                // where the sentinel trick doesn't work (e.g. some Samsung IMEs).
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown &&
                        event.key == Key.Backspace &&
                        value.isEmpty()
                    ) {
                        onBackspace()
                        true
                    } else false
                },
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "•",
                            style = AppTypography.TitleLarge.copy(
                                fontSize = 24.sp,
                                color = Color(0xFFD1D5DB)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

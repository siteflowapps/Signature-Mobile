package com.siteflow.retailsync.login.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.retailsync.core.presentation.components.CdoButton
import com.siteflow.retailsync.core.presentation.components.dismissKeyboardOnTap
import com.siteflow.retailsync.core.presentation.design.AppColors
import com.siteflow.retailsync.core.presentation.design.AppTypography
import com.siteflow.retailsync.login.domain.OtpAction
import com.siteflow.retailsync.login.domain.OtpEvent
import com.siteflow.retailsync.login.domain.OtpViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun OtpScreen(
    mobileNumber: String,
    onNavigateToDashboard: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: OtpViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val focusRequesters = remember { List(6) { FocusRequester() } }

    LaunchedEffect(mobileNumber) {
        viewModel.setMobileNumber(mobileNumber)
        delay(500)
        focusRequesters[0].requestFocus()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                OtpEvent.NavigateToDashboard -> onNavigateToDashboard()
                OtpEvent.NavigateBackToLogin -> onNavigateBack()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .dismissKeyboardOnTap()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.BackgroundGradientStart,
                        AppColors.BackgroundGradientEnd
                    )
                )
            )
            .statusBarsPadding()
            .imePadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.onAction(OtpAction.EditNumber) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF111827)
                )
            }
            Text(
                text = "Verify OTP",
                style = AppTypography.TitleMedium.copy(fontSize = 18.sp),
                color = Color(0xFF111827)
            )
        }

        // Content
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Verification Code",
                    style = AppTypography.TitleSemiLarge,
                    color = Color(0xFF111827)
                )

                Spacer(Modifier.height(12.dp))

                Row {
                    Text(
                        text = "Sent to ",
                        style = AppTypography.BodyPrimary,
                        color = AppColors.TextTertiary
                    )
                    Text(
                        text = state.maskedNumber,
                        style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.BlueGradientStart
                    )
                }

                Spacer(Modifier.height(36.dp))

                // OTP Input Fields
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 6) {
                        OtpDigitField(
                            value = state.otp[i],
                            focusRequester = focusRequesters[i],
                            onValueChange = { newValue ->
                                if (newValue.isEmpty()) {
                                    viewModel.onAction(OtpAction.BackspacePressed(i))
                                    if (i > 0) focusRequesters[i - 1].requestFocus()
                                } else {
                                    viewModel.onAction(OtpAction.DigitEntered(i, newValue))
                                    if (newValue.length == 1 && i < 5) {
                                        focusRequesters[i + 1].requestFocus()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            hasError = state.error != null
                        )
                    }
                }

                if (state.error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = state.error ?: "",
                        style = AppTypography.Caption.copy(fontSize = 13.sp),
                        color = AppColors.Danger,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Resend
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.canResend) {
                        Text(
                            text = "Resend OTP",
                            style = AppTypography.BodyPrimary.copy(
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            ),
                            color = AppColors.BlueGradientStart,
                            modifier = Modifier.clickable {
                                viewModel.onAction(OtpAction.ResendOtp)
                            }
                        )
                    } else {
                        Text(
                            text = "Resend in ${state.resendCountdown}s",
                            style = AppTypography.BodySecondary,
                            color = AppColors.TextTertiary
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                CdoButton(
                    text = "Verify & Continue",
                    onClick = { viewModel.onAction(OtpAction.Submit) },
                    enabled = state.isValid,
                    loading = state.isLoading,
                    trailingIcon = null
                )
            }
        }
    }
}

@Composable
private fun OtpDigitField(
    value: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hasError: Boolean = false
) {
    val filled = value.isNotEmpty()
    val borderColor = when {
        hasError -> AppColors.Danger
        filled -> AppColors.BlueGradientStart
        else -> Color(0xFFE5E7EB)
    }
    val bgColor = if (filled) AppColors.lightBlueFF else Color.White

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                val digitsOnly = newValue.filter { it.isDigit() }
                onValueChange(digitsOnly.take(1))
            },
            textStyle = AppTypography.TitleLarge.copy(
                fontSize = 20.sp,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                autoCorrectEnabled = false
            ),
            singleLine = true,
            modifier = Modifier
                .wrapContentWidth()
                .focusRequester(focusRequester)
        )
    }
}

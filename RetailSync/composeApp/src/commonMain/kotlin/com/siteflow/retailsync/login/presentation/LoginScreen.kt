package com.siteflow.retailsync.login.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.retailsync.core.presentation.design.AppColors
import com.siteflow.retailsync.core.presentation.design.AppTypography
import com.siteflow.retailsync.core.presentation.components.CdoButton
import com.siteflow.retailsync.core.presentation.components.dismissKeyboardOnTap
import com.siteflow.retailsync.login.domain.LoginAction
import com.siteflow.retailsync.login.domain.LoginEvent
import com.siteflow.retailsync.login.domain.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onNavigateToOtp: (mobileNumber: String) -> Unit,
    viewModel: LoginViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition()
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        delay(500)
        focusRequester.requestFocus()
    }

    LaunchedEffect(state.isValid) {
        if (state.isValid) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is LoginEvent.NavigateToOtp -> onNavigateToOtp(event.mobileNumber)
            }
        }
    }

    val scrollState = rememberScrollState()
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
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(60.dp))

            // Logo placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(AppColors.BlueGradientStart, AppColors.BlueGradientEnd)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RS",
                    style = AppTypography.TitleLarge.copy(fontSize = 32.sp),
                    color = Color.White
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "RetailFirst",
                style = AppTypography.TitleLarge.copy(fontSize = 32.sp),
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(AppColors.PillBackground, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "QR INVOICE PLATFORM",
                    style = AppTypography.Caption.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = AppColors.TextTertiary
                )
            }

            Spacer(Modifier.height(30.dp))

            // Login Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Welcome Back",
                        style = AppTypography.TitleSemiLarge.copy(fontSize = 28.sp),
                        color = Color(0xFF1D1B20)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Enter your registered phone number to continue.",
                        style = AppTypography.BodyPrimary,
                        color = AppColors.TextTertiary,
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(40.dp))

                    // Mobile Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+91",
                            style = AppTypography.TitleMedium.copy(fontSize = 20.sp),
                            color = Color(0xFF1D1B20),
                            modifier = Modifier.padding(end = 12.dp)
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            if (state.mobileNumber.isEmpty()) {
                                Text(
                                    text = "Enter 10 Digits",
                                    style = AppTypography.TitleMedium.copy(fontSize = 18.sp),
                                    color = Color(0xFFADAEBC)
                                )
                            }
                            BasicTextField(
                                value = state.mobileNumber,
                                onValueChange = {
                                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                        viewModel.onAction(LoginAction.MobileNumberChanged(it))
                                    }
                                },
                                textStyle = AppTypography.TitleMedium.copy(fontSize = 20.sp, color = Color(0xFF1D1B20)),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    autoCorrectEnabled = false
                                ),
                                visualTransformation = MobileNumberTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                            )
                        }

                        val checkmarkScale by animateFloatAsState(
                            targetValue = if (state.isValid) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )

                        if (checkmarkScale > 0.01f) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AppColors.Success,
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(checkmarkScale)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))

                    Text(
                        text = "${state.mobileNumber.length}/10",
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = TextAlign.End
                    )

                    Spacer(Modifier.height(10.dp))

                    CdoButton(
                        text = "Get OTP",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.onAction(LoginAction.Submit)
                        },
                        enabled = state.isValid,
                        loading = state.isLoading,
                        trailingIcon = null
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

class MobileNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        var out = ""
        for (i in text.indices) {
            out += text[i]
            if (i == 4) out += " "
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 10) return offset + 1
                return 11
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 5) return offset
                if (offset <= 11) return offset - 1
                return 10
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

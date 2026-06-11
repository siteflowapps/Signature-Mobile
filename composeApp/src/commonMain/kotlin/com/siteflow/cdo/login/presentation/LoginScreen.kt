package com.siteflow.cdo.login.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import cdo.composeapp.generated.resources.Res
import cdo.composeapp.generated.resources.campa_logo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import com.siteflow.cdo.core.data.config.AppEnvironment
import com.siteflow.cdo.core.data.config.EnvironmentManager
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import com.siteflow.cdo.core.presentation.components.CdoButton
import com.siteflow.cdo.core.presentation.components.dismissKeyboardOnTap
import com.siteflow.cdo.login.domain.LoginAction
import com.siteflow.cdo.login.domain.LoginEvent
import com.siteflow.cdo.login.domain.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import org.koin.compose.getKoin

// ── Tap count threshold & reset window ──
private const val ENV_TAP_COUNT = 5
private const val ENV_TAP_RESET_MS = 3000L

@Composable
fun LoginScreen(
    onNavigateToOtp: (mobileNumber: String) -> Unit,
    viewModel: LoginViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current

    // 🌍 Environment state
    val koin = getKoin()
    val tokenStorage = remember { koin.get<com.siteflow.cdo.core.data.auth.TokenStorage>() }
    val currentEnv by EnvironmentManager.currentEnv.collectAsState()
    var showEnvDialog by remember { mutableStateOf(false) }
    var tapCount by remember { mutableStateOf(0) }

    // Reset tap count after inactivity window
    LaunchedEffect(tapCount) {
        if (tapCount in 1 until ENV_TAP_COUNT) {
            delay(ENV_TAP_RESET_MS)
            tapCount = 0
        }
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

    // 🎯 Auto-focus keyboard on launch
    LaunchedEffect(Unit) {
        delay(500)
        focusRequester.requestFocus()
    }

    // 📳 Haptic on success
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

    // Auto-scroll to bottom when keyboard opens so CTA is visible
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(imeBottom) {
        if (imeBottom > 0) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // 🌍 Env Selector Dialog
    if (showEnvDialog) {
        EnvSelectorDialog(
            currentEnv = currentEnv,
            onSelect = { selected ->
                EnvironmentManager.switch(selected) { key ->
                    tokenStorage.saveEnvironment(key)
                }
                showEnvDialog = false
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onDismiss = { showEnvDialog = false }
        )
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

            Spacer(Modifier.height(40.dp))

            // Logo Section — 5-tap to open env switcher (hidden feature)
            Image(
                painter = painterResource(Res.drawable.campa_logo),
                contentDescription = "Campa Destination Outlet",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(200.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        tapCount++
                        if (tapCount >= ENV_TAP_COUNT) {
                            tapCount = 0
                            showEnvDialog = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
            )

            Spacer(Modifier.height(4.dp))

            // 🔴 Subtle env badge — only visible when NOT on PROD
            // Normal users won't notice; devs/testers know to look here
            if (currentEnv != AppEnvironment.PROD) {
                Box(
                    modifier = Modifier
                        .background(
                            color = when (currentEnv) {
                                AppEnvironment.QA -> Color(0xFFDC2626)
                                AppEnvironment.UAT -> Color(0xFFD97706)
                                else -> Color.Transparent
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentEnv.badgeLabel,
                        style = AppTypography.Caption.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(6.dp))
            } else {
                Spacer(Modifier.height(10.dp))
            }

            Text(
                text = "CDO",
                style = AppTypography.TitleLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                ),
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Campa Destination Outlet",
                style = AppTypography.BodyPrimary.copy(
                    fontSize = 13.sp,
                    letterSpacing = 0.3.sp
                ),
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(20.dp))

            // 💳 Login Card
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

                    // 📱 Mobile Input
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

                        // ✨ Success Animation
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

                    // 🔢 Character Counter
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

                    // 🚀 Secure Access Button
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

            Spacer(Modifier.height(36.dp))
        }

        // Colophon — fixed at bottom, always visible, outside scroll
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 44.dp)
                .navigationBarsPadding()
                .padding(bottom = 22.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.5.dp)
                    .background(Color(0xFF9CA3AF))
            )
            Text(
                text = "A SITEFLOW PRODUCT",
                fontSize = 8.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 3.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(0.5.dp)
                    .background(Color(0xFF9CA3AF))
            )
        }
    }
}

// ── Hidden Environment Selector Dialog ──

@Composable
private fun EnvSelectorDialog(
    currentEnv: AppEnvironment,
    onSelect: (AppEnvironment) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF1E293B),
        title = {
            Column {
                Text(
                    text = "🔧 Developer Settings",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = "Select environment",
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = Color(0xFF94A3B8)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppEnvironment.entries.forEach { env ->
                    val isSelected = env == currentEnv
                    val (bgColor, accentColor, dotColor) = when (env) {
                        AppEnvironment.QA -> Triple(
                            Color(0xFF7F1D1D).copy(alpha = if (isSelected) 1f else 0.3f),
                            Color(0xFFFCA5A5),
                            Color(0xFFEF4444)
                        )
                        AppEnvironment.UAT -> Triple(
                            Color(0xFF78350F).copy(alpha = if (isSelected) 1f else 0.3f),
                            Color(0xFFFCD34D),
                            Color(0xFFF59E0B)
                        )
                        AppEnvironment.PROD -> Triple(
                            Color(0xFF14532D).copy(alpha = if (isSelected) 1f else 0.3f),
                            Color(0xFF86EFAC),
                            Color(0xFF22C55E)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(env) },
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Live dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(dotColor, androidx.compose.foundation.shape.CircleShape)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = env.displayName,
                                    style = AppTypography.BodyPrimary.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    ),
                                    color = accentColor
                                )
                                Text(
                                    text = env.baseUrl.removePrefix("https://").removeSuffix("/api/v1"),
                                    style = AppTypography.Caption.copy(fontSize = 10.sp),
                                    color = accentColor.copy(alpha = 0.7f)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    style = AppTypography.Button.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF94A3B8)
                )
            }
        }
    )
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




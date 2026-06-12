package com.siteflow.signature.outlet.walkthrough.presentation


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughAction
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughEvent
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun WalkthroughWelcomeScreen(
    onNavigateToOutletDetails: () -> Unit,
    viewModel: WalkthroughViewModel = koinInject()
) {


    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                WalkthroughEvent.NavigateToOutletDetails -> onNavigateToOutletDetails()
                else -> Unit
            }
        }
    }

    // Entrance animation flag
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Pulsing ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.BackgroundGradientStart,
                        AppColors.BackgroundGradientEnd
                    )
                )
            )
    ) {
        // Decorative background circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AppColors.BlueGradientStart.copy(alpha = 0.08f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 50.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AppColors.WalkthroughAccentPurple.copy(alpha = 0.06f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Step indicator
                    WalkthroughStepIndicator(currentStep = 1, totalSteps = 4)

                    Spacer(Modifier.height(16.dp))

                    // Congratulations badge
                    Box(
                        modifier = Modifier
                            .background(
                                AppColors.WalkthroughAccentPurple.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(50)
                            )
                            .border(1.dp, AppColors.WalkthroughAccentPurple.copy(alpha = 0.3f), RoundedCornerShape(50))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Congratulations!",
                            color = AppColors.WalkthroughAccentPurple,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Welcome Onboard",
                        color = AppColors.black27,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Signature Partner",
                        color = AppColors.WalkthroughTextBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Middle section — logo + ring
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 200))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Glowing ring with Signature logo placeholder
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .scale(ringScale)
                            .size(160.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AppColors.BlueGradientStart.copy(alpha = ringAlpha * 0.15f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 3.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        AppColors.BlueGradientStart,
                                        AppColors.WalkthroughAccentPurple,
                                        AppColors.BlueGradientStart
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        // Signature logo placeholder — replace with Image(painterResource(...)) when asset is ready
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AppColors.BlueGradientStart, AppColors.BlueGradientEnd)
                                    )
                                )
                        ) {
                            Text(
                                text = "Signature",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "You are now an official Partner for\nCampa Destination Outlet – PFP Program",
                        color = AppColors.WalkthroughTextSubtle,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Bottom section — gift card + button
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 400)) + slideInVertically(tween(600, delayMillis = 400)) { 40 }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
//                    // Gift reveal card
//                    GiftRevealCard(
//                        revealed = state.giftRevealed,
//                        onTap = { viewModel.onAction(WalkthroughAction.RevealGift) }
//                    )
//
//                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.onAction(WalkthroughAction.ContinueFromWelcome) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.BlueGradientStart
                        )
                    ) {
                        Text(
                            text = "Continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GiftRevealCard(
    revealed: Boolean,
    onTap: () -> Unit
) {
    val scale = remember { Animatable(1f) }

    // Bounce when tapped
    LaunchedEffect(revealed) {
        if (revealed) {
            scale.animateTo(1.08f, tween(120))
            scale.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
        }
    }

    // Shimmer on the card
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by shimmerTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "shimmer_offset"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (revealed) {
                    Brush.linearGradient(
                        colors = listOf(AppColors.WalkthroughSurfaceCard, AppColors.WalkthroughSurfaceCardAlt)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(AppColors.BlueGradientStart, AppColors.WalkthroughAccentPurple, AppColors.BlueGradientEnd),
                        start = Offset(shimmerOffset * 400f, 0f),
                        end = Offset((shimmerOffset + 1f) * 400f, 0f)
                    )
                }
            )
            .border(
                1.dp,
                if (revealed) AppColors.BlueGradientStart.copy(alpha = 0.2f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !revealed) { onTap() }
            .padding(24.dp)
    ) {
        if (revealed) {
            // Revealed state
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🎁", fontSize = 36.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Your Surprise Gift!",
                    color = AppColors.WalkthroughTextBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "A gift hamper will be handed over\nto you by your ASE",
                    color = AppColors.WalkthroughTextSubtle,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        } else {
            // Tap to reveal state
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🎁", fontSize = 36.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Tap to Reveal Surprise Gift",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "A special reward awaits you!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

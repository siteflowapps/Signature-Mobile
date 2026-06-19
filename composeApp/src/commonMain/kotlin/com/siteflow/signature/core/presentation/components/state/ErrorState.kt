package com.siteflow.signature.core.presentation.components.state

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import kotlinx.coroutines.delay

private const val MAX_AUTO_RETRIES = 3
private const val AUTO_RETRY_INTERVAL = 5

/**
 * Premium error state for list screens.
 *
 * Phase 1 (transient): Auto-retries up to 3 times at 5-second intervals.
 *   Shows "Something went wrong" with countdown.
 *
 * Phase 2 (persistent): After 3 failed retries, switches to a different
 *   message indicating a backend issue, with only a manual retry button.
 */
@Composable
fun ErrorState(
    message: String? = null,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var retryCount by remember { mutableIntStateOf(0) }
    var countdown by remember { mutableIntStateOf(AUTO_RETRY_INTERVAL) }
    val exhaustedRetries = retryCount >= MAX_AUTO_RETRIES

    // Auto-retry countdown (only while retries not exhausted)
    LaunchedEffect(retryCount) {
        if (!exhaustedRetries) {
            countdown = AUTO_RETRY_INTERVAL
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
            retryCount++
            onRetry()
        }
    }

    // Subtle breathing animation on the icon
    val infiniteTransition = rememberInfiniteTransition(label = "error_pulse")
    val iconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon — switches after exhausting retries
        Icon(
            imageVector = if (exhaustedRetries) Icons.Outlined.ErrorOutline
                          else Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = if (exhaustedRetries) Color(0xFFFBBF24).copy(alpha = iconAlpha)
                   else Color(0xFFE5E7EB).copy(alpha = iconAlpha),
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Title — changes based on phase
        Text(
            text = if (exhaustedRetries) "Service temporarily unavailable"
                   else "Something went wrong",
            style = AppTypography.TitleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        // Subtitle — changes based on phase
        Text(
            text = if (exhaustedRetries)
                "We're having trouble reaching our servers. This is usually temporary — please try again in a few minutes."
            else
                "We couldn't load your data. Retrying automatically…",
            style = AppTypography.Caption.copy(fontSize = 13.sp),
            color = AppColors.TextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Retry button
        Button(
            onClick = {
                retryCount = 0  // Reset retries on manual tap
                onRetry()
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (exhaustedRetries) Color(0xFF6B7280) else Color(0xFF14B8A6),
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Try Again",
                style = AppTypography.Caption.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        // Status text
        Text(
            text = if (exhaustedRetries)
                "If the issue persists, contact support"
            else
                "Retry ${retryCount + 1}/$MAX_AUTO_RETRIES · trying again in ${countdown}s",
            style = AppTypography.Caption.copy(fontSize = 11.sp),
            color = AppColors.TextTertiary.copy(alpha = 0.6f)
        )
    }
}

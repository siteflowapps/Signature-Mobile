package com.siteflow.signature.core.presentation.components.state

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography

/**
 * Animated "loading more" indicator shown at the bottom of paginated lists.
 * Three pulsing dots with a subtle label — premium feel.
 */
@Composable
fun LoadingMoreIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Three animated dots
        repeat(3) { index ->
            val delay = index * 160
            val animatedAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_alpha_$index"
            )
            val animatedScale by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale_$index"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(8.dp)
                    .scale(animatedScale)
                    .alpha(animatedAlpha)
                    .background(Color(0xFF3B82F6), CircleShape)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = "Loading more…",
            style = AppTypography.Caption.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = AppColors.TextTertiary
        )
    }
}

/**
 * "All caught up" indicator — shown when the user has reached the end of the list.
 * Subtle, non-intrusive with a check icon.
 */
@Composable
fun EndOfListIndicator(
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AppColors.Success.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "You're all caught up",
                style = AppTypography.Caption.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.TextTertiary
            )
            Text(
                text = "$itemCount items",
                style = AppTypography.Caption.copy(fontSize = 11.sp),
                color = AppColors.TextTertiary.copy(alpha = 0.7f)
            )
        }
    }
}

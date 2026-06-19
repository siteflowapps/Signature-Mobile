package com.siteflow.signature.core.presentation.components.toast

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Global toast overlay — top-aligned, non-blocking, swipe/tap to dismiss.
 *
 * UX improvements over the previous bottom toast:
 * - Appears at the TOP so it never hides action buttons
 * - Success: auto-hides in 2s, Error: 3s
 * - Tap anywhere on the toast or swipe up to dismiss instantly
 * - Compact pill design with close icon hint
 */
@Composable
fun GlobalToastOverlay() {
    var currentToast by remember { mutableStateOf<ToastMessage?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        GlobalToastHandler.toasts.collect { toast ->
            // If a toast is already showing, dismiss it first
            if (visible) {
                visible = false
                delay(200)
            }
            currentToast = toast
            visible = true

            // Auto-dismiss: success = 2s, error/info = 3s
            val duration = when (toast.type) {
                ToastType.SUCCESS -> 2000L
                ToastType.ERROR -> 3000L
                ToastType.INFO -> 2500L
            }
            delay(duration)
            visible = false
            delay(250) // wait for exit animation
            currentToast = null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(250)
            ) + fadeOut(animationSpec = tween(250))
        ) {
            currentToast?.let { toast ->
                val (bgColor, icon) = when (toast.type) {
                    ToastType.SUCCESS -> Color(0xFF10B981) to Icons.Default.CheckCircle
                    ToastType.ERROR -> Color(0xFFEF4444) to Icons.Default.Error
                    ToastType.INFO -> Color(0xFF14B8A6) to Icons.Default.Info
                }

                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(14.dp))
                        .background(bgColor, RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // Tap to dismiss
                            visible = false
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                // Swipe up to dismiss
                                if (dragAmount < -10f) {
                                    visible = false
                                }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = toast.message,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

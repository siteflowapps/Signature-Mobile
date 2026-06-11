package com.siteflow.cdo.core.presentation.components.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * Wraps content with a staggered fadeIn + slideInVertically animation.
 *
 * Each item animates in with a slight delay based on its [index],
 * creating a cascading "waterfall" effect like Linear, Notion, Stripe.
 *
 * Only the first [maxAnimatedItems] items animate (to avoid delayed
 * rendering on paginated "load-more" appends).
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    maxAnimatedItems: Int = 10,
    staggerDelayMs: Long = 50L,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (index < maxAnimatedItems) {
            delay(index * staggerDelayMs)
        }
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 300)
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        content()
    }
}

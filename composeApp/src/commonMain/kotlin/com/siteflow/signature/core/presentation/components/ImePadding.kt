package com.siteflow.signature.core.presentation.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * IME padding for screens hosted inside [AppScaffold].
 *
 * The app's [AppScaffold]/NavHost already pads content by the system
 * navigation-bar inset. A plain `Modifier.imePadding()` reserves the *full*
 * IME inset — which spans the nav-bar region too — so when the keyboard opens
 * the nav-bar gap is counted twice, leaving a stray gap above the keyboard.
 *
 * This excludes the nav-bar inset that the scaffold already applied, so the
 * keyboard sits flush against the content in both states.
 *
 * Apply to the ROOT container (not the bottom bar) so the scrollable region
 * gives up space to the pinned bottom bar instead of compressing it.
 */
fun Modifier.imeBottomPadding(): Modifier = composed {
    this.windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars))
}

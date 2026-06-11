package com.siteflow.cdo.core.navigation

import androidx.compose.runtime.Composable

// iOS back navigation is a swipe-right gesture handled by UINavigationController.
// There is no programmable intercept point at the Compose layer in CMP 1.9.x.
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

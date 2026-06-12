package com.siteflow.signature.core.presentation.components.loader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.siteflow.signature.core.presentation.design.AppColors

@Composable
fun GlobalLoadingOverlay() {
    val isLoading by GlobalLoading.isLoading.collectAsState()

    if (!isLoading) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = AppColors.blueEB
        )
    }
}

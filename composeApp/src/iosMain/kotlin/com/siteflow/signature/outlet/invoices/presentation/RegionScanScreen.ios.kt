package com.siteflow.signature.outlet.invoices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * iOS stub for RegionScanScreen.
 * Region-based OCR extraction is not yet implemented on iOS.
 * Shows a placeholder message and a Back button.
 */
@Composable
actual fun RegionScanScreen(
    imagePath: String,
    fieldLabel: String,
    onResult: (String) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Region scan not yet available on iOS", fontSize = 16.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text("Go Back")
            }
        }
    }
}

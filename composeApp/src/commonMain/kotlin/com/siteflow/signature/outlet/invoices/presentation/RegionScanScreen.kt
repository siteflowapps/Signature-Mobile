package com.siteflow.signature.outlet.invoices.presentation

import androidx.compose.runtime.Composable

/**
 * ROI manual field extraction screen.
 * Shows the invoice image with a draggable/resizable selection rectangle.
 * Crops the selected region and runs OCR to extract the value for a specific field.
 *
 * Android: Full implementation with ML Kit + Canvas crop.
 * iOS: Placeholder (not yet implemented).
 */
@Composable
expect fun RegionScanScreen(
    imagePath: String,
    fieldLabel: String,
    onResult: (String) -> Unit,
    onBack: () -> Unit
)

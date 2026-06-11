package com.siteflow.cdo.core.util

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Decodes a PNG/JPEG byte array into a Compose [ImageBitmap].
 * Platform-specific implementations use BitmapFactory (Android) or Skia (iOS).
 */
expect fun decodeToImageBitmap(bytes: ByteArray): ImageBitmap

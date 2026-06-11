package com.siteflow.cdo.core.presentation.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Modifier that dismisses the software keyboard when the user taps
 * outside any focused text field. Essential on iOS which lacks a
 * hardware back button to hide the keyboard.
 *
 * Usage: apply to the root `Column`/`Box` of any screen that has text input.
 */
fun Modifier.dismissKeyboardOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    this.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
    }
}

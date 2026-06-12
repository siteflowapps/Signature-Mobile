package com.siteflow.signature.core.presentation.components.textfield


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun ValidatedTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    showError: Boolean,
    errorText: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    leadingIcon: DrawableResource? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Text(text = label, style = AppTypography.BodySecondary, color = AppColors.grey51, fontSize = 12.sp)
    Spacer(Modifier.height(5.dp))

    SiteFlowTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        minLines = minLines,
        leadingIcon = leadingIcon,
        keyboardOptions = keyboardOptions
    )

    if (showError) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = errorText,
            color = Color(0xFFB00020),
            fontSize = 12.sp
        )
    }
}


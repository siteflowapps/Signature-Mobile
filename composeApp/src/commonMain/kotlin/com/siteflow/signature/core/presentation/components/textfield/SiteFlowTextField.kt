package com.siteflow.signature.core.presentation.components.textfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SiteFlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    leadingIcon: DrawableResource? = null,
    textStyle: TextStyle = AppTypography.BodyPrimary,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        singleLine = minLines == 1, // ✅ important for password & email
        textStyle = textStyle.copy(
            color = AppColors.InputTextColor
        ),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        placeholder = {
            Text(
                text = placeholder,
                style = textStyle.copy(
                    color = AppColors.greyBC
                )
            )
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp) // ✅ FIXED
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.greyEB,
            unfocusedBorderColor = AppColors.greyEB,
            disabledBorderColor = AppColors.greyEB,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = AppColors.InputTextColor,
            unfocusedTextColor = AppColors.InputTextColor
        )
    )
}



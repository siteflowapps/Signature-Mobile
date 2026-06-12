package com.siteflow.signature.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Reusable text field for the Signature app.
 * Based on SiteFlow's SiteFlowTextField — uses Material3 OutlinedTextField
 * with consistent styling, rounded corners, and a blue focus ring.
 */
@Composable
fun SignatureTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    prefix: String? = null,
    suffix: String? = null,
    minLines: Int = 1,
    characterLimit: Int? = null,
    leadingIcon: DrawableResource? = null,
    leadingIconVector: ImageVector? = null,
    trailingIcon: DrawableResource? = null,
    trailingIconVector: ImageVector? = null,
    trailingIconComposable: (@Composable () -> Unit)? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    textStyle: TextStyle = AppTypography.BodyPrimary.copy(fontSize = 15.sp),
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardOptions: KeyboardOptions? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    readOnly: Boolean = false
) {
    Column(modifier = modifier) {
        // Label row
        if (label != null) {
            Row {
                Text(
                    text = label,
                    style = AppTypography.TitleMedium.copy(fontSize = 14.sp),
                    color = Color(0xFF111827)
                )
                if (required) {
                    Text(
                        text = " *",
                        style = AppTypography.TitleMedium.copy(fontSize = 14.sp),
                        color = AppColors.Danger
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        val bgColor = if (enabled) Color.White else Color(0xFFF3F4F6)

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
            singleLine = minLines == 1,
            textStyle = textStyle.copy(color = Color(0xFF111827)),
            keyboardOptions = keyboardOptions
                ?: KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            placeholder = {
                Text(
                    text = placeholder,
                    style = textStyle.copy(color = Color(0xFF9CA3AF))
                )
            },
            prefix = if (prefix != null) {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prefix,
                            style = textStyle.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                        )
                        Spacer(Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(Color(0xFFE5E7EB))
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }
            } else null,
            suffix = if (suffix != null) {
                {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(Color(0xFFE5E7EB))
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = suffix,
                            style = textStyle.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B7280)
                            )
                        )
                    }
                }
            } else null,
            leadingIcon = when {
                leadingIconVector != null -> {
                    {
                        Icon(
                            imageVector = leadingIconVector,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                leadingIcon != null -> {
                    {
                        Icon(
                            painter = painterResource(leadingIcon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                else -> null
            },
            trailingIcon = when {
                trailingIconComposable != null -> trailingIconComposable
                trailingIconVector != null -> {
                    {
                        IconButton(
                            onClick = { onTrailingIconClick?.invoke() },
                            enabled = onTrailingIconClick != null
                        ) {
                            Icon(
                                imageVector = trailingIconVector,
                                contentDescription = null,
                                tint = if (onTrailingIconClick != null) AppColors.BlueGradientStart else Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                trailingIcon != null -> {
                    {
                        IconButton(
                            onClick = { onTrailingIconClick?.invoke() },
                            enabled = onTrailingIconClick != null
                        ) {
                            Icon(
                                painter = painterResource(trailingIcon),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                else -> null
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.BlueGradientStart,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                disabledBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor,
                disabledContainerColor = Color(0xFFF3F4F6),
                cursorColor = AppColors.BlueGradientStart,
                focusedTextColor = Color(0xFF111827),
                unfocusedTextColor = Color(0xFF111827)
            )
        )

        // Character counter with progress indicator
        if (characterLimit != null) {
            val progress = value.length.toFloat() / characterLimit
            val isComplete = value.length >= characterLimit
            val isTyping = value.isNotEmpty() && !isComplete
            // For short fixed-length fields (pincode, phone, OTP), reaching limit = success
            // For long free-text fields, reaching limit = warning
            val isFixedLength = characterLimit <= 20
            val counterColor = when {
                isComplete && isFixedLength -> AppColors.Success
                isComplete && !isFixedLength -> AppColors.Danger
                isTyping -> AppColors.BlueGradientStart
                else -> AppColors.TextTertiary.copy(alpha = 0.5f)
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                // Counter text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isComplete && isFixedLength) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AppColors.Success,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                    }
                    Text(
                        text = "${value.length} / $characterLimit",
                        style = AppTypography.Caption.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isComplete) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = counterColor
                    )
                }
                // Slim progress bar
                if (isFixedLength) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = counterColor,
                        trackColor = Color(0xFFE5E7EB),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}

package com.siteflow.cdo.core.presentation.design


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

actual object AppTypography {

    actual val TitleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold
    )
    actual val TitleSemiLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold
    )
    actual val TitleMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    )

    actual val BodyPrimary = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    )

    actual val BodySecondary = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Gray
    )

    actual val Caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )

    actual val Button = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}

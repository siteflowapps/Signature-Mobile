package com.siteflow.signature.core.presentation.design


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.siteflow.signature.R

private val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

actual object AppTypography {

    actual val TitleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )

    actual val TitleSemiLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold
    )

    actual val TitleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    )

    actual val BodyPrimary = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    )

    actual val BodySecondary = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Gray
    )

    actual val Caption = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )

    actual val Button = TextStyle(
        fontFamily = InterFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
}

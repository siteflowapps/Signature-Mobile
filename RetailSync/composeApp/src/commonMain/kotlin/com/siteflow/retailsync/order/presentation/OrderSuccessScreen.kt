package com.siteflow.retailsync.order.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.retailsync.core.presentation.components.CdoButton
import com.siteflow.retailsync.core.presentation.design.AppColors
import com.siteflow.retailsync.core.presentation.design.AppTypography
import kotlinx.coroutines.delay

@Composable
fun OrderSuccessScreen(
    orderId: String,
    onNavigateToInvoices: () -> Unit
) {
    val checkScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(200)
        checkScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(AppColors.BackgroundGradientStart, AppColors.BackgroundGradientEnd)
                )
            )
            .statusBarsPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(checkScale.value)
                .background(
                    color = AppColors.greenE7,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AppColors.green4A,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Invoice Created!",
            style = AppTypography.TitleLarge,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Your invoice has been successfully created\nand sent for processing.",
            style = AppTypography.BodyPrimary,
            color = AppColors.TextTertiary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(24.dp))

        // Order ID chip
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AppColors.lightBlueFF
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Order ID: ",
                    style = AppTypography.BodySecondary,
                    color = AppColors.TextTertiary
                )
                Text(
                    orderId,
                    style = AppTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.BlueGradientStart
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        CdoButton(
            text = "Back to Invoices",
            onClick = onNavigateToInvoices,
            trailingIcon = null
        )
    }
}

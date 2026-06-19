package com.siteflow.signature.core.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.domain.RoleManager
import com.siteflow.signature.core.domain.UserRole
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography

@Composable
fun DevRolePickerScreen(
    onRoleSelected: (UserRole) -> Unit
) {
    // 🎨 Animated Gradient
    val infiniteTransition = rememberInfiniteTransition()
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.BackgroundGradientStart,
                        AppColors.BackgroundGradientEnd
                    ),
                    startY = -animatedOffset,
                    endY = 1000f + animatedOffset
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            Text(
                text = "🛠️ Dev Mode",
                style = AppTypography.Caption.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = AppColors.Warning,
                modifier = Modifier
                    .background(
                        AppColors.Warning.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Select Your Role",
                style = AppTypography.TitleLarge.copy(fontSize = 28.sp),
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Choose a role to preview the experience",
                style = AppTypography.BodyPrimary,
                color = AppColors.TextTertiary
            )

            Spacer(Modifier.height(48.dp))

            // Role Cards
            RoleCard(
                title = "Outlet / Retailer",
                subtitle = "Dashboard, Upload Invoice, View Status",
                icon = Icons.Default.ShoppingCart,
                gradientColors = listOf(Color(0xFF10B981), Color(0xFF34D399)),
                onClick = {
                    RoleManager.setRole(UserRole.OUTLET)
                    onRoleSelected(UserRole.OUTLET)
                }
            )

            Spacer(Modifier.height(16.dp))

            RoleCard(
                title = "Customer Sales Officer (CSO)",
                subtitle = "Onboard Outlets, Cooler/Branding, Compliance",
                icon = Icons.Default.Person,
                gradientColors = listOf(Color(0xFF0F766E), Color(0xFF14B8A6)),
                onClick = {
                    RoleManager.setRole(UserRole.CSO)
                    onRoleSelected(UserRole.CSO)
                }
            )

            Spacer(Modifier.height(16.dp))

            RoleCard(
                title = "Sales Executive (ASE)",
                subtitle = "Level 1 Approvals (onboarding, assets)",
                icon = Icons.Default.Person,
                gradientColors = listOf(Color(0xFF6366F1), Color(0xFF818CF8)),
                onClick = {
                    RoleManager.setRole(UserRole.ASE)
                    onRoleSelected(UserRole.ASE)
                }
            )

            Spacer(Modifier.height(16.dp))

            RoleCard(
                title = "Area Sales Manager (ASM)",
                subtitle = "Team Dashboard, Invoice Approval (L2)",
                icon = Icons.Default.Star,
                gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
                onClick = {
                    RoleManager.setRole(UserRole.ASM)
                    onRoleSelected(UserRole.ASM)
                }
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "This screen is for development only.\nWill be replaced by JWT role detection.",
                style = AppTypography.Caption.copy(fontSize = 11.sp),
                color = AppColors.TextTertiary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = gradientColors.first().copy(alpha = 0.3f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        brush = Brush.linearGradient(gradientColors),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTypography.TitleMedium.copy(fontSize = 16.sp),
                    color = Color(0xFF111827)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AppTypography.Caption,
                    color = AppColors.TextTertiary
                )
            }

            Text(
                text = "→",
                style = AppTypography.TitleLarge.copy(fontSize = 20.sp),
                color = AppColors.TextTertiary
            )
        }
    }
}

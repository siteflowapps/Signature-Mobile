package com.siteflow.signature.ase.approvals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography

/**
 * Placeholder home for the ASE (Area Sales Executive) role.
 *
 * In the new role model the ASE is a first-line approver — they give Level 1
 * approval on CSO onboardings and cooler/asset requests, and initiate rental
 * amendments. That approval queue is built in Phase 2; this screen stands in so
 * ASE login resolves to a coherent destination in the meantime.
 */
@Composable
fun AseApprovalsHomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FactCheck,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = "Approvals",
                style = AppTypography.TitleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                text = "Level 1 review of CSO onboardings and cooler/asset requests is coming soon.",
                style = AppTypography.BodyPrimary,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

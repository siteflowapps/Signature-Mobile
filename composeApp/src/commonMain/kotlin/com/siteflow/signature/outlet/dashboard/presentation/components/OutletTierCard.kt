package com.siteflow.signature.outlet.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.outlet.dashboard.data.OutletTier

@Composable
fun OutletTierCard(tier: OutletTier) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = tier.bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Emoji badge
            Text(
                text = tier.emoji,
                fontSize = 28.sp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Outlet Classification",
                    style = AppTypography.Caption.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = tier.color.copy(alpha = 0.7f)
                )
                Text(
                    text = "${tier.label} Retailer",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = tier.color
                )
            }

            // Tier badge
            Text(
                text = tier.label.uppercase(),
                style = AppTypography.Caption.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = tier.color,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

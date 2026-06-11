package com.siteflow.cdo.ase.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.siteflow.cdo.ase.dashboard.data.CdoStep
import com.siteflow.cdo.ase.dashboard.data.AssetStatus
import com.siteflow.cdo.ase.dashboard.data.OutletItem
import com.siteflow.cdo.ase.dashboard.data.OutletStatus
import com.siteflow.cdo.core.presentation.components.CdoShimmerEffect
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography

@Composable
fun BottomCtaBar(
    outlet: OutletItem,
    onAction: () -> Unit,
    onSubmitCompliance: () -> Unit,
    onUploadCdoPhoto: () -> Unit,
    onRequestAsset: () -> Unit = {}
) {
    val (label, color, action) = when {
        outlet.status == OutletStatus.ASM_REJECTED -> Triple("Edit & Resubmit", AppColors.Danger, onAction)
        outlet.status == OutletStatus.ASM_APPROVED && outlet.assetStatus == AssetStatus.NOT_REQUESTED ->
            Triple("Request Asset", AppColors.BlueGradientStart, onRequestAsset)
        outlet.assetStatus == AssetStatus.VERIFICATION_PENDING -> return // compliance already submitted
        outlet.nextPendingStep == CdoStep.CDO_VERIFICATION -> Triple("Submit Compliance", AppColors.BlueGradientStart, onSubmitCompliance)
        else -> return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.CardBackground,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Button(
                onClick = action,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text(
                    text = label,
                    style = AppTypography.Button,
                    color = AppColors.CardBackground
                )
            }
        }
    }
}

@Composable
fun SkeletonOutletDetail() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Skeleton Header Card
            Card(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                CdoShimmerEffect(modifier = Modifier.fillMaxSize())
            }
            
            // Skeleton section
            Card(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                CdoShimmerEffect(modifier = Modifier.fillMaxSize())
            }
            
            // Skeleton section
            Card(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                CdoShimmerEffect(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

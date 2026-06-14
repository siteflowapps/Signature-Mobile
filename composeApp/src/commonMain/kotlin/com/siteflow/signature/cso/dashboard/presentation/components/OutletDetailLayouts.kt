package com.siteflow.signature.cso.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.siteflow.signature.cso.dashboard.data.SignatureStep
import com.siteflow.signature.cso.dashboard.data.AssetStatus
import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.dashboard.data.OutletStatus
import com.siteflow.signature.core.presentation.components.SignatureShimmerEffect
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography

@Composable
fun BottomCtaBar(
    outlet: OutletItem,
    onAction: () -> Unit,
    onSubmitCompliance: () -> Unit,
    onUploadSignaturePhoto: () -> Unit
) {
    // Cooler/branding raise + compliance live on the AssetCards; this bar only
    // handles onboarding resubmit and signature-verification compliance.
    val (label, color, action) = when {
        outlet.status == OutletStatus.ASM_REJECTED -> Triple("Edit & Resubmit", AppColors.Danger, onAction)
        outlet.assetStatus == AssetStatus.VERIFICATION_PENDING -> return // compliance already submitted
        outlet.nextPendingStep == SignatureStep.SIGNATURE_VERIFICATION -> Triple("Submit Compliance", AppColors.BlueGradientStart, onSubmitCompliance)
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
                SignatureShimmerEffect(modifier = Modifier.fillMaxSize())
            }
            
            // Skeleton section
            Card(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                SignatureShimmerEffect(modifier = Modifier.fillMaxSize())
            }
            
            // Skeleton section
            Card(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                SignatureShimmerEffect(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

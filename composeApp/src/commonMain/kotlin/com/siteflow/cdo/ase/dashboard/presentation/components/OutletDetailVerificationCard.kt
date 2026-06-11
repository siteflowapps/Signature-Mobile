package com.siteflow.cdo.ase.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.siteflow.cdo.ase.dashboard.data.OutletItem
import com.siteflow.cdo.ase.onboarding.data.PhotoSlot
import com.siteflow.cdo.ase.dashboard.presentation.SectionCard
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography

@Composable
fun OutletDetailVerificationCard(
    outlet: OutletItem,
    onViewPhoto: (PhotoSlot) -> Unit,
    onAddPhoto: (String) -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    SectionCard(
        "CDO Verification",
        trailing = "${outlet.cdoVerificationPhotos.size}/5"
    ) {
        val displaySlots = outlet.cdoVerificationPhotos.toMutableList()
        if (displaySlots.size < 5) {
            displaySlots.add(PhotoSlot("add_new", "Add Photo", false))
        }
        
        displaySlots.chunked(2).forEachIndexed { rowIndex, rowSlots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowSlots.forEach { slot ->
                    val isAddButton = slot.id == "add_new"
                    val isCaptured = slot.imagePath != null
                    
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isCaptured) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onViewPhoto(slot) }
                            ) {
                                AsyncImage(
                                    model = slot.imagePath,
                                    contentDescription = slot.label,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Gradient overlay at bottom for GPS info
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.7f)
                                                )
                                            )
                                        )
                                        .padding(4.dp)
                                ) {
                                    if (outlet.gpsLocation != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(Modifier.width(2.dp))
                                            Text(
                                                text = "${outlet.gpsLocation.latitude}° N, ${outlet.gpsLocation.longitude}° E",
                                                style = AppTypography.Caption.copy(fontSize = 8.sp),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                // Re-capture button (top-right)
                                IconButton(
                                    onClick = { onAddPhoto(slot.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Re-capture",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }

                                // Remove button (top-left)
                                IconButton(
                                    onClick = { onRemovePhoto(slot.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .background(
                                        if (isAddButton) AppColors.blueFE else AppColors.greyF6,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { if (isAddButton) onAddPhoto(slot.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAddButton) Icons.Default.Add else Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = if (isAddButton) AppColors.BlueGradientStart else AppColors.PlaceholderTextColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            text = slot.label,
                            style = AppTypography.BodyPrimary.copy(fontSize = 12.sp),
                            color = if (isAddButton) AppColors.BlueGradientStart else AppColors.TextPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(2.dp))
                        if (!isAddButton) {
                            Text(
                                text = if (slot.required) "Required" else "Optional",
                                style = AppTypography.Caption.copy(fontSize = 11.sp),
                                color = AppColors.TextTertiary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                if (rowSlots.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
            if (rowIndex < (displaySlots.size + 1) / 2 - 1) {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

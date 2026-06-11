import re

with open("composeApp/src/commonMain/kotlin/com/siteflow/cdo/ase/dashboard/presentation/OutletDetailScreen.kt", "r") as f:
    code = f.read()

# Replace general colors
code = code.replace("Color(0xFF111827)", "AppColors.TextPrimary")
code = code.replace("Color(0xFF6B7280)", "AppColors.TextSecondary")
code = code.replace("Color(0xFFF3F4F6)", "AppColors.greyF6")
code = code.replace("Color(0xFFE5E7EB)", "AppColors.Divider")
code = code.replace("Color(0xFFD1D5DB)", "AppColors.PlaceholderTextColor")
code = code.replace("Color(0xFFD1FAE5)", "AppColors.greenE7")
code = code.replace("Color(0xFFFEF3C7)", "AppColors.orangeEB")
code = code.replace("Color(0xFFDBEAFE)", "AppColors.blueFE")
code = code.replace("Color(0xFFF59E0B)", "AppColors.Warning")
code = code.replace("Color(0xFFF9FAFB)", "AppColors.ScreenBackground")

# For 'Color.White', conditionally replace CardBackgrounds
code = code.replace("containerColor = Color.White", "containerColor = AppColors.CardBackground")
code = code.replace("color = Color.White", "color = AppColors.CardBackground")

# Layout Replacement for PhotosCard
old_photos = """@Composable
private fun PhotosCard(outlet: OutletItem, onViewPhoto: (PhotoSlot) -> Unit) {
    SectionCard(
        "Captured Photos",
        trailing = "${outlet.capturedPhotoCount}/${outlet.photoSlots.size}"
    ) {
        outlet.photoSlots.forEachIndexed { index, slot ->
            val isCaptured = slot.imagePath != null

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isCaptured) Modifier.clickable { onViewPhoto(slot) }
                        else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo thumbnail placeholder
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (isCaptured) AppColors.greenE7 else AppColors.greyF6,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCaptured) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = if (isCaptured) AppColors.Success else AppColors.PlaceholderTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.label,
                        style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = if (slot.required) "Required" else "Optional",
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary
                    )
                }

                if (isCaptured) {
                    // View button for captured photos
                    Row(
                        modifier = Modifier
                            .background(AppColors.blueFE, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "View photo",
                            tint = AppColors.BlueGradientStart,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "View",
                            style = AppTypography.Caption.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AppColors.BlueGradientStart
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(AppColors.orangeEB, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Missing",
                            style = AppTypography.Caption.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AppColors.Warning
                        )
                    }
                }
            }

            if (index < outlet.photoSlots.size - 1) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = AppColors.greyF6, modifier = Modifier.padding(start = 68.dp))
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}"""

new_photos = """@Composable
private fun PhotosCard(outlet: OutletItem, onViewPhoto: (PhotoSlot) -> Unit) {
    SectionCard(
        "Captured Photos",
        trailing = "${outlet.capturedPhotoCount}/${outlet.photoSlots.size}"
    ) {
        outlet.photoSlots.chunked(2).forEachIndexed { rowIndex, rowSlots ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowSlots.forEach { slot ->
                    val isCaptured = slot.imagePath != null
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (isCaptured) Modifier.clickable { onViewPhoto(slot) }
                                else Modifier
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Photo thumbnail placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .background(
                                    if (isCaptured) AppColors.greenE7 else AppColors.greyF6,
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCaptured) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (isCaptured) AppColors.Success else AppColors.PlaceholderTextColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            text = slot.label,
                            style = AppTypography.BodyPrimary.copy(fontSize = 12.sp),
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (slot.required) "Required" else "Optional",
                            style = AppTypography.Caption.copy(fontSize = 11.sp),
                            color = AppColors.TextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (rowSlots.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
            if (rowIndex < (outlet.photoSlots.size + 1) / 2 - 1) {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}"""
if old_photos in code:
    print("Replaced PhotosCard")
    code = code.replace(old_photos, new_photos)

# Layout TappablePhoneField
old_tap = """@Composable
private fun TappablePhoneField(
    label: String,
    number: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label,
            style = AppTypography.Caption.copy(fontSize = 11.sp),
            color = AppColors.TextTertiary
        )
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call $number",
                tint = AppColors.BlueGradientStart,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = number,
                style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                color = AppColors.BlueGradientStart
            )
        }
    }
}"""

new_tap = """@Composable
private fun TappablePhoneField(
    label: String,
    number: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label,
            style = AppTypography.Caption.copy(fontSize = 11.sp),
            color = AppColors.TextTertiary
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .background(AppColors.lightBlueFF, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call $number",
                tint = AppColors.BlueGradientStart,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = number,
                style = AppTypography.BodyPrimary.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                color = AppColors.BlueGradientStart
            )
        }
    }
}"""
if old_tap in code:
    print("Replaced TappablePhoneField")
    code = code.replace(old_tap, new_tap)

# Layout BottomCtaBar
old_cta = """@Composable
private fun BottomCtaBar(status: OutletStatus, onAction: () -> Unit) {
    val (label, color) = when (status) {
        OutletStatus.DRAFT -> "Continue Onboarding" to AppColors.BlueGradientStart
        OutletStatus.REJECTED -> "Edit & Resubmit" to AppColors.Danger
        else -> return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.CardBackground,
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
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
}"""

new_cta = """@Composable
private fun BottomCtaBar(status: OutletStatus, onAction: () -> Unit) {
    val (label, color) = when (status) {
        OutletStatus.DRAFT -> "Continue Onboarding" to AppColors.BlueGradientStart
        OutletStatus.REJECTED -> "Edit & Resubmit" to AppColors.Danger
        else -> return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.CardBackground,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = onAction,
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
}"""
if old_cta in code:
    print("Replaced BottomCtaBar")
    code = code.replace(old_cta, new_cta)


# Append SkeletonOutletDetail
skeleton = """

@Composable
fun SkeletonOutletDetail() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground)
            .windowInsetsPadding(WindowInsets.navigationBars)
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
"""

if "fun SkeletonOutletDetail()" not in code:
    code += skeleton

with open("composeApp/src/commonMain/kotlin/com/siteflow/cdo/ase/dashboard/presentation/OutletDetailScreen.kt", "w") as f:
    f.write(code)

print("Done")

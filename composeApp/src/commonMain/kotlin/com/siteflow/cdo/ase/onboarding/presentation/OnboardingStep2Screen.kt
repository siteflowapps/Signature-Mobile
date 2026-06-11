package com.siteflow.cdo.ase.onboarding.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.ase.onboarding.data.Classification
import com.siteflow.cdo.ase.onboarding.data.dto.SlabDto
import com.siteflow.cdo.ase.onboarding.domain.OnboardingAction
import com.siteflow.cdo.ase.onboarding.domain.OnboardingEvent
import com.siteflow.cdo.ase.onboarding.domain.OnboardingViewModel
import com.siteflow.cdo.ase.onboarding.domain.OnboardingValidator
import com.siteflow.cdo.ase.onboarding.domain.PayoutType
import com.siteflow.cdo.core.presentation.components.CdoButton
import com.siteflow.cdo.core.presentation.components.CdoTextField
import com.siteflow.cdo.core.presentation.components.dismissKeyboardOnTap
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun OnboardingStep2Screen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    outletId: String? = null,
    viewModel: OnboardingViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(OnboardingAction.SetCurrentStep(2))
        // When resuming from dashboard, set the outlet ID in state
        outletId?.let { viewModel.onAction(OnboardingAction.SetOutletId(it)) }
        // Fetch classification slabs from API
        viewModel.onAction(OnboardingAction.FetchSlabs)
        viewModel.events.collectLatest { event ->
            when (event) {
                OnboardingEvent.NavigateBack -> onBack()
                OnboardingEvent.NavigateToStep3 -> onContinue()
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .dismissKeyboardOnTap()
    ) {
        // ── Content (scrollable) ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // How Payout Works Card
            PayoutInfoCard()

            Spacer(Modifier.height(20.dp))

            // ── Payout Type Selector ──
            PayoutTypeSelector(
                selectedType = state.payoutType,
                fixedMonthlyVolume = state.fixedMonthlyVolume,
                fixedMonthlyAmount = state.fixedMonthlyAmount,
                onTypeSelected = { viewModel.onAction(OnboardingAction.PayoutTypeSelected(it)) },
                onVolumeChanged = { viewModel.onAction(OnboardingAction.FixedMonthlyVolumeChanged(it)) },
                onAmountChanged = { viewModel.onAction(OnboardingAction.FixedMonthlyAmountChanged(it)) }
            )

            Spacer(Modifier.height(20.dp))

            // Suggested Classification Hero
            Text(
                text = "Suggested Classification",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 15.sp, 
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF374151)
            )

            Spacer(Modifier.height(12.dp))

            ClassificationHeroCard(
                classificationLabel = state.selectedClassification,
                slabs = state.slabs
            )

            Spacer(Modifier.height(20.dp))

            // Classification Slabs (dynamic from API)
            if (state.isSlabsLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = AppColors.BlueGradientStart
                    )
                }
            } else if (state.slabs.isNotEmpty()) {
                DynamicSlabsList(
                    slabs = state.slabs,
                    selectedClassification = state.selectedClassification,
                    onSlabSelected = { slabId ->
                        viewModel.onAction(OnboardingAction.SlabSelected(slabId))
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Bottom Button ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp)
                .background(Color.White)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            CdoButton(
                text = "Continue",
                onClick = { viewModel.onAction(OnboardingAction.ContinueToNextStep) },
                enabled = OnboardingValidator.isStep2Valid(state) && !state.isLoading,
                loading = state.isLoading
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Payout Type Selector
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun PayoutTypeSelector(
    selectedType: PayoutType,
    fixedMonthlyVolume: String,
    fixedMonthlyAmount: String,
    onTypeSelected: (PayoutType) -> Unit,
    onVolumeChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Payout Type",
            style = AppTypography.TitleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color(0xFF374151)
        )

        Spacer(Modifier.height(12.dp))

        // ── Dynamic Payout Card ──
        PayoutOptionCard(
            type = PayoutType.DYNAMIC,
            isSelected = selectedType == PayoutType.DYNAMIC,
            icon = Icons.Default.TrendingUp,
            accentColor = Color(0xFF2563EB),
            accentBg = Color(0xFFEFF6FF),
            selectedBorder = Color(0xFF93C5FD),
            selectedBg = Color(0xFFF0F6FF),
            tagText = null,
            tagBg = Color(0xFFDBEAFE),
            tagColor = Color(0xFF1E40AF),
            onClick = { onTypeSelected(PayoutType.DYNAMIC) }
        )

        Spacer(Modifier.height(12.dp))

//        // ── Fixed Payout Card ──
//        PayoutOptionCard(
//            type = PayoutType.FIXED,
//            isSelected = selectedType == PayoutType.FIXED,
//            icon = Icons.Default.AccountBalance,
//            accentColor = Color(0xFF7C3AED),
//            accentBg = Color(0xFFF5F3FF),
//            selectedBorder = Color(0xFFC4B5FD),
//            selectedBg = Color(0xFFFAF5FF),
//            tagText = null,
//            tagBg = Color.Transparent,
//            tagColor = Color.Transparent,
//            onClick = { onTypeSelected(PayoutType.FIXED) }
//        )

        // ── Fixed Payout Input Fields (animated) ──
        AnimatedVisibility(
            visible = selectedType == PayoutType.FIXED,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Info hint
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFF5F3FF),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Enter the monthly commitment details for this outlet's fixed payout agreement.",
                            style = AppTypography.Caption.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = Color(0xFF5B21B6)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Monthly Volume (Cases)
                    CdoTextField(
                        value = fixedMonthlyVolume,
                        onValueChange = onVolumeChanged,
                        placeholder = "e.g. 50",
                        label = "Monthly Volume Commitment",
                        required = true,
                        suffix = "cases",
                        leadingIconVector = Icons.Default.Inventory2,
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(Modifier.height(16.dp))

                    // Monthly Amount (₹)
                    CdoTextField(
                        value = fixedMonthlyAmount,
                        onValueChange = onAmountChanged,
                        placeholder = "e.g. 5000",
                        label = "Monthly Payout Amount",
                        required = true,
                        prefix = "₹",
                        leadingIconVector = Icons.Default.CurrencyRupee,
                        keyboardType = KeyboardType.Number
                    )
                }
            }
        }
    }
}

@Composable
private fun PayoutOptionCard(
    type: PayoutType,
    isSelected: Boolean,
    icon: ImageVector,
    accentColor: Color,
    accentBg: Color,
    selectedBorder: Color,
    selectedBg: Color,
    tagText: String?,
    tagBg: Color,
    tagColor: Color,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) selectedBorder else Color(0xFFE5E7EB),
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) selectedBg else Color.White,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio indicator
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) accentColor else Color(0xFFD1D5DB),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(accentColor, CircleShape)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = type.label,
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isSelected) Color(0xFF111827) else Color(0xFF374151)
                    )
                    if (tagText != null) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(tagBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tagText,
                                style = AppTypography.Caption.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = tagColor
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = type.description,
                    style = AppTypography.Caption.copy(
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    ),
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun PayoutInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1E40AF), Color(0xFF3B82F6))
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "💰 How Payout Works",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                PayoutInfoRow(
                    icon = Icons.Default.TrendingUp,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    title = "Volume-Based Slab",
                    subtitle = "Your payout tier is determined by your cumulative monthly case volume"
                )

                Spacer(Modifier.height(14.dp))

                PayoutInfoRow(
                    icon = Icons.Default.CurrencyRupee,
                    iconBg = Color(0xFFF0FDF4),
                    iconTint = Color(0xFF16A34A),
                    title = "Fixed ₹ per Case",
                    subtitle = "You earn a fixed rupee amount for every qualifying case delivered."
                )

                Spacer(Modifier.height(14.dp))

                PayoutInfoRow(
                    icon = Icons.Default.Category,
                    iconBg = Color(0xFFFFF7ED),
                    iconTint = Color(0xFFEA580C),
                    title = "CSD, Juices and Energy categories Included",
                    subtitle = "Payout is applicable on CSD, Juices and Energy categories."
                )
            }
        }
    }
}

@Composable
private fun PayoutInfoRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.TitleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = AppTypography.Caption.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun InfoBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFEF3C7), // amber-100 (solid, matches Step 1)
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = Color(0xFFD97706), // amber-600
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = AppTypography.BodyPrimary.copy(fontSize = 13.sp),
            color = Color(0xFF92400E) // amber-800
        )
    }
}

@Composable
private fun ClassificationHeroCard(
    classificationLabel: String,
    slabs: List<SlabDto>
) {
    // Find the index of the selected slab in the sorted list to get the right color theme
    val sorted = slabs.sortedByDescending { it.minQuantity }
    val selectedIndex = sorted.indexOfFirst { it.classification == classificationLabel }
    val colors = getThemeByIndex(if (selectedIndex >= 0) selectedIndex else sorted.size - 1)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(colors.background, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colors.iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = colors.icon,
                    contentDescription = "Classification: $classificationLabel",
                    tint = colors.iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = classificationLabel.ifBlank { "—" },
                style = AppTypography.TitleMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = colors.text
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = "Slab decided by actual sales volume",
                style = AppTypography.Caption.copy(fontSize = 14.sp),
                color = colors.text.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ClassificationSlabsList(selected: Classification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "CLASSIFICATION SLABS",
                    style = AppTypography.Caption.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF6B7280)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Classification.values().reversed().forEachIndexed { index, item ->
                ClassificationSlabItem(
                    classification = item,
                    isSelected = item == selected
                )
                if (index < Classification.values().size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFF3F4F6)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassificationSlabItem(
    classification: Classification,
    isSelected: Boolean
) {
    val theme = getClassificationTheme(classification)
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) theme.background.copy(alpha = 0.5f) else Color.Transparent
    )
    val borderColor = if (isSelected) theme.border else Color.Transparent
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(theme.iconBg.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = theme.icon,
                contentDescription = "${classification.label} tier",
                tint = theme.iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = classification.label,
                style = AppTypography.TitleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF111827)
            )
            Text(
                text = classification.description,
                style = AppTypography.Caption.copy(fontSize = 13.sp),
                color = Color(0xFF6B7280)
            )
        }
        
        Text(
            text = classification.range,
            style = AppTypography.Caption.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = if (isSelected) theme.text else Color(0xFF374151)
        )
    }
}

private data class ClassificationTheme(
    val background: Color,
    val border: Color,
    val iconBg: Color,
    val iconTint: Color,
    val text: Color,
    val icon: ImageVector
)

@Composable
private fun getClassificationTheme(classification: Classification): ClassificationTheme {
    return when (classification) {
        Classification.PLATINUM -> ClassificationTheme(
            background = Color(0xFFF5F3FF), // Light purple
            border = Color(0xFFDDD6FE),
            iconBg = Color(0xFFDDD6FE),
            iconTint = Color(0xFF7C3AED),
            text = Color(0xFF5B21B6),
            icon = Icons.Default.Diamond
        )
        Classification.DIAMOND -> ClassificationTheme(
            background = Color(0xFFF0F9FF), // Light blue
            border = Color(0xFFBAE6FD),
            iconBg = Color(0xFFBAE6FD),
            iconTint = Color(0xFF0284C7),
            text = Color(0xFF075985),
            icon = Icons.Default.Diamond
        )
        Classification.GOLD -> ClassificationTheme(
            background = Color(0xFFFFFBEB), // Light gold
            border = Color(0xFFFEF3C7),
            iconBg = Color(0xFFFEF3C7),
            iconTint = Color(0xFFD97706),
            text = Color(0xFF92400E),
            icon = Icons.Default.WorkspacePremium
        )
        Classification.SILVER -> ClassificationTheme(
            background = Color(0xFFF9FAFB), // Light grey
            border = Color(0xFFE5E7EB),
            iconBg = Color(0xFFE5E7EB),
            iconTint = Color(0xFF6B7280),
            text = Color(0xFF374151),
            icon = Icons.Default.MilitaryTech
        )
    }
}

/**
 * Assigns a color theme by position index (sorted highest tier first).
 * index 0 = Platinum (purple), 1 = Diamond (blue), 2 = Gold (amber), 3+ = Silver (grey)
 */
@Composable
private fun getThemeByIndex(index: Int): ClassificationTheme {
    return when (index) {
        0 -> getClassificationTheme(Classification.PLATINUM)
        1 -> getClassificationTheme(Classification.DIAMOND)
        2 -> getClassificationTheme(Classification.GOLD)
        else -> getClassificationTheme(Classification.SILVER)
    }
}

@Composable
private fun DynamicSlabsList(
    slabs: List<SlabDto>,
    selectedClassification: String,
    onSlabSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "CLASSIFICATION SLABS",
                    style = AppTypography.Caption.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Sort by minQuantity descending (highest tier first)
            val sorted = slabs.sortedByDescending { it.minQuantity }
            sorted.forEachIndexed { index, slab ->
                val isSelected = slab.classification == selectedClassification
                val theme = getThemeByIndex(index)
                val range = if (slab.maxQuantity != null)
                    "${slab.minQuantity.toInt()}-${slab.maxQuantity.toInt()} cs"
                else
                    "${slab.minQuantity.toInt()}+ cs"

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) theme.background.copy(alpha = 0.5f) else Color.Transparent
                )
                val borderColor = if (isSelected) theme.border else Color.Transparent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSlabSelected(slab.id) }
                        .background(bgColor)
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Slab icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(theme.iconBg.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = theme.icon,
                            contentDescription = "${slab.classification} tier",
                            tint = theme.iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = slab.classification,
                            style = AppTypography.TitleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = range,
                            style = AppTypography.Caption.copy(fontSize = 13.sp),
                            color = Color(0xFF6B7280)
                        )
                    }

                    val payoutText = if (slab.percentage != null) {
                        "${slab.percentage.toInt()}% payout"
                    } else if (slab.ratePerCase != null) {
                        "₹${slab.ratePerCase.toInt()}/cs payout"
                    } else {
                        "0% payout"
                    }
                    Text(
                        text = payoutText,
                        style = AppTypography.Caption.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isSelected) theme.text else Color(0xFF374151)
                    )
                }

                if (index < sorted.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFF3F4F6)
                    )
                }
            }
        }
    }
}

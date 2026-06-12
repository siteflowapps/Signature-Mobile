package com.siteflow.signature.outlet.dashboard.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.cso.onboarding.data.dto.SlabDto
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.outlet.dashboard.domain.OutletDashboardAction
import com.siteflow.signature.outlet.dashboard.domain.OutletDashboardViewModel
import com.siteflow.signature.outlet.dashboard.presentation.components.*
import org.koin.compose.koinInject

/**
 * Outlet Dashboard Screen — Home tab.
 * Payout Summary → Upload Invoice → Classification Slabs.
 */
@Composable
fun OutletDashboardScreen(
    onUploadInvoice: () -> Unit = {},
    onViewAllInvoices: () -> Unit = {},
    onNavigateToInvoices: (initialFilter: String) -> Unit = {},
    viewModel: OutletDashboardViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(OutletDashboardAction.LoadDashboard)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                com.siteflow.signature.outlet.dashboard.domain.OutletDashboardEvent.NavigateToUploadInvoice -> onUploadInvoice()
                com.siteflow.signature.outlet.dashboard.domain.OutletDashboardEvent.NavigateToInvoiceList -> onViewAllInvoices()
                com.siteflow.signature.outlet.dashboard.domain.OutletDashboardEvent.NavigateToInvoiceListAll -> onNavigateToInvoices("All")
                com.siteflow.signature.outlet.dashboard.domain.OutletDashboardEvent.NavigateToInvoiceListPending -> onNavigateToInvoices("Pending")
                is com.siteflow.signature.outlet.dashboard.domain.OutletDashboardEvent.NavigateToInvoiceDetail -> { /* handled by caller if needed */ }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // ── Payout Summary Card ──
        state.payoutSummary?.let { payout ->
            OutletPayoutCard(
                payout = payout,
                onTotalInvoicesClick = { viewModel.onAction(OutletDashboardAction.TotalInvoicesClicked) },
                onPendingInvoicesClick = { viewModel.onAction(OutletDashboardAction.PendingInvoicesClicked) }
            )
            Spacer(Modifier.height(16.dp))
        }

        // ── Upload Invoice CTA ──
        Button(
            onClick = onUploadInvoice,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = AppColors.Primary.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Upload Invoice",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── How Payout Works ──
        PayoutInfoCard()

        Spacer(Modifier.height(20.dp))

        // ── Classification Slabs (from API) ──
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
            ReadOnlySlabsList(slabs = state.slabs)
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ═══════════════════════════════════════════════════════
// Payout Info Card — How Payout Works
// ═══════════════════════════════════════════════════════

@Composable
private fun PayoutInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        brush = Brush.horizontalGradient(
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

// ═══════════════════════════════════════════════════════
// Read-Only Classification Slabs List
// ═══════════════════════════════════════════════════════

@Composable
private fun ReadOnlySlabsList(slabs: List<SlabDto>) {
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
                val theme = getSlabThemeByIndex(index)
                val range = if (slab.maxQuantity != null)
                    "${slab.minQuantity.toInt()}-${slab.maxQuantity.toInt()} cs"
                else
                    "${slab.minQuantity.toInt()}+ cs"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.background.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .border(1.dp, theme.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
                        color = theme.text
                    )
                }

                if (index < sorted.size - 1) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// Slab Theme Colors
// ═══════════════════════════════════════════════════════

private data class SlabTheme(
    val background: Color,
    val border: Color,
    val iconBg: Color,
    val iconTint: Color,
    val text: Color,
    val icon: ImageVector
)

private fun getSlabThemeByIndex(index: Int): SlabTheme {
    return when (index) {
        0 -> SlabTheme( // Platinum / Top tier
            background = Color(0xFFF5F3FF),
            border = Color(0xFFDDD6FE),
            iconBg = Color(0xFFDDD6FE),
            iconTint = Color(0xFF7C3AED),
            text = Color(0xFF5B21B6),
            icon = Icons.Default.Diamond
        )
        1 -> SlabTheme( // Diamond / 2nd tier
            background = Color(0xFFF0F9FF),
            border = Color(0xFFBAE6FD),
            iconBg = Color(0xFFBAE6FD),
            iconTint = Color(0xFF0284C7),
            text = Color(0xFF075985),
            icon = Icons.Default.Diamond
        )
        2 -> SlabTheme( // Gold / 3rd tier
            background = Color(0xFFFFFBEB),
            border = Color(0xFFFEF3C7),
            iconBg = Color(0xFFFEF3C7),
            iconTint = Color(0xFFD97706),
            text = Color(0xFF92400E),
            icon = Icons.Default.WorkspacePremium
        )
        else -> SlabTheme( // Silver / 4th+ tier
            background = Color(0xFFF9FAFB),
            border = Color(0xFFE5E7EB),
            iconBg = Color(0xFFE5E7EB),
            iconTint = Color(0xFF6B7280),
            text = Color(0xFF374151),
            icon = Icons.Default.MilitaryTech
        )
    }
}

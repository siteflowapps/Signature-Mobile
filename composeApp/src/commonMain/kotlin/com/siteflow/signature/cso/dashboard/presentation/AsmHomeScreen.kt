package com.siteflow.signature.cso.dashboard.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.cso.dashboard.domain.AsmHomeAction
import com.siteflow.signature.cso.dashboard.domain.AsmHomeEvent
import com.siteflow.signature.cso.dashboard.domain.AsmHomeViewModel
import com.siteflow.signature.cso.onboarding.data.dto.SlabDto
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.outlet.dashboard.data.DashboardData
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

/**
 * ASM Home Dashboard Screen.
 * Shows key stats (ASEs, outlets breakdown, invoices) + classification slab data.
 */
@Composable
fun AsmHomeScreen(
    onNavigateToOutlets: (filter: String) -> Unit = {},
    onNavigateToInvoices: (filter: String) -> Unit = {},
    onNavigateToMyAses: () -> Unit = {},
    onCoolerRequests: () -> Unit = {},
    onBrandingRequests: () -> Unit = {},
    viewModel: AsmHomeViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(AsmHomeAction.LoadDashboard)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                AsmHomeEvent.NavigateToMyAses -> onNavigateToMyAses()
                AsmHomeEvent.NavigateToOutletsAll -> onNavigateToOutlets("All")
                AsmHomeEvent.NavigateToOutletsActive -> onNavigateToOutlets("Active")
                AsmHomeEvent.NavigateToOutletsInProgress -> onNavigateToOutlets("In Progress")
                AsmHomeEvent.NavigateToOutletsSuspended -> onNavigateToOutlets("Suspended")
                AsmHomeEvent.NavigateToOutletsAsmPending -> onNavigateToOutlets("ASM Pending")
                AsmHomeEvent.NavigateToInvoicesPending -> onNavigateToInvoices("Pending")
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

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AppColors.BlueGradientStart,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.5.dp
                )
            }
        } else {
            val dashboard = state.dashboard

            // ── Overview Hero Card (compact summary) ──
            AsmOverviewHeroCard(dashboard)

            Spacer(Modifier.height(16.dp))

            // ── Quick Stats Grid ──
            AsmQuickStatsGrid(
                dashboard = dashboard,
                onTotalAses = { viewModel.onAction(AsmHomeAction.TotalAsesClicked) },
                onTotalOutlets = { viewModel.onAction(AsmHomeAction.TotalOutletsClicked) },
                onActiveOutlets = { viewModel.onAction(AsmHomeAction.ActiveOutletsClicked) },
                onInProgress = { viewModel.onAction(AsmHomeAction.InProgressOutletsClicked) },
                onAsmPending = { viewModel.onAction(AsmHomeAction.AsmPendingOutletsClicked) },
                onPendingInvoices = { viewModel.onAction(AsmHomeAction.PendingInvoicesClicked) }
            )

            Spacer(Modifier.height(16.dp))

            // ── Asset request approval entries (ASM L2) ──
            ApprovalEntryCard(
                title = "Cooler Requests",
                subtitle = "Review cooler requests awaiting L2 approval",
                onClick = onCoolerRequests
            )
            Spacer(Modifier.height(12.dp))
            ApprovalEntryCard(
                title = "Branding Requests",
                subtitle = "Review branding requests awaiting L2 approval",
                onClick = onBrandingRequests
            )

            Spacer(Modifier.height(24.dp))

            // ── Slab Data Section ──
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
                AsmSlabConfigSection(slabs = state.slabs)
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ═══════════════════════════════════════════════════════
// ASM Overview Hero Card — Manager-level summary
// ═══════════════════════════════════════════════════════

@Composable
private fun AsmOverviewHeroCard(dashboard: DashboardData?) {
    val totalAses = dashboard?.totalAse ?: 0
    val totalOutlets = dashboard?.totalOutlets ?: 0
    val active = dashboard?.activeOutlets ?: 0
    val inProgress = dashboard?.inProgressOutlets ?: 0
    val pending = dashboard?.asmPendingOutlets ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1E40AF), Color(0xFF3B82F6))
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column {
                // Title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manager Overview",
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.14f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SupervisorAccount,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Top-level pills: ASEs + Total Outlets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsmHeroPill(
                        value = "$totalAses",
                        label = "ASEs",
                        accent = Color(0xFF93C5FD),
                        modifier = Modifier.weight(1f)
                    )
                    AsmHeroPill(
                        value = "$totalOutlets",
                        label = "Outlets",
                        accent = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    AsmHeroPill(
                        value = "$active",
                        label = "Active",
                        accent = Color(0xFF4ADE80),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Secondary pills: In Progress, Pending
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsmHeroPill(
                        value = "$inProgress",
                        label = "In Progress",
                        accent = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f)
                    )
                    AsmHeroPill(
                        value = "$pending",
                        label = "Pending",
                        accent = Color(0xFFFB923C),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AsmHeroPill(
    value: String,
    label: String,
    accent: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = AppTypography.TitleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            color = accent
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            color = Color.White.copy(alpha = 0.85f),
            maxLines = 1
        )
    }
}

// ═══════════════════════════════════════════════════════
// Attention Banner
// ═══════════════════════════════════════════════════════

@Composable
private fun AsmAttentionBanner(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFBEB), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "$count outlet${if (count != 1) "s" else ""} need${if (count == 1) "s" else ""} your attention",
            style = AppTypography.BodyPrimary.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF92400E),
            modifier = Modifier.weight(1f)
        )
    }
}

// ═══════════════════════════════════════════════════════
// Quick Stats Grid — 7 stat tiles (4 + 3 layout)
// ═══════════════════════════════════════════════════════

@Composable
private fun AsmQuickStatsGrid(
    dashboard: DashboardData?,
    onTotalAses: () -> Unit,
    onTotalOutlets: () -> Unit,
    onActiveOutlets: () -> Unit,
    onInProgress: () -> Unit,
    onAsmPending: () -> Unit,
    onPendingInvoices: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Row 1: Team + Total Outlets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AsmStatTile(
                value = "${dashboard?.totalAse ?: 0}",
                label = "My ASEs",
                icon = Icons.Filled.Group,
                iconBg = Color(0xFFEFF6FF),
                iconTint = Color(0xFF2563EB),
                onClick = onTotalAses,
                modifier = Modifier.weight(1f)
            )
            AsmStatTile(
                value = "${dashboard?.totalOutlets ?: 0}",
                label = "Total Outlets",
                icon = Icons.Filled.Storefront,
                iconBg = Color(0xFFF0F9FF),
                iconTint = Color(0xFF0284C7),
                onClick = onTotalOutlets,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Active + In Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AsmStatTile(
                value = "${dashboard?.activeOutlets ?: 0}",
                label = "Active",
                icon = Icons.Filled.CheckCircle,
                iconBg = Color(0xFFF0FDF4),
                iconTint = Color(0xFF16A34A),
                onClick = onActiveOutlets,
                modifier = Modifier.weight(1f)
            )
            AsmStatTile(
                value = "${dashboard?.inProgressOutlets ?: 0}",
                label = "In Progress",
                icon = Icons.Filled.Pending,
                iconBg = Color(0xFFFFF7ED),
                iconTint = Color(0xFFEA580C),
                onClick = onInProgress,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: ASM Pending + Pending Invoices
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AsmStatTile(
                value = "${dashboard?.asmPendingOutlets ?: 0}",
                label = "ASM Pending",
                icon = Icons.Filled.HourglassTop,
                iconBg = Color(0xFFF5F3FF),
                iconTint = Color(0xFF7C3AED),
                onClick = onAsmPending,
                modifier = Modifier.weight(1f)
            )
            AsmStatTile(
                value = "${dashboard?.submittedInvoices ?: 0}",
                label = "Pending Invoices",
                icon = Icons.Filled.Receipt,
                iconBg = Color(0xFFFFFBEB),
                iconTint = Color(0xFFD97706),
                onClick = onPendingInvoices,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AsmStatTile(
    value: String,
    label: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Navigate",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = value,
                style = AppTypography.TitleMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(1.dp))

            Text(
                text = label,
                style = AppTypography.Caption.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF6B7280),
                maxLines = 1
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// Classification Slab Config Section (shared design with ASE)
// ═══════════════════════════════════════════════════════

@Composable
private fun AsmSlabConfigSection(slabs: List<SlabDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
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

            Spacer(Modifier.height(14.dp))

            val sorted = slabs.sortedByDescending { it.minQuantity }
            sorted.forEachIndexed { index, slab ->
                val theme = getAsmSlabTheme(index)
                val range = if (slab.maxQuantity != null)
                    "${slab.minQuantity.toInt()}-${slab.maxQuantity.toInt()} cs"
                else
                    "${slab.minQuantity.toInt()}+ cs"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.bg.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .border(1.dp, theme.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(theme.iconBg.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = theme.icon,
                            contentDescription = null,
                            tint = theme.iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = slab.classification,
                            style = AppTypography.TitleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = range,
                            style = AppTypography.Caption.copy(fontSize = 12.sp),
                            color = Color(0xFF6B7280)
                        )
                    }

                    val payoutText = if (slab.percentage != null) {
                        "${slab.percentage.toInt()}%"
                    } else if (slab.ratePerCase != null) {
                        "₹${slab.ratePerCase.toInt()}/cs"
                    } else {
                        "0%"
                    }
                    Box(
                        modifier = Modifier
                            .background(theme.bg.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = payoutText,
                            style = AppTypography.Caption.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = theme.text
                        )
                    }
                }

                if (index < sorted.size - 1) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ── Slab Theme ──

private data class AsmSlabTheme(
    val bg: Color,
    val border: Color,
    val iconBg: Color,
    val iconTint: Color,
    val text: Color,
    val icon: ImageVector
)

private fun getAsmSlabTheme(index: Int): AsmSlabTheme {
    return when (index) {
        0 -> AsmSlabTheme(
            bg = Color(0xFFF5F3FF), border = Color(0xFFDDD6FE),
            iconBg = Color(0xFFDDD6FE), iconTint = Color(0xFF7C3AED),
            text = Color(0xFF5B21B6), icon = Icons.Default.Diamond
        )
        1 -> AsmSlabTheme(
            bg = Color(0xFFF0F9FF), border = Color(0xFFBAE6FD),
            iconBg = Color(0xFFBAE6FD), iconTint = Color(0xFF0284C7),
            text = Color(0xFF075985), icon = Icons.Default.Diamond
        )
        2 -> AsmSlabTheme(
            bg = Color(0xFFFFFBEB), border = Color(0xFFFEF3C7),
            iconBg = Color(0xFFFEF3C7), iconTint = Color(0xFFD97706),
            text = Color(0xFF92400E), icon = Icons.Default.WorkspacePremium
        )
        else -> AsmSlabTheme(
            bg = Color(0xFFF9FAFB), border = Color(0xFFE5E7EB),
            iconBg = Color(0xFFE5E7EB), iconTint = Color(0xFF6B7280),
            text = Color(0xFF374151), icon = Icons.Default.MilitaryTech
        )
    }
}

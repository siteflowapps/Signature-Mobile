package com.siteflow.signature.asset.approvals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.asset.approvals.domain.AssetApprovalAction
import com.siteflow.signature.asset.approvals.domain.AssetApprovalViewModel
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.cso.onboarding.data.dto.AssetRequestItemDto
import com.siteflow.signature.cso.onboarding.data.dto.CoolerSizeOption
import org.koin.compose.koinInject

/**
 * Finder for asset requests (kind = COOLER | MARKETING) awaiting the current
 * role's action (ASE→REQUESTED, ASM→ASE_APPROVED). Tapping a request opens that
 * outlet's detail, where the actual Approve/Reject lives (the asset is part of
 * the outlet).
 */
@Composable
fun AssetRequestQueueScreen(
    kind: String,                       // "COOLER" | "MARKETING"
    onOpenOutlet: (outletId: String, outletName: String) -> Unit = { _, _ -> },
    viewModel: AssetApprovalViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(kind) { viewModel.onAction(AssetApprovalAction.Load(kind)) }

    val assetLabel = if (kind == "COOLER") "cooler" else "branding"
    val pills = remember(kind) { assetStatusPills(kind) }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.ScreenBackground)) {
        // ── Status pills ──
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pills, key = { it.label }) { pill ->
                val selected = state.selectedStatus == pill.status
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.onAction(AssetApprovalAction.SelectStatus(pill.status)) },
                    label = { Text(pill.label, style = AppTypography.Caption) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.BlueGradientStart,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    color = AppColors.BlueGradientStart,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.align(Alignment.Center).size(28.dp)
                )

                state.requests.isEmpty() -> {
                    val pillLabel = pills.firstOrNull { it.status == state.selectedStatus }?.label
                    Text(
                        text = "No $assetLabel requests" + (pillLabel?.let { " · $it" } ?: ""),
                        style = AppTypography.BodyPrimary,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.requests, key = { it.id }) { req ->
                        AssetRequestRow(req, kind) {
                            onOpenOutlet(req.outletId ?: "", req.outletName ?: "")
                        }
                    }
                }
            }
        }
    }
}

/** One status pill on the finder. status = null → All stages. */
data class AssetStatusPill(val label: String, val status: String?)

/** Lifecycle status pills for the approval finder, kind-aware. */
fun assetStatusPills(kind: String): List<AssetStatusPill> = buildList {
    add(AssetStatusPill("Requested", "REQUESTED"))
    add(AssetStatusPill("L1 Approved", "ASE_APPROVED"))
    add(AssetStatusPill("L2 Approved", "ASM_APPROVED"))
    if (kind == "MARKETING") add(AssetStatusPill("Marketing Approved", "MARKETING_APPROVED"))
    add(AssetStatusPill("Executed", "EXECUTED"))
    add(AssetStatusPill("Compliant", "COMPLIANT"))
    add(AssetStatusPill("Rejected", "REJECTED"))
}

@Composable
private fun AssetRequestRow(req: AssetRequestItemDto, kind: String, onClick: () -> Unit) {
    val detail = if (kind == "COOLER") {
        val sizeLabel = CoolerSizeOption.entries.firstOrNull { it.backendValue == req.coolerSize }?.label
            ?: req.coolerSize ?: "—"
        "Cooler • $sizeLabel • Qty ${req.quantity ?: 1}"
    } else {
        val items = if (req.items.isNotEmpty()) req.items.sumOf { it.quantity ?: 1 } else (req.quantity ?: 1)
        "Branding • ${req.items.size.coerceAtLeast(1)} item(s) • Qty $items"
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = req.outletName ?: "Outlet",
                    style = AppTypography.TitleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    color = AppColors.TextPrimary
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    text = detail,
                    style = AppTypography.BodySecondary,
                    color = AppColors.TextSecondary
                )
                req.raisedByName?.let {
                    Text("Raised by $it", style = AppTypography.Caption, color = AppColors.TextTertiary)
                }
            }
            Text("→", style = AppTypography.TitleLarge.copy(fontSize = 20.sp), color = AppColors.TextTertiary)
        }
    }
}

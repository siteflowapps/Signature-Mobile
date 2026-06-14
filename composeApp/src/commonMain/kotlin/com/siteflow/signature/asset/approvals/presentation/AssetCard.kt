package com.siteflow.signature.asset.approvals.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.asset.approvals.domain.OutletAssetAction
import com.siteflow.signature.asset.approvals.domain.OutletAssetViewModel
import com.siteflow.signature.core.domain.RoleManager
import com.siteflow.signature.core.domain.UserRole
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.dashboard.data.OutletStatus
import com.siteflow.signature.cso.onboarding.data.dto.AssetRequestItemDto
import com.siteflow.signature.cso.onboarding.data.dto.CoolerSizeOption
import org.koin.compose.koinInject

/**
 * Role-aware asset section for one kind (COOLER or MARKETING) on the outlet
 * detail. Shows status + the action the current role can take:
 *  - CSO: Request (none yet) / Upload Compliance (installed)
 *  - ASE: Approve/Reject when REQUESTED ; ASM: when ASE_APPROVED
 * Renders nothing when there's no request and the role can't raise one.
 */
@Composable
fun AssetCard(
    outlet: OutletItem,
    kind: String,                       // "COOLER" | "MARKETING"
    onRequest: () -> Unit = {},
    onUploadCompliance: () -> Unit = {},
    viewModel: OutletAssetViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var showReject by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }

    LaunchedEffect(outlet.id, kind) { viewModel.onAction(OutletAssetAction.Load(outlet.id, kind)) }

    val title = if (kind == "COOLER") "Cooler" else "Branding"
    val isCso = RoleManager.currentRole.value == UserRole.CSO
    val outletActive = outlet.status == OutletStatus.ASM_APPROVED || outlet.status == OutletStatus.ONBOARDED
    val req = state.request

    // No request yet → only the CSO (on an active outlet) sees a "Request" action.
    if (req == null) {
        if (isCso && outletActive) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$title request",
                        style = AppTypography.TitleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        color = AppColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onRequest,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                    ) { Text("Request", style = AppTypography.Button, color = Color.White) }
                }
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$title Request",
                style = AppTypography.TitleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = assetDetailLine(kind, req),
                style = AppTypography.BodySecondary,
                color = AppColors.TextSecondary
            )
            Text(
                text = stageLabel(req.status),
                style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                color = AppColors.BlueGradientStart
            )
            req.raisedByName?.let {
                Text("Raised by $it", style = AppTypography.Caption, color = AppColors.TextTertiary)
            }

            if (state.canDecide) {
                Spacer(Modifier.size(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { showReject = true; reason = "" },
                        enabled = !state.isProcessing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Danger),
                        modifier = Modifier.weight(1f)
                    ) { Text("Reject", style = AppTypography.Button) }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.onAction(OutletAssetAction.Approve(req.id)) },
                        enabled = !state.isProcessing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.isProcessing) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        } else Text("Approve", style = AppTypography.Button, color = Color.White)
                    }
                }
            } else if (isCso && state.needsCompliance) {
                Spacer(Modifier.size(12.dp))
                Button(
                    onClick = onUploadCompliance,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Upload Compliance", style = AppTypography.Button, color = Color.White) }
            }
        }
    }

    if (showReject) {
        AlertDialog(
            onDismissRequest = { showReject = false },
            title = { Text("Reject $title request", style = AppTypography.TitleMedium.copy(fontWeight = FontWeight.SemiBold)) },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = reason.isNotBlank() && !state.isProcessing,
                    onClick = {
                        viewModel.onAction(OutletAssetAction.Reject(req.id, reason.trim()))
                        showReject = false
                    }
                ) { Text("Reject", color = AppColors.Danger) }
            },
            dismissButton = { TextButton(onClick = { showReject = false }) { Text("Cancel") } }
        )
    }
}

private fun assetDetailLine(kind: String, req: AssetRequestItemDto): String =
    if (kind == "COOLER") {
        val size = CoolerSizeOption.entries.firstOrNull { it.backendValue == req.coolerSize }?.label
            ?: req.coolerSize ?: "—"
        "$size • Qty ${req.quantity ?: 1}"
    } else {
        if (req.items.isNotEmpty()) {
            req.items.joinToString(", ") { "${marketingTypeLabel(it.assetType)} ×${it.quantity ?: 1}" }
        } else "Qty ${req.quantity ?: 1}"
    }

private fun marketingTypeLabel(t: String?): String = when (t) {
    "NON_LIT_BOARD" -> "Non-lit board"
    "GLOW_SIGN_BOARD" -> "Glow sign board"
    "ACP_BOARD" -> "ACP board"
    "BRANDED_TRAYS" -> "Branded trays"
    "WALL_BRANDING" -> "Wall branding"
    "END_CAP" -> "End cap"
    else -> t ?: "Item"
}

private fun stageLabel(status: String?): String = when (status) {
    "REQUESTED" -> "Pending L1 approval"
    "ASE_APPROVED" -> "Pending L2 approval"
    "ASM_APPROVED" -> "Approved — awaiting marketing/install"
    "MARKETING_APPROVED" -> "Approved — awaiting install"
    "EXECUTED" -> "Installed — awaiting compliance"
    "COMPLIANCE_SUBMITTED" -> "Compliance under review"
    "COMPLIANT" -> "Compliant"
    "NON_COMPLIANT" -> "Non-compliant"
    "COMPLIANCE_OVERDUE" -> "Compliance overdue"
    else -> status ?: "—"
}

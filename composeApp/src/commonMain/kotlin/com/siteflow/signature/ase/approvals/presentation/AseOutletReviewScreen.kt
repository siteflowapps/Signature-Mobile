package com.siteflow.signature.ase.approvals.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.siteflow.signature.ase.approvals.domain.AseApprovalAction
import com.siteflow.signature.ase.approvals.domain.AseApprovalEvent
import com.siteflow.signature.ase.approvals.domain.AseApprovalViewModel
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.cso.dashboard.presentation.DistributorDetailsCard
import com.siteflow.signature.cso.dashboard.presentation.OnboardingPhotosCard
import com.siteflow.signature.cso.dashboard.presentation.OutletDetailsCard
import com.siteflow.signature.cso.dashboard.presentation.OutletInfoCard
import com.siteflow.signature.cso.dashboard.presentation.PaymentModeCard
import com.siteflow.signature.cso.dashboard.presentation.SignaturePipelineCard
import org.koin.compose.koinInject

/**
 * ASE Level-1 review of a single outlet. Loads the outlet by id and reuses the
 * shared detail cards. Approve / Reject (wired to `/decision`) are shown only
 * when the outlet is awaiting L1 (ASE_PENDING); otherwise it's read-only.
 */
@Composable
fun AseOutletReviewScreen(
    outletId: String,
    onDone: () -> Unit,
    viewModel: AseApprovalViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    LaunchedEffect(outletId) {
        viewModel.onAction(AseApprovalAction.Load(outletId))
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is AseApprovalEvent.DecisionComplete) onDone()
        }
    }

    val outlet = state.outlet

    when {
        state.isLoading || outlet == null -> Box(
            modifier = Modifier.fillMaxSize().background(AppColors.ScreenBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = AppColors.BlueGradientStart,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(28.dp)
            )
        }

        else -> Column(modifier = Modifier.fillMaxSize().background(AppColors.ScreenBackground)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutletInfoCard(outlet)
                SignaturePipelineCard(outlet)
                OutletDetailsCard(outlet)
                DistributorDetailsCard(outlet)
                PaymentModeCard(outlet)
                OnboardingPhotosCard(outlet, onImageClick = {})
            }

            // Approve / Reject only when the outlet is awaiting L1 review.
            if (state.canDecide) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        enabled = !state.isProcessing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Danger),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reject", style = AppTypography.Button)
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.onAction(AseApprovalAction.Approve(outletId)) },
                        enabled = !state.isProcessing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (state.isProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text("Approve", style = AppTypography.Button, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = {
                Text(
                    "Reject outlet",
                    style = AppTypography.TitleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text("Reason for rejection") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = rejectReason.isNotBlank() && !state.isProcessing,
                    onClick = {
                        viewModel.onAction(AseApprovalAction.Reject(outletId, rejectReason.trim()))
                        showRejectDialog = false
                    }
                ) {
                    Text("Reject", color = AppColors.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("Cancel") }
            }
        )
    }
}

package com.siteflow.signature.asm.invoices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.cso.invoices.data.InvoiceStatus
import com.siteflow.signature.cso.invoices.presentation.InvoiceDetailContent
import com.siteflow.signature.asm.invoices.domain.AsmInvoiceAction
import com.siteflow.signature.asm.invoices.domain.AsmInvoiceEvent
import com.siteflow.signature.asm.invoices.domain.AsmInvoiceViewModel
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.shared.data.PayoutApi
import com.siteflow.signature.shared.data.PayoutCalculationDto
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import org.koin.compose.koinInject

/**
 * ASM Invoice Detail Screen.
 * Shows shared invoice details + Approval Timeline + Approve/Reject actions.
 * Consistent with the ASE InvoiceDetailScreen layout.
 */
@Composable
fun AsmInvoiceDetailScreen(
    invoiceId: String,
    onBack: () -> Unit,
    viewModel: AsmInvoiceViewModel = koinInject()
) {
    // Load invoice detail
    LaunchedEffect(invoiceId) {
        viewModel.onAction(AsmInvoiceAction.LoadInvoiceDetail(invoiceId))
    }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AsmInvoiceEvent.NavigateBack -> onBack()
                is AsmInvoiceEvent.ShowMessage -> { /* Toasts handled via GlobalToastHandler */ }
            }
        }
    }

    val state by viewModel.state.collectAsState()
    val invoice = state.selectedInvoice

    // ── Fetch payout calculation ──
    val payoutApi: PayoutApi = koinInject()
    var payoutData by remember { mutableStateOf<PayoutCalculationDto?>(null) }
    var isPayoutLoading by remember { mutableStateOf(true) }

    LaunchedEffect(invoiceId) {
        isPayoutLoading = true
        val result = payoutApi.calculatePayout(invoiceId)
        when (result) {
            is com.siteflow.signature.core.data.networking.result.NetworkResult.Success -> {
                payoutData = result.data.data
            }
            is com.siteflow.signature.core.data.networking.result.NetworkResult.Error -> { /* silently ignore */ }
        }
        isPayoutLoading = false
    }

    if (invoice == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppColors.BlueGradientStart)
        }
        return
    }

    val isPendingReview = invoice.status == InvoiceStatus.ASE_APPROVED
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var remarks by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        InvoiceDetailContent(
            invoice = invoice,
            showPeriodSection = false,
            payoutData = payoutData,
            isPayoutLoading = isPayoutLoading,
            modifier = Modifier.weight(1f)
        )

        // ── Fixed Bottom Action Bar (only for ASE_APPROVED invoices) ──
        if (isPendingReview) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reject
                    OutlinedButton(
                        onClick = {
                            remarks = ""
                            showRejectDialog = true
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.Danger
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, AppColors.Danger
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = AppColors.Danger,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Reject",
                            style = AppTypography.Button,
                            color = AppColors.Danger
                        )
                    }

                    // Approve
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        AppColors.BlueGradientStart,
                                        AppColors.BlueGradientEnd
                                    )
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                remarks = ""
                                showApproveDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Approve",
                                style = AppTypography.Button,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Approve Dialog ──
    if (showApproveDialog) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .imePadding()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        showApproveDialog = false
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Approve Invoice",
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF111827)
                    )

                    Text(
                        text = "Add remarks for this approval:",
                        style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                        color = AppColors.TextSecondary
                    )

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        placeholder = {
                            Text(
                                "e.g. Approved by area manager",
                                style = AppTypography.Caption.copy(fontSize = 13.sp),
                                color = AppColors.TextTertiary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.BlueGradientStart,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = AppColors.BlueGradientStart
                        ),
                        minLines = 2,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showApproveDialog = false }) {
                            Text("Cancel", color = AppColors.TextSecondary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.onAction(AsmInvoiceAction.ApproveInvoice(invoice.id, remarks))
                                showApproveDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.BlueGradientStart
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Approve", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // ── Reject Dialog ──
    if (showRejectDialog) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .imePadding()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        showRejectDialog = false
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Reject Invoice",
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF111827)
                    )

                    Text(
                        text = "Please provide a reason for rejecting this invoice:",
                        style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                        color = AppColors.TextSecondary
                    )

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        placeholder = {
                            Text(
                                "Reason for rejection...",
                                style = AppTypography.Caption.copy(fontSize = 13.sp),
                                color = AppColors.TextTertiary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Danger,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            cursorColor = AppColors.Danger
                        ),
                        minLines = 2,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showRejectDialog = false }) {
                            Text("Cancel", color = AppColors.TextSecondary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.onAction(AsmInvoiceAction.RejectInvoice(invoice.id, remarks))
                                showRejectDialog = false
                            },
                            enabled = remarks.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Danger
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reject", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


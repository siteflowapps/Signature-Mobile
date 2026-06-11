package com.siteflow.cdo.ase.invoices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.ase.invoices.data.InvoiceItem
import com.siteflow.cdo.ase.invoices.data.InvoiceStatus
import com.siteflow.cdo.ase.invoices.data.SlabQualification
import com.siteflow.cdo.ase.invoices.domain.InvoiceAction
import com.siteflow.cdo.ase.invoices.domain.InvoiceEvent
import com.siteflow.cdo.ase.invoices.domain.InvoiceViewModel
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import com.siteflow.cdo.shared.data.PayoutApi
import com.siteflow.cdo.shared.data.PayoutCalculationDto
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Invoice Detail Screen with swipeable review queue.
 *
 * When there are multiple pending invoices for the same outlet+period,
 * a HorizontalPager enables swiping between them. After approve/reject,
 * auto-advances to the next pending invoice or navigates back if all done.
 */
@Composable
fun InvoiceDetailScreen(
    invoiceId: String,
    onBack: () -> Unit,
    viewModel: InvoiceViewModel = koinInject()
) {
    LaunchedEffect(invoiceId) {
        viewModel.onAction(InvoiceAction.LoadInvoiceDetail(invoiceId))
    }

    // ── Fetch payout calculation ──
    val payoutApi: PayoutApi = koinInject()
    var payoutData by remember { mutableStateOf<PayoutCalculationDto?>(null) }
    var isPayoutLoading by remember { mutableStateOf(true) }

    LaunchedEffect(invoiceId) {
        isPayoutLoading = true
        val result = payoutApi.calculatePayout(invoiceId)
        when (result) {
            is com.siteflow.cdo.core.data.networking.result.NetworkResult.Success -> {
                payoutData = result.data.data
            }
            is com.siteflow.cdo.core.data.networking.result.NetworkResult.Error -> { /* silently ignore */ }
        }
        isPayoutLoading = false
    }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is InvoiceEvent.NavigateBack -> onBack()
                is InvoiceEvent.ShowToast -> { /* TODO: Show snackbar */ }
            }
        }
    }

    val state by viewModel.state.collectAsState()
    val invoice = state.selectedInvoice
    val periodInvoices = state.periodInvoices

    if (invoice == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppColors.BlueGradientStart)
        }
        return
    }

    // Determine pager invoices: all pending for the same outlet+period
    val pendingPeriodInvoices = periodInvoices.filter { it.status == InvoiceStatus.PENDING }
    val hasPagerMode = pendingPeriodInvoices.size > 1 && invoice.status == InvoiceStatus.PENDING

    if (hasPagerMode) {
        SwipeableReviewScreen(
            invoices = pendingPeriodInvoices,
            initialInvoiceId = invoiceId,
            viewModel = viewModel,
            onBack = onBack,
            payoutData = payoutData,
            isPayoutLoading = isPayoutLoading
        )
    } else {
        SingleInvoiceScreen(
            invoice = invoice,
            periodInvoices = periodInvoices,
            viewModel = viewModel,
            payoutData = payoutData,
            isPayoutLoading = isPayoutLoading
        )
    }
}

// ═══════════════════════════════════════════════════════════
// Swipeable multi-invoice review
// ═══════════════════════════════════════════════════════════

@Composable
private fun SwipeableReviewScreen(
    invoices: List<InvoiceItem>,
    initialInvoiceId: String,
    viewModel: InvoiceViewModel,
    onBack: () -> Unit,
    payoutData: PayoutCalculationDto?,
    isPayoutLoading: Boolean
) {
    val scope = rememberCoroutineScope()
    val initialPage = invoices.indexOfFirst { it.id == initialInvoiceId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { invoices.size }

    // Track reviewed pages so we can auto-advance
    var reviewedPages by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // ── Page indicator bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Invoice ${pagerState.currentPage + 1} of ${invoices.size}",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF111827)
            )

            // Dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                invoices.forEachIndexed { index, _ ->
                    val isActive = index == pagerState.currentPage
                    val isReviewed = index in reviewedPages
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 10.dp else 8.dp)
                            .background(
                                when {
                                    isReviewed -> AppColors.Success
                                    isActive -> AppColors.BlueGradientStart
                                    else -> Color(0xFFD1D5DB)
                                },
                                CircleShape
                            )
                    )
                }
            }

            Text(
                text = "Swipe to compare →",
                style = AppTypography.Caption.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.TextTertiary
            )
        }

        // ── Horizontal Pager ──
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            beyondViewportPageCount = 1
        ) { page ->
            val pageInvoice = invoices[page]
            InvoiceDetailContent(
                invoice = pageInvoice,
                showPeriodSection = false,
                payoutData = payoutData,
                isPayoutLoading = isPayoutLoading,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Fixed Bottom Action Bar ──
        val currentInvoice = invoices[pagerState.currentPage]
        val currentAmount = currentInvoice.invoiceAmount
        var reviewNote by remember(pagerState.currentPage) { mutableStateOf("") }
        var showApproveDialog by remember { mutableStateOf(false) }
        var showRejectDialog by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = reviewNote,
                    onValueChange = { reviewNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Add a review note (optional)...",
                            style = AppTypography.Caption.copy(fontSize = 13.sp),
                            color = AppColors.TextTertiary
                        )
                    },
                    textStyle = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.BlueGradientStart,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        cursorColor = AppColors.BlueGradientStart
                    ),
                    minLines = 1,
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
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
                            text = "Reject $currentAmount",
                            style = AppTypography.Button,
                            color = AppColors.Danger
                        )
                    }

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
                                text = "Approve $currentAmount",
                                style = AppTypography.Button,
                                color = Color.White
                            )
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
                            value = reviewNote,
                            onValueChange = { reviewNote = it },
                            placeholder = {
                                Text(
                                    "e.g. Verified and approved",
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
                                    val page = pagerState.currentPage
                                    reviewedPages = reviewedPages + page
                                    viewModel.onAction(InvoiceAction.ApproveInvoice(currentInvoice.id, reviewNote))
                                    showApproveDialog = false
                                    
                                    val nextPage = findNextUnreviewedPage(page, invoices.size, reviewedPages + page)
                                    if (nextPage != null) {
                                        scope.launch { pagerState.animateScrollToPage(nextPage) }
                                    }
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
                            value = reviewNote,
                            onValueChange = { reviewNote = it },
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
                                    val page = pagerState.currentPage
                                    reviewedPages = reviewedPages + page
                                    viewModel.onAction(InvoiceAction.RejectInvoice(currentInvoice.id, reviewNote))
                                    showRejectDialog = false
                                    
                                    val nextPage = findNextUnreviewedPage(page, invoices.size, reviewedPages + page)
                                    if (nextPage != null) {
                                        scope.launch { pagerState.animateScrollToPage(nextPage) }
                                    }
                                },
                                enabled = reviewNote.isNotBlank(),
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
}

/** Find the next page that hasn't been reviewed. */
private fun findNextUnreviewedPage(current: Int, total: Int, reviewed: Set<Int>): Int? {
    // First look forward
    for (i in current + 1 until total) {
        if (i !in reviewed) return i
    }
    // Then look backward
    for (i in 0 until current) {
        if (i !in reviewed) return i
    }
    return null // All reviewed
}

// ═══════════════════════════════════════════════════════════
// Single invoice view (non-pager mode)
// ═══════════════════════════════════════════════════════════

@Composable
private fun SingleInvoiceScreen(
    invoice: InvoiceItem,
    periodInvoices: List<InvoiceItem>,
    viewModel: InvoiceViewModel,
    payoutData: PayoutCalculationDto?,
    isPayoutLoading: Boolean
) {
    val isPendingReview = invoice.status == InvoiceStatus.SUBMITTED
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
            showPeriodSection = periodInvoices.size > 1,
            periodInvoices = periodInvoices,
            payoutData = payoutData,
            isPayoutLoading = isPayoutLoading,
            modifier = Modifier.weight(1f)
        )

        // Fixed Bottom Action Bar (Pending only)
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
                                text = "Verify & Approve",
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
                                "e.g. Verified and approved",
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
                                viewModel.onAction(InvoiceAction.ApproveInvoice(invoice.id, remarks))
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
                                viewModel.onAction(InvoiceAction.RejectInvoice(invoice.id, remarks))
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




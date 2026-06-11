package com.siteflow.retailsync.invoice.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.siteflow.retailsync.core.presentation.design.AppColors
import com.siteflow.retailsync.core.presentation.design.AppTypography
import com.siteflow.retailsync.core.presentation.util.formatCurrency
import com.siteflow.retailsync.invoice.data.dto.InvoiceDto
import com.siteflow.retailsync.invoice.domain.InvoiceListAction
import com.siteflow.retailsync.invoice.domain.InvoiceListEvent
import com.siteflow.retailsync.invoice.domain.InvoiceListViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun InvoiceListScreen(
    onNavigateToScanner: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: InvoiceListViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                InvoiceListEvent.NavigateToScanner -> onNavigateToScanner()
                is InvoiceListEvent.NavigateToDetail -> { /* TODO: detail screen */ }
                InvoiceListEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAction(InvoiceListAction.ScanQr) },
                containerColor = AppColors.BlueGradientStart,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.ScreenBackground)
        ) {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(AppColors.BlueGradientStart, AppColors.BlueGradientEnd)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hello, ${state.userName ?: "User"} 👋",
                                style = AppTypography.BodyPrimary.copy(
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Invoices",
                                style = AppTypography.TitleLarge.copy(
                                    color = Color.White,
                                    fontSize = 28.sp
                                )
                            )
                        }

                        IconButton(
                            onClick = { viewModel.onAction(InvoiceListAction.Logout) }
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val completed = state.invoices.count { it.status?.uppercase() == "COMPLETED" || it.status?.uppercase() == "APPROVED" }
                        val pending = state.invoices.count { it.status?.uppercase() == "PENDING" || it.status?.uppercase() == "SUBMITTED" }
                        val draft = state.invoices.count { it.status?.uppercase() == "DRAFT" || it.status?.uppercase() == "REJECTED" }

                        StatChip("$completed", "Completed", AppColors.greenF4)
                        StatChip("$pending", "Pending", AppColors.orangeEB)
                        StatChip("$draft", "Draft", AppColors.greyF6)
                    }
                }
            }

            // Invoice List
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.BlueGradientStart)
                }
            } else if (state.invoices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AppColors.TextTertiary.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No invoices yet",
                            style = AppTypography.TitleMedium,
                            color = AppColors.TextTertiary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Scan a QR code to create your first invoice",
                            style = AppTypography.BodySecondary,
                            color = AppColors.TextTertiary.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.invoices) { invoice ->
                        InvoiceCard(invoice = invoice, onClick = {
                            viewModel.onAction(InvoiceListAction.ViewDetail(invoice.id))
                        })
                    }
                    item { Spacer(Modifier.height(72.dp)) } // FAB clearance
                }
            }
        }
    }
}

@Composable
private fun StatChip(count: String, label: String, bgColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = count,
                style = AppTypography.TitleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = AppTypography.Caption.copy(color = Color.White.copy(alpha = 0.8f))
            )
        }
    }
}

@Composable
private fun InvoiceCard(invoice: InvoiceDto, onClick: () -> Unit) {
    val statusUpper = invoice.status?.uppercase() ?: ""
    val statusColor = when {
        statusUpper == "COMPLETED" || statusUpper == "APPROVED" -> AppColors.green4A
        statusUpper == "PENDING" || statusUpper == "SUBMITTED" -> AppColors.orange06
        statusUpper == "DRAFT" || statusUpper == "REJECTED" -> AppColors.grey80
        else -> AppColors.TextTertiary
    }
    val statusBgColor = when {
        statusUpper == "COMPLETED" || statusUpper == "APPROVED" -> AppColors.greenE7
        statusUpper == "PENDING" || statusUpper == "SUBMITTED" -> AppColors.orangeD5
        statusUpper == "DRAFT" || statusUpper == "REJECTED" -> AppColors.greyF6
        else -> AppColors.greyF6
    }

    val displayName = invoice.outletName ?: invoice.distributorName ?: "Unknown"
    val displayDate = invoice.invoiceDate ?: invoice.uploadDate ?: "-"
    val displayAmount = invoice.totalAmount ?: 0.0
    val displayItemCount = invoice.items.size

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Outlet avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        AppColors.BlueGradientStart.copy(alpha = 0.1f),
                                        AppColors.BlueGradientEnd.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.take(2).uppercase(),
                            style = AppTypography.Caption.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BlueGradientStart
                            )
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = displayName,
                            style = AppTypography.TitleMedium.copy(fontSize = 15.sp),
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = invoice.invoiceNumber ?: invoice.id,
                            style = AppTypography.Caption,
                            color = AppColors.TextTertiary
                        )
                    }
                }

                // Status chip
                Box(
                    modifier = Modifier
                        .background(statusBgColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = (invoice.status ?: "Unknown").lowercase().replaceFirstChar { it.uppercase() },
                        style = AppTypography.Caption.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Divider))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Date", style = AppTypography.Caption, color = AppColors.TextTertiary)
                    Text(
                        displayDate,
                        style = AppTypography.BodySecondary.copy(fontWeight = FontWeight.Medium),
                        color = AppColors.TextPrimary
                    )
                }
                Column {
                    Text("Items", style = AppTypography.Caption, color = AppColors.TextTertiary)
                    Text(
                        "$displayItemCount",
                        style = AppTypography.BodySecondary.copy(fontWeight = FontWeight.Medium),
                        color = AppColors.TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Amount", style = AppTypography.Caption, color = AppColors.TextTertiary)
                    Text(
                        "₹${displayAmount.formatCurrency()}",
                        style = AppTypography.TitleMedium.copy(fontSize = 15.sp),
                        color = AppColors.BlueGradientStart
                    )
                }
            }
        }
    }
}

package com.siteflow.retailsync.order.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.retailsync.core.presentation.components.CdoButton
import com.siteflow.retailsync.core.presentation.design.AppColors
import com.siteflow.retailsync.core.presentation.util.formatCurrency
import com.siteflow.retailsync.core.presentation.design.AppTypography
import com.siteflow.retailsync.order.data.dto.SkuDto
import com.siteflow.retailsync.order.domain.OrderCreationAction
import com.siteflow.retailsync.order.domain.OrderCreationEvent
import com.siteflow.retailsync.order.domain.OrderCreationViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun OrderCreationScreen(
    outletId: String,
    outletName: String,
    onNavigateToSuccess: (orderId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: OrderCreationViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(outletId, outletName) {
        viewModel.setOutletInfo(outletId, outletName)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OrderCreationEvent.NavigateToSuccess -> onNavigateToSuccess(event.orderId)
                OrderCreationEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(AppColors.BlueGradientStart, AppColors.BlueGradientEnd)
                    )
                )
                .statusBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.onAction(OrderCreationAction.Cancel) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        "Create Invoice",
                        style = AppTypography.TitleMedium.copy(fontSize = 18.sp),
                        color = Color.White
                    )
                }

                // Outlet info card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Store, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(outletName, style = AppTypography.TitleMedium, color = Color.White)
                            Text("ID: $outletId", style = AppTypography.Caption, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }

        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onAction(OrderCreationAction.SearchChanged(it)) },
            placeholder = { Text("Search products...", style = AppTypography.BodySecondary) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = AppColors.TextTertiary) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.BlueGradientStart,
                unfocusedBorderColor = AppColors.Divider,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        // SKU list
        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.BlueGradientStart)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.filteredSkus) { sku ->
                    SkuItemCard(
                        sku = sku,
                        quantity = state.selectedItems[sku.id]?.quantity ?: 0,
                        onAdd = { viewModel.onAction(OrderCreationAction.AddItem(sku.id)) },
                        onRemove = { viewModel.onAction(OrderCreationAction.RemoveItem(sku.id)) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }

        // Bottom bar with total and submit
        if (state.selectedItems.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${state.totalItems} cases",
                            style = AppTypography.BodyPrimary,
                            color = AppColors.TextTertiary
                        )
                        Text(
                            "₹${state.totalAmount.formatCurrency()}",
                            style = AppTypography.TitleMedium.copy(fontSize = 20.sp),
                            color = AppColors.BlueGradientStart
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    CdoButton(
                        text = "Create Invoice",
                        onClick = { viewModel.onAction(OrderCreationAction.Submit) },
                        loading = state.isSubmitting,
                        trailingIcon = null
                    )
                }
            }
        }
    }
}

@Composable
private fun SkuItemCard(
    sku: SkuDto,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppColors.lightBlueFF),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    sku.brand.take(2).uppercase(),
                    style = AppTypography.Caption.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppColors.BlueGradientStart
                    )
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    sku.articleDescription,
                    style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Medium),
                    color = AppColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    Text(
                        "₹${sku.mrpPerCase.formatCurrency(0)}",
                        style = AppTypography.BodySecondary.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.BlueGradientStart
                    )
                    Text(
                        " / case",
                        style = AppTypography.Caption,
                        color = AppColors.TextTertiary
                    )
                }
                Text(
                    "${sku.brand} · ${sku.flavor} · ${sku.ml}ml · ${sku.packForm}",
                    style = AppTypography.Caption,
                    color = AppColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            // Quantity controls
            if (quantity == 0) {
                FilledTonalButton(
                    onClick = onAdd,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AppColors.lightBlueFF,
                        contentColor = AppColors.BlueGradientStart
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Add", style = AppTypography.Caption.copy(fontWeight = FontWeight.SemiBold))
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, null, tint = AppColors.Danger, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        "$quantity",
                        style = AppTypography.TitleMedium,
                        color = AppColors.TextPrimary
                    )
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = AppColors.BlueGradientStart, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

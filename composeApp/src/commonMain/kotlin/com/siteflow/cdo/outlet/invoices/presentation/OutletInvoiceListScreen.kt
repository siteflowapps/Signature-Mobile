package com.siteflow.cdo.outlet.invoices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.siteflow.cdo.ase.invoices.data.InvoiceStatus
import com.siteflow.cdo.ase.invoices.presentation.InvoiceCard
import com.siteflow.cdo.core.presentation.components.CdoFilterChipRow
import com.siteflow.cdo.core.presentation.components.CdoListHeader
import com.siteflow.cdo.core.presentation.components.CdoTextField
import com.siteflow.cdo.core.presentation.components.state.EmptyState
import com.siteflow.cdo.core.presentation.components.state.EndOfListIndicator
import com.siteflow.cdo.core.presentation.components.state.LoadingMoreIndicator
import com.siteflow.cdo.core.presentation.components.state.InvoiceListSkeleton
import com.siteflow.cdo.core.presentation.components.state.ErrorState
import com.siteflow.cdo.core.presentation.components.animation.StaggeredAnimatedItem
import com.siteflow.cdo.outlet.invoices.domain.OutletInvoiceAction
import com.siteflow.cdo.outlet.invoices.domain.OutletInvoiceViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

/**
 * Outlet Invoice List Screen — read-only variant of the ASE Invoice List.
 * Reuses shared components: CdoFilterChipRow, CdoListHeader, EmptyState, InvoiceCard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletInvoiceListScreen(
    onViewInvoiceDetails: (String) -> Unit = {},
    initialFilter: String? = null,
    viewModel: OutletInvoiceViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    val filters = listOf("All", "Pending", "Approved", "Rejected")

    LaunchedEffect(Unit) {
        viewModel.onAction(OutletInvoiceAction.LoadInvoices)
        // Apply initial filter if provided (e.g., from dashboard stat tile)
        if (initialFilter != null && initialFilter in filters) {
            viewModel.onAction(OutletInvoiceAction.FilterSelected(initialFilter))
        }
    }

    // Simulate refresh
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(1200)
            viewModel.onAction(OutletInvoiceAction.LoadInvoices)
            isRefreshing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // Non-scrollable: Search + Filters
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))

            // Search Bar
            CdoTextField(
                value = state.searchQuery,
                placeholder = "Search invoices...",
                onValueChange = { viewModel.onAction(OutletInvoiceAction.SearchQueryChanged(it)) },
                leadingIconVector = Icons.Default.Search
            )

            CdoFilterChipRow(
                filters = filters,
                selectedFilter = state.selectedFilter,
                countForFilter = { filter ->
                    when (filter) {
                        "Pending"  -> state.invoices.count { it.status == InvoiceStatus.SUBMITTED || it.status == InvoiceStatus.ASE_APPROVED || it.status == InvoiceStatus.ASM_APPROVED || it.status == InvoiceStatus.PENDING }
                        "Approved" -> state.invoices.count { it.status == InvoiceStatus.FINANCE_APPROVED || it.status == InvoiceStatus.PAID || it.status == InvoiceStatus.APPROVED }
                        "Rejected" -> state.invoices.count { it.status == InvoiceStatus.REJECTED }
                        else       -> state.totalElements
                    }
                },
                onFilterSelected = { viewModel.onAction(OutletInvoiceAction.FilterSelected(it)) }
            )

            Spacer(Modifier.height(12.dp))

            val headerCount = if (state.totalElements > 0 && state.filteredInvoices.size < state.totalElements && state.selectedFilter == "All")
                "${state.filteredInvoices.size} of ${state.totalElements}"
            else "${state.filteredInvoices.size}"
            CdoListHeader(label = "INVOICES ($headerCount)")

            Spacer(Modifier.height(8.dp))
        }


        // Loading state
        if (state.isLoading && state.invoices.isEmpty()) {
            InvoiceListSkeleton()
        } else if (state.error != null && state.invoices.isEmpty()) {
            ErrorState(
                message = state.error,
                onRetry = { viewModel.onAction(OutletInvoiceAction.LoadInvoices) }
            )
        } else if (state.filteredInvoices.isEmpty() && !state.isLoading) {
            val hasActiveFilter = state.selectedFilter != "All" || state.searchQuery.isNotBlank()
            EmptyState(
                icon = Icons.Default.Receipt,
                title = if (hasActiveFilter) "No invoices match your filter" else "No invoices yet",
                subtitle = if (hasActiveFilter) "Try adjusting your search or filter"
                else "Your uploaded invoices will appear here"
            )
        } else {
            val lazyListState = rememberLazyListState()

            // Infinite scroll
            LaunchedEffect(lazyListState) {
                snapshotFlow {
                    val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    val totalItems = lazyListState.layoutInfo.totalItemsCount
                    lastVisibleItem >= totalItems - 3 && totalItems > 0
                }.distinctUntilChanged().collect { shouldLoad ->
                    if (shouldLoad) {
                        viewModel.onAction(OutletInvoiceAction.LoadMore)
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true },
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 80.dp
                    )
                ) {
                    itemsIndexed(state.filteredInvoices, key = { _, invoice -> invoice.id }) { index, invoice ->
                        StaggeredAnimatedItem(index = index) {
                            InvoiceCard(
                                invoice = invoice,
                                onClick = { onViewInvoiceDetails(invoice.id) },
                                actionableStatuses = emptySet()
                            )
                        }
                    }

                    // Loading more indicator — always present to keep item count stable
                    item(key = "loading_more") {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = state.isLoadingMore,
                            enter = androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.fadeOut()
                        ) {
                            LoadingMoreIndicator()
                        }
                    }

                    // End of list indicator
                    item(key = "end_of_list") {
                        if (state.isLastPage && state.filteredInvoices.isNotEmpty() && !state.isLoading) {
                            EndOfListIndicator(itemCount = state.totalElements)
                        }
                    }
                }
            }
        }
    }
}

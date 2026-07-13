package com.siteflow.signature.cso.invoices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.cso.invoices.data.InvoiceStatus
import com.siteflow.signature.cso.invoices.domain.InvoiceAction
import com.siteflow.signature.cso.invoices.domain.InvoiceViewModel
import com.siteflow.signature.core.presentation.components.SignatureFilterChipRow
import com.siteflow.signature.core.presentation.components.SignatureListHeader
import com.siteflow.signature.core.presentation.components.SignatureTextField
import com.siteflow.signature.core.presentation.components.state.EmptyState
import com.siteflow.signature.core.presentation.components.state.EndOfListIndicator
import com.siteflow.signature.core.presentation.components.state.LoadingMoreIndicator
import com.siteflow.signature.core.presentation.components.state.InvoiceListSkeleton
import com.siteflow.signature.core.presentation.components.state.ErrorState
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import kotlinx.coroutines.delay
import org.koin.compose.koinInject


/**
 * Invoice List Screen — mirrors the ASE Dashboard layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    initialFilter: String? = null,
    onViewInvoiceDetails: (String) -> Unit = {},
    viewModel: InvoiceViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    val filters = listOf("All", "Pending", "Approved", "Rejected")

    LaunchedEffect(Unit) {
        viewModel.onAction(InvoiceAction.LoadInvoices)
        if (initialFilter != null) {
            viewModel.onAction(InvoiceAction.FilterSelected(initialFilter))
        }
    }

    // Simulate refresh
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(1200)
            viewModel.onAction(InvoiceAction.LoadInvoices)
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
            SignatureTextField(
                value = state.searchQuery,
                placeholder = "Search outlets, invoices...",
                onValueChange = { viewModel.onAction(InvoiceAction.SearchQueryChanged(it)) },
                leadingIconVector = Icons.Default.Search
            )

            SignatureFilterChipRow(
                filters = filters,
                selectedFilter = state.selectedFilter,
                countForFilter = { filter ->
                    when (filter) {
                        "Pending"  -> state.invoices.count { it.status == InvoiceStatus.SUBMITTED || it.status == InvoiceStatus.PENDING }
                        "Approved" -> state.invoices.count { it.status == InvoiceStatus.APPROVED || it.status == InvoiceStatus.ASE_APPROVED || it.status == InvoiceStatus.ASM_APPROVED || it.status == InvoiceStatus.FINANCE_APPROVED }
                        "Rejected" -> state.invoices.count { it.status == InvoiceStatus.REJECTED }
                        else       -> state.totalElements
                    }
                },
                onFilterSelected = { viewModel.onAction(InvoiceAction.FilterSelected(it)) }
            )

            Spacer(Modifier.height(12.dp))

            val headerCount = if (state.totalElements > 0 && state.filteredInvoices.size < state.totalElements && state.selectedFilter == "All")
                "${state.filteredInvoices.size} of ${state.totalElements}"
            else "${state.filteredInvoices.size}"
            SignatureListHeader(label = "INVOICES ($headerCount)")

            Spacer(Modifier.height(8.dp))
        }


        // Loading state
        if (state.isLoading && state.invoices.isEmpty()) {
            InvoiceListSkeleton()
        } else if (state.error != null && state.invoices.isEmpty()) {
            ErrorState(
                message = state.error,
                onRetry = { viewModel.onAction(InvoiceAction.LoadInvoices) }
            )
        } else if (state.filteredInvoices.isEmpty() && !state.isLoading) {
            val hasActiveFilter = state.selectedFilter != "All" || state.searchQuery.isNotBlank()
            EmptyState(
                icon = Icons.Default.Receipt,
                title = if (hasActiveFilter) "No invoices match your filter" else "No invoices yet",
                subtitle = if (hasActiveFilter) "Try adjusting your search or filter"
                           else "Invoices will appear here once uploaded"
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
                        viewModel.onAction(InvoiceAction.LoadMore)
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
                    items(state.filteredInvoices, key = { invoice -> invoice.id }) { invoice ->
                        // Not wrapped in StaggeredAnimatedItem: it starts each item invisible
                        // (zero height) on entry, which breaks scroll restoration on back-navigation.
                        InvoiceCard(
                            invoice = invoice,
                            onClick = { onViewInvoiceDetails(invoice.id) }
                        )
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
                            EndOfListIndicator(itemCount = state.filteredInvoices.size)
                        }
                    }
                }
            }
        }
    }
}

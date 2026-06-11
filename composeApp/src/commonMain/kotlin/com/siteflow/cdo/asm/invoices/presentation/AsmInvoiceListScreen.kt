package com.siteflow.cdo.asm.invoices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Person
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
import com.siteflow.cdo.ase.invoices.data.InvoiceItem
import com.siteflow.cdo.ase.invoices.data.InvoiceStatus
import com.siteflow.cdo.ase.invoices.presentation.InvoiceCard
import com.siteflow.cdo.asm.invoices.domain.AsmInvoiceAction
import com.siteflow.cdo.asm.invoices.domain.AsmInvoiceViewModel
import com.siteflow.cdo.core.presentation.components.CdoFilterChipRow
import com.siteflow.cdo.core.presentation.components.CdoListHeader
import com.siteflow.cdo.core.presentation.components.CdoTextField
import com.siteflow.cdo.core.presentation.components.state.EmptyState
import com.siteflow.cdo.core.presentation.components.state.EndOfListIndicator
import com.siteflow.cdo.core.presentation.components.state.LoadingMoreIndicator
import com.siteflow.cdo.core.presentation.components.state.InvoiceListSkeleton
import com.siteflow.cdo.core.presentation.components.state.ErrorState
import com.siteflow.cdo.core.presentation.components.animation.StaggeredAnimatedItem
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import kotlinx.coroutines.delay
import org.koin.compose.koinInject


/**
 * ASM Invoice List Screen — shows invoices submitted by team ASEs
 * that require L2 approval.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsmInvoiceListScreen(
    initialFilter: String? = null,
    onViewInvoiceDetails: (String) -> Unit = {},
    viewModel: AsmInvoiceViewModel = koinInject()
) {
    LaunchedEffect(Unit) {
        viewModel.onAction(AsmInvoiceAction.LoadInvoices)
    }

    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(initialFilter ?: "Pending L2 Review") }
    var isRefreshing by remember { mutableStateOf(false) }

    val filters = listOf("All", "Pending L2 Review", "Approved", "Rejected")

    val filteredInvoices = remember(state.invoices, searchQuery, selectedFilter) {
        state.invoices.filter { invoice ->
            val matchesSearch = searchQuery.isBlank() ||
                    invoice.outletName.contains(searchQuery, ignoreCase = true) ||
                    invoice.location.contains(searchQuery, ignoreCase = true) ||
                    invoice.submittedByAse.contains(searchQuery, ignoreCase = true) ||
                    invoice.id.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "Pending L2 Review" -> invoice.status == InvoiceStatus.ASE_APPROVED
                "Approved" -> invoice.status == InvoiceStatus.ASM_APPROVED || invoice.status == InvoiceStatus.FINANCE_APPROVED || invoice.status == InvoiceStatus.PAID
                "Rejected" -> invoice.status == InvoiceStatus.REJECTED
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(1200)
            isRefreshing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))

            CdoTextField(
                value = searchQuery,
                placeholder = "Search invoices or ASEs...",
                onValueChange = { searchQuery = it },
                leadingIconVector = Icons.Default.Search
            )

            CdoFilterChipRow(
                filters = filters,
                selectedFilter = selectedFilter,
                countForFilter = { filter ->
                    when (filter) {
                        "Pending L2 Review" -> state.invoices.count { it.status == InvoiceStatus.ASE_APPROVED }
                        "Approved"          -> state.invoices.count { it.status == InvoiceStatus.ASM_APPROVED || it.status == InvoiceStatus.FINANCE_APPROVED || it.status == InvoiceStatus.PAID }
                        "Rejected"          -> state.invoices.count { it.status == InvoiceStatus.REJECTED }
                        else                -> state.totalElements
                    }
                },
                onFilterSelected = { selectedFilter = it }
            )

            Spacer(Modifier.height(12.dp))

            val headerCount = if (state.totalElements > 0 && filteredInvoices.size < state.totalElements && selectedFilter == "All")
                "${filteredInvoices.size} of ${state.totalElements}"
            else "${filteredInvoices.size}"
            CdoListHeader(label = "INVOICES ($headerCount)", trailing = null)

            Spacer(Modifier.height(8.dp))
        }


        // Loading state
        if (state.isLoading && state.invoices.isEmpty()) {
            InvoiceListSkeleton()
        } else if (state.error != null && state.invoices.isEmpty()) {
            ErrorState(
                message = state.error,
                onRetry = { viewModel.onAction(AsmInvoiceAction.LoadInvoices) }
            )
        } else if (filteredInvoices.isEmpty()) {
            val hasActiveFilter = selectedFilter != "All" || searchQuery.isNotBlank()
            EmptyState(
                icon = Icons.Default.Receipt,
                title = if (hasActiveFilter) "No invoices match your filter"
                        else "No team invoices pending review",
                subtitle = if (hasActiveFilter) "Try changing the filter chips"
                           else "Invoices approved by your ASEs will appear here for L2 review"
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
                        viewModel.onAction(AsmInvoiceAction.LoadMore)
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 80.dp
                    )
                ) {
                    itemsIndexed(filteredInvoices, key = { _, invoice -> invoice.id }) { index, invoice ->
                        StaggeredAnimatedItem(index = index) {
                            AsmInvoiceCard(
                                invoice = invoice,
                                onClick = { onViewInvoiceDetails(invoice.id) }
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
                        if (state.isLastPage && filteredInvoices.isNotEmpty() && !state.isLoading) {
                            EndOfListIndicator(itemCount = state.totalElements)
                        }
                    }
                }
            }
        }
    }
}

/**
 * ASM variant of InvoiceCard that prepends the "Submitted By" ASE badge.
 */
@Composable
private fun AsmInvoiceCard(
    invoice: InvoiceItem,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        if (invoice.submittedByAse.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AppColors.TextTertiary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Submitted by ${invoice.submittedByAse}",
                    style = AppTypography.Caption.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = AppColors.TextTertiary
                )
            }
        }

        // Reuse shared invoice card — ASM can act on ASE_APPROVED invoices
        InvoiceCard(
            invoice = invoice,
            onClick = onClick,
            actionableStatuses = setOf(InvoiceStatus.ASE_APPROVED)
        )
    }
}

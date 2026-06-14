package com.siteflow.signature.cso.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.dashboard.domain.AssetComplianceFilter
import com.siteflow.signature.cso.dashboard.domain.CsoDashboardViewModel
import com.siteflow.signature.core.domain.RoleManager
import com.siteflow.signature.core.domain.UserRole
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.components.SignatureFilterChipRow
import com.siteflow.signature.core.presentation.components.SignatureListHeader
import com.siteflow.signature.core.presentation.components.SignatureTextField
import com.siteflow.signature.core.presentation.components.state.EmptyState
import com.siteflow.signature.core.presentation.components.state.EndOfListIndicator
import com.siteflow.signature.core.presentation.components.state.LoadingMoreIndicator
import com.siteflow.signature.core.presentation.components.state.OutletListSkeleton
import com.siteflow.signature.core.presentation.components.state.ErrorState
import com.siteflow.signature.core.presentation.components.animation.StaggeredAnimatedItem
import kotlinx.coroutines.flow.drop
import org.koin.compose.koinInject


/**
 * ASE Dashboard — pure content screen.
 * Top bar and FAB are managed centrally by AppNavHost / AppScaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsoDashboardScreen(
    initialFilter: String? = null,
    onViewDetails: (OutletItem) -> Unit = {},
                onContinueOnboarding: (outletId: String, step: Int) -> Unit = { _, _ -> },
    needsRefresh: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    viewModel: CsoDashboardViewModel = koinInject()
) {
    val dashboardState by viewModel.state.collectAsState()

    // Always refresh when this screen enters composition (fresh login, back navigation, etc.)
    LaunchedEffect(Unit) {
        viewModel.loadOutlets()
    }

    // Also refresh when explicitly flagged from navigation (e.g. after onboarding)
    LaunchedEffect(needsRefresh) {
        if (needsRefresh) {
            viewModel.refresh()
            onRefreshConsumed()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(initialFilter ?: "All") }

    val filters = listOf("All", "Draft", "Pending", "Submitted", "Verified", "Rejected")

    // Filter outlets based on search + filter chip
    val filteredOutlets = remember(searchQuery, selectedFilter, dashboardState.outlets) {
        viewModel.getFilteredOutlets(searchQuery, selectedFilter)
    }

    // Debounced search tracking — fires 600ms after user stops typing
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            kotlinx.coroutines.delay(600)
            viewModel.trackOutletSearchUsed(searchQuery.length, filteredOutlets.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // Non-scrollable: Search + Stats + Filters
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))

            // Search Bar
            SignatureTextField(
                value = searchQuery,
                placeholder = "Search outlets...",
                onValueChange = { searchQuery = it },
                leadingIconVector = Icons.Default.Search
            )

            // Key on outlets to recompose chip counts when data loads
            key(dashboardState.outlets.size) {
                SignatureFilterChipRow(
                    filters = filters,
                    selectedFilter = selectedFilter,
                    countForFilter = { filter -> viewModel.countForFilter(filter) },
                    onFilterSelected = { filter ->
                        selectedFilter = filter
                        viewModel.trackOutletFilterSelected(filter)
                    }
                )
            }

            // CSO "to request" pills — server-side compliance filter (cooler / branding).
            if (CsoDashboardViewModel.ASSET_FILTERS_ENABLED &&
                RoleManager.currentRole.value == UserRole.CSO) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssetRequestPill(
                        label = "Cooler to request",
                        count = dashboardState.coolerToRequestCount,
                        selected = dashboardState.assetFilter == AssetComplianceFilter.COOLER_NOT_REQUESTED,
                        onClick = {
                            viewModel.setAssetFilter(
                                if (dashboardState.assetFilter == AssetComplianceFilter.COOLER_NOT_REQUESTED)
                                    AssetComplianceFilter.NONE else AssetComplianceFilter.COOLER_NOT_REQUESTED
                            )
                        }
                    )
                    AssetRequestPill(
                        label = "Branding to request",
                        count = dashboardState.brandingToRequestCount,
                        selected = dashboardState.assetFilter == AssetComplianceFilter.BRANDING_NOT_REQUESTED,
                        onClick = {
                            viewModel.setAssetFilter(
                                if (dashboardState.assetFilter == AssetComplianceFilter.BRANDING_NOT_REQUESTED)
                                    AssetComplianceFilter.NONE else AssetComplianceFilter.BRANDING_NOT_REQUESTED
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            val headerCount = if (dashboardState.totalElements > 0 && filteredOutlets.size < dashboardState.totalElements && selectedFilter == "All")
                "${filteredOutlets.size} of ${dashboardState.totalElements}"
            else "${filteredOutlets.size}"
            SignatureListHeader(label = "OUTLETS ($headerCount)")

            Spacer(Modifier.height(8.dp))
        }


        // Loading state
        if (dashboardState.isLoading && dashboardState.outlets.isEmpty()) {
            OutletListSkeleton()
        } else if (dashboardState.error != null && dashboardState.outlets.isEmpty()) {
            ErrorState(
                message = dashboardState.error,
                onRetry = { viewModel.loadOutlets() }
            )
        } else if (filteredOutlets.isEmpty()) {
            val hasActiveFilter = selectedFilter != "All" || searchQuery.isNotBlank()
            EmptyState(
                icon = Icons.Default.Storefront,
                title = if (hasActiveFilter) "No outlets match your filter" else "No outlets yet",
                subtitle = if (hasActiveFilter) "Try adjusting your search or filter"
                else "Tap + to onboard your first outlet"
            )
        } else {
            val pullToRefreshState = rememberPullToRefreshState()
            val lazyListState = rememberLazyListState()

            // Infinite scroll: load next page when near bottom
            LaunchedEffect(lazyListState) {
                snapshotFlow {
                    val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    val totalItems = lazyListState.layoutInfo.totalItemsCount
                    lastVisibleItem >= totalItems - 3 && totalItems > 0
                }.distinctUntilChanged().collect { shouldLoad ->
                    if (shouldLoad) {
                        viewModel.loadNextPage()
                    }
                }
            }

            // Scroll tracking — fires once per distinct scroll position change
            LaunchedEffect(lazyListState) {
                snapshotFlow { lazyListState.firstVisibleItemIndex }
                    .distinctUntilChanged()
                    .drop(1)
                    .collect { viewModel.trackOutletListScrolled() }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .pullToRefresh(
                        isRefreshing = dashboardState.isRefreshing,
                        state = pullToRefreshState,
                        onRefresh = { viewModel.refresh() }
                    )
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
                    itemsIndexed(filteredOutlets, key = { _, outlet -> outlet.id }) { index, outlet ->
                        StaggeredAnimatedItem(index = index) {
                            OutletCard(
                                outlet = outlet,
                                onContinue = {
                                    if (outlet.isContinuingOnboarding) {
                                        viewModel.trackOutletCardTapped(outlet.id, outlet.status.label, "continue")
                                        onContinueOnboarding(outlet.id, outlet.onboardingStep)
                                    }
                                },
                                onViewDetails = {
                                    viewModel.trackOutletCardTapped(outlet.id, outlet.status.label, "view")
                                    onViewDetails(outlet)
                                }
                            )
                        }
                    }

                    // Loading more indicator — always present to keep item count stable
                    item(key = "loading_more") {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = dashboardState.isLoadingMore,
                            enter = androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.fadeOut()
                        ) {
                            LoadingMoreIndicator()
                        }
                    }

                    // End of list indicator
                    item(key = "end_of_list") {
                        if (dashboardState.isLastPage && filteredOutlets.isNotEmpty() && !dashboardState.isLoading) {
                            EndOfListIndicator(itemCount = dashboardState.totalElements)
                        }
                    }
                }

                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = dashboardState.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

/** A server-backed "to request" pill with a count badge (cooler / branding). */
@Composable
private fun AssetRequestPill(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(if (count > 0) "$label ($count)" else label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppColors.BlueGradientStart,
            selectedLabelColor = Color.White
        )
    )
}

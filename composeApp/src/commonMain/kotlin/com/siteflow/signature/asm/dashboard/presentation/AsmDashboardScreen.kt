package com.siteflow.signature.asm.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import com.siteflow.signature.cso.dashboard.data.ComplianceState
import com.siteflow.signature.cso.dashboard.data.AssetStatus
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.dashboard.data.OutletStatus
import com.siteflow.signature.cso.dashboard.presentation.OutletCard
import com.siteflow.signature.asm.dashboard.domain.AsmDashboardAction
import com.siteflow.signature.asm.dashboard.domain.AsmDashboardViewModel
import com.siteflow.signature.core.presentation.components.SignatureFilterChipRow
import com.siteflow.signature.core.presentation.components.SignatureListHeader
import com.siteflow.signature.core.presentation.components.SignatureTextField
import com.siteflow.signature.core.presentation.components.state.EmptyState
import com.siteflow.signature.core.presentation.components.state.EndOfListIndicator
import com.siteflow.signature.core.presentation.components.state.LoadingMoreIndicator
import com.siteflow.signature.core.presentation.components.state.OutletListSkeleton
import com.siteflow.signature.core.presentation.components.state.ErrorState
import com.siteflow.signature.core.presentation.components.animation.StaggeredAnimatedItem
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import org.koin.compose.koinInject


/**
 * ASM Dashboard — shows outlets from all ASEs under the ASM.
 * Key differences from ASE Dashboard:
 *   - No FAB (ASM doesn't onboard)
 *   - "ASE" badge on each card
 *   - "Verify & Approve" action for ASM_PENDING outlets, "View Details" for all others
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsmDashboardScreen(
    initialFilter: String? = null,
    onReviewOutlet: (OutletItem) -> Unit = {},
    onViewDetails: (OutletItem) -> Unit = {},
    viewModel: AsmDashboardViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(initialFilter ?: "All") }

    // Always refresh when this screen enters composition (fresh login, back navigation, etc.)
    LaunchedEffect(Unit) {
        viewModel.onAction(AsmDashboardAction.LoadOutlets)
    }

    val filters = listOf("All", "In Progress", "Pending Review", "Active", "Suspended", "Rejected")

    val filteredOutlets = remember(state.outlets, searchQuery, selectedFilter) {
        viewModel.getFilteredOutlets(searchQuery, selectedFilter)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // Non-scrollable header: Search + Filters
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))

            SignatureTextField(
                value = searchQuery,
                placeholder = "Search outlets or ASEs...",
                onValueChange = { searchQuery = it },
                leadingIconVector = Icons.Default.Search
            )

            // Key on outlets to recompose chip counts when data loads
            key(state.outlets.size) {
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

            Spacer(Modifier.height(12.dp))

            val headerCount = if (state.totalElements > 0 && filteredOutlets.size < state.totalElements && selectedFilter == "All")
                "${filteredOutlets.size} of ${state.totalElements}"
            else "${filteredOutlets.size}"
            SignatureListHeader(label = "OUTLETS ($headerCount)")

            Spacer(Modifier.height(8.dp))
        }


        // Loading state
        if (state.isLoading && state.outlets.isEmpty()) {
            OutletListSkeleton()
        } else if (state.error != null && state.outlets.isEmpty()) {
            ErrorState(
                message = state.error,
                onRetry = { viewModel.refresh() }
            )
        } else if (filteredOutlets.isEmpty()) {
            val hasActiveFilter = selectedFilter != "All" || searchQuery.isNotBlank()
            EmptyState(
                icon = Icons.Default.Storefront,
                title = if (hasActiveFilter) "No outlets match your filter"
                        else "No outlets from your team yet",
                subtitle = if (hasActiveFilter) "Try adjusting your search or filter"
                           else "Outlets onboarded by your ASEs will appear here"
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
                        viewModel.onAction(AsmDashboardAction.LoadMore)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .pullToRefresh(
                        isRefreshing = state.isRefreshing,
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
                            AsmOutletCard(
                                outlet = outlet,
                                onReview = {
                                    viewModel.trackOutletReviewViewed(outlet.id, outlet.status.label, outlet.onboardedByAse)
                                    onReviewOutlet(outlet)
                                },
                                onViewDetails = { onViewDetails(outlet) }
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
                        if (state.isLastPage && filteredOutlets.isNotEmpty() && !state.isLoading) {
                            EndOfListIndicator(itemCount = state.totalElements)
                        }
                    }
                }

                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = state.isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

/**
 * ASM variant of OutletCard — adds the "Onboarded by" ASE badge
 * and uses status-based action label inside the card:
 *   - ASM_PENDING: "Verify & Approve →" (amber)
 *   - Otherwise: "View Details →" (blue)
 */
@Composable
private fun AsmOutletCard(
    outlet: OutletItem,
    onReview: () -> Unit,
    onViewDetails: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // ASE attribution badge (above card)
        if (outlet.onboardedByAse.isNotBlank()) {
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
                    text = "Onboarded by ${outlet.onboardedByAse}",
                    style = AppTypography.Caption.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = AppColors.TextTertiary
                )
            }
        }

        val isPending = outlet.status == OutletStatus.ASM_PENDING
        val isCompliancePending = outlet.complianceState == ComplianceState.SUBMITTED
            && outlet.assetStatus != AssetStatus.VERIFIED

        val isOnboarding = outlet.isContinuingOnboarding  // DRAFT_* or AGREEMENT_PENDING

        val (actionLabel, actionColor) = when {
            isPending -> "Verify & Approve →" to Color(0xFFF59E0B)
            isCompliancePending -> "Verify Compliance →" to Color(0xFFF59E0B)
            isOnboarding -> "Onboarding in Progress" to AppColors.PlaceholderTextColor
            else -> "View Details →" to AppColors.BlueGradientStart
        }

        OutletCard(
            outlet = outlet,
            onViewDetails = {
                if (!isOnboarding) {
                    if (isPending || isCompliancePending) onReview() else onViewDetails()
                }
            },
            actionLabel = actionLabel,
            actionColor = actionColor
        )
    }
}

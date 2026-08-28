package com.siteflow.signature.cso.dashboard.domain

import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.dashboard.data.OutletStatus
import com.siteflow.signature.cso.onboarding.data.OutletRepository
import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CsoDashboardState(
    val outlets: List<OutletItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Int = 0,
    val isLastPage: Boolean = true,
    val savedScrollIndex: Int = 0,
    val savedScrollOffset: Int = 0,
    val savedFilter: String = "All"
)

class CsoDashboardViewModel(
    private val scope: CoroutineScope,
    private val outletRepository: OutletRepository,
    private val analytics: AnalyticsTracker,
) {

    companion object {
        private const val PAGE_SIZE = 20
    }

    private val _state = MutableStateFlow(CsoDashboardState())
    val state = _state.asStateFlow()

    init {
        loadOutlets()
    }

    fun loadOutlets() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            outletRepository.getOutlets(page = 0, size = PAGE_SIZE, showLoader = false)
                .onSuccess { page ->
                    val items = page.content.map { OutletItem.fromDto(it) }
                        .sortedByDescending { it.updatedAtRaw }
                    _state.update {
                        it.copy(
                            outlets = items,
                            isLoading = false,
                            isRefreshing = false,
                            currentPage = page.page,
                            totalPages = page.totalPages,
                            totalElements = page.totalElements,
                            isLastPage = page.last
                        )
                    }
                    analytics.track(AnalyticsEvent.ASEEvent.DashboardLoaded(
                        totalOutlets = page.totalElements,
                        inProgress = items.count { it.isContinuingOnboarding },
                        asmPending = items.count { it.status == OutletStatus.ASM_PENDING },
                        pendingInvoices = 0
                    ))
                    analytics.track(AnalyticsEvent.ASEEvent.OutletListViewed(
                        outletCount = page.totalElements
                    ))
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun loadNextPage() {
        val current = _state.value
        if (current.isLastPage || current.isLoadingMore || current.isLoading) return

        scope.launch {
            _state.update { it.copy(isLoadingMore = true) }

            val nextPage = current.currentPage + 1
            outletRepository.getOutlets(page = nextPage, size = PAGE_SIZE, showLoader = false)
                .onSuccess { page ->
                    val newItems = page.content.map { OutletItem.fromDto(it) }
                    // Ensure loading indicator is visible for at least 300ms
                    kotlinx.coroutines.delay(300)
                    _state.update {
                        val updatedItems = (it.outlets + newItems).distinctBy { item -> item.id }
                            .sortedByDescending { item -> item.updatedAtRaw }
                        it.copy(
                            outlets = updatedItems,
                            isLoadingMore = false,
                            currentPage = page.page,
                            totalPages = page.totalPages,
                            totalElements = page.totalElements,
                            isLastPage = page.last
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        loadOutlets()
    }

    /**
     * Returns filtered outlets based on search query and filter chip.
     */
    fun getFilteredOutlets(
        searchQuery: String,
        selectedFilter: String
    ): List<OutletItem> {
        return _state.value.outlets.filter { outlet ->
            val matchesSearch = searchQuery.isBlank() ||
                    outlet.name.contains(searchQuery, ignoreCase = true) ||
                    outlet.location.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Draft" -> outlet.isContinuingOnboarding
                "Pending" -> outlet.status == OutletStatus.ASM_PENDING
                "Submitted" -> outlet.status == OutletStatus.ASM_APPROVED
                "Verified" -> outlet.status == OutletStatus.ONBOARDED
                "Rejected" -> outlet.status == OutletStatus.ASM_REJECTED
                else -> outlet.status.label.equals(selectedFilter, ignoreCase = true)
            }
            matchesSearch && matchesFilter
        }
    }

    fun saveScrollPosition(index: Int, offset: Int) {
        _state.update { it.copy(savedScrollIndex = index, savedScrollOffset = offset) }
    }

    fun saveFilter(filter: String) {
        _state.update { it.copy(savedFilter = filter) }
    }

    fun trackOutletFilterSelected(filter: String) {
        analytics.track(AnalyticsEvent.ASEEvent.OutletFilterSelected(filter = filter))
    }

    fun trackOutletSearchUsed(queryLength: Int, resultsCount: Int) {
        analytics.track(AnalyticsEvent.ASEEvent.OutletSearchUsed(queryLength = queryLength, resultsCount = resultsCount))
    }

    fun trackOutletCardTapped(outletId: String, outletStatus: String, action: String) {
        analytics.track(AnalyticsEvent.ASEEvent.OutletCardTapped(outletId = outletId, outletStatus = outletStatus, action = action))
    }

    fun trackOutletListScrolled() {
        analytics.track(AnalyticsEvent.ASEEvent.OutletListScrolled)
    }

    fun countForFilter(filter: String): Int {
        val outlets = _state.value.outlets
        return when (filter) {
            "Draft" -> outlets.count { it.isContinuingOnboarding }
            "Pending" -> outlets.count { it.status == OutletStatus.ASM_PENDING }
            "Submitted" -> outlets.count { it.status == OutletStatus.ASM_APPROVED }
            "Verified" -> outlets.count { it.status == OutletStatus.ONBOARDED }
            "Rejected" -> outlets.count { it.status == OutletStatus.ASM_REJECTED }
            else -> _state.value.totalElements
        }
    }
}

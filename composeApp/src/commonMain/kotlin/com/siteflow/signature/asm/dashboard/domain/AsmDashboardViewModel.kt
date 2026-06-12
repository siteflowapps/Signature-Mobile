package com.siteflow.signature.asm.dashboard.domain


import com.siteflow.signature.cso.dashboard.data.OutletItem
import com.siteflow.signature.cso.dashboard.data.OutletStatus
import com.siteflow.signature.cso.onboarding.data.OutletRepository
import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.core.presentation.components.toast.GlobalToastHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * MVI Contract for the ASM Dashboard.
 */

sealed interface AsmDashboardAction {
    data object LoadOutlets : AsmDashboardAction
    data object Refresh : AsmDashboardAction
    data object LoadMore : AsmDashboardAction
    data class ApproveOutlet(val outletId: String) : AsmDashboardAction
    data class RejectOutlet(val outletId: String, val reason: String) : AsmDashboardAction
    data class VerifyCompliance(val complianceId: String) : AsmDashboardAction
}

data class AsmDashboardState(
    val outlets: List<OutletItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isApproving: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Int = 0,
    val isLastPage: Boolean = true
)

sealed interface AsmDashboardEvent {
    data class ShowMessage(val message: String) : AsmDashboardEvent
}

/**
 * ASM Dashboard ViewModel.
 *
 * Manages the list of outlets onboarded by the ASM's team of ASEs.
 * Uses the same getOutlets() API as the ASE dashboard.
 * ASM can approve or reject outlets that are ASM_PENDING.
 */
class AsmDashboardViewModel(
    private val scope: CoroutineScope,
    private val outletRepository: OutletRepository,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<AsmDashboardState, AsmDashboardAction, AsmDashboardEvent>(
    AsmDashboardState()
) {

    companion object {
        private const val PAGE_SIZE = 20
    }

    init {
        loadOutlets()
    }

    override fun onAction(action: AsmDashboardAction) {
        when (action) {
            AsmDashboardAction.LoadOutlets -> loadOutlets()
            AsmDashboardAction.Refresh -> refresh()
            AsmDashboardAction.LoadMore -> loadNextPage()
            is AsmDashboardAction.ApproveOutlet -> approveOutlet(action.outletId)
            is AsmDashboardAction.RejectOutlet -> rejectOutlet(action.outletId, action.reason)
            is AsmDashboardAction.VerifyCompliance -> verifyCompliance(action.complianceId)
        }
    }

    private fun loadOutlets() {
        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            outletRepository.getOutlets(page = 0, size = PAGE_SIZE)
                .onSuccess { page ->
                    val items = page.content.map { OutletItem.fromDto(it) }
                        .sortedByDescending { it.updatedAtRaw }
                    updateState {
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
                    analytics.track(AnalyticsEvent.ASMEvent.DashboardLoaded(
                        totalOutlets = page.totalElements,
                        inProgress = items.count { it.isContinuingOnboarding },
                        asmPending = items.count { it.status == OutletStatus.ASM_PENDING }
                    ))
                    analytics.track(AnalyticsEvent.ASMEvent.OutletListViewed(outletCount = page.totalElements))
                }
                .onError { error ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    private fun loadNextPage() {
        val current = _state.value
        if (current.isLastPage || current.isLoadingMore || current.isLoading) return

        scope.launch {
            updateState { it.copy(isLoadingMore = true) }

            val nextPage = current.currentPage + 1
            outletRepository.getOutlets(page = nextPage, size = PAGE_SIZE)
                .onSuccess { page ->
                    val newItems = page.content.map { OutletItem.fromDto(it) }
                    // Ensure loading indicator is visible for at least 300ms
                    kotlinx.coroutines.delay(300)
                    updateState {
                        val updatedItems = (it.outlets + newItems).distinctBy { item -> item.id }.sortedByDescending { item -> item.updatedAtRaw }
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
                    updateState {
                        it.copy(
                            isLoadingMore = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun refresh() {
        updateState { it.copy(isRefreshing = true) }
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
                    outlet.location.contains(searchQuery, ignoreCase = true) ||
                    outlet.onboardedByAse.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "In Progress" -> outlet.isContinuingOnboarding
                "Pending Review" -> outlet.status == OutletStatus.ASM_PENDING
                "Active" -> outlet.status == OutletStatus.ASM_APPROVED || outlet.status == OutletStatus.ONBOARDED
                "Suspended" -> outlet.status == OutletStatus.ASM_REJECTED
                "Rejected" -> outlet.status == OutletStatus.ASM_REJECTED
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    fun trackOutletFilterSelected(filter: String) {
        analytics.track(AnalyticsEvent.ASMEvent.OutletFilterSelected(filter = filter))
    }

    fun trackOutletReviewViewed(outletId: String, outletStatus: String, onboardedByAse: String) {
        analytics.track(AnalyticsEvent.ASMEvent.OutletReviewViewed(
            outletId = outletId,
            outletStatus = outletStatus,
            onboardedByAse = onboardedByAse
        ))
    }

    fun countForFilter(filter: String): Int {
        val outlets = _state.value.outlets
        return when (filter) {
            "In Progress" -> outlets.count { it.isContinuingOnboarding }
            "Pending Review" -> outlets.count { it.status == OutletStatus.ASM_PENDING }
            "Active" -> outlets.count { it.status == OutletStatus.ASM_APPROVED || it.status == OutletStatus.ONBOARDED }
            "Suspended" -> outlets.count { it.status == OutletStatus.ASM_REJECTED }
            "Rejected" -> outlets.count { it.status == OutletStatus.ASM_REJECTED }
            else -> _state.value.totalElements
        }
    }

    private fun approveOutlet(outletId: String) {
        scope.launch {
            updateState { it.copy(isApproving = true) }

            outletRepository.asmApprove(outletId)
                .onSuccess {
                    updateState { it.copy(isApproving = false) }
                    GlobalToastHandler.showSuccess("Outlet approved successfully")
                    analytics.track(AnalyticsEvent.ASMEvent.OutletApproved(outletId = outletId, timeOnReviewMs = 0L))
                    emitEvent(AsmDashboardEvent.ShowMessage("Outlet approved successfully"))
                    loadOutlets()
                }
                .onError { error ->
                    updateState { it.copy(isApproving = false) }
                    GlobalToastHandler.showError(error.message)
                    emitEvent(AsmDashboardEvent.ShowMessage(error.message))
                }
        }
    }

    private fun rejectOutlet(outletId: String, reason: String) {
        scope.launch {
            updateState { it.copy(isApproving = true) }

            outletRepository.asmReject(outletId, reason)
                .onSuccess {
                    updateState { it.copy(isApproving = false) }
                    GlobalToastHandler.showSuccess("Outlet rejected")
                    analytics.track(AnalyticsEvent.ASMEvent.OutletRejected(outletId = outletId, rejectionReason = reason, timeOnReviewMs = 0L))
                    emitEvent(AsmDashboardEvent.ShowMessage("Outlet rejected"))
                    loadOutlets()
                }
                .onError { error ->
                    updateState { it.copy(isApproving = false) }
                    GlobalToastHandler.showError(error.message)
                    emitEvent(AsmDashboardEvent.ShowMessage(error.message))
                }
        }
    }

    private fun verifyCompliance(complianceId: String) {
        scope.launch {
            updateState { it.copy(isApproving = true) }

            outletRepository.verifyCompliance(complianceId)
                .onSuccess {
                    updateState { it.copy(isApproving = false) }
                    GlobalToastHandler.showSuccess("Compliance verified")
                    emitEvent(AsmDashboardEvent.ShowMessage("Compliance verified"))
                    loadOutlets()
                }
                .onError { error ->
                    updateState { it.copy(isApproving = false) }
                    GlobalToastHandler.showError(error.message)
                    emitEvent(AsmDashboardEvent.ShowMessage(error.message))
                }
        }
    }
}

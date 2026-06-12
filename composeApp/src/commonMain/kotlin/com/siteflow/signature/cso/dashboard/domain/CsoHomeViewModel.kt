package com.siteflow.signature.cso.dashboard.domain

import com.siteflow.signature.cso.onboarding.data.OutletApi
import com.siteflow.signature.core.analytics.AnalyticsEvent
import com.siteflow.signature.core.analytics.AnalyticsTracker
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.outlet.dashboard.data.DashboardApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for the ASE Home Dashboard.
 * Fetches dashboard stats + classification slabs from APIs.
 */
class CsoHomeViewModel(
    private val dashboardApi: DashboardApi,
    private val outletApi: OutletApi,
    private val analytics: AnalyticsTracker
) : BaseViewModel<CsoHomeState, CsoHomeAction, CsoHomeEvent>(
    initialState = CsoHomeState()
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onAction(action: CsoHomeAction) {
        when (action) {
            CsoHomeAction.LoadDashboard -> loadDashboard()
            CsoHomeAction.TotalOutletsClicked -> {
                analytics.track(AnalyticsEvent.ASEEvent.DashboardCardTapped(cardName = "Total Outlets"))
                emitEvent(CsoHomeEvent.NavigateToOutletsAll)
            }
            CsoHomeAction.InProgressOutletsClicked -> {
                analytics.track(AnalyticsEvent.ASEEvent.DashboardCardTapped(cardName = "In Progress"))
                emitEvent(CsoHomeEvent.NavigateToOutletsInProgress)
            }
            CsoHomeAction.AsmPendingOutletsClicked -> {
                analytics.track(AnalyticsEvent.ASEEvent.DashboardCardTapped(cardName = "ASM Pending"))
                emitEvent(CsoHomeEvent.NavigateToOutletsAsmPending)
            }
            CsoHomeAction.PendingInvoicesClicked -> {
                analytics.track(AnalyticsEvent.ASEEvent.DashboardCardTapped(cardName = "Pending Invoices"))
                emitEvent(CsoHomeEvent.NavigateToInvoicesPending)
            }
        }
    }

    private fun loadDashboard() {
        updateState { it.copy(isLoading = true, isSlabsLoading = true) }

        // Fetch dashboard stats
        scope.launch {
            dashboardApi.getDashboard()
                .onSuccess { response ->
                    updateState { it.copy(dashboard = response.data, isLoading = false) }
                }
                .onError {
                    println("[CsoHomeVM] Dashboard API error")
                    updateState { it.copy(isLoading = false) }
                }
        }

        // Fetch slabs
        scope.launch {
            outletApi.getSlabs()
                .onSuccess { response ->
                    val slabs = response.data?.content ?: emptyList()
                    updateState { it.copy(slabs = slabs, isSlabsLoading = false) }
                    if (slabs.isNotEmpty()) {
                        analytics.track(AnalyticsEvent.ASEEvent.SlabChartViewed)
                    }
                }
                .onError {
                    println("[CsoHomeVM] Slabs API error")
                    updateState { it.copy(isSlabsLoading = false) }
                }
        }
    }
}

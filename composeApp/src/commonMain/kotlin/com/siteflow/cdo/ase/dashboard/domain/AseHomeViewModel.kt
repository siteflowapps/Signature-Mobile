package com.siteflow.cdo.ase.dashboard.domain

import com.siteflow.cdo.ase.onboarding.data.OutletApi
import com.siteflow.cdo.core.analytics.AnalyticsEvent
import com.siteflow.cdo.core.analytics.AnalyticsTracker
import com.siteflow.cdo.core.data.networking.result.onError
import com.siteflow.cdo.core.data.networking.result.onSuccess
import com.siteflow.cdo.core.presentation.BaseViewModel
import com.siteflow.cdo.outlet.dashboard.data.DashboardApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for the ASE Home Dashboard.
 * Fetches dashboard stats + classification slabs from APIs.
 */
class AseHomeViewModel(
    private val dashboardApi: DashboardApi,
    private val outletApi: OutletApi,
    private val analytics: AnalyticsTracker
) : BaseViewModel<AseHomeState, AseHomeAction, AseHomeEvent>(
    initialState = AseHomeState()
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onAction(action: AseHomeAction) {
        when (action) {
            AseHomeAction.LoadDashboard -> loadDashboard()
            AseHomeAction.TotalOutletsClicked -> {
                analytics.track(AnalyticsEvent.ASEEvent.DashboardCardTapped(cardName = "Total Outlets"))
                emitEvent(AseHomeEvent.NavigateToOutletsAll)
            }
            AseHomeAction.InProgressOutletsClicked -> {
                analytics.track(AnalyticsEvent.ASEEvent.DashboardCardTapped(cardName = "In Progress"))
                emitEvent(AseHomeEvent.NavigateToOutletsInProgress)
            }
            AseHomeAction.AsmPendingOutletsClicked -> {
                analytics.track(AnalyticsEvent.ASEEvent.DashboardCardTapped(cardName = "ASM Pending"))
                emitEvent(AseHomeEvent.NavigateToOutletsAsmPending)
            }
            AseHomeAction.PendingInvoicesClicked -> {
                analytics.track(AnalyticsEvent.ASEEvent.DashboardCardTapped(cardName = "Pending Invoices"))
                emitEvent(AseHomeEvent.NavigateToInvoicesPending)
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
                    println("[AseHomeVM] Dashboard API error")
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
                    println("[AseHomeVM] Slabs API error")
                    updateState { it.copy(isSlabsLoading = false) }
                }
        }
    }
}

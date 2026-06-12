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
 * ViewModel for the ASM Home Dashboard.
 * Fetches dashboard stats + classification slabs from APIs.
 */
class AsmHomeViewModel(
    private val dashboardApi: DashboardApi,
    private val outletApi: OutletApi,
    private val analytics: AnalyticsTracker
) : BaseViewModel<AsmHomeState, AsmHomeAction, AsmHomeEvent>(
    initialState = AsmHomeState()
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onAction(action: AsmHomeAction) {
        when (action) {
            AsmHomeAction.LoadDashboard -> loadDashboard()
            AsmHomeAction.TotalAsesClicked -> {
                analytics.track(AnalyticsEvent.ASMEvent.MyTeamTapped)
                emitEvent(AsmHomeEvent.NavigateToMyAses)
            }
            AsmHomeAction.TotalOutletsClicked -> {
                analytics.track(AnalyticsEvent.ASMEvent.DashboardCardTapped(cardName = "Total Outlets"))
                emitEvent(AsmHomeEvent.NavigateToOutletsAll)
            }
            AsmHomeAction.ActiveOutletsClicked -> {
                analytics.track(AnalyticsEvent.ASMEvent.DashboardCardTapped(cardName = "Active Outlets"))
                emitEvent(AsmHomeEvent.NavigateToOutletsActive)
            }
            AsmHomeAction.InProgressOutletsClicked -> {
                analytics.track(AnalyticsEvent.ASMEvent.DashboardCardTapped(cardName = "In Progress"))
                emitEvent(AsmHomeEvent.NavigateToOutletsInProgress)
            }
            AsmHomeAction.SuspendedOutletsClicked -> {
                analytics.track(AnalyticsEvent.ASMEvent.DashboardCardTapped(cardName = "Suspended"))
                emitEvent(AsmHomeEvent.NavigateToOutletsSuspended)
            }
            AsmHomeAction.AsmPendingOutletsClicked -> {
                analytics.track(AnalyticsEvent.ASMEvent.DashboardCardTapped(cardName = "ASM Pending"))
                emitEvent(AsmHomeEvent.NavigateToOutletsAsmPending)
            }
            AsmHomeAction.PendingInvoicesClicked -> {
                analytics.track(AnalyticsEvent.ASMEvent.DashboardCardTapped(cardName = "Pending Invoices"))
                emitEvent(AsmHomeEvent.NavigateToInvoicesPending)
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
                    println("[AsmHomeVM] Dashboard API error")
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
                    println("[AsmHomeVM] Slabs API error")
                    updateState { it.copy(isSlabsLoading = false) }
                }
        }
    }
}

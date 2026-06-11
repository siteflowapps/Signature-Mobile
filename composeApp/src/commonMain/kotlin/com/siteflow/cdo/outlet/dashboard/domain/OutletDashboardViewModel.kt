package com.siteflow.cdo.outlet.dashboard.domain

import com.siteflow.cdo.ase.onboarding.data.OutletApi
import com.siteflow.cdo.core.analytics.AnalyticsEvent
import com.siteflow.cdo.core.analytics.AnalyticsTracker
import com.siteflow.cdo.core.data.networking.result.onError
import com.siteflow.cdo.core.data.networking.result.onSuccess
import com.siteflow.cdo.core.presentation.BaseViewModel
import com.siteflow.cdo.outlet.dashboard.data.DashboardApi
import com.siteflow.cdo.outlet.dashboard.data.OutletPayoutSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for the Outlet Dashboard (Home tab).
 * Fetches dashboard stats + classification slabs from APIs.
 */
class OutletDashboardViewModel(
    private val outletApi: OutletApi,
    private val dashboardApi: DashboardApi,
    private val analytics: AnalyticsTracker,
) : BaseViewModel<OutletDashboardState, OutletDashboardAction, OutletDashboardEvent>(
    initialState = OutletDashboardState()
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onAction(action: OutletDashboardAction) {
        when (action) {
            OutletDashboardAction.LoadDashboard -> loadDashboard()
            OutletDashboardAction.UploadInvoiceClicked -> {
                analytics.track(AnalyticsEvent.OutletEvent.UploadInvoiceTapped(source = "dashboard"))
                emitEvent(OutletDashboardEvent.NavigateToUploadInvoice)
            }
            OutletDashboardAction.ViewAllInvoicesClicked -> {
                analytics.track(AnalyticsEvent.OutletEvent.DashboardCardTapped(cardName = "view_all_invoices"))
                emitEvent(OutletDashboardEvent.NavigateToInvoiceList)
            }
            OutletDashboardAction.TotalInvoicesClicked -> {
                analytics.track(AnalyticsEvent.OutletEvent.DashboardCardTapped(cardName = "total_invoices"))
                emitEvent(OutletDashboardEvent.NavigateToInvoiceListAll)
            }
            OutletDashboardAction.PendingInvoicesClicked -> {
                analytics.track(AnalyticsEvent.OutletEvent.DashboardCardTapped(cardName = "pending_invoices"))
                emitEvent(OutletDashboardEvent.NavigateToInvoiceListPending)
            }
            is OutletDashboardAction.DismissNotification -> {
                analytics.track(AnalyticsEvent.OutletEvent.NotificationDismissed(notificationId = action.id))
                updateState {
                    it.copy(notifications = it.notifications.filter { n -> n.id != action.id })
                }
            }
            is OutletDashboardAction.ViewNotification -> {
                analytics.track(AnalyticsEvent.OutletEvent.NotificationViewed(
                    notificationId = action.id,
                    notificationType = action.notificationType
                ))
            }
            is OutletDashboardAction.RecentInvoiceTapped -> {
                analytics.track(AnalyticsEvent.OutletEvent.RecentInvoiceTapped(
                    invoiceId = action.invoiceId,
                    invoiceStatus = action.invoiceStatus
                ))
                emitEvent(OutletDashboardEvent.NavigateToInvoiceDetail(action.invoiceId))
            }
        }
    }

    private fun loadDashboard() {
        updateState { it.copy(isLoading = true, isSlabsLoading = true) }

        // Fetch dashboard stats
        scope.launch {
            dashboardApi.getDashboard()
                .onSuccess { response ->
                    val data = response.data
                    val payout = OutletPayoutSummary(
                        lastPayoutAmount = "₹0.00",
                        lastPayoutDate = null,
                        totalInvoices = data?.totalInvoices ?: 0,
                        pendingInvoices = data?.submittedInvoices ?: 0
                    )
                    updateState { it.copy(payoutSummary = payout, isLoading = false) }
                    analytics.track(AnalyticsEvent.OutletEvent.DashboardLoaded(
                        totalInvoices = data?.totalInvoices ?: 0,
                        pendingInvoices = data?.submittedInvoices ?: 0
                    ))
                }
                .onError {
                    println("[OutletDashboardVM] Dashboard API error")
                    updateState {
                        it.copy(
                            payoutSummary = OutletPayoutSummary(),
                            isLoading = false
                        )
                    }
                }
        }

        // Fetch slabs
        scope.launch {
            outletApi.getSlabs()
                .onSuccess { response ->
                    val slabs = response.data?.content ?: emptyList()
                    updateState { it.copy(slabs = slabs, isSlabsLoading = false) }
                }
                .onError {
                    println("[OutletDashboardVM] Slabs API error")
                    updateState { it.copy(isSlabsLoading = false) }
                }
        }
    }
}
